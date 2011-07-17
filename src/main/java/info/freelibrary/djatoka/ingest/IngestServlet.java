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

	private static final String JP2_EXT = ".jp2";

	private static final String PROPERTIES_FILE = "djatoka.properties";

	private DjatokaEncodeParam myParams;

	private ICompress myCompression;

	private String[] myExts;

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

			// Catch the rest of our encoding parameters
			myParams = new DjatokaEncodeParam(props);
			myCompression = new KduCompressExe();
			myExts = extString.split(","); // TODO: support ; etc?

			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("Loading/processing images from {}", dataDir);
			}

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Looking for '{}' files", StringUtils.toString(
						myExts, ' '));
			}

			if (source.exists()) {
				if (!source.isDirectory() || !source.canRead()) {
					String msg = dataDir + " cannot be read or is not a dir";
					LOGGER.error(msg, new IOException(dataDir));
					aResp.sendError(HttpServletResponse.SC_BAD_REQUEST, msg);
				}
				else {
					PrintWriter toBrowser = getWriter(aResp, acceptsHTML);
					int total = processImageFiles(source, dest);
					String message = total + " file(s) converted to JP2";
					String title = "Results of the JP2 Conversion Batch Script";

					if (acceptsHTML) {
						toBrowser.write(wrapInHTML(title, message));
					}
					else {
						toBrowser.write(message);
					}

					toBrowser.close();
				}
			}
			else if (LOGGER.isWarnEnabled()) {
				String msg = "Supplied source directory didn't exist" + dataDir;
				LOGGER.warn(msg, new FileNotFoundException(dataDir));
				aResp.sendError(HttpServletResponse.SC_BAD_REQUEST, msg);
			}
		}
		catch (Exception details) {
			throw new IOException(details.getMessage(), details);
		}
	}

	private int processImageFiles(File aSource, File aDest) throws IOException,
			Exception {
		File[] files = aSource.listFiles(new FileExtFileFilter(myExts));
		File[] dirs = aSource.listFiles(new DirFileFilter());
		int total = 0;

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Found {} directories in {}", dirs.length, aSource);
		}

		for (File nextSource : dirs) {
			String fileName = nextSource.getName();

			if (!fileName.startsWith(".")) {
				File nextDest = new File(aDest, nextSource.getName());

				if (nextDest.exists()
						&& (!nextDest.canWrite() || !nextDest.isDirectory())) {
					throw new IOException(
							"Problem with destination directory: "
									+ nextDest.getAbsolutePath());
				}
				else {
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("Descending into new directory: {}",
								nextDest);
					}

					if ((!nextDest.exists() && nextDest.mkdirs())
							|| (nextDest.exists() && nextDest.isDirectory())) {
						total += processImageFiles(nextSource, nextDest);
					}
					else {
						throw new IOException(
								"Failed to create a new directory: "
										+ nextDest.getAbsolutePath());
					}
				}
			}
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Found {} files in {}", files.length, aSource);
		}

		for (File nextSource : files) {
			String fileName = stripExt(nextSource.getName()) + JP2_EXT;
			File nextDest = new File(aDest, fileName);

			if (!fileName.startsWith(".") && !nextDest.exists()) {
				if (LOGGER.isInfoEnabled()) {
					LOGGER.info("Compressing {} to {} ({})", new String[] {
							nextSource.getAbsolutePath(),
							nextDest.getAbsolutePath(),
							Integer.toString(total + 1) });
				}

				DjatokaCompress.compress(myCompression, nextSource
						.getAbsolutePath(), nextDest.getAbsolutePath(),
						myParams);
				total += 1;
			}
		}

		return total;
	}

	private PrintWriter getWriter(HttpServletResponse aResponse,
			boolean aHTMLResponse) throws IOException {
		if (aHTMLResponse) {
			aResponse.setContentType("text/html");
		}
		else {
			aResponse.setContentType("text/txt");
		}

		return aResponse.getWriter();
	}

	private String stripExt(String aFileName) {
		int index;

		if ((index = aFileName.lastIndexOf('.')) != -1) {
			return aFileName.substring(0, index);
		}

		return aFileName;
	}

	/*
	 * quick and dirty
	 */
	private String wrapInHTML(String aTitle, String aMessage) {
		return StringUtils.formatMessage("<html>\n" + "  <head>\n"
				+ "    <title>{}</title>\n" + "  </head>\n" + "  <body>\n"
				+ "    <div>{}</div>\n" + "  </body>\n" + "</html>\n",
				new String[] { aTitle, aMessage });

	}
}
