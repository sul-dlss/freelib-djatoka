
package info.freelibrary.djatoka;

import static org.junit.Assert.fail;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.freelibrary.util.FileUtils;
import info.freelibrary.util.StringUtils;

public class ConfigurationTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationTest.class);

    private static final String MESSAGE = "The intended {} directory '{}' doesn't exist or can't be written to";

    private static Properties PROPERTIES;

    @BeforeClass
    public static void setUp() {
        try {
            final FileInputStream fis = new FileInputStream(new File("target/classes/djatoka-properties.xml"));
            final BufferedInputStream bis = new BufferedInputStream(fis);

            PROPERTIES = new Properties();
            PROPERTIES.loadFromXML(bis);
        } catch (final IOException details) {
            fail(details.getMessage());
        }
    }

    /**
     * Tests that the source data directory exists and is okay to use.
     */
    @Test
    public void testSourceDataDirIsOkay() {
        final String name = PROPERTIES.getProperty("djatoka.ingest.data.dir");

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Checking source image directory: {}", name);
        }

        if (!FileUtils.dirIsUseable(name, "rx")) {
            fail(StringUtils.format("The source image data directory '{}' doesn't exist or can't be read", name));
        }
    }

    /**
     * Tests that the JP2 data directory exists and is okay to use.
     */
    @Test
    public void testJP2DirIsOkay() {
        final String name = PROPERTIES.getProperty("djatoka.ingest.jp2.dir");

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Checking JP2 image directory: {}", name);
        }

        if (!FileUtils.dirIsUseable(name, "rwx")) {
            fail(StringUtils.format(MESSAGE, "JP2 data", name));
        }
    }

    /**
     * Tests that the Pairtree cache exists and is okay to use.
     */
    @Test
    public void testPairtreeCacheIsOkay() {
        final String name = PROPERTIES.getProperty("djatoka.view.cache.dir");

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Checking Pairtree cache directory: {}", name);
        }

        if (!FileUtils.dirIsUseable(name, "rwx")) {
            fail(StringUtils.format(MESSAGE, "Pairtree cache", name));
        }
    }

    /**
     * Tests that the OpenURL cache exists and is okay to use
     */
    @Test
    public void testOpenURLCacheIsOkay() {
        final String name = PROPERTIES.getProperty("OpenURLJP2KService.cacheTmpDir");

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Checking OpenURL image cache directory: {}", name);
        }

        if (!FileUtils.dirIsUseable(name, "rwx")) {
            fail(StringUtils.format(MESSAGE, "OpenURL image cache", name));
        }
    }
}
