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

import info.openurl.oom.ContextObject;
import info.openurl.oom.entities.Referent;
import info.openurl.oom.entities.Referrer;
import info.openurl.oom.entities.ReferringEntity;
import info.openurl.oom.entities.Requester;
import info.openurl.oom.entities.Resolver;
import info.openurl.oom.entities.ServiceType;

class ContextObjectImpl implements ContextObject {

    private final Referent referent;

    private final ReferringEntity[] referringEntities;

    private final Requester[] requesters;

    private final ServiceType[] serviceTypes;

    private final Resolver[] resolvers;

    private final Referrer[] referrers;

    ContextObjectImpl(final Referent referent, final ReferringEntity[] referringEntities,
            final Requester[] requesters, final ServiceType[] serviceTypes, final Resolver[] resolvers,
            final Referrer[] referrers) {
        this.referent = referent;
        this.referringEntities = referringEntities;
        this.requesters = requesters;
        this.serviceTypes = serviceTypes;
        this.resolvers = resolvers;
        this.referrers = referrers;
    }

    @Override
    public Referent getReferent() {
        return referent;
    }

    @Override
    public ServiceType[] getServiceTypes() {
        return serviceTypes;
    }

    @Override
    public ReferringEntity[] getReferringEntities() {
        return referringEntities;
    }

    @Override
    public Requester[] getRequesters() {
        return requesters;
    }

    @Override
    public Resolver[] getResolvers() {
        return resolvers;
    }

    @Override
    public Referrer[] getReferrers() {
        return referrers;
    }

    @Override
    public String toString() {
        return new StringBuffer(referent.toString()).append("\n").append(referringEntities).append("\n").append(
                requesters).append("\n").append(serviceTypes).append("\n").append(resolvers).append("\n").append(
                referrers).append("\n").toString();
    }
}
