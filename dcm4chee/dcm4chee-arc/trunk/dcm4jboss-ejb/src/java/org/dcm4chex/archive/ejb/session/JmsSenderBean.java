package org.dcm4chex.archive.ejb.session;

import java.io.Serializable;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;
import java.util.Map.Entry;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.dcm4chex.archive.common.BaseJmsOrder;
import org.dcm4chex.archive.ejb.interfaces.MessageCreator;

/**
 * Helper stateless session bean to send JMS messages.
 *
 * @author fang.yang@agfa.com
 * @version $Id$
 * @since April 4, 2006

 * @ejb.bean name="JmsSender"
 *           type="Stateless"
 *           display-name="Jms Sender helpers"
 *           jndi-name="ejb/JmsSender"
 *           local-jndi-name="ejb/JmsSenderLocal"
 *           view-type="both"
 *
 * @ejb.transaction type = "Required"
 *
 * @ejb.util generate = "physical"
 *
 *
 */
public class JmsSenderBean implements SessionBean {

	private static final long serialVersionUID = -7231087561749921446L;
	private static final Logger log = Logger.getLogger(JmsSenderBean.class);
		
	private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(JmsSenderBean.class.getName());

    private SessionContext _ctx;
    private transient Context _jndi;
    private transient QueueConnectionFactory queueConnectionFactory;
    private transient TopicConnectionFactory topicConnectionFactory;
    public static final String CONNECTION_FACTORY = "java:ConnectionFactory";
    private static final String JNDI_PROVIDER_URL = "jnp://localhost:1100";
    private static final Properties p = new java.util.Properties();
    private static final int MAX_RETRY = 30;
    private static final long RETRY_INTERVAL = 3000; // ms

    private int maxRetries = MAX_RETRY;
    private long retryInterval = RETRY_INTERVAL;

    static
    {
    	p.put(Context.PROVIDER_URL, JNDI_PROVIDER_URL);    	
    }
    
    /**
     * Creates a new instance.
     *
     * @throws javax.ejb.CreateException if the bean couldn't be created
     * @ejb.create-method
     */
    public void ejbCreate() throws CreateException {
    }

    public void setSessionContext(SessionContext context) {
        this._ctx = context;
        try {
        	init();
        } catch (NamingException e) {
        	log.error(e);
        }
    }

    /**
     * Publish a message on a topic.
     *
     * @ejb.interface-method
     * @param messageCreator MessageCreator to create the message with.
     * @param topic The topic to publish on.
     */
    public void publish(MessageCreator messageCreator, Topic topic) throws JMSException {
        if (logger.isDebugEnabled()) {
            logger.debug("Publishing message [" + messageCreator + "] on topic [" + topic.getTopicName() + "]");
        }

        TopicConnection connection = null;
        TopicSession session = null;
        TopicPublisher publisher = null;

        for ( int attempts = 0 ; attempts <= maxRetries ; attempts++)
        {
	        try {
	        	init();
	            connection = openTopicConnection();
	            session = connection.createTopicSession(false, TopicSession.AUTO_ACKNOWLEDGE);
	            publisher = session.createPublisher(topic);
	            publisher.publish(messageCreator.getMessage(session));
	            return;
	        } 
	        catch (NamingException e) {
	            log.error( e + " - retry: " + (attempts+1));
	            
	            try {
					Thread.sleep(retryInterval);
				} catch (InterruptedException e1) {}
	        } 
	        finally {
	            closeTopicPublisher(publisher);
	            closeSession(session);
	            closeConnection(connection);
	        }
        }
        throw new JMSException("Give up publishing JMS topic message. Detailed exception should already be provided.");
    }

    /**
     * Publish text on a topic.
     *
     * @ejb.interface-method
     * @param message The message to publish.
     * @param topic The topic to publish on.
     */
    public void publish(String message, Topic topic) throws JMSException {
        if (logger.isDebugEnabled()) {
            logger.debug("Publishing text [" + message + "] on topic [" + topic.getTopicName() + "]");
        }

        publish(new TextMessageCreator(message), topic);
    }

    /**
     * Publish an object on a topic.
     *
     * @ejb.interface-method
     * @param message The message to publish.
     * @param topic The topic to publish on.
     */
    public void publish(Object message, Topic topic) throws JMSException {
        if (logger.isDebugEnabled()) {
            logger.debug("Publishing object [" + message + "] on topic [" + topic.getTopicName() + "]");
        }

        publish(new ObjectMessageCreator(message), topic);
    }


   /**
     * Send a Message to a queue with a scheduled delivery and returns the ID assigned by
     * the JMS provider.
     *
     * @ejb.interface-method
     * @param messageCreator MessageCreator to create the message with.
     * @param queue The queue to send to.
     * @param deliveryDate the date at which the message should be delivered. If null, no delivery date is set
     * @param properties the properties for this message
     * @return the JMSMessageID asssigned to this message.
     */
    public String send(MessageCreator messageCreator, Queue queue, Date deliveryDate, Properties properties) throws JMSException {
        if (logger.isDebugEnabled()) {
            logger.debug("Sending message [" + messageCreator + "] to queue [" + queue.getQueueName() + "]");
        }        

        QueueConnection connection = null;
        QueueSession session = null;
        QueueSender sender = null;

        for ( int attempts = 0 ; attempts <= maxRetries ; attempts++)
        {
	        try {
	        	init();
	            connection = openQueueConnection();
	            session = connection.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);
	            sender = session.createSender(queue);
	            Message msg = messageCreator.getMessage(session);
	
	            // If applicable, record the queueName in the object
	            if(msg instanceof ObjectMessage
	            	&& ((ObjectMessage)msg).getObject() instanceof BaseJmsOrder)
	            	((BaseJmsOrder)((ObjectMessage)msg).getObject()).setQueueName(queue.getQueueName());

	            if (deliveryDate != null) {
	                logger.debug("Message will be delivered on ["+deliveryDate+"]");
	                msg.setLongProperty("JMS_JBOSS_SCHEDULED_DELIVERY", deliveryDate.getTime());
	            }
	            
	            int priority = ObjectMessage.DEFAULT_PRIORITY;
	            long expiration = ObjectMessage.DEFAULT_TIME_TO_LIVE;
	
	            // Add properties if available
	            if(properties != null)
	            {
	            	// Check if there are any known properties that we care
	            	if(properties.containsKey("JMSExpiration"))
	            		expiration = Long.parseLong(properties.remove("JMSExpiration").toString());
	            	if(properties.containsKey("JMSPriority"))
	            		priority = Integer.parseInt(properties.remove("JMSPriority").toString());
	            	if(properties.containsKey("JMSType"))
	            		msg.setJMSType((String)properties.remove("JMSType"));
	            	
	            	// Otherwise, add all
	            	Iterator it = properties.entrySet().iterator();
	            	while(it.hasNext())
	            	{
	            		Entry entry = (Entry)it.next();
	            		msg.setObjectProperty((String)entry.getKey(), entry.getValue());
	            	}
	            }
	            
	            sender.send(msg,
	            		DeliveryMode.PERSISTENT,
	                    priority,
	                    expiration);
	            
	            return msg.getJMSMessageID();
	        } 
	        catch (NamingException e) {
	            log.error( e + " - retry: " + (attempts+1));
	            
	            try {
					Thread.sleep(retryInterval);
				} catch (InterruptedException e1) {}
	        } 
	        finally {
	            closeQueueSender(sender);
	            closeSession(session);
	            closeConnection(connection);
	        }
        }
        throw new JMSException("Give up sending JMS Queue message. Detailed exception should already be provided.");

    }
    
    /**
     * Send a Message to a queue and returns the ID assigned by the JMS provider.
     *
     * @ejb.interface-method
     * @param messageCreator MessageCreator to create the message with.
     * @param queue The queue to send to.
     * @return the JMSMessageID asssigned to this message.
     */
    public String send(MessageCreator messageCreator, Queue queue, Properties properties) throws JMSException {
        return send(messageCreator, queue, null, properties);
    }

    /**
     * Send text to a queue and returns the ID assigned by the JMS provider.
     *
     * @ejb.interface-method
     * @param message The message to publish.
     * @param queue The queue to send to.
     * @return the JMSMessageID asssigned to this message.
     */
    public String send(String message, Queue queue) throws JMSException {
        return send(new TextMessageCreator(message), queue);
    }

    /**
     * Send an Object to a queue and returns its ID assigned by the JMS provider.
     *
     * @ejb.interface-method
     * @param message The message to publish.
     * @param queue The queue to send to.
     * @return the JMSMessageID asssigned to this message.
     */
    public String send(Object message, Queue queue, Properties properties) throws JMSException {
        return send(new ObjectMessageCreator(message), queue, properties);
    }
    
    /**
     * Send an Object to a queue and returns its ID assigned by the JMS provider.
     *
     * @ejb.interface-method
     * @param message The message to publish.
     * @param queue The queue to send to.
     * @return the JMSMessageID asssigned to this message.
     */
    public String send(Object message, Queue queue) throws JMSException {
        return send(new ObjectMessageCreator(message), queue, null);
    }
    
    /**
     * Send an Object to a queue and returns its ID assigned by the JMS provider.
     *
     * @ejb.interface-method
     * @param message The message to publish.
     * @param queueName The name of the queue to send to.
     * @return the JMSMessageID asssigned to this message.
     */
    public String send(Object message, String queueName, Properties properties) throws JMSException {
    	return send(message, getQueue(queueName), properties);
    }
    
    /**
     * Send an Object to a queue and returns its ID assigned by the JMS provider.
     *
     * @ejb.interface-method
     * @param message The message to publish.
     * @param queueName The name of the queue to send to.
     * @return the JMSMessageID asssigned to this message.
     */
    public String send(Object message, String queueName) throws JMSException {
    	return send(message, getQueue(queueName), null);
    }
    
    private Queue getQueue(String queueName) throws JMSException {
    	try
    	{
    		Queue queue = (Queue) _jndi.lookup("queue/" + queueName);
    		return queue;
    	}
    	catch(Exception e)
    	{
    		logger.error("Failed to look up queue: " + queueName, e);
    		throw new JMSException("Failed to look up queue: " + queueName);
    	}
    }

    public void ejbActivate() {
        try {
            init();
        } catch (NamingException e) {
            throw new EJBException("Could not activate", e);
        }
    }

    public void ejbPassivate() {
    }

    public void ejbRemove() {
    }

    /**
     * Initializes the bean.
     */
    private void init() throws NamingException {
        _jndi = new InitialContext();
        queueConnectionFactory = (QueueConnectionFactory) _jndi.lookup(CONNECTION_FACTORY);
        //topicConnectionFactory = (TopicConnectionFactory) _jndi.lookup("java:comp/env/jms/TopicConnectionFactory");
    }

    /**
     * Retrieves a new JMS Connection from the pool
     * @return a <code>QueueConnection</code>
     * @throws JMSException if the connection could not be retrieved
     */
    private QueueConnection openQueueConnection() throws JMSException {
        return queueConnectionFactory.createQueueConnection();
       // queueConnection.start(); this is a pool we don't need to start the connection
    }

    /**
     * Retrieves a new JMS Connection from the pool
     * @return a <code>QueueConnection</code>
     * @throws JMSException if the connection could not be retrieved
     */
    private TopicConnection openTopicConnection() throws JMSException {
        return topicConnectionFactory.createTopicConnection();
    }

    /**
     * Closes the JMS connection.
     */
    private void closeConnection(Connection connection) {
        try {
            if (connection != null)
                connection.close();
        } catch (JMSException e) {
            logger.warn("Could not close JMS connection", e);
        }
    }

    /**
     * Closes the JMS session.
     */
    private void closeSession(Session session) {
        try {
            if (session != null)
                session.close();
        } catch (JMSException e) {
            logger.warn("Could not close JMS session", e);
        }
    }

    /**
     * Closes the JMS session.
     */
    private void closeQueueSender(QueueSender queueSender) {
        try {
            if (queueSender!= null)
                queueSender.close();
        } catch (JMSException e) {
            logger.warn("Could not close queue sender", e);
        }
    }

    /**
     * Closes the JMS session.
     */
    private void closeTopicPublisher(TopicPublisher topicPublisher) {
        try {
            if (topicPublisher != null)
                topicPublisher.close();
        } catch (JMSException e) {
            logger.warn("Could not close queue sender", e);
        }
    }

    private class TextMessageCreator implements MessageCreator {
        private String message;

        public TextMessageCreator(String message) {
            this.message = message;
        }

        public Message getMessage(Session session) throws JMSException {
            return session.createTextMessage(message);
        }

        public String toString() {
            return message;
        }
    }

    private class ObjectMessageCreator implements MessageCreator {
        private Serializable message;

        public ObjectMessageCreator(Object message) throws JMSException {
            if (!(message instanceof Serializable)) {
                throw new JMSException("Object ["+message+"] is not serializable");
            }

            this.message = (Serializable) message;
        }

        public Message getMessage(Session session) throws JMSException {
            return session.createObjectMessage(message);
        }

        public String toString() {
            if (message != null)
                return message.toString();
            else
                return "null";
        }
    }
}
