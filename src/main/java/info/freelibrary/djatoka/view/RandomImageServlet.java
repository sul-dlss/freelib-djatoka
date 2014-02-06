
package info.freelibrary.djatoka.view;

import java.net.URLEncoder;

import info.freelibrary.util.PairtreeUtils;

import gov.lanl.adore.djatoka.util.IOUtils;

import info.freelibrary.djatoka.Constants;
import info.freelibrary.util.FileUtils;
import info.freelibrary.util.RegexFileFilter;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RandomImageServlet extends HttpServlet implements Constants {

    private static final long serialVersionUID = -7221546341356013641L;

    private static final Logger LOGGER = LoggerFactory
            .getLogger(RandomImageServlet.class);

    private Properties myProps;

    @SuppressWarnings("unchecked")
    @Override
    protected void doGet(HttpServletRequest aRequest,
            HttpServletResponse aResponse) throws ServletException, IOException {
        String pt = myProps.getProperty(JP2_DATA_DIR) + "/pairtree_root/--";
        RegexFileFilter filter = new RegexFileFilter(".*");
        ServletContext context = getServletContext();
        Object object = context.getAttribute("djin");
        Random random = new Random();
        List<String> files;
        String image;

        if (object == null) {
            File[] jp2Files = FileUtils.listFiles(new File(pt), filter, true);

            files = new ArrayList<String>();

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Building list of files for {} from {}",
                        getClass().getSimpleName(), pt);
            }

            for (int index = 0; index < jp2Files.length; index++) {
                files.add(FileUtils.stripExt(jp2Files[index].getName()));
            }

            files = Collections.synchronizedList(files);
            context.setAttribute("djin", files);
        } else {
            files = (List<String>) object;
        }

        image = files.get(random.nextInt((files.size() - 2) + 1));
        image = URLEncoder.encode(PairtreeUtils.decodeID(image), "UTF-8");

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Image randomly selected is: {}", image);
        }

        image = aRequest.getRequestURI().replace("random", image);
        aRequest.getRequestDispatcher(image).forward(aRequest, aResponse);
    }

    @Override
    public void init() throws ServletException {
        String dir = getServletContext().getRealPath("/WEB-INF/classes") + "/";
        String propertiesFile = dir + PROPERTIES_FILE;

        try {
            myProps = IOUtils.loadConfigByPath(propertiesFile);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Loaded properties file: {}", propertiesFile);
            }
        } catch (Exception details) {
            throw new ServletException(details);
        }
    }

}
