/*
 * Copyright (c) 2002,2003 by TIANI MEDGRAPH AG
 *
 * This file is part of dcm4che.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package org.dcm4chex.service;

import javax.management.ObjectName;

import org.dcm4che.dict.UIDs;
import org.dcm4che.net.AcceptorPolicy;
import org.dcm4che.net.DcmServiceRegistry;
import org.dcm4che.server.DcmHandler;
import org.jboss.system.ServiceMBeanSupport;

/**
 * @jmx.mbean
 *  extends="org.jboss.system.ServiceMBean"
 * 
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision$
 * @since 31.08.2003
 */
public class FindScpService
    extends ServiceMBeanSupport
    implements org.dcm4chex.service.FindScpServiceMBean
{
    private final static String[] NATIVE_TS = {
            UIDs.ExplicitVRLittleEndian,
            UIDs.ImplicitVRLittleEndian
            };
            
    private ObjectName dcmServerName;
    private DcmHandler dcmHandler;
    private FindScp scp = new FindScp(this);

    /**
     * @jmx.managed-attribute
     */
    public ObjectName getDcmServerName() {
        return dcmServerName;
    }

    /**
     * @jmx.managed-attribute
     */
    public void setDcmServerName(ObjectName dcmServerName) {
        this.dcmServerName = dcmServerName;
    }

    /**
     * @jmx.managed-attribute
     */
    public String getDatasource() {
        return scp.getDsJndiName();
    }

    /**
     * @jmx.managed-attribute
     */
    public void setDatasource(String datasource) {
        scp.setDsJndiName(datasource);
    }
    
    protected void startService() throws Exception {
        dcmHandler =
                (DcmHandler) server.getAttribute(dcmServerName, "DcmHandler");
        bindDcmServices();
        updatePolicy(NATIVE_TS);
    }

    protected void stopService() throws Exception {
        updatePolicy(null);
        unbindDcmServices();
        dcmHandler = null;
    }

    private void bindDcmServices()
    {
        DcmServiceRegistry services = dcmHandler.getDcmServiceRegistry();
        services.bind(UIDs.PatientRootQueryRetrieveInformationModelFIND, scp);
        services.bind(UIDs.StudyRootQueryRetrieveInformationModelFIND, scp);
        services.bind(UIDs.PatientStudyOnlyQueryRetrieveInformationModelFIND, scp);
    }

    private void unbindDcmServices()
    {
        DcmServiceRegistry services = dcmHandler.getDcmServiceRegistry();
        services.bind(UIDs.PatientRootQueryRetrieveInformationModelFIND, null);
        services.bind(UIDs.StudyRootQueryRetrieveInformationModelFIND, null);
        services.bind(UIDs.PatientStudyOnlyQueryRetrieveInformationModelFIND, null);
    }

    private void updatePolicy(String[] tsuids)
    {
        AcceptorPolicy policy = dcmHandler.getAcceptorPolicy();
        policy.putPresContext(UIDs.PatientRootQueryRetrieveInformationModelFIND, tsuids);
        policy.putPresContext(UIDs.StudyRootQueryRetrieveInformationModelFIND, tsuids);
        policy.putPresContext(UIDs.PatientStudyOnlyQueryRetrieveInformationModelFIND, tsuids);
    }

}
