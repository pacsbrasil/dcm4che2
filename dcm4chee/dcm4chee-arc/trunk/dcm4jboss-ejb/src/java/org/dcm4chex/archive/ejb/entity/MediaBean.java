/*
 * Copyright (c) 2002,2003 by TIANI MEDGRAPH AG
 *
 * This file is part of dcm4che.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
/* 
 * File: $Source$
 * Author: gunter
 * Date: 08.07.2003
 * Time: 18:28:10
 * CVS Revision: $Revision$
 * Last CVS Commit: $Date$
 * Author of last CVS Commit: $Author$
 */
package org.dcm4chex.archive.ejb.entity;

import javax.ejb.CreateException;
import javax.ejb.EntityBean;
import javax.ejb.RemoveException;

import org.apache.log4j.Logger;

/**
 * @ejb:bean
 *  name="Media"
 *  type="CMP"
 *  view-type="local"
 *  primkey-field="pk"
 *  local-jndi-name="ejb/Media"
 * 
 * @ejb:transaction 
 *  type="Required"
 * 
 * @ejb.persistence
 *  table-name="media"
 * 
 * @jboss.entity-command
 *  name="get-last-oid"
 *  class="org.jboss.ejb.plugins.cmp.jdbc.postgres.JDBCPostgresCreateCommand"
 * 
 * @ejb.finder
 *  signature="Collection findAll()"
 *  query="SELECT OBJECT(a) FROM Media AS a"
 *  transaction-type="Supports"
 *
 * @ejb.finder
 *  signature="org.dcm4chex.archive.ejb.interfaces.MediaLocal findByFilesetIuid(java.lang.String uid)"
 *  query="SELECT OBJECT(a) FROM Media AS a WHERE a.filesetIuid = ?1"
 *  transaction-type="Supports"
 *
 * @ejb.finder
 *  signature="Collection findByMediaStatus(int status)"
 *  query="SELECT OBJECT(a) FROM Media AS a WHERE a.mediaStatus = ?1"
 *  transaction-type="Supports"
 *
 * @author <a href="mailto:gunter@tiani.com">Gunter Zeilinger</a>
 *
 */
public abstract class MediaBean implements EntityBean
{
    private static final Logger log = Logger.getLogger(MediaBean.class);
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

    public abstract void setPk(Integer pk);

    /**
     * File-set UID
     *
     * @ejb.interface-method
     * @ejb.persistence
     *  column-name="fs_iuid"
     */
    public abstract String getFilesetIuid();

    public abstract void setFilesetIuid(String iuid);

    /**
     * File-set Label
     *
     * @ejb.interface-method
     * @ejb.persistence
     *  column-name="fs_id"
     */
    public abstract String getFilesetId();

    /**
     * @ejb.interface-method
     */
    public abstract void setFilesetId(String id);

    /**
     * Media status
     *
     * @ejb.interface-method
     * @ejb.persistence
     *  column-name="media_status"
     */
    public abstract int getMediaStatus();

    /**
     * @ejb.interface-method
     */
    public abstract void setMediaStatus(int status);


    /**
     * @ejb:interface-method view-type="local"
     * @ejb:relation
     *  name="media-file"
     *  role-name="media-with-file"
     *    
     * @return all files on this media
     */
    public abstract java.util.Collection getFiles();
    public abstract void setFiles(java.util.Collection studies);

    /**
     * Create Media.
     *
     * @ejb.create-method
     */
    public Integer ejbCreate(String uid) throws CreateException
    {
        setFilesetIuid(uid);
        return null;
    }

    public void ejbPostCreate(String uid) throws CreateException
    {
        log.info("Created " + prompt());

    }

    public void ejbRemove() throws RemoveException
    {
        log.info("Deleting " + prompt());
    }

    /**
     * 
     * @ejb.interface-method
     */
    public String asString()
    {
        return prompt();
    }

    private String prompt() {
        return "Media[pk="
            + getPk()
            + ", uid="
            + getFilesetIuid()
            + ", status="
            + getMediaStatus()
            + "]";
    }

}
