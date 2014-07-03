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

package org.oclc.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * General-purpose static utility methods.
 *
 * @author Jeffrey A. Young
 * @deprecated Use info.openurl.oom.util.OOMUtil instead
 */
@Deprecated
public class OOMUtil {

    private OOMUtil() {
    }

    /**
     * Gets a parameter map.
     *
     * @param queryString
     * @return A sorted map with the parameters
     */
    public static SortedMap getParameterMap(final String queryString) {
        return getParameterMap(new String[] { queryString });
    }

    /**
     * Transforms a queryString into a Map of String/Object[]
     *
     * @param queryStrings
     * @return a Map of key/values from the queryString.
     */
    public static SortedMap getParameterMap(final String[] queryStrings) {
        final HashMap tempMap = new HashMap();

        if (queryStrings != null) {
            for (final String queryString : queryStrings) {
                final String[] parameters = queryString.split("&");
                for (final String parameter2 : parameters) {
                    final String[] parameter = parameter2.split("=", 2);
                    ArrayList list = (ArrayList) tempMap.get(parameter[0]);
                    if (list == null) {
                        list = new ArrayList();
                        tempMap.put(parameter[0], list);
                    }
                    if (parameter.length == 2) {
                        list.add(parameter[1]);
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
}
