package info.freelibrary.djatoka.ingest;

import gov.lanl.adore.djatoka.DjatokaCompress;
import gov.lanl.adore.djatoka.DjatokaEncodeParam;
import gov.lanl.adore.djatoka.ICompress;
import gov.lanl.adore.djatoka.kdu.KduCompressExe;

import info.freelibrary.util.DirFileFilter;
import info.freelibrary.util.FileExtFileFilter;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IngestThread extends Thread {

    private static final Logger LOGGER = LoggerFactory
	    .getLogger(IngestThread.class);

    private static final String JP2_EXT = ".jp2";
    
    private static boolean isFinished;
    private static boolean isWaiting;
    private static int myCount;

    private DjatokaEncodeParam myParams;
    private ICompress myCompression;
    private String[] myExts;
    private File mySource;
    private File myDest;

    public IngestThread(File aSource, File aDest, String[] aExts,
	    Properties aCfg) throws Exception {
	super();

	mySource = aSource;
	myDest = aDest;
	myExts = aExts;

	myParams = new DjatokaEncodeParam(aCfg);
	myCompression = new KduCompressExe();
    }

    @Override
    public void run() {
	super.run();

	isWaiting = true;
	
	try {
	    convert(mySource, myDest);
	}
	catch (Exception details) {
	    LOGGER.error(details.getMessage(), details);
	}
	
	isFinished = true;
	
	while (isWaiting) {
	    try {
		Thread.sleep(1000);
	    }
	    catch (InterruptedException details) {
		if (LOGGER.isWarnEnabled()) {
		    LOGGER.warn(details.getMessage(), details);
		}
	    }
	}
    }

    public int getCount() {
	return myCount;
    }
    
    public void finish() {
	isWaiting = false;
    }
    
    private int convert(File aSource, File aDest) throws IOException, Exception {
	File[] files = aSource.listFiles(new FileExtFileFilter(myExts));
	File[] dirs = aSource.listFiles(new DirFileFilter());
	int total = 0;

	if (LOGGER.isDebugEnabled()) {
	    LOGGER.debug("Found {} directories in {}", dirs.length, aSource);
	}

	for (File nextSource : dirs) {
	    String fileName = nextSource.getName();

	    if (!fileName.startsWith(".")) {
		File dest = new File(aDest, nextSource.getName());

		if (dest.exists() && (!dest.canWrite() || !dest.isDirectory())) {
		    throw new IOException(
			    "Problem with destination directory: "
				    + dest.getAbsolutePath());
		}
		else {
		    if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Descending into new directory: {}", dest);
		    }

		    if ((!dest.exists() && dest.mkdirs())
			    || (dest.exists() && dest.isDirectory())) {
			total += convert(nextSource, dest);
		    }
		    else {
			throw new IOException(
				"Failed to create a new directory: "
					+ dest.getAbsolutePath());
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
		String sourceFileName = nextSource.getAbsolutePath();
		String destFileName = nextDest.getAbsolutePath();

		if (LOGGER.isInfoEnabled()) {
		    LOGGER.info("Compressing {} to {} ({})",
			    new String[] { sourceFileName, destFileName,
				    Integer.toString(total + 1) });
		}

		total += 1;
		DjatokaCompress.compress(myCompression, sourceFileName,
			destFileName, myParams);
		myCount += 1;

		if (LOGGER.isDebugEnabled()) {
		    String fileSize = Long.toString(new File(destFileName)
			    .length());
		    LOGGER.debug("{} written to disk: {}", new String[] {
			    destFileName, fileSize });
		}
	    }
	}

	return total;
    }

    private String stripExt(String aFileName) {
	int index;

	if ((index = aFileName.lastIndexOf('.')) != -1) {
	    return aFileName.substring(0, index);
	}

	return aFileName;
    }
}
