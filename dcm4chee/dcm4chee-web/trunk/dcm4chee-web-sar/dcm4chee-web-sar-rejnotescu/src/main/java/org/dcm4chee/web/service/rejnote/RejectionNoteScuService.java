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
package org.dcm4chee.web.service.rejnote;

import java.io.IOException;
import java.security.GeneralSecurityException;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.management.ObjectName;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;
import org.dcm4che2.data.UIDDictionary;
import org.dcm4che2.net.Association;
import org.dcm4che2.net.DataWriterAdapter;
import org.dcm4che2.net.DimseRSPHandler;
import org.dcm4che2.net.NoPresentationContextException;
import org.dcm4che2.net.TransferCapability;
import org.dcm4che2.util.StringUtils;
import org.dcm4chee.web.service.common.AbstractScuService;
import org.dcm4chee.web.service.common.JMSDelegate;
import org.dcm4chee.web.service.common.RetryIntervalls;

/**
 * @author franz.willer@gmail.com
 * @version $Revision$ $Date$
 * @since Feb 11, 2010
 */
public class RejectionNoteScuService extends AbstractScuService implements MessageListener {

    private String[] calledAETs;
    private JMSDelegate jmsDelegate = new JMSDelegate(this);
    private RetryIntervalls retryIntervalls = new RetryIntervalls();
    private static int MESSAGE_PRIORITY_MIN = 0;
    private int concurrency;
    private String queueName;
    
    public RejectionNoteScuService() {
        super();
        TransferCapability tc = new TransferCapability(
                UID.KeyObjectSelectionDocumentStorage,
                NATIVE_LE_TS, TransferCapability.SCU);
        setTransferCapability(new TransferCapability[]{tc});
    }

    public String getCalledAETs() {
        return calledAETs == null ? NONE : StringUtils.join(calledAETs, '\\');
    }

    public void setCalledAETs(String calledAET) {
        this.calledAETs = NONE.equals(calledAET) ? null : StringUtils.split(calledAET, '\\');
    }

    public final String getRetryIntervalls() {
        return retryIntervalls.toString();
    }

    public final void setRetryIntervalls(String s) {
        this.retryIntervalls = new RetryIntervalls(s);
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
    
    public void sendRejectionNote(DicomObject kos) throws Exception {
        if (calledAETs != null) {
            for ( String aet : calledAETs) {
                jmsDelegate.queue(queueName, new RejectionNoteOrder(aet,kos), Message.DEFAULT_PRIORITY, 0);
            }
        }
    }
    
    private int sendRejectionNote(String aet, DicomObject kos) throws IOException, InterruptedException, GeneralSecurityException {
        Association assoc = open(aet);
        TransferCapability tc = assoc.getTransferCapabilityAsSCU(UID.KeyObjectSelectionDocumentStorage);
        if ( tc == null ) {
            throw new NoPresentationContextException(UIDDictionary.getDictionary().prompt(UID.KeyObjectSelectionDocumentStorage));
        }
        
        String cuid = tc.getSopClass();
        String tsuid = tc.getTransferSyntax()[0];
        LOG.debug("Send C-STORE request for Rejection Note to {}:\n{}", aet, kos);
        RspHandler rspHandler = new RspHandler();
        assoc.cstore(cuid, kos.getString(Tag.SOPInstanceUID), priority, 
                new DataWriterAdapter(kos), tsuid, rspHandler);
        assoc.waitForDimseRSP();
        try {
            assoc.release(true);
        } catch (InterruptedException t) {
            LOG.error("Association release failed! aet:"+aet, t);
        }
        return rspHandler.getStatus();
    }


    public void onMessage(Message message) {
        ObjectMessage om = (ObjectMessage) message;
        try {
            RejectionNoteOrder order = (RejectionNoteOrder) om.getObject();
            log.info("Start processing " + order);
            try {
                this.sendRejectionNote(order.getDestAET(), order.getRejectionNote());
                log.info("Finished processing " + order);
            } catch (Exception e) {
                order.setThrowable(e);
                final int failureCount = order.getFailureCount() + 1;
                order.setFailureCount(failureCount);
                final long delay = retryIntervalls.getIntervall(failureCount);
                if (delay == -1L) {
                    log.error("Give up to process " + order, e);
                    jmsDelegate.fail(queueName, order);
                } else {
                    log.warn("Failed to process " + order + ". Scheduling retry.", e);
                    jmsDelegate.queue(queueName, order, MESSAGE_PRIORITY_MIN, 
                            System.currentTimeMillis() + delay);
                }
            }
        } catch (JMSException e) {
            log.error("jms error during processing message: " + message, e);
        } catch (Throwable e) {
            log.error("unexpected error during processing message: " + message,
                    e);
        }
    }
        
    private class RspHandler extends DimseRSPHandler {
        private int status;

        public int getStatus() {
            return status;
        }

        @Override
        public void onDimseRSP(Association as, DicomObject cmd,
                DicomObject data) {
            int status = cmd.getInt(Tag.Status);
            switch (status) {
            case 0:
                LOG.debug("Rejection Note KOS stored at {}",as.getCalledAET());
                break;
            case 0xB000:
            case 0xB006:
            case 0xB007:
                LOG.warn("Rejection Note KOS stored at {} with Status {}H",as.getCalledAET(), StringUtils.shortToHex(status));
                break;
            default:
                LOG.error("Sending Rejection Note KOS failed with Status {}H at calledAET:{}", StringUtils.shortToHex(status), as.getCalledAET());
                System.out.print('F');
            }
        }
    }

}

