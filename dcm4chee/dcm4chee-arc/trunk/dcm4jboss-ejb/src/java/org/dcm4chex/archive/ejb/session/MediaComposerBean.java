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
import java.util.Iterator;
import java.util.List;

import javax.ejb.EJBException;
import javax.ejb.FinderException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.dcm4chex.archive.ejb.interfaces.InstanceLocalHome;
import org.dcm4chex.archive.ejb.interfaces.MediaDTO;
import org.dcm4chex.archive.ejb.interfaces.MediaLocal;
import org.dcm4chex.archive.ejb.interfaces.MediaLocalHome;

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
    public int prepareMedia(long studyReceivedBefore) throws FinderException {
        Collection c = instHome.findNotOnMediaAndStudyReceivedBefore(
                new Timestamp(studyReceivedBefore));
        //TODO
        return c.size();
    }
    
    /**
     * @ejb.interface-method
     */
    public List getPreparingMedia() throws FinderException {
        return toMediaDTOs(mediaHome.findPreparing());
    }
    
    /**
     * @ejb.interface-method
     */
    public List getBurningMedia() throws FinderException {
        return toMediaDTOs(mediaHome.findBurning());
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
        dto.setFilesetId(media.getFilesetId());
        dto.setFilesetIuid(media.getFilesetIuid());
        dto.setMediaCreationRequestIuid(media.getMediaCreationRequestIuid());
        return dto;
    }    

    /**
     * @ejb.interface-method
     */
    public void update(MediaDTO dto) throws FinderException {
        MediaLocal media = mediaHome.findByPrimaryKey(
                new Integer(dto.getPk()));
        if (!equals(media.getFilesetId(), dto.getFilesetId()))
            media.setFilesetId(dto.getFilesetId());
        if (!equals(media.getFilesetIuid(), dto.getFilesetIuid()))
            media.setFilesetIuid(dto.getFilesetIuid());
        if (!equals(media.getMediaCreationRequestIuid(), 
                dto.getMediaCreationRequestIuid()))
            media.setMediaCreationRequestIuid(dto.getMediaCreationRequestIuid());
    }

    private boolean equals(Object o1, Object o2) {
        return o1 == null ? o2 == null : o1.equals(o2);
    }

}