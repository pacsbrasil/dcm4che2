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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EntityBean;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;

import org.apache.log4j.Logger;
import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;
import org.dcm4cheri.util.StringUtils;
import org.dcm4chex.archive.ejb.interfaces.PatientLocal;
import org.dcm4chex.archive.ejb.interfaces.SeriesLocal;
import org.dcm4chex.archive.ejb.util.DatasetUtil;

/**
 * @ejb.bean
 *  name="Study"
 *  type="CMP"
 *  view-type="local"
 *  primkey-field="pk"
 *  local-jndi-name="ejb/Study"
 * 
 * @jboss.container-configuration
 *  name="Standard CMP 2.x EntityBean with cache invalidation"
 *  
 * @ejb.transaction 
 *  type="Required"
 * 
 * @ejb.persistence
 *  table-name="study"
 * 
 * @jboss.entity-command
 *  name="hsqldb-fetch-key"
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
 * @author <a href="mailto:gunter@tiani.com">Gunter Zeilinger</a>
 *
 */
public abstract class StudyBean implements EntityBean {

    private static final Logger log = Logger.getLogger(StudyBean.class);
    private Set retrieveAETSet;

    public void unsetEntityContext() {
        retrieveAETSet = null;
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
     * Number Of Study Related Series
     *
     * @ejb.interface-method
     * @ejb.persistence
     *  column-name="num_series"
     * 
     */
    public abstract int getNumberOfStudyRelatedSeries();

    public abstract void setNumberOfStudyRelatedSeries(int num);

    /**
     * Number Of Study Related Instances
     *
     * @ejb.interface-method
     * @ejb.persistence
     *  column-name="num_instances"
     * 
     */
    public abstract int getNumberOfStudyRelatedInstances();

    public abstract void setNumberOfStudyRelatedInstances(int num);

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
     * Retrieve AETs
     *
     * @ejb.interface-method
     * @ejb.persistence
     *  column-name="retrieve_aets"
     */
    public abstract String getRetrieveAETs();

    public abstract void setRetrieveAETs(String aets);

    /**
     * @ejb.relation
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
     * @ejb.interface-method view-type="local"
     * 
     * @return patient of this study
     */
    public abstract PatientLocal getPatient();

    /**
     * @ejb.interface-method view-type="local"
     *
     * @param series all series of this study
     */
    public abstract void setSeries(java.util.Collection series);

    /**
     * @ejb.interface-method view-type="local"
     * @ejb.relation
     *  name="study-series"
     *  role-name="study-has-series"
     *    
     * @return all series of this study
     */
    public abstract java.util.Collection getSeries();

    /**
     * Create study.
     *
     * @ejb.create-method
     */
    public Integer ejbCreate(Dataset ds, PatientLocal patient)
        throws CreateException {
        setAttributes(ds);
        return null;
    }

    public void ejbPostCreate(Dataset ds, PatientLocal patient)
        throws CreateException {
        setPatient(patient);
        log.info("Created " + prompt());
    }

    public void ejbRemove() throws RemoveException {
        log.info("Deleting " + prompt());
    }

    /**
     * @ejb.interface-method
     */
    public Dataset getAttributes() {
        return DatasetUtil.fromByteArray(getEncodedAttributes());
    }

    /**
     * @ejb.interface-method
     */
    public void setAttributes(Dataset ds) {
        setStudyIuid(ds.getString(Tags.StudyInstanceUID));
        setStudyId(ds.getString(Tags.StudyID));
        setStudyDateTime(ds.getDateTime(Tags.StudyDate, Tags.StudyTime));
        setAccessionNumber(ds.getString(Tags.AccessionNumber));
        setReferringPhysicianName(ds.getString(Tags.ReferringPhysicianName));
        setEncodedAttributes(DatasetUtil.toByteArray(ds));
    }

    /**
     * @ejb.interface-method
     */
    public void incNumberOfStudyRelatedSeries(int inc) {
        setNumberOfStudyRelatedSeries(getNumberOfStudyRelatedSeries() + inc);
    }

    /**
     * @ejb.interface-method
     */
    public void incNumberOfStudyRelatedInstances(int inc) {
        setNumberOfStudyRelatedInstances(
            getNumberOfStudyRelatedInstances() + inc);
    }

    /**
     * @ejb.interface-method
     */
    public Set getRetrieveAETSet() {
        if (retrieveAETSet == null) {
            retrieveAETSet = new HashSet();
            String aets = getRetrieveAETs();
            if (aets != null) {
                retrieveAETSet.addAll(
                    Arrays.asList(StringUtils.split(aets, '\\')));
            }
        }
        return retrieveAETSet;
    }

    /**
     * @ejb.interface-method
     */
    public boolean addRetrieveAET(String aet) {
        if (getRetrieveAETSet().contains(aet)) {
            return false;
        }
        if (!areAllSeriesRetrieveableFrom(aet)) {
            return false;
        }
        retrieveAETSet.add(aet);
        String prev = getRetrieveAETs();
        if (prev == null || prev.length() == 0) {
            setRetrieveAETs(aet);
        } else {
            setRetrieveAETs(prev + '\\' + aet);
        }
        return true;
    }

    private boolean areAllSeriesRetrieveableFrom(String aet) {
        Collection c = getSeries();
        for (Iterator it = c.iterator(); it.hasNext();) {
            if (!((SeriesLocal) it.next()).getRetrieveAETSet().contains(aet)) {
                return false;
            }
        }
        return true;
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
        try {
            return ejbSelectModalitiesInStudy();
        } catch (FinderException e) {
            throw new EJBException(e);
        }
    }

    /**
     * 
     * @ejb.interface-method
     */
    public String asString() {
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

    /*
    public void update() {
        Collection c = getSeries();
        setNumberOfStudyRelatedSeries(c.size());
        int numInstances = 0;
        Set resultAetSet = null;
        for (Iterator it = c.iterator(); it.hasNext();) {
            SeriesLocal series = (SeriesLocal) it.next();
            series.update();
            numInstances += series.getNumberOfSeriesRelatedInstances();
            String aets = series.getRetrieveAETs();
            if (aets != null) {
                List aetList = Arrays.asList(StringUtils.split(aets, '\\'));
                if (resultAetSet == null) {
                    resultAetSet = new HashSet(aetList);
                } else {
                    resultAetSet.retainAll(aetList);
                }
            }
        }
        setNumberOfStudyRelatedInstances(numInstances);
        setRetrieveAETs(
            resultAetSet == null
                ? null
                : StringUtils.toString(
                    (String[]) resultAetSet.toArray(
                        new String[resultAetSet.size()]),
                    '\\'));
    }
     */
}
