
package info.freelibrary.djatoka.util;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import java.util.concurrent.CountDownLatch;

import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.Session;

@WebSocket
public class LogSocket {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(LogSocket.class);

    private static final String LOG_DIR = new File("target/logs")
            .getAbsolutePath();

    private final CountDownLatch myCloseLatch;

    private Session mySession;

    private LogWatcherThread myLogThread;

    /**
     * Creates a <code>LogSocket</code> for use with the
     * <code>LogSocketServlet</code>.
     */
    public LogSocket() {
        myCloseLatch = new CountDownLatch(1);
    }

    /**
     * Tasks to run on the closing of this socket.
     * 
     * @param aStatusCode The int code for the socket's closing
     * @param aReason The reason for the socket's closing
     */
    @OnWebSocketClose
    public void onClose(int aStatusCode, String aReason) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[{}] Session closed: {}", mySession.hashCode(),
                    aReason);
        }

        if (myLogThread != null) {
            myLogThread.interrupt();
            myLogThread = null;
        }

        mySession = null;
        myCloseLatch.countDown();
    }

    /**
     * On the connection of a new <code>LogSocket</code>, set the
     * <code>Session</code> information.
     * 
     * @param aSession The session associated with this <code>LogSocket</code>
     */
    @OnWebSocketConnect
    public void onConnect(Session aSession) {
        if (LOGGER.isDebugEnabled()) {
            int hashCode = aSession.hashCode();
            LOGGER.debug("[{}] New log viewer session connected: {}", hashCode,
                    hashCode);
        }

        // Set our new session
        mySession = aSession;
    }

    @OnWebSocketError
    public void onError(Session aSession, Throwable aThrowable) {
        LOGGER.error("[{}] Error: {}", aSession.hashCode(), aThrowable
                .getMessage());
    }

    @OnWebSocketMessage
    public void onMessage(String aMessage) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[{}] Message received from browser: {}", mySession
                    .hashCode(), aMessage);
        }

        try {
            RemoteEndpoint remote = mySession.getRemote();

            if (aMessage.contains(":")) {
                String[] instructions = aMessage.split(":");

                if (instructions[0].equals("log") && instructions.length > 1) {
                    openNewLog(instructions[1], remote);
                } else if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn("[{}] Requested viewer missing log name",
                            mySession.hashCode());
                }
            } else {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("[{}] Unrecognized request from remote",
                            mySession.hashCode());
                }

                mySession.close(StatusCode.BAD_DATA, "Unrecognized request");
            }
        } catch (Throwable details) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn(details.getMessage(), details);
            }
        }
    }

    /**
     * When opening a new log, start reading from it to send its log events to
     * the remote client.
     * 
     * @param aLog A log we want to read
     * @param aRemote A remote client to which we want to send the log events
     */
    private void openNewLog(String aLog, RemoteEndpoint aRemote) {
        int hashID = mySession.hashCode();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(
                    "[{}] Handling request for the contents of the {}.log",
                    hashID, aLog);
        }

        try {
            if (myLogThread != null) {
                myLogThread.interrupt(); // discontinue other log watcher
            }

            // Configure our new log watcher thread
            myLogThread = new LogWatcherThread(LOG_DIR, hashID, aRemote);

            // ... and fire it up
            myLogThread.start();
        } catch (IOException details) {
            LOGGER.error(details.getMessage(), details);
            myLogThread = null;
        }
    }
}
