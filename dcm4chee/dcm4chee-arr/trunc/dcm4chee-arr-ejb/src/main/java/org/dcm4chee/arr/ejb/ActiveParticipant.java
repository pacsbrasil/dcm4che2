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
import java.util.Collection;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision$ $Date$
 * @since Jun 13, 2006
 *
 */
@Entity
@Table(name = "active_particpant")
public class ActiveParticipant implements Serializable {
    private int pk;
    private AuditRecord auditRecord;
    private String userID;
    private String alternativeUserID;
    private String userName;
    private boolean requestor;
    private String networkAccessPointID;
    private int networkAccessPointType;
    private Collection<Code> roleIDCode;  
    
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

    @ManyToMany(targetEntity=Code.class)
    @JoinTable(
            name = "rel_act_part_role", 
            joinColumns = {@JoinColumn(name = "act_participant_fk")},
            inverseJoinColumns = {@JoinColumn(name = "code_fk")})
    public Collection<Code> getRoleIDCode() {
        return roleIDCode;
    }

    public void setRoleIDCode(Collection<Code> roleIDCode) {
        this.roleIDCode = roleIDCode;
    }
    
    @Column(name="user_id")
    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    @Column(name="alt_user_id")
    public String getAlternativeUserID() {
        return alternativeUserID;
    }

    public void setAlternativeUserID(String alternativeUserID) {
        this.alternativeUserID = alternativeUserID;
    }

    @Column(name = "user_name")
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Column(name = "requestor")
    public boolean getRequestor() {
        return requestor;
    }

    public void setRequestor(boolean requestor) {
        this.requestor = requestor;
    }

    @Column(name = "net_access_pt_id")
    public String getNetworkAccessPointID() {
        return networkAccessPointID;
    }

    public void setNetworkAccessPointID(String id) {
        this.networkAccessPointID = id;
    }

    @Column(name = "net_access_pt_type")
    public int getNetworkAccessPointType() {
        return networkAccessPointType;
    }

    public void setNetworkAccessPointType(int code) {
        this.networkAccessPointType = code;
    }
}
