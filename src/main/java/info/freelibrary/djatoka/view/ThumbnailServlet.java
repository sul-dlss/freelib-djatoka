package info.freelibrary.djatoka.view;

import info.freelibrary.djatoka.Constants;
import info.freelibrary.util.StringUtils;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThumbnailServlet extends HttpServlet implements Constants {

	/**
	 * The <code>serialVersionUID</code>.
	 */
	private static final long serialVersionUID = -560722637176381102L;

	private static final Logger LOGGER = LoggerFactory
			.getLogger(ThumbnailServlet.class);

	private static final String THUMBNAIL_URL = "/resolve?url_ver=Z39.88-2004&rft_id={}&svc_id=info:lanl-repo/svc/getRegion&svc_val_fmt=info:ofi/fmt:kev:mtx:jpeg2000&svc.format={}&svc.level=1";

	@Override
	protected void doGet(HttpServletRequest aRequest,
			HttpServletResponse aResponse) throws ServletException, IOException {
		String id = parseID(aRequest.getPathInfo());
		
		// check local cache first before trying to resolve
		
		String[] values = new String[] { id, DEFAULT_VIEW_FORMAT };
		String url = StringUtils.formatMessage(THUMBNAIL_URL, values);
		RequestDispatcher dispatcher = aRequest.getRequestDispatcher(url);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Thumbnail requested: {} - {}",
					new String[] { id, url });
		}

		dispatcher.forward(aRequest, aResponse);
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

	private String parseID(String aPathInfo) {
		if (aPathInfo.startsWith("/")) {
			return aPathInfo.substring(1);
		}

		return aPathInfo;
	}
}
