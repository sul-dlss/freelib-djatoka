
package info.freelibrary.djatoka.ingest;

import info.freelibrary.djatoka.Constants;

import info.freelibrary.util.FileUtils;
import info.freelibrary.util.RegexFileFilter;
import info.freelibrary.util.DirFileFilter;
import info.freelibrary.util.FileExtFileFilter;
import info.freelibrary.util.PairtreeObject;
import info.freelibrary.util.PairtreeUtils;
import info.freelibrary.util.PairtreeRoot;

import java.io.FileNotFoundException;
import java.io.FilenameFilter;

import gov.lanl.adore.djatoka.DjatokaCompress;
import gov.lanl.adore.djatoka.DjatokaEncodeParam;
import gov.lanl.adore.djatoka.DjatokaException;
import gov.lanl.adore.djatoka.ICompress;
import gov.lanl.adore.djatoka.kdu.KduCompressExe;

import java.io.File;
import java.io.IOException;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IngestThread extends Thread implements Constants {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(IngestThread.class);

    private static final String MAX_SIZE = "djatoka.ingest.file.maxSize";

    private boolean isFinished;

    private boolean isWaiting;

    private int myCount;

    private DjatokaEncodeParam myParams;

    private ICompress myCompression;

    private String[] myExts;

    private long myMaxSize;

    private File mySource;

    private File myDest;

    private boolean myThreadRunsUnattended;

    /**
     * Creates a new ingest thread with the supplied source and target
     * directories; this thread looks for files with the extensions in the
     * supplied array and is configured with a boolean to run attended (by a
     * human) or unattended.
     * 
     * @param aSource A source directory of source files
     * @param aDest A target directory of JP2 files
     * @param aExts Extensions of the files to convert and ingest
     * @param aCfg A configuration to use for the ingest
     * @param aUnattendedRun Whether the ingest is attended by a person or not
     * @throws Exception If there is a problem
     */
    public IngestThread(File aSource, File aDest, String[] aExts,
            Properties aCfg, boolean aUnattendedRun) throws Exception {
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
            LOGGER.debug("Starting up ingest thread: #{}", getId());
        }

        isWaiting = true;

        try {
            // Go through requested directory, converting to TIFs to JP2s
            convert(mySource, myDest);

            // Add converted file system JP2s to the Pairtree cache directory
            loadFileSystemImages(myDest);
        } catch (Exception details) {
            LOGGER.error(details.getMessage(), details);
        }

        isFinished = true;

        while (isWaiting && !myThreadRunsUnattended) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException details) {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn(details.getMessage(), details);
                }
            }
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Shutting down ingest thread: #{} ({} ingested)",
                    getId(), myCount);
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

    private void convert(File aSource, File aDest) throws IOException,
            Exception {
        File[] files = aSource.listFiles(new FileExtFileFilter(myExts));
        File[] dirs = aSource.listFiles(new DirFileFilter());
        PairtreeRoot ptRoot = new PairtreeRoot(myDest); // JP2 directory
        int pathIndex = myDest.getAbsolutePath().length();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Found {} directories in {}", dirs.length, aSource);
        }

        // These are the directories into which we convert our JP2s
        for (File nextSource : dirs) {
            String fileName = nextSource.getName();

            if (!fileName.startsWith(".")) {
                File dest = new File(aDest, nextSource.getName());

                if (dest.exists() && (!dest.canWrite() || !dest.isDirectory())) {
                    throw new IOException(
                            "Problem with destination directory: " +
                                    dest.getAbsolutePath());
                } else {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Descending into new directory: {}", dest);
                    }

                    if ((!dest.exists() && dest.mkdirs()) ||
                            (dest.exists() && dest.isDirectory())) {
                        convert(nextSource, dest); // go into a sub-directory
                    } else {
                        throw new IOException(
                                "Failed to create a new directory: " +
                                        dest.getAbsolutePath());
                    }
                }
            }
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Found {} files in {}", files.length, aSource);
        }

        // These are the actual image files that we're going to convert
        for (File next : files) {
            String fileName = FileUtils.stripExt(next.getName()) + JP2_EXT;
            String sourceFileName = next.getAbsolutePath();
            File nextDest = new File(aDest, fileName); // JP2 image file

            if (next.length() > myMaxSize) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Source file too large: {} ({} MB)",
                            sourceFileName, next.length() / 1048576);
                }

                continue; // We've written to the error log, move along...
            }

            if (!aDest.exists() && !aDest.mkdirs()) {
                throw new IOException("Unable to create new directory: " +
                        nextDest.getAbsolutePath());
            }

            if (!fileName.startsWith(".")) {
                String destFileName = nextDest.getAbsolutePath();

                // Check to see whether we've already converted this file!
                String path = FileUtils.stripExt(nextDest.getAbsolutePath());
                String id = "--" + path.substring(pathIndex);
                PairtreeObject ptDir = ptRoot.getObject(id);
                String ptFileName = PairtreeUtils.encodeID(id);
                File jp2 = new File(ptDir, ptFileName);

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Checking to see if {} already exists", jp2);
                }

                // We skip if it exists; delete it if you want to re-convert
                if (jp2.exists()) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Skipping already converted JP2: {}", jp2);
                    }

                    continue;
                }

                // This file would only exist if the file system copy from a
                // previous conversion wasn't copied into the JP2 directory
                // like it should have been... it's an indication of a problem.
                if (nextDest.exists()) {
                    if (LOGGER.isWarnEnabled()) {
                        LOGGER.warn("Moving aside stale artifact: {}", nextDest
                                .getAbsolutePath());
                    }

                    nextDest.delete(); // clean up old problematic files
                }

                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("Compressing {} to {} ({})", sourceFileName,
                            destFileName, Integer.toString(myCount + 1));
                }

                try {
                    DjatokaCompress.compress(myCompression, sourceFileName,
                            destFileName, myParams);

                    // If we get here, the conversion was successful; note it
                    myCount += 1;

                    if (LOGGER.isDebugEnabled()) {
                        String fileSize =
                                Long.toString(new File(destFileName).length());
                        LOGGER.debug("{} written to disk: {}", destFileName,
                                fileSize);
                    }
                } catch (DjatokaException details) {
                    LOGGER.error("Compression of {} (from {}) failed: {}",
                            destFileName, sourceFileName, details.getMessage());

                    if (!new File(destFileName).delete()) {
                        LOGGER.error("Could not delete broken file: {}",
                                destFileName);
                    }
                } catch (NullPointerException details) {
                    LOGGER.error("File ({}) seems to be an empty source image",
                            sourceFileName);
                }
            }
        }
    }

    private void loadFileSystemImages(File aJP2Dir) throws IOException,
            FileNotFoundException {
        FilenameFilter filter = new RegexFileFilter(JP2_FILE_PATTERN);
        String[] skipped = new String[] {"pairtree_root"};
        PairtreeRoot pairtree = new PairtreeRoot(aJP2Dir);

        // +1 below is to lose the trailing slash; we add via "--/" below
        int pathIndex = aJP2Dir.getAbsolutePath().length() + 1;

        // Descend through file system skipping our already mapped Pairtree dir
        for (File file : FileUtils.listFiles(aJP2Dir, filter, true, skipped)) {
            String id = file.getAbsolutePath().substring(pathIndex);

            // Add a path prefix for file-system based Pairtree objects...
            // Making assumption that no external IDs will start with "--"
            id = "--/" + FileUtils.stripExt(id);

            PairtreeObject ptDir = pairtree.getObject(id);
            String ptFileName = PairtreeUtils.encodeID(id);
            File jp2PtFile = new File(ptDir, ptFileName);

            // Move the file into the Pairtree structure
            file.renameTo(jp2PtFile);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.info("Adding image {} to local cache: {}", id, jp2PtFile
                        .getAbsolutePath());
            }
        }
    }

}
