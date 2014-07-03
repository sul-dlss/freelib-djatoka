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

package gov.lanl.adore.djatoka.util;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.lanl.adore.djatoka.io.FormatConstants;
import ij.io.FileInfo;
import ij.io.Opener;
import ij.io.TiffDecoder;

/**
 * Image Processing Utilities
 *
 * @author Ryan Chute
 */
public class ImageProcessingUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageProcessingUtils.class);

    private ImageProcessingUtils() {
    }

    /**
     * Perform a rotation of the provided BufferedImage using degrees of 90, 180, or 270.
     *
     * @param bi BufferedImage to be rotated
     * @param degree
     * @return rotated BufferedImage instance
     */
    public static BufferedImage rotate(final BufferedImage bi, final int degree) {
        final int width = bi.getWidth();
        final int height = bi.getHeight();

        BufferedImage biFlip;

        if (degree == 90 || degree == 270) {
            biFlip = new BufferedImage(height, width, bi.getType());
        } else if (degree == 180) {
            biFlip = new BufferedImage(width, height, bi.getType());
        } else {
            return bi;
        }

        if (degree == 90) {
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    biFlip.setRGB(height - j - 1, i, bi.getRGB(i, j));
                }
            }
        }

        if (degree == 180) {
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    biFlip.setRGB(width - i - 1, height - j - 1, bi.getRGB(i, j));
                }
            }
        }

        if (degree == 270) {
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    biFlip.setRGB(j, width - i - 1, bi.getRGB(i, j));
                }
            }
        }

        bi.flush(); // only clears from VRAM
        // bi = null;

        return biFlip;
    }

    /**
     * Return the number of resolution levels the djatoka API will generate based on the provided pixel dimensions.
     *
     * @param w max pixel width
     * @param h max pixel height
     * @return number of resolution levels
     */
    public static int getLevelCount(final int w, final int h) {
        int l = Math.max(w, h);
        final int m = 96;
        int r = 0;
        int i;
        if (l > 0) {
            for (i = 1; l >= m; i++) {
                l = l / 2;
                r = i;
            }
        }
        return r;
    }

    /**
     * Return the resolution level the djatoka API will use to extract an image for scaling.
     *
     * @param w max pixel width
     * @param h max pixel height
     * @param out_w max pixel width
     * @param out_h max pixel height
     */
    public static int getScalingLevel(final int w, final int h, final int out_w, final int out_h) {
        final int levels = getLevelCount(w, h);
        final int max_source = Math.max(w, h);
        final int max_out = Math.max(out_w, out_h);
        int r = levels + 2;
        int i = max_source;
        while (i >= max_out) {
            i = i / 2;
            r--;
        }
        return r;
    }

    /**
     * Scale provided BufferedImage by the provided factor. A scaling factor value should be greater than 0 and less
     * than 2. Note that scaling will impact performance and image quality.
     *
     * @param bi BufferedImage to be scaled.
     * @param scale positive scaling factor
     * @return scaled instance of provided BufferedImage
     */
    public static BufferedImage scale(final BufferedImage bi, final double scale) {
        final AffineTransformOp op = new AffineTransformOp(AffineTransform.getScaleInstance(scale, scale), null);
        return op.filter(bi, null);
    }

    /**
     * Scale provided BufferedImage to the specified width and height dimensions. If a provided dimension is 0, the
     * aspect ratio is used to calculate a value. Also, if either contains -1, the positive value will be used as for
     * the long side.
     *
     * @param bi BufferedImage to be scaled.
     * @param aWidth width the image is to be scaled to.
     * @param aHeight height the image is to be scaled to.
     * @return scaled instance of provided BufferedImage
     */
    public static BufferedImage scale(final BufferedImage bi, final int aWidth, final int aHeight) {
        int width;
        int height;

        // If either w,h are -1, then calculate based on long side.
        if (aWidth == -1 || aHeight == -1) {
            final int tl = Math.max(aWidth, aHeight);

            if (bi.getWidth() > bi.getHeight()) {
                width = tl;
                height = 0;
            } else {
                height = tl;
                width = 0;
            }
        } else {
            width = aWidth;
            height = aHeight;
        }

        // Calculate dim. based on aspect ratio
        if (width == 0 || height == 0) {
            if (width == 0 && height == 0) {
                return bi;
            }

            if (width == 0) {
                final double n = new Double(height) / new Double(bi.getHeight());
                width = (int) Math.ceil(bi.getWidth() * n);
            }

            if (height == 0) {
                final double n = new Double(width) / new Double(bi.getWidth());
                height = (int) Math.ceil(bi.getHeight() * n);
            }
        }

        final double scaleH = new Double(height) / new Double(bi.getHeight());
        final double scaleW = new Double(width) / new Double(bi.getWidth());

        return scale(bi, Math.min(scaleH, scaleW));
    }

    private static final String magic = "000c6a502020da87a";

    /**
     * Read first 12 bytes from File to determine if JP2 file.
     *
     * @param aFilename Path to JPEG 2000 image file
     * @return true is JP2 compatible format
     */
    public final static boolean checkIfJp2(final String aFilename) {
        boolean isJP2 = false;

        try {
            final BufferedInputStream inStream = new BufferedInputStream(new FileInputStream(new File(aFilename)));
            isJP2 = checkIfJp2(inStream);
            inStream.close();
        } catch (final FileNotFoundException e) {
            LOGGER.error("File not found: {}", aFilename);
        } catch (final IOException e) {
            LOGGER.error("Could not read: {}", aFilename);
        }

        return isJP2;
    }

    /**
     * Read first 12 bytes from InputStream to determine if JP2 file. Note: Be sure to reset your stream after calling
     * this method.
     *
     * @param in InputStream of possible JP2 codestream
     * @return true is JP2 compatible format
     */
    public final static boolean checkIfJp2(final InputStream in) {
        final byte[] buf = new byte[12];
        try {
            in.read(buf, 0, 12);
        } catch (final IOException e) {
            e.printStackTrace();
            return false;
        }
        final StringBuffer sb = new StringBuffer(buf.length * 2);
        for (final byte element : buf) {
            sb.append(Integer.toHexString(0xff & element));
        }
        final String hexString = sb.toString();
        return hexString.equals(magic);
    }

    /**
     * Given a mimetype, indicates if mimetype is JP2 compatible.
     *
     * @param mimetype mimetype to check if JP2 compatible
     * @return true is JP2 compatible
     */
    public final static boolean isJp2Type(final String mimetype) {
        if (mimetype == null) {
            return false;
        }

        switch (mimetype.toLowerCase()) {
            case FormatConstants.FORMAT_MIMEYPE_JP2:
            case FormatConstants.FORMAT_MIMEYPE_JPX:
            case FormatConstants.FORMAT_MIMEYPE_JPM:
                return true;
            default:
                return false;
        }
    }

    /**
     * Attempt to determine if file is a TIFF using the file header.
     *
     * @param file
     * @return true if the file is a TIFF
     */
    public final static boolean checkIfTiff(final String file) {
        return new Opener().getFileType(file) == Opener.TIFF;
    }

    /**
     * Attempt to determine is file is an uncompressed TIFF
     *
     * @param file File path for image to check
     * @return true if file is an uncompressed TIFF
     */
    public static boolean isUncompressedTiff(final String file) {
        final File f = new File(file);
        FileInfo[] fi = null;
        final TiffDecoder ti = new TiffDecoder(f.getParent() + "/", f.getName());
        try {
            fi = ti.getTiffInfo();
        } catch (final IOException e) {
            return false;
        }
        if (fi[0].compression == 1) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Populates a BufferedImage from a RenderedImage Source: http://www.jguru.com/faq/view.jsp?EID=114602
     *
     * @param img RenderedImage to be converted to BufferedImage
     * @return BufferedImage with complete raster data
     */
    public static BufferedImage convertRenderedImage(final RenderedImage img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }
        final ColorModel cm = img.getColorModel();
        final int width = img.getWidth();
        final int height = img.getHeight();
        final WritableRaster raster = cm.createCompatibleWritableRaster(width, height);
        final boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        final Hashtable properties = new Hashtable();
        final String[] keys = img.getPropertyNames();
        if (keys != null) {
            for (final String key : keys) {
                properties.put(key, img.getProperty(key));
            }
        }
        final BufferedImage result = new BufferedImage(cm, raster, isAlphaPremultiplied, properties);
        img.copyData(raster);
        return result;
    }
}
