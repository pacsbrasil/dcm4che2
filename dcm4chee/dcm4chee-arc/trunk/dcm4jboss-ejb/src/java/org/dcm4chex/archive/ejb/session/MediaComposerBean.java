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
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

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
import org.dcm4chex.archive.ejb.interfaces.FileLocal;
import org.dcm4chex.archive.ejb.interfaces.InstanceLocal;
import org.dcm4chex.archive.ejb.interfaces.InstanceLocalHome;
import org.dcm4chex.archive.ejb.interfaces.MediaDTO;
import org.dcm4chex.archive.ejb.interfaces.MediaLocal;
import org.dcm4chex.archive.ejb.interfaces.MediaLocalHome;
import org.dcm4chex.archive.ejb.util.InstanceCollector;
import org.dcm4chex.archive.ejb.util.InstanceCollector.InstanceContainer;

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
     * Collect studies to media for storage.
     * <p>
     * <DL>
     * <DD>1) Find all instances that are not assigned to a media and are older as <code>time</code></DD>
     * <DD>2) collect instances to studies.</DD>
     * <DD>3) collect studies for media</DD>
     * <DD>4) assign media to studies</DD>
     * </DL>
     * @param time 			Timestamp: instances must be received before this timestamp.
     * @param maxMediaUsage	The number of bytes that can be used to store instances on a media.
     * @param prefix		Prefix for the FileSet id. Used if a new media object is created.
     * 
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

        splitTooLargeStudies( collector, maxMediaUsage, prefix );
        
        List mediaCollection = (List) mediaHome.findByStatus( MediaDTO.COLLECTING );
        Comparator comp = new Comparator() {
    		public int compare(Object arg0, Object arg1) {
    			MediaLocal ml1 = (MediaLocal) arg0;
    			MediaLocal ml2 = (MediaLocal) arg1;
    			return (int) ( ml2.getMediaUsage() - ml1.getMediaUsage() );//more usage before lower usage!
    		}
    	};
        Collections.sort( mediaCollection, comp );
        log.info("Number of 'COLLECTING' media found:"+mediaCollection.size() );
        MediaLocal mediaLocal;
        List instancesForMedia;
        long maxSize, collSize;
        while ( nrOfStudies > 0 ) {
        	try {
				mediaLocal = getNextMediaLocal( mediaCollection, prefix );
			} catch (CreateException e) {
				log.error("Cant create MediaLocal! skip "+nrOfStudies+" for assigning media!");
				break;
			}
        	maxSize = maxMediaUsage - mediaLocal.getMediaUsage();
        	if ( log.isDebugEnabled() ) log.debug("Collect for media:"+mediaLocal.getFilesetId()+" free:"+maxSize+" - Number of studies avail:"+nrOfStudies+ " totalSize:"+collector.getTotalSize() );
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
	        	if ( log.isDebugEnabled() ) log.debug("Final collected for media:"+mediaLocal.getFilesetId()+" free:"+maxSize+" bytes; instancesForMedia:"+instancesForMedia);
        	}
        	//media full -> assign media -> new media
        	this.assignMedia( instancesForMedia, mediaLocal );
        	
        	nrOfStudies = collector.getNumberOfStudies();
        }
        
        return c.size();
    }
    
    /**
     * Returns the next MediaLocal object from <code>col</code>.
     * <p>
     * If <code>col</code> is empty, a new MediaLocal is created.
     * <p>
     * In the other case the returning MediaLocal object is removed from the collection.
     *  
     * @param coll 	Collection with MediaLocal objects.
     * 
     * @return		The next MediaLocal object from co or a new created MediaLocal.
     * 
     * @throws CreateException if create of a new MediaLocal failed.
     */
    private MediaLocal getNextMediaLocal( Collection coll, String prefix ) throws CreateException {
    	MediaLocal ml = null;
    	if ( coll.size() > 0 ) {
    		ml = (MediaLocal) coll.iterator().next();
    		coll.remove( ml );
    	} else {
			ml = createMedia( prefix );
    	}
    	return ml;
    }
    
    /**
     * Checks the <code>collector</code> for studies with a size greater than <code>maxMediaUsage</code>.
     * <p>
     * If the collector contains such studies, they will be splitted and assigned to new created media objects.
     * 
     * @param collector		The InstanceCollector with instances collected to studies.
     * @param maxMediaUsage	Max number of bytes that can be used to store instances on a media.
     * @param prefix		Prefix for FileSet id.
     */
    private void splitTooLargeStudies(InstanceCollector collector, long maxMediaUsage, String prefix ){
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
        			ml = createMedia( prefix );
    				assignMedia( ic, ml );
        		}
			} catch (Exception e) {
				log.error("Split study for storage: Cant create new MediaLocal! studyPk:"+largest.getStudyPk(), e );
			}
        	largest = collector.getLargestStudy();
        }
    }

    /**
	 * @param prefix
	 * @return
     * @throws CreateException
	 */
	private MediaLocal createMedia(String prefix) throws CreateException {
		MediaLocal ml = mediaHome.create( UIDGenerator.getInstance().createUID() );
		ml.setFilesetId( prefix+ml.getPk() );
		ml.setMediaStatus( MediaDTO.COLLECTING );
		if ( log.isInfoEnabled() ) log.info("New media created:"+ml.getFilesetId() );
		return ml;
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
        	if ( log.isDebugEnabled() ) log.debug( "Assign media "+mediaLocal.getFilesetId()+
        										" to instance pk="+il.getPk()+"("+il.getSopIuid()+
												") of study pk="+container.getStudyPk()+"!");
			il.setMedia( mediaLocal );
			mediaLocal.setMediaUsage( mediaLocal.getMediaUsage() + getInstanceSize( il ) );
		}
    }
	/**
	 * Returns the size of the given instance.
	 * <p>
	 * If the instance contains more than one file, the size of the latest (with highest pk) file is used.
	 * <p>
	 * Copy of InstanceCollector.getInstanceSize method!!
	 * 
	 * @param instance The instance
	 * 
	 * @return The file size of the instance.
	 */
	private int getInstanceSize(InstanceLocal instance) {
		Collection col = instance.getFiles();
		if ( col.size() == 1 ) { FileLocal l;
			return ( (FileLocal) col.iterator().next() ).getFileSize();
		} else {
			int size = 0;
			int pk = Integer.MIN_VALUE;
			Iterator iter = col.iterator();
			FileLocal file;
			while ( iter.hasNext() ) {
				file = (FileLocal) iter.next();
				if ( file.getPk().intValue() > pk ) {
					pk = file.getPk().intValue();
					size = file.getFileSize();
				}
			}
			return size;
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
        dto.setMediaStatusInfo(media.getMediaStatusInfo());
        dto.setFilesetId(media.getFilesetId());
        dto.setFilesetIuid(media.getFilesetIuid());
        dto.setMediaCreationRequestIuid(media.getMediaCreationRequestIuid());
        return dto;
    }    

    /**
     * @ejb.interface-method
     */
    public void setMediaCreationRequestIuid(int pk, String iuid)
    		throws FinderException {
        MediaLocal media = mediaHome.findByPrimaryKey(new Integer(pk));
        media.setMediaCreationRequestIuid(iuid);
    }

    /**
     * @ejb.interface-method
     */
    public void setMediaStatus(int pk, int status, String info)
    		throws FinderException {
        MediaLocal media = mediaHome.findByPrimaryKey(new Integer(pk));
        media.setMediaStatus(status);
        media.setMediaStatusInfo(info);
    }
}