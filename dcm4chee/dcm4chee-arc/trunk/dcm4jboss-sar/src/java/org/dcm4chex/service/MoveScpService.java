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
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.dcm4che.conf.ldap.LdapConfig;
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
public class MoveScpService
    extends ServiceMBeanSupport
    implements org.dcm4chex.service.MoveScpServiceMBean
{
    private final static String[] NATIVE_TS = {
            UIDs.ExplicitVRLittleEndian,
            UIDs.ImplicitVRLittleEndian
            };
                
    private ObjectName dcmServerName;
    private DcmHandler dcmHandler;
    private String dsJndiName;
    private DataSource datasource;
    private LdapConfig ldap = new LdapConfig();
    private MoveScp scp = new MoveScp(this);    

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
    public String getDsJndiName() {
        return dsJndiName;
    }

    /**
     * @jmx.managed-attribute
     */
    public void setDsJndiName(String dsJndiName) {
        this.dsJndiName = dsJndiName;
    }
        
    /**
     * @jmx.managed-attribute
     */
    public final String getLdapBaseDN() {
        return ldap.getBaseDN();
    }

    /**
     * @jmx.managed-attribute
     */
    public final void setLdapBaseDN(String baseDN) {
        ldap.setBaseDN(baseDN);
    }

    /**
     * @jmx.managed-attribute
     */
    public final String getLdapHost() {
        return ldap.getHost();
    }

    /**
     * @jmx.managed-attribute
     */
    public final void setLdapHost(String host) {
        ldap.setHost(host);
    }

    /**
     * @jmx.managed-attribute
     */
    public final String getLdapPort() {
        return ldap.getPort();
    }

    /**
     * @jmx.managed-attribute
     */
    public final void setLdapPort(String port) {
        ldap.setPort(port);
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
        services.bind(UIDs.PatientRootQueryRetrieveInformationModelMOVE, scp);
        services.bind(UIDs.StudyRootQueryRetrieveInformationModelMOVE, scp);
        services.bind(UIDs.PatientStudyOnlyQueryRetrieveInformationModelMOVE, scp);
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
        policy.putPresContext(UIDs.PatientRootQueryRetrieveInformationModelMOVE, tsuids);
        policy.putPresContext(UIDs.StudyRootQueryRetrieveInformationModelMOVE, tsuids);
        policy.putPresContext(UIDs.PatientStudyOnlyQueryRetrieveInformationModelMOVE, tsuids);
    }

    public LdapConfig getLdapConfig() {
        return ldap;
    }

    public DataSource getDataSource() throws NamingException {
        if (datasource == null) {
            Context jndiCtx = new InitialContext();
            try {
                datasource = (DataSource) jndiCtx.lookup(dsJndiName);
            } finally {
                try {
                    jndiCtx.close();
                } catch (NamingException ignore) {}
            }
        }
        return datasource;
    }

}
