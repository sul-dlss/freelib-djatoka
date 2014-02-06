
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

public class HelloWorldIntegrationTest {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(HelloWorldIntegrationTest.class);

    private static final String QUERY =
            "http://localhost:{}/resolve?url_ver=Z39.88-2004"
                    + "&rft_id=http%3A%2F%2Fmemory.loc.gov%2Fgmd%2Fgmd433%2Fg4330%2Fg4330%2Fnp000066.jp2"
                    + "&svc_id=info:lanl-repo/svc/getRegion&svc_val_fmt=info:ofi/fmt:kev:mtx:jpeg2000"
                    + "&svc.format=image/jpeg&svc.level=1";

    /**
     * Sets up the Hello World integration test.
     */
    @Before
    public void setup() {
        File ptRoot = new File(System.getProperty("pairtree.root"));

        // Clean out any existing JP2s so we don't load from cache
        if (ptRoot.exists()) {
            ptRoot.delete();
        }
    }

    /**
     * Runs the Hello World integration test.
     */
    @Test
    public void test() {
        String jettyPort = System.getProperty("jetty.port");

        try {
            Integer.parseInt(jettyPort);
        } catch (NumberFormatException details) {
            fail("jetty.port is not an integer: " + jettyPort);
        }

        if (LOGGER.isDebugEnabled()) {
            String className = HelloWorldIntegrationTest.class.getSimpleName();
            LOGGER.debug("Running simple integration test: {}", className);
        }

        try {
            URL url = new URL(StringUtils.format(QUERY, jettyPort));
            HttpURLConnection huc = (HttpURLConnection) url.openConnection();

            huc.connect();

            assertEquals("HTTP response code check failed", 200, huc
                    .getResponseCode());

            assertEquals("LC image content length check failed", 9206, huc
                    .getContentLength());

        } catch (Exception details) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Error connecting to djatoka server", details);
            }

            fail(details.getMessage());
        }
    }

}
