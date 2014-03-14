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

package org.oclc.oomRef.entities;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import info.openurl.oom.entities.Entity;

/**
 * @author Jeffrey A. Young TODO Describe type
 */
public class EntityImpl implements Entity {

    private List descriptors;

    protected EntityImpl(final Object descriptors) {
        if (descriptors == null) {
            this.descriptors = null;
        } else if (descriptors instanceof Object[]) {
            this.descriptors = new ArrayList(Arrays.asList((Object[]) descriptors));
        } else {
            this.descriptors = new ArrayList(Arrays.asList(new Object[] { descriptors }));
        }
    }

    /**
     * Gets the descriptors.
     */
    @Override
    public Object[] getDescriptors() {
        return descriptors.toArray();
    }

    /**
     * Gets the descriptors for the supplied class.
     */
    @Override
    public Object[] getDescriptors(final Class c) {
        final ArrayList<Object> list = new ArrayList<Object>();
        for (int i = 0; i < descriptors.size(); ++i) {
            final Object descriptor = descriptors.get(i);
            if (c.isInstance(descriptor)) {
                list.add(descriptor);
            }
        }
        return list.toArray((Object[]) Array.newInstance(c, list.size()));
    }

    /**
     * Gets a string representation of the entity.
     */
    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        for (int i = 0; i < descriptors.size(); ++i) {
            sb.append(descriptors.get(i).toString()).append("\n");
        }
        return sb.toString();
    }

    /**
     * Adds a descriptor to the entity.
     * 
     * @param descriptor A descriptor
     */
    @Override
    public void addDescriptor(final Object descriptor) {
        descriptors.add(descriptor);
    }
}
