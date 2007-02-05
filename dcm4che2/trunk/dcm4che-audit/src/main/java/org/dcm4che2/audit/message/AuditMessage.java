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

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Generic Audit Message according RFC 3881. Typically an event type specific 
 * sub-class will be initiated.
 * 
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Nov 17, 2006
 */
public class AuditMessage extends BaseElement {

    private static final String XML_VERSION_1_0_ENCODING_UTF_8 = 
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
    private static boolean incXMLDecl = false;
    private final AuditEvent event;
    private final ArrayList activeParticipants = new ArrayList(3);
    private final ArrayList auditSources = new ArrayList(1);
    private final ArrayList participantObjects = new ArrayList(3);
    
    public AuditMessage(AuditEvent event) {
        super("AuditMessage");
        if (event == null) {
            throw new NullPointerException();
        }
        this.event = event;
    }
    
    public static final boolean isIncludeXMLDeclaration() {
        return incXMLDecl;
    }

    public static final void setIncludeXMLDeclaration(boolean incXMLDecl) {
        AuditMessage.incXMLDecl = incXMLDecl;
    }

    public final AuditEvent getAuditEvent() {
        return event;
    }
    
    public List getAuditSources() {
        return Collections.unmodifiableList(auditSources);
    }
       
    public AuditSource addAuditSource(AuditSource sourceId) {
        if (sourceId == null) {
            throw new NullPointerException();
        }
        auditSources.add(sourceId);
        return sourceId;
    }

    public List getActiveParticipants() {
        return Collections.unmodifiableList(activeParticipants);
    }
           
    public ActiveParticipant addActiveParticipant(ActiveParticipant apart) {
        if (apart == null) {
            throw new NullPointerException();
        }
        activeParticipants.add(apart);
        return apart;
    }
    
    public List getParticipantObjects() {
        return Collections.unmodifiableList(participantObjects);
    }
           
    public ParticipantObject addParticipantObject(ParticipantObject obj) {
        if (obj == null) {
            throw new NullPointerException();
        }
        participantObjects.add(obj);
        return obj;
    }
    
    protected boolean isEmpty() {
        return false;
    }

    public String toString() {
        return toString(1024);
    }    
    
    public void output(Writer out) throws IOException {
        validate();
        if (incXMLDecl) {
            out.write(XML_VERSION_1_0_ENCODING_UTF_8);
        }
        super.output(out);
    }

    public void validate() {
        if (activeParticipants.isEmpty()) {
            throw new IllegalStateException("No Active Participant");
        }
    }
    
    protected void outputContent(Writer out) throws IOException {
        event.output(out);
        outputChilds(out, activeParticipants);
        if (auditSources.isEmpty()) {
            AuditSource.getDefaultAuditSource().output(out);
        } else {
            outputChilds(out, auditSources);
        }
        outputChilds(out, participantObjects);
    }
}
