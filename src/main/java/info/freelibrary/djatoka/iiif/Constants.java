
package info.freelibrary.djatoka.iiif;

/**
 * Constants used by FreeLib-Djatoka's IIIF interface.
 *
 * @author <a href="mailto:ksclarke@gmail.com">Kevin S. Clarke</a>
 */
public interface Constants {

    public static final String IIIF_URL = "http://library.stanford.edu/iiif/image-api/";

    public static final String IIIF_NS = IIIF_URL + "ns/";

    /**
     * Default character set used by Djatoka's IIIF interface.
     */
    public static final String DEFAULT_CHARSET = "UTF-8";

    /**
     * Content-type for XML responses (this is no longer supported by the IIIF interface, but we still use it).
     */
    public static final String XML_CONTENT_TYPE = "text/xml";

    /**
     * Content-type for JSON responses.
     */
    public static final String JSON_CONTENT_TYPE = "application/json";

    /**
     * Content-type for PDFs.
     */
    public static final String PDF_CONTENT_TYPE = "application/pdf";

    /**
     * Content-type for PNG images.
     */
    public static final String PNG_CONTENT_TYPE = "image/png";

    /**
     * Content-type for JPG images.
     */
    public static final String JPG_CONTENT_TYPE = "image/jpeg";

    /**
     * Content-type for TIFF images.
     */
    public static final String TIF_CONTENT_TYPE = "image/tiff";

    /**
     * Content-type for GIF images.
     */
    public static final String GIF_CONTENT_TYPE = "image/gif";

    /**
     * Content-type for JP2 images.
     */
    public static final String JP2_CONTENT_TYPE = "image/jp2";

    /**
     * Default content-type for Freelib-Djatoka is JPEG.
     */
    public static final String DEFAULT_CONTENT_TYPE = "image/jpeg";

    /**
     * The content-types supported via the IIIF interface.
     */
    public static final String[] CONTENT_TYPES = { PNG_CONTENT_TYPE, JPG_CONTENT_TYPE, GIF_CONTENT_TYPE,
        PDF_CONTENT_TYPE, JP2_CONTENT_TYPE, TIF_CONTENT_TYPE };
}
