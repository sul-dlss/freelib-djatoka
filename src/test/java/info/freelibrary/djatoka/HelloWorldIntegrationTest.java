
package info.freelibrary.djatoka;

import static org.junit.Assert.fail;
import static org.junit.Assert.assertEquals;

import info.freelibrary.util.StringUtils;

import java.net.HttpURLConnection;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.net.URL;

import org.junit.Test;

public class HelloWorldIntegrationTest {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(HelloWorldIntegrationTest.class);

    private static final String QUERY =
            "http://localhost:{}/resolve?url_ver=Z39.88-2004&rft_id=http%3A%2F%2Fmemory.loc.gov%2Fgmd%2Fgmd433%2Fg4330%2Fg4330%2Fnp000066.jp2&svc_id=info:lanl-repo/svc/getRegion&svc_val_fmt=info:ofi/fmt:kev:mtx:jpeg2000&svc.format=image/jpeg&svc.level=1";

    @Test
    public void test() {
        String jettyPort = System.getProperty("jetty.port");
        
        try {
            Integer.parseInt(jettyPort);
        }
        catch (NumberFormatException details) {
            fail("jetty.port is not an integer: " + jettyPort);
        }
        
        try {
            URL url = new URL(StringUtils.format(QUERY, jettyPort));
            HttpURLConnection huc = (HttpURLConnection) url.openConnection();

            huc.connect();

            assertEquals(200, huc.getResponseCode());
            assertEquals(9206, huc.getContentLength());
        } catch (Exception details) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Error connecting to djatoka server", details);
            }

            fail(details.getMessage());
        }
    }

}
