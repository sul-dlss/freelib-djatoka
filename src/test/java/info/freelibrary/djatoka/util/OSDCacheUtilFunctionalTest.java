
package info.freelibrary.djatoka.util;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.freelibrary.util.FileUtils;
import info.freelibrary.util.PairtreeObject;
import info.freelibrary.util.PairtreeRoot;
import info.freelibrary.util.PairtreeUtils;
import info.freelibrary.util.StringUtils;

public class OSDCacheUtilFunctionalTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(OSDCacheUtilFunctionalTest.class);

    private static final String ID = "--/walters/W102_000059_950";

    private static final File SOURCE_FILE = new File("src/test/resources/images/walters/W102_000059_950.jp2");

    private static final File PAIRTREE_ROOT = new File(System.getProperty("pairtree.cache"));

    private static final String SERVICE = "iiif";

    private static final String LOCALHOST = "http://localhost:";

    private static final String JPEG_CONTENT_TYPE[] = new String[] { "image/jpeg", "image/jpg" };

    private static int PORT = Integer.parseInt(System.getProperty("jetty.port"));

    /**
     * This project requires that the Jetty Web server be run in forked mode; this means we need to confirm that it's
     * actually up and ready to respond before any of our functional tests will work.
     */
    @Before
    public void setUp() {
        try {
            final PairtreeRoot ptRoot = new PairtreeRoot(PAIRTREE_ROOT);
            final PairtreeObject ptObj = ptRoot.getObject(ID);
            final File jp2 = new File(ptObj, PairtreeUtils.encodeID(ID));
            final HttpURLConnection http = (HttpURLConnection) new URL(LOCALHOST + PORT + "/health").openConnection();

            if (ptObj.exists()) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Removing pre-existing tile cache for test image");
                }

                if (!FileUtils.delete(ptObj)) {
                    fail("Unable to delete test image's tile cache: " + ptObj);
                } else if (!ptObj.mkdirs()) {
                    fail("Unable to create test image's pairtree directory: " + ptObj);
                }
            }

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Checking to see if Jetty is ready for functional tests");
            }

            // Check that the Jetty server is responsive
            if (http.getResponseCode() != 200) {
                fail("Jetty is not accepting requests");
            }

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Copying test file from '{}' to '{}'", SOURCE_FILE, jp2);
            }

            // Copy our test JP2 file into the Pairtree structure
            FileUtils.copy(SOURCE_FILE, jp2);
        } catch (final IOException details) {
            fail(details.getMessage());
        }
    }

    /**
     * Test the region path generation of the OpenSeadragon tiler.
     */
    @Test
    public void testGetPaths() {
        final String[] tilePaths = new OSDCacheUtil().getPaths(SERVICE, ID, 256, 7613, 10557);
        HttpURLConnection http = null;
        int processed = 0;

        // TODO: clear cache so it's run each time
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Testing OpenSeadragon tile generation");
        }

        try {
            for (final String path : tilePaths) {
                http = (HttpURLConnection) new URL(LOCALHOST + PORT + "/" + path).openConnection();

                // Output something so folks know it's not stalled
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Creating tile for: " + http.getURL());
                } else if (LOGGER.isInfoEnabled() && ++processed % 500 == 0) {
                    LOGGER.info("{} tiles generated", processed);
                }

                if (http.getResponseCode() != 200 ||
                        Arrays.binarySearch(JPEG_CONTENT_TYPE, http.getContentType()) < 0) {
                    fail(StringUtils.format("Couldn't create a tile for: {} [response code: {} | content type: {}]",
                            http.getURL(), http.getResponseCode(), http.getContentType()));
                }
            }
        } catch (final MalformedURLException details) {
            fail("Bad URL syntax: " + (http != null ? http.getURL() : "[Unknown URL]"));
        } catch (final IOException details) {
            fail("Unable to access: " + (http != null ? http.getURL() : "[Unknown URL]"));
        } finally {
            if (http != null) {
                http.disconnect();
            }
        }

    }
}
