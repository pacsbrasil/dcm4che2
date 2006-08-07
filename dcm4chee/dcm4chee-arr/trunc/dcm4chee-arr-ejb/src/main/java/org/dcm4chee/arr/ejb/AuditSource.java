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
 * @since Jun 17, 2006
 *
 */
@Entity
@Table(name = "audit_source")
public class AuditSource implements Serializable {
    private int pk;
    private AuditRecord auditRecord;
    private String enterpriseSiteID;
    private String sourceID;
    private int sourceTypeCode;
    private int sourceTypeCode2;    
    private int sourceTypeCode3;    

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

    @Column(name = "site_id")
    public String getEnterpriseSiteID() {
        return enterpriseSiteID;
    }
    
    public void setEnterpriseSiteID(String auditEnterpriseSiteID) {
        this.enterpriseSiteID = auditEnterpriseSiteID;
    }
    
    @Column(name = "source_id")
    public String getSourceID() {
        return sourceID;
    }
    
    public void setSourceID(String auditSourceID) {
        this.sourceID = auditSourceID;
    }

    @Column(name = "source_type")
    public int getSourceTypeCode() {
        return sourceTypeCode;
    }

    public void setSourceTypeCode(int typeCode) {
        this.sourceTypeCode = typeCode;
    }

    @Column(name = "source_type2")
    public int getSourceTypeCode2() {
        return sourceTypeCode2;
    }

    public void setSourceTypeCode2(int typeCode) {
        this.sourceTypeCode2 = typeCode;
    }


    @Column(name = "source_type3")
    public int getSourceTypeCode3() {
        return sourceTypeCode3;
    }

    public void setSourceTypeCode3(int typeCode) {
        this.sourceTypeCode3 = typeCode;
    }
}
