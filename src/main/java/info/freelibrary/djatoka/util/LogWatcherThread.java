
package info.freelibrary.djatoka.util;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogWatcherThread extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogWatcherThread.class);

    private final WatchService myWatchService;

    private final Map<WatchKey, Path> myKeys;

    private final int mySessionHashCode;

    /**
     * Creates a thread that watches the log files.
     *
     * @param aLogPath The path of the files to watch
     * @param aSessionHashCode A session hash code
     * @param aRemoteClient The remote endpoint
     * @throws IOException If there is a problem reading or writing
     */
    public LogWatcherThread(final String aLogPath, final int aSessionHashCode, final RemoteEndpoint aRemoteClient)
            throws IOException {
        final FileSystem fs = FileSystems.getDefault();
        final Path logPath = fs.getPath(aLogPath);

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("[{}] Initializing new {} to monitor file system at {}", aSessionHashCode,
                    LogWatcherThread.class.getSimpleName(), aLogPath);
        }

        // Start the new watch service for our log files
        myWatchService = fs.newWatchService();
        myKeys = new ConcurrentHashMap<WatchKey, Path>();
        mySessionHashCode = aSessionHashCode;

        // Register the path of the log files with our watch service
        myKeys.put(logPath.register(myWatchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY), logPath);
    }

    @Override
    public void interrupt() {
        super.interrupt();

        // Clean up the thread's resources
        try {
            myKeys.clear();
            myWatchService.close();
        } catch (final IOException details) {
            LOGGER.error("[{}] Exception while closing log WatchService: {}", mySessionHashCode,
                    details.getMessage(), details);
        }
    }

    @Override
    public void run() {
        super.run();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[{}] Starting up a new {} (thread ID: {})", mySessionHashCode, LogWatcherThread.class
                    .getSimpleName(), getId());
        }

        while (Thread.interrupted() == false) {
            WatchKey key;

            try {
                key = myWatchService.poll(10, TimeUnit.MILLISECONDS);
            } catch (final InterruptedException details) {
                break;
            } catch (final ClosedWatchServiceException details) {
                break;
            }

            if (key != null) {
                final Path path = myKeys.get(key);

                for (final WatchEvent<?> i : key.pollEvents()) {
                    @SuppressWarnings("unchecked")
                    final WatchEvent<Path> event = (WatchEvent<Path>) i;
                    final WatchEvent.Kind<Path> kind = event.kind();
                    final Path name = event.context();
                    final Path child = path.resolve(name);

                    if (LOGGER.isInfoEnabled()) {
                        LOGGER.info("[{}] {}: {} {}", mySessionHashCode, kind.name(), path, child);
                    }

                    if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                        if (Files.isDirectory(child, LinkOption.NOFOLLOW_LINKS)) {
                            try {
                                Files.walkFileTree(child, new LogFileVisitor());
                            } catch (final IOException details) {
                                details.printStackTrace();
                            }
                        }
                    }

                    if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                        LOGGER.info("Last modified: {}", child.toFile().lastModified());

                    }
                }

                if (key.reset() == false) {
                    if (LOGGER.isInfoEnabled()) {
                        LOGGER.info("[{}] Key '{}' is invalid", mySessionHashCode, key);
                    }

                    myKeys.remove(key);

                    if (myKeys.isEmpty()) {
                        break;
                    }
                }
            }
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[{}] Shutting down running {}; its thread ID is: {}", mySessionHashCode,
                    LogWatcherThread.class.getSimpleName(), getId());
        }
    }

    private class LogFileVisitor extends SimpleFileVisitor<Path> {

        @Override
        public FileVisitResult preVisitDirectory(final Path aLogPath, final BasicFileAttributes aAttributes)
                throws IOException {

            myKeys.put(aLogPath.register(myWatchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY), aLogPath);

            return super.preVisitDirectory(aLogPath, aAttributes);
        }
    }
}
