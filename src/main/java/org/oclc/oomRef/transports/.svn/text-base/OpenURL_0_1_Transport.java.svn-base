/**
 * Copyright 2006 OCLC Online Computer Library Center Licensed under the Apache
 * License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or
 * agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.oclc.oomRef.transports;

import info.openurl.oom.ContextObject;
import info.openurl.oom.OpenURLException;
import info.openurl.oom.OpenURLRequest;
import info.openurl.oom.OpenURLRequestProcessor;
import info.openurl.oom.Service;
import info.openurl.oom.Transport;
import info.openurl.oom.config.ClassConfig;
import info.openurl.oom.config.OpenURLConfig;
import info.openurl.oom.entities.Referent;
import info.openurl.oom.entities.Referrer;
import info.openurl.oom.entities.ReferringEntity;
import info.openurl.oom.entities.Requester;
import info.openurl.oom.entities.Resolver;
import info.openurl.oom.entities.ServiceType;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.oclc.oomRef.descriptors.ByReferenceMetadataImpl;
import org.oclc.oomRef.descriptors.ByValueMetadataKevImpl;

/**
 * @author Jeffrey A. Young
 *
 * This class transforms HTTP requests into OpenURL
 * ContextObjects. Override this class to change the request
 * pattern. Configure the Servlet to use your new class by
 * adding/changing the following property to the Servlet.props
 * file:
 * 
 * Servlet.transportClassname={packageName.className}
 */

public class OpenURL_0_1_Transport implements Transport {
    private OpenURLConfig openURLConfig;
//    private ClassConfig classConfig;
    
	/**
	 * Construct an HTTP OpenURL Inline Transport object
	 * 
	 * @param openURLConfig
	 * @param classConfig
	 */
	public OpenURL_0_1_Transport(OpenURLConfig openURLConfig,
            ClassConfig classConfig) {
        this.openURLConfig = openURLConfig;
//        this.classConfig = classConfig;
	}

	public OpenURLRequest toOpenURLRequest(
			OpenURLRequestProcessor processor,
			HttpServletRequest req) 
	throws OpenURLException {
		try {
			// Assume this is a 0.1 request
			
	        String url_ver = "Z39.88-2004";
	        String url_ctx_fmt = "info:ofi/fmt:kev:mtx:ctx";
	        
	        Set entrySet = req.getParameterMap().entrySet();
	        Iterator iter = entrySet.iterator();
	        
	    	ArrayList referentDescriptors = new ArrayList();
	    	ArrayList requesterDescriptors = new ArrayList();
	    	ArrayList referringEntityDescriptors = new ArrayList();
	    	ArrayList referrerDescriptors = new ArrayList();
	    	ArrayList resolverDescriptors = new ArrayList();
	    	ArrayList serviceTypeDescriptors = new ArrayList();
	    	HashMap openURLKeys = new HashMap();
	    	HashMap adminKeys = new HashMap();
	    	HashMap foreignKeys = new HashMap();
	    	
    		openURLKeys.put("url_ver", url_ver);
    		openURLKeys.put("url_ctx_fmt", url_ctx_fmt);
    		
	    	/* It's a bad idea for Service class implementations
	    	 * to use this foreign key because it unnecessarily
	    	 * binds the service to the HTTP protocol. I've
	    	 * included it, though, because some services
	    	 * really are bound to HTTP.
	    	 */
	    	foreignKeys.put("HttpServletRequest", req);
	    	
	        while (iter.hasNext()) {
	        	Map.Entry entry = (Entry) iter.next();
	        	String key = (String) entry.getKey();
	        	String[] values = (String[]) entry.getValue();
	        	
	        	if ("sid".equals(key)) {
	            	for (int i=0; i<values.length; ++i) {
		    	        referrerDescriptors.add(new URI("info:sid/" + values[i]));
	            	}
	    		} else if ("id".equals(key)) {
	            	for (int i=0; i<values.length; ++i) {
		    	        referentDescriptors.add("info:" + values[i]);
	            	}
	    		} else {
	    			ByValueMetadataKevImpl bvm =
	    				new ByValueMetadataKevImpl(entrySet);
	    			referentDescriptors.add(bvm);
	    		}
	        }
	        
	        if (serviceTypeDescriptors.size() == 0) {
                URI uri = new URI("info:localhost/svc_id/default");
                
                serviceTypeDescriptors.add(uri);
                
                // Throw in the corresponding Java class while we're here
                Service service = (Service) openURLConfig.getService(uri);
                serviceTypeDescriptors.add(service);
	        }
	        
	    	Referent referent =
	    		processor.referentFactory(referentDescriptors.toArray());
	    	Requester requester =
	    		processor.requesterFactory(requesterDescriptors.toArray());
	    	ReferringEntity referringEntity =
	    		processor.referringEntityFactory(referringEntityDescriptors.toArray());
	    	Referrer referrer =
	    		processor.referrerFactory(referrerDescriptors.toArray());
	    	Resolver resolver =
	    		processor.resolverFactory(resolverDescriptors.toArray());
	    	ServiceType serviceType =
	    		processor.serviceTypeFactory(serviceTypeDescriptors.toArray());
	    	
	        // Construct the ContextObject
	        ContextObject contextObject = processor.contextObjectFactory(
                    referent,
	                new ReferringEntity[] { referringEntity },
	                new Requester[] { requester },
	                new ServiceType[] { serviceType },
	                new Resolver[] { resolver },
	                new Referrer[] { referrer });
	        return processor.openURLRequestFactory(contextObject,
	                openURLKeys,
	                adminKeys,
	                foreignKeys);
		} catch (Exception e) {
			throw new OpenURLException(e.getMessage(), e);
		}
	}

    public URI getTransportID() throws URISyntaxException {
        return new URI("info:ofi/tsp:http:openurl-inline");
    }
}
