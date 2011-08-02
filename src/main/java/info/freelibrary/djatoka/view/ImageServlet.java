package info.freelibrary.djatoka.view;

import info.freelibrary.util.StringUtils;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImageServlet extends HttpServlet {

	/**
	 * The <code>ImageServlet</code>'s <code>serialVersionUID</code>.
	 */
	private static final long serialVersionUID = -4142816720756238591L;

	private static final Logger LOGGER = LoggerFactory
			.getLogger(ImageServlet.class);

	private static final String DEFAULT_IMG_FORMAT = "image/jpeg";

	private static final String IMAGE_URL = "/resolve?url_ver=Z39.88-2004&rft_id={}&svc_id=info:lanl-repo/svc/getRegion&svc_val_fmt=info:ofi/fmt:kev:mtx:jpeg2000&svc.format={}&svc.level=3";

	@Override
	protected void doGet(HttpServletRequest aRequest,
			HttpServletResponse aResponse) throws ServletException, IOException {
		String id = parseID(aRequest.getPathInfo());

		// TODO: check local cache first before trying to resolve

		String[] values = new String[] { id, DEFAULT_IMG_FORMAT };
		String url = StringUtils.formatMessage(IMAGE_URL, values);
		RequestDispatcher dispatcher = aRequest.getRequestDispatcher(url);

		if (LOGGER.isDebugEnabled()) {
			String[] messageDetails = new String[] { id, url };
			LOGGER.debug("Image requested: {} - {}", messageDetails);
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
