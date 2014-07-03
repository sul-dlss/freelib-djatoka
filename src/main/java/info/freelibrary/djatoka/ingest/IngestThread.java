
package info.freelibrary.djatoka.ingest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Properties;

import nu.xom.Builder;
import nu.xom.Nodes;
import nu.xom.ParsingException;
import gov.lanl.adore.djatoka.DjatokaCompress;
import gov.lanl.adore.djatoka.DjatokaEncodeParam;
import gov.lanl.adore.djatoka.DjatokaException;
import gov.lanl.adore.djatoka.ICompress;
import gov.lanl.adore.djatoka.kdu.KduCompressExe;

import info.freelibrary.djatoka.Constants;
import info.freelibrary.util.DirFileFilter;
import info.freelibrary.util.FileExtFileFilter;
import info.freelibrary.util.FileUtils;
import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;
import info.freelibrary.util.PairtreeObject;
import info.freelibrary.util.PairtreeRoot;
import info.freelibrary.util.PairtreeUtils;
import info.freelibrary.util.RegexFileFilter;

public class IngestThread extends Thread implements Constants {

    private static final Logger LOGGER = LoggerFactory.getLogger(IngestThread.class, "freelib-djatoka_messages");

    private static final String MAX_SIZE = "djatoka.ingest.file.maxSize";

    private static final String ID_QUERY = "//id";

    private boolean isFinished;

    private boolean isWaiting;

    private int myCount;

    private final DjatokaEncodeParam myParams;

    private final ICompress myCompression;

    private final String[] myExts;

    private final long myMaxSize;

    private final File mySource;

    private final File myDest;

    private final boolean myThreadRunsUnattended;

    /**
     * Creates a new ingest thread with the supplied source and target directories; this thread looks for files with
     * the extensions in the supplied array and is configured with a boolean to run attended (by a human) or
     * unattended.
     *
     * @param aSource A source directory of source files
     * @param aDest A target directory of JP2 files
     * @param aExts Extensions of the files to convert and ingest
     * @param aCfg A configuration to use for the ingest
     * @param aUnattendedRun Whether the ingest is attended by a person or not
     * @throws Exception If there is a problem
     */
    public IngestThread(final File aSource, final File aDest, final String[] aExts, final Properties aCfg,
            final boolean aUnattendedRun) throws Exception {
        super();

        mySource = aSource;
        myDest = aDest;
        myExts = aExts;
        myThreadRunsUnattended = aUnattendedRun;

        myParams = new DjatokaEncodeParam(aCfg);
        myCompression = new KduCompressExe();

        // Convert maximum file size to bytes
        myMaxSize = Long.parseLong(aCfg.getProperty(MAX_SIZE, "200")) * 1048576;
    }

    @Override
    public void run() {
        super.run();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("INGEST_THREAD_STARTING", getId());
        }

        isWaiting = true;

        try {
            // Go through requested directory, converting to TIFs to JP2s
            convert(mySource, myDest);

            // Add converted file system JP2s to the Pairtree cache directory
            loadFileSystemImages(myDest, mySource);
        } catch (final Exception details) {
            LOGGER.error("INGEST_EXCEPTION", details);
        }

        isFinished = true;

        while (isWaiting && !myThreadRunsUnattended) {
            try {
                Thread.sleep(1000);
            } catch (final InterruptedException details) {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn(details.getMessage(), details);
                }
            }
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("INGEST_SHUTDOWN", getId(), myCount);
        }
    }

    /**
     * Returns the current ingest count.
     *
     * @return The current ingest count
     */
    public int getCount() {
        return myCount;
    }

    /**
     * Cleans up the ingest thread.
     */
    public void cleanUp() {
        isWaiting = false;
    }

    /**
     * Returns true if the ingest thread is finished; else, false.
     *
     * @return True if the ingest thread is finished; else, false
     */
    public boolean isFinished() {
        return isFinished;
    }

    private void convert(final File aSource, final File aDest) throws IOException, Exception {
        final File[] files = aSource.listFiles(new FileExtFileFilter(myExts));
        final File[] dirs = aSource.listFiles(new DirFileFilter());
        final PairtreeRoot ptRoot = new PairtreeRoot(myDest); // JP2 directory
        final int pathIndex = myDest.getAbsolutePath().length();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("INGEST_DIRS_FOUND", dirs.length, aSource);
        }

        // These are the directories into which we convert our JP2s
        for (final File nextSource : dirs) {
            final String fileName = nextSource.getName();

            if (!fileName.startsWith(".")) {
                final File dest = new File(aDest, nextSource.getName());

                if (dest.exists() && (!dest.canWrite() || !dest.isDirectory())) {
                    throw new IOException("Problem with destination directory: " + dest.getAbsolutePath());
                } else {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("INGEST_DESCENDING", dest);
                    }

                    if (!dest.exists() && dest.mkdirs() || dest.exists() && dest.isDirectory()) {
                        convert(nextSource, dest); // go into a sub-directory
                    } else {
                        throw new IOException("Failed to create a new directory: " + dest.getAbsolutePath());
                    }
                }
            }
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("INGEST_FILES_FOUND", files.length, aSource);
        }

        // These are the actual image files that we're going to convert
        for (final File next : files) {
            final String fileName = FileUtils.stripExt(next.getName()) + JP2_EXT;
            final String sourceFileName = next.getAbsolutePath();
            final File nextDest = new File(aDest, fileName); // JP2 image file

            if (next.length() > myMaxSize) {
                if (LOGGER.isErrorEnabled()) {
                    final long size = next.length() / 1048576;
                    LOGGER.error("INGEST_TOO_LARGE", sourceFileName, size);
                }

                continue; // We've written to the error log, move along...
            }

            if (!aDest.exists() && !aDest.mkdirs()) {
                throw new IOException("Unable to create new directory: " + nextDest.getAbsolutePath());
            }

            if (!fileName.startsWith(".")) {
                final String destFileName = nextDest.getAbsolutePath();

                // Check to see whether we've already converted this file!
                final String path = FileUtils.stripExt(nextDest.getAbsolutePath());
                final String id = "--" + path.substring(pathIndex);
                final PairtreeObject ptDir = ptRoot.getObject(id);
                final String ptFileName = PairtreeUtils.encodeID(id);
                final File jp2 = new File(ptDir, ptFileName);

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("INGEST_EXISTS_CHECK", jp2);
                }

                // We skip if it exists; delete it if you want to re-convert
                if (jp2.exists()) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("INGEST_SKIPPING", jp2);
                    }

                    continue;
                }

                // This file would only exist if the file system copy from a
                // previous conversion wasn't copied into the JP2 directory
                // like it should have been... it's an indication of a problem.
                if (nextDest.exists()) {
                    if (LOGGER.isWarnEnabled()) {
                        LOGGER.warn("INGEST_MOVING_STALE", nextDest);
                    }

                    if (!nextDest.delete() && LOGGER.isWarnEnabled()) {
                        LOGGER.warn("FILE_NOT_DELETED", nextDest);
                    }
                }

                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("INGEST_COMPRESSING", sourceFileName, destFileName, Integer.toString(myCount + 1));
                }

                try {
                    DjatokaCompress.compress(myCompression, sourceFileName, destFileName, myParams);

                    // If we get here, the conversion was successful; note it
                    myCount += 1;

                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("INGEST_WRITTEN_TO_DISK", destFileName, Long.toString(new File(destFileName)
                                .length()));
                    }
                } catch (final DjatokaException details) {
                    LOGGER.error("INGEST_COMPRESSION_FAILED", destFileName, sourceFileName, details.getMessage());

                    if (!new File(destFileName).delete()) {
                        LOGGER.error("INGEST_BROKEN_FILE", destFileName);
                    }
                } catch (final NullPointerException details) {
                    LOGGER.error("INGEST_EMPTY_SOURCE", sourceFileName);
                }
            }
        }
    }

    private void loadFileSystemImages(final File aJP2Dir, final File aSource) throws IOException,
            FileNotFoundException {
        final FilenameFilter filter = new RegexFileFilter(JP2_FILE_PATTERN);
        final PairtreeRoot pairtree = new PairtreeRoot(aJP2Dir);
        final String skipped = "pairtree_root";
        final Builder bob = new Builder();

        // +1 below is to lose the trailing slash; we add via "--/" below
        final int pathIndex = aJP2Dir.getAbsolutePath().length() + 1;

        // Descend through file system skipping our already mapped Pairtree dir
        for (final File file : FileUtils.listFiles(aJP2Dir, filter, true, skipped)) {
            final String id = file.getAbsolutePath().substring(pathIndex);
            final String baseName = FileUtils.stripExt(id);
            final File xmlDescriptor = new File(aSource, baseName + ".xml");

            // Check to see if XML descriptor file exists and use ID from it
            if (xmlDescriptor.exists()) {
                try {
                    final Nodes nodes = bob.build(xmlDescriptor).query(ID_QUERY);

                    if (nodes.size() > 0) {
                        final String idValue = nodes.get(0).getValue();

                        // We store image with its explicit identifier
                        if (!idValue.equals("")) {
                            if (LOGGER.isInfoEnabled()) {
                                LOGGER.info("INGEST_ID_FOUND", file, idValue);
                            }

                            store(pairtree, idValue, file);
                        } else {
                            if (LOGGER.isWarnEnabled()) {
                                LOGGER.warn("INGEST_MISSING_ID", file);
                            }

                            // We fall back to the default storage mechanism
                            store(pairtree, "--/" + baseName, file);
                        }
                    } else {
                        if (LOGGER.isWarnEnabled()) {
                            LOGGER.warn("INGEST_MISSING_ID", file);
                        }

                        // We fall back to the default storage mechanism
                        store(pairtree, "--/" + baseName, file);
                    }
                } catch (final ParsingException details) {
                    if (LOGGER.isErrorEnabled()) {
                        LOGGER.error("INGEST_PARSING_EXCEPTION", file, details);
                    }

                    // We fall back to the default storage mechanism
                    store(pairtree, "--/" + baseName, file);
                }
            } else {
                // Add a path prefix for file-system based Pairtree objects...
                // Making assumption that no external IDs will start with "--"
                store(pairtree, "--/" + baseName, file);
            }
        }
    }

    private void store(final PairtreeRoot aPairtree, final String aID, final File aFile) throws IOException {
        final PairtreeObject ptDir = aPairtree.getObject(aID);
        final String ptFileName = PairtreeUtils.encodeID(aID);
        final File jp2PtFile = new File(ptDir, ptFileName);

        // Move the file into the Pairtree structure
        aFile.renameTo(jp2PtFile);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.info("INGEST_TO_CACHE", aID, jp2PtFile.getAbsolutePath());
        }
    }
}
