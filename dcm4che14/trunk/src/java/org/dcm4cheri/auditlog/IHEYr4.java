/*                                                                           *
 *  Copyright (c) 2002,2003 by TIANI MEDGRAPH AG                             *
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
package org.dcm4cheri.auditlog;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.dcm4che.auditlog.MediaDescription;

import org.dcm4che.auditlog.InstancesAction;
import org.dcm4che.auditlog.Patient;
import org.dcm4che.auditlog.RemoteNode;
import org.dcm4che.auditlog.User;
import org.dcm4che.data.Dataset;

/**
 * @author     <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @since      August 27, 2002
 * @version    $Revision$ $Date$
 */
class IHEYr4
{

    /**
     *  Description of the Interface
     *
     * @author    gunter
     * @since     March 16, 2003
     */
    interface Message
    {
        void writeTo(StringBuffer sb);
    }

    // Constants -----------------------------------------------------
    private final static SimpleDateFormat TIMESTAMP =
            new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");

    // Variables -----------------------------------------------------
    private static String localHostName;

    private Message msg;
    private String host;
    private long millis;

    // Constructors --------------------------------------------------
    private IHEYr4(Message msg, String host, long millis)
    {
        this.msg = msg;
        this.host = host;
        this.millis = millis;
    }


    /**
     *  Description of the Method
     *
     * @param  rnode   Description of the Parameter
     * @param  action  Description of the Parameter
     * @param  host    Description of the Parameter
     * @param  millis  Description of the Parameter
     * @return         Description of the Return Value
     */
    public static IHEYr4 newInstancesStored(RemoteNode rnode,
            InstancesAction action,
            String host, long millis)
    {
        return new IHEYr4(
                new RnodeWithInstanceActionDescription("InstancesStored",
                rnode, action, "RemoteNode"), host, millis);
    }


    /**
     *  Description of the Method
     *
     * @param  rnode   Description of the Parameter
     * @param  action  Description of the Parameter
     * @param  host    Description of the Parameter
     * @param  millis  Description of the Parameter
     * @return         Description of the Return Value
     */
    public static IHEYr4 newBeginStoringInstances(RemoteNode rnode,
            InstancesAction action,
            String host, long millis)
    {
        return new IHEYr4(
                new RnodeWithInstanceActionDescription("BeginStoringInstances",
                rnode, action, "Rnode"), host, millis);
    }


    /**
     *  Description of the Method
     *
     * @param  rnode   Description of the Parameter
     * @param  action  Description of the Parameter
     * @param  host    Description of the Parameter
     * @param  millis  Description of the Parameter
     * @return         Description of the Return Value
     */
    public static IHEYr4 newInstancesSent(RemoteNode rnode,
            InstancesAction action,
            String host, long millis)
    {
        return new IHEYr4(
                new RnodeWithInstanceActionDescription("InstancesSent",
                rnode, action, "RNode"), host, millis);
    }


    /**
     *  Description of the Method
     *
     * @param  actorName          Description of the Parameter
     * @param  applicationAction  Description of the Parameter
     * @param  user               Description of the Parameter
     * @param  host               Description of the Parameter
     * @param  millis             Description of the Parameter
     * @return                    Description of the Return Value
     */
    public static IHEYr4 newActorStartStop(
            String actorName, String applicationAction, User user,
            String host, long millis)
    {
        return new IHEYr4(
                new ActorStartStop(actorName, applicationAction, user),
                host, millis);
    }


    /**
     *  Description of the Method
     *
     * @param  description  Description of the Parameter
     * @param  user         Description of the Parameter
     * @param  configType   Description of the Parameter
     * @param  host         Description of the Parameter
     * @param  millis       Description of the Parameter
     * @return              Description of the Return Value
     */
    public static IHEYr4 newActorConfig(
            String description, User user, String configType,
            String host, long millis)
    {
        return new IHEYr4(
                new ActorConfig(description, user, configType), host, millis);
    }


    /**
     *  Description of the Method
     *
     * @param  action  Description of the Parameter
     * @param  host    Description of the Parameter
     * @param  millis  Description of the Parameter
     * @return         Description of the Return Value
     */
    public static IHEYr4 newStudyDeleted(InstancesAction action,
            String host, long millis)
    {
        return new IHEYr4(new StudyDeleted(action), host, millis);
    }


    /**
     *  Description of the Method
     *
     * @param  keys       Description of the Parameter
     * @param  requestor  Description of the Parameter
     * @param  cuid       Description of the Parameter
     * @param  host       Description of the Parameter
     * @param  millis     Description of the Parameter
     * @return            Description of the Return Value
     */
    public static IHEYr4 newDicomQuery(
            Dataset keys, RemoteNode requestor, String cuid,
            String host, long millis)
    {
        return new IHEYr4(new DicomQuery(keys, requestor, cuid), host, millis);
    }


    /**
     *  Description of the Method
     *
     * @param  alertType    Description of the Parameter
     * @param  user         Description of the Parameter
     * @param  description  Description of the Parameter
     * @param  host         Description of the Parameter
     * @param  millis       Description of the Parameter
     * @return              Description of the Return Value
     */
    public static IHEYr4 newSecurityAlert(
            String alertType, User user, String description,
            String host, long millis)
    {
        return new IHEYr4(
                new SecurityAlert(alertType, user, description), host, millis);
    }


    /**
     *  Description of the Method
     *
     * @param  localUserName  Description of the Parameter
     * @param  action         Description of the Parameter
     * @param  host           Description of the Parameter
     * @param  millis         Description of the Parameter
     * @return                Description of the Return Value
     */
    public static IHEYr4 newUserAuthenticated(
            String localUserName, String action,
            String host, long millis)
    {
        return new IHEYr4(
                new UserAuthenticated(localUserName, action), host, millis);
    }


    /**
     *  Description of the Method
     *
     * @param  media   Description of the Parameter
     * @param  user    Description of the Parameter
     * @param  host    Description of the Parameter
     * @param  millis  Description of the Parameter
     * @return         Description of the Return Value
     */
    public static IHEYr4 newExport(
            MediaDescription media, User user,
            String host, long millis)
    {
        return new IHEYr4(
                new Export(media, user), host, millis);
    }

    // Methods -------------------------------------------------------
    /**
     *  Description of the Method
     *
     * @return    Description of the Return Value
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer(512);
        sb.append("<IHEYr4>");
        msg.writeTo(sb);
        sb.append("<Host>").append(host).append("</Host><TimeStamp>")
                .append(TIMESTAMP.format(new Date(millis)))
                .append("</TimeStamp></IHEYr4>");
        return sb.toString();
    }
}

