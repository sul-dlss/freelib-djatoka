
package info.freelibrary.djatoka.util;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
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

    private static final String[] ID = new String[] { "--/walters/W102_000059_950", "--/walters/W102_000061_950" };

    private static final File[] SOURCE_FILES = new File[] {
        new File("src/test/resources/images/walters/W102_000059_950.jp2"),
        new File("src/test/resources/images/walters/W102_000061_950.jp2") };

    private static final File PAIRTREE_ROOT = new File(System.getProperty("pairtree.cache"));

    private static final String SERVICE = "iiif";

    private static final String LOCALHOST = "http://localhost:";

    private static final String JPEG_CONTENT_TYPE[] = new String[] { "image/jpeg", "image/jpg" };

    private static int PORT = Integer.parseInt(System.getProperty("jetty.port"));

    private HttpURLConnection myHTTPConx;

    /**
     * This project requires that the Jetty Web server be run in forked mode; this means we need to confirm that it's
     * actually up and ready to respond before any of our functional tests will work.
     */
    @Before
    public void setUp() {
        try {
            final PairtreeRoot ptRoot = new PairtreeRoot(PAIRTREE_ROOT);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Checking to see if Jetty is ready for functional tests");
            }

            myHTTPConx = (HttpURLConnection) new URL(LOCALHOST + PORT + "/health").openConnection();

            // Check that the Jetty server is responsive
            if (myHTTPConx.getResponseCode() != 200) {
                fail("Jetty is not accepting requests");
            }

            for (int index = 0; index < SOURCE_FILES.length; index++) {
                final PairtreeObject ptObj = ptRoot.getObject(ID[index]);
                final File jp2 = new File(ptObj, PairtreeUtils.encodeID(ID[index]));

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
                    LOGGER.debug("Copying test file from '{}' to '{}'", SOURCE_FILES[index], jp2);
                }

                // Copy our test JP2 file into the Pairtree structure
                FileUtils.copy(SOURCE_FILES[index], jp2);
            }
        } catch (final IOException details) {
            fail(details.getMessage());
        } finally {
            if (myHTTPConx != null) {
                myHTTPConx.disconnect();
            }
        }
    }

    /**
     * Test the region path generation of the OpenSeadragon tiler.
     */
    @Test
    public void testCachingTilePaths() {
        final String[] firstImagePaths = new OSDCacheUtil().getPaths(SERVICE, ID[0], 256, 7613, 10557);
        final String[] secondImagePaths = new OSDCacheUtil().getPaths(SERVICE, ID[1], 256, 7475, 10419);
        final ArrayList<String> filePaths = new ArrayList<String>();

        int processed = 0;

        // Collect all the paths we want to test
        filePaths.addAll(Arrays.asList(firstImagePaths));
        filePaths.addAll(Arrays.asList(secondImagePaths));

        // TODO: clear cache so it's run each time?
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Testing OpenSeadragon tile generation");
        }

        try {
            for (final String path : filePaths) {
                myHTTPConx = (HttpURLConnection) new URL(LOCALHOST + PORT + "/" + path).openConnection();

                // Output something so folks know it's not stalled
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Creating tile for: {}", myHTTPConx.getURL());
                } else if (LOGGER.isInfoEnabled() && ++processed % 500 == 0) {
                    LOGGER.info("{} tiles cached", processed);
                }

                if (myHTTPConx.getResponseCode() != 200 ||
                        Arrays.binarySearch(JPEG_CONTENT_TYPE, myHTTPConx.getContentType()) < 0) {
                    fail(StringUtils.format("Couldn't create a tile for: {} [response code: {} | content type: {}]",
                            myHTTPConx.getURL(), myHTTPConx.getResponseCode(), myHTTPConx.getContentType()));
                }
            }
        } catch (final MalformedURLException details) {
            fail("Bad URL syntax: " + (myHTTPConx != null ? myHTTPConx.getURL() : "[Unknown URL]"));
        } catch (final IOException details) {
            fail("Unable to access: " + (myHTTPConx != null ? myHTTPConx.getURL() : "[Unknown URL]"));
        } finally {
            if (myHTTPConx != null) {
                myHTTPConx.disconnect();
            }
        }

    }
}
