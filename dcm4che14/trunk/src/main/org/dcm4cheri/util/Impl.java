/* $Id$ */
/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2002 by TIANI MEDGRAPH AG <gunter.zeilinger@tiani.com>     *
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

package org.dcm4cheri.util;

/**
 * This class includes the static data of the DICOM implementation.
 *
 * @author  gunter zeilinger
 * @version 1.0
 */
public class Impl {

    /**
     * The DICOM UID prefix of the Tiani DICOM implementation.
     */
    public static final String CLASS_UID = "1.2.40.0.13.1.1";
    
    /**
     * Reflects the version name of Tiani DICOM implementation.
     */
    public static final String VERSION_NAME = "DCM4CHE01APR02";
    
}//end class Implementation
