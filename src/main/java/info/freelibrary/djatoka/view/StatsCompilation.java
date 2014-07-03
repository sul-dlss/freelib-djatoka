
package info.freelibrary.djatoka.view;

import java.io.File;
import java.io.FileOutputStream;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Serializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.freelibrary.djatoka.Constants;
import info.freelibrary.util.FileUtils;
import info.freelibrary.util.RegexFileFilter;

public class StatsCompilation implements Constants {

    private static final Logger LOGGER = LoggerFactory.getLogger(StatsCompilation.class);

    private String myJP2sSize;

    private String myJP2sCount;

    private String myTIFsSize;

    private String myTIFsCount;

    /**
     * A compilation of statistics about the ingest source and target directories.
     *
     * @param aTIFDir A source directory
     * @param aJP2Dir A target directory
     */
    public StatsCompilation(final File aTIFDir, final File aJP2Dir) {
        final RegexFileFilter jp2Pattern = new RegexFileFilter(JP2_FILE_PATTERN);
        final RegexFileFilter tifPattern = new RegexFileFilter(TIFF_FILE_PATTERN);
        long jp2CountLong = 0;
        long tifCountLong = 0;

        if (!aJP2Dir.exists() && !aJP2Dir.mkdirs() && LOGGER.isWarnEnabled()) {
            LOGGER.warn("Couldn't create requested JP2 directory: {}", aJP2Dir);
        }

        try {
            final File[] jp2Files = FileUtils.listFiles(aJP2Dir, jp2Pattern, true);
            final File[] tifFiles = FileUtils.listFiles(aTIFDir, tifPattern, true);

            // These two just count the size of the files, not directories
            for (final File file : jp2Files) {
                jp2CountLong += file.length();
            }

            for (final File file : tifFiles) {
                tifCountLong += file.length();
            }

            myJP2sSize = FileUtils.sizeFromBytes(jp2CountLong, true);
            myTIFsSize = FileUtils.sizeFromBytes(tifCountLong, true);
            myJP2sCount = Integer.toString(jp2Files.length);
            myTIFsCount = Integer.toString(tifFiles.length);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("TIF file count (size): {} ({})", myTIFsCount, myTIFsSize);
                LOGGER.debug("JP2 file count (size): {} ({})", myJP2sCount, myJP2sSize);
            }
        } catch (final Exception details) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(details.getMessage(), details);
            }
        }
    }

    /**
     * Saves a stats file to disk.
     *
     * @param aStatsFile A file of statistics
     */
    public void save(final File aStatsFile) {
        try {
            final FileOutputStream outStream = new FileOutputStream(aStatsFile);
            final Serializer serializer = new Serializer(outStream);
            final Element root = new Element("djatoka");
            final Element jp2Elem = new Element("jp2s");
            final Element tifElem = new Element("tifs");

            jp2Elem.addAttribute(new Attribute(JP2_SIZE_ATTR, myJP2sSize));
            jp2Elem.addAttribute(new Attribute(JP2_COUNT_ATTR, myJP2sCount));
            tifElem.addAttribute(new Attribute(TIF_SIZE_ATTR, myTIFsSize));
            tifElem.addAttribute(new Attribute(TIF_COUNT_ATTR, myTIFsCount));

            root.appendChild(jp2Elem);
            root.appendChild(tifElem);
            serializer.write(new Document(root));
        } catch (final Exception details) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(details.getMessage(), details);
            }
        }
    }

    /**
     * Gets the total size of JP2 files.
     *
     * @return The total size of the JP2 files
     */
    public String getJP2sSize() {
        return myJP2sSize;
    }

    /**
     * Gets the total number of JP2 files.
     *
     * @return The total size of the JP2 files
     */
    public String getJP2sCount() {
        return myJP2sCount;
    }

    /**
     * Gets the total size of TIFF files.
     *
     * @return The total size of the TIFF files
     */
    public String getTIFsSize() {
        return myTIFsSize;
    }

    /**
     * Gets the total number of TIFF files.
     *
     * @return The total size of the TIFF files
     */
    public String getTIFsCount() {
        return myTIFsCount;
    }

}
