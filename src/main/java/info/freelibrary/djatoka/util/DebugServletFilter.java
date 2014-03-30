
package info.freelibrary.djatoka.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link javax.servlet.ServletFilter} that records requests received.
 * 
 * @author <a href="mailto:ksclarke@gmail.com">Kevin S. Clarke</a>
 */
public class DebugServletFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(DebugServletFilter.class);

    private FilterConfig myFilterConfig;

    private Set<String> myRequests;

    /**
     * Destroys the {@link javax.servlet.ServletFilter}.
     */
    public void destroy() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Shutting down ServletFilter: {}", myFilterConfig.getFilterName());
        }

        myFilterConfig = null;
    }

    /**
     * Records a new URI in the request log or provides a list of all the requested URIs.
     * 
     * @param aRequest The servlet request
     * @param aResponse The servlet response
     */
    public void doFilter(ServletRequest aRequest, ServletResponse aResponse, FilterChain aFilterChain)
            throws IOException, ServletException {
        if (LOGGER.isDebugEnabled() && aRequest instanceof HttpServletRequest) {
            HttpServletRequest httpRequest = (HttpServletRequest) aRequest;
            String peek = aRequest.getParameter("peek");
            String uri = httpRequest.getRequestURI();

            // If we have a peek parameter, we want to see requested URIs
            if (peek != null && peek.equalsIgnoreCase("true")) {
                PrintWriter toBrowser = aResponse.getWriter();
                Iterator<String> iterator = myRequests.iterator();

                while (iterator.hasNext()) {
                    toBrowser.print(iterator.next());
                    toBrowser.print(System.getProperty("line.separator"));
                }

                toBrowser.close();
            } else {
                if (myRequests.add(uri)) {
                    LOGGER.debug("Added new request URI to log: {}", uri);
                }

                aFilterChain.doFilter(aRequest, aResponse);
            }
        } else {
            aFilterChain.doFilter(aRequest, aResponse);
        }
    }

    @Override
    public void init(FilterConfig aFilterConfig) throws ServletException {
        myRequests = new ConcurrentSkipListSet<String>();
        myFilterConfig = aFilterConfig;
    }
}
