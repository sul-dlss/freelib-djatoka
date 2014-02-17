
package gov.lanl.adore.djatoka.io.writer;

import static org.junit.Assert.fail;

import gov.lanl.adore.djatoka.io.FormatIOException;
import gov.lanl.adore.djatoka.io.reader.DjatokaReader;

import java.awt.image.BufferedImage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.junit.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TIFWriterTest {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(TIFWriterTest.class);

    private static final String TIF =
            "src/test/resources/images/envelopes/MS0332_gra_09954.tiff";

    /**
     * Tests writing a TIFF file.
     */
    @Test
    public void testWrite() {
        File outFile = null;

        try {
            outFile = File.createTempFile("test1-", ".tif");

            BufferedImage bufImage = new DjatokaReader().open(TIF);
            OutputStream outStream = new FileOutputStream(outFile);
            TIFWriter tifWriter = new TIFWriter();

            tifWriter.write(bufImage, outStream);
            outStream.close();
        } catch (IOException details) {
            fail(details.getMessage());
        } catch (FormatIOException details) {
            fail(details.getMessage());
        } finally {
            if (outFile != null) {
                outFile.delete();
            }
        }
    }

    /**
     * Tests setting writer properties.
     */
    @Test
    public void testSetWriterProperties() {
        // fail("Not yet implemented");
    }

}
