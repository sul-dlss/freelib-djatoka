
package info.freelibrary.djatoka.view;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import nu.xom.Document;
import nu.xom.Element;

import org.junit.Test;

import info.freelibrary.djatoka.iiif.Constants;

public class ImageInfoTest {

    /**
     * Tests the image info constructor.
     */
    @Test
    public void testImageInfo() {
        final ImageInfo imageInfo = new ImageInfo("id", 24, 42, 4);

        assertEquals("id", imageInfo.getIdentifier());
        assertEquals(24, imageInfo.getHeight());
        assertEquals(42, imageInfo.getWidth());
    }

    /**
     * Tests adding a format to the image info object.
     */
    @Test
    public void testAddFormat() {
        final ImageInfo imageInfo = new ImageInfo("id", 24, 42, 4);
        imageInfo.addFormat("jpg");

        assertEquals("jpg", imageInfo.getFormats().get(0));
    }

    /**
     * Tests the toXML() function of the image info object.
     */
    @Test
    public void testToXML() {
        final ImageInfo imageInfo = new ImageInfo("id", 24, 42, 4);
        final Document xml = new Document(new Element("info", Constants.IIIF_NS));
        final Element identifier = new Element("identifier", Constants.IIIF_NS);
        final Element height = new Element("height", Constants.IIIF_NS);
        final Element width = new Element("width", Constants.IIIF_NS);

        identifier.appendChild("id");
        height.appendChild("24");
        width.appendChild("42");

        // The order below is significant
        xml.getRootElement().appendChild(identifier);
        xml.getRootElement().appendChild(width);
        xml.getRootElement().appendChild(height);

        assertEquals(xml.toXML(), imageInfo.toXML());
    }

    /**
     * Tests the toString() function of the image info object.
     */
    @Test
    public void testToString() {
        final ImageInfo imageInfo = new ImageInfo("id", 24, 42, 4);
        final Document xml = new Document(new Element("info", Constants.IIIF_NS));
        final Element identifier = new Element("identifier", Constants.IIIF_NS);
        final Element height = new Element("height", Constants.IIIF_NS);
        final Element width = new Element("width", Constants.IIIF_NS);

        identifier.appendChild("id");
        height.appendChild("24");
        width.appendChild("42");

        // The order below is significant
        xml.getRootElement().appendChild(identifier);
        xml.getRootElement().appendChild(width);
        xml.getRootElement().appendChild(height);

        assertEquals(xml.toXML(), imageInfo.toString());
    }

    /**
     * Tests the toJSON() function of the image info object.
     */
    @Test
    public void testToJSON() {
        final ImageInfo imageInfo = new ImageInfo("id", 24, 42, 4);
        final String imageAPI = "http://library.stanford.edu/iiif/image-api";
        final String json =
                "{\"@context\":\"" + imageAPI + "/1.1/context.json\"," + "\"@id\":\"http://localhost/iiif/id\"," +
                        "\"width\":42,\"height\":24," + "\"scale_factors\":[1,2,3,4],\"tile_width\":256," +
                        "\"tile_height\":256," + "\"formats\":[],\"qualities\":[\"native\"]," + "\"profile\":\"" +
                        imageAPI + "/1.1/compliance.html#level1\"}";
        try {
            assertEquals(json, imageInfo.toJSON("http://localhost", "iiif"));
        } catch (final Exception details) {
            fail(details.getMessage());
        }
    }

}
