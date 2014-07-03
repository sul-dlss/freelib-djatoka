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

package org.oclc.oomRef.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.TransformerException;

import org.apache.xpath.XPathAPI;
import org.oclc.util.XMLHelper;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;

/**
 * @author Jeffrey A. Young TODO Describe type
 */
@SuppressWarnings("deprecation")
public class ClassConfig implements info.openurl.oom.config.ClassConfig {

    private final Node classNode;

    ClassConfig(final Node classNode) {
        this.classNode = classNode;
    }

    /**
     * Gets the class name.
     */
    @Override
    public String getClassName() throws TransformerException {
        return XPathAPI.eval(classNode, "oomRef:className", XMLHelper.getXmlnsEl()).str();
    }

    /**
     * Gets the argument.
     */
    @Override
    public String getArg(final String key) throws TransformerException {
        final String xpath = new StringBuffer("oomRef:args/oomRef:").append(key).toString();
        return XPathAPI.eval(classNode, xpath, XMLHelper.getXmlnsEl()).str();
    }

    /**
     * Gets an arguments array.
     *
     * @param key A key to pull from the configuration
     */
    @Override
    public String[] getArgs(final String key) throws TransformerException {
        final ArrayList<String> args = new ArrayList<String>();

        final String xpath = new StringBuffer("oomRef:args/oomRef:").append(key).toString();
        final NodeIterator iter = XPathAPI.selectNodeIterator(classNode, xpath, XMLHelper.getXmlnsEl());
        Node node;
        while ((node = iter.nextNode()) != null) {
            args.add(XPathAPI.eval(node, ".").str());
        }
        return args.toArray(new String[args.size()]);
    }

    /**
     * Returns all the arguments.
     */
    @Override
    public Map getArgs() throws TransformerException {
        final Map map = new HashMap();

        if (classNode != null) {
            final NodeIterator iter = XPathAPI.selectNodeIterator(classNode, "oomRef:args/*", XMLHelper.getXmlnsEl());
            Node node;
            while ((node = iter.nextNode()) != null) {
                final String key = XPathAPI.eval(node, "name()").str();
                map.put(key, XPathAPI.eval(node, ".").str());
            }
        }
        return map;
    }

}
