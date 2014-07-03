
package org.oclc.oomRef;

import java.util.Map;

import info.openurl.oom.ContextObject;
import info.openurl.oom.OpenURLRequest;

/**
 * An OpenURLRequest implementation.
 *
 * @author Jeffrey A. Young
 */
public class OpenURLRequestImpl implements OpenURLRequest {

    private final Map openURLKeys;

    private final Map foreignKeys;

    private final ContextObject[] contextObjects;

    protected OpenURLRequestImpl(final ContextObject contextObject) {
        this(null, new ContextObject[] { contextObject }, null);
    }

    OpenURLRequestImpl(final ContextObject[] contextObjects) {
        this(null, contextObjects, null);
    }

    OpenURLRequestImpl(final Map openURLKeys, final ContextObject contextObject, final Map foreignKeys) {
        this(openURLKeys, new ContextObject[] { contextObject }, foreignKeys);
    }

    OpenURLRequestImpl(final Map openURLKeys, final ContextObject[] contextObjects, final Map foreignKeys) {
        this.openURLKeys = openURLKeys;
        this.contextObjects = contextObjects;
        this.foreignKeys = foreignKeys;
    }

    /**
     * @deprecated Proper Transports should consume OpenURL keys rather than pass them on to the Service classes
     */
    @Deprecated
    @Override
    public Map getOpenURLKeys() {
        return openURLKeys;
    }

    /**
     * @deprecated Proper Transports should consume foreign keys rather than pass them on to the Service classes
     */
    @Deprecated
    @Override
    public Map getForeignKeys() {
        return foreignKeys;
    }

    /**
     * Gets the context objects.
     */
    @Override
    public ContextObject[] getContextObjects() {
        return contextObjects;
    }
}
