
package info.freelibrary.maven;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.exec.environment.EnvironmentUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.lanl.adore.djatoka.DjatokaEncodeParam;
import gov.lanl.adore.djatoka.kdu.KduCompressExe;

import info.freelibrary.djatoka.Constants;

/**
 * Ingests content into FreeLib-Djatoka's Pairtree file system.
 * <p/>
 *
 * @author <a href="mailto:ksclarke@gmail.com">Kevin S. Clarke</a>
 */
@Mojo(name = "ingest")
public class DjatokaIngestMojo extends AbstractIngestMojo {

    private static final Logger LOGGER = LoggerFactory.getLogger(DjatokaIngestMojo.class);

    @Override
    protected File convertToJp2(final File aSource, final DjatokaEncodeParam aParams) throws MojoExecutionException {
        try {
            final File tmpJp2File = File.createTempFile("djatoka-", Constants.JP2_EXT);
            final Properties properties = myProject.getProperties();
            final String kakadu = properties.getProperty("LD_LIBRARY_PATH");
            final DjatokaEncodeParam params = getEncodingParams();
            final String source = aSource.getAbsolutePath();
            final String target = tmpJp2File.getAbsolutePath();

            String command;

            if (kakadu == null) {
                throw new MojoExecutionException(BUNDLE.get("INGEST_KAKADU_CFG"));
            }

            // Build the console command we use to do the conversion
            command = KduCompressExe.getKduCompressCommand(source, target, params);
            command = command.replaceFirst("^null/", kakadu + "/");

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(BUNDLE.get("INGEST_CONVERSION_COMMAND"), command);
            }

            final DefaultExecuteResultHandler handler = new DefaultExecuteResultHandler();
            final CommandLine cmdLine = CommandLine.parse(command);
            final DefaultExecutor executor = new DefaultExecutor();
            final ExecuteWatchdog watchdog = new ExecuteWatchdog(60000 * 10);
            final ByteArrayOutputStream error = new ByteArrayOutputStream();
            final ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
            final Map<String, String> environment = EnvironmentUtils.getProcEnvironment();

            if (System.getProperty("os.name").toLowerCase().indexOf("mac") >= 0) {
                environment.put("DYLD_LIBRARY_PATH", kakadu);
            } else {
                environment.put("LD_LIBRARY_PATH", kakadu);
            }

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
                return tmpJp2File;
            }
        } catch (final IOException details) {
            throw new MojoExecutionException(details.getMessage(), details);
        }
    }
}
