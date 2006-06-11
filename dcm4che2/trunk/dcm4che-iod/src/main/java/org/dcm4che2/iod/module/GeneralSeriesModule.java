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

package org.dcm4che2.iod.module;

import java.util.Date;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision$ $Date$
 * @since Jun 9, 2006
 * 
 */
public class GeneralSeriesModule extends Module {

    public GeneralSeriesModule(DicomObject dcmobj) {
        super(dcmobj);
    }

    public String getModality() {
        return dcmobj.getString(Tag.Modality);
    }

    public void setModality(String s) {
        dcmobj.putString(Tag.Modality, VR.CS, s);
    }

    public String getSeriesInstanceUID() {
        return dcmobj.getString(Tag.SeriesInstanceUID);
    }

    public void setSeriesInstanceUID(String s) {
        dcmobj.putString(Tag.SeriesInstanceUID, VR.UI, s);
    }

    public String getSeriesNumber() {
        return dcmobj.getString(Tag.SeriesNumber);
    }

    public void setSeriesNumber(String s) {
        dcmobj.putString(Tag.SeriesNumber, VR.IS, s);
    }

    public String getLaterality() {
        return dcmobj.getString(Tag.Laterality);
    }

    public void setLaterality(String s) {
        dcmobj.putString(Tag.Laterality, VR.CS, s);
    }

    public Date getSeriesDateTime() {
        return dcmobj.getDate(Tag.SeriesDate, Tag.SeriesTime);
    }

    public void setSeriesDateTime(Date d) {
        dcmobj.putDate(Tag.SeriesDate, VR.DA, d);
        dcmobj.putDate(Tag.SeriesTime, VR.TM, d);
    }

    public String[] getPerformingPhysiciansName() {
        return dcmobj.getStrings(Tag.PerformingPhysiciansName);
    }

    public void setPerformingPhysiciansName(String[] ss) {
        dcmobj.putStrings(Tag.PerformingPhysiciansName, VR.PN, ss);
    }

    public PersonIdentification[] getPerformingPhysicianIdentification() {
        return PersonIdentification.toPersonIdentifications(
                dcmobj.get(Tag.PerformingPhysicianIdentificationSequence));
    }

    public void setPerformingPhysicianIdentification(PersonIdentification[] ids) {
        updateSequence(Tag.PerformingPhysicianIdentificationSequence, ids);
    }
    
    public String getProtocolName() {
        return dcmobj.getString(Tag.ProtocolName);
    }

    public void setProtocolName(String s) {
        dcmobj.putString(Tag.ProtocolName, VR.LO, s);
    }

    public String getSeriesDescription() {
        return dcmobj.getString(Tag.SeriesDescription);
    }

    public void getSeriesDescription(String s) {
        dcmobj.putString(Tag.SeriesDescription, VR.LO, s);
    }

    public String[] getOperatorsName() {
        return dcmobj.getStrings(Tag.OperatorsName);
    }

    public void setOperatorsName(String[] ss) {
        dcmobj.putStrings(Tag.OperatorsName, VR.PN, ss);
    }

    public PersonIdentification[] getOperatorIdentification() {
        return PersonIdentification.toPersonIdentifications(
                dcmobj.get(Tag.OperatorIdentificationSequence));
    }

    public void setOperatorIdentification(PersonIdentification[] ids) {
        updateSequence(Tag.OperatorIdentificationSequence, ids);
    }
    
    public SOPInstanceReference getReferencedPerformedProcedureStepSOPInstance() {
        DicomObject item = dcmobj
                .getNestedDicomObject(Tag.ReferencedPerformedProcedureStepSequence);
        return item != null ? new SOPInstanceReference(item) : null;
    }

    public void setReferencedPerformedProcedureStepSOPInstance(
            SOPInstanceReference refSOP) {
        updateSequence(Tag.ReferencedPerformedProcedureStepSequence, refSOP);
    }

    public RelatedSeries[] getRelatedSeries() {
        return RelatedSeries.toRelatedSeries(dcmobj.get(Tag.RelatedSeriesSequence));
    }

    public void setRelatedSeries(RelatedSeries[] relseries) {
        updateSequence(Tag.RelatedSeriesSequence, relseries);
    }

    public String getBodyPartExamined() {
        return dcmobj.getString(Tag.BodyPartExamined);
    }

    public void setBodyPartExamined(String s) {
        dcmobj.putString(Tag.BodyPartExamined, VR.CS, s);
    }

    public String getPatientPosition() {
        return dcmobj.getString(Tag.PatientPosition);
    }

    public void setPatientPosition(String s) {
        dcmobj.putString(Tag.PatientPosition, VR.CS, s);
    }

    public int getSmallestPixelValueinSeries() {
        return dcmobj.getInt(Tag.SmallestPixelValueinSeries);
    }

    public void setSmallestPixelValueinSeries(int s) {
        dcmobj.putInt(Tag.SmallestPixelValueinSeries,
                isSignedPixelValues() ? VR.SS : VR.US, s);
    }

    public int getLargestPixelValueinSeries() {
        return dcmobj.getInt(Tag.LargestPixelValueinSeries);
    }

    public void setLargestPixelValueinSeries(int s) {
        dcmobj.putInt(Tag.LargestPixelValueinSeries,
                isSignedPixelValues() ? VR.SS : VR.US, s);
    }

    public RequestAttributes[] getRequestAttributes() {
        return RequestAttributes.valuesOf(
                dcmobj.get(Tag.RequestAttributesSequence));
    }

    public void setRequestAttributes(RequestAttributes[] rqattrs) {
        updateSequence(Tag.RequestAttributesSequence, rqattrs);
    }

    public String getPerformedProcedureStepID() {
        return dcmobj.getString(Tag.PerformedProcedureStepID);
    }

    public void setPerformedProcedureStepID(String s) {
        dcmobj.putString(Tag.PerformedProcedureStepID, VR.SH, s);
    }

    public Date getPerformedProcedureStepDateTime() {
        return dcmobj.getDate(Tag.PerformedProcedureStepStartDate,
                Tag.PerformedProcedureStepStartTime);
    }

    public void setPerformedProcedureStepDateTime(Date d) {
        dcmobj.putDate(Tag.PerformedProcedureStepStartDate, VR.DA, d);
        dcmobj.putDate(Tag.PerformedProcedureStepStartTime, VR.TM, d);
    }

    public String getPerformedProcedureStepDescription() {
        return dcmobj.getString(Tag.PerformedProcedureStepDescription);
    }

    public void getPerformedProcedureStepDescription(String s) {
        dcmobj.putString(Tag.PerformedProcedureStepDescription, VR.LO, s);
    }

    public ProtocolCodeAndContext[] getPerformedProtocolCodes() {
        return ProtocolCodeAndContext.toProtocolCodeAndContexts(
                dcmobj.get(Tag.PerformedProtocolCodeSequence));
    }

    public void setPerformedProtocolCodes(ProtocolCodeAndContext[] codes) {
        updateSequence(Tag.PerformedProtocolCodeSequence, codes);
    }

    public String getCommentsonthePerformedProcedureStep() {
        return dcmobj.getString(Tag.CommentsonthePerformedProcedureStep);
    }

    public void getCommentsonthePerformedProcedureStep(String s) {
        dcmobj.putString(Tag.CommentsonthePerformedProcedureStep, VR.ST, s);
    }

}
