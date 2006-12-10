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
 * See listed authors below.
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

package org.dcm4che2.audit.message;

/**
 * This message describes any event for which a node needs to report a 
 * security alert, e.g., a node authentication failure when establishing a 
 * secure communications channel.
 * <p>
 * Note: The Node Authentication event can be used to report both successes
 * and failures. If reporting of success is done, this could generate a very
 * large number of audit messages, since every authenticated DICOM association,
 * HL7 transaction, and HTML connection should result in a successful node
 * authentication. It is expected that in most situations only the node
 * authentication failures will be reported.
 * 
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Nov 23, 2006
 */
public class SecurityAlertMessage extends AuditMessage {
    
    public SecurityAlertMessage(AuditEvent event, ActiveParticipant reporter) {
        super(event, reporter);
    }
    
    public SecurityAlertMessage(AuditEvent event, ActiveParticipant reporter1,
            ActiveParticipant reporter2) {
        super(event, reporter1);
        super.addActiveParticipant(reporter2); 
    }
    
    public SecurityAlertMessage addPerfomingParticipant(
            PerformingParticipant performer) {
        super.addActiveParticipant(performer);
        return this;
    }

    public SecurityAlertMessage addAlertSubject(AlertSubject subject) {
        super.addParticipantObject(subject);
        return this;
    } 

    /**
     * This method is deprecated and should not be used.
     * 
     * @deprecated
     * @exception java.lang.IllegalArgumentException if apart is not a 
     * {@link PerformingParticipant}
     * @see #addPerfomingParticipant(PerformingParticipant)
     */
    public AuditMessage addActiveParticipant(ActiveParticipant apart) {
        if (apart instanceof PerformingParticipant) {
            return super.addActiveParticipant(apart);            
        }
        throw new IllegalArgumentException();
    }

    /**
     * This method is deprecated and should not be used.
     *
     * @deprecated
     * @exception java.lang.IllegalArgumentException if obj is not a 
     * {@link AlertSubject}
     * @see #addAlertSubject(AlertSubject)
     */
    public AuditMessage addParticipantObject(ParticipantObject obj) {
        if (obj instanceof Study) {
            return super.addParticipantObject(obj);            
        }
        throw new IllegalArgumentException();
    }
    
    public static class AuditEvent extends org.dcm4che2.audit.message.AuditEvent {

        public AuditEvent(TypeCode code) {
            super(ID.SECURITY_ALERT);
            super.setEventActionCode(ActionCode.EXECUTE);
            super.addEventTypeCode(code);
        }
        
        public static class NodeAuthentication extends AuditEvent {

            public NodeAuthentication() {
                super(TypeCode.NODE_AUTHENTICATION);
            }        
        }
        
        public static class EmergencyOverride extends AuditEvent {

            public EmergencyOverride() {
                super(TypeCode.EMERGENCY_OVERRIDE);
            }        
        }

        public static class NetworkConfiguration extends AuditEvent {

            public NetworkConfiguration() {
                super(TypeCode.NETWORK_CONFIGURATION);
            }        
        }

        public static class SecurityConfiguration extends AuditEvent {

            public SecurityConfiguration() {
                super(TypeCode.SECURITY_CONFIGURATION);
            }        
        }
 
        public static class HardwareConfiguration extends AuditEvent {

            public HardwareConfiguration() {
                super(TypeCode.HARDWARE_CONFIGURATION);
            }        
        }
 
        public static class SoftwareConfiguration extends AuditEvent {

            public SoftwareConfiguration() {
                super(TypeCode.SOFTWARE_CONFIGURATION);
            }        
        }

        public static class UseOfRestrictedFunction extends AuditEvent {

            public UseOfRestrictedFunction() {
                super(TypeCode.USE_OF_RESTRICTED_FUNCTION);
            }        
        }
 
        public static class AuditRecordingStopped extends AuditEvent {

            public AuditRecordingStopped() {
                super(TypeCode.AUDIT_RECORDING_STOPPED);
            }        
        }
 
        public static class AuditRecordingStarted extends AuditEvent {

            public AuditRecordingStarted() {
                super(TypeCode.AUDIT_RECORDING_STARTED);
            }        
        }
  
        public static class ObjectSecurityAttributesChanged extends AuditEvent {

            public ObjectSecurityAttributesChanged() {
                super(TypeCode.OBJECT_SECURITY_ATTRIBUTES_CHANGED);
            }        
        }
  
        public static class SecurityRolesChanged extends AuditEvent {

            public SecurityRolesChanged() {
                super(TypeCode.SECURITY_ROLES_CHANGED);
            }        
        }

        public static class UserSecurityAttributesChanged extends AuditEvent {

            public UserSecurityAttributesChanged() {
                super(TypeCode.USER_SECURITY_ATTRIBUTES_CHANGED);
            }        
        }
    }
    
}