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
 * TIANI Medgraph AG.
 * Portions created by the Initial Developer are Copyright (C) 2003-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunter.zeilinger@tiani.com>
 * Franz Willer <franz.willer@gwi-ag.com>
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

import javax.ejb.CreateException;
import javax.ejb.EntityBean;

import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;

/**
 * @ejb.bean name="Association" type="CMP" view-type="local" primkey-field="pk"
 *           local-jndi-name="ejb/Association"
 * 
 * @ejb.transaction type="Required"
 * @ejb.persistence table-name="assoc"
 * @jboss.entity-command name="hsqldb-fetch-key"
 * @jboss.audit-created-time field-name="createdTime"
 * @jboss.audit-updated-time field-name="updatedTime"
 * 
 * @ejb.finder signature="java.util.Collection findNotUpdatedSince(java.sql.Timestamp time)"
 *             query="SELECT OBJECT(a) FROM Association a WHERE a.updatedTime < ?1"
 *             transaction-type="Supports"
 * 
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision$ $Date$
 * @since May 3, 2006
 *
 */
public abstract class AssociationBean implements EntityBean {

    /**
     * @ejb.create-method
     */
    public Integer ejbCreate(String callingAET, String calledAET,
            String retrieveAET) throws CreateException {
        setCallingAET(callingAET);
        setCalledAET(calledAET);
        setRetrieveAET(retrieveAET);
        return null;
    }

    public void ejbPostCreate(String callingAET, String calledAET,
            String retrieveAET) throws CreateException {
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
     * @ejb.interface-method
     * @ejb.persistence column-name="calling_aet"
     */
    public abstract String getCallingAET();
    public abstract void setCallingAET(String aet);

    /**
     * @ejb.interface-method
     * @ejb.persistence column-name="called_aet"
     */
    public abstract String getCalledAET();
    public abstract void setCalledAET(String aet);

    /**
     * @ejb.interface-method
     * @ejb.persistence column-name="retrieve_aet"
     */
    public abstract String getRetrieveAET();
    public abstract void setRetrieveAET(String aet);
    
    /**
     * @ejb.interface-method
     * @ejb.persistence column-name="pat_id"
     */
    public abstract String getPatientId();
    public abstract void setPatientId(String pid);

    /**
     * @ejb.interface-method
     * @ejb.persistence column-name="pat_name"
     */
    public abstract String getPatientName();
    public abstract void setPatientName(String name);

    /**
     * @ejb.interface-method
     * @ejb.persistence column-name="accession_no"
     */
    public abstract String getAccessionNumber();
    public abstract void setAccessionNumber(String no);
    
    /**
     * @ejb.interface-method
     * @ejb.persistence column-name="study_iuid"
     */
    public abstract String getStudyInstanceUID();
    public abstract void setStudyInstanceUID(String uid);

    /**
     * @ejb.interface-method
     * @ejb.persistence column-name="series_iuid"
     */
    public abstract String getSeriesInstanceUID();
    public abstract void setSeriesInstanceUID(String uid);
    
    /**
     * @ejb.interface-method
     * @ejb.persistence column-name="pps_iuid"
     */
    public abstract String getPPSInstanceUID();
    public abstract void setPPSInstanceUID(String uid);
    
    /**
     * @ejb.interface-method
     * @ejb.persistence column-name="pps_cuid"
     */
    public abstract String getPPSClassUID();
    public abstract void setPPSClassUID(String uid);
    
    /**
     * @ejb.relation name="assoc-files" role-name="assoc-store-files"
     *               target-ejb="File" target-role-name="file-stored-in-assoc"
     *               target-multiple="yes"
     * @jboss.relation-table table-name="rel_assoc_files"
     * @jboss.relation fk-column="files_fk" related-pk-field="pk"     
     * @jboss.target-relation fk-column="assoc_fk" related-pk-field="pk"     
     *    
     * @ejb.interface-method
     */
    public abstract java.util.Collection getFiles();
    public abstract void setFiles(java.util.Collection files);
    
    /**
     * @ejb.interface-method
     */
    public void setAttributes(Dataset attrs) {
        setPatientId(attrs.getString(Tags.PatientID));
        setPatientName(attrs.getString(Tags.PatientName));
        setAccessionNumber(attrs.getString(Tags.AccessionNumber));
        setStudyInstanceUID(attrs.getString(Tags.StudyInstanceUID));
        setSeriesInstanceUID(attrs.getString(Tags.SeriesInstanceUID));
        Dataset refpps = attrs.getItem(Tags.RefPPSSeq);
        if (refpps == null) {
            setPPSInstanceUID(null);
            setPPSClassUID(null);
        } else {
            setPPSInstanceUID(refpps.getString(Tags.RefSOPInstanceUID));
            setPPSClassUID(refpps.getString(Tags.RefSOPClassUID));
        }
    }

}
