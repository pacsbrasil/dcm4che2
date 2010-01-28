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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.UUID;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.dcm4chee.dashboard.model.ReportModel;
import org.dcm4chee.dashboard.model.SystemPropertyModel;
import org.jboss.system.ServiceMBeanSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Robert David <robert.david@agfa.com>
 * @version $Revision$ $Date$
 * @since 25.11.2009
 */
public class DashboardService extends ServiceMBeanSupport {

    private static Logger log = LoggerFactory.getLogger(DashboardService.class);
    
    private String[] jbossSystemTypesToQuery = new String[] {"Server", "ServerConfig", "ServerInfo"};
    private String newline = System.getProperty("line.separator");

    // data source separator is ,
    private String[] dataSourceList = new String[0];
    
    // group separator is ,
    private String[] groupList = new String[0];

    // property name separator is ,
    private String[] propertyNameList = new String[0];

    private String reportFilename = "";
    private String groupFilename = "";
    
    public void setDataSourceList(String dataSourceList) {
        this.dataSourceList = tokenize(dataSourceList);
    }

    public String getDataSourceList() {
        return arrayToString(this.dataSourceList);
    }

    public void setGroupList(String groupList) {
        this.groupList = tokenize(groupList);
    }

    public String getGroupList() {
        return arrayToString(this.groupList);
    }
    
    public void setPropertyNameList(String propertyNameList) {
        this.propertyNameList = tokenize(propertyNameList);
    }

    public String getPropertyNameList() {
        return arrayToString(this.propertyNameList);
    }
    
    public void setReportFilename(String reportFilename) {
        this.reportFilename = reportFilename;
    }

    public String getReportFilename() {
        return reportFilename;
    }

    public void setGroupFilename(String groupFilename) {
        this.groupFilename = groupFilename;
    }

    public String getGroupFilename() {
        return groupFilename;
    }

    public String[] listAllFileSystemGroups() throws MalformedObjectNameException, NullPointerException {
        return this.groupList;
    }
    
    public File[] listFileSystemsOfGroup(String groupname) throws InstanceNotFoundException, MalformedObjectNameException, ReflectionException, MBeanException, NullPointerException {
        return (File[]) this.server.invoke(
                        new ObjectName(groupname),
                        "listFileSystemDirectories", null, null);
    }
    
    public long getMinimumFreeDiskSpaceOfGroup(String groupname) throws InstanceNotFoundException, MalformedObjectNameException, ReflectionException, MBeanException, NullPointerException, AttributeNotFoundException {
        return ((Long) this.server.getAttribute(
                      new ObjectName(groupname),
                      "MinimumFreeDiskSpaceBytes")).longValue();
    }

    public long getExpectedDataVolumePerDay(String groupname) throws InstanceNotFoundException, MalformedObjectNameException, ReflectionException, MBeanException, NullPointerException, AttributeNotFoundException {
        return ((Long) this.server.getAttribute(
                      new ObjectName(groupname),
                      "ExpectedDataVolumePerDayBytes")).longValue();
    }

    public SystemPropertyModel[] getSystemProperties() throws InstanceNotFoundException, MalformedObjectNameException, ReflectionException, MBeanException, NullPointerException {

        SystemPropertyModel[] properties = new SystemPropertyModel[this.propertyNameList.length];
        for (int i = 0; i < this.propertyNameList.length; i++) {
            properties[i] = new SystemPropertyModel(
                            this.propertyNameList[i], 
                            (String) this.server.invoke(new ObjectName("jboss:name=SystemProperties,type=Service"),
                                                "get", 
                                                new Object[] {this.propertyNameList[i]}, new String[] {"java.lang.String"}));
            if (properties[i].getValue() == null) {
                for (String type : this.jbossSystemTypesToQuery) {
                    try {
                        properties[i].setValue((String) this.server.getAttribute(
                                new ObjectName("jboss.system:type=" + type), 
                                this.propertyNameList[i]));
                        break;
                    } catch (AttributeNotFoundException e) {}
                }
            }
        }
        return properties;
    }

    public ReportModel[] listAllReports() {
        try {
            List<ReportModel> reportList = new ArrayList<ReportModel>();
            
            if (new File(this.reportFilename).exists()) {            
                BufferedReader reader = new BufferedReader(new FileReader(this.reportFilename));
                String line = "";
                String all = "";
                while ((line = reader.readLine()) != null){
                    all += line + this.newline;
                }
                if (!all.equals("")) {
                    String[] attributes = all.split(";");
                    for (int i = 0; i < attributes.length; i += 7) {
                        if (attributes[i].equals(this.newline)) break;
                        reportList.add(new ReportModel(attributes[i].replace(this.newline, ""), attributes[i + 1], attributes[i + 2], attributes[i + 3], (attributes[i + 4].equals("") ? null : new Integer(attributes[i + 4])), Boolean.valueOf(attributes[i + 5]), attributes[i + 6]));
                    }
                }
            }
            return reportList.toArray(new ReportModel[0]);
        } catch (IOException e) {
            log.debug("Exception: ", e);
            return null;
        }
    }
    
    public void createReport(ReportModel report) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(this.reportFilename, true));
            writer.write((report.getUuid() == null ? UUID.randomUUID() : report.getUuid()) + ";" + report.getTitle() + ";" + (report.getDataSource() != null ? report.getDataSource() : "") + ";" + report.getStatement().replaceAll(";", "") + ";" + (report.getDiagram() == null ? "" : report.getDiagram()) + ";" + report.getTable() + ";" + report.getGroupUuid() + ";");
            writer.newLine();
            writer.close();
        } catch (IOException e) {
            log.debug("Exception: ", e);
        }        
    }
    
    public void updateReport(ReportModel report) {
        modifyReport(report, false);
    }
    
    public void deleteReport(ReportModel report) {
        modifyReport(report, true);
    }
    
    private void modifyReport(ReportModel report, boolean deleteLine) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(this.reportFilename));
            File reportFile = new File(this.reportFilename);            
            String tempFilename = reportFile.getAbsolutePath().substring(0, reportFile.getAbsolutePath().length() - reportFile.getName().length()) 
                                + UUID.randomUUID().toString();
            BufferedWriter writer = new BufferedWriter(new FileWriter(tempFilename, true));
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.replace(this.newline,"").startsWith(report.getUuid())) {
                    writer.write(line);
                    writer.newLine();
                } else {
                    int result = 0;
                    while (result < 7) { 
                        int start = line.indexOf(";");
                        while (start != -1) {
                            result++;
                            start = line.indexOf(";", start+1);
                        }
                        if (result < 7) line = reader.readLine();
                    }
                    if (!deleteLine) {
                        writer.write(report.getUuid() + ";" + report.getTitle() + ";" + (report.getDataSource() != null ? report.getDataSource() : "") + ";" + report.getStatement().replaceAll(";", "") + ";" + (report.getDiagram() == null ? "" : report.getDiagram()) + ";" + report.getTable() + ";" + report.getGroupUuid() + ";");
                        writer.newLine();
                    }
                }
            }
            reader.close();
            writer.close();
            reportFile.delete();
            new File(tempFilename).renameTo(reportFile);
        } catch (IOException e) {
            log.debug("Exception: ", e);
        }
    }
    
    public ReportModel[] listAllReportGroups() {
        try {
            List<ReportModel> groupList = new ArrayList<ReportModel>();

            if (new File(this.groupFilename).exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(this.groupFilename));
                String line = "";
                String all = "";
                while ((line = reader.readLine()) != null){
                    all += line + this.newline;
                }
                if (!all.equals("")) {
                    String[] attributes = all.split(";");
                    for (int i = 0; i < attributes.length; i += 2) {
                        if (attributes[i].equals(this.newline)) break;
                        groupList.add(new ReportModel(attributes[i].replace(this.newline, ""), attributes[i + 1], null, null, null, false, null));
                    }
                }
            }
            return groupList.toArray(new ReportModel[0]);
        } catch (IOException e) {
            log.debug("Exception: ", e);
            return null;
        }
    }

    public void createGroup(ReportModel group) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(this.groupFilename, true));
            writer.write((group.getUuid() == null ? UUID.randomUUID() : group.getUuid()) + ";" + group.getTitle() + ";");
            writer.newLine();
            writer.close();
        } catch (IOException e) {
            log.debug("Exception: ", e);
        }        
    }

    public void deleteGroup(ReportModel group) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(this.reportFilename));
            File reportFile = new File(this.reportFilename);            
            String tempFilename = reportFile.getAbsolutePath().substring(0, reportFile.getAbsolutePath().length() - reportFile.getName().length()) 
                                + UUID.randomUUID().toString();
            BufferedWriter writer = new BufferedWriter(new FileWriter(tempFilename, true));
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.replace(this.newline,"").endsWith(group.getUuid() + ";")) {
                    writer.write(line);
                    writer.newLine();
                }
            }
            reader.close();
            writer.close();
            reportFile.delete();
            new File(tempFilename).renameTo(reportFile);
            
            reader = new BufferedReader(new FileReader(this.groupFilename));
            File groupFile = new File(this.groupFilename);            
            tempFilename = groupFile.getAbsolutePath().substring(0, groupFile.getAbsolutePath().length() - groupFile.getName().length()) 
                                + UUID.randomUUID().toString();
            writer = new BufferedWriter(new FileWriter(tempFilename, true));
            while ((line = reader.readLine()) != null) {
                if (!line.replace(this.newline,"").startsWith(group.getUuid())) {
                    writer.write(line);
                    writer.newLine();
                }
            }
            reader.close();
            writer.close();
            groupFile.delete();
            new File(tempFilename).renameTo(groupFile);            
        } catch (IOException e) {
            log.debug("Exception: ", e);
        }
    }

    private String[] tokenize(String sourceString) {
        StringTokenizer st = new StringTokenizer(sourceString, newline);
        List<String> tokens = new ArrayList<String>();        
        while (st.hasMoreTokens()) {
            String token =  st.nextToken();
            if (token.endsWith("\t") || 
                token.endsWith("\r") ||
                token.endsWith("\n"))
                    token = token.substring(0, token.length() - 1);
            if (token.length() > 0) tokens.add(token);
        }
        return tokens.toArray(new String[0]);
    }
    
    private String arrayToString(String[] array) {
        String arrayString = "";
        for (String string : array) arrayString += (newline + string);
        return (arrayString.length() <= 0 ? arrayString : arrayString.substring(1)) + newline;
    }
}
