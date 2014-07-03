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

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.oclc.oomRef.descriptors.ByReferenceMetadataImpl;
import org.oclc.oomRef.descriptors.ByValueMetadataImpl;

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

/**
 * @author Jeffrey A. Young This class transforms HTTP requests into OpenURL ContextObjects. Override this class to
 *         change the request pattern. Configure the Servlet to use your new class by adding/changing the following
 *         property to the Servlet.props file: Servlet.transportClassname={packageName.className}
 */

public class HttpOpenURLInlineTransport implements Transport {

    private final OpenURLConfig openURLConfig;

    // private ClassConfig classConfig;

    /**
     * Construct an HTTP OpenURL Inline Transport object
     *
     * @param openURLConfig
     * @param classConfig
     */
    public HttpOpenURLInlineTransport(final OpenURLConfig openURLConfig, final ClassConfig classConfig) {
        this.openURLConfig = openURLConfig;
        // this.classConfig = classConfig;
    }

    /**
     * Gets an OpenURLRequest from the supplied HttpServletRequest and processor.
     */
    @Override
    public OpenURLRequest toOpenURLRequest(final OpenURLRequestProcessor processor, final HttpServletRequest req)
            throws OpenURLException {
        try {
            String url_ver = null;

            // url_ver=Z39.88-2004 is the only acceptable value
            final String[] url_vers = req.getParameterValues("url_ver");

            if (url_vers != null) {
                for (int i = 0; url_ver == null && i < url_vers.length; ++i) {
                    if ("Z39.88-2004".equals(url_vers[i])) {
                        url_ver = url_vers[i];
                    }
                }
            }

            /*
             * url_ctx_fmt=null or info:ofi/fmt:kev:mtx:ctx are the only acceptable values
             */

            String url_ctx_fmt = "info:ofi/fmt:kev:mtx:ctx";
            final String[] url_ctx_fmts = req.getParameterValues("url_ctx_fmt");
            if (url_ctx_fmts != null) {
                for (int i = 0; "info:ofi/fmt:kev:mtx:ctx".equals(url_ctx_fmt); ++i) {
                    if (url_ctx_fmts[i].length() > 0 && !url_ctx_fmts[i].equals(url_ctx_fmt)) {
                        url_ctx_fmt = url_ctx_fmts[i];
                    }
                }
            }

            if (!("Z39.88-2004".equals(url_ver) && url_ctx_fmt.equals("info:ofi/fmt:kev:mtx:ctx"))) {
                // sorry, this isn't our type of request
                return null;
            }

            final Set entrySet = req.getParameterMap().entrySet();
            final Iterator iter = entrySet.iterator();

            final ArrayList referentDescriptors = new ArrayList();
            final ArrayList requesterDescriptors = new ArrayList();
            final ArrayList referringEntityDescriptors = new ArrayList();
            final ArrayList referrerDescriptors = new ArrayList();
            final ArrayList resolverDescriptors = new ArrayList();
            final ArrayList serviceTypeDescriptors = new ArrayList();
            final HashMap openURLKeys = new HashMap();
            final HashMap adminKeys = new HashMap();
            final HashMap foreignKeys = new HashMap();

            while (iter.hasNext()) {
                final Map.Entry entry = (Entry) iter.next();
                final String key = (String) entry.getKey();
                final String[] values = (String[]) entry.getValue();

                if (key.startsWith("url_")) {
                    openURLKeys.put(key, values);
                } else if (key.startsWith("ctx_")) {
                    adminKeys.put(key, values);
                } else if ("rft_id".equals(key)) {
                    for (int i = 0; i < values.length; ++i) {
                        values[i] = URLEncoder.encode(values[i], "UTF-8");
                        referentDescriptors.add(new URI(values[i]));
                    }
                } else if ("rft_dat".equals(key)) {
                    for (final String value : values) {
                        referentDescriptors.add(value);
                    }
                } else if ("rft_val_fmt".equals(key)) {
                    for (final String value : values) {
                        final ByValueMetadataImpl bvm = new ByValueMetadataImpl(new URI(value), "rft.", entrySet);
                        referentDescriptors.add(bvm);
                    }
                } else if ("rft_ref_fmt".equals(key)) {
                    for (final String value : values) {
                        final String[] rft_refs = req.getParameterValues("rft_ref");
                        for (final String rft_ref : rft_refs) {
                            final ByReferenceMetadataImpl brm =
                                    new ByReferenceMetadataImpl(new URI(value), new URL(rft_ref));
                            referentDescriptors.add(brm);
                        }
                    }
                } else if ("req_id".equals(key)) {
                    for (final String value : values) {
                        requesterDescriptors.add(new URI(value));
                    }
                } else if ("req_dat".equals(key)) {
                    for (final String value : values) {
                        requesterDescriptors.add(value);
                    }
                } else if ("req_val_fmt".equals(key)) {
                    for (final String value : values) {
                        final ByValueMetadataImpl bvm = new ByValueMetadataImpl(new URI(value), "req.", entrySet);
                        requesterDescriptors.add(bvm);
                    }
                } else if ("req_ref_fmt".equals(key)) {
                    for (final String value : values) {
                        final String[] req_refs = req.getParameterValues("req_ref");
                        for (final String req_ref : req_refs) {
                            final ByReferenceMetadataImpl brm =
                                    new ByReferenceMetadataImpl(new URI(value), new URL(req_ref));
                            requesterDescriptors.add(brm);
                        }
                    }
                } else if ("rfe_id".equals(key)) {
                    for (final String value : values) {
                        referringEntityDescriptors.add(new URI(value));
                    }
                } else if ("rfe_dat".equals(key)) {
                    for (final String value : values) {
                        referringEntityDescriptors.add(value);
                    }
                } else if ("rfe_val_fmt".equals(key)) {
                    for (final String value : values) {
                        final ByValueMetadataImpl bvm = new ByValueMetadataImpl(new URI(value), "rfe.", entrySet);
                        referringEntityDescriptors.add(bvm);
                    }
                } else if ("rfe_ref_fmt".equals(key)) {
                    for (final String value : values) {
                        final String[] rfe_refs = req.getParameterValues("rfe_ref");
                        for (final String rfe_ref : rfe_refs) {
                            final ByReferenceMetadataImpl brm =
                                    new ByReferenceMetadataImpl(new URI(value), new URL(rfe_ref));
                            referringEntityDescriptors.add(brm);
                        }
                    }
                } else if ("rfr_id".equals(key)) {
                    for (final String value : values) {
                        referrerDescriptors.add(new URI(value));
                    }
                } else if ("rfr_dat".equals(key)) {
                    for (final String value : values) {
                        referrerDescriptors.add(value);
                    }
                } else if ("rfr_val_fmt".equals(key)) {
                    for (final String value : values) {
                        final ByValueMetadataImpl bvm = new ByValueMetadataImpl(new URI(value), "rfr.", entrySet);
                        referrerDescriptors.add(bvm);
                    }
                } else if ("rfr_ref_fmt".equals(key)) {
                    for (final String value : values) {
                        final String[] rfr_refs = req.getParameterValues("rfr_ref");
                        for (final String rfr_ref : rfr_refs) {
                            final ByReferenceMetadataImpl brm =
                                    new ByReferenceMetadataImpl(new URI(value), new URL(rfr_ref));
                            referrerDescriptors.add(brm);
                        }
                    }
                } else if ("res_id".equals(key)) {
                    for (final String value : values) {
                        resolverDescriptors.add(new URI(value));
                    }
                } else if ("res_dat".equals(key)) {
                    for (final String value : values) {
                        resolverDescriptors.add(value);
                    }
                } else if ("res_val_fmt".equals(key)) {
                    for (final String value : values) {
                        final ByValueMetadataImpl bvm = new ByValueMetadataImpl(new URI(value), "res.", entrySet);
                        resolverDescriptors.add(bvm);
                    }
                } else if ("res_ref_fmt".equals(key)) {
                    for (final String value : values) {
                        final String[] res_refs = req.getParameterValues("res_ref");
                        for (final String res_ref : res_refs) {
                            final ByReferenceMetadataImpl brm =
                                    new ByReferenceMetadataImpl(new URI(value), new URL(res_ref));
                            resolverDescriptors.add(brm);
                        }
                    }
                } else if ("svc_id".equals(key)) {
                    for (final String value : values) {
                        final URI uri = new URI(value);

                        serviceTypeDescriptors.add(uri);

                        // Throw in the corresponding Java class while we're
                        // here
                        final Service service = openURLConfig.getService(uri);
                        serviceTypeDescriptors.add(service);
                    }
                } else if ("svc_dat".equals(key)) {
                    for (final String value : values) {
                        serviceTypeDescriptors.add(value);
                    }
                } else if ("svc_val_fmt".equals(key)) {
                    for (final String value : values) {
                        final ByValueMetadataImpl bvm = new ByValueMetadataImpl(new URI(value), "svc.", entrySet);
                        serviceTypeDescriptors.add(bvm);
                    }
                } else if ("svc_ref_fmt".equals(key)) {
                    for (final String value : values) {
                        final String[] svc_refs = req.getParameterValues("svc_ref");
                        for (final String svc_ref : svc_refs) {
                            final ByReferenceMetadataImpl brm =
                                    new ByReferenceMetadataImpl(new URI(value), new URL(svc_ref));
                            serviceTypeDescriptors.add(brm);
                        }
                    }
                } else if (key.startsWith("rft.") || key.startsWith("rfe.") || key.startsWith("req.") ||
                        key.startsWith("rfr.") || key.startsWith("res.") || key.startsWith("svc.")) {
                    // do nothing
                } else {
                    foreignKeys.put(key, values);
                }
            }

            final Referent referent = processor.referentFactory(referentDescriptors.toArray());
            final Requester requester = processor.requesterFactory(requesterDescriptors.toArray());
            final ReferringEntity referringEntity =
                    processor.referringEntityFactory(referringEntityDescriptors.toArray());
            final Referrer referrer = processor.referrerFactory(referrerDescriptors.toArray());
            final Resolver resolver = processor.resolverFactory(resolverDescriptors.toArray());
            final ServiceType serviceType = processor.serviceTypeFactory(serviceTypeDescriptors.toArray());

            // Construct the ContextObject
            final ContextObject contextObject =
                    processor.contextObjectFactory(referent, new ReferringEntity[] { referringEntity },
                            new Requester[] { requester }, new ServiceType[] { serviceType },
                            new Resolver[] { resolver }, new Referrer[] { referrer });
            return processor.openURLRequestFactory(contextObject);
        } catch (final Exception e) {
            throw new OpenURLException(e.getMessage(), e);
        }
    }

    /**
     * Gets the transport ID.
     */
    @Override
    public URI getTransportID() throws URISyntaxException {
        return new URI("info:ofi/tsp:http:openurl-inline");
    }
}
