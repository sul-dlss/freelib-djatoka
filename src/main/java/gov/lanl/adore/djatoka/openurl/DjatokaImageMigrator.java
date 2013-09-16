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

package gov.lanl.adore.djatoka.openurl;

import info.freelibrary.util.PairtreeObject;
import info.freelibrary.util.PairtreeRoot;
import info.freelibrary.util.PairtreeUtils;

import java.net.URL;
import java.net.URI;

import gov.lanl.adore.djatoka.DjatokaEncodeParam;
import gov.lanl.adore.djatoka.DjatokaException;
import gov.lanl.adore.djatoka.ICompress;
import gov.lanl.adore.djatoka.IExtract;
import gov.lanl.adore.djatoka.io.FormatConstants;
import gov.lanl.adore.djatoka.kdu.KduCompressExe;
import gov.lanl.adore.djatoka.kdu.KduExtractExe;
import gov.lanl.adore.djatoka.util.IOUtils;
import gov.lanl.adore.djatoka.util.ImageProcessingUtils;
import gov.lanl.adore.djatoka.util.ImageRecord;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class used to harvest URIs and compress files into JP2.
 * 
 * @author Ryan Chute
 * @author Kevin S. Clarke <a
 *         href="mailto:ksclarke@gmail.com">ksclarke@gmail.com</a>
 */
public class DjatokaImageMigrator implements FormatConstants, IReferentMigrator {

    private static Logger LOGGER = LoggerFactory
            .getLogger(DjatokaImageMigrator.class);

    private List<String> processing = java.util.Collections
            .synchronizedList(new LinkedList<String>());

    private HashMap<String, String> formatMap;

    private File myPtRootDir;

    /**
     * Constructor. Initialized formatMap with common extension suffixes
     */
    public DjatokaImageMigrator() {
        formatMap = new HashMap<String, String>();
        formatMap.put(FORMAT_ID_JPEG, FORMAT_MIMEYPE_JPEG);
        formatMap.put(FORMAT_ID_JP2, FORMAT_MIMEYPE_JP2);
        formatMap.put(FORMAT_ID_PNG, FORMAT_MIMEYPE_PNG);
        formatMap.put(FORMAT_ID_PNM, FORMAT_MIMEYPE_PNM);
        formatMap.put(FORMAT_ID_TIFF, FORMAT_MIMEYPE_TIFF);
        formatMap.put(FORMAT_ID_GIF, FORMAT_MIMEYPE_GIF);
        // Additional Extensions
        formatMap.put(FORMAT_ID_JPG, FORMAT_MIMEYPE_JPEG);
        formatMap.put(FORMAT_ID_TIF, FORMAT_MIMEYPE_TIFF);
        // Additional JPEG 2000 Extensions
        formatMap.put(FORMAT_ID_J2C, FORMAT_MIMEYPE_JP2);
        formatMap.put(FORMAT_ID_JPC, FORMAT_MIMEYPE_JP2);
        formatMap.put(FORMAT_ID_J2K, FORMAT_MIMEYPE_JP2);
        formatMap.put(FORMAT_ID_JPF, FORMAT_MIMEYPE_JPX);
        formatMap.put(FORMAT_ID_JPX, FORMAT_MIMEYPE_JPX);
        formatMap.put(FORMAT_ID_JPM, FORMAT_MIMEYPE_JPM);
    }

    public void setPairtreeRoot(String aPtRootPath) {
        myPtRootDir = new File(aPtRootPath);
    }

    public String getPairtreeRoot() {
        return myPtRootDir.getAbsolutePath();
    }

    public boolean hasPairtreeRoot() {
        return myPtRootDir != null;
    }

    /**
     * Returns a delete on exit File object for a provide URI
     * 
     * @param aReferent the identifier for the remote file
     * @param aURI the URI of an image to be downloaded and compressed as JP2
     * @return File object of JP2 compressed image
     * @throws DjatokaException
     */
    public File convert(String aReferent, URI aURI) throws DjatokaException {
        File file = null;

        processing.add(aReferent);

        try {
            URL url = aURI.toURL();
            boolean isJp2 = aReferent.equals(url.toString()) ? false : true;

            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Processing remote {}: {}", isJp2 ? "JP2" : "URI",
                        url);
            }

            if (LOGGER.isDebugEnabled() && !isJp2) {
                LOGGER.debug("{} != {}", aReferent, aURI.toURL().toString());
            }

            // Obtain remote resource
            InputStream source = IOUtils.getInputStream(url);

            // If we know it's JP2 at this point, it's because it's been passed
            // in as one of our parsable URLs.
            if (isJp2 && myPtRootDir != null) {
                PairtreeRoot pairtree = new PairtreeRoot(myPtRootDir);
                PairtreeObject dir = pairtree.getObject(aReferent);
                String filename = PairtreeUtils.encodeID(aReferent);
                FileOutputStream destination;
                boolean result;

                file = new File(dir, filename);
                destination = new FileOutputStream(file);

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Remote stream ({}) is{}accessible", url,
                            source.available() > 0 ? " " : " not ");
                }

                // FIXME: Islandora special sauce to work around weirdness
                if (source.available() == 0) {
                    source = IOUtils.getInputStream(url);
                }
                
                result = IOUtils.copyStream(source, destination);

                if (LOGGER.isDebugEnabled() && source.available() > 0 && result) {
                    LOGGER.debug("Stored retrieved JP2 into Pairtree FS: {}",
                            file.getAbsolutePath());
                }

                source.close();
                destination.close();

                // Clean up the file stub of unsuccessful copies
                if (file.length() == 0) {
                    file.delete();
                }
            } else {
                int extIndex = url.toString().lastIndexOf(".") + 1;
                String ext = url.toString().substring(extIndex).toLowerCase();
                int hash = aURI.hashCode();

                if (ext.equals(FORMAT_ID_TIF) || ext.equals(FORMAT_ID_TIFF)) {
                    ext = "." + FORMAT_ID_TIF;
                    file = File.createTempFile("convert" + hash, ext);
                } else if (formatMap.containsKey(ext) &&
                        (formatMap.get(ext).equals(FORMAT_MIMEYPE_JP2) || formatMap
                                .get(ext).equals(FORMAT_MIMEYPE_JPX))) {
                    file = File.createTempFile("cache" + hash, "." + ext);
                    isJp2 = true;
                } else {
                    if (source.markSupported()) {
                        source.mark(15);
                    }

                    if (ImageProcessingUtils.checkIfJp2(source)) {
                        ext = "." + FORMAT_ID_JP2;
                        file = File.createTempFile("cache" + hash, ext);
                    }

                    if (source.markSupported()) {
                        source.reset();
                    } else { // close and reopen the stream
                        source.close();
                        source = IOUtils.getInputStream(url);
                    }
                }

                if (file == null) {
                    file = File.createTempFile("convert" + hash, ".img");
                }

                file.deleteOnExit();

                FileOutputStream destination = new FileOutputStream(file);
                IOUtils.copyStream(source, destination);

                // Process Image
                if (!isJp2) {
                    file = processImage(file, aURI);
                }

                // Clean-up
                source.close();
                destination.close();
            }

            return file;
        } catch (Exception details) {
            throw new DjatokaException(details.getMessage(), details);
        } finally {
            if (processing.contains(aReferent)) {
                processing.remove(aReferent);
            }
        }
    }

    /**
     * Returns a delete on exit File object for a provide URI
     * 
     * @param img File object on local image to be compressed
     * @param uri the URI of an image to be compressed as JP2
     * @return File object of JP2 compressed image
     * @throws DjatokaException
     */
    public File processImage(File img, URI uri) throws DjatokaException {
        String imgPath = img.getAbsolutePath();
        String fmt =
                formatMap.get(imgPath.substring(imgPath.lastIndexOf('.') + 1)
                        .toLowerCase());
        try {
            if (fmt == null || !ImageProcessingUtils.isJp2Type(fmt)) {
                ICompress jp2 = new KduCompressExe();
                File jp2Local =
                        File.createTempFile("cache" + uri.hashCode() + "-",
                                ".jp2");
                jp2Local.delete();
                jp2.compressImage(img.getAbsolutePath(), jp2Local
                        .getAbsolutePath(), new DjatokaEncodeParam());
                img.delete();
                img = jp2Local;
            } else {
                try {
                    IExtract ex = new KduExtractExe();
                    ex.getMetadata(new ImageRecord(uri.toString(), img
                            .getAbsolutePath()));
                } catch (DjatokaException e) {
                    throw new DjatokaException("Unknown JP2/JPX file format");
                }
            }
        } catch (Exception e) {
            throw new DjatokaException(e.getMessage(), e);
        }
        return img;
    }

    /**
     * Return a unmodifiable list of images currently being processed. Images
     * are removed once complete.
     * 
     * @return list of images being processed
     */
    public List<String> getProcessingList() {
        return processing;
    }

    /**
     * Returns map of format extension (e.g. jpg) to mime-type mappings (e.g.
     * image/jpeg)
     * 
     * @return format extension to mime-type mappings
     */
    public HashMap<String, String> getFormatMap() {
        return formatMap;
    }

    /**
     * Sets map of format extension (e.g. jpg) to mime-type mappings (e.g.
     * image/jpeg)
     * 
     * @param formatMap extension to mime-type mappings
     */
    public void setFormatMap(HashMap<String, String> formatMap) {
        this.formatMap = formatMap;
    }

}
