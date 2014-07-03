
package org.oclc.oomRef;

import java.net.URI;

import javax.servlet.http.HttpServletRequest;

import info.openurl.oom.OpenURLException;
import info.openurl.oom.OpenURLRequest;
import info.openurl.oom.OpenURLRequestProcessor;
import info.openurl.oom.Service;
import info.openurl.oom.Transport;
import info.openurl.oom.config.ClassConfig;
import info.openurl.oom.config.OpenURLConfig;

/**
 * This class is obsolete and will be removed in a future version.
 *
 * @author Jeffrey A. Young
 * @deprecated Don't extend this class. It's not necessary, and doing so tightly couples your Transports with the
 *             OOMRef-J implementation.
 */
@Deprecated
public abstract class TransportImpl implements Transport {

    protected OpenURLConfig openURLConfig;

    protected ClassConfig classConfig;

    private URI transportID;

    /**
     * @param openURLConfig
     * @param classConfig
     * @param transportID
     * @deprecated Don't extend this class. It's not necessary, and doing so tightly couples your Transports with the
     *             OOMRef-J implementation.
     */
    @Deprecated
    public TransportImpl(final OpenURLConfig openURLConfig, final ClassConfig classConfig, final URI transportID) {
        this.openURLConfig = openURLConfig;
        this.classConfig = classConfig;
        this.transportID = transportID;
    }

    /**
     * @deprecated Don't extend this class. It's not necessary, and doing so tightly couples your Transports with the
     *             OOMRef-J implementation.
     */
    @Deprecated
    public TransportImpl() {
    }

    /**
     * @deprecated Don't extend this class. It's not necessary, and doing so tightly couples your Transports with the
     *             OOMRef-J implementation. You should remove the extends clause and implement
     *             info.openurl.oom.Transport directly.
     */
    @Deprecated
    @Override
    public URI getTransportID() {
        return transportID;
    }

    /**
     * @param className
     * @return a Service instantiation
     * @throws OpenURLException
     * @deprecated Don't extend this class. It's not necessary, and doing so tightly couples your Transports with the
     *             OOMRef-J implementation. You should remove the extends clause and implement
     *             info.openurl.oom.Transport directly.
     */
    @Deprecated
    public Service getService(final String className) throws OpenURLException {
        try {
            return openURLConfig.getService(className);
        } catch (final Exception e) {
            e.printStackTrace();
            throw new OpenURLException(e.getMessage(), e);
        }
    }

    /**
     * @param processor - this is required because different OpenURL implementations can implement the ContextObject
     *        interface in proprietary ways.
     * @param req
     * @return an OpenURLRequest representation of the incoming HttpServletRequest
     * @throws OpenURLException
     * @deprecated Don't extend this class. It's not necessary, and doing so tightly couples your Transports with the
     *             OOMRef-J implementation. You should remove the extends clause and implement
     *             info.openurl.oom.Transport directly.
     */
    @Deprecated
    @Override
    public abstract OpenURLRequest toOpenURLRequest(OpenURLRequestProcessor processor, HttpServletRequest req)
            throws OpenURLException;
}