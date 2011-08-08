package info.freelibrary.djatoka.view;

import info.freelibrary.djatoka.Constants;
import info.freelibrary.util.IOUtils;
import info.freelibrary.util.PairtreeObject;
import info.freelibrary.util.PairtreeRoot;
import info.freelibrary.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImageServlet extends HttpServlet implements Constants {

	/**
	 * The <code>ImageServlet</code>'s <code>serialVersionUID</code>.
	 */
	private static final long serialVersionUID = -4142816720756238591L;

	private static final Logger LOGGER = LoggerFactory
			.getLogger(ImageServlet.class);

	private static final String IMAGE_URL = "/resolve?url_ver=Z39.88-2004&rft_id={}&svc_id=info:lanl-repo/svc/getRegion&svc_val_fmt=info:ofi/fmt:kev:mtx:jpeg2000&svc.format={}&svc.level={}";

	private static final String REGION_URL = "/resolve?url_ver=Z39.88-2004&rft_id={}&svc_id=info:lanl-repo/svc/getRegion&svc_val_fmt=info:ofi/fmt:kev:mtx:jpeg2000&svc.format={}&svc.level={}";

	private static String myFormatExt;

	private static String myCache;

	@Override
	protected void doGet(HttpServletRequest aRequest,
			HttpServletResponse aResponse) throws ServletException, IOException {
		String level = getServletConfig().getInitParameter("level");
		String id = parseID(aRequest.getPathInfo());
		PairtreeObject cacheObject = null;
		String region = null;

		if (level == null) {
			level = DEFAULT_VIEW_LEVEL;
		}

		if (myCache != null) {
			PairtreeRoot cacheDir = new PairtreeRoot(new File(myCache));
			String cacheFileName;
			File imageFile;

			cacheObject = cacheDir.getObject(id);
			cacheFileName = "image_" + level + "." + myFormatExt;
			imageFile = new File(cacheObject, cacheFileName);

			if (imageFile.exists()) {
				ServletOutputStream outStream = aResponse.getOutputStream();
				IOUtils.copyStream(imageFile, outStream);
				IOUtils.closeQuietly(outStream);

				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("{} served from Pairtree cache", imageFile);
				}
			}
			else {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("{} not found in cache", imageFile);
				}

				serveNewImage(id, level, region, aRequest, aResponse);

				// For now, not caching ROIs in our permanent cache
				// It's fine; we fall back to adore-djatoka's LRU cache
				if (region == null) {
					cacheNewImage(aRequest, aResponse, imageFile);
				}
			}
		}
		else {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("Cache isn't configured correctly (null)");
			}

			serveNewImage(id, level, region, aRequest, aResponse);
			// We can't cache, because we don't have a cache configured
		}
	}

	@Override
	public void init() throws ServletException {
		InputStream is = getClass().getResourceAsStream("/" + PROPERTIES_FILE);

		if (is != null) {
			try {
				Properties props = new Properties();
				props.load(is);

				if (props.containsKey(VIEW_CACHE_DIR)) {
					// TODO: use the default java cache dir as fallback?
					myCache = props.getProperty(VIEW_CACHE_DIR);

					if (LOGGER.isInfoEnabled()) {
						LOGGER.info("Cache directory set to {}", myCache);
					}
				}

				if (props.containsKey(VIEW_FORMAT_EXT)) {
					myFormatExt = props.getProperty(VIEW_FORMAT_EXT,
							DEFAULT_VIEW_EXT);

					if (LOGGER.isInfoEnabled()) {
						LOGGER.info("Format extension set to {}", myFormatExt);
					}
				}
			}
			catch (IOException details) {
				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn("Unable to load properties file: {}", details
							.getMessage());
				}
			}
		}
	}

	@Override
	protected void doHead(HttpServletRequest aRequest,
			HttpServletResponse aResponse) throws ServletException, IOException {
		super.doHead(aRequest, aResponse);
	}

	@Override
	protected long getLastModified(HttpServletRequest aRequest) {
		return super.getLastModified(aRequest);
	}

	private void serveNewImage(String aID, String aLevel, String aRegion,
			HttpServletRequest aRequest, HttpServletResponse aResponse)
			throws IOException, ServletException {
		RequestDispatcher dispatcher;
		String[] values;
		String url;

		if (aRegion == null) {
			values = new String[] { aID, DEFAULT_VIEW_FORMAT, aLevel };
			url = StringUtils.formatMessage(IMAGE_URL, values);
		}
		else {
			values = new String[] { aID, DEFAULT_VIEW_FORMAT, aLevel };
			url = StringUtils.formatMessage(REGION_URL, values);
		}

		dispatcher = aRequest.getRequestDispatcher(url);

		if (LOGGER.isDebugEnabled()) {
			String[] messageDetails = new String[] { aID, url };
			LOGGER.debug("Image requested: {} - {}", messageDetails);
		}

		dispatcher.forward(aRequest, aResponse);
	}

	private void cacheNewImage(HttpServletRequest aRequest,
			HttpServletResponse aResponse, File aDestFile) {
		HttpSession session = aRequest.getSession();
		String fileName = (String) session.getAttribute(VIEW_CACHE_FILE);

		if (fileName != null) {
			File cachedFile = new File(fileName);

			// This moves the newly created file from the adore-djatoka cache
			// to the freelib-djatoka cache (which is pure-FS/Pairtree-based)
			if (cachedFile.exists() && aDestFile != null) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Renaming cache file from {} to {}",
							cachedFile, aDestFile);
				}

				// FIXME: Don't assume files are of the same type(?)
				cachedFile.renameTo(aDestFile);
				// TODO: remove from in-memory map of cache files?
			}
			else if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("Session had a cache file, but it didn't exist");
			}
		}
		else if (LOGGER.isWarnEnabled()) {
			LOGGER.warn("Couldn't cache; session lacked new image information");
		}
	}

	private String parseID(String aPathInfo) {
		if (aPathInfo.startsWith("/")) {
			return aPathInfo.substring(1);
		}

		return aPathInfo;
	}
}
