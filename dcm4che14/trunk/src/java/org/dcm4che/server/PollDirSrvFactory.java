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

package org.dcm4che.server;

import org.dcm4che.Implementation;
import org.dcm4che.net.AcceptorPolicy;
import org.dcm4che.net.DcmServiceRegistry;

/**
 * <description>
 *
 * @see <related>
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
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
public abstract class PollDirSrvFactory {
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   
   // Static --------------------------------------------------------
   public static PollDirSrvFactory getInstance() {
      return (PollDirSrvFactory)Implementation.findFactory(
            "dcm4che.server.PollDirSrvFactory");
   }
      
   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------
   public abstract PollDirSrv newPollDirSrv(PollDirSrv.Handler handler);
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
}
