/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2002 by TIANI MEDGRAPH AG                                  *
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

package org.dcm4che.net;

import java.io.IOException;
import java.util.List;

/**
 * Simplified and specialized version of
 * EDU.oswego.cs.dl.util.concurrent.FutureResult
 * in Doug Lee's util.concurrent package.
 *
 * @see <related>
 * @author  <a href="http://g.oswego.edu/index.html">Doug Lee</a>
 * @author  <a href="mailto:gunter@tiani.com">Gunter Zeilinger</a>
 * @version $Revision$ $Date$
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>yyyymmdd author:</b>
 * <ul>
 * <li> explicit fix description (no line numbers but methods) go
 *            beyond the cvs commit message
 * </ul>
 */
public interface FutureRSP {

   void set(Dimse rsp);
   
   void setException(IOException ex);
   
   Dimse get() throws InterruptedException, IOException;
   
   List listPending(); 
   
   IOException getException();
   
   boolean isReady();
   
   Dimse peek();
}
