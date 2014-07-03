
package gov.lanl.adore.djatoka.openurl;

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gov.lanl.adore.djatoka.DjatokaException;

public interface IReferentMigrator {

    /**
     * Sets the Pairtree root for images that can be migrated into the Pairtree structure.
     *
     * @param aPtRootDir The root directory to the Pairtree file system
     */
    public abstract void setPairtreeRoot(File aPtRootDir);

    /**
     * Returns true if the migrator has a Pairtree file system set.
     *
     * @return True if the migrator has a Pairtree file system set; else, false
     */
    public abstract boolean hasPairtreeRoot();

    /**
     * Returns the migrator's Pairtree root directory.
     *
     * @return The migrator's Pairtree root directory
     */
    public abstract File getPairtreeRoot();

    /**
     * Returns a local File object for a provide URI
     *
     * @param aReferent the identifier of the requested image file
     * @param aURI the URI of an image to be downloaded and compressed as JP2
     * @return File object of JP2 compressed image
     * @throws DjatokaException
     */
    public abstract File convert(String aReferent, URI aURI) throws DjatokaException;

    /**
     * Returns a local File object for a provide URI
     *
     * @param aImgFile File object on local image to be compressed
     * @param aURI the URI of an image to be compressed as JP2
     * @return File object of JP2 compressed image
     * @throws DjatokaException
     */
    public abstract File processImage(File aImgFile, URI aURI) throws DjatokaException;

    /**
     * Return list of images currently being processed. Images are removed once complete.
     *
     * @return list of images being processed
     */
    public abstract List<?> getProcessingList();

    /**
     * Returns map of format extension (e.g. jpg) to mime-type mappings (e.g. image/jpeg)
     *
     * @return format extension to mime-type mappings
     */
    public abstract Map<?, ?> getFormatMap();

    /**
     * Sets map of format extension (e.g. jpg) to mime-type mappings (e.g. image/jpeg)
     *
     * @param formatMap extension to mime-type mappings
     */
    public abstract void setFormatMap(HashMap<String, String> formatMap);

}