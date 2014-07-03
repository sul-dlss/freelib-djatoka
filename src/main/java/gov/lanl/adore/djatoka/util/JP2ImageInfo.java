/*
 * Copyright (c) 2008  Los Alamos National Security, LLC.
 *
 * Los Alamos National Laboratory
 * Research Library
 * Digital Library Research & Prototyping Team
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
 *
 */

package gov.lanl.adore.djatoka.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JPEG 2000 Metadata Parser
 *
 * @author Ryan Chute
 */
public class JP2ImageInfo implements JP2Markers {

    private static final Logger LOGGER = LoggerFactory.getLogger(JP2ImageInfo.class);

    private InputStream is;

    private int currentDataLength;

    private int currentMarker;

    private final ImageRecord ir;

    private List<String> xmlDocs;

    /**
     * Creates an image info object.
     *
     * @param f A file to initialize the image info object
     * @throws IOException If there is trouble reading from the file
     */
    public JP2ImageInfo(final File f) throws IOException {
        this(new BufferedInputStream(new FileInputStream(f)));
        ir.setImageFile(f.getAbsolutePath());
    }

    /**
     * Creates an image info object.
     *
     * @param is An input stream from which to initialize the image info object
     * @throws IOException If there is trouble reading from the input stream
     */
    public JP2ImageInfo(final InputStream is) throws IOException {
        this.is = is;
        ir = new ImageRecord();
        setImageInfo();
    }

    /**
     * Gets a populated ImageRecords for the initialized image
     *
     * @return a populated ImageRecord
     */
    public ImageRecord getImageRecord() {
        return ir;
    }

    /**
     * Gets a list of xml docs contained in the JPEG 2000 header
     */
    public String[] getXmlDocs() {
        if (xmlDocs != null) {
            return xmlDocs.toArray(new String[xmlDocs.size()]);
        } else {
            return null;
        }
    }

    private void setImageInfo() throws IOException {
        try {
            currentDataLength = read(4);
            if (currentDataLength == MARKER_JP_LEN) {
                currentMarker = read(4);
                if (MARKER_JP != currentMarker) {
                    throw new IOException("Expected JP Marker");
                }
                if (MARKER_JP_SIG != read(4)) {
                    throw new IOException("Invalid JP Marker");
                }
                nextHeader();
                if (MARKER_FTYP != currentMarker) {
                    throw new IOException("FTYP Marker not found");
                }
                skip(currentDataLength - 8);
                nextHeader();
                boolean done = false;
                do {
                    if (MARKER_JP2H == currentMarker) {
                        nextHeader();
                    } else if (MARKER_IHDR == currentMarker) {
                        setIHDR();
                        nextHeader();
                    } else if (MARKER_COLR == currentMarker) {
                        setCOLR();
                        nextHeader();
                    } else if (MARKER_RES_BOX == currentMarker) {
                        setResBox();
                        nextHeader();
                    } else if (MARKER_JP2C == currentMarker) {
                        setJP2C();
                        done = true;
                    } else if (MARKER_XML == currentMarker) {
                        addXmlDoc(getXML());
                        nextHeader();
                    } else if (MARKER_SOC == currentMarker) {
                        done = true;
                    } else {
                        skip(currentDataLength - 8);
                        nextHeader();
                    }
                } while (!done);
                final int compLayers = countCompLayers(readBytes(is.available()));
                ir.setCompositingLayerCount(compLayers);
            } else {
                throw new IOException("Invalid Jpeg2000 file");
            }
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (final Exception e) {
                }
                is = null;
            }
        }
    }

    /**
     * Add a doc to the list of xml docs contained in the JPEG 2000 header
     *
     * @param docs
     */
    private void addXmlDoc(final String doc) {
        if (xmlDocs == null) {
            xmlDocs = new LinkedList<String>();
        }
        xmlDocs.add(doc);
    }

    private void nextHeader() throws IOException {
        currentDataLength = read(4);
        currentMarker = read(4);
        if (currentDataLength == 1) {
            // Process SuperBox
            if (read(4) != 0) {
                throw new IOException("Box length too large");
            }
            currentDataLength = read(4);
            if (currentDataLength == 0) {
                throw new IOException("Invalid box size");
            }
        } else if (currentDataLength <= 0 && currentMarker != MARKER_JP2C) {
            throw new IOException("Invalid box size");
        }
    }

    private int read(final int n) throws IOException {
        int c = 0;
        for (int i = n - 1; i >= 0; i--) {
            c |= (0xff & is.read()) << 8 * i;
        }
        return c;
    }

    private void skip(final int toSkip) throws IOException {
        int n = toSkip;
        long i;

        while (n > 0) {
            i = is.skip(n);

            if (i <= 0) {
                break;
            }

            n -= i;
        }
    }

    private byte[] readBytes(final int n) throws IOException {
        final byte[] b = new byte[n];
        final int bytesRead = is.read(b);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Bytes read: {}", bytesRead);
        }

        return b;
    }

    private static int countCompLayers(final byte[] data) {
        final byte[] pattern = MARKER_JPLH_BIN;
        int cnt = 1;
        int j = 1;
        if (data.length == 0) {
            return 0;
        }

        for (final byte element : data) {
            if (pattern[j] == element) {
                j++;
            } else {
                j = 1;
            }
            if (j == pattern.length) {
                cnt++;
                j = 1;
            }
        }
        return cnt;
    }

    private void setIHDR() throws IOException {
        final int scaledHeight = read(4);
        ir.setHeight(scaledHeight);
        final int scaledWidth = read(4);
        ir.setWidth(scaledWidth);
        final int components = read(2);
        ir.setNumChannels(components);
        final int bitDepth = read(1);
        ir.setBitDepth(bitDepth == 7 ? bitDepth + 1 : bitDepth);
        read(1);
        read(1);
        read(1);
    }

    private void setCOLR() throws IOException {
        final int method = read(1);
        read(1);
        read(1);
        if (method == 2) {
            read(currentDataLength - 3);
        } else {
            read(4);
        }
    }

    private void setResBox() throws IOException {
        read(3);
        read(3);
        read(3);
        read(3);
        read(3);
        read(3);
    }

    private String getXML() throws IOException {
        // Subtract XML Marker, Length Value, XML Flag
        final byte[] xml = readBytes(currentDataLength - 16);
        if (xml.length > 0) {
            return new String(xml);
        } else {
            return null;
        }
    }

    private void setJP2C() throws IOException {
        read(2);
        boolean hend = false;
        while (!hend) {
            final int h = read(2);
            if (h == MARKER_SIZ) { // SIZ
                read(2);
                read(2);
                read(4);
                read(4);
                read(4);
                read(4);
                read(4);
                read(4);
                read(4);
                read(4);
                read(2);
                read(1);
                read(1);
                read(1);
                read(2);
                read(2);
                read(2);
            } else if (h == MARKER_COD) { // COD
                read(2);
                read(1);
                read(1);
                final int sgcod_layers = read(2); // Number of layers
                ir.setQualityLayers(sgcod_layers);
                read(1);
                final int sgcod_levels = read(1); // Number of levels
                ir.setDWTLevels(sgcod_levels);
                final int djatokaLevels = ImageProcessingUtils.getLevelCount(ir.getWidth(), ir.getHeight());
                ir.setLevels(djatokaLevels > sgcod_levels ? sgcod_levels : djatokaLevels);
                read(1);
                read(1);
                read(1);
                read(1);
                hend = true;
            } else {
                throw new IOException("Expecting MARKER_COD or MARKER_SIZ in header");
            }

        }
    }
}
