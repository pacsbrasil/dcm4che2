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
 * Date: 20.07.2003
 * Time: 22:39:22
 * CVS Revision: $Revision$
 * Last CVS Commit: $Date$
 * Author of last CVS Commit: $Author$
 */
package org.dcm4chex.archive.ejb.entity;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import javax.ejb.FinderException;
import javax.ejb.ObjectNotFoundException;
import javax.ejb.RemoveException;

import org.apache.log4j.Logger;
import org.dcm4chex.archive.ejb.interfaces.PrincipalLocal;
import org.dcm4chex.archive.ejb.interfaces.PrincipalLocalHome;

/**
/**
 * @ejb:bean
 *  name="Principal"
 *  type="CMP"
 *  view-type="local"
 *  primkey-field="pk"
 *  local-jndi-name="ejb/Principal"
 * 
 * @ejb:transaction 
 *  type="Required"
 * 
 * @ejb.persistence
 *  table-name="principal"
 * 
 * @jboss.entity-command
 *  name="get-last-oid"
 *  class="org.jboss.ejb.plugins.cmp.jdbc.postgres.JDBCPostgresCreateCommand"
 * 
 * @ejb.finder
 *  signature="Collection findAll()"
 *  query="SELECT OBJECT(a) FROM Principal AS a"
 *  transaction-type="Supports"
 *
 * @ejb.finder
 *  signature="PrincipalLocal findByName(java.lang.String pid)"
 *  query="SELECT OBJECT(a) FROM Principal AS a WHERE a.name = ?1"
 *  transaction-type="Supports"
 * 
 * @author <a href="mailto:gunter@tiani.com">Gunter Zeilinger</a>
 *
 */
public abstract class PrincipalBean implements EntityBean
{
    private static final Logger log = Logger.getLogger(PrincipalBean.class);

    private EntityContext ctx;

    public void setEntityContext(EntityContext ctx) 
    {
        this.ctx = ctx;
    }

    public void unsetEntityContext() 
    {
        this.ctx = null;
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

    public abstract void setPk(Integer pk);

    /**
     * Patient ID
     *
     * @ejb.interface-method
     * @ejb.persistence
     *  column-name="name"
     */
    public abstract String getName();

    public abstract void setName(String name);

    /**
     * Create patient.
     *
     * @ejb.create-method
     */
    public Integer ejbCreate(String name) throws CreateException
    {
        setName(name);
        return null;
    }

    public void ejbPostCreate(String name) throws CreateException
    {
        log.info("Created " + prompt());
    }

    public void ejbRemove() throws RemoveException
    {
        log.info("Deleting " + prompt());
    }

    /**
     * @ejb.home-method  
     */
    public PrincipalLocal ejbHomeGetPrincipal(String name) {
        PrincipalLocalHome home = (PrincipalLocalHome) ctx.getEJBLocalHome();
        try
        {
            try
            {
                return home.findByName(name);
            }
            catch (ObjectNotFoundException e) {
                return home.create(name);
            }
        }
        catch (FinderException e)
        {
            throw new EJBException(e);
        }
        catch (CreateException e)
        {
            throw new EJBException(e);
        }
        
    }
    
    private String prompt()
    {
        return "Principal[pk="
            + getPk()
            + ", name="
            + getName()
            + "]";
    }
}
