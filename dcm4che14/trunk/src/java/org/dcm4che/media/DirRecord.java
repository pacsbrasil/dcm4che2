/*$Id$*/
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

package org.dcm4che.media;

import org.dcm4che.data.Dataset;

import java.io.IOException;

/**
 *
 * @author  gunter zeilinger
 * @version 1.0.0
 */
public interface DirRecord {
    // Constants -----------------------------------------------------
    public static final int INACTIVE = 0x0000;
    public static final int IN_USE   = 0xFFFF;
       
    public String getType();
        
    public int getInUseFlag();
    
    public String[] getRefFileIDs();

    public String getRefSOPClassUID();

    public String getRefSOPInstanceUID();

    public String getRefSOPTransferSyntaxUID();

    public Dataset getDataset();
        
    public DirRecord getFirstChild(boolean onlyInUse) throws IOException;

    public DirRecord getNextSibling(boolean onlyInUse) throws IOException;
}
