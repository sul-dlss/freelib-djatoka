
package info.freelibrary.maven;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVReader;
import gov.lanl.adore.djatoka.DjatokaEncodeParam;

import info.freelibrary.djatoka.Constants;
import info.freelibrary.util.FileUtils;
import info.freelibrary.util.PairtreeObject;
import info.freelibrary.util.PairtreeRoot;
import info.freelibrary.util.PairtreeUtils;
import info.freelibrary.util.StringUtils;

/**
 * An abstract class for ingesting content into FreeLib-Djatoka's Pairtree file system.
 * <p/>
 *
 * @author <a href="mailto:ksclarke@gmail.com">Kevin S. Clarke</a>
 */
public abstract class AbstractIngestMojo extends AbstractPairtreeMojo {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractIngestMojo.class);

    private long myMaxSize;

    private DjatokaEncodeParam myParams;

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

        // Sets the Maven loggers' levels (not the levels of loggers used by this plugin)
        MavenUtils.setLogLevels(MavenUtils.ERROR_LOG_LEVEL, MavenUtils.getMavenLoggers());

        try {
            final PairtreeRoot pairtree = new PairtreeRoot(new File(ptfs));

            if (myCsvFile != null && myCsvFile.exists()) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(BUNDLE.get("INGEST_CSV", myCsvFile, myCsvPathCol, myCsvIdCol));
                }

                final int ingestCount = ingestCSVFile(pairtree);

                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info(BUNDLE.get("INGEST_SUCCESS", ingestCount));
                }
            } else if (myCsvFile == null && LOGGER.isWarnEnabled()) {
                LOGGER.warn(BUNDLE.get("INGEST_EMPTY"));
            }
        } catch (final FileNotFoundException details) {
            throw new MojoExecutionException(details.getMessage(), details);
        } catch (final IOException details) {
            throw new MojoExecutionException(details.getMessage(), details);
        }
    }

    protected abstract File convertToJp2(final File aSource, final DjatokaEncodeParam aParams)
            throws MojoExecutionException;

    protected DjatokaEncodeParam getEncodingParams() throws IOException {
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

    private int ingestCSVFile(final PairtreeRoot aPairtree) throws IOException, MojoExecutionException {
        CSVReader csvReader = null;
        int imageCounter = 0;
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
                if (csv.length == 1 && StringUtils.trimToNull(csv[0]) == null) {
                    continue;
                }

                if (myCsvIdCol >= csv.length || myCsvPathCol >= csv.length || (myCsvIdCol == myCsvPathCol)) {
                    throw new MojoExecutionException(BUNDLE.get("INGEST_INDEX", myCsvPathCol, myCsvIdCol, csv.length));
                }

                if (jp2Pattern.matcher(csv[myCsvPathCol]).matches()) {
                    final File jp2 = new File(csv[myCsvPathCol]);

                    if (jp2.exists() && jp2.canRead()) {
                        storeJP2(csv[myCsvIdCol], jp2, aPairtree);
                        imageCounter++;
                    } else if (LOGGER.isWarnEnabled()) {
                        LOGGER.warn(BUNDLE.get("INGEST_FILE_FAIL", jp2));
                    }
                } else if (tifPattern.matcher(csv[myCsvPathCol]).matches()) {
                    final File tiff = new File(csv[myCsvPathCol]);

                    if (tiff.exists() && tiff.canRead()) {
                        final DjatokaEncodeParam params = getEncodingParams();

                        if (tiff.length() < myMaxSize) {
                            final File jp2 = convertToJp2(tiff, params);

                            if (jp2 != null) {
                                storeJP2(csv[myCsvIdCol], jp2, aPairtree);
                                imageCounter++;
                            }
                        } else if (LOGGER.isErrorEnabled()) {
                            LOGGER.error(BUNDLE.get("INGEST_MAXSIZE", tiff.length(), myMaxSize));
                        }
                    } else if (LOGGER.isWarnEnabled()) {
                        LOGGER.warn(BUNDLE.get("INGEST_FILE_FAIL", tiff));
                    }
                } else if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn(BUNDLE.get("INGEST_FORMAT_UNKNOWN", csv[myCsvPathCol]));
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

        return imageCounter++;
    }

    private void storeJP2(final String aID, final File aJP2File, final PairtreeRoot aPairtree) throws IOException {
        final PairtreeObject dir = aPairtree.getObject(aID);
        final String filename = PairtreeUtils.encodeID(aID);
        final File newJP2File = new File(dir, filename);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(BUNDLE.get("INGEST_COPY", aJP2File, newJP2File));
        }

        // We overwrite the JP2 file if it already exists
        FileUtils.copy(aJP2File, newJP2File);
    }
}
