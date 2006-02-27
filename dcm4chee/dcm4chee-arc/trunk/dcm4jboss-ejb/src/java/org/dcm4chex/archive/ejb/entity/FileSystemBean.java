/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), available at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * TIANI Medgraph AG.
 * Portions created by the Initial Developer are Copyright (C) 2003-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunter.zeilinger@tiani.com>
 * Franz Willer <franz.willer@gwi-ag.com>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

package org.dcm4chex.archive.ejb.entity;

import javax.ejb.CreateException;
import javax.ejb.EntityBean;
import javax.ejb.RemoveException;

import org.apache.log4j.Logger;
import org.dcm4chex.archive.ejb.interfaces.FileSystemDTO;
import org.dcm4chex.archive.ejb.interfaces.FileSystemLocal;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision$ $Date$
 * @since 31.08.2004
 *
 * @ejb.bean name="FileSystem"
 * 	         type="CMP"
 * 	         view-type="local"
 * 	         primkey-field="pk"
 * 	         local-jndi-name="ejb/FileSystem"
 * 
 * @ejb.transaction type="Required"
 * @ejb.persistence table-name="filesystem"
 * @jboss.entity-command name="hsqldb-fetch-key"
 * 
 * @ejb.finder signature="java.util.Collection findAll()"
 *             query=""transaction-type="Supports"
 * @jboss.query signature="java.util.Collection findAll()"
 *             query="SELECT OBJECT(a) FROM FileSystem AS a"
 *             strategy="on-find" eager-load-group="*"
 *
 * @ejb.finder signature="org.dcm4chex.archive.ejb.interfaces.FileSystemLocal findByDirectoryPath(java.lang.String path)"
 *             query="" transaction-type="Supports"
 * @jboss.query signature="org.dcm4chex.archive.ejb.interfaces.FileSystemLocal findByDirectoryPath(java.lang.String path)"
 *             query="SELECT OBJECT(a) FROM FileSystem AS a WHERE a.directoryPath = ?1"
 *             strategy="on-find" eager-load-group="*"
 *
 * @ejb.finder signature="java.util.Collection findByLikeDirectoryPath(java.lang.String path, int availability, int status)"
 *             query="" transaction-type="Supports"
 * @jboss.query signature="java.util.Collection findByLikeDirectoryPath(java.lang.String path, int availability, int status)"
 *             query="SELECT OBJECT(a) FROM FileSystem AS a WHERE a.directoryPath LIKE ?1 AND a.availability = ?2 AND a.status = ?3"
 *             strategy="on-find" eager-load-group="*"
 *
 * @ejb.finder signature="java.util.Collection findByRetrieveAETAndAvailabilityAndStatus(java.lang.String aet, int availability, int status)"
 *             query="" transaction-type="Supports"
 * @jboss.query signature="java.util.Collection findByRetrieveAETAndAvailabilityAndStatus(java.lang.String aet, int availability, int status)"
 *             query="SELECT OBJECT(a) FROM FileSystem AS a WHERE a.retrieveAET = ?1 AND a.availability = ?2 AND a.status = ?3"
 *             strategy="on-find" eager-load-group="*"
 *
 * @ejb.finder signature="java.util.Collection findByRetrieveAETAndAvailabilityAndStatus2(java.lang.String aet, int availability, int status, int alt)"
 *             query="" transaction-type="Supports"
 * @jboss.query signature="java.util.Collection findByRetrieveAETAndAvailabilityAndStatus2(java.lang.String aet, int availability, int status, int alt)"
 *             query="SELECT OBJECT(a) FROM FileSystem AS a WHERE a.retrieveAET = ?1 AND a.availability = ?2 AND (a.status = ?3 OR a.status = ?4)"
 *             strategy="on-find" eager-load-group="*"
 */
public abstract class FileSystemBean implements EntityBean {

    private static final Logger log = Logger.getLogger(FileSystemBean.class);

    /**
	 * Create File System.
	 * 
	 * @ejb.create-method
	 */
    public Integer ejbCreate(String dirpath, String aet, int availability,
    		int status, String userInfo)
        throws CreateException
    {
		setDirectoryPath(dirpath);      
		setRetrieveAET(aet);
		setAvailability(availability);
		setStatus(status);
		setUserInfo(userInfo);
        return null;
    }

    public void ejbPostCreate(String dirpath, String aets, int availability,
    		String userInfo)
        throws CreateException
    {
        log.info("Created " + asString());
    }

    public void ejbRemove() throws RemoveException
    {
        log.info("Deleting " + asString());
    }
    
    /**
     * @ejb.interface-method
     */ 
    public String asString()
    {
        return "FileSystem[pk="
            + getPk()
            + ", dirpath="
            + getDirectoryPath()
            + ", retrieveAET="
            + getRetrieveAET()
            + ", availability="
            + getAvailability()
            + ", status="
            + getStatus()
            + ", userInfo="
            + getUserInfo()
            + ", next="
            + getNextFileSystem()
            + "]";
    }
    
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

    /**
	 * @ejb.interface-method
	 * @ejb.persistence column-name="dirpath"
	 */
    public abstract String getDirectoryPath();

    /**
     * @ejb.interface-method
     */ 
    public abstract void setDirectoryPath(String dirpath);

    /**
     * @ejb.interface-method
	 * @ejb.persistence column-name="retrieve_aet"
	 */
    public abstract String getRetrieveAET();

    /**
     * @ejb.interface-method
     */ 
    public abstract void setRetrieveAET(String aet);

    /**
     * @ejb.interface-method
     * @ejb.persistence column-name="availability"
     */
    public abstract int getAvailability();

    /**
     * @ejb.interface-method
     */
    public abstract void setAvailability(int availability);
    
    /**
     * @ejb.interface-method
     * @ejb.persistence column-name="fs_status"
     */
    public abstract int getStatus();

    /**
     * @ejb.interface-method
     */
    public abstract void setStatus(int status);
    
    /**
     * @ejb.interface-method
	 * @ejb.persistence column-name="user_info"
	 */
    public abstract String getUserInfo();

    /**
     * @ejb.interface-method
     */ 
    public abstract void setUserInfo(String info);


    /**
     * @ejb.relation name="next-filesystem"
     *    role-name="prev-filesystem"
     *    target-role-name="next-filesystem"
     *    target-ejb="FileSystem"
     *    target-multiple="yes"
     *    cascade-delete="no"
     *
     * @jboss.relation fk-column="next_fk" related-pk-field="pk"
     */
    public abstract FileSystemLocal getNextFileSystem();

    /**
     * @ejb.interface-method
     */
    public abstract void setNextFileSystem(FileSystemLocal filesystem);
    
    /**
     * @ejb.interface-method
     */
    public void fromDTO(FileSystemDTO dto) {
        setDirectoryPath(dto.getDirectoryPath());
        setRetrieveAET(dto.getRetrieveAET());
        setAvailability(dto.getAvailability());
        setStatus(dto.getStatus());
        setUserInfo(dto.getUserInfo());
    }
    /**
     * @ejb.interface-method
     */
    public FileSystemDTO toDTO() {
		FileSystemDTO dto = new FileSystemDTO();
		dto.setPk(getPk().intValue());
		dto.setDirectoryPath(getDirectoryPath());
		dto.setRetrieveAET(getRetrieveAET());
		dto.setAvailability(getAvailability());
		dto.setStatus(getStatus());
		dto.setUserInfo(getUserInfo());
    	FileSystemLocal next = getNextFileSystem();
    	if (next != null) {
    		dto.setNext(next.getDirectoryPath());
    	}
    	return dto;
    }
}
