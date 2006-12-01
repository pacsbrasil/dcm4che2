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
 * TIANI Medgraph AG.
 * Portions created by the Initial Developer are Copyright (C) 2003-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunter.zeilinger@tiani.com>
 * Franz Willer <franz.willer@gwi-ag.com>
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

package org.dcm4chex.archive.hl7;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.Hashtable;

import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.ObjectName;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.dcm4che.server.Server;
import org.dcm4che.server.ServerFactory;
import org.dcm4che.util.MLLP_Protocol;
import org.dcm4cheri.util.StringUtils;
import org.dcm4chex.archive.mbean.TLSConfigDelegate;
import org.dom4j.Document;
import org.dom4j.io.DocumentSource;
import org.dom4j.io.SAXContentHandler;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.system.server.ServerConfigLocator;
import org.regenstrief.xhl7.HL7XMLReader;
import org.regenstrief.xhl7.HL7XMLWriter;
import org.regenstrief.xhl7.MLLPDriver;
import org.regenstrief.xhl7.XMLWriter;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision$ $Date$
 * @since 25.10.2004
 * 
 */
public class HL7ServerService extends ServiceMBeanSupport implements
        Server.Handler {

    private static final String ISO_8859_1 = "ISO-8859-1";

    public static final String EVENT_TYPE = "org.dcm4chex.archive.hl7";

    public static final NotificationFilter NOTIF_FILTER = new NotificationFilter() {

        private static final long serialVersionUID = 4049637871541892405L;

        public boolean isNotificationEnabled(Notification notif) {
            return EVENT_TYPE.equals(notif.getType());
        }
    };

    private String ackStylesheetURL = "resource:dcm4chee-hl7/msh2ack.xsl";

    private String logStylesheetURL = "resource:dcm4chee-hl7/logmsg.xsl";

    private File logDir;

    private Server hl7srv = ServerFactory.getInstance().newServer(this);

    private MLLP_Protocol protocol = MLLP_Protocol.MLLP;

    private ObjectName auditLogName;

    private boolean fileReceivedHL7AsXML;

    private boolean fileReceivedHL7;

    private boolean sendNotification;

    private int soTimeout = 0;

    private int numberOfReceivedMessages = 0;

    private TLSConfigDelegate tlsConfig = new TLSConfigDelegate(this);

    private Hashtable templates = new Hashtable();

    private Hashtable serviceRegistry = new Hashtable();

    private String[] noopMessageTypes = {};

    public final String getAckStylesheetURL() {
        return ackStylesheetURL;
    }

    public final void setAckStylesheetURL(String ackStylesheetURL) {
        this.ackStylesheetURL = ackStylesheetURL;
        reloadStylesheets();
    }

    public final String getLogStylesheetURL() {
        return logStylesheetURL;
    }

    public final void setLogStylesheetURL(String logStylesheetURL) {
        this.logStylesheetURL = logStylesheetURL;
        reloadStylesheets();
    }

    public ObjectName getAuditLoggerName() {
        return auditLogName;
    }

    public void setAuditLoggerName(ObjectName auditLogName) {
        this.auditLogName = auditLogName;
    }

    public final ObjectName getTLSConfigName() {
        return tlsConfig.getTLSConfigName();
    }

    public final void setTLSConfigName(ObjectName tlsConfigName) {
        tlsConfig.setTLSConfigName(tlsConfigName);
    }

    public int getPort() {
        return hl7srv.getPort();
    }

    public void setPort(int port) {
        hl7srv.setPort(port);
    }

    public final String getNoopMessageTypes() {
        return StringUtils.toString(noopMessageTypes, ',');
    }

    public final void setNoopMessageTypes(String noopMessageTypes) {
        this.noopMessageTypes = StringUtils.split(noopMessageTypes, ',');
    }

    public String getProtocolName() {
        return protocol.toString();
    }

    public void setProtocolName(String protocolName) {
        this.protocol = MLLP_Protocol.valueOf(protocolName);
    }

    public int getMaxClients() {
        return hl7srv.getMaxClients();
    }

    public void setMaxClients(int newMaxClients) {
        hl7srv.setMaxClients(newMaxClients);
    }

    public int getNumClients() {
        return hl7srv.getNumClients();
    }

    public int getMaxIdleThreads() {
        return hl7srv.getMaxIdleThreads();
    }

    public int getNumIdleThreads() {
        return hl7srv.getNumIdleThreads();
    }

    public void setMaxIdleThreads(int max) {
        hl7srv.setMaxIdleThreads(max);
    }

    public final boolean isFileReceivedHL7AsXML() {
        return fileReceivedHL7AsXML;
    }

    public final void setFileReceivedHL7AsXML(boolean fileReceivedHL7AsXML) {
        this.fileReceivedHL7AsXML = fileReceivedHL7AsXML;
    }

    public final boolean isFileReceivedHL7() {
        return fileReceivedHL7;
    }

    public final void setFileReceivedHL7(boolean fileReceivedHL7) {
        this.fileReceivedHL7 = fileReceivedHL7;
    }

    public final int getSoTimeout() {
        return soTimeout;
    }

    public final void setSoTimeout(int soTimeout) {
        this.soTimeout = soTimeout;
    }

    public final boolean isSendNotification() {
        return sendNotification;
    }

    public final void setSendNotification(boolean sendNotification) {
        this.sendNotification = sendNotification;
    }

    public final int getNumberOfReceivedMessages() {
        return numberOfReceivedMessages;
    }

    public void registerService(String messageType, HL7Service service) {
        if (service != null)
            serviceRegistry.put(messageType, service);
        else
            serviceRegistry.remove(messageType);
    }

    public Templates getTemplates(String uri)
            throws TransformerConfigurationException {
        Templates tpl = (Templates) templates.get(uri);
        if (tpl == null) {
            tpl = TransformerFactory.newInstance().newTemplates(
                    new StreamSource(uri));
            templates.put(uri, tpl);
        }
        return tpl;
    }

    public void reloadStylesheets() {
        templates.clear();
    }

    protected void startService() throws Exception {
        logDir = new File(ServerConfigLocator.locate().getServerHomeDir(),
                "log");
        hl7srv.addHandshakeFailedListener(tlsConfig.handshakeFailedListener());
        hl7srv.setServerSocketFactory(tlsConfig.serverSocketFactory(protocol
                .getCipherSuites()));
        hl7srv.start();
    }

    protected void stopService() throws Exception {
        hl7srv.stop();
        templates.clear();
    }

    public void ack(Document document, ContentHandler hl7out, HL7Exception hl7ex) {
        try {
            Transformer t = getTemplates(ackStylesheetURL).newTransformer();
            if (hl7ex != null) {
                t.setParameter("AcknowledgementCode", hl7ex
                        .getAcknowledgementCode());
                t.setParameter("TextMessage", hl7ex.getMessage());
            }
            t.transform(new DocumentSource(document), new SAXResult(hl7out));
        } catch (TransformerException e) {
            log.error("Failed to acknowlege message", e);
        }
    }

    private HL7Service getService(MSH msh) throws HL7Exception {
        String messageType = msh.messageType + '^' + msh.triggerEvent;
        HL7Service service = (HL7Service) serviceRegistry.get(messageType);
        if (service == null) {
            if (Arrays.asList(noopMessageTypes).indexOf(messageType) == -1)
                throw new HL7Exception("AR", "Unsupported message type: "
                        + messageType.replace('^', '_'));
        }
        return service;
    }

    // Server.Handler Implementation-------------------------------
    public void handle(Socket s) throws IOException {
        if (soTimeout > 0) {
            s.setSoTimeout(soTimeout);
        }
        MLLPDriver mllpDriver = new MLLPDriver(s.getInputStream(), s
                .getOutputStream(), false);
        InputStream mllpIn = mllpDriver.getInputStream();
        XMLReader xmlReader = new HL7XMLReader();
        XMLWriter xmlWriter = new HL7XMLWriter();
        xmlWriter.setOutputStream(mllpDriver.getOutputStream());
        ContentHandler hl7out = xmlWriter.getContentHandler();
        SAXContentHandler hl7in = new SAXContentHandler();
        xmlReader.setContentHandler(hl7in);
        byte[] bb = new byte[1024];
        while (mllpDriver.hasMoreInput()) {
            int msglen = 0;
            int read = 0;
            do {
                msglen += read;
                if (msglen == bb.length) {
                    bb = realloc(bb, bb.length, bb.length * 2);
                }
                read = mllpIn.read(bb, msglen, bb.length - msglen);
            } while (read > 0);
            int msgNo = ++numberOfReceivedMessages;
            if (fileReceivedHL7) {
                File logfile = makeLogfile("hl7-######.hl7", msgNo);
                try {
                    OutputStream loghl7 = new BufferedOutputStream(
                            new FileOutputStream(logfile));
                    loghl7.write(bb, 0, msglen);
                    loghl7.close();
                } catch (IOException e) {
                    log.warn(
                            "Failed to log received HL7 message to " + logfile,
                            e);
                }
            }
            try {
                try {
                    ByteArrayInputStream bbin = new ByteArrayInputStream(bb, 0,
                            msglen);
                    InputSource in = new InputSource(new InputStreamReader(
                            bbin, ISO_8859_1));
                    xmlReader.parse(in);
                    Document msg = hl7in.getDocument();
                    logMessage(msg);
                    if (fileReceivedHL7AsXML) {
                        fileReceivedHL7AsXML(msg, makeLogfile("hl7-######.xml",
                                msgNo));
                    }
                    MSH msh = new MSH(msg);
                    HL7Service service = getService(msh);
                    if (service == null || service.process(msh, msg, hl7out)) {
                        ack(msg, hl7out, null);
                    }
                    if (sendNotification) {
                        sendNotification(makeNotification(realloc(bb, msglen,msglen),
                                msg));
                    }
                } catch (SAXException e) {
                    throw new HL7Exception("AE", "Failed to parse message ", e);
                }
            } catch (HL7Exception e) {
                log.warn("Processing HL7 failed:", e);
                mllpDriver.discardPendingOutput();
                ack(hl7in.getDocument(), hl7out, e);
            }
            mllpDriver.turn();
        }
    }

    private byte[] realloc(byte[] bb, int len, int newlen) {
        byte[] out = new byte[newlen];
        System.arraycopy(bb, 0, out, 0, len);
        return out;
    }

    private File makeLogfile(String pattern, int msgNo) {
        final int endPrefix = pattern.indexOf('#');
        final int startSuffix = pattern.lastIndexOf('#') + 1;
        final String noStr = String.valueOf(msgNo);
        StringBuffer sb = new StringBuffer(pattern.substring(0, endPrefix));
        for (int i = endPrefix + noStr.length(); i < startSuffix; ++i) {
            sb.append('0');
        }
        sb.append(noStr);
        sb.append(pattern.substring(startSuffix));
        return new File(logDir, sb.toString());
    }

    private void fileReceivedHL7AsXML(Document msg, File f) {
        try {
            Transformer tr = TransformerFactory.newInstance().newTransformer();
            tr.setOutputProperty(OutputKeys.INDENT, "yes");
            FileOutputStream out = new FileOutputStream(f);
            try {
                tr.transform(new DocumentSource(msg), new StreamResult(out));
            } finally {
                out.close();
            }
        } catch (Exception e) {
            log.warn("Failed to log HL7 to " + f, e);
        }
    }

    public void logMessage(Document document) {
        if (!log.isInfoEnabled())
            return;
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            out.write("Received HL7 message:".getBytes());
            Transformer t = getTemplates(logStylesheetURL).newTransformer();
            t.transform(new DocumentSource(document), new StreamResult(out));
            log.info(out.toString());
        } catch (Exception e) {
            log.warn("Failed to log message", e);
        }
    }

    public boolean isSockedClosedByHandler() {
        return false;
    }

    private Notification makeNotification(byte[] hl7msg, Document msg) {
        long eventID = super.getNextNotificationSequenceNumber();
        Notification notif = new Notification(EVENT_TYPE, this, eventID);
        notif.setUserData(new Object[]{hl7msg, msg});
        return notif;
    }
}