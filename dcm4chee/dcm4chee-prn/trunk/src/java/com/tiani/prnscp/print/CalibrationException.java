/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2002,2003 by TIANI MEDGRAPH AG                             *
 *                                                                           *
 *  This file is part of dcm4che.                                            *
 *                                                                           *
 *  This library is free software; you can redistribute it and/or modify it  *
 *  under the terms of the GNU Lesser General Public License as published    *
 *  by the Free Software Foundation; either version 2 of the License, or     *
 *  (at your option) any later version.                                      *
 *                                                                           *
 *  This library is distributed in the hope that it will be useful, but      *
 *  WITHOUT ANY WARRANTY; without even the implied warranty of               *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU        *
 *  Lesser General Public License for more details.                          *
 *                                                                           *
 *  You should have received a copy of the GNU Lesser General Public         *
 *  License along with this library; if not, write to the Free Software      *
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA  *
 *                                                                           *
 *****************************************************************************/

package com.tiani.prnscp.print;

/**
 * <description>
 *
 * @see <related>
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @version $Revision$
 * @since January 3, 2003
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>yyyymmdd author:</b>
 * <ul>
 * <li> explicit fix description (no line numbers but methods) go
 *            beyond the cvs commit message
 * </ul>
 */
public class CalibrationException extends java.lang.Exception {
   
   /**
    * Creates a new instance of <code>CalibrationException</code>
    * without detail message.
    */
   public CalibrationException() {
   }
   
   
   /**
    * Constructs an instance of <code>CalibrationException</code>
    * with the specified detail message.
    * @param msg the detail message.
    */
   public CalibrationException(String msg) {
      super(msg);
   }
   
   /**
    * Constructs an instance of <code>CalibrationException</code>
    * with the specified detail message and cause.
    *
    * @param msg the detail message.
    * @param cause the cause.
    */
   public CalibrationException(String msg, Throwable cause) {
      super(msg, cause);
   }
   
   /**
    * Constructs a new exception with the specified cause and a detail
    * message of <tt>(cause==null ? null : cause.toString())</tt>
    *
    * @param  cause the cause
    */
   public CalibrationException(Throwable cause) {
      super(cause);
   }
}
