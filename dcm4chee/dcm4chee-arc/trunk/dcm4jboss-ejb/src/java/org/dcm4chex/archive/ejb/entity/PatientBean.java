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
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmDecodeParam;
import org.dcm4che.dict.Tags;
import org.dcm4chex.archive.common.DatasetUtils;
import org.dcm4chex.archive.common.PrivateTags;
import org.dcm4chex.archive.ejb.interfaces.PatientLocal;

/**
 * @ejb.bean
 *  name="Patient"
 *  type="CMP"
 *  view-type="local"
 *  primkey-field="pk"
 *  local-jndi-name="ejb/Patient"
 * 
 * @ejb.transaction 
 *  type="Required"
 * 
 * @ejb.persistence
 *  table-name="patient"
 * 
 * @jboss.entity-command
 *  name="hsqldb-fetch-key"
 * 
 * @ejb.finder
 *  signature="Collection findAll()"
 *  query="SELECT OBJECT(a) FROM Patient AS a"
 *  transaction-type="Supports"
 *
 * @ejb.finder
 *  signature="java.util.Collection findByPatientId(java.lang.String pid)"
 *  query="SELECT OBJECT(a) FROM Patient AS a WHERE a.patientId = ?1"
 *  transaction-type="Supports"
 *
 * @jboss.query
 *  signature="java.util.Collection findByPatientId(java.lang.String pid)"
 *  strategy="on-find"
 *  eager-load-group="*"
 * 
 * @ejb.finder
 *  signature="java.util.Collection findByPatientIdWithIssuer(java.lang.String pid, java.lang.String issuer)"
 *  query="SELECT OBJECT(a) FROM Patient AS a WHERE a.patientId = ?1 AND (a.issuerOfPatientId IS NULL OR a.issuerOfPatientId = ?2)"
 *  transaction-type="Supports"
 * 
 * @author <a href="mailto:gunter@tiani.com">Gunter Zeilinger</a>
 *
 */
public abstract class PatientBean implements EntityBean {

    private static final Logger log = Logger.getLogger(PatientBean.class);

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
     *  column-name="pat_id"
     */
    public abstract String getPatientId();

    public abstract void setPatientId(String pid);

    /**
     * Patient ID Issuer
     *
     * @ejb.interface-method
     * @ejb.persistence
     *  column-name="pat_id_issuer"
     */
    public abstract String getIssuerOfPatientId();

    /**
     * @ejb.interface-method
     */
    public abstract void setIssuerOfPatientId(String issuer);

    /**
     * Patient Name
     *
     * @ejb.interface-method
     * @ejb.persistence
     *  column-name="pat_name"
     */
    public abstract String getPatientName();

    /**
     * @ejb.interface-method
     * @param name
     */
    public abstract void setPatientName(String name);

    /**
     * Patient Birth Date
     *
     * @ejb.interface-method
     * @ejb.persistence
     *  column-name="pat_birthdate"
     */
    public abstract java.sql.Timestamp getPatientBirthDate();

    /**
     * @ejb.interface-method
     */
    public abstract void setPatientBirthDate(java.sql.Timestamp date);

    /**
     * Patient Sex
     *
     * @ejb.interface-method
     * @ejb.persistence
     *  column-name="pat_sex"
     */
    public abstract String getPatientSex();

    /**
     * @ejb.interface-method
     *
     */
    public abstract void setPatientSex(String sex);

    /**
     * Patient DICOM Attributes
     *
     * @ejb.persistence
     *  column-name="pat_attrs"
     * 
     */
    public abstract byte[] getEncodedAttributes();

    public abstract void setEncodedAttributes(byte[] bytes);

    /**
     * @return Patient, with which this Patient was merged.
     *
     * @ejb:interface-method view-type="local"
     * @ejb:relation
     *    name="merged-patients"
     *    role-name="dereferenced-patient"
     *    target-role-name="dominant-patient"
     *    target-ejb="Patient"
     *    target-multiple="yes"
     *    cascade-delete="yes"
     *
     * @jboss:relation
     *    fk-column="merge_fk"
     *    related-pk-field="pk"
     */
    public abstract PatientLocal getMergedWith();

    /**
     * @param mergedWith, Patient, with which this Patient was merged.
     *
     * @ejb:interface-method
     */
    public abstract void setMergedWith(PatientLocal mergedWith);

    /**
     * @ejb.interface-method view-type="local"
     *
     * @param studies all studies of this patient
     */
    public abstract void setStudies(java.util.Collection studies);

    /**
     * @ejb.interface-method view-type="local"
     * @ejb.relation
     *  name="patient-study"
     *  role-name="patient-has-studies"
     *    
     * @return all studies of this patient
     */
    public abstract java.util.Collection getStudies();

    /**
     * @ejb.interface-method view-type="local"
     */
    public abstract void setMwlItems(java.util.Collection mwlItems);

    /**
     * @ejb.interface-method view-type="local"
     * @ejb.relation
     *  name="patient-mwlitems"
     *  role-name="patient-has-mwlitems"
     */
    public abstract java.util.Collection getMwlItems();

    /**
     * @ejb.interface-method view-type="local"
     */
    public abstract void setMpps(java.util.Collection mpps);

    /**
     * @ejb.interface-method view-type="local"
     * @ejb.relation
     *  name="patient-mpps"
     *  role-name="patient-has-mpps"
     */
    public abstract java.util.Collection getMpps();

    /**
     * Create patient.
     *
     * @ejb.create-method
     */
    public Integer ejbCreate(Dataset ds) throws CreateException {
        setAttributes(ds);
        return null;
    }

    public void ejbPostCreate(Dataset ds) throws CreateException {
        log.info("Created " + prompt());
    }

    public void ejbRemove() throws RemoveException {
        log.info("Deleting " + prompt());
    }

    /**
     * @ejb.interface-method
     */
    public Dataset getAttributes(boolean supplement) {
        Dataset ds = DatasetUtils.fromByteArray(getEncodedAttributes(),
                DcmDecodeParam.EVR_LE,
                null);
        if (supplement) {
            ds.setPrivateCreatorID(PrivateTags.CreatorID);
            ds.putUL(PrivateTags.PatientPk, getPk().intValue());
        }
        return ds;
    }

    /**
     * @ejb.interface-method
     */
    public void setAttributes(Dataset ds) {
        setPatientId(ds.getString(Tags.PatientID));
        setIssuerOfPatientId(ds.getString(Tags.IssuerOfPatientID));
        setPatientName(ds.getString(Tags.PatientName));
        try {
	        setPatientBirthDate(ds.getDate(Tags.PatientBirthDate));
	    } catch (IllegalArgumentException e) {
	        log.warn("Illegal Patient Birth Date format: " + e.getMessage());
	    }
        setPatientSex(ds.getString(Tags.PatientSex));
        Dataset tmp = ds.excludePrivate();
        setEncodedAttributes(DatasetUtils.toByteArray(tmp,
                DcmDecodeParam.EVR_LE));
    }

    /**
     * @ejb.interface-method
     */
    public void setPatientBirthDate(java.util.Date date) {
        setPatientBirthDate(date != null ? new java.sql.Timestamp(date
                .getTime()) : null);
    }

    /**
     * 
     * @ejb.interface-method
     */
    public String asString() {
        return prompt();
    }

    private String prompt() {
        return "Patient[pk=" + getPk() + ", pid=" + getPatientId() + ", name="
                + getPatientName() + "]";
    }
}