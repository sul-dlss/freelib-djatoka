
package info.freelibrary.djatoka.iiif;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A representation of the request's image quality.
 * 
 * @author <a href="mailto:ksclarke@gmail.com">Kevin S. Clarke</a>
 */
public class Quality {

    private static final Logger LOGGER = LoggerFactory.getLogger(Quality.class);

    private String myQuality;

    /**
     * Creates a representation of an IIIF request's image quality.
     * 
     * @param aQuality
     * @throws IIIFException
     */
    public Quality(String aQuality) throws IIIFException {
        int end = aQuality.indexOf(".");

        if (end != -1) {
            throw new IIIFException("Unexpected image format: " +
                    aQuality.substring(end));
        } else {
            myQuality = aQuality;
        }

        if (!myQuality.equalsIgnoreCase("native") &&
                !aQuality.equalsIgnoreCase("color") &&
                !aQuality.equalsIgnoreCase("grey") &&
                !aQuality.equalsIgnoreCase("bitonal")) {
            throw new IIIFException("Unsupported request quality value: " +
                    aQuality);
        }
    }

    /**
     * Returns true if the image request's quality is native; else, false.
     * 
     * @return True if the image request's quality is native; else, false
     */
    public boolean isNative() {
        return myQuality.equalsIgnoreCase("native");
    }

    /**
     * Returns true if the image request's quality is color; else, false.
     * 
     * @return True if the image request's quality is color; else, false
     */
    public boolean isColor() {
        return myQuality.equalsIgnoreCase("color");
    }

    /**
     * Returns true if the image request's quality is grey; else, false.
     * 
     * @return True if the image request's quality is grey; else, false
     */
    public boolean isGrey() {
        return myQuality.equalsIgnoreCase("grey");
    }

    /**
     * Returns true if the image request's quality is bitonal; else, false.
     * 
     * @return True if the image request's quality is bitonal; else, false
     */
    public boolean isBitonal() {
        return myQuality.equalsIgnoreCase("bitonal");
    }

    /**
     * Returns a string representation of the image request's quality.
     * 
     * @return A string representation of the image request's quality
     */
    public String toString() {
        return myQuality.toLowerCase();
    }

}
