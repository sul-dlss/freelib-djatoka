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

package info.openurl.oom.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import info.openurl.oom.entities.Entity;

/**
 * General-purpose static utility methods.
 *
 * @author Jeffrey A. Young
 */
public class OOMUtil {

    private OOMUtil() {
    }

    /**
     * @param queryString
     * @return a SortedMap of the parameters
     * @throws UnsupportedEncodingException
     */
    public static SortedMap getParameterMap(final String queryString) throws UnsupportedEncodingException {
        String[] queryStrings;
        if (queryString == null) {
            queryStrings = new String[0];
        } else {
            queryStrings = new String[] { queryString };
        }
        return getParameterMap(queryStrings);
    }

    /**
     * @param parameterMap
     * @param key
     * @return a parameter value
     */
    public static Object getSingleParameterValue(final Map parameterMap, final Object key) {
        Object parameterValue = null;

        final Object[] values = (Object[]) parameterMap.get(key);
        if (values != null && values.length > 0) {
            parameterValue = values[0];
        }

        return parameterValue;
    }

    /**
     * Transforms a queryString into a Map of String/Object[]
     *
     * @param queryStrings
     * @return a Map of key/values from the queryString.
     * @throws UnsupportedEncodingException
     */
    public static SortedMap getParameterMap(final String[] queryStrings) throws UnsupportedEncodingException {
        final HashMap tempMap = new HashMap();

        if (queryStrings != null) {
            for (final String queryString : queryStrings) {
                final String[] parameters = queryString.split("&");
                for (final String parameter2 : parameters) {
                    final String[] parameter = parameter2.split("=", 2);
                    final String key = URLDecoder.decode(parameter[0], "UTF-8");
                    final String value = URLDecoder.decode(parameter[1], "UTF-8");
                    ArrayList list = (ArrayList) tempMap.get(key);
                    if (list == null) {
                        list = new ArrayList();
                        tempMap.put(key, list);
                    }
                    if (parameter.length == 2) {
                        list.add(value);
                    } else {
                        list.add("");
                    }
                }
            }
        }

        // Switch the values from ArrayList to String[]
        final SortedMap parameterMap = new TreeMap();
        final Iterator iter = tempMap.entrySet().iterator();
        while (iter.hasNext()) {
            final Map.Entry entry = (Entry) iter.next();
            final String key = (String) entry.getKey();
            final ArrayList value = (ArrayList) entry.getValue();
            parameterMap.put(key, value.toArray(new String[value.size()]));
        }
        return parameterMap;
    }

    /**
     * Get the bytes out of an InputStream.
     *
     * @param is the stream to be read.
     * @return the bytes found in the stream.
     * @throws IOException
     */
    public static byte[] getBytes(final InputStream is) throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final byte[] bytes = new byte[1024];
        int len;
        while ((len = is.read(bytes)) != -1) {
            baos.write(bytes, 0, len);
        }
        return baos.toByteArray();
    }

    /**
     * Get a single Descriptor of the specified time from the Entity.
     *
     * @param entity
     * @param c
     * @return the first specified Descriptor
     */
    public static Object getSingleDescriptor(final Entity entity, final Class c) {
        if (entity != null) {
            final Object[] objects = entity.getDescriptors(c);
            if (objects != null && objects.length > 0) {
                return objects[0];
            }
        }
        return null;
    }
}
