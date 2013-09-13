
package info.freelibrary.djatoka.view;

import gov.lanl.adore.djatoka.openurl.DjatokaImageMigrator;
import gov.lanl.adore.djatoka.openurl.IReferentMigrator;
import gov.lanl.adore.djatoka.openurl.IReferentResolver;
import gov.lanl.adore.djatoka.openurl.ResolverException;

import gov.lanl.adore.djatoka.util.ImageRecord;

import info.freelibrary.djatoka.Constants;
import info.freelibrary.util.FileUtils;
import info.freelibrary.util.PairtreeObject;
import info.freelibrary.util.PairtreeRoot;
import info.freelibrary.util.PairtreeUtils;
import info.freelibrary.util.RegexFileFilter;
import info.freelibrary.util.StringUtils;

import info.openurl.oom.entities.Referent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class IdentifierResolver implements IReferentResolver, Constants {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(IdentifierResolver.class);

    private IReferentMigrator myMigrator = new DjatokaImageMigrator();

    private Map<String, ImageRecord> myRemoteImages;

    private Map<String, ImageRecord> myLocalImages;

    private String myJP2Dir;

    public ImageRecord getImageRecord(String aReferentID)
            throws ResolverException {
        ImageRecord image = getCachedImage(aReferentID);

        if (image == null && isResolvableURI(aReferentID)) {
            image = getRemoteImage(aReferentID);
        }

        return image;
    }

    public ImageRecord getImageRecord(Referent aReferent)
            throws ResolverException {
        String id = ((URI) aReferent.getDescriptors()[0]).toASCIIString();
        return getImageRecord(id);
    }

    public IReferentMigrator getReferentMigrator() {
        return myMigrator;
    }

    public int getStatus(String aReferentID) {
        if (myRemoteImages.get(aReferentID) != null ||
                getCachedImage(aReferentID) != null) {
            return HttpServletResponse.SC_OK;
        } else if (myMigrator.getProcessingList().contains(aReferentID)) {
            return HttpServletResponse.SC_ACCEPTED;
        } else {
            return HttpServletResponse.SC_NOT_FOUND;
        }
    }

    public void setProperties(Properties aProps) throws ResolverException {
        String prodInstance = aProps.getProperty("djatoka.ignore.fscache");
        boolean skipFS = Boolean.parseBoolean(prodInstance);

        myJP2Dir = aProps.getProperty(JP2_DATA_DIR);

        myLocalImages = new ConcurrentHashMap<String, ImageRecord>();
        myRemoteImages = new ConcurrentHashMap<String, ImageRecord>();

        try {
            if (!skipFS) {
                loadFileSystemImages(myJP2Dir);
            } else if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("File system mapping disabled");
            }
        } catch (FileNotFoundException details) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("{} couldn't be found", myJP2Dir);
            }
        }
    }

    public void loadFileSystemImages(String aJP2DataDir)
            throws FileNotFoundException {
        File jp2Dir = new File(aJP2DataDir);
        FilenameFilter filter = new RegexFileFilter(JP2_FILE_PATTERN);
        String[] skipped = new String[] {"pairtree_root"};

        // Descend through file system but skipped our ID mapped PT directory
        for (File file : FileUtils.listFiles(jp2Dir, filter, true, skipped)) {
            ImageRecord image = new ImageRecord();
            String id = stripExt(file.getName());

            try {
                id = URLEncoder.encode(id, "UTF-8");
            } catch (UnsupportedEncodingException details) {
                // Should be impossible to get here, UTF-8 is always supported
                throw new RuntimeException(details);
            }

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Loading {} ({})", id, file);
            }

            image.setIdentifier(id);
            image.setImageFile(file.getAbsolutePath());

            if (myLocalImages == null) {
                myLocalImages = new ConcurrentHashMap<String, ImageRecord>();
                myRemoteImages = new ConcurrentHashMap<String, ImageRecord>();
            }

            myLocalImages.put(id, image);
        }
    }

    private boolean isResolvableURI(String aReferentID) {
        return aReferentID.startsWith("http"); // keeping it simple
    }

    // Not sure we should do this, but...
    private String stripExt(String aFileName) {
        int index = aFileName.lastIndexOf('.');
        return index != -1 ? aFileName.substring(0, index) : aFileName;
    }

    private ImageRecord getCachedImage(String aReferentID) {
        ImageRecord image = null;

        // First try cache of files loaded from file system
        // This is used for the simple file system viewer
        image = myLocalImages.get(aReferentID);

        if (LOGGER.isDebugEnabled() && image != null) {
            LOGGER.debug("{} found in the local cache", aReferentID);
        } else if (image == null) { // Try loading from our PairTree FS
            try {
                PairtreeRoot pairtree = new PairtreeRoot(new File(myJP2Dir));
                String id = URLDecoder.decode(aReferentID, "UTF-8");
                PairtreeObject dir = pairtree.getObject(id);
                String filename = PairtreeUtils.encodeID(id);
                File file = new File(dir, filename);

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Checking in Pairtree cache: {}", file);
                }

                if (file.exists()) {
                    image = new ImageRecord();
                    image.setIdentifier(id);
                    image.setImageFile(file.getAbsolutePath());
                }
            } catch (IOException details) {
                LOGGER.error("Failed to load file from cache", details);
            }
        }

        return image;
    }

    private ImageRecord getRemoteImage(String aReferent) {
        ImageRecord image = null;

        try {
            URI uri = new URI(aReferent);

            // Check to see if it's already in the processing queue
            if (myMigrator.getProcessingList().contains(uri.toString())) {
                Thread.sleep(1000);
                int index = 0;

                while (myMigrator.getProcessingList().contains(uri) &&
                        index < (5 * 60)) {
                    Thread.sleep(1000);
                    index++;
                }

                if (myRemoteImages.containsKey(aReferent)) {
                    return myRemoteImages.get(aReferent);
                }
            }

            File file = myMigrator.convert(uri);
            image = new ImageRecord(aReferent, file.getAbsolutePath());

            if (file.length() > 0) {
                myRemoteImages.put(aReferent, image);
            } else
                throw new ResolverException(
                        "An error occurred processing file:" +
                                uri.toURL().toString());
        } catch (Exception details) {
            LOGGER.error(StringUtils.formatMessage("Unable to access {} ({})",
                    new String[] {aReferent, details.getMessage()}), details);

            return null;
        }

        return image;
    }
}
