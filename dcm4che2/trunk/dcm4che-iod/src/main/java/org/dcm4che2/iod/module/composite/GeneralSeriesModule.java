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
import org.dcm4che2.iod.module.macro.PersonIdentification;
import org.dcm4che2.iod.module.macro.ProtocolCodeAndContext;
import org.dcm4che2.iod.module.macro.SOPInstanceReference;
import org.dcm4che2.iod.value.PixelRepresentation;

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
        return dcmobj.getString(Tag.MODALITY);
    }

    public void setModality(String s) {
        dcmobj.putString(Tag.MODALITY, VR.CS, s);
    }

    public String getSeriesInstanceUID() {
        return dcmobj.getString(Tag.SERIES_INSTANCE_UID);
    }

    public void setSeriesInstanceUID(String s) {
        dcmobj.putString(Tag.SERIES_INSTANCE_UID, VR.UI, s);
    }

    /**
     * A number that identiries this series.
     * <p>
     * Please not that this is an IS DICOM value, which is supposed to be
     * encoded in JAVA as an int. Nevertheless, {@link String} has been chosen
     * because:
     * <ul>
     * <li> I have already seen objects, which uses non-numeric values for this
     * identifiers.
     * <li>For identifiers, the non-numeric value may still of some
     * use/information as opposed to e.g. a non-numeric Frame Number..
     * </ul>
     * <p>
     * Type 2
     * 
     * @return
     */
    public String getSeriesNumber() {
        return dcmobj.getString(Tag.SERIES_NUMBER);
    }

    /**
     * A number that identiries this series.
     * <p>
     * Please not that this is an IS DICOM value, which is supposed to be
     * encoded in JAVA as an int. Nevertheless, {@link String} has been chosen
     * because:
     * <ul>
     * <li> I have already seen objects, which uses non-numeric values for this
     * identifiers.
     * <li>For identifiers, the non-numeric value may still of some
     * use/information as opposed to e.g. a non-numeric Frame Number..
     * </ul>
     * <p>
     * Type 2
     * 
     * @param s
     */
    public void setSeriesNumber(String s) {
        dcmobj.putString(Tag.SERIES_NUMBER, VR.IS, s);
    }

    public String getLaterality() {
        return dcmobj.getString(Tag.LATERALITY);
    }

    public void setLaterality(String s) {
        dcmobj.putString(Tag.LATERALITY, VR.CS, s);
    }

    public Date getSeriesDateTime() {
        return dcmobj.getDate(Tag.SERIES_DATE, Tag.SERIES_TIME);
    }

    public void setSeriesDateTime(Date d) {
        dcmobj.putDate(Tag.SERIES_DATE, VR.DA, d);
        dcmobj.putDate(Tag.SERIES_TIME, VR.TM, d);
    }

    public String[] getPerformingPhysiciansName() {
        return dcmobj.getStrings(Tag.PERFORMING_PHYSICIANS_NAME);
    }

    public void setPerformingPhysiciansName(String[] ss) {
        dcmobj.putStrings(Tag.PERFORMING_PHYSICIANS_NAME, VR.PN, ss);
    }

    public PersonIdentification[] getPerformingPhysicianIdentification() {
        return PersonIdentification.toPersonIdentifications(dcmobj
                .get(Tag.PERFORMING_PHYSICIAN_IDENTIFICATION_SEQUENCE));
    }

    public void setPerformingPhysicianIdentification(PersonIdentification[] ids) {
        updateSequence(Tag.PERFORMING_PHYSICIAN_IDENTIFICATION_SEQUENCE, ids);
    }

    public String getProtocolName() {
        return dcmobj.getString(Tag.PROTOCOL_NAME);
    }

    public void setProtocolName(String s) {
        dcmobj.putString(Tag.PROTOCOL_NAME, VR.LO, s);
    }

    public String getSeriesDescription() {
        return dcmobj.getString(Tag.SERIES_DESCRIPTION);
    }

    public void setSeriesDescription(String s) {
        dcmobj.putString(Tag.SERIES_DESCRIPTION, VR.LO, s);
    }

    public String[] getOperatorsName() {
        return dcmobj.getStrings(Tag.OPERATORS_NAME);
    }

    public void setOperatorsName(String[] ss) {
        dcmobj.putStrings(Tag.OPERATORS_NAME, VR.PN, ss);
    }

    public PersonIdentification[] getOperatorIdentification() {
        return PersonIdentification.toPersonIdentifications(dcmobj
                .get(Tag.OPERATOR_IDENTIFICATION_SEQUENCE));
    }

    public void setOperatorIdentification(PersonIdentification[] ids) {
        updateSequence(Tag.OPERATOR_IDENTIFICATION_SEQUENCE, ids);
    }

    public SOPInstanceReference getReferencedPerformedProcedureStep() {
        DicomObject item = dcmobj
                .getNestedDicomObject(Tag.REFERENCED_PERFORMED_PROCEDURE_STEP_SEQUENCE);
        return item != null ? new SOPInstanceReference(item) : null;
    }

    public void setReferencedPerformedProcedureStep(
            SOPInstanceReference refSOP) {
        updateSequence(Tag.REFERENCED_PERFORMED_PROCEDURE_STEP_SEQUENCE, refSOP);
    }

    public RelatedSeries[] getRelatedSeries() {
        return RelatedSeries.toRelatedSeries(dcmobj
                .get(Tag.RELATED_SERIES_SEQUENCE));
    }

    public void setRelatedSeries(RelatedSeries[] relseries) {
        updateSequence(Tag.RELATED_SERIES_SEQUENCE, relseries);
    }

    public String getBodyPartExamined() {
        return dcmobj.getString(Tag.BODY_PART_EXAMINED);
    }

    public void setBodyPartExamined(String s) {
        dcmobj.putString(Tag.BODY_PART_EXAMINED, VR.CS, s);
    }

    public String getPatientPosition() {
        return dcmobj.getString(Tag.PATIENT_POSITION);
    }

    public void setPatientPosition(String s) {
        dcmobj.putString(Tag.PATIENT_POSITION, VR.CS, s);
    }

    public int getSmallestPixelValueinSeries() {
        return dcmobj.getInt(Tag.SMALLEST_PIXEL_VALUE_IN_SERIES);
    }

    public void setSmallestPixelValueinSeries(int s) {
        dcmobj.putInt(Tag.SMALLEST_PIXEL_VALUE_IN_SERIES, PixelRepresentation
                .isSigned(dcmobj) ? VR.SS : VR.US, s);
    }

    public int getLargestPixelValueinSeries() {
        return dcmobj.getInt(Tag.LARGEST_PIXEL_VALUE_IN_SERIES);
    }

    public void setLargestPixelValueinSeries(int s) {
        dcmobj.putInt(Tag.LARGEST_PIXEL_VALUE_IN_SERIES, PixelRepresentation
                .isSigned(dcmobj) ? VR.SS : VR.US, s);
    }

    public RequestAttributes[] getRequestAttributes() {
        return RequestAttributes.toRequestAttributes(dcmobj
                .get(Tag.REQUEST_ATTRIBUTES_SEQUENCE));
    }

    public void setRequestAttributes(RequestAttributes[] rqattrs) {
        updateSequence(Tag.REQUEST_ATTRIBUTES_SEQUENCE, rqattrs);
    }

    public String getPerformedProcedureStepID() {
        return dcmobj.getString(Tag.PERFORMED_PROCEDURE_STEP_ID);
    }

    public void setPerformedProcedureStepID(String s) {
        dcmobj.putString(Tag.PERFORMED_PROCEDURE_STEP_ID, VR.SH, s);
    }

    public Date getPerformedProcedureStepStartDateTime() {
        return dcmobj.getDate(Tag.PERFORMED_PROCEDURE_STEP_START_DATE,
                Tag.PERFORMED_PROCEDURE_STEP_START_TIME);
    }

    public void setPerformedProcedureStepStartDateTime(Date d) {
        dcmobj.putDate(Tag.PERFORMED_PROCEDURE_STEP_START_DATE, VR.DA, d);
        dcmobj.putDate(Tag.PERFORMED_PROCEDURE_STEP_START_TIME, VR.TM, d);
    }

    public String getPerformedProcedureStepDescription() {
        return dcmobj.getString(Tag.PERFORMED_PROCEDURE_STEP_DESCRIPTION);
    }

    public void setPerformedProcedureStepDescription(String s) {
        dcmobj.putString(Tag.PERFORMED_PROCEDURE_STEP_DESCRIPTION, VR.LO, s);
    }

    public ProtocolCodeAndContext[] getPerformedProtocolCodes() {
        return ProtocolCodeAndContext.toProtocolCodeAndContexts(dcmobj
                .get(Tag.PERFORMED_PROTOCOL_CODE_SEQUENCE));
    }

    public void setPerformedProtocolCodes(ProtocolCodeAndContext[] codes) {
        updateSequence(Tag.PERFORMED_PROTOCOL_CODE_SEQUENCE, codes);
    }

    public String getCommentsonthePerformedProcedureStep() {
        return dcmobj.getString(Tag.COMMENTS_ON_THE_PERFORMED_PROCEDURE_STEP);
    }

    public void setCommentsonthePerformedProcedureStep(String s) {
        dcmobj.putString(Tag.COMMENTS_ON_THE_PERFORMED_PROCEDURE_STEP, VR.ST, s);
    }

}
