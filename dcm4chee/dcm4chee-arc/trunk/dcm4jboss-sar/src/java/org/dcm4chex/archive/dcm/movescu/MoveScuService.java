/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.dcm.movescu;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.QueueSession;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.dcm4chex.archive.exceptions.ConfigurationException;
import org.dcm4chex.archive.util.JMSDelegate;
import org.jboss.system.ServiceMBeanSupport;

import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision$ $Date$
 * @since 17.12.2003
 */
public class MoveScuService extends ServiceMBeanSupport implements
        MessageListener {

    private static final String DEFAULT_AET = "MOVE_SCU";

    private String aet = DEFAULT_AET;

    private String dsJndiName = "java:/DefaultDS";

    private DataSource datasource;

    private QueueSession jmsSession;

    private RetryIntervalls retryIntervalls = new RetryIntervalls();
    
    private PooledExecutor pool = new PooledExecutor();
    
    public final String getDataSourceJndiName() {
        return dsJndiName;
    }

    public final void setDataSourceJndiName(String jndiName) {
        this.dsJndiName = jndiName;
    }
    
    public final int getConcurrency() {
        return pool.getMaximumPoolSize();
    }

    public final void setConcurrency(int concurrency) {
        pool.setMaximumPoolSize(concurrency);
    }
    
    public DataSource getDataSource() throws ConfigurationException {
        if (datasource == null) {
            try {
                Context jndiCtx = new InitialContext();
                try {
                    datasource = (DataSource) jndiCtx.lookup(dsJndiName);
                } finally {
                    try {
                        jndiCtx.close();
                    } catch (NamingException ignore) {
                    }
                }
            } catch (NamingException ne) {
                throw new ConfigurationException(
                        "Failed to access Data Source: " + dsJndiName, ne);
            }
        }
        return datasource;
    }

    public String getRetryIntervalls() {
        return retryIntervalls.toString();
    }

    public void setRetryIntervalls(String text) {
        retryIntervalls = new RetryIntervalls(text);
    }

    public String getAET() {
        return aet;
    }

    public void setAET(String aet) {
        this.aet = aet;
    }    

    protected void startService() throws Exception {
        if (this.jmsSession != null) {
            log.warn("Closing existing JMS Session for receiving messages");
            this.jmsSession.close();
            this.jmsSession = null;
        }
        this.jmsSession = JMSDelegate.getInstance(MoveOrder.QUEUE)
                .setMessageListener(this);
    }

    protected void stopService() throws Exception {
        if (jmsSession != null) {
            jmsSession.close();
            jmsSession = null;
        }
    }

    void queueFailedMoveOrder(MoveOrder order) {
        final long delay = retryIntervalls
                .getIntervall(order.getFailureCount());
        if (delay == -1L) {
            log.error("Give up to process move order: " + order);
        } else {
            log.warn("Failed to process " + order + ". Scheduling retry.");
            try {
                JMSDelegate.getInstance(MoveOrder.QUEUE).queueMessage(order,
                        0,
                        System.currentTimeMillis() + delay);
            } catch (JMSException e) {
                log.error("Failed to reschedule order: " + order);
            }
        }
    }

    public void onMessage(Message message) {
        ObjectMessage om = (ObjectMessage) message;
        MoveOrder order = null;
        try {
            pool.execute(new MoveCmd(this, order = (MoveOrder) om.getObject()));
        } catch (JMSException e) {
            log.error("jms error during processing message: " + message, e);
        } catch (InterruptedException e) {
            log.error("Failed to process " + order, e);
        }
    }
}