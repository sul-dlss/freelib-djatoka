
package gov.lanl.util;

/*
 * Copyright  2000-2005 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

// MODIFIED FROM ANT CVS HEAD:
//    http://cvs.apache.org/viewcvs.cgi/ant/src/main/org/apache/tools/ant/taskdefs/
/**
 * Copies standard output and error of subprocesses to standard output and error of the parent process.
 *
 * @since Ant 1.2
 */
public class PumpStreamHandler implements ExecuteStreamHandler {

    private Thread outputThread;

    private Thread errorThread;

    private StreamPumper inputPump;

    private final OutputStream out;

    private final OutputStream err;

    private final InputStream input;

    /**
     * Construct a new <CODE>PumpStreamHandler</CODE>.
     *
     * @param out the output <CODE>OutputStream</CODE>.
     * @param err the error <CODE>OutputStream</CODE>.
     * @param input the input <CODE>InputStream</CODE>.
     */
    public PumpStreamHandler(final OutputStream out, final OutputStream err, final InputStream input) {
        this.out = out;
        this.err = err;
        this.input = input;
    }

    /**
     * Construct a new <CODE>PumpStreamHandler</CODE>.
     *
     * @param out the output <CODE>OutputStream</CODE>.
     * @param err the error <CODE>OutputStream</CODE>.
     */
    public PumpStreamHandler(final OutputStream out, final OutputStream err) {
        this(out, err, null);
    }

    /**
     * Construct a new <CODE>PumpStreamHandler</CODE>.
     *
     * @param outAndErr the output/error <CODE>OutputStream</CODE>.
     */
    public PumpStreamHandler(final OutputStream outAndErr) {
        this(outAndErr, outAndErr);
    }

    /**
     * Construct a new <CODE>PumpStreamHandler</CODE>.
     */
    public PumpStreamHandler() {
        this(System.out, System.err);
    }

    /**
     * Set the <CODE>InputStream</CODE> from which to read the standard output of the process.
     *
     * @param is the <CODE>InputStream</CODE>.
     */
    @Override
    public void setProcessOutputStream(final InputStream is) {
        createProcessOutputPump(is, out);
    }

    /**
     * Set the <CODE>InputStream</CODE> from which to read the standard error of the process.
     *
     * @param is the <CODE>InputStream</CODE>.
     */
    @Override
    public void setProcessErrorStream(final InputStream is) {
        if (err != null) {
            createProcessErrorPump(is, err);
        }
    }

    /**
     * Set the <CODE>OutputStream</CODE> by means of which input can be sent to the process.
     *
     * @param os the <CODE>OutputStream</CODE>.
     */
    @Override
    public void setProcessInputStream(final OutputStream os) {
        if (input != null) {
            inputPump = createInputPump(input, os, true);
        } else {
            try {
                os.close();
            } catch (final IOException e) {
                // ignore
            }
        }
    }

    /**
     * Start the <CODE>Thread</CODE>s.
     */
    @Override
    public void start() {
        outputThread.start();
        errorThread.start();
        if (inputPump != null) {
            final Thread inputThread = new Thread(inputPump);
            inputThread.setDaemon(true);
            inputThread.start();
        }
    }

    /**
     * Stop pumping the streams.
     */
    @Override
    public void stop() {
        try {
            outputThread.join();
        } catch (final InterruptedException e) {
            // ignore
        }
        try {
            errorThread.join();
        } catch (final InterruptedException e) {
            // ignore
        }

        if (inputPump != null) {
            inputPump.stop();
        }

        try {
            err.flush();
        } catch (final IOException e) {
            // ignore
        }
        try {
            out.flush();
        } catch (final IOException e) {
            // ignore
        }
    }

    /**
     * Get the error stream.
     *
     * @return <CODE>OutputStream</CODE>.
     */
    protected OutputStream getErr() {
        return err;
    }

    /**
     * Get the output stream.
     *
     * @return <CODE>OutputStream</CODE>.
     */
    protected OutputStream getOut() {
        return out;
    }

    /**
     * Create the pump to handle process output.
     *
     * @param is the <code>InputStream</code>.
     * @param os the <code>OutputStream</code>.
     */
    protected void createProcessOutputPump(final InputStream is, final OutputStream os) {
        outputThread = createPump(is, os);
    }

    /**
     * Create the pump to handle error output.
     *
     * @param is the input stream to copy from.
     * @param os the output stream to copy to.
     */
    protected void createProcessErrorPump(final InputStream is, final OutputStream os) {
        errorThread = createPump(is, os);
    }

    /**
     * Creates a stream pumper to copy the given input stream to the given output stream.
     *
     * @param is the input stream to copy from.
     * @param os the output stream to copy to.
     * @return a thread object that does the pumping.
     */
    protected Thread createPump(final InputStream is, final OutputStream os) {
        return createPump(is, os, false);
    }

    /**
     * Creates a stream pumper to copy the given input stream to the given output stream.
     *
     * @param is the input stream to copy from.
     * @param os the output stream to copy to.
     * @param closeWhenExhausted if true close the inputstream.
     * @return a thread object that does the pumping.
     */
    protected Thread createPump(final InputStream is, final OutputStream os, final boolean closeWhenExhausted) {
        final Thread result = new Thread(new StreamPumper(is, os, closeWhenExhausted));
        result.setDaemon(true);
        return result;
    }

    /**
     * Creates a stream pumper to copy the given input stream to the given output stream. Used for standard input.
     *
     * @since Ant 1.6.3
     */
    /* protected */StreamPumper createInputPump(final InputStream is, final OutputStream os,
            final boolean closeWhenExhausted) {
        final StreamPumper pumper = new StreamPumper(is, os, closeWhenExhausted);
        pumper.setAutoflush(true);
        return pumper;
    }

}
