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
 * Agfa Healthcare.
 * Portions created by the Initial Developer are Copyright (C) 2006
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

package org.dcm4chee.audit.logger;

import java.net.InetAddress;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;
import org.dcm4che2.audit.message.Destination;
import org.dcm4che2.audit.message.DestinationMedia;
import org.dcm4che2.audit.message.NetworkAccessPoint;
import org.dcm4che2.audit.message.ParticipantObjectDescription;
import org.dcm4che2.audit.message.Patient;
import org.dcm4che2.audit.message.Source;
import org.dcm4che2.audit.message.Study;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Id$
 * @since Jan 8, 2007
 */
public class LoggerUtils {

    public static final Logger log = Logger.getLogger("audit");
    private static boolean hostLookup = true;

    public static boolean isDisableHostLookup() {
        return !hostLookup;
    }
    
    public void setDisableHostLookup(boolean disable) {
        hostLookup = !disable;
    }
    
    public static NetworkAccessPoint toNetworkAccessPoint(InetAddress remoteAddr) {
        String id = hostLookup 
                ? remoteAddr.getHostName()
                : remoteAddr.getHostAddress();
        NetworkAccessPoint nap = Character.isDigit(id.charAt(0))
                ? (NetworkAccessPoint) new NetworkAccessPoint.IPAddress(id)
                : (NetworkAccessPoint) new NetworkAccessPoint.HostName(id);
        return nap;
    }

    public static NetworkAccessPoint toRemoteNetworkAccessPoint(
            HttpServletRequest rq) {
        String id = hostLookup 
                ? rq.getRemoteHost()
                : rq.getRemoteAddr();
        NetworkAccessPoint nap = Character.isDigit(id.charAt(0))
                ? (NetworkAccessPoint) new NetworkAccessPoint.IPAddress(id)
                : (NetworkAccessPoint) new NetworkAccessPoint.HostName(id);
        return nap;
    }

    public static NetworkAccessPoint toLocalNetworkAccessPoint(
            HttpServletRequest rq) {
        String id = rq.getServerName();
        NetworkAccessPoint nap = Character.isDigit(id.charAt(0))
                ? (NetworkAccessPoint) new NetworkAccessPoint.IPAddress(id)
                : (NetworkAccessPoint) new NetworkAccessPoint.HostName(id);
        return nap;
    }
    
    public static Source toSource(boolean requestor, String aet, InetAddress addr) {
        Source src;
        if (addr == null) {
            src = new Source(aet);
        } else {
            NetworkAccessPoint nap = toNetworkAccessPoint(addr);
            src = new Source(nap.getNodeID());
            src.setNetworkAccessPoint(nap);
        }
        src.setAETitle(aet);
        src.setAETitle(aet);
        if (!requestor) {
            src.setUserIsRequestor(false);
        }
        return src;
    }

    public static Destination toDestination(boolean requestor, String aet,
            InetAddress addr) {
        Destination dst;
        if (addr == null) {
            dst = new Destination(aet);
        } else {
            NetworkAccessPoint nap = toNetworkAccessPoint(addr);
            dst = new Destination(nap.getNodeID());
            dst.setNetworkAccessPoint(nap);
        }
        dst.setAETitle(aet);
        if (!requestor) {
            dst.setUserIsRequestor(false);
        }
        return dst;
    }

    public static Source toLocalSource(HttpServletRequest request) {
        NetworkAccessPoint nap = toLocalNetworkAccessPoint(request);
        Source src = new Source(request.getRequestURL().toString());
        src.setUserIsRequestor(false);
        src.setNetworkAccessPoint(nap);
        return src;
    }

    public static Destination toRemoteDestination(HttpServletRequest request) {
        NetworkAccessPoint nap = toRemoteNetworkAccessPoint(request);
        String user = request.getRemoteUser();
        String id = user != null ? (user + '@' + nap.getID()) : nap.getID();
        Destination dst = new Destination(id);
        dst.setNetworkAccessPoint(nap);
        return dst;
    }

    public static DestinationMedia toRemoteDestinationMedia(HttpServletRequest request) {
        NetworkAccessPoint nap = toRemoteNetworkAccessPoint(request);
        String user = request.getRemoteUser();
        String id = user != null ? (user + '@' + nap.getID()) : nap.getID();
        DestinationMedia dst = new DestinationMedia(id);
        dst.setNetworkAccessPoint(nap);
        return dst;
    }
    
    public static Patient toPatient(Dataset ds) {
        Patient patient = new Patient(ds.getString(Tags.PatientID));
        String pn = ds.getString(Tags.PatientName);
        if (pn != null) {
            patient.setParticipantObjectName(pn);
        }
        return patient;
    }

    public static Study toStudy(Dataset ds) {
        Study study = new Study(ds.getString(Tags.StudyInstanceUID));
        String cuid = ds.getString(Tags.SOPClassUID);
        if ( cuid != null ) {
            ParticipantObjectDescription desc = new ParticipantObjectDescription();
                ParticipantObjectDescription.SOPClass sopClass =
                    new ParticipantObjectDescription.SOPClass(cuid);
                sopClass.setNumberOfInstances(1);
                desc.addSOPClass(sopClass);            
                study.addParticipantObjectDescription(desc);
        }
        return study ;
    }
}
