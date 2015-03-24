/*
 * Copyright (c) 2008 Los Alamos National Security, LLC.
 *
 * Los Alamos National Laboratory Research Library Digital Library Research &
 * Prototyping Team
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
 */

package gov.lanl.adore.djatoka.kdu;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.martiansoftware.jsap.CommandLineTokenizer;

import gov.lanl.adore.djatoka.DjatokaEncodeParam;
import gov.lanl.adore.djatoka.DjatokaException;
import gov.lanl.adore.djatoka.ICompress;
import gov.lanl.adore.djatoka.util.IOUtils;
import gov.lanl.adore.djatoka.util.ImageProcessingUtils;
import gov.lanl.adore.djatoka.util.ImageRecord;
import gov.lanl.adore.djatoka.util.ImageRecordUtils;

import info.freelibrary.util.StringUtils;

/**
 * Java bridge for kdu_compress application
 *
 * @author Ryan Chute
 * @author <a href="mailto:ksclarke@gmail.com">Kevin S. Clarke</a>
 */
public class KduCompressExe implements ICompress {

    private static Logger LOGGER = LoggerFactory.getLogger(KduCompressExe.class);

    private static boolean isWindows;

    private static String env;

    private static String exe;

    private static String[] envParams;

    /** The name of the compressing executable */
    public static final String KDU_COMPRESS_EXE = "kdu_compress";

    public static final String STDOUT = "/dev/stdout";

    static {
        env = System.getProperty("kakadu.home") + System.getProperty("file.separator");
        exe = env + (System.getProperty("os.name").startsWith("Win") ? KDU_COMPRESS_EXE + ".exe" : KDU_COMPRESS_EXE);

        if (System.getProperty("os.name").startsWith("Mac")) {
            envParams = new String[] { "DYLD_LIBRARY_PATH", System.getProperty("DYLD_LIBRARY_PATH") };
        } else if (System.getProperty("os.name").startsWith("Win")) {
            isWindows = true;
        } else if (System.getProperty("os.name").startsWith("Linux")) {
            envParams = new String[] { "LD_LIBRARY_PATH", System.getProperty("LD_LIBRARY_PATH") };
        } else if (System.getProperty("os.name").startsWith("Solaris")) {
            envParams = new String[] { "LD_LIBRARY_PATH", System.getProperty("LD_LIBRARY_PATH") };
        }

        LOGGER.debug("envParams: " + (envParams != null ? StringUtils.toString(envParams, '=') + " | " : "") + exe);
    }

    /**
     * Constructor which expects the following system properties to be defined and exported.
     * <p/>
     * (Win/Linux/UNIX) LD_LIBRARY_PATH=$DJATOKA_HOME/lib/$DJATOKA_OS
     * <p/>
     * (Mac OS-X) DYLD_LIBRARY_PATH=$DJATOKA_HOME/lib/$DJATOKA_OS
     *
     * @throws Exception
     */
    public KduCompressExe() {
        env = System.getProperty("kakadu.home");

        if (env == null) {
            LOGGER.error("kakadu.home is not defined");
            throw new RuntimeException("kakadu.home is not defined");
        }
    }

    /**
     * Compress input BufferedImage using provided DjatokaEncodeParam parameters.
     *
     * @param bi in-memory image to be compressed
     * @param output absolute file path for output file.
     * @param aParams DjatokaEncodeParam containing compression parameters.
     * @throws DjatokaException
     */
    @Override
    public void compressImage(final BufferedImage bi, final String output, final DjatokaEncodeParam aParams)
            throws DjatokaException {
        DjatokaEncodeParam params;

        if (aParams == null) {
            params = new DjatokaEncodeParam();
        } else {
            params = aParams;
        }

        if (params.getLevels() == 0) {
            params.setLevels(ImageProcessingUtils.getLevelCount(bi.getWidth(), bi.getHeight()));
        }

        File in = null;

        try {
            in = IOUtils.createTempTiff(bi);
            compressImage(in.getAbsolutePath(), output, params);
        } catch (final IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new DjatokaException(e.getMessage(), e);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new DjatokaException(e.getMessage(), e);
        } finally {
            if (in != null) {
                if (!in.delete() && LOGGER.isWarnEnabled()) {
                    LOGGER.warn("File not deleted: {}", in);
                }
            }
        }
    }

    /**
     * Compress input BufferedImage using provided DjatokaEncodeParam parameters.
     *
     * @param bi in-memory image to be compressed
     * @param output OutputStream to serialize compressed image.
     * @param aParams DjatokaEncodeParam containing compression parameters.
     * @throws DjatokaException
     */
    @Override
    public void compressImage(final BufferedImage bi, final OutputStream output, final DjatokaEncodeParam aParams)
            throws DjatokaException {
        DjatokaEncodeParam params;

        if (aParams == null) {
            params = new DjatokaEncodeParam();
        } else {
            params = aParams;
        }

        if (params.getLevels() == 0) {
            params.setLevels(ImageProcessingUtils.getLevelCount(bi.getWidth(), bi.getHeight()));
        }

        File in = null;
        File out = null;

        try {
            in = IOUtils.createTempTiff(bi);
            out = File.createTempFile("tmp", ".jp2");

            compressImage(in.getAbsolutePath(), out.getAbsolutePath(), params);
            IOUtils.copyStream(new FileInputStream(out), output);
        } catch (final IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new DjatokaException(e.getMessage(), e);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new DjatokaException(e.getMessage(), e);
        }

        if (in != null) {
            if (!in.delete() && LOGGER.isWarnEnabled()) {
                LOGGER.warn("File not deleted: {}", in);
            }
        }

        if (out != null) {
            if (!out.delete() && LOGGER.isWarnEnabled()) {
                LOGGER.warn("File not deleted: {}", out);
            }
        }
    }

    /**
     * Compress input using provided DjatokaEncodeParam parameters.
     *
     * @param input InputStream containing TIFF image bitstream
     * @param output absolute file path for output file.
     * @param aParams DjatokaEncodeParam containing compression parameters.
     * @throws DjatokaException
     */
    @Override
    public void compressImage(final InputStream input, final String output, final DjatokaEncodeParam aParams)
            throws DjatokaException {
        DjatokaEncodeParam params;

        if (aParams == null) {
            params = new DjatokaEncodeParam();
        } else {
            params = aParams;
        }

        File inputFile;

        try {
            inputFile = File.createTempFile("tmp", ".tif");
            inputFile.deleteOnExit();

            IOUtils.copyStream(input, new FileOutputStream(inputFile));

            if (params.getLevels() == 0) {
                ImageRecord dim = ImageRecordUtils.getImageDimensions(inputFile.getAbsolutePath());

                params.setLevels(ImageProcessingUtils.getLevelCount(dim.getWidth(), dim.getHeight()));
                dim = null;
            }
        } catch (final IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new DjatokaException(e.getMessage(), e);
        }

        compressImage(inputFile.getAbsolutePath(), output, params);

        if (inputFile != null) {
            inputFile.delete();
        }
    }

    /**
     * Compress input using provided DjatokaEncodeParam parameters.
     *
     * @param input absolute file path for input file.
     * @param output absolute file path for output file.
     * @param aParams DjatokaEncodeParam containing compression parameters.
     * @throws DjatokaException
     */
    @Override
    public void compressImage(final String input, final String output, final DjatokaEncodeParam aParams)
            throws DjatokaException {
        DjatokaEncodeParam params;

        if (aParams == null) {
            params = new DjatokaEncodeParam();
        } else {
            params = aParams;
        }

        boolean tmp = false;
        File inputFile = null;

        if ((input.toLowerCase().endsWith(".tif") || input.toLowerCase().endsWith(".tiff") || ImageProcessingUtils
                .checkIfTiff(input)) &&
                ImageProcessingUtils.isUncompressedTiff(input)) {
            LOGGER.debug("Processing TIFF: " + input);
            inputFile = new File(input);
        } else {
            try {
                inputFile = IOUtils.createTempTiff(input);
                tmp = true;
            } catch (final Exception details) {
                throw new DjatokaException("Unrecognized file format: " + details.getMessage());
            }
        }

        if (params.getLevels() == 0) {
            ImageRecord dim = ImageRecordUtils.getImageDimensions(inputFile.getAbsolutePath());

            params.setLevels(ImageProcessingUtils.getLevelCount(dim.getWidth(), dim.getHeight()));
            dim = null;
        }

        final File outFile = new File(output);
        final String command = getKduCompressCommand(inputFile.getAbsolutePath(), outFile.getAbsolutePath(), params);
        final List<String> cmdParts = Lists.newArrayList(CommandLineTokenizer.tokenize(command));

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Compression command and environment: {} [{}]", StringUtils
                    .toString(cmdParts.toArray(), ' '), StringUtils.toString(envParams, '='));
            LOGGER.debug("Input file '{}' " + (inputFile.exists() ? "exists" : "doesn't exist"), inputFile);

            // For debug mode, let's tell kakadu to give us more information
            cmdParts.add("-v");
        }

        try {
            final ProcessBuilder processBuilder = new ProcessBuilder(cmdParts);

            processBuilder.directory(new File(env));
            processBuilder.redirectErrorStream(true);

            if (envParams.length == 2) {
                processBuilder.environment().put(envParams[0], envParams[1]);
            }

            final Process process = processBuilder.start();

            if (LOGGER.isInfoEnabled()) {
                final String message =
                        StringUtils.trimToNull(new String(IOUtils.getByteArray(process.getInputStream())));

                if (message != null) {
                    LOGGER.info(message);
                }
            }

            final int result = process.waitFor();

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Compression command exit code for {}: {}", output, result);
            }

            info.freelibrary.util.IOUtils.closeQuietly(process.getInputStream());
            info.freelibrary.util.IOUtils.closeQuietly(process.getOutputStream());
            info.freelibrary.util.IOUtils.closeQuietly(process.getErrorStream());

            process.destroy();

            if (result != 0) {
                throw new DjatokaException("Failed to compress image [exit code: " + output + "]");
            }
        } catch (final IOException details) {
            LOGGER.error(details.getMessage(), details);
            throw new DjatokaException(details.getMessage(), details);
        } catch (final InterruptedException details) {
            LOGGER.error(details.getMessage(), details);
            throw new DjatokaException(details.getMessage(), details);
        } catch (final Exception details) {
            LOGGER.error(details.getMessage(), details);
            throw new DjatokaException(details.getMessage(), details);
        } finally {
            if (tmp) {
                if (!inputFile.delete() && LOGGER.isWarnEnabled()) {
                    LOGGER.warn("File not deleted: {}", inputFile);
                }
            }
        }

        if (!outFile.getAbsolutePath().equals(STDOUT) && !outFile.exists()) {
            throw new DjatokaException("Unknown error occurred during processing.");
        }
    }

    /**
     * Get kdu_compress command line for specified input, output, params.
     *
     * @param input absolute file path for input file.
     * @param output absolute file path for output file.
     * @param params DjatokaEncodeParam containing compression parameters.
     * @return kdu_compress command line for specified input, output, params
     */
    public static final String getKduCompressCommand(final String input, final String output,
            final DjatokaEncodeParam params) {
        final StringBuffer command = new StringBuffer(exe);

        command.append(" -quiet -i ").append(escape(new File(input).getAbsolutePath()));
        command.append(" -o ").append(escape(new File(output).getAbsolutePath()));
        command.append(" ").append(toKduCompressArgs(params));

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Compress command: {}", command.toString());
        }

        return command.toString();
    }

    private static final String escape(final String path) {
        if (path.contains(" ")) {
            return "\"" + path + "\"";
        }

        return path;
    }

    private static String toKduCompressArgs(final DjatokaEncodeParam params) {
        final StringBuffer sb = new StringBuffer();

        if (params.getRate() != null) {
            sb.append("-rate ").append(params.getRate()).append(" ");
        } else {
            sb.append("-slope ").append(params.getSlope()).append(" ");
        }

        if (params.getLevels() > 0) {
            sb.append("Clevels=").append(params.getLevels()).append(" ");
        }

        if (params.getPrecincts() != null) {
            sb.append("Cprecincts=").append(escape(params.getPrecincts()));
            sb.append(" ");
        }

        if (params.getLayers() > 0) {
            sb.append("Clayers=").append(params.getLayers()).append(" ");
        }

        if (params.getProgressionOrder() != null) {
            sb.append("Corder=").append(params.getProgressionOrder()).append(" ");
        }

        if (params.getPacketDivision() != null) {
            sb.append("ORGtparts=").append(params.getPacketDivision()).append(" ");
        }

        if (params.getCodeBlockSize() != null) {
            sb.append("Cblk=").append(escape(params.getCodeBlockSize())).append(" ");
        }

        sb.append("ORGgen_plt=").append(params.getInsertPLT() ? "yes" : "no").append(" ");
        sb.append("Creversible=").append(params.getUseReversible() ? "yes" : "no").append(" ");

        if (params.getJP2ColorSpace() != null && !params.getJP2ColorSpace().isEmpty()) {
            sb.append("-jp2_space ").append(params.getJP2ColorSpace());
            sb.append(' ');
        }

        return sb.toString();
    }
}
