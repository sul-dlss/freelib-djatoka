
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
     * Runs basic image does not error test.
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
     * Runs escaped characters processed test.
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
}
