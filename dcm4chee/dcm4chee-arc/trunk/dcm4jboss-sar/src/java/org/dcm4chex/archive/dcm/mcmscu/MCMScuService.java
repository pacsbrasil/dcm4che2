/*
 * Created on 16.12.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.dcm4chex.archive.dcm.mcmscu;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.dcm4chex.archive.ejb.interfaces.MediaComposer;
import org.dcm4chex.archive.ejb.interfaces.MediaComposerHome;
import org.dcm4chex.archive.ejb.interfaces.MediaDTO;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.dcm4chex.archive.util.JMSDelegate;
import org.jboss.system.ServiceMBeanSupport;

/**
 * @author franz.willer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MCMScuService extends ServiceMBeanSupport
	implements MessageListener {
	
	private long maxMediaUsage;
	
	private String fileSetIdPrefix;
	
	private int daysBefore;

	private static final long ONE_DAY_IN_MILLIS = 86400000;// one day has 86400000 milli seconds

	private static final long MEGA_BYTE = 1000000L;

    private static final String QUEUE = "MCMScu";

	/**
	 * Returns the prefix for FileSetID creation.
	 * 
	 * @return Returns the fileSetIdPrefix.
	 */
	public String getFileSetIdPrefix() {
		return fileSetIdPrefix;
	}
	/**
	 * Set the prefix for FileSetID creation.
	 * 
	 * @param fileSetIdPrefix The fileSetIdPrefix to set.
	 */
	public void setFileSetIdPrefix(String fileSetIdPrefix) {
		this.fileSetIdPrefix = fileSetIdPrefix;
	}
	/**
	 * Returns the max media usage for collecting studies.
	 * <p>
	 * The number of bytes, that can be used to collect studies for a media.<br>
	 * This values is usually smaller than the real media size to save space for index and 
	 * optional html files.
	 * 
	 * @return Returns the maxMediaUsage in bytes.
	 */
	public long getMaxMediaUsage() {
		return maxMediaUsage/MEGA_BYTE;
	}
	/**
	 * Set the max media usage for collecting studies.
	 * 
	 * @param maxMediaUsage The maxMediaUsage to set (in bytes).
	 */
	public void setMaxMediaUsage(long maxMediaUsage) {
		this.maxMediaUsage = maxMediaUsage*MEGA_BYTE;
	}

	/**
	 * This value is used to get the search date from current date.
	 * <p>
	 * Instances must be older than the search date.  
	 * <p>
	 * This method returns always a positive value!
	 * 
	 * @return Returns the daysBefore.
	 */
	public int getDaysBefore() {
		return daysBefore;
	}
	/**
	 * Setter for daysBefore. 
	 * 
	 * @param daysBefore The daysBefore to set.
	 */
	public void setDaysBefore(int daysBefore) {
		if ( daysBefore < 0) daysBefore *= -1;
		this.daysBefore = daysBefore;
	}
	
	public int collectStudies() {
		MediaComposer mc = null;
		try {
			MediaComposerHome home = (MediaComposerHome) EJBHomeFactory
	        .getFactory().lookup(MediaComposerHome.class,
	        		MediaComposerHome.JNDI_NAME);
			mc = home.create();
		} catch ( Exception x ) {
			log.error("Can not create MediaComposer!",x);
			return -1;
		}
		
		try {
			int size = mc.collectStudiesReceivedBefore( getSearchDate(), maxMediaUsage, getFileSetIdPrefix() );
			return size;
		} catch ( Exception x ) {
			log.error("Can not collect studies!",x);
			return -2;
		}
		
	}
	
	private long getSearchDate() {
		return System.currentTimeMillis() - ( getDaysBefore() * ONE_DAY_IN_MILLIS );
	}
	
    protected void startService() throws Exception {
        JMSDelegate.startListening(QUEUE, this);
    }

    protected void stopService() throws Exception {
        JMSDelegate.stopListening(QUEUE);
    }

    public void onMessage(Message message) {
        ObjectMessage om = (ObjectMessage) message;
        try {
            MediaDTO mediaDTO = (MediaDTO) om.getObject();
            log.info("Start processing " + mediaDTO);
            process(mediaDTO);
        } catch (JMSException e) {
            log.error("jms error during processing message: " + message, e);
        } catch (Throwable e) {
            log.error("unexpected error during processing message: " + message,
                    e);
        }
    }

    private void process(MediaDTO mediaDTO) {
        // TODO Auto-generated method stub
        
    }
}
