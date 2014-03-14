
package info.freelibrary.djatoka.iiif;

import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An interface for an IIIF request (information or image).
 * 
 * @author <a href="mailto:ksclarke@gmail.com">Kevin S. Clarke</a>
 */
public interface IIIFRequest {

    /**
     * The session key for an <code>IIIFRequest</code>.
     */
    public static final String KEY = "IIIF_REQUEST";

    /**
     * Gets the identifier for the request.
     * 
     * @return The identifier for the request
     */
    public String getIdentifier();

    /**
     * Gets the extension for the request.
     * 
     * @return The extension for the request
     */
    public String getExtension();

    /**
     * Returns true if the request has an extension; else, false
     * 
     * @return True if the request has an extension; else, false
     */
    public boolean hasExtension();

    /**
     * Returns the service prefix of the request.
     * 
     * @return The service prefix of the request
     */
    public String getServicePrefix();

    /**
     * Returns true if the request has a service prefix; else, false
     * 
     * @return True if the request has a service prefix; else, false
     */
    public boolean hasServicePrefix();

    /**
     * A request builder that builds either a {@link ImageRequest} or a {@link InfoRequest}.
     * 
     * @author <a href="mailto:ksclarke@gmail.com">Kevin S. Clarke</a>
     */
    public static class Builder {

        private static final Logger LOGGER = LoggerFactory.getLogger(Builder.class);

        /**
         * Returns a <code>IIIFRequest</code> for the supplied {@link URL}.
         * 
         * @param aURL A {@link URL} representing the <code>IIIFRequest</code>
         * @return An implementation of the {@link IIIFRequest} interface
         * @throws IIIFException If there is a problem creating the request
         */
        public static IIIFRequest getRequest(URL aURL) throws IIIFException {
            return getRequest(aURL, null);
        }

        /**
         * Returns a <code>IIIFRequest</code> for the supplied {@link URL}.
         * 
         * @param aURL A {@link URL} representing the <code>IIIFRequest</code>
         * @param aServicePrefix A pre-configured prefix to use in parsing the request
         * @return An implementation of the {@link IIIFRequest} interface
         * @throws IIIFException If there is a problem creating the request
         */
        public static IIIFRequest getRequest(URL aURL, String aServicePrefix) throws IIIFException {
            String path = aURL.getPath();

            if (path.endsWith(".xml") || path.endsWith(".json")) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("IIIF InfoRequest created from: {}", aURL);
                }

                return new InfoRequest(aURL, aServicePrefix);
            } else {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("IIIF ImageRequest created from: {}", aURL);
                }

                // May not be an ImageRequest if the filter is misconfigured
                return new ImageRequest(aURL, aServicePrefix);
            }
        }

        /**
         * Checks for a service prefix.
         * 
         * @param aServicePrefix A service prefix to check for in the request
         * @return The service prefix
         */
        static String checkServicePrefix(String aServicePrefix) {
            if (aServicePrefix != null) {
                String servicePrefix;

                if (aServicePrefix.startsWith("/")) {
                    servicePrefix = aServicePrefix.substring(1);
                } else {
                    servicePrefix = aServicePrefix;
                }

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Service prefix parsed: {}", servicePrefix);
                }

                return servicePrefix;
            } else {
                return aServicePrefix;
            }
        }
    }
}
