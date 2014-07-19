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
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.martiansoftware.jsap.CommandLineTokenizer;

import gov.lanl.adore.djatoka.DjatokaDecodeParam;
import gov.lanl.adore.djatoka.DjatokaException;
import gov.lanl.adore.djatoka.IExtract;
import gov.lanl.adore.djatoka.io.FormatFactory;
import gov.lanl.adore.djatoka.io.reader.PNMReader;
import gov.lanl.adore.djatoka.io.writer.JPGWriter;
import gov.lanl.adore.djatoka.util.IOUtils;
import gov.lanl.adore.djatoka.util.ImageProcessingUtils;
import gov.lanl.adore.djatoka.util.ImageRecord;
import gov.lanl.adore.djatoka.util.JP2ImageInfo;

/**
 * Java bridge for kdu_expand application
 *
 * @author Ryan Chute
 * @author <a href="mailto:ksclarke@gmail.com">Kevin S. Clarke</a>
 */
public class KduExtractExe implements IExtract {

    private static Logger LOGGER = LoggerFactory.getLogger(KduExtractExe.class);

    private static boolean isWindows = false;

    private static String env;

    private static String exe;

    private static String[] envParams;

    private final static BufferedImage OOB = getOutOfBoundsImage();

    private static final FormatFactory FORMAT_FACTORY = new FormatFactory();

    /** Name of extraction executable */
    public static final String KDU_EXPAND_EXE = "kdu_expand";

    static {
        env = System.getProperty("kakadu.home") + System.getProperty("file.separator");
        exe = env + (System.getProperty("os.name").contains("Win") ? KDU_EXPAND_EXE + ".exe" : KDU_EXPAND_EXE);

        if (System.getProperty("os.name").startsWith("Mac")) {
            envParams = new String[] { "DYLD_LIBRARY_PATH=" + System.getProperty("DYLD_LIBRARY_PATH") };
        } else if (System.getProperty("os.name").startsWith("Win")) {
            isWindows = true;
        } else if (System.getProperty("os.name").startsWith("Linux")) {
            envParams = new String[] { "LD_LIBRARY_PATH=" + System.getProperty("LD_LIBRARY_PATH") };
        } else if (System.getProperty("os.name").startsWith("Solaris")) {
            envParams = new String[] { "LD_LIBRARY_PATH=" + System.getProperty("LD_LIBRARY_PATH") };
        }

        LOGGER.debug("envParams: " + (envParams != null ? envParams[0] + " | " : "") + exe);
    }

    @Override
    public void extract(final String i, final String o, final DjatokaDecodeParam p, final String f)
            throws DjatokaException {
        ArrayList<Double> dims = null;

        if (p.getRegion() != null) {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            InputStream ins = null;

            try {
                ins = new FileInputStream(i);
                IOUtils.copyStream(ins, baos);
                dims = getRegionMetadata(new ByteArrayInputStream(baos.toByteArray()), p);
            } catch (final FileNotFoundException details) {
                throw new DjatokaException(details);
            } finally {
                info.freelibrary.util.IOUtils.closeQuietly(ins);
                info.freelibrary.util.IOUtils.closeQuietly(baos);
            }
        }

        final String command = getKduExtractCommand(i, o, dims, p);
        final String[] cmdParts = CommandLineTokenizer.tokenize(command);

        try {
            final Process process = Runtime.getRuntime().exec(cmdParts, envParams, new File(env));
            final int result = waitFor(process);

            if (result != 0 && LOGGER.isErrorEnabled()) {
                LOGGER.error("Extraction returned non-zero result");
            } else {
                final BufferedImage image = new PNMReader().open(o + ".ppm");

                // TODO make this support other than JPEGs (using format)
                new JPGWriter().write(image, new FileOutputStream(o));
            }
        } catch (final Exception details) {
            LOGGER.error(details.getMessage(), details);
            throw new DjatokaException(details.getMessage(), details);
        }
    }

    /**
     * Gets KDU Extract Command-line based on dims and parameters.
     *
     * @param input absolute file path of JPEG 2000 image file.
     * @param output absolute file path of PGM output image
     * @param dims array of region parameters (i.e. y,x,h,w)
     * @param params contains rotate and level extraction information
     * @return command line string to extract region using kdu_extract
     */
    public final String getKduExtractCommand(final String input, final String output, final ArrayList<Double> dims,
            final DjatokaDecodeParam params) {
        final StringBuffer command = new StringBuffer(exe);

        command.append(" -quiet -i ");
        command.append(escape(new File(input).getAbsolutePath()));
        command.append(" -o ");
        command.append(escape(new File(output).getAbsolutePath()) + ".ppm");
        command.append(" ").append(toKduExtractArgs(params));

        if (dims != null && dims.size() == 4) {
            final StringBuffer region = new StringBuffer();

            region.append("{").append(dims.get(0)).append(",");
            region.append(dims.get(1)).append("}").append(",");
            region.append("{").append(dims.get(2)).append(",");
            region.append(dims.get(3)).append("}");

            command.append("-region ").append(region.toString()).append(" ");
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(command.toString());
        }

        return command.toString();
    }

    /**
     * Returns populated JPEG 2000 ImageRecord instance
     *
     * @param r ImageRecord containing file path the JPEG 2000 image
     * @return a populated JPEG 2000 ImageRecord instance
     * @throws DjatokaException
     */
    @Override
    public final ImageRecord getMetadata(final ImageRecord r) throws DjatokaException, FileNotFoundException {
        final BufferedInputStream imageStream = null;

        if (r == null) {
            throw new DjatokaException("ImageRecord is null");
        }

        if (r.getImageFile() == null && r.getObject() != null) {
            final Object irObj = r.getObject();
            final ImageRecord ir = getMetadata(getStreamFromObject(irObj));

            ir.setObject(irObj);

            return ir;
        }

        final File f = new File(r.getImageFile());

        if (!f.exists()) {
            throw new FileNotFoundException("Image doesn't exist: " + f.getAbsolutePath());
        }

        if (!ImageProcessingUtils.checkIfJp2(r.getImageFile())) {
            throw new DjatokaException("Image is not a JP2: " + f.getAbsolutePath());
        }

        try {
            return getMetadata(new BufferedInputStream(new FileInputStream(f)));
        } catch (final Exception details) {
            throw new DjatokaException("Image isn't a valid JP2 file: " + details.getMessage(), details);
        } finally {
            info.freelibrary.util.IOUtils.closeQuietly(imageStream);
        }
    }

    /**
     * Returns populated JPEG 2000 ImageRecord instance
     *
     * @param is an InputStream containing the JPEG 2000 codestream
     * @return a populated JPEG 2000 ImageRecord instance
     * @throws DjatokaException
     */
    public final ImageRecord getMetadata(final InputStream is) throws DjatokaException {
        JP2ImageInfo info;

        try {
            info = new JP2ImageInfo(is);
        } catch (final IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new DjatokaException(e.getMessage(), e);
        }

        return info.getImageRecord();
    }

    /**
     * Returns array of XMLBox records contained in JP2 resource.
     *
     * @param r an ImageRecord containing a file path to resource or has object defined
     * @return an array of XML records contained in JP2 XMLboxes
     */
    @Override
    public final String[] getXMLBox(final ImageRecord r) throws DjatokaException {
        String[] xml = null;

        try {
            if (r.getImageFile() == null && r.getObject() != null) {
                xml = new JP2ImageInfo(getStreamFromObject(r.getObject())).getXmlDocs();
            } else {
                xml = new JP2ImageInfo(new File(r.getImageFile())).getXmlDocs();
            }
        } catch (final IOException e) {
            LOGGER.error(e.getMessage(), e);
        }

        return xml;
    }

    /**
     * Utility method to determine type of object stored in ImageRecord and to return it as an InputStream
     *
     * @param o
     * @return an InputStream for the resource contained in ImageRecord object
     */
    public static InputStream getStreamFromObject(final Object o) {
        if (o instanceof BufferedInputStream) {
            return (InputStream) o;
        }

        if (o instanceof InputStream) {
            return new BufferedInputStream((InputStream) o);
        }

        if (o instanceof byte[]) {
            return new ByteArrayInputStream((byte[]) o);
        }

        LOGGER.error(o.getClass().getName() + " is not a supported ImageRecord object type.");

        return null;
    }

    private final ArrayList<Double> getRegionMetadata(final InputStream input, final DjatokaDecodeParam params)
            throws DjatokaException {
        final ImageRecord r = getMetadata(input);
        return getRegionMetadata(r, params);
    }

    private final ArrayList<Double> getRegionMetadata(final String input, final DjatokaDecodeParam params)
            throws DjatokaException, FileNotFoundException {
        return getRegionMetadata(getMetadata(new ImageRecord(input)), params);
    }

    private final ArrayList<Double> getRegionMetadata(final ImageRecord r, final DjatokaDecodeParam params)
            throws DjatokaException {
        if (params.getLevel() >= 0) {
            int levels = ImageProcessingUtils.getLevelCount(r.getWidth(), r.getHeight());
            levels = r.getDWTLevels() < levels ? r.getDWTLevels() : levels;
            final int reduce = levels - params.getLevel();
            params.setLevelReductionFactor(reduce >= 0 ? reduce : 0);
        } else if (params.getLevel() == -1 && params.getRegion() == null && params.getScalingDimensions() != null) {
            final int width = params.getScalingDimensions()[0];
            final int height = params.getScalingDimensions()[1];
            int levels = ImageProcessingUtils.getLevelCount(r.getWidth(), r.getHeight());
            final int scale_level = ImageProcessingUtils.getScalingLevel(r.getWidth(), r.getHeight(), width, height);
            levels = r.getDWTLevels() < levels ? r.getDWTLevels() : levels;
            final int reduce = levels - scale_level;
            params.setLevelReductionFactor(reduce >= 0 ? reduce : 0);
        }

        final int reduce = 1 << params.getLevelReductionFactor();
        final ArrayList<Double> dims = new ArrayList<Double>();

        if (params.getRegion() != null) {
            final StringTokenizer st = new StringTokenizer(params.getRegion(), "{},");
            String token;

            // top
            if ((token = st.nextToken()).contains(".")) {
                dims.add(Double.parseDouble(token));
            } else {
                final int t = Integer.parseInt(token);

                if (r.getHeight() < t) {
                    throw new DjatokaException("Region inset out of bounds: " + t + ">" + r.getHeight());
                }

                dims.add(Double.parseDouble(token) / r.getHeight());
            }

            // left
            if ((token = st.nextToken()).contains(".")) {
                dims.add(Double.parseDouble(token));
            } else {
                final int t = Integer.parseInt(token);

                if (r.getWidth() < t) {
                    throw new DjatokaException("Region inset out of bounds: " + t + ">" + r.getWidth());
                }

                dims.add(Double.parseDouble(token) / r.getWidth());
            }

            // height
            if ((token = st.nextToken()).contains(".")) {
                dims.add(Double.parseDouble(token));
            } else {
                dims.add(Double.parseDouble(token) / (Double.valueOf(r.getHeight()) / Double.valueOf(reduce)));
            }

            // width
            if ((token = st.nextToken()).contains(".")) {
                dims.add(Double.parseDouble(token));
            } else {
                dims.add(Double.parseDouble(token) / (Double.valueOf(r.getWidth()) / Double.valueOf(reduce)));
            }
        }

        return dims;
    }

    private static BufferedImage getOutOfBoundsImage() {
        final BufferedImage bi = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        final int rgb = bi.getRGB(0, 0);
        final int alpha = rgb >> 24 & 0xff;
        bi.setRGB(0, 0, alpha);
        return bi;
    }

    private static String toKduExtractArgs(final DjatokaDecodeParam params) {
        final StringBuffer sb = new StringBuffer();

        if (params.getLevelReductionFactor() > 0) {
            sb.append("-reduce ").append(params.getLevelReductionFactor()).append(" ");
        }

        if (params.getRotationDegree() > 0) {
            sb.append("-rotate ").append(params.getRotationDegree()).append(" ");
        }

        if (params.getCompositingLayer() > 0) {
            sb.append("-jpx_layer ").append(params.getCompositingLayer()).append(" ");
        }

        return sb.toString();
    }

    private static final String escape(final String path) {
        if (path.contains(" ")) {
            return "\"" + path + "\"";
        }

        return path;
    }

    // Process Handler Utils
    private int waitFor(final Process process) {
        try {
            process.waitFor();
            return process.exitValue();
        } catch (final InterruptedException e) {
            process.destroy();
        }

        return 2;
    }

    private static void closeStreams(final Process process) {
        close(process.getInputStream());
        close(process.getOutputStream());
        close(process.getErrorStream());
        process.destroy();
    }

    private static void close(final InputStream device) {
        if (device != null) {
            try {
                device.close();
            } catch (final IOException ioex) {
            }
        }
    }

    private static void close(final OutputStream device) {
        if (device != null) {
            try {
                device.close();
            } catch (final IOException ioex) {
            }
        }
    }
}
