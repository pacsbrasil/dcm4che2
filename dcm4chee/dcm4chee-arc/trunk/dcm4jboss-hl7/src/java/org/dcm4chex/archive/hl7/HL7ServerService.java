/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.hl7;

import java.io.IOException;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;

import javax.management.ObjectName;
import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

import org.dcm4che.auditlog.AuditLogger;
import org.dcm4che.auditlog.AuditLoggerFactory;
import org.dcm4che.util.MLLP_Protocol;
import org.dcm4che.util.SSLContextAdapter;
import org.dcm4chex.archive.mbean.TLSConfigDelegate;
import org.dcm4chex.archive.mbean.TLSConfigService;
import org.jboss.system.ServiceMBeanSupport;

import ca.uhn.hl7v2.app.Application;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision$ $Date$
 * @since 24.02.2004
 */
public class HL7ServerService extends ServiceMBeanSupport {

    private HL7Server hl7srv = new HL7Server(this);

    private MLLP_Protocol protocol = MLLP_Protocol.MLLP;

    private ObjectName auditLogName;

    private TLSConfigDelegate tlsConfig = new TLSConfigDelegate(this);
    
    public final ObjectName getTLSConfigName() {
        return tlsConfig.getTLSConfigName();
    }

    public final void setTLSConfigName(ObjectName tlsConfigName) {
        tlsConfig.setTLSConfigName(tlsConfigName);
    }


    public ObjectName getAuditLoggerName() {
        return auditLogName;
    }

    public void setAuditLoggerName(ObjectName auditLogName) {
        this.auditLogName = auditLogName;
    }

    public int getPort() {
        return hl7srv.getPort();
    }

    public void setPort(int port) {
        hl7srv.setPort(port);
    }

    public String getProtocolName() {
        return protocol.toString();
    }

    public void setProtocolName(String protocolName) {
        this.protocol = MLLP_Protocol.valueOf(protocolName);
    }

    public void registerApplication(String messageType, String triggerEvent,
            Application handler) {
        hl7srv.registerApplication(messageType, triggerEvent, handler);
    }

    protected void startService() throws Exception {
        hl7srv.setServerSocketFactory(tlsConfig.getServerSocketFactory(protocol
                .getCipherSuites()));
        hl7srv.start();
    }

    protected void stopService() throws Exception {
        hl7srv.stop();
    }

    public void handshake(SSLSocket s) {
        try {
            s.startHandshake();
            if (log.isInfoEnabled()) {
                SSLSession se = s.getSession();
                try {
                    X509Certificate cert = (X509Certificate) se
                            .getPeerCertificates()[0];
                    log.info(s.getInetAddress().toString() + ": accept "
                            + se.getCipherSuite() + " with "
                            + cert.getSubjectDN());
                } catch (SSLPeerUnverifiedException e) {
                    log.error("SSL peer not verified:", e);
                }
            }
        } catch (IOException e) {
            logSecurityAlert("NodeAuthentification", s, null, e.getMessage());
        }

    }

    private void logSecurityAlert(String alertType, Socket socket, String aet,
            String description) {
        if (auditLogName == null) return;
        try {
            server.invoke(auditLogName, "logSecurityAlert",
                new Object[] { alertType, socket, aet, description },
                new String[] { String.class.getName(), Socket.class.getName(),
                    String.class.getName(), String.class.getName()});
        } catch (Exception e) {
            log.warn("Audit Log failed:", e);
        }
    }
}
