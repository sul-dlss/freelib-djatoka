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

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import info.openurl.oom.OpenURLRequest;
import info.openurl.oom.OpenURLRequestProcessor;
import info.openurl.oom.OpenURLResponse;
import info.openurl.oom.Transport;
import info.openurl.oom.config.OpenURLConfig;

/**
 * @author Jeffrey A. Young OpenURL Servlet
 */
public class OpenURLServlet extends HttpServlet {

    /**
     * Initial version
     */
    private static final long serialVersionUID = 1L;

    private OpenURLConfig openURLConfig;

    private OpenURLRequestProcessor processor;

    private Transport[] transports;

    /**
     * Initializes the servlet.
     */
    @Override
    public void init(final ServletConfig config) throws ServletException {
        super.init(config);

        try {
            // load the configuration file from the classpath
            openURLConfig = new org.oclc.oomRef.config.OpenURLConfig(config);

            // Construct the configured transports
            transports = openURLConfig.getTransports();

            // Construct a processor
            processor = openURLConfig.getProcessor();
        } catch (final Exception e) {
            e.printStackTrace();
            throw new ServletException(e.getMessage(), e);
        }
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException {
        try {
            // Try each Transport until someone takes responsibility
            OpenURLRequest openURLRequest = null;
            for (int i = 0; openURLRequest == null && i < transports.length; ++i) {
                openURLRequest = transports[i].toOpenURLRequest(processor, req);
            }

            if (openURLRequest == null) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid Request");
                return;
            }

            // Process the ContextObjects
            final OpenURLResponse result = processor.resolve(openURLRequest);

            // See if anyone handled the request
            int status;
            if (result == null) {
                status = HttpServletResponse.SC_NOT_FOUND;
            } else {
                status = result.getStatus();
                final Cookie[] cookies = result.getCookies();
                if (cookies != null) {
                    for (int i = 0; i < cookies.length; ++i) {
                        resp.addCookie(cookies[i]);
                    }
                }

                final Map sessionMap = result.getSessionMap();
                if (sessionMap != null) {
                    final HttpSession session = req.getSession(true);
                    final Iterator iter = sessionMap.entrySet().iterator();
                    while (iter.hasNext()) {
                        final Map.Entry entry = (Entry) iter.next();
                        session.setAttribute((String) entry.getKey(), entry.getValue());
                    }
                }

                final Map headerMap = result.getHeaderMap();
                if (headerMap != null) {
                    final Iterator iter = headerMap.entrySet().iterator();
                    while (iter.hasNext()) {
                        final Map.Entry entry = (Entry) iter.next();
                        resp.setHeader((String) entry.getKey(), (String) entry.getValue());
                    }
                }
            }

            // Allow the processor to generate a variety of response types
            switch (status) {
                case HttpServletResponse.SC_MOVED_TEMPORARILY:
                    resp.sendRedirect(resp.encodeRedirectURL(result.getRedirectURL()));
                    break;
                case HttpServletResponse.SC_SEE_OTHER:
                case HttpServletResponse.SC_MOVED_PERMANENTLY:
                    resp.setStatus(status);
                    resp.setHeader("Location", result.getRedirectURL());
                    break;
                case HttpServletResponse.SC_NOT_FOUND:
                    resp.sendError(status);
                    break;
                default:
                    final OutputStream out = resp.getOutputStream();
                    resp.setStatus(status);
                    resp.setContentType(result.getContentType());
                    final InputStream is = result.getInputStream();
                    final byte[] bytes = new byte[1024];
                    int len;

                    while ((len = is.read(bytes)) != -1) {
                        out.write(bytes, 0, len);
                    }

                    out.close();
                    break;
            }
        } catch (final Throwable e) {
            e.printStackTrace();
            // throw new ServletException(e.getMessage(), e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException {
        doGet(req, resp);
    }
}
