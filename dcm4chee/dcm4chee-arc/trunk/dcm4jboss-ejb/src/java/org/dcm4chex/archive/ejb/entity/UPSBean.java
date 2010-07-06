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
 * Agfa HealthCare.
 * Portions created by the Initial Developer are Copyright (C) 2010
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See @authors listed below.
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
import org.dcm4che.data.DcmElement;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4chex.archive.common.DatasetUtils;
import org.dcm4chex.archive.common.Priority;
import org.dcm4chex.archive.common.UPSState;
import org.dcm4chex.archive.ejb.interfaces.CodeLocal;
import org.dcm4chex.archive.ejb.interfaces.CodeLocalHome;
import org.dcm4chex.archive.ejb.interfaces.PatientLocal;
import org.dcm4chex.archive.ejb.interfaces.UPSLocal;
import org.dcm4chex.archive.ejb.interfaces.UPSRelatedPSLocal;
import org.dcm4chex.archive.ejb.interfaces.UPSRelatedPSLocalHome;
import org.dcm4chex.archive.ejb.interfaces.UPSReplacedPSLocal;
import org.dcm4chex.archive.ejb.interfaces.UPSReplacedPSLocalHome;
import org.dcm4chex.archive.ejb.interfaces.UPSRequestLocal;
import org.dcm4chex.archive.ejb.interfaces.UPSRequestLocalHome;
import org.dcm4chex.archive.ejb.interfaces.UPSSubscriptionLocal;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date:: xxxx-xx-xx $
 * @since Mar 29, 2010
 * 
 * @ejb.bean name="UPS" type="CMP" view-type="local" primkey-field="pk"
 *           local-jndi-name="ejb/UPS"
 * @ejb.transaction type="Required"
 * @ejb.persistence table-name="ups"
 * 
 * @jboss.container-configuration name="Instance Per Transaction CMP 2.x EntityBean"
 * @jboss.entity-command name="hsqldb-fetch-key"
 * @jboss.audit-created-time field-name="createdTime"
 * @jboss.audit-updated-time field-name="updatedTime"
 * 
 * @ejb.finder signature="org.dcm4chex.archive.ejb.interfaces.UPSLocal findBySopInstanceUID(java.lang.String uid)"
 *                 query="SELECT OBJECT(ups) FROM UPS AS ups WHERE ups.sopInstanceUID = ?1"
 *             transaction-type="Supports"
 *
 * @ejb.ejb-ref ejb-name="Code" view-type="local" ref-name="ejb/Code"
 * @ejb.ejb-ref ejb-name="UPSRequest" view-type="local" ref-name="ejb/UPSRequest"
 * @ejb.ejb-ref ejb-name="UPSRelatedPS" view-type="local" ref-name="ejb/UPSRelatedPS"
 * @ejb.ejb-ref ejb-name="UPSReplacedPS" view-type="local" ref-name="ejb/UPSReplacedPS"
 */
public abstract class UPSBean implements EntityBean {

    private static final Logger LOG = Logger.getLogger(UPSBean.class);

    private EntityContext ejbctx;
    private CodeLocalHome codeHome;
    private UPSRequestLocalHome rqHome;
    private UPSRelatedPSLocalHome relPSHome;
    private UPSReplacedPSLocalHome replPSHome;

    public void setEntityContext(EntityContext ctx) {
        ejbctx = ctx;
        Context jndiCtx = null;
        try {
            jndiCtx = new InitialContext();
            codeHome = (CodeLocalHome)
                    jndiCtx.lookup("java:comp/env/ejb/Code");
            rqHome = (UPSRequestLocalHome) 
                    jndiCtx.lookup("java:comp/env/ejb/UPSRequest");
            relPSHome = (UPSRelatedPSLocalHome)
                    jndiCtx.lookup("java:comp/env/ejb/UPSRelatedPS");
            replPSHome = (UPSReplacedPSLocalHome)
                    jndiCtx.lookup("java:comp/env/ejb/UPSReplacedPS");
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
        relPSHome = null;
        replPSHome = null;
        ejbctx = null;
    }

    /**
     * @ejb.create-method
     */
    public Long ejbCreate(Dataset ds, PatientLocal patient) throws CreateException {
        setAttributes(ds);
        return null;
    }

    public void ejbPostCreate(Dataset ds, PatientLocal patient)
    throws CreateException {
        setPatient(patient);
        try {
            setScheduledWorkItemCode(CodeBean.valueOf(codeHome,
                        ds.getItem(Tags.ScheduledWorkitemCodeSeq)));
            CodeBean.addCodesTo(codeHome, 
                        ds.get(Tags.ScheduledProcessingApplicationsCodeSeq),
                    getScheduledProcessingApplicationsCodes());
            CodeBean.addCodesTo(codeHome,
                        ds.get(Tags.ScheduledStationNameCodeSeq),
                    getScheduledStationNameCodes());
            CodeBean.addCodesTo(codeHome,
                        ds.get(Tags.ScheduledStationClassCodeSeq),
                    getScheduledStationClassCodes());
            CodeBean.addCodesTo(codeHome,
                        ds.get(Tags.ScheduledStationGeographicLocationCodeSeq),
                    getScheduledStationGeographicLocationCodes());
            updateScheduledHumanPerformers(
                    ds.get(Tags.ScheduledHumanPerformersSeq));
            updateRefRequests(ds.get(Tags.RefRequestSeq));
            updateRelatedPS(ds.get(Tags.RelatedProcedureStepSeq));
            // TODO Tags.ReplacedProcedureStepSeq not yet defined
            // updateReplacedPS(ds.get(Tags.ReplacedProcedureStepSeq)
        } catch (Exception e) {
            throw new EJBException(e);
        }
        LOG.info(prompt("Created UPS[pk="));
        if (LOG.isDebugEnabled()) {
            LOG.debug(ds);
        }
    }

    public void ejbRemove() throws RemoveException {
        LOG.info(prompt("Deleting UPS[pk="));
    }

    private Object prompt(String prefix) {
        return prefix + getPk()
                + ", uid=" + getSopInstanceUID()
                + "]";
    }

    private void updateScheduledHumanPerformers(DcmElement sq)
            throws CreateException, FinderException {
        if (sq == null) return;
        Collection<CodeLocal> c = getScheduledHumanPerformerCodes();
        c.clear();
        for (int i = 0, n = sq.countItems(); i < n; i++) {
            DcmElement codeSq = sq.getItem(i).get(Tags.HumanPerformerCodeSeq);
            Dataset code;
            if (codeSq != null && (code = codeSq.getItem()) != null)
                c.add(CodeBean.valueOf(codeHome, code));
        }
    }
    private void updateRefRequests(DcmElement sq) throws CreateException {
        if (sq == null) return;
        Collection<UPSRequestLocal> c = getRefRequests();
        c.clear();
        UPSLocal ups = (UPSLocal) ejbctx.getEJBLocalObject();
        for (int i = 0, n = sq.countItems(); i < n; i++)
            c.add(rqHome.create(sq.getItem(i), ups));
    }

    private void updateRelatedPS(DcmElement sq) throws CreateException {
        if (sq == null) return;
        Collection<UPSRelatedPSLocal> c = getRelatedProcedureSteps();
        c.clear();
        UPSLocal ups = (UPSLocal) ejbctx.getEJBLocalObject();
        for (int i = 0, n = sq.countItems(); i < n; i++)
            c.add(relPSHome.create(sq.getItem(i), ups));
    }

    private void updateReplacedPS(DcmElement sq) throws CreateException {
        if (sq == null) return;
        Collection<UPSReplacedPSLocal> c = getReplacedProcedureSteps();
        c.clear();
        UPSLocal ups = (UPSLocal) ejbctx.getEJBLocalObject();
        for (int i = 0, n = sq.countItems(); i < n; i++)
            c.add(replPSHome.create(sq.getItem(i), ups));
    }

    /**
     * Auto-generated Primary Key
     * 
     * @ejb.interface-method
     * @ejb.pk-field
     * @ejb.persistence column-name="pk"
     * @jboss.persistence auto-increment="true"
     */
    public abstract Long getPk();

    public abstract void getPk(Long pk);

    /**
     * @ejb.persistence column-name="ups_iuid"
     * @ejb.interface-method
     */
    public abstract String getSopInstanceUID();

    public abstract void setSopInstanceUID(String iuid);

    /**
     * @ejb.persistence column-name="ups_tuid"
     * @ejb.interface-method
     */
    public abstract String getTransactionUID();

    /**
     * @ejb.interface-method
     */
    public abstract void setTransactionUID(String iuid);

    /**
     * @ejb.persistence column-name="adm_id"
     * @ejb.interface-method
     */
    public abstract String getAdmissionID();

    public abstract void setAdmissionID(String admissionID);

    /**
     * @ejb.persistence column-name="adm_id_issuer_id"
     * @ejb.interface-method
     */
    public abstract String getIssuerOfAdmissionIDLocalNamespaceEntityID();

    public abstract void setIssuerOfAdmissionIDLocalNamespaceEntityID(String id);

    /**
     * @ejb.persistence column-name="adm_id_issuer_uid"
     * @ejb.interface-method
     */
    public abstract String getIssuerOfAdmissionIDUniversialEntityID();

    public abstract void setIssuerOfAdmissionIDUniversialEntityID(String uid);

    /**
     * @ejb.persistence column-name="ups_label"
     * @ejb.interface-method
     */
    public abstract String getProcedureStepLabel();

    public abstract void setProcedureStepLabel(String label);

    /**
     * @ejb.persistence column-name="uwl_label"
     * @ejb.interface-method
     */
    public abstract String getWorklistLabel();

    public abstract void setWorklistLabel(String label);

    /**
     * @ejb.interface-method
     * @ejb.persistence column-name="ups_start_time"
     */
    public abstract java.sql.Timestamp getScheduledStartDateTime();

    public abstract void setScheduledStartDateTime(java.sql.Timestamp dateTime);

    /**
     * @ejb.interface-method
     * @ejb.persistence column-name="ups_compl_time"
     */
    public abstract java.sql.Timestamp getExpectedCompletionDateTime();

    public abstract void setExpectedCompletionDateTime(java.sql.Timestamp time);

    /**
     * @ejb.interface-method
     * @ejb.persistence column-name="ups_state"
     */
    public abstract int getStateAsInt();

    public abstract void setStateAsInt(int state);

    /**
     * @ejb.persistence column-name="ups_prior"
     */
    public abstract int getPriorityAsInt();

    public abstract void setPriorityAsInt(int prior);

    /**
     * @ejb.interface-method
     * @ejb.persistence column-name="created_time"
     */
    public abstract java.sql.Timestamp getCreatedTime();

    public abstract void setCreatedTime(java.sql.Timestamp time);

    /**
     * @ejb.interface-method
     * @ejb.persistence column-name="updated_time"
     */
    public abstract java.sql.Timestamp getUpdatedTime();

    public abstract void setUpdatedTime(java.sql.Timestamp time);

    /**
     * @ejb.persistence column-name="ups_attrs"
     */
    public abstract byte[] getEncodedAttributes();

    public abstract void setEncodedAttributes(byte[] bytes);

    /**
     * @ejb.interface-method
     * @ejb.relation name="patient-ups" role-name="ups-of-patient"
     *               cascade-delete="yes"
     * @jboss.relation fk-column="patient_fk" related-pk-field="pk"
     */
    public abstract void setPatient(PatientLocal patient);

    /**
     * @ejb.interface-method view-type="local"
     */
    public abstract PatientLocal getPatient();

    /**
     * @ejb.relation name="ups-workitemcode" role-name="ups-with-workitemcode"
     *               target-ejb="Code" target-role-name="workitemcode-of-ups"
     *               target-multiple="yes"
     * @jboss.relation fk-column="code_fk" related-pk-field="pk"
     */
    public abstract CodeLocal getScheduledWorkItemCode();

    public abstract void setScheduledWorkItemCode(CodeLocal code);

    /**
     * @ejb.relation name="ups-appcode" role-name="ups-with-appcodes"
     *               target-ejb="Code" target-role-name="appcode-for-upss"
     *               target-multiple="yes"
     * @jboss.relation-table table-name="rel_ups_appcode"
     * @jboss.relation fk-column="appcode_fk" related-pk-field="pk"
     * @jboss.target-relation fk-column="ups_fk" related-pk-field="pk"
     */
    public abstract Collection<CodeLocal> getScheduledProcessingApplicationsCodes();

    public abstract void setScheduledProcessingApplicationsCodes(
            Collection<CodeLocal> codes);

    /**
     * @ejb.relation name="ups-devnamecode" role-name="ups-with-devnamecodes"
     *               target-ejb="Code" target-role-name="devnamecode-for-upss"
     *               target-multiple="yes"
     * @jboss.relation-table table-name="rel_ups_devname"
     * @jboss.relation fk-column="devname_fk" related-pk-field="pk"
     * @jboss.target-relation fk-column="ups_fk" related-pk-field="pk"
     */
    public abstract Collection<CodeLocal> getScheduledStationNameCodes();

    public abstract void setScheduledStationNameCodes(
            Collection<CodeLocal> codes);

    /**
     * @ejb.relation name="ups-devclasscode" role-name="ups-with-devclasscodes"
     *               target-ejb="Code" target-role-name="devclasscode-for-upss"
     *               target-multiple="yes"
     * @jboss.relation-table table-name="rel_ups_devclass"
     * @jboss.relation fk-column="devclass_fk" related-pk-field="pk"
     * @jboss.target-relation fk-column="ups_fk" related-pk-field="pk"
     */
    public abstract Collection<CodeLocal> getScheduledStationClassCodes();

    public abstract void setScheduledStationClassCodes(
            Collection<CodeLocal> codes);

    /**
     * @ejb.relation name="ups-devloccode" role-name="ups-with-devloccodes"
     *               target-ejb="Code" target-role-name="devloccode-for-upss"
     *               target-multiple="yes"
     * @jboss.relation-table table-name="rel_ups_devloc"
     * @jboss.relation fk-column="devloc_fk" related-pk-field="pk"
     * @jboss.target-relation fk-column="ups_fk" related-pk-field="pk"
     */
    public abstract Collection<CodeLocal> getScheduledStationGeographicLocationCodes();

    public abstract void setScheduledStationGeographicLocationCodes(
            Collection<CodeLocal> codes);

    /**
     * @ejb.relation name="ups-performercode" role-name="ups-with-performercodes"
     *               target-ejb="Code" target-role-name="performercode-for-upss"
     *               target-multiple="yes"
     * @jboss.relation-table table-name="rel_ups_performer"
     * @jboss.relation fk-column="performer_fk" related-pk-field="pk"
     * @jboss.target-relation fk-column="ups_fk" related-pk-field="pk"
     */
    public abstract Collection<CodeLocal> getScheduledHumanPerformerCodes();

    public abstract void setScheduledHumanPerformerCodes(Collection<CodeLocal> codes);

    /**
     * @ejb.interface-method
     * @ejb.relation name="ups-request" role-name="ups-for-requests"
     */
    public abstract Collection<UPSRequestLocal> getRefRequests();

    public abstract void setRefRequests(Collection<UPSRequestLocal> refRequests);

    /**
     * @ejb.relation name="ups-related-ps" role-name="ups-with-related-ps"
     */
    public abstract Collection<UPSRelatedPSLocal> getRelatedProcedureSteps();

    public abstract void setRelatedProcedureSteps(Collection<UPSRelatedPSLocal> relPSs);

    /**
     * @ejb.relation name="ups-replaced-ps" role-name="ups-with-replaced-ps"
     */
    public abstract Collection<UPSReplacedPSLocal> getReplacedProcedureSteps();

    public abstract void setReplacedProcedureSteps(Collection<UPSReplacedPSLocal> relPSs);

    /**
     * @ejb.interface-method
     * @ejb.relation name="ups-subscription" role-name="ups-with-subscriptions"
     */
    public abstract Collection<UPSSubscriptionLocal> getSubscriptions();

    public abstract void setSubscriptions(Collection<UPSSubscriptionLocal> subscriptions);

    /**
     * @ejb.interface-method
     */
    public Dataset getAttributes() {
        return DatasetUtils.fromByteArray(getEncodedAttributes());
    }

    private static java.sql.Timestamp toTimestamp(java.util.Date date) {
        return date != null ? new java.sql.Timestamp(date.getTime()) : null;
    }

    /**
     * @ejb.interface-method
     */
    public boolean updateState(int newState) {
        if (getStateAsInt() == newState)
            return false;
        setStateAsInt(newState);
        Dataset ds = getAttributes();
        ds.putCS(Tags.UPSState, UPSState.toString(newState));
        setEncodedAttributes(DatasetUtils.toByteArray(ds,
                UIDs.DeflatedExplicitVRLittleEndian));
        return true;
    }

    /**
     * @ejb.interface-method
     */
    public void setAttributes(Dataset ds) {
        setSopInstanceUID(ds.getString(Tags.SOPInstanceUID));
        setAdmissionID(ds.getString(Tags.AdmissionID));
        Dataset issuer = ds.getItem(Tags.IssuerOfAdmissionIDSeq);
        if (issuer != null) {
            setIssuerOfAdmissionIDLocalNamespaceEntityID(
                    issuer.getString(Tags.LocalNamespaceEntityID));
            setIssuerOfAdmissionIDUniversialEntityID(
                    issuer.getString(Tags.UniversalEntityID));
        }
        setProcedureStepLabel(ds.getString(Tags.ProcedureStepLabel));
        setWorklistLabel(ds.getString(Tags.WorklistLabel));
        setStateAsInt(UPSState.toInt(ds.getString(Tags.UPSState)));
        setPriorityAsInt(Priority.toInt(ds.getString(Tags.SPSPriority)));
        setScheduledStartDateTime(toTimestamp(ds.getDate(Tags.SPSStartDateAndTime)));
        setExpectedCompletionDateTime(
                toTimestamp(ds.getDate(Tags.ExpectedCompletionDateAndTime)));
        setEncodedAttributes(DatasetUtils.toByteArray(ds,
                UIDs.DeflatedExplicitVRLittleEndian));
    }

    /**
     * @ejb.interface-method
     */
    public void updateAttributes(Dataset newAttrs) {
        try {
            updateWorkitemCode(newAttrs.getItem(Tags.ScheduledWorkitemCodeSeq));
            updateScheduledProcessingApplicationsCodes(
                    newAttrs.get(Tags.ScheduledProcessingApplicationsCodeSeq));
            updateScheduledStationNameCodes(
                    newAttrs.get(Tags.ScheduledStationNameCodeSeq));
            updateScheduledStationClassCodes(
                    newAttrs.get(Tags.ScheduledStationClassCodeSeq));
            updateScheduledStationGeographicLocationCodes(
                    newAttrs.get(Tags.ScheduledStationGeographicLocationCodeSeq));
            updateScheduledHumanPerformers(
                    newAttrs.get(Tags.ScheduledHumanPerformersSeq));
            updateRefRequests(newAttrs.get(Tags.RefRequestSeq));
            updateRelatedPS(newAttrs.get(Tags.RelatedProcedureStepSeq));
        } catch (Exception e) {
            throw new EJBException(e);
        }
        Dataset ds = getAttributes();
        ds.putAll(newAttrs, Dataset.REPLACE_ITEMS);
        setAttributes(ds);
    }

    private void updateWorkitemCode(Dataset newCode)
            throws CreateException, FinderException {
        if (newCode != null)
            setScheduledWorkItemCode(CodeBean.valueOf(codeHome, newCode));
    }

    private void updateScheduledProcessingApplicationsCodes(DcmElement newCodes)
            throws CreateException, FinderException {
        if (newCodes != null) {
            Collection<CodeLocal> codes = getScheduledProcessingApplicationsCodes();
            codes.clear();
            CodeBean.addCodesTo(codeHome, newCodes, codes);
        }
    }

    private void updateScheduledStationNameCodes(DcmElement newCodes)
            throws CreateException, FinderException {
        if (newCodes != null) {
            Collection<CodeLocal> codes = getScheduledStationNameCodes();
            codes.clear();
            CodeBean.addCodesTo(codeHome, newCodes, codes);
        }
    }

    private void updateScheduledStationClassCodes(DcmElement newCodes)
            throws CreateException, FinderException {
        if (newCodes != null) {
            Collection<CodeLocal> codes = getScheduledStationClassCodes();
            codes.clear();
            CodeBean.addCodesTo(codeHome, newCodes, codes);
        }
    }

    private void updateScheduledStationGeographicLocationCodes(
            DcmElement newCodes) throws CreateException, FinderException {
        if (newCodes != null) {
            Collection<CodeLocal> codes =
                    getScheduledStationGeographicLocationCodes();
            codes.clear();
            CodeBean.addCodesTo(codeHome, newCodes, codes);
        }
    }

    /**
     * @ejb.select query="SELECT ups.sopInstanceUID FROM UPS AS ups WHERE ups.subscriptions IS EMPTY"
     */ 
    public abstract Collection<String> ejbSelectNotSubscribed(String aet)
            throws FinderException;

    /**
     * @ejb.home-method
     */
    public Collection<String> ejbHomeGetNotSubscribed(String aet)
            throws FinderException {
        return ejbSelectNotSubscribed(aet);
    }

}
