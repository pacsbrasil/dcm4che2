/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.ejb.entity;

import java.util.Collection;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmDecodeParam;
import org.dcm4che.data.DcmElement;
import org.dcm4che.dict.Tags;
import org.dcm4chex.archive.common.DatasetUtils;
import org.dcm4chex.archive.common.GPSPSPriority;
import org.dcm4chex.archive.common.GPSPSStatus;
import org.dcm4chex.archive.common.InputAvailabilityFlag;
import org.dcm4chex.archive.ejb.interfaces.CodeLocal;
import org.dcm4chex.archive.ejb.interfaces.CodeLocalHome;
import org.dcm4chex.archive.ejb.interfaces.PatientLocal;
import org.dcm4chex.archive.ejb.interfaces.RefRequestLocalHome;

/**
 * @author gunter.zeilinger@tiani.com
 * @version Revision $Date$
 * @since 28.03.2005
 * 
 * @ejb.bean name="GPSPS" type="CMP" view-type="local"
 *           local-jndi-name="ejb/GPSPS" primkey-field="pk"
 * @ejb.persistence table-name="gpsps"
 * @ejb.transaction type="Required"
 * @jboss.entity-command name="hsqldb-fetch-key"
 * @ejb.ejb-ref ejb-name="Code" view-type="local" ref-name="ejb/Code"
 * @ejb.ejb-ref ejb-name="RefRequest" view-type="local" ref-name="ejb/RefRequest"
 * 
 * @ejb.finder signature="org.dcm4chex.archive.ejb.interfaces.GPSPSLocal findBySopIuid(java.lang.String uid)"
 *             query="SELECT OBJECT(s) FROM GPSPS AS s WHERE s.sopIuid = ?1"
 *             transaction-type="Supports"
 */

public abstract class GPSPSBean implements EntityBean {

    private static final Logger log = Logger.getLogger(GPSPSBean.class);

    private static java.sql.Timestamp toTimestamp(java.util.Date date) {
        return date != null ? new java.sql.Timestamp(date.getTime()) : null;
    }
    
    private CodeLocalHome codeHome;
    private RefRequestLocalHome rqHome;

    public void setEntityContext(EntityContext ctx) {
        Context jndiCtx = null;
        try {
            jndiCtx = new InitialContext();
            codeHome = (CodeLocalHome) jndiCtx.lookup("java:comp/env/ejb/Code");
            rqHome = (RefRequestLocalHome) jndiCtx.lookup("java:comp/env/ejb/RefRequest");
        } catch (NamingException e) {
            throw new EJBException(e);
        } finally {
            if (jndiCtx != null) {
                try {
                    jndiCtx.close();
                } catch (NamingException ignore) {
                }
            }
        }
    }

    public void unsetEntityContext() {
        codeHome = null;
        rqHome = null;
    }
    
    /**
     * @ejb.create-method
     */
    public Integer ejbCreate(Dataset ds, PatientLocal patient) throws CreateException {
        setAttributes(ds);
        return null;
    }

    public void ejbPostCreate(Dataset ds, PatientLocal patient) throws CreateException {
        setPatient(patient);
        DcmElement sq;
        Collection c;
        try {
            setScheduledWorkItemCode(CodeBean.valueOf(codeHome, ds
                    .getItem(Tags.ScheduledWorkitemCodeSeq)));
            copyCodes(ds.get(Tags.ScheduledProcessingApplicationsCodeSeq),
                    getScheduledProcessingApplicationsCodes());
            copyCodes(ds.get(Tags.ScheduledStationNameCodeSeq),
                    getScheduledStationNameCodes());
            copyCodes(ds.get(Tags.ScheduledStationClassCodeSeq),
                    getScheduledStationClassCodes());
            copyCodes(ds.get(Tags.ScheduledStationGeographicLocationCodeSeq),
                    getScheduledStationGeographicLocationCodes());
            copyCodes(ds.get(Tags.ScheduledHumanPerformersSeq),
                    getScheduledHumanPerformerCodes());
            copyRefRequests(ds.get(Tags.RefRequestSeq),
                    getRefRequests());            
        } catch (CreateException e) {
            throw new CreateException(e.getMessage());
        } catch (FinderException e) {
            throw new CreateException(e.getMessage());
        }
        log.info("Created " + toString());
    }

    private void copyRefRequests(DcmElement sq, Collection c)
            throws CreateException, FinderException {
        for (int i = 0, n = sq.vm(); i < n; i++) {
            c.add(RefRequestBean.valueOf(rqHome, sq.getItem(i)));
        }
    }

    private void copyCodes(DcmElement sq, Collection c) throws CreateException,
            FinderException {
        for (int i = 0, n = sq.vm(); i < n; i++) {
            c.add(CodeBean.valueOf(codeHome, sq.getItem(i)));
        }
    }

    public void ejbRemove() throws RemoveException {
        log.info("Deleting " + toString());
    }

    /**
     * Auto-generated Primary Key
     *
     * @ejb.interface-method
     * @ejb.pk-field
     * @ejb.persistence column-name="pk"
     * @jboss.persistence auto-increment="true"
     */
    public abstract Integer getPk();

    public abstract void setPk(Integer pk);
    
    /**
     * @ejb.persistence column-name="gpsps_iuid"
     * @ejb.interface-method
     */
    public abstract String getSopIuid();

    public abstract void setSopIuid(String iuid);

    /**
     * @ejb.interface-method
     * @ejb.persistence column-name="start_datetime"
     */
    public abstract java.sql.Timestamp getSpsStartDateTime();
    public abstract void setSpsStartDateTime(java.sql.Timestamp dateTime);
    
    /**
     * @ejb.interface-method
     * @ejb.persistence column-name="end_datetime"
     */
    public abstract java.sql.Timestamp getExpectedCompletionDateTime();
    public abstract void setExpectedCompletionDateTime(java.sql.Timestamp time);
    
    /**
     * @ejb.persistence column-name="gpsps_status"
     */
    public abstract int getGpspsStatusAsInt();
    public abstract void setGpspsStatusAsInt(int status);

    /**
     * @ejb.interface-method
     */
    public String getGpspsStatus() {
        return GPSPSStatus.toString(getGpspsStatusAsInt());
    }

    public void setGpspsStatus(String status) {
        setGpspsStatusAsInt(GPSPSStatus.toInt(status));
    }

    /**
     * @ejb.persistence column-name="gpsps_prior"
     */
    public abstract int getGpspsPriorityAsInt();

    public abstract void setGpspsPriorityAsInt(int prior);

    /**
     * @ejb.interface-method
     */
    public String getGpspsPriority() {
        return GPSPSPriority.toString(getGpspsPriorityAsInt());
    }

    public void setGpspsPriority(String prior) {
        setGpspsPriorityAsInt(GPSPSPriority.toInt(prior));
    }

    /**
     * @ejb.persistence column-name="in_availability"
     */
    public abstract int getInputAvailabilityFlagAsInt();

    public abstract void setInputAvailabilityFlagAsInt(int availability);

    /**
     * @ejb.interface-method
     */
    public String getInputAvailabilityFlag() {
        return InputAvailabilityFlag.toString(getInputAvailabilityFlagAsInt());
    }

    public void setInputAvailabilityFlag(String availability) {
        setInputAvailabilityFlagAsInt(InputAvailabilityFlag.toInt(availability));
    }

    /**
     * @ejb.persistence column-name="item_attrs"
     */
    public abstract byte[] getEncodedAttributes();

    public abstract void setEncodedAttributes(byte[] bytes);

    /**
     * @ejb.interface-method view-type="local"
     * @ejb.relation name="patient-gpsps" role-name="gpsps-of-patient"
     *               cascade-delete="yes"
     * @jboss.relation fk-column="patient_fk" related-pk-field="pk"
     */
    public abstract void setPatient(PatientLocal patient);

    /**
     * @ejb.interface-method view-type="local"
     */
    public abstract PatientLocal getPatient();

    /**
     * @ejb.relation name="gpsps-workitemcode" role-name="gpsps-with-workitemcode"
     *               target-ejb="Code" target-role-name="workitemcode-of-gpsps"
     *               target-multiple="yes"
     * @jboss.relation fk-column="workitemcode_fk"
     *                 related-pk-field="pk"
     */
    public abstract CodeLocal getScheduledWorkItemCode();
    public abstract void setScheduledWorkItemCode(CodeLocal code);

    
    /**
     * @ejb.relation name="gpsps-appcode" role-name="gpsps-with-appcodes"
     *               target-ejb="Code" target-role-name="appcode-for-gpspss"
     *               target-multiple="yes"
     * @jboss.relation-table table-name="rel_gpsps_appcode"
     * @jboss.relation fk-column="appcode_fk" related-pk-field="pk"     
     * @jboss.target-relation fk-column="gpsps_fk" related-pk-field="pk"     
     */    
    public abstract java.util.Collection getScheduledProcessingApplicationsCodes();
    public abstract void setScheduledProcessingApplicationsCodes(java.util.Collection codes);
    
    /**
     * @ejb.relation name="gpsps-devnamecode" role-name="gpsps-with-devnamecodes"
     *               target-ejb="Code" target-role-name="devnamecode-for-gpspss"
     *               target-multiple="yes"
     * @jboss.relation-table table-name="rel_gpsps_devname"
     * @jboss.relation fk-column="devname_fk" related-pk-field="pk"     
     * @jboss.target-relation fk-column="gpsps_fk" related-pk-field="pk"     
     */    
    public abstract java.util.Collection getScheduledStationNameCodes();
    public abstract void setScheduledStationNameCodes(java.util.Collection codes);
    
    /**
     * @ejb.relation name="gpsps-devclasscode" role-name="gpsps-with-devclasscodes"
     *               target-ejb="Code" target-role-name="devclasscode-for-gpspss"
     *               target-multiple="yes"
     * @jboss.relation-table table-name="rel_gpsps_devclass"
     * @jboss.relation fk-column="devclass_fk" related-pk-field="pk"     
     * @jboss.target-relation fk-column="gpsps_fk" related-pk-field="pk"     
     */    
    public abstract java.util.Collection getScheduledStationClassCodes();
    public abstract void setScheduledStationClassCodes(java.util.Collection codes);
        
    /**
     * @ejb.relation name="gpsps-devloccode" role-name="gpsps-with-devloccodes"
     *               target-ejb="Code" target-role-name="devloccode-for-gpspss"
     *               target-multiple="yes"
     * @jboss.relation-table table-name="rel_gpsps_devloc"
     * @jboss.relation fk-column="devloc_fk" related-pk-field="pk"     
     * @jboss.target-relation fk-column="gpsps_fk" related-pk-field="pk"     
     */    
    public abstract java.util.Collection getScheduledStationGeographicLocationCodes();
    public abstract void setScheduledStationGeographicLocationCodes(java.util.Collection codes);

    /**
     * @ejb.relation name="gpsps-performercode" role-name="gpsps-with-performercodes"
     *               target-ejb="Code" target-role-name="performercode-for-gpspss"
     *               target-multiple="yes"
     * @jboss.relation-table table-name="rel_gpsps_human"
     * @jboss.relation fk-column="human_fk" related-pk-field="pk"     
     * @jboss.target-relation fk-column="gpsps_fk" related-pk-field="pk"     
     */    
    public abstract java.util.Collection getScheduledHumanPerformerCodes();
    public abstract void setScheduledHumanPerformerCodes(java.util.Collection codes);
    
    /**
     * @ejb.relation name="gpsps-request" role-name="gpsps-for-requests"
     *               target-ejb="Code" target-role-name="request-of-gpspss"
     *               target-multiple="yes"
     * @jboss.relation-table table-name="rel_gpsps_request"
     * @jboss.relation fk-column="request_fk" related-pk-field="pk"     
     * @jboss.target-relation fk-column="gpsps_fk" related-pk-field="pk"     
     */    
    public abstract java.util.Collection getRefRequests();
    public abstract void setRefRequests(java.util.Collection refRequests);
    
    /**
     * @ejb.interface-method
     */
    public Dataset getAttributes() {
        return DatasetUtils.fromByteArray(
                getEncodedAttributes(), DcmDecodeParam.EVR_LE, null);
    }

    /**
     * @ejb.interface-method
     */
    public void setAttributes(Dataset ds) {
        setSopIuid(ds.getString(Tags.SOPInstanceUID));
        setGpspsStatus(ds.getString(Tags.GPSPSStatus));
        setGpspsPriority(ds.getString(Tags.GPSPSPriority));
        setInputAvailabilityFlag(ds.getString(Tags.InputAvailabilityFlag));
        setSpsStartDateTime(toTimestamp(ds.getDate(Tags.SPSStartDateAndTime)));
        setExpectedCompletionDateTime(toTimestamp(ds.getDate(Tags.ExpectedCompletionDateAndTime)));
        setEncodedAttributes(
                DatasetUtils.toByteArray(ds, DcmDecodeParam.EVR_LE));
    }
}
