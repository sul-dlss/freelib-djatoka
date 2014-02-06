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

package gov.lanl.adore.djatoka.kdu.jni;

import kdu_jni.KduException;
import kdu_jni.Kdu_compressed_source_nonnative;
import kdu_jni.Kdu_global;

public class KduCompressedSource extends Kdu_compressed_source_nonnative {

    private byte[] b;

    int offset = 0;

    /**
     * Creates a compressed source from the supplied byte array.
     * 
     * @param b A byte array
     */
    public KduCompressedSource(byte[] b) {
        this.b = b;
    }

    /**
     * Gets the capabilities of the compressed source.
     */
    public int Get_capabilities() {
        return Kdu_global.KDU_SOURCE_CAP_SEQUENTIAL;
    }

    /**
     * Reads bytes from the compressed source.
     * 
     * @param num_bytes The number of bytes to read
     */
    public int Post_read(int num_bytes) {
        try {
            Push_data(b, offset, num_bytes);
            offset = offset + num_bytes;
        } catch (KduException e) {
            e.printStackTrace();
        }
        return num_bytes;
    }

    /**
     * Seeks ahead in the compressed source.
     * 
     * @param offset An offset to seek
     */
    public boolean Seek(long offset) {
        if (offset > -1 && offset <= b.length) {
            this.offset = (int) offset;
            return true;
        }
        return false;
    }

    /**
     * Gets the current offset position.
     */
    public long Get_pos() {
        return offset;
    }

    /**
     * Closes the compressed source.
     */
    public void close() {
        this.b = null;
    }

}
