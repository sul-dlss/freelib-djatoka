
package info.freelibrary.djatoka;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Before;

import info.freelibrary.util.StringUtils;
import java.net.HttpURLConnection;
import java.net.URL;

import java.io.File;

import org.junit.BeforeClass;
import org.junit.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IdentifierIntegrationTest {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(IdentifierIntegrationTest.class);

    private static final String QUERY =
            "http://localhost:{}/resolve?url_ver=Z39.88-2004&rft_id={}&"
                    + "svc_id=info:lanl-repo/svc/getRegion"
                    + "&svc_val_fmt=info:ofi/fmt:kev:mtx:jpeg2000"
                    + "&svc.format=image/jpeg&svc.level=1";

    private static String myJettyPort;

    /**
     * Sets up the identifier integration test.
     * 
     * @throws Exception If there is an exception thrown during testing
     */
    @BeforeClass
    public static void setUp() throws Exception {
        myJettyPort = System.getProperty("jetty.port");

        try {
            Integer.parseInt(myJettyPort);
        } catch (NumberFormatException details) {
            fail("jetty.port is not an integer: " + myJettyPort);
        }
    }

    /**
     * Sets up the identifier integration test.
     */
    @Before
    public void setup() {
        File ptRoot = new File(System.getProperty("pairtree.root"));

        // Clean out any existing JP2s so we don't load from cache
        if (ptRoot.exists()) {
            if (ptRoot.delete() && LOGGER.isDebugEnabled()) {
                LOGGER.debug("Deleting Pairtree root for new test");
            }
        }
    }

    /**
     * Tests the ID resolver guess functionality.
     */
    @Test
    public void testIDGuess() {
        testReferent("np000066", 9206);
    }

    /**
     * Tests the ID resolver source functionality.
     */
    @Test
    public void testIDSource() {
        String id = "http://memory.loc.gov/gmd/gmd433/g4330/g4330/np000066.jp2";
        testReferent(id, 9206);

        // TODO: check that it put it in the JP2 Pairtree file system
    }

    /**
     * Queries the supplied ID and checks that the length of the response is
     * equal to the length of the image.
     * 
     * @param aID A referent to check
     * @param aImageLength The length of the image returned
     */
    private void testReferent(String aID, int aImageLength) {
        try {
            URL url = new URL(StringUtils.format(QUERY, myJettyPort, aID));
            HttpURLConnection huc = (HttpURLConnection) url.openConnection();
            int code, length;

            // Do the work
            huc.connect();
            code = huc.getResponseCode();
            length = huc.getContentLength();

            // Check the results
            assertEquals("HTTP response code check failed", 200, code);
            assertEquals("LC image content length check failed", 9206, length);
        } catch (Exception details) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Error connecting to djatoka server", details);
            }

            fail(details.getMessage());
        }
    }
}
