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
package org.dcm4che2.iod.module.sr;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4che2.iod.module.macro.Code;
import org.dcm4che2.iod.module.macro.SOPInstanceReference;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$
 * @since 25.07.2006
 */
public class SRDocumentGeneralModule extends KODocumentModule {

    public SRDocumentGeneralModule(DicomObject dcmobj) {
	super(dcmobj);
    }

    public String getCompletionFlag() {
        return dcmobj.getString(Tag.COMPLETION_FLAG);
    }

    public void setCompletionFlag(String s) {
        dcmobj.putString(Tag.COMPLETION_FLAG, VR.CS, s);
    }

    public String getCompletionFlagDescription() {
        return dcmobj.getString(Tag.COMPLETION_FLAG_DESCRIPTION);
    }

    public void setCompletionFlagDescription(String s) {
        dcmobj.putString(Tag.COMPLETION_FLAG_DESCRIPTION, VR.LO, s);
    }

    public String getVerificationFlag() {
        return dcmobj.getString(Tag.VERIFICATION_FLAG);
    }

    public void setVerificationFlag(String s) {
        dcmobj.putString(Tag.VERIFICATION_FLAG, VR.CS, s);
    }

    public VerifyingObserver[] getVerifyingObservers() {
        return VerifyingObserver.toVerifyingObservers(
        	dcmobj.get(Tag.VERIFYING_OBSERVER_SEQUENCE));
    }
    
    public void setVerifyingObservers(VerifyingObserver[] a) {
        updateSequence(Tag.VERIFYING_OBSERVER_SEQUENCE, a);
    }

    public IdentifiedPersonOrDevice getAuthorObserver() {
        DicomObject item = dcmobj.getNestedDicomObject(
        	Tag.AUTHOR_OBSERVER_SEQUENCE);
        return item != null ? new IdentifiedPersonOrDevice(item) : null;
    }

    public void setAuthorObserver(IdentifiedPersonOrDevice observer) {
        updateSequence(Tag.AUTHOR_OBSERVER_SEQUENCE, observer);
    }    

    public Participant[] getParticipants() {
        return Participant.toParticipants(dcmobj.get(Tag.PARTICIPANT_SEQUENCE));
    }

    public void setParticipants(Participant[] participants) {
        updateSequence(Tag.PARTICIPANT_SEQUENCE, participants);
    }

    public InsitutionNameAndCode getCustodialOrganization() {
        DicomObject item = dcmobj.getNestedDicomObject(
        	Tag.CUSTODIAL_ORGANIZATION_SEQUENCE);
        return item != null ? new InsitutionNameAndCode(item) : null;
    }

    public void setCustodialOrganization(InsitutionNameAndCode org) {
        updateSequence(Tag.CUSTODIAL_ORGANIZATION_SEQUENCE, org);
    }    

    public SOPInstanceReferenceMacro[] getPredecessorDocuments() {
        return SOPInstanceReferenceMacro.toSOPInstanceReferenceMacros(
        	dcmobj.get(Tag.PREDECESSOR_DOCUMENTS_SEQUENCE));
    }

    public void setPredecessorDocuments(SOPInstanceReferenceMacro[] refs) {
        updateSequence(Tag.PREDECESSOR_DOCUMENTS_SEQUENCE, refs);
    }
    
    public Code[] getPerformedProcedureCodes() {
        return Code.toCodes(dcmobj.get(Tag.PERFORMED_PROCEDURE_CODE_SEQUENCE));
    }

    public void setPerformedProcedureCodes(Code[] codes) {
        updateSequence(Tag.PERFORMED_PROCEDURE_CODE_SEQUENCE, codes);
    } 
    
    public SOPInstanceReferenceMacro[] getPertinentOtherEvidences() {
        return SOPInstanceReferenceMacro.toSOPInstanceReferenceMacros(
        	dcmobj.get(Tag.PERTINENT_OTHER_EVIDENCE_SEQUENCE));
    }

    public void setPertinentOtherEvidences(SOPInstanceReferenceMacro[] refs) {
        updateSequence(Tag.PERTINENT_OTHER_EVIDENCE_SEQUENCE, refs);
    }

    public SOPInstanceReference getEquivalentCDADocument() {
        DicomObject item = dcmobj.getNestedDicomObject(
        	Tag.EQUIVALENT_CDA_DOCUMENT_SEQUENCE);
        return item != null ? new SOPInstanceReference(item) : null;
    }
    
    public void setEquivalentCDADocument(SOPInstanceReference refSOP) {
        updateSequence(Tag.EQUIVALENT_CDA_DOCUMENT_SEQUENCE, refSOP);
    }
    
}
