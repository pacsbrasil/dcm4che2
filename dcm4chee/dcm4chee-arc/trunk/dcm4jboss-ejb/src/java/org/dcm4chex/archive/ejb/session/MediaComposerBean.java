/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.ejb.session;

import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.FinderException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.dcm4che.util.UIDGenerator;
import org.dcm4chex.archive.ejb.interfaces.InstanceLocal;
import org.dcm4chex.archive.ejb.interfaces.InstanceLocalHome;
import org.dcm4chex.archive.ejb.interfaces.MediaDTO;
import org.dcm4chex.archive.ejb.interfaces.MediaLocal;
import org.dcm4chex.archive.ejb.interfaces.MediaLocalHome;
import org.dcm4chex.archive.util.InstanceCollector;
import org.dcm4chex.archive.util.InstanceCollector.InstanceContainer;

/**
 * @ejb.bean
 *  name="MediaComposer"
 *  type="Stateless"
 *  view-type="remote"
 *  jndi-name="ejb/MediaComposer"
 * 
 * @ejb.transaction-type 
 *  type="Container"
 * 
 * @ejb.transaction 
 *  type="Required"
 * 
 * @ejb.ejb-ref
 *  ejb-name="Media" 
 *  view-type="local"
 *  ref-name="ejb/Media" 
 * 
 * @ejb.ejb-ref
 *  ejb-name="Instance" 
 *  view-type="local"
 *  ref-name="ejb/Instance" 
 * 
 * @author gunter.zeilinger@tiani.com
 * @version Revision $Date$
 * @since 14.12.2004
 */

public abstract class MediaComposerBean implements SessionBean {

	private static Logger log = Logger.getLogger( MediaComposerBean.class.getName() );
	
    private MediaLocalHome mediaHome;

    private InstanceLocalHome instHome;

    public void setSessionContext(SessionContext arg0) throws EJBException,
            RemoteException {
        Context jndiCtx = null;
        try {
            jndiCtx = new InitialContext();
            mediaHome = (MediaLocalHome) jndiCtx
                    .lookup("java:comp/env/ejb/Media");
            instHome = (InstanceLocalHome) jndiCtx
                    .lookup("java:comp/env/ejb/Instance");
        } catch (NamingException e) {
            throw new EJBException(e);
        } finally {
            if (jndiCtx != null) {
                try {
                    jndiCtx.close();
                } catch (NamingException ignore) {
                }
            }
        }
    }

    public void unsetSessionContext() {
        mediaHome = null;
        instHome = null;
    }
    
    /**
     * @ejb.interface-method
     */
    public int collectStudiesReceivedBefore(long time, long maxMediaUsage, String prefix) throws FinderException {
        Collection c = instHome.findNotOnMediaAndStudyReceivedBefore(
                new Timestamp(time));
        if ( c.size() < 1 ) return 0;
        Iterator iter = c.iterator();
        InstanceLocal instance;
        InstanceCollector collector = new InstanceCollector();
        while ( iter.hasNext() ) {
        	instance = (InstanceLocal) iter.next();
        	collector.add( instance );
        }
        int nrOfStudies = collector.getNumberOfStudies();
        log.info( "Collected for storage: "+c.size()+" instances in "+nrOfStudies+" studies !");

        splitTooLargeStudies( collector, maxMediaUsage );
        
        Collection mediaCollection = mediaHome.findByStatus( MediaDTO.COLLECTING );
        MediaLocal mediaLocal;
        List instancesForMedia;
        long maxSize, collSize;
        while ( nrOfStudies > 0 ) {
        	try {
				mediaLocal = getNextMediaLocal( mediaCollection );
			} catch (CreateException e) {
				log.error("Cant create MediaLocal! skip "+nrOfStudies+" for assigning media!");
				break;
			}
        	maxSize = maxMediaUsage;//TODO use free size of media here!!!
        	log.info("Collect for maxSize:"+maxMediaUsage+" nr of studies avail:"+nrOfStudies+ " totalSize:"+collector.getTotalSize() );
        	instancesForMedia = new ArrayList();
        	collSize = collector.collectInstancesForSize( instancesForMedia, maxSize );
        	if ( collSize > 0L ){
        		maxSize -= collSize; 
	          	if ( log.isDebugEnabled() ) log.debug("Initial collected: "+collSize+" free:"+maxSize+" bytes; instancesForMedia:"+instancesForMedia);
	        	while ( collector.getSmallestStudy() != null && maxSize >= collector.getSmallestStudy().getStudySize() ) { 
	        		//collector contains one ore more studies that can be collected to current media
	        		collSize = collector.collectInstancesForSize( instancesForMedia, maxSize );
	        		if ( collSize > 0L ) {
	                	maxSize -= collSize;
	                	if ( log.isDebugEnabled() ) log.debug("Additional Collected: "+collSize );
	        		} else {
	        			break; 
	        		}
	        	} 
	        	if ( log.isDebugEnabled() ) log.debug("Final collected: free:"+maxSize+" bytes; instancesForMedia:"+instancesForMedia);
        	}
        	//media full -> assign media -> new media
        	this.assignMedia( instancesForMedia, mediaLocal );
        	
        	nrOfStudies = collector.getNumberOfStudies();
        }
        
        return c.size();
    }
    
    private MediaLocal getNextMediaLocal( Collection coll ) throws CreateException {
    	MediaLocal ml = null;
    	if ( coll.size() > 0 ) {
    		ml = (MediaLocal) coll.iterator().next();
    	} else {
			ml = mediaHome.create( UIDGenerator.getInstance().createUID() );
    	}
    	return ml;
    }
    
    private void splitTooLargeStudies(InstanceCollector collector, long maxMediaUsage ){
        InstanceContainer largest = collector.getLargestStudy();
        while ( largest != null && largest.getStudySize() > maxMediaUsage ){
        	if ( log.isInfoEnabled() ) log.info( "Study (pk="+largest.getStudyPk()+") to large ("+largest.getStudySize()+") for a single medium ("+maxMediaUsage+") !");
        	List splitList = collector.split( largest, maxMediaUsage );
        	if ( log.isInfoEnabled() ) log.info( "Study (pk="+largest.getStudyPk()+") splitted to "+splitList.size()+" media!");
        	
        	try {
        		Iterator iter = splitList.iterator();
        		InstanceContainer ic;
        		MediaLocal ml;
        		InstanceLocal il;
        		while ( iter.hasNext() ) {
        			ic = (InstanceContainer) iter.next();
    				ml = mediaHome.create( UIDGenerator.getInstance().createUID() );
    				assignMedia( ic, ml );
        		}
			} catch (Exception e) {
				log.error("Split study for storage: Cant create new MediaLocal! studyPk:"+largest.getStudyPk(), e );
			}
        	largest = collector.getLargestStudy();
        }
    }

    /**
     * Assign the given medialLocal to all instances of given InstanceContainer.
     * 
     * @param container		The InstanceContainer with a study for given media
     * @param mediaLocal	The media that will be assigned.
     */
    private void assignMedia( InstanceContainer container, MediaLocal mediaLocal ) {
		Iterator iterInstances = container.getInstances().iterator();
		InstanceLocal il;
		while ( iterInstances.hasNext() ) {
			il = (InstanceLocal) iterInstances.next();
        	if ( log.isInfoEnabled() ) log.info( "Assign media "+mediaLocal.getFilesetIuid()+
        										" to instance "+il.getSopIuid()+
												" for splitted study pk="+container.getStudyPk()+"!");
			il.setMedia( mediaLocal );
		}
    }

    /**
     * Assign the given MediaLocal to all instances of given collection of InstanceContainer(studies).
     * 
     * @param studies		Collection of InstanceContainer.
     * @param mediaLocal	The media that will be assigned.
     */
    private void assignMedia( Collection studies, MediaLocal mediaLocal) {
    	InstanceContainer ic;
    	Iterator iter = studies.iterator();
    	while ( iter.hasNext() ) {
    		ic = (InstanceContainer) iter.next();
    		assignMedia( ic, mediaLocal );
    	}
    }
    
    /**
     * @ejb.interface-method
     */
    public List getWithStatus(int status) throws FinderException {
        return toMediaDTOs(mediaHome.findByStatus(status));
    }
        
    private List toMediaDTOs(Collection c) {
        ArrayList list = new ArrayList();
        for (Iterator it = c.iterator(); it.hasNext();) {
            list.add(toMediaDTO((MediaLocal) it.next()));
        }
        return list;
    }

    private MediaDTO toMediaDTO(MediaLocal media) {
        MediaDTO dto = new MediaDTO();
        dto.setPk(media.getPk().intValue());
        dto.setCreatedTime(media.getCreatedTime());
        dto.setUpdatedTime(media.getUpdatedTime());
        dto.setMediaUsage(media.getMediaUsage());
        dto.setMediaStatus(media.getMediaStatus());
        dto.setFilesetId(media.getFilesetId());
        dto.setFilesetIuid(media.getFilesetIuid());
        dto.setMediaCreationRequestIuid(media.getMediaCreationRequestIuid());
        return dto;
    }    

    /**
     * @ejb.interface-method
     */
    public void setMediaCreationRequestIuid(int pk, String iuid) throws FinderException {
        MediaLocal media = mediaHome.findByPrimaryKey(new Integer(pk));
        media.setMediaCreationRequestIuid(iuid);
    }

    /**
     * @ejb.interface-method
     */
    public void setMediaStatus(int pk, int status) throws FinderException {
        MediaLocal media = mediaHome.findByPrimaryKey(new Integer(pk));
        media.setMediaStatus(status);
    }
}