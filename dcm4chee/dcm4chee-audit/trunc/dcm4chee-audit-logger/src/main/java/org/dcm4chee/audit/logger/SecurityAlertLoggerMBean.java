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
 * Agfa-Gevaert AG.
 * Portions created by the Initial Developer are Copyright (C) 2002-2005
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

import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.Principal;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.management.AttributeChangeNotification;
import javax.management.AttributeChangeNotificationFilter;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.Notification;
import javax.management.NotificationFilterSupport;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

import org.apache.log4j.Logger;
import org.dcm4che.util.HandshakeFailedEvent;
import org.dcm4che2.audit.message.ActiveParticipant;
import org.dcm4che2.audit.message.AlertSubject;
import org.dcm4che2.audit.message.Application;
import org.dcm4che2.audit.message.ApplicationActivityMessage;
import org.dcm4che2.audit.message.ApplicationLauncher;
import org.dcm4che2.audit.message.AuditEvent;
import org.dcm4che2.audit.message.AuditMessage;
import org.dcm4che2.audit.message.AuditSource;
import org.dcm4che2.audit.message.NetworkAccessPoint;
import org.dcm4che2.audit.message.ParticipantObject;
import org.dcm4che2.audit.message.PerformingParticipant;
import org.dcm4che2.audit.message.SecurityAlertMessage;
import org.jboss.annotation.ejb.Management;
import org.jboss.annotation.ejb.Service;
import org.jboss.mx.util.MBeanServerLocator;
import org.jboss.security.SecurityAssociation;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Dec 7, 2006
 */
@Service(objectName = "dcm4chee.archive.logger:name=SecurityAlertLogger,type=service")
@Management(SecurityAlertLogger.class)
public class SecurityAlertLoggerMBean implements SecurityAlertLogger {

    private static final Logger auditlog = Logger.getLogger("audit");
    private static final Logger log = 
            Logger.getLogger(SecurityAlertLoggerMBean.class);
    private static final String AUDIT_SOURCE_URL =
            "resource:dcm4chee-audit/audit_source.properties";
    private static final String SECURITY_CONFIGURATION_URL =
            "resource:dcm4chee-audit/security_configuration.properties";
    private static final String NETWORK_CONFIGURATION_URL =
            "resource:dcm4chee-audit/network_configuration.properties";
    private static final String SOFTWARE_CONFIGURATION_URL = 
            "resource:dcm4chee-audit/software_configuration.properties";

    private static final ObjectName tlsConfigName;
    static {
        try {
            tlsConfigName = new ObjectName("dcm4chee.archive:service=TLSConfig");
        } catch (MalformedObjectNameException e) {
            throw new RuntimeException(e);
        }
    }
    
    private final HashSet<ObjectName> acnSources = new HashSet<ObjectName>();
    private MBeanServer server;
    
    private String applicationID;
    private String aeTitles;
    private String altUserID;
    private NetworkAccessPoint nap;
    private boolean hostLookup = true;
    
    public final String getApplicationID() {
        return applicationID;
    }

    public final void setApplicationID(String applicationID) {
        if (applicationID.length() == 0) {
            throw new IllegalArgumentException("applicationID cannot be empty");
        }
        this.applicationID = applicationID;
    }

    public final String getAETitles() {
        return aeTitles;
    }

    public final void setAETitles(String aeTitles) {
        if (aeTitles.length() == 0) {
            throw new IllegalArgumentException("aeTitles cannot be empty");
        }
        this.aeTitles = aeTitles;
        altUserID = "AETITLES=" + aeTitles.replace('\\', ';');
    }
    
    public final boolean isDisableHostLookup() {
        return !hostLookup;
    }

    public final void setDisableHostLookup(boolean disable) {
        this.hostLookup = !disable;
    }
    
    public final NetworkAccessPoint getNetworkAccessPoint() {
        return nap;
    }

    public String getAuditSourceID() {
        return AuditMessage.getDefaultAuditSource().getAuditSourceID();
    }
        
    public String getAuditEnterpriseSiteID() {
        return AuditMessage.getDefaultAuditSource().getAuditEnterpriseSiteID();
    }
    
    public String getAuditSourceTypeCodes() {
        List list = 
                AuditMessage.getDefaultAuditSource().getAuditSourceTypeCodes();
        if (list.isEmpty()) {
            return null;
        }
        StringBuffer sb = new StringBuffer();
        for (Object code : list) {
            sb.append(((AuditSource.TypeCode) code).getCode()).append('+');
        }
        return sb.substring(0, sb.length()-1);
    }
        
    private void initLocalNetworkAccessPoint() {
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            String ip = localHost.getHostAddress();
            String host = localHost.getHostName();
            nap = ip.equals(host)
                    ? (NetworkAccessPoint) new NetworkAccessPoint.IPAddress(ip)
                    : (NetworkAccessPoint) new NetworkAccessPoint.HostName(host);
        } catch (UnknownHostException e) {
            log.error("Failed to determine Network Access Point ID:", e);
            nap = new NetworkAccessPoint.HostName("???");
        }
    }
    
    private void initDefaultAuditSource() throws Exception {
        Properties p = new Properties();
        InputStream in = new URL(AUDIT_SOURCE_URL).openStream();
        try {
            p.load(in);
        } finally {
            in.close();
        }

        String auditSourceID = p.getProperty("AuditSourceID");
        String enterpriseSiteID = p.getProperty("AuditEnterpriseSiteID");
        String auditSourceTypeCodes = p.getProperty("AuditSourceTypeCodes");
        AuditSource auditSource = new AuditSource(auditSourceID);
        if (enterpriseSiteID != null
                && enterpriseSiteID.length() != 0) {
            auditSource.setAuditEnterpriseSiteID(enterpriseSiteID);
        }
        if (auditSourceTypeCodes != null
                && auditSourceTypeCodes.length() != 0) {
            char[] cs = auditSourceTypeCodes.toCharArray();
            for (int i = 0; i < cs.length; i++, i++) {
                auditSource.addAuditSourceTypeCode(
                        new AuditSource.TypeCode(String.valueOf(cs[i])));
            }
        }
        AuditMessage.setDefaultAuditSource(auditSource);
        
        setApplicationID(p.getProperty("ApplicationID"));
        setAETitles(p.getProperty("AETitles"));
    }
    
    public void create() throws Exception {
        server = MBeanServerLocator.locate();
        initLocalNetworkAccessPoint();
        initDefaultAuditSource();
        logApplicationActivity(AuditEvent.TypeCode.APPLICATION_START);
    }

    public void destroy() throws Exception {
        logApplicationActivity(AuditEvent.TypeCode.APPLICATION_STOP);
        server = null;
    }
    
    public void start() throws Exception {
        registerACNListener(SECURITY_CONFIGURATION_URL, 
                AuditEvent.TypeCode.SECURITY_CONFIGURATION);
        registerACNListener(NETWORK_CONFIGURATION_URL, 
                AuditEvent.TypeCode.NETWORK_CONFIGURATION);
        registerACNListener(SOFTWARE_CONFIGURATION_URL, 
                AuditEvent.TypeCode.SOFTWARE_CONFIGURATION);
        registerSSLHandshakeListener(hsfl, HandshakeFailedEvent.class);
        registerSSLHandshakeListener(hscl, HandshakeCompletedEvent.class);
        if (SecurityAssociation.getPrincipal() != null) {
            logSecurityAlert(AuditEvent.TypeCode.AUDIT_RECORDING_STARTED, null);
        }
    }

    public void stop() {
        unregisterACNListener();
        unregisterSSLHandshakeListener(hsfl);
        unregisterSSLHandshakeListener(hscl);
        if (SecurityAssociation.getPrincipal() != null) {
            logSecurityAlert(AuditEvent.TypeCode.AUDIT_RECORDING_STOPPED, null);
        }
    }

    private void registerSSLHandshakeListener(NotificationListener l, Class c) 
            throws Exception {
        NotificationFilterSupport f = new NotificationFilterSupport();
        f.enableType(c.getName());
        server.addNotificationListener(tlsConfigName, l, f , null);
    }

    private void unregisterSSLHandshakeListener(NotificationListener l) {
        try {
            server.removeNotificationListener(tlsConfigName, l);
        } catch (Exception e) {
            log.warn("Failed to unregister SSL Handshake Notification" +
                            "Listener from " + tlsConfigName, e);
        }
    }
    
    private void registerACNListener(String url, AuditEvent.TypeCode code)
            throws Exception {
        Properties p = new Properties();
        InputStream in = new URL(url).openStream();
        try {
            p.load(in);
        } finally {
            in.close();
        }
        for (Map.Entry entry :  p.entrySet()) {
            ObjectName name = new ObjectName((String) entry.getKey());
            server.addNotificationListener(name, acnl, 
                    toACNFilter((String) entry.getValue()), code);
            acnSources.add(name);
        }
    }

    private void unregisterACNListener() {
        for (Iterator<ObjectName> it = acnSources.iterator(); it.hasNext();) {
            ObjectName source = it.next();
            try {
                server.removeNotificationListener(source, acnl);
                it.remove();
            } catch (Exception e) {
                log.warn("Failed to unregister Attribute Change Notification" +
                                "Listener from " + source, e);
            }
        }
    }
    
    private AttributeChangeNotificationFilter toACNFilter(String attrs) {
        AttributeChangeNotificationFilter acnf = 
                new AttributeChangeNotificationFilter();
        StringTokenizer stk = new StringTokenizer(attrs, ", ");
        while (stk.hasMoreTokens()) {
            acnf.enableAttribute(stk.nextToken());
        }
        return acnf;
    }

    private void logApplicationActivity(AuditEvent.TypeCode typeCode) {
        ApplicationActivityMessage msg = new ApplicationActivityMessage(
                new ApplicationActivityMessage.AuditEvent(typeCode), 
                getApplication());
        msg.addApplicationLauncher(new ApplicationLauncher(getPrincipal()));
        SecurityAlertLoggerMBean.auditlog.info(msg);        
    }

    private Application getApplication() {
        Application app = new Application(applicationID);
        app.setAlternativeUserID(altUserID);
        return app;
    }

    private void logSecurityAlert(AuditEvent.TypeCode typeCode, 
            AlertSubject alertSubject) {
        SecurityAlertMessage msg = new SecurityAlertMessage(
                new SecurityAlertMessage.AuditEvent(typeCode),
                getReporter());
        msg.addPerfomingParticipant(new PerformingParticipant(getPrincipal()));
        if (alertSubject != null) {
            msg.addAlertSubject(alertSubject);
        }
        SecurityAlertLoggerMBean.auditlog.info(msg);
    }
    
    private ActiveParticipant getReporter() {
        return new ActiveParticipant(applicationID);
    }
    
    private String getPrincipal() {
        Principal p = SecurityAssociation.getPrincipal();
        return p != null ? p.getName() : System.getProperty("user.name");
    }

    private AlertSubject mkAlertSubject(String desc) {
        return new AlertSubject(nap.getNodeID(),
                ParticipantObject.IDTypeCode.NODE_ID, desc);
    }
    
    private final NotificationListener acnl = new NotificationListener() {

        public void handleNotification(Notification notif, Object handback) {
            AttributeChangeNotification scn = (AttributeChangeNotification) notif;
            if (scn.getNewValue().equals(scn.getOldValue())) {
                return;
            }
            AuditEvent.TypeCode typeCode = (AuditEvent.TypeCode) handback;
            logSecurityAlert(typeCode, mkAlertSubject(toText(scn)));
        }

        private String toText(AttributeChangeNotification scn) {
            return "Change " + scn.getSource() + "#" + scn.getAttributeName()
                + " from " + scn.getOldValue() + " to " + scn.getNewValue();
        }
    };

    private PerformingParticipant toPerformer(SSLSocket sock,
            InetAddress remoteAddr) {
        SSLSession session = sock.getSession();
        String host = hostLookup ? remoteAddr.getHostName()
                : remoteAddr.getHostAddress();
        String id;
        try {
            Principal p = session.getPeerPrincipal();
            id = p.getName();
        } catch (SSLPeerUnverifiedException e) {
            id = host;
        }
        PerformingParticipant perf = new PerformingParticipant(id);
        perf.setNetworkAccessPoint(Character.isDigit(host.charAt(0))
                ? (NetworkAccessPoint) new NetworkAccessPoint.IPAddress(host)
                : (NetworkAccessPoint) new NetworkAccessPoint.HostName(host));
        return perf;
    }

    private final NotificationListener hsfl = new NotificationListener() {

        public void handleNotification(Notification notif, Object handback) {
            HandshakeFailedEvent hsfEvent = 
                    (HandshakeFailedEvent) notif.getUserData();
            SecurityAlertMessage.AuditEvent.NodeAuthentication event = 
                    new SecurityAlertMessage.AuditEvent.NodeAuthentication();
            event.setEventOutcomeIndicator(
                    AuditEvent.OutcomeIndicator.MINOR_FAILURE);
            SecurityAlertMessage msg = new SecurityAlertMessage(event,
                    getReporter());
            msg.addPerfomingParticipant(toPerformer(hsfEvent.getSocket(), 
                    hsfEvent.getRemoteAddress()));
            msg.addAlertSubject(mkAlertSubject(
                    hsfEvent.getException().getMessage()));
            SecurityAlertLoggerMBean.auditlog.warn(msg);           
        }};

    private final NotificationListener hscl = new NotificationListener() {

        public void handleNotification(Notification notif, Object handback) {
            HandshakeCompletedEvent hscEvent = 
                    (HandshakeCompletedEvent) notif.getUserData();
            SecurityAlertMessage msg = new SecurityAlertMessage(
                    new SecurityAlertMessage.AuditEvent.NodeAuthentication(),
                    getReporter());
            msg.addPerfomingParticipant(toPerformer(hscEvent.getSocket(), 
                    hscEvent.getSocket().getInetAddress()));
            msg.addAlertSubject(mkAlertSubject(toText(hscEvent)));
            SecurityAlertLoggerMBean.auditlog.info(msg);
            
        }

        private String toText(HandshakeCompletedEvent hscEvent) {
            return "SSL handshake completed, cipher suite: "
                    + hscEvent.getCipherSuite();
        }};
}
