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

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.lanl.adore.djatoka.io.reader.DjatokaReader;
import gov.lanl.adore.djatoka.io.writer.TIFWriter;

/**
 * General Utilities for i/o operations in djatoka
 *
 * @author Ryan Chute
 * @author Kevin S. Clarke
 */
public class IOUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(IOUtils.class);

    private IOUtils() {
    }

    /**
     * Create temporary tiff file from provided image file.
     *
     * @param input Absolute path to image file to be converted to tiff.
     * @return File object for temporary image file
     * @throws Exception a I/O or Unsupported format exception
     */
    public static File createTempTiff(final String input) throws Exception {
        final BufferedImage bi = new DjatokaReader().open(input);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("BufferedImage created with h/w: {}/{}", bi.getHeight(), bi.getWidth());
        }

        return createTempTiff(bi);
    }

    /**
     * Create temporary tiff file from provided InputStream. Returns null if exception occurs.
     *
     * @param input InputStream containing a image bitstream
     * @return File object for temporary image file
     */
    public static File createTempImage(final InputStream input) {
        File output = null;
        OutputStream out = null;
        try {
            output = File.createTempFile("tmp", ".img");
            output.deleteOnExit();
            out = new BufferedOutputStream(new FileOutputStream(output));
            copyStream(input, out);
        } catch (final Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (final Exception e) {
                }
            }
        }
        return output;
    }

    /**
     * Create temporary TIFF file from provided BufferedImage object
     *
     * @param bImage BufferedImage containing raster data
     * @return File object for temporary image file
     */
    public static File createTempTiff(final BufferedImage bImage) throws Exception {
        final TIFWriter tifWriter = new TIFWriter();
        final File tifFile = File.createTempFile("tmp", ".tif");

        final FileOutputStream fileOut = new FileOutputStream(tifFile);
        final BufferedOutputStream outStream = new BufferedOutputStream(fileOut);

        tifWriter.write(bImage, outStream);
        outStream.close();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Temp tiff file created: {} (size: {})", tifFile, tifFile.length());
        }

        return tifFile;
    }

    /**
     * Gets an InputStream object for provide URL location
     *
     * @param location File, http, or ftp URL to open connection and obtain InputStream
     * @return InputStream containing the requested resource
     * @throws Exception
     */
    public static InputStream getInputStream(final URL location) throws Exception {
        InputStream in = null;

        if (location.getProtocol().equals("file")) {
            final String fileName = location.getFile();
            in = new BufferedInputStream(new FileInputStream(fileName));
        } else {
            try {
                final URLConnection huc = location.openConnection();
                huc.connect();
                in = huc.getInputStream();
            } catch (final MalformedURLException details) {
                throw new Exception("A MalformedURLException occurred for " + location.toString(), details);
            } catch (final IOException details) {
                throw new Exception("An IOException occurred attempting to connect to " + location.toString(),
                        details);
            }
        }

        return in;
    }

    /**
     * Gets the output stream of the supplied location.
     *
     * @param location The location of the stream to get
     * @return The output stream of the supplied location
     * @throws Exception If there is trouble getting the output stream
     */
    public static OutputStream getOutputStream(final URL location) throws Exception {
        return getOutputStream(getInputStream(location));
    }

    /**
     * Gets the output stream of the supplied input stream.
     *
     * @param ins The input stream to get an output stream for
     * @return The output stream of the supplied input stream
     * @throws Exception If there is trouble getting the output stream
     */
    public static OutputStream getOutputStream(final InputStream ins) throws Exception {
        return getOutputStream(ins, 1024 * 4);
    }

    /**
     * Gets the output stream of the supplied input stream using the supplied buffer size.
     *
     * @param ins The input stream to get an output stream for
     * @param bufferSize The buffer size
     * @return The output stream of the supplied input stream
     * @throws Exception If there is trouble getting the output stream
     */
    public static OutputStream getOutputStream(final InputStream ins, final int bufferSize) throws Exception {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final byte[] buffer = new byte[bufferSize];
        int count = 0;
        final BufferedInputStream bis = new BufferedInputStream(ins);
        while ((count = bis.read(buffer)) != -1) {
            baos.write(buffer, 0, count);
        }
        baos.close();
        return baos;
    }

    /**
     * Copies file from source to destination.
     *
     * @param src The source file
     * @param dest The destination file
     * @return Returns true if the file was successfully copied
     */
    public static boolean copyFile(final File src, final File dest) {
        InputStream in = null;
        OutputStream out = null;
        byte[] buf = null;
        final int bufLen = 20000 * 1024;
        try {
            in = new BufferedInputStream(new FileInputStream(src));
            out = new BufferedOutputStream(new FileOutputStream(dest));
            buf = new byte[bufLen];
            byte[] tmp = null;
            int len = 0;
            while ((len = in.read(buf, 0, bufLen)) != -1) {
                tmp = new byte[len];
                System.arraycopy(buf, 0, tmp, 0, len);
                out.write(tmp);
            }
        } catch (final IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (final Exception e) {
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (final Exception e) {
                }
            }
        }
        return true;
    }

    /**
     * Copies from an input stream to an output stream.
     *
     * @param src The input stream
     * @param dest The output stream
     * @return True if the stream was successfull copied
     */
    public static boolean copyStream(final InputStream src, final OutputStream dest) {
        InputStream in = null;
        OutputStream out = null;
        byte[] buf = null;
        final int bufLen = 20000 * 1024;
        try {
            in = new BufferedInputStream(src);
            out = new BufferedOutputStream(dest);
            buf = new byte[bufLen];
            byte[] tmp = null;
            int len = 0;
            while ((len = in.read(buf, 0, bufLen)) != -1) {
                tmp = new byte[len];
                System.arraycopy(buf, 0, tmp, 0, len);
                out.write(tmp);
            }
        } catch (final IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (final Exception e) {
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (final Exception e) {
                }
            }
        }
        return true;
    }

    /**
     * Returns a byte array from a supplied input stream.
     *
     * @param ins The input stream
     * @return The byte array
     * @throws Exception If there was a problem getting the byte array
     */
    public static byte[] getByteArray(final InputStream ins) throws Exception {
        // TODO: remove CR/LFs
        return ((ByteArrayOutputStream) getOutputStream(ins)).toByteArray();
    }

    /**
     * Loads a configuration file by its path.
     *
     * @param path The path of the configuration file to load
     * @return A properties object with the configuration information
     * @throws Exception If there is trouble reading the configuration file
     */
    public static Properties loadConfigByPath(final String path) throws Exception {
        final FileInputStream fi = new FileInputStream(path);
        return loadProperty(fi);
    }

    /**
     * Loads a configuration file from the supplied input stream.
     *
     * @param in The input stream of the configuration file to load
     * @return A properties object with the configuration information
     * @throws IOException If there is trouble reading the configuration
     */
    public static Properties loadProperty(final InputStream in) throws IOException {
        Properties prop;
        try {
            prop = new java.util.Properties();
            prop.loadFromXML(in);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (final Exception ex) {
                }
            }
        }
        return prop;
    }

    /**
     * Reads bytes from a file.
     *
     * @param file A file from which to read the bytes
     * @return The byte array
     * @throws IOException If there is trouble reading bytes from the file
     */
    public static byte[] getBytesFromFile(final File file) throws IOException {
        InputStream is = null;

        try {
            is = new FileInputStream(file);

            // Get the size of the file
            final long length = file.length();

            final byte[] bytes = new byte[(int) length];

            int offset = 0;
            int numRead = 0;
            while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
                offset += numRead;
            }

            if (offset < bytes.length) {
                is.close();
                throw new IOException("Could not completely read file " + file.getName());
            }

            is.close();
            return bytes;
        } finally {
            info.freelibrary.util.IOUtils.closeQuietly(is);
        }
    }

    /**
     * Loads a configuration file from the classpath.
     *
     * @param name The name of the file to read from the classpath
     * @return A properties object with the configuration data
     * @throws Exception If there is trouble reading from the classpath file
     */
    public static Properties loadConfigByCP(final String name) throws Exception {

        // Get our class loader
        final ClassLoader cl = IOUtils.class.getClassLoader();

        // Attempt to open an input stream to the configuration file.
        // The configuration file is considered to be a system
        // resource.
        java.io.InputStream in;

        if (cl != null) {
            in = cl.getResourceAsStream(name);
        } else {
            in = ClassLoader.getSystemResourceAsStream(name);
        }

        // If the input stream is null, then the configuration file
        // was not found
        if (in == null) {
            throw new Exception("configuration file '" + name + "' not found");
        } else {
            return loadProperty(in);
        }
    }

    /**
     * Gets a ArrayList of File objects provided a dir or file path.
     *
     * @param filePath Absolute path to file or directory
     * @param fileFilter Filter dir content by extention; allows "null"
     * @param recursive Recursively search for files
     * @return ArrayList of File objects matching specified criteria.
     */
    public static ArrayList<File> getFileList(final String filePath, final FileFilter fileFilter,
            final boolean recursive) {
        final ArrayList<File> files = new ArrayList<File>();
        final File file = new File(filePath);
        if (file.exists() && file.isDirectory()) {
            final File[] fa = file.listFiles(fileFilter);
            for (final File element : fa) {
                if (element.isFile()) {
                    files.add(element);
                } else if (recursive && element.isDirectory()) {
                    files.addAll(getFileList(element.getAbsolutePath(), fileFilter, recursive));
                }
            }
        } else if (file.exists() && file.isFile()) {
            files.add(file);
        }

        return files;
    }
}
