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

package org.dcm4che2.iod.module.composite;

import java.util.Date;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4che2.iod.module.Module;
import org.dcm4che2.iod.module.macro.Code;
import org.dcm4che2.iod.module.macro.SOPInstanceReference;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision$ $Date$
 * @since Jun 9, 2006
 *
 */
public class PatientModule extends Module {

    public PatientModule(DicomObject dcmobj) {
        super(dcmobj);
    }

    public String getPatientsName() {
        return dcmobj.getString(Tag.PATIENTS_NAME);
    }
    
    public void setPatientsName(String s) {
        dcmobj.putString(Tag.PATIENTS_NAME, VR.PN, s);
    }
    
    public String getPatientID() {
        return dcmobj.getString(Tag.PATIENT_ID);
    }
    
    public void setPatientID(String s) {
        dcmobj.putString(Tag.PATIENT_ID, VR.LO, s);
    }
    
    public String getIssuerofPatientID() {
        return dcmobj.getString(Tag.ISSUER_OF_PATIENT_ID);
    }
    
    public void setIssuerofPatientID(String s) {
        dcmobj.putString(Tag.ISSUER_OF_PATIENT_ID, VR.LO, s);
    }
    
    public Date getPatientsBirthDate() {
        return dcmobj.getDate(Tag.PATIENTS_BIRTH_DATE);
    }
    
    public void setPatientsBirthDate(Date d) {
        dcmobj.putDate(Tag.PATIENTS_BIRTH_DATE, VR.DA, d);
    }
    
    public Date getPatientsBirthTime() {
        return dcmobj.getDate(Tag.PATIENTS_BIRTH_TIME);
    }
    
    public void setPatientsBirthTime(Date d) {
        dcmobj.putDate(Tag.PATIENTS_BIRTH_TIME, VR.TM, d);
    }
    
    public String getPatientsSex() {
        return dcmobj.getString(Tag.PATIENTS_SEX);
    }
    
    public void setPatientsSex(String s) {
        dcmobj.putString(Tag.PATIENTS_SEX, VR.CS, s);
    }
    
    public SOPInstanceReference getReferencedPatientSOPInstance() {
        DicomObject item = dcmobj.getNestedDicomObject(Tag.REFERENCED_PATIENT_SEQUENCE);
        return item != null ? new SOPInstanceReference(item) : null;
    }
    
    public void setReferencedPatientSOPInstance(SOPInstanceReference refSOP) {
        updateSequence(Tag.REFERENCED_PATIENT_SEQUENCE, refSOP);
    }
    
    public String[] getOtherPatientIDs() {
        return dcmobj.getStrings(Tag.OTHER_PATIENT_IDS);
    }
    
    public void setOtherPatientIDs(String[] ss) {
        dcmobj.putStrings(Tag.OTHER_PATIENT_IDS, VR.LO, ss);
    }
    
    public String[] getOtherPatientNames() {
        return dcmobj.getStrings(Tag.OTHER_PATIENT_NAMES);
    }
    
    public void setOtherPatientNames(String[] ss) {
        dcmobj.putStrings(Tag.OTHER_PATIENT_NAMES, VR.PN, ss);
    }
    
    public String getEthnicGroup() {
        return dcmobj.getString(Tag.ETHNIC_GROUP);
    }
    
    public void setEthnicGroup(String s) {
        dcmobj.putString(Tag.ETHNIC_GROUP, VR.SH, s);
    }
    
    public String getPatientComments() {
        return dcmobj.getString(Tag.PATIENT_COMMENTS);
    }
    
    public void setPatientComments(String s) {
        dcmobj.putString(Tag.PATIENT_COMMENTS, VR.LT, s);
    }
    
    public String getPatientIdentifyRemoved() {
        return dcmobj.getString(Tag.PATIENT_IDENTIFY_REMOVED);
    }
    
    public void setPatientIdentifyRemoved(String s) {
        dcmobj.putString(Tag.PATIENT_IDENTIFY_REMOVED, VR.CS, s);
    }
    
    public String getDeidentificationMethod() {
        return dcmobj.getString(Tag.DE_IDENTIFICATION_METHOD);
    }
    
    public void setDeidentificationMethod(String s) {
        dcmobj.putString(Tag.DE_IDENTIFICATION_METHOD, VR.LO, s);
    }
    
    public Code[] getDeidentificationMethodCodes() {
        return Code.toCodes(dcmobj.get(Tag.DE_IDENTIFICATION_METHOD_CODE_SEQUENCE));
    }

    public void setDeidentificationMethodCodes(Code[] codes) {
        updateSequence(Tag.DE_IDENTIFICATION_METHOD_CODE_SEQUENCE, codes);
    }    
}
