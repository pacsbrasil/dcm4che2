/*
 * $Id$ Copyright (c) 2002,2003 by TIANI MEDGRAPH AG
 * 
 * This file is part of dcm4che.
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.dcm4chex.service;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.dcm4chex.config.DAOFactory;
import org.dcm4chex.config.DeviceDAO;
import org.dcm4chex.config.DeviceInfo;
import org.jboss.system.ServiceMBeanSupport;

/**
 * @jmx.mbean
 *  extends="org.jboss.system.ServiceMBean"
 * 
 * @author gunter.zeilinger@tiani.com
 * @version $Date$
 * @since 22.10.2003
 */
public class DeviceConfigService
    extends ServiceMBeanSupport
    implements org.dcm4chex.service.DeviceConfigServiceMBean {

    private String ldapURL = "ldap://localhost:389/dc=tiani,dc=com";
    private DAOFactory daoFactory;
	private DeviceDAO deviceDAO;
	private DeviceInfo deviceInfo;
    private String deviceName;
 
    /**
	 * @jmx.managed-attribute
	 */
    public String getLdapURL() {
        return ldapURL;
    }

    /**
	 * @jmx.managed-attribute
	 */
    public void setLdapURL(String ldapURL) {
        this.ldapURL = ldapURL;
    }

	/**
	 * @jmx.managed-attribute
	 */
	 public DeviceInfo getDeviceInfo() {
		 return deviceInfo;
	 }

    protected ObjectName getObjectName(MBeanServer beanSrv, ObjectName name)
        throws MalformedObjectNameException {
        deviceName = name.getKeyProperty("device");
        if (deviceName == null) {
            throw new MalformedObjectNameException("Missing property device:" + name);
        }
        return name;
    }

     protected void startService() throws Exception {
		daoFactory = DAOFactory.getLdapDAOFactory(ldapURL);
		deviceDAO = daoFactory.getDeviceDAO();
		deviceInfo = deviceDAO.find(deviceName);
		deviceInfo.setInstalled(true);
		deviceDAO.commit(deviceInfo);
    }

     protected void stopService() throws Exception {
		deviceInfo.setInstalled(false);
		deviceDAO.commit(deviceInfo);
    }

}
