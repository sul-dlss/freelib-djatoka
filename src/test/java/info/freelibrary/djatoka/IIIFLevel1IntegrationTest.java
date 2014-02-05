
package info.freelibrary.djatoka;

import static org.junit.Assert.fail;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import java.io.File;

import info.freelibrary.util.StringUtils;

import java.net.URL;
import java.net.HttpURLConnection;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class IIIFLevel1IntegrationTest {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(IIIFLevel1IntegrationTest.class);

    private static final String ID = "67352ccc-d1b0-11e1-89ae-279075081939";

    private static final String QUERY = "http://localhost:{}/iiif/{}/";

    private String myJettyPort;

    /**
     * Sets up the IIIF level 1 compatibility integration test.
     */
    @Before
    public void setup() {
        File ptRoot = new File(System.getProperty("pairtree.root"));

        // Clean out any existing JP2s so we don't load from cache
        if (ptRoot.exists()) {
            ptRoot.delete();
        }

        try {
            myJettyPort = System.getProperty("jetty.port");
            Integer.parseInt(myJettyPort);
        } catch (NumberFormatException details) {
            fail("jetty.port is not an integer: " + myJettyPort);
        }
    }

    /**
     * Retrieve basic image does not error test.
     */
    @Test
    public void testBasicImageDoesNotError() {
        if (LOGGER.isDebugEnabled()) {
            String c = IIIFLevel1IntegrationTest.class.getSimpleName();
            LOGGER.debug("Running basic image doesn't error test: {}", c);
        }

        try {
            String query = StringUtils.format(QUERY, myJettyPort, ID);
            URL url = new URL(query + "full/full/0/native");
            HttpURLConnection huc = (HttpURLConnection) url.openConnection();

            huc.connect();
            assertEquals(url + " failed", 200, huc.getResponseCode());

        } catch (Exception details) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Error connecting to djatoka server", details);
            }

            fail(details.getMessage());
        }
    }

    /**
     * Check escaped characters processed test.
     */
    @Test
    public void testEscapedCharactersProcessed() {
        if (LOGGER.isDebugEnabled()) {
            String c = IIIFLevel1IntegrationTest.class.getSimpleName();
            LOGGER.debug("Running test escaped characters processed: {}", c);
        }

        try {
            String id = "67352ccc%252Dd1b0%252D11e1%252D89ae%252D279075081939";
            String query = StringUtils.format(QUERY, myJettyPort, id);
            URL url = new URL(query + "full/full/0/native");
            HttpURLConnection huc = (HttpURLConnection) url.openConnection();

            huc.connect();
            assertEquals(url + " failed", 200, huc.getResponseCode());

        } catch (Exception details) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Error connecting to djatoka server", details);
            }

            fail(details.getMessage());
        }
    }

    /**
     * Check image is correct test.
     */
    @Test
    public void testCheckImageIsCorrect() {
        if (LOGGER.isDebugEnabled()) {
            String c = IIIFLevel1IntegrationTest.class.getSimpleName();
            LOGGER.debug("Running test check image is correct: {}", c);
        }

        try {
            String query = StringUtils.format(QUERY, myJettyPort, ID);
            URL url = new URL(query + "full/full/0/native.jpg");
            HttpURLConnection huc = (HttpURLConnection) url.openConnection();

            huc.connect();
            assertEquals(url + " failed", 200, huc.getResponseCode());

        } catch (Exception details) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Error connecting to djatoka server", details);
            }

            fail(details.getMessage());
        }
    }
    
    /**
     * 404 by Random ID test.
     */
    @Test
    public void testRandomID() {
        if (LOGGER.isDebugEnabled()) {
            String c = IIIFLevel1IntegrationTest.class.getSimpleName();
            LOGGER.debug("Running test random ID: {}", c);
        }

        try {
            String id = "c959c260-8a3b-11e3-a8e5-0050569b3c3f";
            String query = StringUtils.format(QUERY, myJettyPort, id);
            URL url = new URL(query + "full/full/0/native");
            HttpURLConnection huc = (HttpURLConnection) url.openConnection();

            huc.connect();
            assertEquals(url + " failed", 404, huc.getResponseCode());

        } catch (Exception details) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Error connecting to djatoka server", details);
            }

            fail(details.getMessage());
        }
    }

    /**
     * 400 by ID with Unescaped Characters test.
     */
    @Test
    public void testUnescapedCharacters() {
        if (LOGGER.isDebugEnabled()) {
            String c = IIIFLevel1IntegrationTest.class.getSimpleName();
            LOGGER.debug("Running test unescaped characters: {}", c);
        }

        try {
            String id = "%5Bfrob%5D";
            String query = StringUtils.format(QUERY, myJettyPort, id);
            URL url = new URL(query + "full/full/0/native");
            HttpURLConnection huc = (HttpURLConnection) url.openConnection();

            huc.connect();
            assertEquals(url + " failed", 404, huc.getResponseCode());

        } catch (Exception details) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Error connecting to djatoka server", details);
            }

            fail(details.getMessage());
        }
    }
    
    /**
     * 404 by ID with Forward Slash test.
     */
    @Test
    public void testIDWithForwardSlash() {
        if (LOGGER.isDebugEnabled()) {
            String c = IIIFLevel1IntegrationTest.class.getSimpleName();
            LOGGER.debug("Running test ID with forward slash: {}", c);
        }

        try {
            String id = "a%2Fb";
            String query = StringUtils.format(QUERY, myJettyPort, id);
            URL url = new URL(query + "full/full/0/native");
            HttpURLConnection huc = (HttpURLConnection) url.openConnection();

            huc.connect();
            assertEquals(url + " failed", 404, huc.getResponseCode());

        } catch (Exception details) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Error connecting to djatoka server", details);
            }

            fail(details.getMessage());
        }
    }
    
    /**
     * Check error is 400 on invalid region test.
     */
    @Test
    public void testInvalidRegion() {
        if (LOGGER.isDebugEnabled()) {
            String c = IIIFLevel1IntegrationTest.class.getSimpleName();
            LOGGER.debug("Running test invalid region: {}", c);
        }

        try {
            String query = StringUtils.format(QUERY, myJettyPort, ID);
            URL url = new URL(query + "lrq%3Bc8/full/0/native");
            HttpURLConnection huc = (HttpURLConnection) url.openConnection();

            huc.connect();
            assertEquals(url + " failed", 400, huc.getResponseCode());

        } catch (Exception details) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Error connecting to djatoka server", details);
            }

            fail(details.getMessage());
        }
    }
    
    /**
     * Check error is 400 on invalid quality test.
     */
    @Test
    public void testCheck400onInvalidQuality() {
        if (LOGGER.isDebugEnabled()) {
            String c = IIIFLevel1IntegrationTest.class.getSimpleName();
            LOGGER.debug("Running check for 400 on invalid quality: {}", c);
        }

        try {
            String query = StringUtils.format(QUERY, myJettyPort, ID);
            URL url = new URL(query + "full/full/0/%3A_g%40OY");
            HttpURLConnection huc = (HttpURLConnection) url.openConnection();

            huc.connect();
            assertEquals(url + " failed", 400, huc.getResponseCode());

        } catch (Exception details) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Error connecting to djatoka server", details);
            }

            fail(details.getMessage());
        }
    }
    
    /**
     * Check error is 400 on invalid format test.
     */
    @Test
    public void testCheck400onInvalidFormat() {
        if (LOGGER.isDebugEnabled()) {
            String c = IIIFLevel1IntegrationTest.class.getSimpleName();
            LOGGER.debug("Running check for 400 on invalid format: {}", c);
        }

        try {
            String query = StringUtils.format(QUERY, myJettyPort, ID);
            URL url = new URL(query + "full/full/0/native.udm");
            HttpURLConnection huc = (HttpURLConnection) url.openConnection();

            huc.connect();
            assertEquals(url + " failed", 400, huc.getResponseCode());

        } catch (Exception details) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Error connecting to djatoka server", details);
            }

            fail(details.getMessage());
        }
    }
    
    /**
     * Check size with percent test.
     */
    @Test
    public void testCheckSizeWithPercent() {
        if (LOGGER.isDebugEnabled()) {
            String c = IIIFLevel1IntegrationTest.class.getSimpleName();
            LOGGER.debug("Running check size with percent test: {}", c);
        }

        try {
            String query = StringUtils.format(QUERY, myJettyPort, ID);
            URL url = new URL(query + "full/pct%3A62/0/native");
            HttpURLConnection huc = (HttpURLConnection) url.openConnection();

            huc.connect();
            assertEquals(url + " failed", 200, huc.getResponseCode());

        } catch (Exception details) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Error connecting to djatoka server", details);
            }

            fail(details.getMessage());
        }
    }
    
    /**
     * Check size with only width test.
     */
    @Test
    public void testCheckSizeWithOnlyWidth() {
        if (LOGGER.isDebugEnabled()) {
            String c = IIIFLevel1IntegrationTest.class.getSimpleName();
            LOGGER.debug("Running check size with only width test: {}", c);
        }

        try {
            String query = StringUtils.format(QUERY, myJettyPort, ID);
            URL url = new URL(query + "full/593%2C/0/native");
            HttpURLConnection huc = (HttpURLConnection) url.openConnection();

            huc.connect();
            assertEquals(url + " failed", 200, huc.getResponseCode());

        } catch (Exception details) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Error connecting to djatoka server", details);
            }

            fail(details.getMessage());
        }
    }
    
    /**
     * Check size with only height test.
     */
    @Test
    public void testCheckSizeWithOnlyHeight() {
        if (LOGGER.isDebugEnabled()) {
            String c = IIIFLevel1IntegrationTest.class.getSimpleName();
            LOGGER.debug("Running check size with only height test: {}", c);
        }

        try {
            String query = StringUtils.format(QUERY, myJettyPort, ID);
            URL url = new URL(query + "full/%2C661/0/native");
            HttpURLConnection huc = (HttpURLConnection) url.openConnection();

            huc.connect();
            assertEquals(url + " failed", 200, huc.getResponseCode());

        } catch (Exception details) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Error connecting to djatoka server", details);
            }

            fail(details.getMessage());
        }
    }
    
    /**
     * Check error is 400 on invalid size test.
     */
    @Test
    public void testCheck400ErrorOnInvalidSize() {
        if (LOGGER.isDebugEnabled()) {
            String c = IIIFLevel1IntegrationTest.class.getSimpleName();
            LOGGER.debug("Running check for 400 on invalid size test: {}", c);
        }

        try {
            String query = StringUtils.format(QUERY, myJettyPort, ID);
            URL url = new URL(query + "full/2ascX%5B/0/native");
            HttpURLConnection huc = (HttpURLConnection) url.openConnection();

            huc.connect();
            assertEquals(url + " failed", 400, huc.getResponseCode());

        } catch (Exception details) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Error connecting to djatoka server", details);
            }

            fail(details.getMessage());
        }
    }
    
    /**
     * Check 90o rotations of full image test.
     */
    @Test
    public void testCheck90DegreeRotationOfFullImage() {
        if (LOGGER.isDebugEnabled()) {
            String c = IIIFLevel1IntegrationTest.class.getSimpleName();
            LOGGER.debug("Running check full image 90 rotation test: {}", c);
        }

        try {
            String query = StringUtils.format(QUERY, myJettyPort, ID);
            URL url = new URL(query + "full/full/180/native");
            HttpURLConnection huc = (HttpURLConnection) url.openConnection();

            huc.connect();
            assertEquals(url + " failed", 200, huc.getResponseCode());

        } catch (Exception details) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Error connecting to djatoka server", details);
            }

            fail(details.getMessage());
        }
    }
    
    /**
     * Check 90o rotations of region test.
     */
    @Test
    public void testCheck90DegreeRotationOfRegion() {
        if (LOGGER.isDebugEnabled()) {
            String c = IIIFLevel1IntegrationTest.class.getSimpleName();
            LOGGER.debug("Running check 90 rotation of region test: {}", c);
        }

        try {
            String query = StringUtils.format(QUERY, myJettyPort, ID);
            URL url = new URL(query + "313%2C613%2C76%2C76/full/180/native");
            HttpURLConnection huc = (HttpURLConnection) url.openConnection();

            huc.connect();
            assertEquals(url + " failed", 200, huc.getResponseCode());

        } catch (Exception details) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Error connecting to djatoka server", details);
            }

            fail(details.getMessage());
        }
    }
    
    /**
     * Check error is 400 on invalid rotation test.
     */
    @Test
    public void testCheck400ErrorOnInvalidRotation() {
        if (LOGGER.isDebugEnabled()) {
            String c = IIIFLevel1IntegrationTest.class.getSimpleName();
            LOGGER.debug("Running check 400 on invalid rotation test: {}", c);
        }

        try {
            String query = StringUtils.format(QUERY, myJettyPort, ID);
            URL url = new URL(query + "full/full/9u1h/native");
            HttpURLConnection huc = (HttpURLConnection) url.openConnection();

            huc.connect();
            assertEquals(url + " failed", 400, huc.getResponseCode());

        } catch (Exception details) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Error connecting to djatoka server", details);
            }

            fail(details.getMessage());
        }
    }
}
