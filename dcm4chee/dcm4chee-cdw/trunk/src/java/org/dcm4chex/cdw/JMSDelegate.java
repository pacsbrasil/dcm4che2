/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.cdw;

import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.TextMessage;
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

    static final String PENDING_QUEUE = "queue/Pending";

    static final String PROPERTY_SCHEDULED_DELIVERY = "JMS_JBOSS_SCHEDULED_DELIVERY";

    static final String PROPERTY_RETRY = "RETRY";

    private static JMSDelegate instance;

    public synchronized static JMSDelegate getInstance() {
        if (instance == null) instance = new JMSDelegate();
        return instance;
    }

    private QueueConnection conn;

    private Queue pendingQueue;

    private QueueReceiver pendingReceiver;

    private JMSDelegate() {
        InitialContext iniCtx = null;
        try {
            iniCtx = new InitialContext();
            Object tmp = iniCtx.lookup(CONNECTION_FACTORY);
            QueueConnectionFactory qcf = (QueueConnectionFactory) tmp;
            conn = qcf.createQueueConnection();
            pendingQueue = (Queue) iniCtx.lookup(PENDING_QUEUE);
            QueueSession inqs = conn.createQueueSession(false,
                    QueueSession.AUTO_ACKNOWLEDGE);
            QueueSession outqs = conn.createQueueSession(false,
                    QueueSession.AUTO_ACKNOWLEDGE);
            pendingReceiver = inqs.createReceiver(pendingQueue);
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

    public void listenPending(MessageListener listener) {
        try {
            pendingReceiver.setMessageListener(listener);
        } catch (JMSException e) {
            throw new ConfigurationException(e);
        }
    }

    public void queuePending(String iuid, boolean persistentDeliveryMode,
            int priority, int retry, long delay) throws JMSException {
        QueueSession session = null;
        QueueSender send = null;
        try {
            session = conn.createQueueSession(false,
                    QueueSession.AUTO_ACKNOWLEDGE);
            send = session.createSender(pendingQueue);
            TextMessage msg = session.createTextMessage(iuid);
            msg.setIntProperty(PROPERTY_RETRY, retry);
            if (delay > 0) {
                long now = System.currentTimeMillis();
                msg.setLongProperty(PROPERTY_SCHEDULED_DELIVERY, now + delay);
            }
            send.send(msg, persistentDeliveryMode ? DeliveryMode.PERSISTENT
                    : DeliveryMode.NON_PERSISTENT, priority, 0);
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
