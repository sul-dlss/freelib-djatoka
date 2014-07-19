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

package gov.lanl.adore.djatoka;

import java.io.File;
import java.util.ArrayList;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.lanl.adore.djatoka.kdu.KduCompressExe;
import gov.lanl.adore.djatoka.util.IOUtils;
import gov.lanl.adore.djatoka.util.SourceImageFileFilter;

/**
 * Compression Application
 *
 * @author Ryan Chute
 */
public class DjatokaCompress {

    private static Logger LOGGER = LoggerFactory.getLogger(DjatokaCompress.class);

    private DjatokaCompress() {
    }

    /**
     * Uses apache commons cli to parse input args. Passes parsed parameters to ICompress implementation.
     *
     * @param args command line parameters to defined input,output,etc.
     */
    public static void main(final String[] args) {
        final CommandLineParser parser = new PosixParser();
        final Options options = new Options();

        options.addOption("i", "input", true, "Filepath of the input file or dir.");
        options.addOption("o", "output", true, "Filepath of the output file or dir.");
        options.addOption("r", "rate", true, "Absolute Compression Ratio");
        // options.addOption("s", "slope", true,
        // "Used to generate relative compression ratio based on content characteristics.");
        options.addOption("y", "Clayers", true, "Number of quality levels.");
        options.addOption("l", "Clevels", true, "Number of DWT levels (reolution levels).");
        options.addOption("v", "Creversible", true, "Use Reversible Wavelet");
        options.addOption("c", "Cprecincts", true, "Precinct dimensions");
        options.addOption("p", "props", true, "Compression Properties File");
        options.addOption("d", "Corder", true, "Progression order");
        options.addOption("g", "ORGgen_plt", true, "Enables insertion of packet length information in the header");
        options.addOption("t", "ORGtparts", true, "Division of each tile's packets into tile-parts");
        options.addOption("b", "Cblk", true, "Codeblock Size");
        options.addOption("a", "AltImpl", true, "Alternate ICompress Implemenation");
        options.addOption("j", "jp2_profile", true, "Supported JP2 Color Space Profile");

        try {
            if (args.length == 0) {
                final HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("gov.lanl.adore.djatoka.DjatokaCompress", options);
                System.exit(0);
            }

            // parse the command line arguments
            final CommandLine line = parser.parse(options, args);
            final String input = line.getOptionValue("i");
            final String propsFile = line.getOptionValue("p");
            final String rate = line.getOptionValue("r");
            final String slope = line.getOptionValue("s");
            final String Clayers = line.getOptionValue("y");
            final String Clevels = line.getOptionValue("l");
            final String Creversible = line.getOptionValue("v");
            final String Cprecincts = line.getOptionValue("c");
            final String Corder = line.getOptionValue("d");
            final String ORGgen_plt = line.getOptionValue("g");
            final String Cblk = line.getOptionValue("b");
            final String alt = line.getOptionValue("a");
            final String jp2ColorSpace = line.getOptionValue("j");

            String output = line.getOptionValue("o");
            DjatokaEncodeParam p;

            if (propsFile != null) {
                final Properties props = IOUtils.loadConfigByPath(propsFile);

                p = new DjatokaEncodeParam(props);
            } else {
                p = new DjatokaEncodeParam();
            }

            if (rate != null) {
                p.setRate(rate);
            }

            if (slope != null) {
                p.setSlope(slope);
            }

            if (Clayers != null) {
                p.setLayers(Integer.parseInt(Clayers));
            }

            if (Clevels != null) {
                p.setLevels(Integer.parseInt(Clevels));
            }

            if (Creversible != null) {
                p.setUseReversible(Boolean.parseBoolean(Creversible));
            }

            if (Cprecincts != null) {
                p.setPrecincts(Cprecincts);
            }

            if (Corder != null) {
                p.setProgressionOrder(Corder);
            }

            if (ORGgen_plt != null) {
                p.setInsertPLT(Boolean.parseBoolean(ORGgen_plt));
            }

            if (Cblk != null) {
                p.setCodeBlockSize(Cblk);
            }

            if (jp2ColorSpace != null) {
                p.setJP2ColorSpace(jp2ColorSpace);
            }

            ICompress jp2 = new KduCompressExe();

            if (alt != null) {
                jp2 = (ICompress) Class.forName(alt).newInstance();
            }

            if (new File(input).isDirectory() && new File(output).isDirectory()) {
                final ArrayList<File> files = IOUtils.getFileList(input, new SourceImageFileFilter(), false);

                for (final File f : files) {
                    final long x = System.currentTimeMillis();
                    final String name = f.getName().substring(0, f.getName().indexOf("."));
                    final File outFile = new File(output, name + ".jp2");

                    compress(jp2, f.getAbsolutePath(), outFile.getAbsolutePath(), p);
                    report(f.getAbsolutePath(), x);
                }
            } else {
                final long x = System.currentTimeMillis();
                final File f = new File(input);
                final String name = f.getName().substring(0, f.getName().indexOf("."));

                if (output == null) {
                    output = name + ".jp2";
                }

                if (new File(output).isDirectory()) {
                    output = name + ".jp2";
                }

                compress(jp2, input, output, p);
                report(input, x);
            }
        } catch (final ParseException e) {
            LOGGER.error("Parse exception:" + e.getMessage(), e);
        } catch (final DjatokaException e) {
            LOGGER.error("djatoka Compression exception:" + e.getMessage(), e);
        } catch (final InstantiationException e) {
            LOGGER.error("Unable to initialize alternate implemenation:" + e.getMessage(), e);
        } catch (final Exception e) {
            LOGGER.error("An exception occured:" + e.getMessage(), e);
        }
    }

    /**
     * Print time, in seconds, to process resource
     *
     * @param id Identifier or File Path to indicate processing resource
     * @param x System time in milliseconds when resource processing started
     */
    public static void report(final String id, final long x) {
        LOGGER.info("Compression Time: " + (double) (System.currentTimeMillis() - x) / 1000 + " seconds for " + id);
    }

    /**
     * Simple compress wrapper to catch exceptions, useful when
     *
     * @param jp2
     * @param input
     * @param output
     * @param p
     */
    public static void compress(final ICompress jp2, final String input, final String output,
            final DjatokaEncodeParam p) throws DjatokaException {
        jp2.compressImage(input, output, p);
    }
}
