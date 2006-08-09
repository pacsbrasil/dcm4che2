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
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.dcm4chee.arr.util.XSLTUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision$ $Date$
 * @since Jun 5, 2006
 *
 */
@Entity
@Name("audit_record")
@Scope(ScopeType.PAGE)
@Table(name = "audit_record")
public class AuditRecord implements Serializable {
    private int pk;
    private Code eventID;
    private String eventActionCode;
    private int eventOutcomeIndicator;
    private Date eventDateTime;
    private Date receiveDateTime;
    private Collection<Code> eventTypeCode;  
    private Collection<ActiveParticipant> activeParticipant;
    private Collection<AuditSource> auditSource;
    private Collection<ParticipantObject> participantObject;
    private boolean iheYr4;
    private byte[] xmldata;
    @Transient
    private String summary;

    @Id
    @GeneratedValue
    @Column(name = "pk")
    public int getPk() {
        return pk;
    }
    public void setPk(int pk) {
        this.pk = pk;
    }
    
    @ManyToOne
    @JoinColumn(name="event_id_fk")
    public Code getEventID() {
        return eventID;
    }

    public void setEventID(Code eventID) {
        this.eventID = eventID;
    }    

    @ManyToMany(targetEntity=Code.class)
    @JoinTable(
            name = "rel_event_type", 
            joinColumns = {@JoinColumn(name = "audit_record_fk")},
            inverseJoinColumns = {@JoinColumn(name = "code_fk")})
    public Collection<Code> getEventTypeCode() {
        return eventTypeCode;
    }

    public void setEventTypeCode(Collection<Code> eventTypeCode) {
        this.eventTypeCode = eventTypeCode;
    }
    
    @Transactional
    public String getSummary() {
//	if (summary == null) {
	    summary = XSLTUtils.toSummary(xmldata);
//	}
	return summary;
    }

    @Transactional
    public void setSummary(String summary) {
	this.summary = summary;
    }
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy="auditRecord")
    public Collection<ActiveParticipant> getActiveParticipant() {
        return activeParticipant;
    }
        
    public void setActiveParticipant(Collection<ActiveParticipant> c) {
        this.activeParticipant = c;
    }
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy="auditRecord")
    public Collection<AuditSource> getAuditSource() {
        return auditSource;
    }
        
    public void setAuditSource(Collection<AuditSource> c) {
        this.auditSource = c;
    }
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy="auditRecord")
    public Collection<ParticipantObject> getParticipantObject() {
        return participantObject;
    }
    
    public void setParticipantObject(Collection<ParticipantObject> c) {
        this.participantObject = c;
    }
    
    @Column(name = "event_action_code")
    public String getEventActionCode() {
        return eventActionCode;
    }
    
    public void setEventActionCode(String eventActionCode) {
        this.eventActionCode = eventActionCode;
    }

    @Column(name = "event_outcome")
    public int getEventOutcomeIndicator() {
        return eventOutcomeIndicator;
    }
    
    public void setEventOutcomeIndicator(int eventOutcomeIndicator) {
        this.eventOutcomeIndicator = eventOutcomeIndicator;
    }

    @Basic @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "event_date_time")
    public Date getEventDateTime() {
        return eventDateTime;
    }
    
    public void setEventDateTime(Date dt) {
        this.eventDateTime = dt;
    }
    
    @Basic @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "receive_date_time")
    public Date getReceiveDateTime() {
        return receiveDateTime;
    }
    
    public void setReceiveDateTime(Date dt) {
        this.receiveDateTime = dt;
    }
    
    @Column(name = "iheyr4")
    public boolean isIHEYr4() {
        return iheYr4;
    }
    
    public void setIHEYr4(boolean iheYr4) {
        this.iheYr4 = iheYr4;
    }    
    
    @Lob
    @Column(name = "xmldata")
    public byte[] getXmldata() {
        return xmldata;
    }
    
    public void setXmldata(byte[] xmldata) {
        this.xmldata = xmldata;
    }
}
