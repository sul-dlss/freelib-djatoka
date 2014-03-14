
package info.freelibrary.djatoka.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.compress.utils.IOUtils;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

@WebServlet(name = "Djatoka Log Viewer", urlPatterns = { "/logs" })
public class LogSocketServlet extends WebSocketServlet {

    /**
     * The <code>serialVersionUID</code> for the servlet.
     */
    private static final long serialVersionUID = 5332580855425368721L;

    @Override
    public void configure(final WebSocketServletFactory aFactory) {
        aFactory.getPolicy().setIdleTimeout(1800000); // millisecs (== 30 mins)
        aFactory.register(LogSocket.class);
    }

    @Override
    protected void doGet(final HttpServletRequest aRequest, final HttpServletResponse aResponse)
            throws ServletException, IOException {
        final String path = getServletContext().getRealPath(".");
        final File html = new File(path + "/logs.html");
        final ServletOutputStream outStream = aResponse.getOutputStream();
        final FileInputStream fileStream = new FileInputStream(html);
        final BufferedInputStream inStream = new BufferedInputStream(fileStream);

        IOUtils.copy(inStream, outStream);

        inStream.close();
        outStream.close();
    }

}
