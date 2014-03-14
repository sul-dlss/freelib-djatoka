/*
 * Copyright (c) 2009  Los Alamos National Security, LLC.
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

package gov.lanl.adore.djatoka.io.reader;

import java.awt.image.BufferedImage;
import java.io.InputStream;

import gov.lanl.adore.djatoka.io.FormatIOException;
import gov.lanl.adore.djatoka.io.IReader;

/**
 * Djatoka Reader Wrapper - Uses known IReader impl. to read image InputStream or image file path.
 * 
 * @author Ryan Chute
 */
public class DjatokaReader implements IReader {

    private final ImageJReader imagejReader = new ImageJReader();

    /**
     * Returns a BufferedImage instance for provided InputStream
     * 
     * @param input an InputStream consisting of an image bitstream
     * @return a BufferedImage instance for source image InputStream
     * @throws FormatIOException
     */
    @Override
    public BufferedImage open(final String input) throws FormatIOException {
        return imagejReader.open(input);
    }

    /**
     * Returns a BufferedImage instance for provided image file path
     * 
     * @param input absolute file path for image file
     * @return a BufferedImage instance for source image file
     * @throws FormatIOException
     */
    @Override
    public BufferedImage open(final InputStream input) throws FormatIOException {
        BufferedImage bi = null;

        bi = imagejReader.open(input);

        // FIXME: delete; this block can't be reached can it?
        // if (bi == null) {
        // LOGGER.debug("Unable to open using ImageJReader, trying ImageIOReader");
        // bi = imageioReader.open(input);
        // } else {
        // LOGGER.debug("Reading the file using ImageJReader");
        // }

        return bi;
    }
}
