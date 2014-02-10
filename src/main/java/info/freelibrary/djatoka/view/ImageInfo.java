
package info.freelibrary.djatoka.view;

import java.io.UnsupportedEncodingException;

import java.net.URLEncoder;

import info.freelibrary.djatoka.iiif.Constants;

import java.util.Iterator;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Serializer;

public class ImageInfo {

    private Document myInfoDoc;

    /**
     * Creates an image info object.
     * 
     * @param aID An image ID
     * @param aHeight The height of the image represented by the supplied ID
     * @param aWidth The width of the image represented by the supplied ID
     */
    public ImageInfo(String aID, int aHeight, int aWidth) {
        Element id = new Element("identifier", Constants.IIIF_NS);
        Element height = new Element("height", Constants.IIIF_NS);
        Element width = new Element("width", Constants.IIIF_NS);
        Element root = new Element("info", Constants.IIIF_NS);

        width.appendChild(Integer.toString(aWidth));
        height.appendChild(Integer.toString(aHeight));
        id.appendChild(aID);

        myInfoDoc = new Document(root);
        root.appendChild(id);
        root.appendChild(width);
        root.appendChild(height);
    }

    /**
     * Gets the image's identifier.
     * 
     * @return The image's identifier
     */
    public String getIdentifier() {
        return getValue("identifier");
    }

    /**
     * Gets the image's height.
     * 
     * @return The height of the image
     */
    public int getHeight() {
        return Integer.parseInt(getValue("height"));
    }

    /**
     * Gets the image's width.
     * 
     * @return The width of the image
     */
    public int getWidth() {
        return Integer.parseInt(getValue("width"));
    }

    /**
     * Adds the supplied format to the list of handled formats.
     * 
     * @param aFormat A format to add to the supported list
     */
    public void addFormat(String aFormat) {
        Element root = myInfoDoc.getRootElement();
        Elements elements = root.getChildElements("formats", Constants.IIIF_NS);
        Element format = new Element("format", Constants.IIIF_NS);
        Element formats;

        if (elements.size() > 0) {
            formats = elements.get(0);
        } else {
            formats = new Element("formats", Constants.IIIF_NS);
            root.appendChild(formats);
        }

        format.appendChild(aFormat);
        formats.appendChild(format);
    }

    /**
     * Gets the list of supported formats.
     * 
     * @return The list of supported formats
     */
    public List<String> getFormats() {
        return getValues("format");
    }

    /**
     * Gets the XML representation of the image's metadata.
     * 
     * @return The XML representation of the image's metadata
     */
    public String toXML() {
        return myInfoDoc.toXML();
    }

    /**
     * Gets the string representation of the image's metadata.
     * 
     * @return The string representation of the image's metadata
     */
    public String toString() {
        return myInfoDoc.toXML();
    }

    /**
     * Gets the JSON representation of the image's metadata.
     * 
     * @param aService The IIIF service
     * @param aPrefix The IIIF prefix
     * @return The JSON representation of the image's metadata
     */
    public String toJSON(String aService, String aPrefix) {
        StringBuilder sb = new StringBuilder("{");
        Iterator<String> iterator;

        // FIXME: Constants.IIIF_URL
        sb.append("\"@context\" : \"http://library.stanford.edu/");
        sb.append("iiif/image-api/1.1/context.json\", \"@id\" : \"");

        // FIXME: aService + aPrefix
        try {
            sb.append(aService).append('/').append(aPrefix).append('/');
            sb.append(URLEncoder.encode(getIdentifier(), "UTF-8"));
            sb.append("\", ");
        } catch (UnsupportedEncodingException details) {
            throw new RuntimeException(details);
        }

        sb.append("\"width\" : ").append(getWidth()).append(", ");
        sb.append("\"height\" : ").append(getHeight()).append(", ");

        // FIXME: Does this ever vary for us?
        sb.append("\"tile_width\" : ").append(256).append(", ");
        sb.append("\"tile_height\" : ").append(256).append(", ");

        iterator = getFormats().iterator();
        sb.append("\"formats\" : [ ");

        while (iterator.hasNext()) {
            sb.append('"').append(iterator.next()).append('"');

            if (iterator.hasNext()) {
                sb.append(", ");
            } else {
                sb.append(" ");
            }
        }

        sb.append("], ");

        sb.append("\"scale_factors\" : [ 1, 2, 3, 4 ], ");

        sb.append("\"qualities\" : [ \"native\" ], \"profile\" : \"");
        sb.append(Constants.IIIF_URL).append("1.1/compliance.html#level1\"");

        return sb.append("}").toString();
    }

    /**
     * Serializes the image info the supplied output stream.
     * 
     * @param aOutputStream The output stream to which the image info should be
     *        serialized
     * @throws IOException If there is a problem reading or writing the image
     *         info
     */
    public void toStream(OutputStream aOutputStream) throws IOException {
        new Serializer(aOutputStream).write(myInfoDoc);
    }

    private List<String> getValues(String aName) {
        ArrayList<String> list = new ArrayList<String>();
        Element root = myInfoDoc.getRootElement();
        Elements elements = root.getChildElements();

        for (int eIndex = 0; eIndex < elements.size(); eIndex++) {
            Element element = elements.get(eIndex);
            Elements children =
                    element.getChildElements(aName, Constants.IIIF_NS);

            if (children.size() > 0) {
                for (int cIndex = 0; cIndex < children.size(); cIndex++) {
                    list.add(children.get(cIndex).getValue());
                }

                break;
            }
        }

        return list;
    }

    private String getValue(String aName) {
        Element root = myInfoDoc.getRootElement();
        Elements elements = root.getChildElements(aName, Constants.IIIF_NS);

        if (elements.size() > 0) {
            return elements.get(0).getValue();
        }

        return null;
    }

}
