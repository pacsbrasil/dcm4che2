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
 * Date: 09.07.2003
 * Time: 09:08:46
 * CVS Revision: $Revision$
 * Last CVS Commit: $Date$
 * Author of last CVS Commit: $Author$
 */
package org.dcm4chex.archive.ejb.entity;

import java.util.Set;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;

import org.apache.log4j.Logger;
import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;
import org.dcm4chex.archive.ejb.interfaces.PatientLocal;
import org.dcm4chex.archive.ejb.interfaces.PrincipalLocalHome;
import org.dcm4chex.archive.ejb.util.DatasetUtil;
import org.dcm4chex.archive.ejb.util.EJBHomeFactoryException;
import org.dcm4chex.archive.ejb.util.EJBLocalHomeFactory;

/**

/**
 * @ejb:bean
 *  name="Study"
 *  type="CMP"
 *  view-type="local"
 *  primkey-field="pk"
 *  local-jndi-name="ejb/Study"
 * 
 * @ejb:transaction 
 *  type="Required"
 * 
 * @ejb.persistence
 *  table-name="study"
 * 
 * @jboss.entity-command
 *  name="get-last-oid"
 *  class="org.jboss.ejb.plugins.cmp.jdbc.postgres.JDBCPostgresCreateCommand"
 * 
 * @ejb.finder
 *  signature="Collection findAll()"
 *  query="SELECT OBJECT(a) FROM Study AS a"
 *  transaction-type="Supports"
 *
 * @ejb.finder
 *  signature="org.dcm4chex.archive.ejb.interfaces.StudyLocal findByStudyIuid(java.lang.String uid)"
 *  query="SELECT OBJECT(a) FROM Study AS a WHERE a.studyIuid = ?1"
 *  transaction-type="Supports"
 *
 * @ejb.ejb-ref
 *  ejb-name="Principal" 
 *  view-type="local"
 *  ref-name="ejb/org.dcm4chex.archive.ejb.interfaces.PrincipalLocalHome"
 * 
 * @author <a href="mailto:gunter@tiani.com">Gunter Zeilinger</a>
 *
 */
public abstract class StudyBean implements EntityBean {

    private static final String ATTRS_CFG = "study-attrs.cfg";

    private static final Logger log = Logger.getLogger(StudyBean.class);
    
    private EntityContext ctx;
    private PrincipalLocalHome principalHome;

    public void setEntityContext(EntityContext ctx) 
    {
        this.ctx = ctx;
        try
        {
            EJBLocalHomeFactory factory = EJBLocalHomeFactory.getInstance();
            principalHome = (PrincipalLocalHome) factory.lookup(PrincipalLocalHome.class);
        }
        catch (EJBHomeFactoryException e)
        {
            throw new EJBException(e);
        }
    }
    
    public void unsetEntityContext() 
    {
        ctx = null;
        principalHome = null;
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
     * Study Instance UID
     *
     * @ejb.interface-method
     * @ejb.persistence
     *  column-name="study_iuid"
     */
    public abstract String getStudyIuid();

    public abstract void setStudyIuid(String uid);

    /**
     * Study ID
     *
     * @ejb.interface-method
     * @ejb.persistence
     *  column-name="study_id"
     */
    public abstract String getStudyId();

    public abstract void setStudyId(String uid);

    /**
     * Study Datetime
     *
     * @ejb.interface-method
     * @ejb.persistence
     *  column-name="study_datetime"
     */
    public abstract java.util.Date getStudyDateTime();

    public abstract void setStudyDateTime(java.util.Date dateTime);

    /**
     * Accession Number
     *
     * @ejb.interface-method
     * @ejb.persistence
     *  column-name="accession_no"
     */
    public abstract String getAccessionNumber();

    public abstract void setAccessionNumber(String no);

    /**
     * Referring Physician
     *
     * @ejb.interface-method
     * @ejb.persistence
     *  column-name="ref_physician"
     */
    public abstract String getReferringPhysicianName();

    public abstract void setReferringPhysicianName(String physician);

    /**
     * Study DICOM Attributes
     *
     * @ejb.persistence
     *  column-name="study_attrs"
     * 
     */
    public abstract byte[] getEncodedAttributes();

    public abstract void setEncodedAttributes(byte[] bytes);

    /**
     * @ejb:relation
     *  name="patient-study"
     *  role-name="study-of-patient"
     *  cascade-delete="yes"
     *
     * @jboss:relation
     *  fk-column="patient_fk"
     *  related-pk-field="pk"
     * 
     * @param patient patient of this study
     */
    public abstract void setPatient(PatientLocal patient);
    
    /**
     * @ejb:interface-method view-type="local"
     * 
     * @return patient of this study
     */
    public abstract PatientLocal getPatient();
    
    /**
     * @ejb:interface-method view-type="local"
     *
     * @param series all series of this study
     */
    public abstract void setSeries(java.util.Collection series);

    /**
     * @ejb:interface-method view-type="local"
     * @ejb:relation
     *  name="study-series"
     *  role-name="study-has-series"
     *    
     * @return all series of this study
     */
    public abstract java.util.Collection getSeries();
    
    /**
     * @ejb:interface-method
     * @ejb:relation
     *  name="principal-study"
     *  role-name="study-owned-by-principal"
     *  target-ejb="Principal"
     *  target-role-name="principal-of-study"
     *  target-multiple="true"
     * @jboss.relation-table
     *  table-name="link_principal_study"
     * @jboss:relation
     *  fk-column="principal_fk"
     *  related-pk-field="pk"
     * @jboss:target-relation
     *  fk-column="study_fk"
     *  related-pk-field="pk"
     *    
     * @return all principals of this study
     */
    public abstract java.util.Set getPrincipals();

    public abstract void setPrincipals(java.util.Set principals);

    /**
     * Create study.
     *
     * @ejb.create-method
     */
    public Integer ejbCreate(Dataset ds, PatientLocal patient) throws CreateException {
        setAttributes(ds);
        return null;
    }

    public void ejbPostCreate(Dataset ds, PatientLocal patient) throws CreateException {
        setPatient(patient);
        log.info("Created " + prompt());
    }

    public void ejbRemove() throws RemoveException {
        log.info("Deleting " + prompt());
    }
    
    /**
     * @ejb:interface-method
     */
    public Dataset getAttributes()
    {
        return DatasetUtil.fromByteArray(getEncodedAttributes());
    }

    /**
     * @ejb:interface-method
     */
    public void setAttributes(Dataset ds)
    {
        setStudyIuid(ds.getString(Tags.StudyInstanceUID));
        setStudyId(ds.getString(Tags.StudyID));
        setStudyDateTime(ds.getDateTime(Tags.StudyDate, Tags.StudyTime));
        setAccessionNumber(ds.getString(Tags.AccessionNumber));
        setReferringPhysicianName(ds.getString(Tags.ReferringPhysicianName));
        setEncodedAttributes(DatasetUtil.toByteArray(ds.subSet(DatasetUtil.getFilter(ATTRS_CFG))));
    }
    
    /**
     * @ejb.select
     *  query="SELECT s.modality FROM Study AS a, IN(a.series) s"
     */
    public abstract Set ejbSelectModalitiesInStudy() throws FinderException; 

    /**
     * Modalities In Study
     *
     * @ejb.interface-method
     */
    public Set getModalitiesInStudy() {
        try
        {
            return ejbSelectModalitiesInStudy();
        }
        catch (FinderException e)
        {
            throw new EJBException(e);
        }
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
        return "Study[pk="
            + getPk()
            + ", uid="
            + getStudyIuid()
            + ", patient->"
            + getPatient()
            + "]";
    }
}
