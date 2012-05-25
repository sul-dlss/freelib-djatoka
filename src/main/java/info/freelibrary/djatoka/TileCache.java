package info.freelibrary.djatoka;

import info.freelibrary.util.I18nObject;
import info.freelibrary.util.XMLBundleControl;
import info.freelibrary.util.XMLResourceBundle;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

import java.net.HttpURLConnection;
import java.net.URL;

import java.util.ResourceBundle;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVReader;

public class TileCache extends I18nObject {

    private static final Logger LOGGER = LoggerFactory
	    .getLogger(TileCache.class);

    private static final XMLResourceBundle BUNDLE = (XMLResourceBundle) ResourceBundle
	    .getBundle("FreeLib-Djatoka_Messages", new XMLBundleControl());

    /**
     * @param args
     */
    public static void main(String[] args) throws IOException {
	String[] ids;

	if (args.length == 2 || args.length == 3) {
	    String server = "http://localhost:8888/";
	    File csvFile = new File(args[0]);
	    CSVReader csvReader;
	    int index;

	    if (!csvFile.exists()) {
		String fileName = csvFile.getAbsolutePath();
		System.err.println("File not found: " + fileName);
		printUsageAndExit();
	    }

	    // Make sure format of supplied server URL is what we expect
	    if (args.length == 3) {
		if (!args[2].startsWith("http://")) {
		    args[2] = "http://" + args[2];
		}

		if (!args[2].endsWith("/")) {
		    args[2] = args[2] + "/";
		}

		server = args[2];
	    }

	    if (!isLive(server)) {
		LOGGER.error(BUNDLE.get("TC_SERVER_404"), server);
		printUsageAndExit();
	    }

	    try {
		csvReader = new CSVReader(new FileReader(csvFile));
		index = Integer.parseInt(args[1]) - 1; // columns 1-based

		while ((ids = csvReader.readNext()) != null) {
		    cache(ids[index]);
		}
	    }
	    catch (NumberFormatException details) {
		LOGGER.error(details.getMessage());
		printUsageAndExit();
	    }

	}
	// else if (args.length == 1) {} // TODO: descend through directories
	else {
	    printUsageAndExit();
	}

    }

    private static void cache(String aID) {

    }

    private static boolean isLive(String aServer) throws IOException {
	URL url = new URL(aServer + "health");
	HttpURLConnection uc = (HttpURLConnection) url.openConnection();

	if (LOGGER.isDebugEnabled()) {
	    LOGGER.debug("Checking server status at {}", url.toString());
	}

	// TODO? Check sizes that come back, showing there is an image there?
	if (uc.getResponseCode() == 200) {
	    try {
		Document xml = new Builder().build(uc.getInputStream());
		Element response = (Element) xml.getRootElement();
		Element health = response.getFirstChildElement("health");
		String status = health.getValue();

		if (status.equals("dying") || status.equals("sick")) {
		    if (LOGGER.isWarnEnabled()) {
			LOGGER.warn("Server status is '{}'", status);
		    }
		    
		    return true;
		}
		else if (status.equals("ok")) {
		    return true;
		}
		else {
		    LOGGER.error("Unexpected server status: {}", status);
		}
	    }
	    catch (Exception details) {
		LOGGER.error(details.getMessage());
	    }
	}

	return false;
    }

    private static void printUsageAndExit() {
	String eol = System.getProperty("line.separator");
	StringBuilder sb = new StringBuilder(eol);
	sb.append("Usage: mvn exec:java ");
	sb.append("-Dexec.mainClass=\"info.freelibrary.djatoka.TileCache\" ");
	sb.append("-Dexec.args=\"/path/to/ids.csv ID_column_number [server_URL]\"");
	sb.append(eol).append("   or: mvn exec:java ");
	sb.append("-Dexec.mainClass=\"info.freelibrary.djatoka.TileCache\" ");
	sb.append("-Dexec.args=\"/path/to/tiff/files/dir/\" (not yet implemented)");
	System.out.println(sb.toString());
	System.exit(1);
    }
}
