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

import gov.lanl.adore.djatoka.DjatokaException;

/**
 * General Resolver Exception Handler
 * 
 * @author Ryan Chute
 */
public class ResolverException extends DjatokaException {

    private static final long serialVersionUID = 6532963984240949392L;

    /**
     * Creates a resolver exception using the supplied message.
     * 
     * @param message The message of the exception
     */
    public ResolverException(String message) {
        super(message);
    }

    /**
     * Creates a resolver exception from the supplied Throwable using the
     * supplied message.
     * 
     * @param message The message of the exception
     * @param cause The underlying cause of the exception
     */
    public ResolverException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a resolver exception from the supplied Throwable.
     * 
     * @param cause The underlying cause
     */
    public ResolverException(Throwable cause) {
        super(cause);
    }
}
