
package info.freelibrary.maven;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.exec.environment.EnvironmentUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVReader;
import gov.lanl.adore.djatoka.DjatokaEncodeParam;
import gov.lanl.adore.djatoka.kdu.KduCompressExe;

import info.freelibrary.djatoka.Constants;
import info.freelibrary.util.FileUtils;
import info.freelibrary.util.PairtreeObject;
import info.freelibrary.util.PairtreeRoot;
import info.freelibrary.util.PairtreeUtils;
import info.freelibrary.util.XMLBundleControl;
import info.freelibrary.util.XMLResourceBundle;

/**
 * Ingests content into FreeLib-Djatoka's Pairtree file system.
 * <p/>
 * 
 * @author <a href="mailto:ksclarke@gmail.com">Kevin S. Clarke</a>
 */
@Mojo(name = "ingest")
public class DjatokaIngestMojo extends AbstractMojo {

    private static final String PAIRTREE_FS = "djatoka.jp2.data";

    private static final Logger LOGGER = LoggerFactory.getLogger(DjatokaIngestMojo.class);

    private final XMLResourceBundle BUNDLE = (XMLResourceBundle) ResourceBundle.getBundle("freelib-djatoka_messages",
            new XMLBundleControl());

    private long myMaxSize;

    private DjatokaEncodeParam myParams;

    /**
     * The Maven project directory.
     */
    @Component
    private MavenProject myProject;

    /**
     * The name of a CSV file with images to ingest.
     */
    @Parameter(property = "csv.file")
    private File myCsvFile;

    /**
     * The index of the ID column in the CSV file.
     */
    @Parameter(property = "csv.id", defaultValue = "1")
    private int myCsvIdCol;

    /**
     * The index of the file system path column in the CSV file.
     */
    @Parameter(property = "csv.path", defaultValue = "0")
    private int myCsvPathCol;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        final String ptfs = myProject.getProperties().getProperty(PAIRTREE_FS);

        try {
            final PairtreeRoot pairtree = new PairtreeRoot(new File(ptfs));

            if (myCsvFile != null && myCsvFile.exists()) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(BUNDLE.get("INGEST_CSV", myCsvFile, myCsvPathCol, myCsvIdCol));
                }

                ingestCSVFile(pairtree);
            }

            if (myCsvFile == null) {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn(BUNDLE.get("INGEST_EMPTY"));
                }
            }
        } catch (final FileNotFoundException details) {
            throw new MojoExecutionException(details.getMessage(), details);
        } catch (final IOException details) {
            throw new MojoExecutionException(details.getMessage(), details);
        }
    }

    private void ingestCSVFile(final PairtreeRoot aPairtree) throws IOException, MojoExecutionException {
        CSVReader csvReader = null;
        Pattern jp2Pattern;
        Pattern tifPattern;
        String[] csv;

        try {
            jp2Pattern = Pattern.compile(Constants.JP2_FILE_PATTERN);
            tifPattern = Pattern.compile(Constants.TIFF_FILE_PATTERN);
        } catch (final PatternSyntaxException details) {
            throw new MojoExecutionException(details.getMessage(), details);
        }

        try {
            csvReader = new CSVReader(new FileReader(myCsvFile));

            while ((csv = csvReader.readNext()) != null) {
                if (myCsvIdCol >= csv.length || myCsvPathCol >= csv.length || (myCsvIdCol == myCsvPathCol)) {
                    throw new MojoExecutionException(BUNDLE.get("INGEST_INDEX", myCsvPathCol, myCsvIdCol, csv.length));
                }

                if (jp2Pattern.matcher(csv[myCsvPathCol]).matches()) {
                    final File jp2 = new File(csv[myCsvPathCol]);

                    if (jp2.exists() && jp2.canRead()) {
                        storeJP2(csv[myCsvIdCol], jp2, aPairtree);
                    } else if (LOGGER.isWarnEnabled()) {
                        LOGGER.warn(BUNDLE.get("INGEST_FILE_FAIL", jp2));
                    }
                } else if (tifPattern.matcher(csv[myCsvPathCol]).matches()) {
                    final File tiff = new File(csv[myCsvPathCol]);

                    if (tiff.exists() && tiff.canRead()) {
                        final DjatokaEncodeParam params = getEncodingParams();

                        if (tiff.length() < myMaxSize) {
                            final File jp2 = convert(tiff, params);

                            if (jp2 != null) {
                                storeJP2(csv[myCsvIdCol], jp2, aPairtree);
                            }
                        } else if (LOGGER.isErrorEnabled()) {
                            LOGGER.error(BUNDLE.get("INGEST_MAXSIZE", tiff.length(), myMaxSize));
                        }
                    } else if (LOGGER.isWarnEnabled()) {
                        LOGGER.warn(BUNDLE.get("INGEST_FILE_FAIL", tiff));
                    }
                } else if (LOGGER.isWarnEnabled()) {
                    final String fileName = csv[myCsvPathCol];
                    LOGGER.warn(BUNDLE.get("INGEST_FORMAT_UNKNOWN", fileName));
                }
            }
        } catch (final FileNotFoundException details) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(details.getMessage(), details);
            }
        } finally {
            if (csvReader != null) {
                try {
                    csvReader.close();
                } catch (final IOException details) {
                    if (LOGGER.isErrorEnabled()) {
                        LOGGER.error(details.getMessage(), details);
                    }
                }
            }
        }
    }

    private void storeJP2(final String aID, final File aJP2File, final PairtreeRoot aPairtree) throws IOException {
        final PairtreeObject dir = aPairtree.getObject(aID);
        final String filename = PairtreeUtils.encodeID(aID);
        final File newJP2File = new File(dir, filename);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(BUNDLE.get("INGEST_COPY", aJP2File, newJP2File));
        }

        FileUtils.copy(aJP2File, newJP2File);
    }

    private File convert(final File aSource, final DjatokaEncodeParam aParams) throws IOException,
            MojoExecutionException {
        final File tmpFile = File.createTempFile("djatoka-", Constants.JP2_EXT);
        final Properties properties = myProject.getProperties();
        final String kakadu = properties.getProperty("LD_LIBRARY_PATH");

        if (kakadu == null) {
            throw new MojoExecutionException(BUNDLE.get("INGEST_KAKADU_CFG"));
        }

        final String command =
                KduCompressExe.getKduCompressCommand(aSource.getAbsolutePath(), tmpFile.getAbsolutePath(), myParams)
                        .replaceFirst("^null/", kakadu + "/");

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(BUNDLE.get("INGEST_CONVERSION_COMMAND"), command);
        }

        final DefaultExecuteResultHandler handler = new DefaultExecuteResultHandler();
        final CommandLine cmdLine = CommandLine.parse(command);
        final DefaultExecutor executor = new DefaultExecutor();
        final ExecuteWatchdog watchdog = new ExecuteWatchdog(60000 * 10);
        final ByteArrayOutputStream error = new ByteArrayOutputStream();
        final ByteArrayOutputStream stdOut = new ByteArrayOutputStream();

        @SuppressWarnings("unchecked")
        final Map<String, String> environment = EnvironmentUtils.getProcEnvironment();

        // These are set in the pom.xml file's profile configuration
        environment.put("LD_LIBRARY_PATH", kakadu);
        environment.put("DYLD_LIBRARY_PATH", kakadu);

        executor.setStreamHandler(new PumpStreamHandler(stdOut, error));
        executor.setWatchdog(watchdog);
        executor.execute(cmdLine, environment, handler);

        try {
            handler.waitFor();
        } catch (final InterruptedException details) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(BUNDLE.get("INGEST_INTERRUPTED", aSource, error.toString()));
            }
        }

        if (handler.getExitValue() != 0) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(BUNDLE.get("INGEST_CONVERSION_FAILED", aSource, error.toString()));
            }

            return null;
        } else {
            return tmpFile;
        }
    }

    private DjatokaEncodeParam getEncodingParams() throws IOException {
        if (myParams == null) {
            final String dir = myProject.getBuild().getOutputDirectory();
            final File propFile = new File(dir, Constants.PROPERTIES_FILE);
            final FileInputStream fis = new FileInputStream(propFile);
            final BufferedInputStream bis = new BufferedInputStream(fis);
            final Properties properties = new Properties();
            String maxSize;

            try {
                properties.loadFromXML(bis);
                myParams = new DjatokaEncodeParam(properties);

                // While we're getting settings, let's remember the maxSize
                maxSize = properties.getProperty(Constants.MAX_SIZE, "200");

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(BUNDLE.get("INGEST_CONFIG_MAXSIZE", maxSize));
                }

                myMaxSize = Long.parseLong(maxSize) * 1048576;
            } finally {
                bis.close();
            }
        }

        return myParams;
    }
}
