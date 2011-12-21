package info.freelibrary.djatoka.view;

import gov.lanl.adore.djatoka.util.IOUtils;

import info.freelibrary.djatoka.Constants;
import info.freelibrary.util.FileUtils;
import info.freelibrary.util.RegexDirFilter;
import info.freelibrary.util.RegexFileFilter;
import info.freelibrary.util.StringUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ViewServlet extends HttpServlet implements Constants {

    /**
     * The <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 5298506582675331814L;

    private static final String XSL_STYLESHEET = "<?xml-stylesheet href='/view.xsl' type='text/xsl'?>";

    private static final Logger LOGGER = LoggerFactory
	    .getLogger(ViewServlet.class);

    private Properties myProps;

    @Override
    protected void doGet(HttpServletRequest aRequest,
	    HttpServletResponse aResponse) throws ServletException, IOException {
	File tifDir = new File(myProps.getProperty(TIFF_DATA_DIR));
	File jp2Dir = new File(myProps.getProperty(JP2_DATA_DIR));
	String servletPath = aRequest.getServletPath();
	String dirParam = aRequest.getPathInfo();
	long jp2CountLong = 0;
	long tifCountLong = 0;
	FilenameFilter dirFilter;
	FilenameFilter jp2Filter;
	PrintWriter writer;
	String jp2Count;
	String tifCount;
	File dir;

	// We need the ending slash for the browser to construct links
	if (dirParam == null || !dirParam.endsWith("/")) {
	    aResponse.sendRedirect(servletPath + "/");
	    return;
	}

	if (!dirParam.startsWith("/")) {
	    dirParam = "/" + dirParam;
	}

	// Catch folks who want something significant from altering URL
	if (dirParam.equalsIgnoreCase("/thumbnails/")) {
	    aResponse.sendRedirect(servletPath + "/");
	    return;
	}

	dir = new File(jp2Dir, dirParam);

	// TODO: cache all this work in servlet session or something
	try {
	    File[] jp2Files = FileUtils.listFiles(jp2Dir, new RegexFileFilter(
		    JP2_FILE_PATTERN), true);
	    File[] tifFiles = FileUtils.listFiles(tifDir, new RegexFileFilter(
		    TIFF_FILE_PATTERN), true);

	    // These two just count the size of the files, not directories too
	    for (File file : jp2Files) {
		jp2CountLong += file.length();
	    }

	    for (File file : tifFiles) {
		tifCountLong += file.length();
	    }

	    jp2Count = FileUtils.sizeFromBytes(jp2CountLong, true);
	    tifCount = FileUtils.sizeFromBytes(tifCountLong, true);

	    if (LOGGER.isDebugEnabled()) {
		LOGGER.debug("TIFF file count (size): {} ({})",
			tifFiles.length, tifCount);
		LOGGER.debug("JP2 file count (size): {} ({})", jp2Files.length,
			jp2Count);
	    }

	    if (!jp2Dir.exists()) {
		aResponse.sendError(HttpServletResponse.SC_NOT_FOUND,
			StringUtils.formatMessage(
				"Configured JP2 directory ({}) doesn't exist",
				jp2Dir.getAbsolutePath()));
	    }
	    else {
		if (!tifDir.exists() && LOGGER.isWarnEnabled()) {
		    LOGGER.warn(
			    "The configured TIFF directory ({}) doesn't exist",
			    tifDir);
		}

		writer = getWriter(aResponse);
		writer.write(XSL_STYLESHEET);

		// Being sketchy and not using a real XML library like I should
		writer.write("<djatokaViewer>");
		writer.write(StringUtils.formatMessage(
			"<tifStats fileCount='{}' totalSize='{}'/>"
				+ "<jp2Stats fileCount='{}' totalSize='{}'/>",
			new String[] { Integer.toString(tifFiles.length),
				tifCount, Integer.toString(jp2Files.length),
				jp2Count }));

		writer.write("<defaultPath>" + servletPath + "</defaultPath>");
		writer.write("<path>" + tokenize(dirParam) + "</path>");

		if (dir.exists()) {
		    if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Viewing contents of {}", dir);
		    }

		    dirFilter = new RegexDirFilter(".*");
		    jp2Filter = new RegexFileFilter(JP2_FILE_PATTERN);

		    for (File file : dir.listFiles(dirFilter)) {
			writer.write("<dir name='" + file.getName() + "'/>");
		    }

		    for (File file : dir.listFiles(jp2Filter)) {
			writer.write("<file name='" + file.getName() + "'/>");
		    }
		}

		writer.write("</djatokaViewer>");

	    }
	}
	catch (Exception details) {
	    LOGGER.error(details.getMessage(), details);
	    aResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
		    details.getMessage());
	}
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
	}
	catch (Exception details) {
	    throw new ServletException(details);
	}
    }

    @Override
    public void log(String aMessage, Throwable aThrowable) {
	super.log(aMessage, aThrowable);
    }

    @Override
    public void log(String aMessage) {
	super.log(aMessage);
    }

    private PrintWriter getWriter(HttpServletResponse aResponse)
	    throws IOException {
	aResponse.setContentType("application/xml");
	return aResponse.getWriter();
    }

    private String tokenize(String aPath) {
	StringBuilder builder = new StringBuilder();
	String[] pathParts = aPath.split("/");

	for (String pathPart : pathParts) {
	    if (!pathPart.equals("")) {
		builder.append("<part>").append(pathPart).append("</part>");
	    }
	}

	return builder.toString();
    }
}
