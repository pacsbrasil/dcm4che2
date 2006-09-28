/*
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
 */
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

