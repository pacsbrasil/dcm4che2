/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.cdw.common;

import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * @author gunter.zeilinter@tiani.com
 * @version $Revision$ $Date$
 * @since 23.06.2004
 *
 */
public class JMSDelegate {

    static final String CONNECTION_FACTORY = "java:ConnectionFactory";

    static final String COMPOSER_QUEUE = "queue/MediaComposer";

    static final String MKISOFS_QUEUE = "queue/MakeIsoImage";

    static final String WRITER_QUEUE = "queue/MediaWriter";

    static int toJMSPriority(String dcmPriority) {
        if (dcmPriority.equals(Priority.LOW)) return 3;
        if (dcmPriority.equals(Priority.HIGH)) return 5;
        return 4;
    }

    private static JMSDelegate instance;

    public synchronized static JMSDelegate getInstance() {
        if (instance == null) instance = new JMSDelegate();
        return instance;
    }

    private QueueConnection conn;

    private Queue composerQueue;

    private Queue mkisofsQueue;

    private Queue writerQueue;

    private QueueReceiver composerReceiver;

    private QueueReceiver mkisofsReceiver;

    private QueueReceiver writerReceiver;

    private JMSDelegate() {
        InitialContext iniCtx = null;
        try {
            iniCtx = new InitialContext();
            Object tmp = iniCtx.lookup(CONNECTION_FACTORY);
            QueueConnectionFactory qcf = (QueueConnectionFactory) tmp;
            conn = qcf.createQueueConnection();
            composerQueue = (Queue) iniCtx.lookup(COMPOSER_QUEUE);
            mkisofsQueue = (Queue) iniCtx.lookup(MKISOFS_QUEUE);
            writerQueue = (Queue) iniCtx.lookup(WRITER_QUEUE);
            QueueSession composerSession = conn.createQueueSession(false,
                    QueueSession.AUTO_ACKNOWLEDGE);
            QueueSession mkisofsSession = conn.createQueueSession(false,
                    QueueSession.AUTO_ACKNOWLEDGE);
            QueueSession writerSession = conn.createQueueSession(false,
                    QueueSession.AUTO_ACKNOWLEDGE);
            composerReceiver = composerSession.createReceiver(composerQueue);
            mkisofsReceiver = mkisofsSession.createReceiver(mkisofsQueue);
            writerReceiver = writerSession.createReceiver(writerQueue);
            conn.start();
        } catch (NamingException e) {
            throw new ConfigurationException(e);
        } catch (JMSException e) {
            throw new ConfigurationException(e);
        } finally {
            if (iniCtx != null) {
                try {
                    iniCtx.close();
                } catch (Exception ignore) {
                }
            }
        }
    }

    public void setMediaWriterListener(MessageListener listener) {
        try {
            writerReceiver.setMessageListener(listener);
        } catch (JMSException e) {
            throw new ConfigurationException(e);
        }
    }

    public void setMediaComposerListener(MessageListener listener) {
        try {
            composerReceiver.setMessageListener(listener);
        } catch (JMSException e) {
            throw new ConfigurationException(e);
        }
    }

    public void setMakeIsoImageListener(MessageListener listener) {
        try {
            mkisofsReceiver.setMessageListener(listener);
        } catch (JMSException e) {
            throw new ConfigurationException(e);
        }
    }

    public void queueForMediaComposer(MediaCreationRequest rq)
            throws JMSException {
        queueFor(rq, composerQueue);
    }

    public void queueForMediaWriter(MediaCreationRequest rq)
            throws JMSException {
        queueFor(rq, writerQueue);
    }

    public void queueForMakeIsoImage(MediaCreationRequest rq)
            throws JMSException {
        queueFor(rq, mkisofsQueue);
    }

    private void queueFor(MediaCreationRequest request, Queue queue)
            throws JMSException {
        QueueSession session = null;
        QueueSender send = null;
        try {
            session = conn.createQueueSession(false,
                    QueueSession.AUTO_ACKNOWLEDGE);
            send = session.createSender(queue);
            ObjectMessage msg = session.createObjectMessage(request);
            send.send(msg, DeliveryMode.PERSISTENT, toJMSPriority(request
                    .getPriority()), 0);
        } finally {
            if (send != null) {
                try {
                    send.close();
                } catch (Exception ignore) {
                }
            }
            if (session != null) {
                try {
                    session.close();
                } catch (Exception ignore) {
                }
            }
        }
    }
}
