/* $Id$
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
package org.dcm4chex.archive.ejb.entity;

import javax.ejb.CreateException;
import javax.ejb.EntityBean;
import javax.ejb.RemoveException;

import org.apache.log4j.Logger;

/**
 * @ejb.bean
 *  name="Directory"
 *  type="CMP"
 *  view-type="local"
 *  primkey-field="pk"
 *  local-jndi-name="ejb/Directory"
 * 
 * @ejb.transaction 
 *  type="Required"
 * 
 * @ejb.persistence
 *  table-name="directory"
 * 
 * @jboss.entity-command
 *  name="postgresql-fetch-seq"
 * 
 * @ejb.finder
 *  signature="Collection findAll()"
 *  query="SELECT OBJECT(a) FROM Directory AS a"
 *  transaction-type="Supports"
 *
 * @ejb.finder
 *  signature="org.dcm4chex.archive.ejb.interfaces.DirectoryLocal findByHostNameAndDirectoryPath(java.lang.String value, java.lang.String designator)"
 *  query="SELECT OBJECT(a) FROM Directory AS a WHERE a.hostName = ?1 AND a.directoryPath = ?2"
 *  transaction-type="Supports"
 * 
 * @author <a href="mailto:gunter@tiani.com">Gunter Zeilinger</a>
 *
 */
public abstract class DirectoryBean implements EntityBean
{
    private static final Logger log = Logger.getLogger(DirectoryBean.class);
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
     * Hostname
     *
     * @ejb.interface-method
     * @ejb.persistence
     *  column-name="hostname"
     */
    public abstract String getHostName();

    public abstract void setHostName(String hostname);

    /**
     * Directory Path
     *
     * @ejb.interface-method
     * @ejb.persistence
     *  column-name="dirpath"
     */
    public abstract String getDirectoryPath();

    public abstract void setDirectoryPath(String dirpath);

    /**
     * Retrieve AETs
     *
     * @ejb.interface-method
     * @ejb.persistence
     *  column-name="retrieve_aets"
     */
    public abstract String getRetrieveAETs();

    /**
     * 
     * @ejb.interface-method
     */
    public abstract void setRetrieveAETs(String aets);

    /**
     * 
     * @ejb.interface-method
     */
    public String asString()
    {
        return prompt();
    }

    private String prompt()
    {
        return "Directory[pk="
            + getPk()
            + ", hostname="
            + getHostName()
            + ", dirpath="
            + getDirectoryPath()
            + ", retrieveAETs="
            + getRetrieveAETs()
            + "]";
    }

    /**
     * Create Directory.
     *
     * @ejb.create-method
     */
    public Integer ejbCreate(String hostname, String dirpath)
        throws CreateException
    {
        setHostName(hostname);
        setDirectoryPath(dirpath);
        return null;
    }

    public void ejbPostCreate(String hostname, String dirpath)
        throws CreateException
    {
        log.info("Created " + prompt());

    }

    public void ejbRemove() throws RemoveException
    {
        log.info("Deleting " + prompt());
    }
}
