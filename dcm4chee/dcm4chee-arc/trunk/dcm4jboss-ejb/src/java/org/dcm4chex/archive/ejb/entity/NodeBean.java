/*
 * Copyright (c) 2002,2003 by TIANI MEDGRAPH AG
 *
 * This node is part of dcm4che.
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
 * Node: $Source$
 * Author: gunter
 * Date: 09.07.2003
 * Time: 09:08:46
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
 *  name="Node"
 *  type="CMP"
 *  view-type="local"
 *  primkey-field="pk"
 *  local-jndi-name="ejb/Node"
 * 
 * @ejb:transaction 
 *  type="Required"
 * 
 * @ejb.persistence
 *  table-name="node"
 * 
 * @jboss.entity-command
 *  name="get-last-oid"
 *  class="org.jboss.ejb.plugins.cmp.jdbc.postgres.JDBCPostgresCreateCommand"
 *
 * @ejb.finder
 *  signature="java.util.Collection findAll()"
 *  query="SELECT OBJECT(a) FROM Node AS a"
 *  transaction-type="Supports"
 *
 * @ejb.finder
 *  signature="org.dcm4chex.archive.ejb.interfaces.NodeLocal findByStorageAET(java.lang.String aet)"
 *  query="SELECT OBJECT(a) FROM Node AS a WHERE a.storageAET = ?1"
 *  transaction-type="Supports"
 * 
 * @author <a href="mailto:gunter@tiani.com">Gunter Zeilinger</a>
 *
 */
public abstract class NodeBean implements EntityBean {

    private static final Logger log = Logger.getLogger(NodeBean.class);

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
     * Archive root directory aet
     *
     * @ejb.interface-method
     * @ejb.persistence
     *  column-name="uri"
     */
    public abstract String getURI();
    public abstract void setURI(String uri);

    /**
     * Storage AET associated with this Node
     *
     * @ejb.interface-method
     * @ejb.persistence
     *  column-name="storage_aet"
     */
    public abstract String getStorageAET();
    public abstract void setStorageAET(String aet);

    /**
     * Transfer Syntax UID
     *
     * @ejb.interface-method
     * @ejb.persistence
     *  column-name="retrieve_aet"
     */
    public abstract String getRetrieveAET();
    public abstract void setRetrieveAET(String aet);

    /**
     * 
     * @ejb.interface-method
     */
    public String asString() {
        return prompt();
    }

    private String prompt() {
        return "Node[pk="
            + getPk()
            + ", uri="
            + getURI()
            + ", storageAET="
            + getStorageAET()
            + ", retrieveAET="
            + getRetrieveAET()
            + "]";
    }

    /**
     * Create node.
     *
     * @ejb.create-method
     */
    public Integer ejbCreate(
        String uri,
        String storageAET,
        String retrieveAET)
        throws CreateException {
        setURI(uri);
        setStorageAET(storageAET);
        setRetrieveAET(retrieveAET);
        return null;
    }

    public void ejbPostCreate(
        String uri,
        String storageAET,
        String retrieveAET)
        throws CreateException {
        log.info("Created " + prompt());
    }

    public void ejbRemove() throws RemoveException {
        log.info("Deleting " + prompt());
    }
}
