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
import javax.ejb.RemoveException;

import org.apache.log4j.Logger;


/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision$ $Date$
 * @since 31.08.2004
 *
 * @ejb.bean
 * 	name="FileSystem"
 * 	type="CMP"
 * 	view-type="local"
 * 	primkey-field="pk"
 * 	local-jndi-name="ejb/FileSystem"
 * 
 * @ejb.transaction
 * 	type="Required"
 * 
 * @ejb.persistence
 * 	table-name="filesystem"
 * 
 * @jboss.entity-command
 * 	name="hsqldb-fetch-key"
 * 
 * @ejb.finder
 *  signature="Collection findAll()"
 *  query="SELECT OBJECT(a) FROM FileSystem AS a"
 *  transaction-type="Supports"
 *
 * @ejb.finder
 *  signature="org.dcm4chex.archive.ejb.interfaces.FileSystemLocal findByDirectoryPath(java.lang.String path)"
 *  query="SELECT OBJECT(a) FROM FileSystem AS a WHERE a.directoryPath = ?1"
 *  transaction-type="Supports"
 */
public abstract class FileSystemBean implements EntityBean {

    private static final Logger log = Logger.getLogger(FileSystemBean.class);

    /**
	 * Create File System.
	 * 
	 * @ejb.create-method
	 */
    public Integer ejbCreate(
        String dirpath,
        String aets,
        long used,
        long hwm)
        throws CreateException
    {
		setDirectoryPath(dirpath);      
		setRetrieveAETs(aets);      
        setUsed(used);
        setHighWaterMark(hwm);
        return null;
    }

    public void ejbPostCreate(String dirpath,
            String aets,
            long used,
            long total)
        throws CreateException
    {
        log.info("Created " + prompt());
    }

    public void ejbRemove() throws RemoveException
    {
        log.info("Deleting " + prompt());
    }
    
    private String prompt()
    {
        return "FileSystem[pk="
            + getPk()
            + ", dirpath="
            + getDirectoryPath()
            + ", retrieveAETs="
            + getRetrieveAETs()
            + ", used="
            + getUsed()
            + ", hwm="
            + getHighWaterMark()
            + "]";
    }
    
    /**
     * Auto-generated Primary Key
     *
     * @ejb.interface-method
     * @ejb.pk-field
     * @ejb.persistence
     *  column-name="pk"
     * @jboss.persistence
     *  auto-increment="true"
     *
     */
    public abstract Integer getPk();

    /**
	 * Directory Path
	 * 
	 * @ejb.interface-method
	 * @ejb.persistence
	 * 	column-name="dirpath"
	 */
    public abstract String getDirectoryPath();

    /**
     * @ejb.interface-method
     */ 
    public abstract void setDirectoryPath(String dirpath);

    /**
	 * Retrieve AETs
	 * 
     * @ejb.interface-method
	 * @ejb.persistence
	 * 	column-name="retrieve_aets"
	 */
    public abstract String getRetrieveAETs();

    /**
     * @ejb.interface-method
     */ 
    public abstract void setRetrieveAETs(String aets);

    /**
	 * Free Size
	 * 
     * @ejb.interface-method
	 * @ejb.persistence
	 * 	column-name="used"
	 */
    public abstract long getUsed();

    /**
     * @ejb.interface-method
     */ 
    public abstract void setUsed(long size);

    /**
	 * High Water Mark
	 * 
     * @ejb.interface-method
	 * @ejb.persistence
	 * 	column-name="hwm"
	 */
    public abstract long getHighWaterMark();

    /**
     * @ejb.interface-method
     */ 
    public abstract void setHighWaterMark(long hwm);

    /**
     * @ejb.interface-method
     */ 
    public long getAvailable() {
        final long hwm = getHighWaterMark();
        return hwm == 0 ? 0 : hwm - getUsed();
    }
}
