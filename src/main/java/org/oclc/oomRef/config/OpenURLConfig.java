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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.xml.transform.TransformerException;

import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;

import info.openurl.oom.OpenURLRequestProcessor;
import info.openurl.oom.Service;
import info.openurl.oom.Transport;
import info.openurl.oom.util.XMLHelper;

/**
 * @author Jeffrey A. Young TODO Describe type
 */
public class OpenURLConfig implements info.openurl.oom.config.OpenURLConfig {

    private static final Class[] CONFIG_CLASSES = new Class[] { info.openurl.oom.config.OpenURLConfig.class,
        info.openurl.oom.config.ClassConfig.class };

    private final ServletConfig servletConfig;

    private static Document oomConfig;
    static {
        try {
            oomConfig =
                    XMLHelper.parse(Thread.currentThread().getContextClassLoader().getResourceAsStream("oomRef.xml"));
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param config
     */
    public OpenURLConfig(final ServletConfig config) {
        servletConfig = config;
    }

    /**
     * @return The servlet configuration
     */
    @Override
    public ServletConfig getServletConfig() {
        return servletConfig;
    }

    /**
     * Gets the configured transports.
     */
    @Override
    public Transport[] getTransports() throws TransformerException, ClassNotFoundException, SecurityException,
            NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException,
            InvocationTargetException {
        final ArrayList transports = new ArrayList();
        final Element xmlnsEl = XMLHelper.getXmlnsEl();
        final NodeIterator nodeIter =
                XPathAPI.selectNodeIterator(oomConfig, "/oomRef:config/oomRef:transportMap/oomRef:transport", xmlnsEl);
        Node node;
        while ((node = nodeIter.nextNode()) != null) {
            final ClassConfig classConfig = new ClassConfig(node);
            final String transportClassName = classConfig.getClassName();
            final Class transportClass = Class.forName(transportClassName);
            Constructor transportConstructor = null;
            try {
                transportConstructor = transportClass.getConstructor(CONFIG_CLASSES);
            } catch (final NoSuchMethodException e) {
                // Uh Oh. Somebody extended OOMRef-J's HowImpl directly.
                transportConstructor = transportClass.getConstructor(CONFIG_CLASSES);
            }
            final Transport transport =
                    (Transport) transportConstructor.newInstance(new Object[] { this, classConfig });
            transports.add(transport);
        }
        return (Transport[]) transports.toArray(new Transport[transports.size()]);
    }

    /**
     * Gets the service associated with the supplied URI.
     */
    @Override
    public Service getService(final URI uri) throws TransformerException, ClassNotFoundException, SecurityException,
            NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException,
            InvocationTargetException {
        final Element xmlnsEl = XMLHelper.getXmlnsEl();
        final Node node =
                XPathAPI.selectSingleNode(oomConfig, "/oomRef:config/oomRef:serviceMap/oomRef:service[@ID='" +
                        uri.toString() + "']", xmlnsEl);
        if (node != null) {
            final ClassConfig classConfig = new ClassConfig(node);
            final String className = XPathAPI.eval(node, "oomRef:className", xmlnsEl).str();
            final Class serviceClass = Class.forName(className);
            Constructor serviceConstructor = null;
            try {
                serviceConstructor = serviceClass.getConstructor(CONFIG_CLASSES);
            } catch (final NoSuchMethodException e) {
                // Uh Oh. Somebody implemented an OOMRef-J class directly.
                serviceConstructor = serviceClass.getConstructor(CONFIG_CLASSES);
            }
            return (Service) serviceConstructor.newInstance(new Object[] { this, classConfig });
        }

        return null;
    }

    /**
     * Gets the service associated with the supplied class name.
     */
    @Override
    public Service getService(final String className) throws TransformerException, SecurityException,
            NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException,
            InvocationTargetException {
        try {
            final Node node =
                    XPathAPI.selectSingleNode(oomConfig,
                            "/oomRef:config/oomRef:serviceMap/oomRef:service[oomRef:className='" + className + "']",
                            XMLHelper.getXmlnsEl());
            final ClassConfig classConfig = new ClassConfig(node);
            final Class serviceClass = Class.forName(className);
            Constructor serviceConstructor = null;
            try {
                serviceConstructor = serviceClass.getConstructor(CONFIG_CLASSES);
            } catch (final NoSuchMethodException e) {
                // Uh Oh. Somebody extended the ServiceImpl directly.
                serviceConstructor = serviceClass.getConstructor(CONFIG_CLASSES);
            }
            return (Service) serviceConstructor.newInstance(new Object[] { this, classConfig });
        } catch (final ClassNotFoundException e) {
            // do nothing
        }

        return null;
    }

    /**
     * Gets the OpenURL request processor.
     */
    @Override
    public OpenURLRequestProcessor getProcessor() throws TransformerException, ClassNotFoundException,
            InstantiationException, IllegalAccessException {
        final Node node =
                XPathAPI.selectSingleNode(oomConfig, "/oomRef:config/oomRef:processor", XMLHelper.getXmlnsEl());
        final ClassConfig classConfig = new ClassConfig(node);
        final String className = classConfig.getClassName();
        final Class c = Class.forName(className);
        return (OpenURLRequestProcessor) c.newInstance();
    }

    /**
     * Gets the value for the supplied argument key.
     *
     * @param key An argument key
     */
    @Override
    public String getArg(final String key) throws TransformerException {
        final String xpath = new StringBuffer("/oomRef:config/oomRef:args/oomRef:").append(key).toString();
        return XPathAPI.eval(oomConfig, xpath, XMLHelper.getXmlnsEl()).str();
    }

    /**
     * Gets the values associated with the supplied argument key.
     *
     * @param key An argument key
     */
    @Override
    public String[] getArgs(final String key) throws TransformerException {
        final ArrayList args = new ArrayList();

        final String xpath = new StringBuffer("/oomRef:config/oomRef:args/oomRef:").append(key).toString();
        final NodeIterator iter = XPathAPI.selectNodeIterator(oomConfig, xpath, XMLHelper.getXmlnsEl());
        Node node;
        while ((node = iter.nextNode()) != null) {
            args.add(XPathAPI.eval(node, ".").str());
        }
        return (String[]) args.toArray(new String[args.size()]);
    }

    /**
     * Returns all the configurations.
     */
    @Override
    public Map getArgs() throws TransformerException {
        final Map map = new HashMap();

        if (oomConfig != null) {
            final NodeIterator iter =
                    XPathAPI.selectNodeIterator(oomConfig, "/oomRef:config/oomRef:args/*", XMLHelper.getXmlnsEl());
            Node node;
            while ((node = iter.nextNode()) != null) {
                final String key = XPathAPI.eval(node, "name()").str();
                map.put(key, XPathAPI.eval(node, ".").str());
            }
        }
        return map;
    }
}
