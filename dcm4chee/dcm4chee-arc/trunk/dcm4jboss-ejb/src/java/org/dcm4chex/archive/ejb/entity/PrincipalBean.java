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
import javax.ejb.EntityBean;
import javax.ejb.RemoveException;

import org.apache.log4j.Logger;

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
 *  transaction-type="NotSupported"
 *
 * @ejb.finder
 *  signature="java.util.Collection findByName(java.lang.String pid)"
 *  query="SELECT OBJECT(a) FROM Principal AS a WHERE a.name = ?1"
 *  transaction-type="NotSupported"
 * 
 * @author <a href="mailto:gunter@tiani.com">Gunter Zeilinger</a>
 *
 */
public abstract class PrincipalBean implements EntityBean
{
    private static final Logger log = Logger.getLogger(PrincipalBean.class);

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
     * @ejb:interface-method
     * @ejb:relation
     *  name="principal-patient"
     *  role-name="principal-of-patient"
     *  target-ejb="Patient"
     *  target-role-name="patient-owned-by-principal"
     *  target-multiple="true"
     * @jboss.relation-table
     *  table-name="link_principal_patient"
     * @jboss:relation
     *  fk-column="patient_fk"
     *  related-pk-field="pk"
     * @jboss:target-relation
     *  fk-column="principal_fk"
     *  related-pk-field="pk"
     *    
     * @return all patients of this principal
     */
    public abstract java.util.Collection getPatients();

    public abstract void setPatients(java.util.Collection studies);

    /**
     * @ejb:interface-method
     * @ejb:relation
     *  name="principal-study"
     *  role-name="principal-of-study"
     *  target-ejb="Study"
     *  target-role-name="study-owned-by-principal"
     *  target-multiple="true"
     * @jboss.relation-table
     *  table-name="link_principal_study"
     * @jboss:relation
     *  fk-column="study_fk"
     *  related-pk-field="pk"
     * @jboss:target-relation
     *  fk-column="principal_fk"
     *  related-pk-field="pk"
     *    
     * @return all studies of this principal
     */
    public abstract java.util.Collection getStudies();

    public abstract void setStudies(java.util.Collection studies);

    /**
     * @ejb:interface-method
     * @ejb:relation
     *  name="principal-series"
     *  role-name="principal-of-series"
     *  target-ejb="Series"
     *  target-role-name="series-owned-by-principal"
     *  target-multiple="true"
     * @jboss.relation-table
     *  table-name="link_principal_series"
     * @jboss:relation
     *  fk-column="series_fk"
     *  related-pk-field="pk"
     * @jboss:target-relation
     *  fk-column="principal_fk"
     *  related-pk-field="pk"
     *    
     * @return all series of this principal
     */
    public abstract java.util.Collection getSeries();

    public abstract void setSeries(java.util.Collection studies);

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

    private String prompt()
    {
        return "Principal[pk="
            + getPk()
            + ", name="
            + getName()
            + "]";
    }
}
