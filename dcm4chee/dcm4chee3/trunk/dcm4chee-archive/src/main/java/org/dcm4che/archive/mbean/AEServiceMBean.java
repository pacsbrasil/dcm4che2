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
 * Agfa-Gevaert Group.
 * Portions created by the Initial Developer are Copyright (C) 2003-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunterze@gmail.com>
 * Franz Willer <franz.willer@gwi-ag.com>
 * Damien Evans <damien.daddy@gmail.com>
 * Jeremy Vosters <jlvosters@gmail.coml>
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
package org.dcm4che.archive.mbean;

import java.net.InetAddress;
import java.rmi.RemoteException;
import java.util.List;

import javax.management.ObjectName;

import org.dcm4che.archive.entity.AE;

public interface AEServiceMBean {

    /**
     * @return Returns the echoServiceName.
     */
    public abstract ObjectName getEchoServiceName();

    /**
     * @param echoServiceName
     *            The echoServiceName to set.
     */
    public abstract void setEchoServiceName(ObjectName echoServiceName);

    public abstract ObjectName getAuditLoggerName();

    public abstract void setAuditLoggerName(ObjectName auditLogName);

    /**
     * @return Returns the autoConfig.
     */
    public abstract boolean isDontSaveIP();

    /**
     * @param dontSaveIP
     *            The dontSaveIP to set.
     */
    public abstract void setDontSaveIP(boolean dontSaveIP);

    /**
     * @return Returns the portNumbers.
     */
    public abstract String getPortNumbers();

    /**
     * @param portNumbers
     *            The portNumbers to set.
     */
    public abstract void setPortNumbers(String ports);

    public abstract String getAEs() throws Exception;

    public abstract List listAEs() throws Exception;

    public abstract AE getAE(String title) throws Exception;

    public abstract boolean updateAETitle(String prevAET, String newAET)
            throws Exception;

    public abstract AE getAE(String title, String host) throws RemoteException,
            Exception;

    public abstract AE getAE(String aet, InetAddress addr) throws Exception;

    /**
     * Adds or updates an AE Title.
     * 
     * @param pk
     *            The primary key of the AE title in the database if this is an
     *            update.
     * @param aet
     *            Application Entity Title
     * @param host
     *            Hostname or IP addr.
     * @param port
     *            port number
     * @param cipher
     *            String with cypher(s) to create a secure connection (seperated
     *            with ',') or null
     * @param issuer
     *            A string containing the issuer of the patient id used at this
     *            AE title.
     * @param desc
     *            A description string for this AE title.
     * @param checkHost
     *            Enable/disable checking if the host can be resolved.
     * 
     * @throws Exception
     */
    public abstract void updateAE(Long pk, String title, String host, int port,
            String cipher, String issuer, String desc, boolean checkHost)
            throws Exception;

    public abstract void addAE(String title, String host, int port,
            String cipher, String issuer, String desc, boolean checkHost)
            throws Exception;

    /**
     * Remove the specified AE titles from the database.
     * 
     * @param titles
     *            A delimited (space, comma, semi-colon, tab, new line) string
     *            containing the AE titles to remove.
     * @throws Exception
     */
    public abstract void removeAE(String titles) throws Exception;

}
