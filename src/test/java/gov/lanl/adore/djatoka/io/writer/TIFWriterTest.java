
package gov.lanl.adore.djatoka.io.writer;

import static org.junit.Assert.fail;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.junit.Test;

import gov.lanl.adore.djatoka.io.FormatIOException;
import gov.lanl.adore.djatoka.io.reader.DjatokaReader;

public class TIFWriterTest {

    private static final String TIF = "src/test/resources/images/envelopes/MS0332_gra_09954.tiff";

    /**
     * Tests writing a TIFF file.
     */
    @Test
    public void testWrite() {
        File outFile = null;

        try {
            outFile = File.createTempFile("test1-", ".tif");

            final BufferedImage bufImage = new DjatokaReader().open(TIF);
            final OutputStream outStream = new FileOutputStream(outFile);
            final TIFWriter tifWriter = new TIFWriter();

            tifWriter.write(bufImage, outStream);
            outStream.close();
        } catch (final IOException details) {
            fail(details.getMessage());
        } catch (final FormatIOException details) {
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
