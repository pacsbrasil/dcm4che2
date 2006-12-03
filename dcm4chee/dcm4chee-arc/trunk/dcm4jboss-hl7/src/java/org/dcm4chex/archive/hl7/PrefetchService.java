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
 * See @authors listed below.
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

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXResult;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4chex.archive.config.DicomPriority;
import org.dcm4chex.archive.config.RetryIntervalls;
import org.dcm4chex.archive.mbean.JMSDelegate;
import org.dcm4chex.archive.mbean.TLSConfigDelegate;
import org.dom4j.Document;
import org.dom4j.io.DocumentSource;
import org.jboss.system.ServiceMBeanSupport;
import org.regenstrief.xhl7.HL7XMLLiterate;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Nov 30, 2006
 */
public class PrefetchService extends ServiceMBeanSupport implements
        NotificationListener, MessageListener {

    private String prefetchSourceAET;
    private String destinationQueryAET;
    private String destinationStorageAET;
    private String stylesheetURL = "resource:dcm4chee-hl7/orm2prefetch.xsl";
    private ObjectName hl7ServerName;
    private ObjectName moveScuServiceName;
    private String queueName;
    private RetryIntervalls retryIntervalls = new RetryIntervalls();
    private int sourceQueryPriority = 0;
    private int destinationQueryPriority = 0;
    private int retrievePriority = 0;

    private int concurrency = 1;

    private JMSDelegate jmsDelegate = new JMSDelegate(this);
    private TLSConfigDelegate tlsConfig = new TLSConfigDelegate(this);

    public final String getPrefetchSourceAET() {
        return prefetchSourceAET != null ? prefetchSourceAET : "NONE";
    }

    public final void setPrefetchSourceAET(String aet) {
        this.prefetchSourceAET = "NONE".equalsIgnoreCase(aet) ? null : aet;
    }
    
    public final String getDestinationQueryAET() {
        return destinationQueryAET;
    }

    public final void setDestinationQueryAET(String aet) {
        this.destinationQueryAET = aet;
    }

    public final String getDestinationStorageAET() {
        return destinationStorageAET;
    }

    public final void setDestinationStorageAET(String aet) {
        this.destinationStorageAET = aet;
    }

    public final String getSourceQueryPriority() {
        return DicomPriority.toString(sourceQueryPriority);
    }

    public final void setSourceQueryPriority(String cs) {
        this.sourceQueryPriority = DicomPriority.toCode(cs);
    }
    
    public final String getDestinationQueryPriority() {
        return DicomPriority.toString(destinationQueryPriority);
    }

    public final void setDestinationQueryPriority(String cs) {
        this.destinationQueryPriority = DicomPriority.toCode(cs);
    }

    public final String getRetrievePriority() {
        return DicomPriority.toString(retrievePriority);
    }

    public final void setRetrievePriority(String retrievePriority) {
        this.retrievePriority = DicomPriority.toCode(retrievePriority);
    }
    
    public String getStylesheetURL() {
        return stylesheetURL;
    }

    public void setStylesheetURL(String stylesheetURL) {
        this.stylesheetURL = stylesheetURL;
        reloadStylesheets();
    }

    public final ObjectName getJmsServiceName() {
        return jmsDelegate.getJmsServiceName();
    }

    public final void setJmsServiceName(ObjectName jmsServiceName) {
        jmsDelegate.setJmsServiceName(jmsServiceName);
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

    public String getRetryIntervalls() {
        return retryIntervalls.toString();
    }

    public void setRetryIntervalls(String text) {
        retryIntervalls = new RetryIntervalls(text);
    }
    
    public final String getQueueName() {
        return queueName;
    }

    public final void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public final ObjectName getHL7ServerName() {
        return hl7ServerName;
    }

    public final void setHL7ServerName(ObjectName hl7ServerName) {
        this.hl7ServerName = hl7ServerName;
    } 

    public final ObjectName getTLSConfigName() {
        return tlsConfig.getTLSConfigName();
    }

    public final void setTLSConfigName(ObjectName tlsConfigName) {
        tlsConfig.setTLSConfigName(tlsConfigName);
    }
    
    public final ObjectName getMoveScuServiceName() {
            return moveScuServiceName;
    }

    public final void setMoveScuServiceName(ObjectName moveScuServiceName) {
            this.moveScuServiceName = moveScuServiceName;
    }
    
    protected void startService() throws Exception {
        jmsDelegate.startListening(queueName, this, concurrency);
        server.addNotificationListener(hl7ServerName, this,
                HL7ServerService.NOTIF_FILTER, null);
    }

    protected void stopService() throws Exception {
        server.removeNotificationListener(hl7ServerName, this,
                HL7ServerService.NOTIF_FILTER, null);
        jmsDelegate.stopListening(queueName);
    }
    
    public void handleNotification(Notification notif, Object handback) {
        if (prefetchSourceAET == null) {
            return;
        }
        Object[] hl7msg = (Object[]) notif.getUserData();
        Document hl7doc = (Document) hl7msg[1];
        if (isORM_O01_NW(hl7doc)) {
            Dataset findRQ = DcmObjectFactory.getInstance().newDataset();
            try {
                Transformer t = getTemplates(stylesheetURL).newTransformer();
                t.transform(new DocumentSource(hl7doc), new SAXResult(findRQ
                        .getSAXHandler2(null)));
            } catch (TransformerException e) {
                log.error("Failed to transform ORM into prefetch request", e);
                return;
            }
            PrefetchOrder order = new PrefetchOrder(findRQ);
            try {
                log.info("Scheduling " + order);
                jmsDelegate.queue(queueName, order, Message.DEFAULT_PRIORITY,
                        0L);
            } catch (Exception e) {
                log.error("Failed to schedule " + order, e);
            }            
        }
    }

    private boolean isORM_O01_NW(Document hl7doc) {
        MSH msh = new MSH(hl7doc);
        return "ORM".equals(msh.messageType) && "O01".equals(msh.triggerEvent)
            && "NW".equals(hl7doc.getRootElement().element("ORC")
                    .element(HL7XMLLiterate.TAG_FIELD).getText());
    }

    public void onMessage(Message message) {
        ObjectMessage om = (ObjectMessage) message;
        try {
            PrefetchOrder order = (PrefetchOrder) om.getObject();
            log.info("Start processing " + order);
            try {
                process(order);
                log.info("Finished processing " + order);
            } catch (Exception e) {
                final int failureCount = order.getFailureCount() + 1;
                order.setFailureCount(failureCount);
                final long delay = retryIntervalls.getIntervall(failureCount);
                if (delay == -1L) {
                    log.error("Give up to process " + order, e);
                } else {
                    log.warn("Failed to process " + order
                            + ". Scheduling retry.", e);
                    jmsDelegate.queue(queueName, order, 0, System
                            .currentTimeMillis()
                            + delay);
                }
            }
        } catch (JMSException e) {
            log.error("jms error during processing message: " + message, e);
        } catch (Throwable e) {
            log.error("unexpected error during processing message: " + message,
                    e);
        }
    }

    private Templates getTemplates(String uri) {
        try {
            return (Templates) server.invoke(hl7ServerName, "getTemplates",
                    new Object[] { uri },
                    new String[] { String.class.getName() });
        } catch (Exception e) {
            String prompt = "Failed to load XSL " + uri;
            log.error(prompt, e);
            throw new RuntimeException(prompt, e);
        }
    }
    
    private void reloadStylesheets() {
        if (getState() != STARTED) return;
        try {
            server.invoke(hl7ServerName, "reloadStylesheets",
                    null, null);
        } catch (Exception e) {
            log.error("JMX error:", e);
            throw new RuntimeException("JMX error:", e);
        }
    }
    
    private void process(PrefetchOrder order) {
        // TODO Auto-generated method stub
        
    }

}
