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
package org.dcm4chee.web.service.hl7.v2;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.naming.InitialContext;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.io.SAXWriter;
import org.dcm4che2.io.StopTagInputHandler;
import org.dcm4che2.util.StringUtils;
import org.dcm4chee.archive.entity.AE;
import org.dcm4chee.web.dao.AEHomeLocal;
import org.dcm4chee.web.service.common.JMSDelegate;
import org.dcm4chee.web.service.common.RetryIntervalls;
import org.dcm4chee.web.service.common.XSLTUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXContentHandler;
import org.jboss.system.ServiceMBeanSupport;
import org.regenstrief.xhl7.HL7XMLLiterate;
import org.regenstrief.xhl7.HL7XMLReader;
import org.regenstrief.xhl7.MLLPDriver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * @author franz.willer@gmail.com
 * @version $Revision$ $Date$
 * @since Feb 11, 2010
 */
public class HL7SendV2Service extends ServiceMBeanSupport implements MessageListener {

    private JMSDelegate jmsDelegate = new JMSDelegate(this);
    private ObjectName tlscfgServiceName;

    private String[] receiver;
    private String sendingApplication, sendingFacility;
    private Map<String,String> xslFilenames;
    
    private String queueName;
    private RetryIntervalls retryIntervalls = new RetryIntervalls();
    private int concurrency;
    private long messageControlID;
    
    private static final String NONE ="NONE";
    private static final SimpleDateFormat tsFormat = new SimpleDateFormat("yyyyMMddHHmmss.SSS");
    private static final String NEWLINE = System.getProperty("line.separator", "\n");

    private AEHomeLocal aeHome;
    private int acTimeout;
    private int soCloseDelay;
    private String charsetName;
    
    private static final TransformerFactory tf = TransformerFactory.newInstance();
    private Map<String, Templates> templates = new HashMap<String, Templates>();

    public HL7SendV2Service() {
    }

    public String getReceiver() {
        return receiver == null ? NONE : StringUtils.join(receiver, '\\');
    }

    public void setReceiver(String receiver) {
        this.receiver = NONE.equals(receiver) ? null : StringUtils.split(receiver, '\\');
    }

    public String getSendingApplication() {
        return sendingApplication;
    }

    public void setSendingApplication(String app) {
        this.sendingApplication = app;
    }
    
    public String getSendingFacility() {
        return sendingFacility;
    }

    public void setSendingFacility(String facility) {
        this.sendingFacility = facility;
    }

    public String getXslFilenames() {
        if (xslFilenames == null ) 
            return NONE;
        StringBuilder sb = new StringBuilder();
        for ( Map.Entry<String,String> e : xslFilenames.entrySet()) {
            sb.append(e.getKey()).append("=").append(e.getValue()).append(NEWLINE);
        }
        return sb.toString();
    }
    
    public void setXslFilenames(String fn) {
        if ( fn == null || NONE.equals(fn)) {
            xslFilenames = null;
        }
        templates.clear();
        StringTokenizer st = new StringTokenizer(fn, " \r\n\t;");
        xslFilenames = new HashMap<String, String>(st.countTokens());
        String tk;
        int pos;
        while (st.hasMoreTokens()) {
            tk = st.nextToken().trim();
            if (tk.length() == 0)
                continue;
            pos = tk.indexOf('=');
            if (pos != -1)
                xslFilenames.put(tk.substring(0,pos).trim(), tk.substring(++pos).trim());
        }
        if (xslFilenames.size() < 1)
            xslFilenames = null;
    }
    
    public String getCharsetName() {
        return charsetName;
    }

    public void setCharsetName(String charsetName) {
        this.charsetName = charsetName;
    }

    public final String getRetryIntervalls() {
        return retryIntervalls.toString();
    }

    public final void setRetryIntervalls(String s) {
        this.retryIntervalls = new RetryIntervalls(s);
    }

    public int getAcceptTimeout() {
        return acTimeout;
    }

    public void setAcceptTimeout(int acTimeout) {
        this.acTimeout = acTimeout;
    }

    public int getSocketCloseDelay() {
        return soCloseDelay;
    }

    public void setSocketCloseDelay(int soCloseDelay) {
        this.soCloseDelay = soCloseDelay;
    }

    public final String getQueueName() {
        return queueName;
    }
    public final void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public final ObjectName getJmsServiceName() {
        return jmsDelegate.getJmsServiceName();
    }
    public final void setJmsServiceName(ObjectName jmsServiceName) {
        jmsDelegate.setJmsServiceName(jmsServiceName);
    }

    public ObjectName getTlsCfgServiceName() {
        return tlscfgServiceName;
    }

    public void setTlsCfgServiceName(ObjectName tlscfgServiceName) {
        this.tlscfgServiceName = tlscfgServiceName;
    }

    public final int getConcurrency() {
        return concurrency;
    }
    public final void setConcurrency(int concurrency) throws Exception {
        if (concurrency <= 0)
            throw new IllegalArgumentException("Concurrency: " + concurrency);
        if (this.concurrency != concurrency) {
            final boolean restart = getState() == STARTED;
            if (restart)
                stop();
            this.concurrency = concurrency;
            if (restart)
                start();
        }
    }

    protected void startService() throws Exception {
        jmsDelegate.startListening(queueName, this, concurrency);
    }

    protected void stopService() throws Exception {
        jmsDelegate.stopListening(queueName);
    }

    public void schedulePatientUpdate(DicomObject patAttrs) throws Exception {
        scheduleHL7Message(patAttrs, null, "pat_upd");
    }

    public void schedulePatientMerge(DicomObject patAttrsDominant, DicomObject[] patAttrsPrior) throws Exception {
        Map<String,String> paraPriorPat = new HashMap<String,String>();
        for (DicomObject prior : patAttrsPrior) {
            paraPriorPat.put("priorPatId", prior.getString(Tag.PatientID));
            paraPriorPat.put("priorPatIdIssuer", prior.getString(Tag.IssuerOfPatientID));
            paraPriorPat.put("priorPatName", prior.getString(Tag.PatientName));
            scheduleHL7Message(patAttrsDominant, paraPriorPat, "pat_mrg");
            paraPriorPat.clear();
        }
    }
    
    public void scheduleHL7Message(DicomObject attrs, Map<String, String> parameter, String msgTypeID) throws Exception {
        if (receiver != null) {
            for ( String r : receiver) {
                jmsDelegate.queue(queueName, new HL7SendOrder(r, attrs, parameter, msgTypeID), Message.DEFAULT_PRIORITY, 0);
            }
        }
    }

    public String showDcmFileAsHL7Msg(String dcmFilename, String receiver, String msgTypeID) throws InstanceNotFoundException, TransformerConfigurationException, MBeanException, ReflectionException, TransformerFactoryConfigurationError, SAXException, IOException {
        if (receiver == null || receiver.trim().length() < 1) {
            receiver = this.receiver != null ? this.receiver[0] : "TEST^TEST";
        }
        File dcmFile = new File(dcmFilename);
        DicomInputStream dis = new DicomInputStream(dcmFile);
        dis.setHandler(new StopTagInputHandler(Tag.PixelData));
        DicomObject attrs = dis.readDicomObject();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        log.info("Dataset to transform to msgTypeID:"+msgTypeID+" attrs:\n"+attrs);
        writeHL7Message(attrs, toTemplates(msgTypeID), null, receiver, new BufferedOutputStream(baos));
        return baos.toString(charsetName);
    }
    
    private void writeHL7Message(DicomObject attrs, Templates tpl, Map<String,String> parameter,
            String receiver, OutputStream os) 
        throws TransformerConfigurationException, TransformerFactoryConfigurationError, InstanceNotFoundException, MBeanException, ReflectionException, SAXException, IOException {
        TransformerHandler th = XSLTUtils.transformerFactory.newTransformerHandler(tpl);
        Transformer t = th.getTransformer();
        t.setOutputProperty(OutputKeys.METHOD, "text");
        t.setOutputProperty(OutputKeys.ENCODING, charsetName);
        t.setParameter("messageControlID", String.valueOf(++messageControlID) );
        t.setParameter("messageDateTime", tsFormat.format(new Date()));
        int pos = receiver.indexOf('^');
        t.setParameter("receivingApplication", receiver.substring(0, pos++));
        t.setParameter("receivingFacility", receiver.substring(pos));
        t.setParameter("sendingApplication", sendingApplication);
        t.setParameter("sendingFacility", sendingFacility);
        if ( parameter != null) {
            for ( Map.Entry<String, String> e : parameter.entrySet()) {
                t.setParameter(e.getKey(), e.getValue());
            }
        }
        th.setResult(new StreamResult(os));
        SAXWriter writer = new SAXWriter(th,null);
        writer.write(attrs);
    }
    
    private boolean sendDcmAsHL7Msg(DicomObject attrs, Map<String, String> parameter, String receiver, String msgTypeID) 
    throws IOException, InterruptedException, GeneralSecurityException, 
    InstanceNotFoundException, MBeanException, ReflectionException, SAXException, TransformerConfigurationException, TransformerFactoryConfigurationError {
        if ( receiver == null && this.receiver != null) {
            receiver = this.receiver[0];
        }
        AE ae = lookupAEHome().findByTitle(receiver);
        List<String> l = ae.getCipherSuites();
        String[] ciphers = (l==null || l.size()<1) ? null : l.toArray(new String[l.size()]);
        Socket s = (Socket) server.invoke(
                tlscfgServiceName, "initSocket",
                new Object[] { ae.getHostName(), ae.getPort(), ciphers, null, 0 },
                new String[] { String.class.getName(), int.class.getName(), 
                        String[].class.getName(), String.class.getName(), int.class.getName()});
        try {
            MLLPDriver mllpDriver = new MLLPDriver(s.getInputStream(), s
                    .getOutputStream(), true);
            writeHL7Message(attrs, toTemplates(msgTypeID), parameter, receiver, mllpDriver.getOutputStream());
            mllpDriver.turn();
            if (acTimeout > 0) {
                s.setSoTimeout(acTimeout);
            }
            if (!mllpDriver.hasMoreInput()) {
                throw new IOException("Receiver " + "receiver"
                        + " closed socket " + s
                        + " during waiting on response.");
            }
            Document rsp = readMessage(mllpDriver.getInputStream());
            return checkResponse(rsp);
        } finally {
            if (soCloseDelay > 0)
                try {
                    Thread.sleep(soCloseDelay);
                } catch (InterruptedException ignore) {
                }
                s.close();
        }
    }
    
    private Templates toTemplates(String msgTypeID) throws InstanceNotFoundException, MBeanException, ReflectionException, TransformerConfigurationException {
        Templates tpl = templates.get(msgTypeID);
        if (tpl == null) {
            String url = xslFilenames.get(msgTypeID);
            log.info("Get template for url:"+url);
            if (url == null)
                throw new IllegalArgumentException("Unknown msgTypeID! You need to map a XSL URL to msgTypeID:"+msgTypeID);
            try {
                URL url1 = new URL(url);
            } catch (MalformedURLException e) {
                log.error("Malformed URL!", e);
            }
            tpl = tf.newTemplates(new StreamSource(url));
            templates.put(msgTypeID, tpl);
        }
        return tpl;
    }

    public boolean sendHL7File(String receiver, String filename) throws IOException, InstanceNotFoundException, MBeanException, ReflectionException, InterruptedException, GeneralSecurityException, SAXException {
        FileInputStream fis = new FileInputStream(filename);
        byte[] ba = new byte[fis.available()];
        fis.read(ba);
        return sendHL7Msg(receiver, ba);
    }
    
    private boolean sendHL7Msg(String receiver, byte[] hl7msg) 
            throws IOException, InterruptedException, GeneralSecurityException, 
            InstanceNotFoundException, MBeanException, ReflectionException, SAXException {
        if ( receiver == null && this.receiver != null) {
            receiver = this.receiver[0];
        }
        AE ae = lookupAEHome().findByTitle(receiver);
        List<String> l = ae.getCipherSuites();
        String[] ciphers = (l==null || l.size()<1) ? null : l.toArray(new String[l.size()]);
        Socket s = (Socket) server.invoke(
                tlscfgServiceName, "initSocket",
                new Object[] { ae.getHostName(), ae.getPort(), ciphers, null, 0 },
                new String[] { String.class.getName(), int.class.getName(), 
                        String[].class.getName(), String.class.getName(), int.class.getName()});
        try {
            MLLPDriver mllpDriver = new MLLPDriver(s.getInputStream(), s
                    .getOutputStream(), true);
            mllpDriver.getOutputStream().write(hl7msg);
            mllpDriver.turn();
            if (acTimeout > 0) {
                s.setSoTimeout(acTimeout);
            }
            if (!mllpDriver.hasMoreInput()) {
                throw new IOException("Receiver " + "receiver"
                        + " closed socket " + s
                        + " during waiting on response.");
            }
            Document rsp = readMessage(mllpDriver.getInputStream());
            return this.checkResponse(rsp);
        } finally {
            if (soCloseDelay > 0)
                try {
                    Thread.sleep(soCloseDelay);
                } catch (InterruptedException ignore) {
            }
            s.close();
        }
    }

    private Document readMessage(InputStream mllpIn) throws IOException, SAXException {
        InputSource in = new InputSource(mllpIn);
        in.setEncoding(charsetName);
        XMLReader xmlReader = new HL7XMLReader();
        SAXContentHandler hl7in = new SAXContentHandler();
        xmlReader.setContentHandler(hl7in);
        xmlReader.parse(in);
        Document msg = hl7in.getDocument();
        return msg;
    }
    
    private boolean checkResponse(Document rsp) {
        Element msh = rsp.getRootElement().element("MSH");
        if (msh == null)
                throw new IllegalArgumentException("Missing MSH Segment");
        List<Element> fields = msh.elements(HL7XMLLiterate.TAG_FIELD);
        if ("ACK".equals(toString(fields.get(6)))) {
            Element msa = rsp.getRootElement().element("MSA");
            if (msa == null)
                throw new IllegalArgumentException("Missing MSA Segment");
            List<Element> msaFields = msa.elements(HL7XMLLiterate.TAG_FIELD);
            String ackCode = toString(msaFields.get(0));
            if (!("AA".equals(ackCode)
                    || "CA".equals(ackCode))) {
                log.error("Error: acknowledgmentCode"+ackCode+" textMessage:"+ msaFields.get(2));
                return false;
            }
        } else {
            log.warn("Unsupport response message type: " + fields.get(6)
                    + ". Assume message is sent successful.");
        }
        return true;
    }
    
    private static String toString(Element el) {
        return el != null ? el.getText() : "";
    }

    public void onMessage(Message msg) {
        ObjectMessage om = (ObjectMessage) msg;
        try {
            HL7SendOrder order = (HL7SendOrder) om.getObject();
            try {
                log.info("Start processing " + order);
                sendDcmAsHL7Msg(order.getAttrs(), order.getParameter(), order.getReceiving(), order.getMsgTypeID());
                log.info("Finished processing " + order);
            } catch (Exception e) {
                order.setThrowable(e);
                final int failureCount = order.getFailureCount() + 1;
                order.setFailureCount(failureCount);
                final long delay = retryIntervalls.getIntervall(failureCount);
                if (delay == -1L) {
                    log.error("Give up to process " + order);
                    jmsDelegate.fail(queueName,order);
                } else {
                    log.warn("Failed to process " + order
                            + ". Scheduling retry.", e);
                    jmsDelegate.queue(queueName, order, 0, System
                            .currentTimeMillis()
                            + delay);
                }
            }
        } catch (JMSException e) {
            log.error("jms error during processing message: " + msg, e);
        } catch (Throwable e) {
            log.error("unexpected error during processing message: " + msg, e);
        }
    }

    private AEHomeLocal lookupAEHome() {
        if ( aeHome == null ) {
            try {
                InitialContext jndiCtx = new InitialContext();
                aeHome = (AEHomeLocal) jndiCtx.lookup(AEHomeLocal.JNDI_NAME);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return aeHome;
    }
}

