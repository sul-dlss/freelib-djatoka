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

package org.oclc.oomRef;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Set;

import org.oclc.oomRef.descriptors.ByReferenceMetadataImpl;
import org.oclc.oomRef.descriptors.ByValueMetadataImpl;
import org.oclc.oomRef.descriptors.ByValueMetadataKevImpl;
import org.oclc.oomRef.descriptors.ByValueMetadataXmlImpl;
import org.oclc.oomRef.entities.ReferentImpl;
import org.oclc.oomRef.entities.ReferrerImpl;
import org.oclc.oomRef.entities.ReferringEntityImpl;
import org.oclc.oomRef.entities.RequesterImpl;
import org.oclc.oomRef.entities.ResolverImpl;
import org.oclc.oomRef.entities.ServiceTypeImpl;
import org.w3c.dom.Document;

import info.openurl.oom.ContextObject;
import info.openurl.oom.OpenURLException;
import info.openurl.oom.OpenURLRequest;
import info.openurl.oom.OpenURLRequestProcessor;
import info.openurl.oom.OpenURLResponse;
import info.openurl.oom.Service;
import info.openurl.oom.descriptors.ByReferenceMetadata;
import info.openurl.oom.descriptors.ByValueMetadata;
import info.openurl.oom.descriptors.ByValueMetadataKev;
import info.openurl.oom.descriptors.ByValueMetadataXml;
import info.openurl.oom.entities.Referent;
import info.openurl.oom.entities.Referrer;
import info.openurl.oom.entities.ReferringEntity;
import info.openurl.oom.entities.Requester;
import info.openurl.oom.entities.Resolver;
import info.openurl.oom.entities.ServiceType;

/**
 * For the sake of simplicity, this resolver assumes that multiple ServiceType Identifiers should be processed in
 * sequence until one of them returns a Response instead of null. For example, the ContextObject could specify two
 * services: 1) The desired service 2) A failover service
 *
 * @author Jeffrey A. Young
 */
public class OpenURLRequestProcessorImpl implements OpenURLRequestProcessor {

    /**
     * Gets the processor ID.
     */
    @Override
    public URI getProcessorID() throws URISyntaxException {
        return new URI("info:oom/oomProcessors/OOMRef-J");
    }

    /**
     * Resolves the OpenURLResponse.
     */
    @Override
    public OpenURLResponse resolve(final OpenURLRequest openURLRequest) throws OpenURLException {
        OpenURLResponse response = null;

        // Try each ContentObject until someone responds
        final ContextObject[] contextObjects = openURLRequest.getContextObjects();
        for (int i = 0; response == null && i < contextObjects.length; ++i) {
            final ContextObject contextObject = contextObjects[i];
            final ServiceType[] serviceTypes = contextObject.getServiceTypes();

            // Try each ServiceType/Service until someone responds
            for (int j = 0; response == null && j < serviceTypes.length; ++j) {
                final ServiceType serviceType = serviceTypes[j];
                final Object[] descriptors = serviceType.getDescriptors();

                // Try each Service until someone responds
                for (int k = 0; response == null && k < descriptors.length; ++k) {
                    final Object descriptor = descriptors[k];
                    if (descriptor instanceof Service) {
                        final Service service = (Service) descriptor;
                        try {
                            response = service.resolve(
                            // this,
                                    serviceType, contextObject, openURLRequest, this);
                        } catch (final Exception e) {
                            throw new OpenURLException(e.getMessage(), e);
                        }
                    }
                }
            }
        }
        return response;
    }

    /**
     * Creates a context object.
     */
    @Override
    public ContextObject contextObjectFactory(final Referent ref, final ReferringEntity refEntity,
            final Requester req, final ServiceType serviceType, final Resolver resolver, final Referrer referrer) {
        ReferringEntity[] referringEntities = null;
        Requester[] requesters = null;
        ServiceType[] serviceTypes = null;
        Resolver[] resolvers = null;
        Referrer[] referrers = null;

        if (refEntity != null) {
            referringEntities = new ReferringEntity[] { refEntity };
        }
        if (req != null) {
            requesters = new Requester[] { req };
        }
        if (serviceType != null) {
            serviceTypes = new ServiceType[] { serviceType };
        }
        if (resolver != null) {
            resolvers = new Resolver[] { resolver };
        }
        if (referrer != null) {
            referrers = new Referrer[] { referrer };
        }

        return contextObjectFactory(ref, referringEntities, requesters, serviceTypes, resolvers, referrers);
    }

    /**
     * Returns a context object using a factory.
     *
     * @param referent A referent
     * @param referringEntities Referring entities
     */
    @Override
    public ContextObject contextObjectFactory(final Referent referent, final ReferringEntity[] referringEntities,
            final Requester[] requesters, final ServiceType[] serviceTypes, final Resolver[] resolvers,
            final Referrer[] referrers) {
        return new ContextObjectImpl(referent, referringEntities, requesters, serviceTypes, resolvers, referrers);
    }

    /**
     * Creates a referent.
     */
    @Override
    public Referent referentFactory(final Object object) {
        return new ReferentImpl(object);
    }

    /**
     * Creates a service type.
     */
    @Override
    public ServiceType serviceTypeFactory(final Object object) {
        return new ServiceTypeImpl(object);
    }

    /**
     * Creates a requester.
     */
    @Override
    public Requester requesterFactory(final Object object) {
        return new RequesterImpl(object);
    }

    /**
     * Creates a referring entity.
     */
    @Override
    public ReferringEntity referringEntityFactory(final Object object) {
        return new ReferringEntityImpl(object);
    }

    /**
     * Creates a referrer.
     */
    @Override
    public Referrer referrerFactory(final Object object) {
        return new ReferrerImpl(object);
    }

    /**
     * Creates a resolver.
     */
    @Override
    public Resolver resolverFactory(final Object object) {
        return new ResolverImpl(object);
    }

    /**
     * Creates a ByReferenceMetadata object.
     */
    @Override
    public ByReferenceMetadata byReferenceMetadataFactory(final URI ref_fmt, final URL ref) {
        return new ByReferenceMetadataImpl(ref_fmt, ref);
    }

    /**
     * Creates a ByValueMetadata object.
     */
    @Override
    public ByValueMetadata byValueMetadataFactory(final URI val_fmt, final String prefix, final Set entrySet) {
        return new ByValueMetadataImpl(val_fmt, prefix, entrySet);
    }

    /**
     * Creates a ByValueMetadataKev.
     */
    @Override
    public ByValueMetadataKev byValueMetadataKevFactory(final URI val_fmt, final String prefix, final Set entrySet) {
        return new ByValueMetadataKevImpl(val_fmt, prefix, entrySet);
    }

    /**
     * Creates a ByValueMetadataXml.
     */
    @Override
    public ByValueMetadataXml byValueMetadataXmlFactory(final URI val_fmt, final Document xmlDoc) {
        return new ByValueMetadataXmlImpl(val_fmt, xmlDoc);
    }

    /**
     * Creates an OpenURLRequest.
     */
    @Override
    public OpenURLRequest openURLRequestFactory(final ContextObject contextObject) {
        return new OpenURLRequestImpl(contextObject);
    }

    /**
     * Creates an OpenURLResponse.
     */
    @Override
    public OpenURLResponse openURLResponseFactory(final int status, final String redirectURL,
            final String contentType, final byte[] bytes) {
        return new OpenURLResponse(status, redirectURL, contentType, bytes);
    }
}
