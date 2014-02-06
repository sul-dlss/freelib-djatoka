
package info.freelibrary.djatoka.util;

import org.apache.commons.compress.utils.IOUtils;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.File;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.servlet.annotation.WebServlet;
import javax.servlet.ServletOutputStream;

import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;

@WebServlet(name = "Djatoka Log Viewer", urlPatterns = {"/logs"})
public class LogSocketServlet extends WebSocketServlet {

    /**
     * The <code>serialVersionUID</code> for the servlet.
     */
    private static final long serialVersionUID = 5332580855425368721L;

    @Override
    public void configure(WebSocketServletFactory aFactory) {
        aFactory.getPolicy().setIdleTimeout(1800000); // millisecs (== 30 mins)
        aFactory.register(LogSocket.class);
    }

    @Override
    protected void doGet(HttpServletRequest aRequest,
            HttpServletResponse aResponse) throws ServletException, IOException {
        String path = getServletContext().getRealPath(".");
        File html = new File(path + "/logs.html");
        ServletOutputStream outStream = aResponse.getOutputStream();
        FileInputStream fileStream = new FileInputStream(html);
        BufferedInputStream inStream = new BufferedInputStream(fileStream);

        IOUtils.copy(inStream, outStream);

        inStream.close();
        outStream.close();
    }

}
