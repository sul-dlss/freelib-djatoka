package org.oclc.oomRef;

import info.openurl.oom.ContextObject;
import info.openurl.oom.OpenURLRequest;

import java.util.Map;

/**
 * An OpenURLRequest implementation.
 * 
 * @author Jeffrey A. Young
 */
public class OpenURLRequestImpl implements OpenURLRequest {
	private Map openURLKeys;
	private Map adminKeys;
	private Map foreignKeys;
	private ContextObject[] contextObjects;

	protected OpenURLRequestImpl(ContextObject contextObject) {
		this(null, new ContextObject[] { contextObject }, null);
	}
	
	OpenURLRequestImpl(ContextObject[] contextObjects) {
		this(null, contextObjects, null);
	}
	
	OpenURLRequestImpl(Map openURLKeys,
			ContextObject contextObject,
			Map foreignKeys) {
		this(openURLKeys, new ContextObject[] { contextObject }, foreignKeys);
	}

	OpenURLRequestImpl(Map openURLKeys, ContextObject[] contextObjects,
			Map foreignKeys) {
	    this(contextObjects, openURLKeys, null, foreignKeys);
	}
	
    public OpenURLRequestImpl(ContextObject contextObject, Map openURLKeys,
            Map adminKeys, Map foreignKeys) {
        this(new ContextObject[] { contextObject },
                openURLKeys,
                adminKeys,
                foreignKeys);
    }

    public OpenURLRequestImpl(ContextObject[] contextObjects, Map openURLKeys,
            Map adminKeys, Map foreignKeys) {
        this.openURLKeys = openURLKeys;
        this.adminKeys = adminKeys;
        this.foreignKeys = foreignKeys;
        this.contextObjects = contextObjects;
    }

    /**
     * @deprecated Proper Transports should consume OpenURL keys rather
     * than pass them on to the Service classes
     */
	public Map getOpenURLKeys() {
		return openURLKeys;
	}
	
    /**
     * @deprecated Proper Transports should consume foreign keys rather
     * than pass them on to the Service classes
     */
	public Map getForeignKeys() {
		return foreignKeys;
	}
	
	public Map getAdminKeys() {
	    return adminKeys;
	}
	
	public ContextObject[] getContextObjects() {
		return contextObjects;
	}
}
