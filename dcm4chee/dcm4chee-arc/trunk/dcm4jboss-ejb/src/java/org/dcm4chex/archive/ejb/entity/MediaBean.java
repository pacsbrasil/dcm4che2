/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.ejb.entity;

import javax.ejb.CreateException;
import javax.ejb.EntityBean;

import org.apache.log4j.Logger;

/**
 * @ejb.bean name="Media" type="CMP" view-type="local"
 * 	primkey-field="pk" local-jndi-name="ejb/Media"
 * 
 * @ejb.transaction type="Required"
 * 
 * @ejb.persistence table-name="media"
 * 
 * @jboss.entity-command name="hsqldb-fetch-key"
 * 
 * @jboss.audit-created-time field-name="createdTime"
 * @jboss.audit-updated-time field-name="updatedTime"
 * 
 * @ejb.finder
 *  signature="java.util.Collection findAll()"
 *  query="SELECT OBJECT(m) FROM Media AS m"
 *  transaction-type="Supports"
 *
 * @ejb.finder
 *  signature="java.util.Collection findByStatus(int status)"
 *  query="SELECT OBJECT(m) FROM Media AS m WHERE m.mediaStatus = ?1"
 *  transaction-type="Supports"
 *
 * @jboss.query
 *  signature="java.util.Collection findByStatus(int status)"
 *  strategy="on-find"
 *  eager-load-group="*"
 *
 * @ejb.finder
 *  signature="org.dcm4chex.archive.ejb.interface.MediaLocal findByFilesetIuid(java.lang.String uid)"
 *  query="SELECT OBJECT(m) FROM Media AS m WHERE m.filesetIuid = ?1"
 *  transaction-type="Supports"
 *
 * @jboss.query
 *  signature="org.dcm4chex.archive.ejb.interface.MediaLocal findByFilesetIuid(java.lang.String uid)"
 *  strategy="on-find"
 *  eager-load-group="*"
 *
 * @author gunter.zeilinger@tiani.com
 * @version $Revision$ $Date$
 * @since 26.11.2004
 */
public abstract class MediaBean implements EntityBean {

    private static final Logger log = Logger.getLogger(MediaBean.class);

    /**
     * Auto-generated Primary Key
     * 
     * @ejb.interface-method 
     * @ejb.pk-field
     * @ejb.persistence column-name="pk"
     * @jboss.persistence auto-increment="true"
     *  
     */
    public abstract Integer getPk();

    public abstract void setPk(Integer pk);

    /**
     * @ejb.interface-method
     * @ejb.persistence
     *  column-name="created_time"
     */
    public abstract java.sql.Timestamp getCreatedTime();

    public abstract void setCreatedTime(java.sql.Timestamp time);

    /**
     * @ejb.interface-method
     * @ejb.persistence
     *  column-name="updated_time"
     */
    public abstract java.sql.Timestamp getUpdatedTime();

    public abstract void setUpdatedTime(java.sql.Timestamp time);
    
    /**
     * @ejb.interface-method
     * @ejb.persistence column-name="fileset_id"
     */
    public abstract String getFilesetId();

    /**
     * @ejb.interface-method
     */
    public abstract void setFilesetId(String id);

    /**
     * @ejb.interface-method
     * @ejb.persistence column-name="fileset_iuid"
     */
    public abstract String getFilesetIuid();

    public abstract void setFilesetIuid(String iuid);

    /**
     * @ejb.interface-method
     * @ejb.persistence column-name="media_rq_iuid"
     */
    public abstract String getMediaCreationRequestIuid();

    /**
     * @ejb.interface-method
     */
    public abstract void setMediaCreationRequestIuid(String iuid);

    /**
     * @ejb.interface-method
     * @ejb.persistence column-name="media_usage"
     */
    public abstract long getMediaUsage();

    /**
     * @ejb.interface-method
     */
    public abstract void setMediaUsage(long mediaUsage);
    
    /**
     * @ejb.interface-method
     * @ejb.persistence column-name="media_status"
     */
    public abstract int getMediaStatus();

    /**
     * @ejb.interface-method
     */
    public abstract void setMediaStatus(int mediaStatus);
    
    /**
    * @ejb.create-method
    */
   public Integer ejbCreate(String fsIuid) throws CreateException {
       setFilesetIuid(fsIuid);
       return null;
   }

   public void ejbPostCreate(String fsIuid) throws CreateException {
   }
}
