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
 * Java(TM), available at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Agfa HealthCare.
 * Portions created by the Initial Developer are Copyright (C) 2006-2008
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See listed authors below.
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

package org.dcm4chee.dashboard.mbean;

import java.io.File;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.dcm4chee.dashboard.model.ReportModel;
import org.dcm4chee.dashboard.model.SystemPropertyModel;
import org.jboss.mx.util.MBeanServerLocator;

/**
 * @author Robert David <robert.david@agfa.com>
 * @version $Revision$ $Date$
 * @since 25.11.2009
 */
public class DashboardDelegator {

    private final MBeanServer server = MBeanServerLocator.locate();
    private ObjectName objectName;
    
    public DashboardDelegator(String objectName) throws MalformedObjectNameException, NullPointerException {
        this.objectName = new ObjectName(objectName);
    }

    public String[] listAllFileSystemGroups() throws MalformedObjectNameException, NullPointerException, InstanceNotFoundException, ReflectionException, MBeanException {
        return (String[]) this.server.invoke(
                        this.objectName, 
                        "listAllFileSystemGroups", null, null);
    }
    
    public File[] listFileSystemsOfGroup(String groupname) throws InstanceNotFoundException, MalformedObjectNameException, ReflectionException, MBeanException, NullPointerException {
        return (File[]) server.invoke(
                        this.objectName,
                        "listFileSystemsOfGroup",
                        new Object[] { groupname },
                        new String[] { "java.lang.String" });
    }
    
    public long getMinimumFreeDiskSpaceOfGroup(String groupname) throws InstanceNotFoundException, MalformedObjectNameException, ReflectionException, MBeanException, NullPointerException, AttributeNotFoundException {
        return ((Long) server.invoke(
                        this.objectName,
                        "getMinimumFreeDiskSpaceOfGroup",
                        new Object[] { groupname },
                        new String[] { "java.lang.String" }))
                        .longValue();
    }
    
    public SystemPropertyModel[] getSystemProperties() throws InstanceNotFoundException, MalformedObjectNameException, ReflectionException, MBeanException, NullPointerException {
        return (SystemPropertyModel[]) server.invoke(
                        this.objectName,
                        "getSystemProperties", null, null);
    }
    
    public ReportModel[] listAllReports() throws InstanceNotFoundException, MalformedObjectNameException, ReflectionException, MBeanException, NullPointerException {
        return (ReportModel[]) server.invoke(
                        this.objectName,
                        "listAllReports", null, null);
    }
    
    public ReportModel getReport(String uuid) throws InstanceNotFoundException, MalformedObjectNameException, ReflectionException, MBeanException, NullPointerException {
        return (ReportModel) server.invoke(
                        this.objectName,
                        "getReport", 
                        new Object[] { uuid },
                        new String[] { "java.lang.String" });
    }

    public void createReport(ReportModel report) throws InstanceNotFoundException, MalformedObjectNameException, ReflectionException, MBeanException, NullPointerException {
                        server.invoke(
                        this.objectName,
                        "createReport", 
                        new Object[] { report }, 
                        new String[] { "org.dcm4chee.dashboard.model.ReportModel" });
    }
    
    public void updateReport(ReportModel report) throws InstanceNotFoundException, MalformedObjectNameException, ReflectionException, MBeanException, NullPointerException {
                        server.invoke(
                        this.objectName,
                        "updateReport", 
                        new Object[] { report }, 
                        new String[] { "org.dcm4chee.dashboard.model.ReportModel" });       
    }
    
    public void deleteReport(ReportModel report) throws InstanceNotFoundException, MalformedObjectNameException, ReflectionException, MBeanException, NullPointerException {
                        server.invoke(
                        this.objectName,
                        "deleteReport", 
                        new Object[] { report }, 
                        new String[] { "org.dcm4chee.dashboard.model.ReportModel" });       
    }
}
