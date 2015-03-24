
package info.freelibrary.djatoka;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.freelibrary.util.PairtreeObject;
import info.freelibrary.util.PairtreeRoot;
import info.freelibrary.util.StringUtils;

/**
 * Integration tests related to the IdentifierResolver. To run just one method in this class, from the command line do
 * something like:<br/>
 * <code>
 * mvn -q -Ptravis verify -Dtest=NoTest -DfailIfNoTests=false -Dit.test=IdentifierIntegrationTest#testIDGuess
 * </code>
 *
 * @author <a href="mailto:ksclarke@gmail.com">Kevin S. Clarke</a>
 */
public class IdentifierIntegrationTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(IdentifierIntegrationTest.class);

    private static final String QUERY = "http://localhost:{}/resolve?url_ver=Z39.88-2004&rft_id={}&"
            + "svc_id=info:lanl-repo/svc/getRegion" + "&svc_val_fmt=info:ofi/fmt:kev:mtx:jpeg2000"
            + "&svc.format=image/jpeg&svc.level=1";

    private static final String ID = "67352ccc-d1b0-11e1-89ae-279075081939";

    private static final long EXPECTED_SIZE = 55840;

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
        } catch (final NumberFormatException details) {
            fail("jetty.port is not an integer: " + myJettyPort);
        }
    }

    /**
     * Sets up the identifier integration test.
     */
    @Before
    public void setup() {
        try {
            if (new PairtreeRoot(System.getProperty("pairtree.cache")).delete() && LOGGER.isDebugEnabled()) {
                LOGGER.debug("Deleted Pairtree root '{}' in preparation for another test", new PairtreeRoot(System
                        .getProperty("pairtree.cache")));
            }
        } catch (final IOException details) {
            fail(details.getMessage());
        }

        assertFalse("ID " + ID + " shouldn't exist after test setup: " + getPtObject(ID), getPtObject(ID).exists());
    }

    /**
     * Tests the ID resolver guess functionality. This tests reading this from the "remote" source and then storing
     * the image in the Pairtree file system with the supplied ID as the key.
     */
    @Test
    public void testIDGuess() {
        final File file = getPtObject(ID);

        assertFalse("ID shouldn't exist in Pairtree prior to the test: " + file, file.exists());

        final long size = testReferent(ID);

        assertEquals(file + " size...", EXPECTED_SIZE, size);
        assertTrue("Didn't find a Pairtree file system object when there should be one: " + file, file.exists());
    }

    /**
     * Tests the URL ID resolver source functionality. This tests detecting the matching URL pattern and then storing
     * the image in the Pairtree file system with the supplied ID as the key.
     */
    @Test
    public void testURLIDSource() {
        final File file = getPtObject(ID);

        assertFalse("ID shouldn't exist in Pairtree prior to the test: " + file, file.exists());

        final long size = testReferent("http://localhost:" + myJettyPort + "/images/" + ID + ".jp2");

        assertEquals(file + " size...", EXPECTED_SIZE, size);
        assertTrue("Didn't find a Pairtree file system object when there should be one: " + file, file.exists());
    }

    /**
     * Tests the file ID resolver source functionality. This tests without putting it into the Pairtree file system.
     */
    @Test
    public void testFileIDSource() {
        final File file = getPtObject(ID);

        assertFalse("ID shouldn't exist in Pairtree prior to the test: " + file, file.exists());

        final String path = new File("").getAbsolutePath() + "/src/test/resources/images/iiif-test/" + ID + ".jp2";
        final long size = testReferent("file://" + path);

        assertEquals(file + " size...", EXPECTED_SIZE, size);
        assertFalse("Found a Pairtree file system object when there shouldn't be one: " + file, file.exists());
    }

    /**
     * Queries the supplied ID and checks that the length of the response is equal to the length of the image.
     *
     * @param aID A referent to check
     * @param aImageLength The length of the image returned
     */
    private long testReferent(final String aID) {
        try {
            final URL url = new URL(StringUtils.format(QUERY, myJettyPort, aID));

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Trying to connect to {}", url);
            }

            final HttpURLConnection http = (HttpURLConnection) url.openConnection();

            // Do the work
            http.connect();

            final int code = http.getResponseCode();
            long length = http.getContentLengthLong();

            // Check the results
            assertEquals("HTTP response code check failed: " + url.toExternalForm(), 200, code);

            if (length == -1) {
                final BufferedInputStream stream = new BufferedInputStream(http.getInputStream());

                // Zero this out, we'll have to count it ourselves
                length = 0;

                while (stream.read() != -1) {
                    length += 1;
                }

                stream.close();

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Returning calculated content length for '{}' image: {}", aID, length);
                }
            }

            return length;
        } catch (final Exception details) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Error connecting to djatoka server", details);
            }

            fail(details.getMessage());
            return -1;
        }
    }

    private File getPtObject(final String aID) {
        try {
            final PairtreeObject ptObj = new PairtreeRoot(System.getProperty("pairtree.cache")).getObject(aID);
            return new File(ptObj, aID); // This is the actual image file in the Pairtree object
        } catch (final IOException details) {
            fail(details.getMessage());
            return new File(System.getProperty("java.io.tmpdir"), "ptObj-" + new Date().getTime());
        }
    }

}
