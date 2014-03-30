
package info.freelibrary.maven;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.MavenProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import info.freelibrary.djatoka.util.OSDCacheUtil;
import info.freelibrary.util.FileUtils;
import info.freelibrary.util.PairtreeRoot;
import info.freelibrary.util.PairtreeUtils;
import info.freelibrary.util.RegexFileFilter;
import info.freelibrary.util.StringUtils;
import info.freelibrary.util.XMLBundleControl;
import info.freelibrary.util.XMLResourceBundle;

/**
 * Caches tiles for JP2s in FreeLib-Djatoka's Pairtree file system.
 * <p/>
 * 
 * @author <a href="mailto:ksclarke@gmail.com">Kevin S. Clarke</a>
 */
@Mojo(name = "cache-tiles")
public class DjatokaTileMojo extends AbstractMojo {

    private static final String PAIRTREE_FS = "djatoka.jp2.data";

    private static final String JETTY_PORT = "jetty.port";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String CACHE_URL = "http://localhost:{}/{}";

    private static final String METADATA_URL =
            "http://localhost:{}/resolve?url_ver=Z39.88-2004&rft_id={}&svc_id=info:lanl-repo/svc/getMetadata";

    private static final String JPEG_CONTENT_TYPE[] = new String[] { "image/jpeg", "image/jpg" };

    private static final Logger LOGGER = LoggerFactory.getLogger(DjatokaIngestMojo.class);

    private final XMLResourceBundle BUNDLE = (XMLResourceBundle) ResourceBundle.getBundle("freelib-djatoka_messages",
            new XMLBundleControl());

    /**
     * The Maven project directory.
     */
    @Component
    private MavenProject myProject;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        final String ptfs = myProject.getProperties().getProperty(PAIRTREE_FS);
        final String port = myProject.getProperties().getProperty(JETTY_PORT);
        final OSDCacheUtil tiler = new OSDCacheUtil();

        // Make sure the supplied port number is actually an integer
        try {
            Integer.parseInt(port);
        } catch (final NumberFormatException details) {
            throw new MojoExecutionException(StringUtils.format("Supplied port ({}) must be an integer", port));
        }

        // Check that the FreeLib-Djatoka server is up (required for caching our tiles)
        try {
            final URL url = new URL("http://localhost:" + port + "/health");
            final HttpURLConnection http = (HttpURLConnection) url.openConnection();

            if (http.getResponseCode() != 200) {
                throw new MojoExecutionException("FreeLib-Djatoka server is up, but didn't respond as expected");
            }
        } catch (final IOException details) {
            throw new MojoExecutionException("Could not connect to a running FreeLib-Djatoka server; is it up?");
        }

        try {
            final PairtreeRoot pairtree = new PairtreeRoot(new File(ptfs));
            final RegexFileFilter filter = new RegexFileFilter(".*");
            final String eol = System.getProperty("line.separator");

            for (final File file : FileUtils.listFiles(pairtree, filter, true)) {
                final String id = PairtreeUtils.decodeID(file.getName());
                final URL url = new URL(StringUtils.format(METADATA_URL, port, id));
                final JsonNode json = MAPPER.readTree(url.openStream());

                // Pull out relevant info from our metadata service
                final int width = json.get("width").asInt();
                final int height = json.get("height").asInt();
                final String[] tilePaths = tiler.getPaths("iiif", id, 256, width, height);

                if (LOGGER.isDebugEnabled()) {
                    final StringBuilder builder = new StringBuilder("Generating tiles for ");

                    builder.append(id).append(eol);

                    // Get URLs OpenSeadragon's IIIF interface will call
                    for (final String path : tilePaths) {
                        cache(port, path);
                        builder.append("  ").append(path).append(eol);
                    }

                    LOGGER.debug(builder.toString());
                } else {
                    for (final String path : tilePaths) {
                        cache(port, path);
                    }
                }
            }

        } catch (final FileNotFoundException details) {
            throw new MojoExecutionException(details.getMessage(), details);
        } catch (final IOException details) {
            throw new MojoExecutionException(details.getMessage(), details);
        }
    }

    private void cache(final String aPort, final String aPath) throws MalformedURLException, IOException {
        final URL url = new URL(StringUtils.format(CACHE_URL, aPort, aPath));
        final HttpURLConnection http = (HttpURLConnection) url.openConnection();

        if (http.getResponseCode() != 200 || Arrays.binarySearch(JPEG_CONTENT_TYPE, http.getContentType()) < 0) {
            throw new IOException("Unable to cache tile image: " + aPath);
        }
    }
}
