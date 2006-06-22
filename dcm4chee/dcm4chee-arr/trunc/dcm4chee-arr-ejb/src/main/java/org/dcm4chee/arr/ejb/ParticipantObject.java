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
 * Java(TM), hosted at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Gunter Zeilinger, Huetteldorferstr. 24/10, 1150 Vienna/Austria/Europe.
 * Portions created by the Initial Developer are Copyright (C) 2002-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunterze@gmail.com>
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

package org.dcm4chee.arr.ejb;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision$ $Date$
 * @since Jun 13, 2006
 *
 */
@Entity
@Table(name = "participant_obj")
public class ParticipantObject implements Serializable {
    private int pk;
    private AuditRecord auditRecord;
    private String participantObjectID;
    private int participantObjectTypeCode;
    private int participantObjectTypeCodeRole;
    private int participantObjectDataLifeCycle;
    private String participantObjectSensitivity;
    private int participantObjectIDTypeCodeRFC;
    private Code participantObjectIDTypeCode;
    private String participantObjectName;
    
    @Id
    @GeneratedValue
    @Column(name="pk")
    public int getPk() {
        return pk;
    }
    
    public void setPk(int pk) {
        this.pk = pk;
    }

    @ManyToOne
    @JoinColumn(name="audit_record_fk")
    public AuditRecord getAuditRecord() {
        return auditRecord;
    }

    public void setAuditRecord(AuditRecord auditRecord) {
        this.auditRecord = auditRecord;
    }

    @ManyToOne
    @JoinColumn(name="id_type_code_fk")
    public Code getParticipantObjectIDTypeCode() {
        return participantObjectIDTypeCode;
    }

    public void setParticipantObjectIDTypeCode(Code code) {
        this.participantObjectIDTypeCode = code;
    }

    @Column(name="part_obj_id_type")
    public int getParticipantObjectIDTypeCodeRFC() {
        return participantObjectIDTypeCodeRFC;
    }

    public void setParticipantObjectIDTypeCodeRFC(int code) {
        this.participantObjectIDTypeCodeRFC = code;
    }

    @Column(name="part_obj_name")
    public String getParticipantObjectName() {
        return participantObjectName;
    }

    public void setParticipantObjectName(String name) {
        this.participantObjectName = name;
    }

    @Column(name="part_obj_id")
    public String getParticipantObjectID() {
        return participantObjectID;
    }

    public void setParticipantObjectID(String id) {
        this.participantObjectID = id;
    }

    @Column(name="part_obj_type")
    public int getParticipantObjectTypeCode() {
        return participantObjectTypeCode;
    }

    public void setParticipantObjectTypeCode(int code) {
        this.participantObjectTypeCode = code;
    }

    @Column(name="part_obj_role")
    public int getParticipantObjectTypeCodeRole() {
        return participantObjectTypeCodeRole;
    }

    public void setParticipantObjectTypeCodeRole(int code) {
        this.participantObjectTypeCodeRole = code;
    }

    @Column(name="part_obj_data_lc")
    public int getParticipantObjectDataLifeCycle() {
        return participantObjectDataLifeCycle;
    }

    public void setParticipantObjectDataLifeCycle(int code) {
        this.participantObjectDataLifeCycle = code;
    }

    @Column(name="part_obj_vip")
    public String getParticipantObjectSensitivity() {
        return participantObjectSensitivity;
    }

    public void setParticipantObjectSensitivity(String sensitivity) {
        this.participantObjectSensitivity = sensitivity;
    }

}
