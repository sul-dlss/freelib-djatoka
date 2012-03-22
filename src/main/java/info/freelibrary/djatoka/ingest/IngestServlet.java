package info.freelibrary.djatoka.ingest;

import gov.lanl.adore.djatoka.DjatokaCompress;
import gov.lanl.adore.djatoka.DjatokaEncodeParam;
import gov.lanl.adore.djatoka.ICompress;
import gov.lanl.adore.djatoka.kdu.KduCompressExe;
import gov.lanl.adore.djatoka.util.IOUtils;

import info.freelibrary.util.DirFileFilter;
import info.freelibrary.util.FileExtFileFilter;
import info.freelibrary.util.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IngestServlet extends HttpServlet {

    private static final Logger LOGGER = LoggerFactory
	    .getLogger(IngestServlet.class);

    /**
     * IngestServlet's <code>serialVersionUID</code>.
     */
    private static final long serialVersionUID = 8661545409056868772L;

    private static final String PROPERTIES_FILE = "djatoka.properties";

    @Override
    protected void doGet(HttpServletRequest aReq, HttpServletResponse aResp)
	    throws ServletException, IOException {
	String dir = getServletContext().getRealPath("/WEB-INF/classes") + "/";
	boolean acceptsHTML = aReq.getHeader("Accept").contains("html");
	String propertiesFile = dir + PROPERTIES_FILE;

	if (LOGGER.isDebugEnabled()) {
	    LOGGER.debug("Loading properties file: {}", propertiesFile);
	}

	try {
	    Properties props = IOUtils.loadConfigByPath(propertiesFile);
	    String dataDir = props.getProperty("djatoka.ingest.data.dir");
	    String jp2Dir = props.getProperty("djatoka.ingest.jp2.dir");
	    String extString = props.getProperty("djatoka.ingest.data.exts");
	    File source = new File(dataDir);
	    File dest = new File(jp2Dir);
	    String[] exts = extString.split(","); // TODO: support ; etc?

	    if (LOGGER.isDebugEnabled()) {
		LOGGER.debug("Loading/processing images from {}", dataDir);
	    }

	    if (LOGGER.isDebugEnabled()) {
		LOGGER.debug("Looking for '{}' files",
			StringUtils.toString(exts, ' '));
	    }

	    if (source.exists()) {
		if (!source.isDirectory() || !source.canRead()) {
		    String msg = dataDir + " cannot be read or is not a dir";
		    LOGGER.error(msg, new IOException(dataDir));
		    aResp.sendError(HttpServletResponse.SC_BAD_REQUEST, msg);
		}
		else {
		    IngestThread thread = new IngestThread(source, dest, exts, props);
		    thread.start();
		    getServletContext().setAttribute("ingest", thread);
		}
	    }
	    else if (LOGGER.isWarnEnabled()) {
		String msg = "Supplied source directory didn't exist: "
			+ dataDir;
		LOGGER.warn(msg, new FileNotFoundException(dataDir));
		aResp.sendError(HttpServletResponse.SC_BAD_REQUEST, msg);
	    }
	}
	catch (Exception details) {
	    throw new IOException(details.getMessage(), details);
	}
    }

}
