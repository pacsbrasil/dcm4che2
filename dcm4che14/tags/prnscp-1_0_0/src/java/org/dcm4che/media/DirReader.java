/* $Id$ */
/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2001,2002 by TIANI MEDGRAPH AG <gunter.zeilinger@tiani.com>*
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

package org.dcm4che.media;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmValueException;

import java.io.File;
import java.io.IOException;
import javax.imageio.stream.ImageInputStream;


/**
 *
 * @author  gunter zeilinger
 * @version 1.0.0
 */
public interface DirReader {
    // Constants -----------------------------------------------------
    public static final int CONSISTENCY   = 0x0000;
    public static final int INCONSISTENCY = 0xFFFF;
        
    // Public --------------------------------------------------------
    public Dataset getFileSetInfo();

    public File getRefFile(File root, String[] fileIDs);

    public File getRefFile(String[] fileIDs);

    public File getDescriptorFile(File root) throws DcmValueException;

    public File getDescriptorFile() throws DcmValueException;

    public boolean isEmpty(boolean onlyInUse) throws IOException;

    public DirRecord getFirstRecord(boolean onlyInUse) throws IOException;

    public void close() throws IOException;
    
}//end interface DirReader
