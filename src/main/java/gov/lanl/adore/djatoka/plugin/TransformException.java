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

package gov.lanl.adore.djatoka.plugin;

import gov.lanl.adore.djatoka.DjatokaException;

/**
 * @author Ryan Chute
 */
public class TransformException extends DjatokaException {

    private static final long serialVersionUID = -3452607688194996388L;

    /**
     * Creates a transform exception using the supplied message.
     *
     * @param message The exception message
     */
    public TransformException(final String message) {
        super(message);
    }

    /**
     * Creates a transform exception from the supplied Throwable using the supplied message.
     *
     * @param message The exception message
     * @param cause The underlying exception
     */
    public TransformException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a tranform exception from the supplied Throwable.
     *
     * @param cause The underlying cause of the exception
     */
    public TransformException(final Throwable cause) {
        super(cause);
    }

}
