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

package com.tiani.prnscp.scp;

import org.dcm4che.net.AcceptorPolicy;
import org.jboss.system.ServiceMBean;
import javax.management.ObjectName;

/**
 * <description>
 *
 * @see <related>
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @version $Revision$
 * @since November 3, 2002
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>yyyymmdd author:</b>
 * <ul>
 * <li> explicit fix description (no line numbers but methods) go
 *            beyond the cvs commit message
 * </ul>
 */
public interface PrintScpServiceMBean extends ServiceMBean {

   String SPOOL_SESSION_LOCK_SUFFIX = "LOCK";
   
   String SPOOL_HARDCOPY_DIR_SUFFIX = "HC";

   String SPOOL_JOB_DIR_SUFFIX = "JOBS";
   
   /** Getter for property dcmServer.
    * @return Value of property dcmServer.
    */
   ObjectName getDcmServer();
   
   /** Setter for property dcmServer.
    * @param dcmServer New value of property dcmServer.
    */
   void setDcmServer(ObjectName dcmServer);
   
   /** Getter for property spoolDirectory.
    * @return Value of property spoolDirectory.
    */
   String getSpoolDirectory();
   
   /** Setter for property spoolDirectory.
    * @param spoolDirPath New value of property spoolDirectory.
    */
   void setSpoolDirectory(String spoolDirectory);
   
   /** Getter for property keepSpoolFiles.
    * @return Value of property keepSpoolFiles.
    */
   public boolean isKeepSpoolFiles();
   
   /** Setter for property keepSpoolFiles.
    * @param keepSpoolFiles New value of property keepSpoolFiles.
    */
   public void setKeepSpoolFiles(boolean keepSpoolFiles);
   
   /** Getter for property numCreatedJobs.
    * @return Value of property numCreatedJobs.
    */
   public int getNumCreatedJobs();
      
   /** Getter for property numStoredPrints.
    * @return Value of property numStoredPrints.
    */
   public int getNumStoredPrints();

   public String showLicense();
      
   public void putAcceptorPolicy(String aet, AcceptorPolicy policy);
   
   public void onJobStartPrinting(String job);
      
   public void onJobFailed(String job);

   public void onJobDone(String job);
}
