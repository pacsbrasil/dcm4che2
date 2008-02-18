/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * TIANI Medgraph AG.
 * Portions created by the Initial Developer are Copyright (C) 2002-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunter.zeilinger@tiani.com>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

package com.tiani.prnscp.scp;

import javax.management.ObjectName;

import org.dcm4che.net.AcceptorPolicy;
import org.jboss.system.ServiceMBean;

/**
 *  <description>
 *
 * @author     <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @since      April 5, 2003
 * @created    November 3, 2002
 * @version    $Revision$
 */
public interface PrintScpServiceMBean extends ServiceMBean
{

    String SPOOL_SESSION_LOCK_SUFFIX = "LOCK";

    String SPOOL_HARDCOPY_DIR_SUFFIX = "HC";

    String SPOOL_JOB_DIR_SUFFIX = "JOBS";


    /**
     *  Gets the auditLoggerName attribute of the PrintScpServiceMBean object
     *
     * @return    The auditLoggerName value
     */
    public ObjectName getAuditLoggerName();


    /**
     *  Sets the auditLoggerName attribute of the PrintScpServiceMBean object
     *
     * @param  auditLogName  The new auditLoggerName value
     */
    public void setAuditLoggerName(ObjectName auditLogName);


    /**
     *  Gets the dcmServerName attribute of the PrintScpServiceMBean object
     *
     * @return    The dcmServerName value
     */
    public ObjectName getDcmServerName();


    /**
     *  Sets the dcmServerName attribute of the PrintScpServiceMBean object
     *
     * @param  dcmServerName  The new dcmServerName value
     */
    public void setDcmServerName(ObjectName dcmServerName);


    /**
     *  Gets the spoolDirectory attribute of the PrintScpServiceMBean object
     *
     * @return    The spoolDirectory value
     */
    public String getSpoolDirectory();


    /**
     *  Sets the spoolDirectory attribute of the PrintScpServiceMBean object
     *
     * @param  spoolDirectory  The new spoolDirectory value
     */
    public void setSpoolDirectory(String spoolDirectory);


    /**
     *  Gets the keepSpoolFiles attribute of the PrintScpServiceMBean object
     *
     * @return    The keepSpoolFiles value
     */
    public boolean isKeepSpoolFiles();


    /**
     *  Sets the keepSpoolFiles attribute of the PrintScpServiceMBean object
     *
     * @param  keepSpoolFiles  The new keepSpoolFiles value
     */
    public void setKeepSpoolFiles(boolean keepSpoolFiles);


    /**
     *  Gets the maskWarningAsSuccess attribute of the PrintScpServiceMBean object
     *
     * @return    The maskWarningAsSuccess value
     */
    public boolean isMaskWarningAsSuccess();


    /**
     *  Sets the maskWarningAsSuccess attribute of the PrintScpServiceMBean object
     *
     * @param  maskWarningAsSuccess  The new maskWarningAsSuccess value
     */
    public void setMaskWarningAsSuccess(boolean maskWarningAsSuccess);


    /**
     *  Gets the auditCreateSession attribute of the PrintScpServiceMBean object
     *
     * @return    The auditCreateSession value
     */
    public boolean isAuditCreateSession();


    /**
     *  Sets the auditCreateSession attribute of the PrintScpServiceMBean object
     *
     * @param  auditCreateSession  The new auditCreateSession value
     */
    public void setAuditCreateSession(boolean auditCreateSession);


    /**
     *  Gets the auditCreateFilmBox attribute of the PrintScpServiceMBean object
     *
     * @return    The auditCreateFilmBox value
     */
    public boolean isAuditCreateFilmBox();


    /**
     *  Sets the auditCreateFilmBox attribute of the PrintScpServiceMBean object
     *
     * @param  auditCreateFilmBox  The new auditCreateFilmBox value
     */
    public void setAuditCreateFilmBox(boolean auditCreateFilmBox);


    /**
     *  Gets the auditPrintJob attribute of the PrintScpServiceMBean object
     *
     * @return    The auditPrintJob value
     */
    public boolean isAuditPrintJob();


    /**
     *  Sets the auditPrintJob attribute of the PrintScpServiceMBean object
     *
     * @param  auditPrintJob  The new auditPrintJob value
     */
    public void setAuditPrintJob(boolean auditPrintJob);


    /**
     *  Gets the numCreatedJobs attribute of the PrintScpServiceMBean object
     *
     * @return    The numCreatedJobs value
     */
    public int getNumCreatedJobs();


    /**
     *  Gets the numStoredPrints attribute of the PrintScpServiceMBean object
     *
     * @return    The numStoredPrints value
     */
    public int getNumStoredPrints();


    /**
     *  Description of the Method
     *
     * @param  aet     Description of the Parameter
     * @param  policy  Description of the Parameter
     */
    public void putAcceptorPolicy(String aet, AcceptorPolicy policy);


    /**
     *  Description of the Method
     *
     * @param  job  Description of the Parameter
     */
    public void onJobStartPrinting(String job);


    /**
     *  Description of the Method
     *
     * @param  job  Description of the Parameter
     */
    public void onJobFailed(String job);


    /**
     *  Description of the Method
     *
     * @param  job  Description of the Parameter
     */
    public void onJobDone(String job);

}

