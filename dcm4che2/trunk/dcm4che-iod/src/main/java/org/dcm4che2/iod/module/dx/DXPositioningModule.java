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

package org.dcm4che2.iod.module.dx;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4che2.iod.module.Module;
import org.dcm4che2.iod.module.macro.Code;

/**
 * 
 * Table C.8-72 contains IOD Attributes that describe the positioning used in
 * acquiring Digital X-Ray Images.
 * 
 * @author Antonio Magni <dcm4ceph@antoniomagni.org>
 * 
 */
public class DXPositioningModule extends Module {

    public DXPositioningModule(DicomObject dcmobj) {
        super(dcmobj);
    }

    /**
     * Sequence A Sequence that describes the radiographic method of patient,
     * tube and detector positioning to achieve a well described projection or
     * view.
     * 
     * Only a single Item shall be permitted in this Sequence.
     * 
     * Shall be consistent with the other Attributes in this Module, if present,
     * but may more specifically describe the image acquisition.
     * 
     * @param codes
     */
    public void setProjectionEponymousNameCodes(Code[] codes) {
        updateSequence(Tag.ProjectionEponymousNameCodeSequence, codes);
    }

    public Code[] getProjectionEponymousNameCodes() {
        return Code
                .toCodes(dcmobj.get(Tag.ProjectionEponymousNameCodeSequence));
    }

    /**
     * Description of imaging subject’s position relative to the equipment.
     * 
     * See C.7.3.1.1.2 for Defined Terms and further explanation.
     * 
     * If present, shall be consistent with Patient Gantry Relationship Code
     * Sequence (0054,0414) and Patient Orientation Modifier Code Sequence
     * (0054,0412).
     * 
     * Type 3
     * 
     * @param cs
     */
    public void setPatientPosition(String cs) {
        dcmobj.putString(Tag.PatientPosition, VR.CS, cs);
    }

    public String getPatientPosition() {
        return dcmobj.getString(Tag.PatientPosition);
    }

    /**
     * Radiographic view of the image relative to the imaging subject’s
     * orientation.
     * 
     * Shall be consistent with View Code Sequence (0054,0220). See C.8.11.5.1.1
     * for further explanation.
     * 
     * Type 3
     * 
     * @param cs
     */
    public void setViewPosition(String cs) {
        dcmobj.putString(Tag.ViewPosition, VR.CS, cs);
    }

    public String getViewPosition() {
        return dcmobj.getString(Tag.ViewPosition);
    }

    // TODO View Code Sequence and Modified Code Seuquence

    // TODO Patient Orientation Code and Modifier sequences

    /**
     * Sequence Sequence which describes the orientation of the patient with
     * respect to the gantry.
     * 
     * Only a single Item shall be permitted in this Sequence.
     * 
     * Type 3
     */
    public void setPatientGantryRelationshipCodes(Code[] codes) {
        updateSequence(Tag.PatientGantryRelationshipCodeSequence, codes);
    }

    public Code[] getPatientGantryRelationshipCodes() {
        return Code.toCodes(dcmobj
                .get(Tag.PatientGantryRelationshipCodeSequence));
    }

    /**
     * Distance in mm from source to the table, support or bucky side that is
     * closest to the Imaging Subject, as measured along the central ray of the
     * X-Ray beam.
     * 
     * Note:
     * <ol>
     * <li> This definition is less useful in terms of estimating geometric
     * magnification than a measurement to a defined point within the Imaging
     * Subject, but accounts for what is realistically measurable in an
     * automated fashion in a clinical setting.
     * <li> This measurement does not take into account any air gap between the
     * Imaging Subject and the “front” of the table or bucky.
     * <li> If the detector is not mounted in a table or bucky, then the actual
     * position relative to the patient is implementation or operator defined.
     * <li> This value is traditionally referred to as Source Object Distance
     * (SOD).
     * </ol>
     * See C.8.11.7 Mammography Image Module for explanation if Positioner Type
     * (0018,1508) is MAMMOGRAPHIC.
     * 
     * Type 3
     * 
     * @param ds
     */
    public void setDistanceSourcetoPatient(String ds) {
        dcmobj.putString(Tag.DistanceSourcetoPatient, VR.DS, ds);
    }

    public String getDistanceSourcetoPatient() {
        return dcmobj.getString(Tag.DistanceSourcetoPatient);
    }

    /**
     * Distance in mm from source to detector center.
     * 
     * Note: This value is traditionally referred to as Source Image Receptor
     * Distance (SID).
     * 
     * See C.8.11.7 Mammography Image Module for explanation if Positioner Type
     * (0018,1508) is MAMMOGRAPHIC.
     * 
     * Type 3
     * 
     * @param ds
     */
    public void setDistanceSourcetoDetector(String ds) {
        dcmobj.putString(Tag.DistanceSourcetoDetector, VR.DS, ds);
    }

    public String getDistanceSourcetoDetector() {
        return dcmobj.getString(Tag.DistanceSourcetoDetector);
    }

    /**
     * Factor Ratio of Source Image Receptor Distance (SID) over Source Object
     * Distance (SOD).
     * 
     * Type 3
     * 
     * @param ds
     */
    public void setEstimatedRadiographicMagnificationFactor(String ds) {
        dcmobj.putString(Tag.EstimatedRadiographicMagnificationFactor, VR.DS,
                ds);
    }

    public String getEstimatedRadiographicMagnificationFactor() {
        return dcmobj.getString(Tag.EstimatedRadiographicMagnificationFactor);
    }

    /**
     * Defined Terms: CARM COLUMN MAMMOGRAPHIC PANORAMIC CEPHALOSTAT RIGID NONE
     * Notes:
     * <ol>
     * <li> The term CARM can apply to any positioner with 2 degrees of freedom
     * of rotation of the X-Ray beam about the Imaging Subject.
     * <li> The term COLUMN can apply to any positioner with 1 degree of freedom
     * of rotation of the X-Ray beam about the Imaging Subject.
     * </ol>
     * 
     * Type 3
     * 
     * @param cs
     */
    public void setPositionerType(String cs) {
        dcmobj.putString(Tag.PositionerType, VR.CS, cs);
    }

    public String getPositionerType() {
        return dcmobj.getString(Tag.PositionerType);
    }

    /**
     * Position of the X-Ray beam about the patient from the RAO to LAO
     * direction where movement from RAO to vertical is positive, if Positioner
     * Type (0018,1508) is CARM.
     * 
     * See C.8.7.5 XA Positioner Module for further explanation if Positioner
     * Type (0018,1508) is CARM.
     * 
     * See C.8.11.7 Mammography Image Module for explanation if Positioner Type
     * (0018,1508) is MAMMOGRAPHIC.
     * 
     * Type 3
     * 
     * @param ds
     */
    public void setPositonerPrimaryAngle(String ds) {
        dcmobj.putString(Tag.PositionerPrimaryAngle, VR.DS, ds);
    }

    public String getPositionerPrimaryAngle() {
        return dcmobj.getString(Tag.PositionerPrimaryAngle);
    }

    /**
     * Position of the X-Ray beam about the patient from the CAU to CRA
     * direction where movement from CAU to vertical is positive, if Positioner
     * Type (0018,1508) is CARM.
     * 
     * See C.8.7.5 XA Positioner Module for further explanation if Positioner
     * Type (0018,1508) is CARM.
     * 
     * See C.8.11.7 Mammography Image Module for explanation if Positioner Type
     * (0018,1508) is MAMMOGRAPHIC.
     * 
     * Type 3
     * 
     * @param ds
     */
    public void setPositonerSecondaryAngle(String ds) {
        dcmobj.putString(Tag.PositionerSecondaryAngle, VR.DS, ds);
    }

    public String getPositionerSecondaryAngle() {
        return dcmobj.getString(Tag.PositionerSecondaryAngle);
    }

    /**
     * Angle of the X-Ray beam in the row direction in degrees relative to the
     * normal to the detector plane. Positive values indicate that the X-Ray
     * beam is tilted toward higher numbered columns. Negative values indicate
     * that the X-Ray beam is tilted toward lower numbered columns.
     * 
     * See C.8.7.5 XA Positioner Module for further explanation.
     * 
     * See C.8.11.7 Mammography Image Module for explanation if Positioner Type
     * (0018,1508) is MAMMOGRAPHIC.
     * 
     * Type 3
     * 
     * @param ds
     */
    public void setDetectorPrimaryAngle(String ds) {
        dcmobj.putString(Tag.DetectorPrimaryAngle, VR.DS, ds);
    }

    public String getDetectorPrimaryAngle() {
        return dcmobj.getString(Tag.DetectorPrimaryAngle);
    }

    /**
     * Angle of the X-Ray beam in the column direction in degrees relative to
     * the normal to the detector plane. Positive values indicate that the X-Ray
     * beam is tilted toward lower numbered rows. Negative values indicate that
     * the X-Ray beam is tilted toward higher numbered rows.
     * 
     * See C.8.7.5 XA Positioner Module for further explanation.
     * 
     * See C.8.11.7 Mammography Image Module for explanation if Positioner Type
     * (0018,1508) is MAMMOGRAPHIC.
     * 
     * Type 3
     * 
     * @param ds
     */
    public void setDetectorSecondaryAngle(String ds) {
        dcmobj.putString(Tag.DetectorSecondaryAngle, VR.DS, ds);
    }

    public String getDetectorSecondaryAngle() {
        return dcmobj.getString(Tag.DetectorSecondaryAngle);
    }

    /**
     * Angle of the X-Ray beam in degree relative to an orthogonal axis to the
     * detector plane. Positive values indicate that the tilt is toward the head
     * of the table.
     * 
     * Note: The detector plane is assumed to be parallel to the table plane.
     * 
     * Only meaningful if Positioner Type (0018,1508) is COLUMN.
     * 
     * Type 3
     * 
     * @param ds
     */
    public void setColumnAngulation(String ds) {
        dcmobj.putString(Tag.ColumnAngulation, VR.DS, ds);
    }

    public String getColumnAngulation() {
        return dcmobj.getString(Tag.ColumnAngulation);
    }

    /**
     * Defined Terms: FIXED TILTING NONE
     * 
     * Type 3
     * 
     * @param cs
     */
    public void setTableType(String cs) {
        dcmobj.putString(Tag.TableType, VR.CS, cs);
    }

    public String getTableType() {
        return dcmobj.getString(Tag.TableType);
    }

    /**
     * Angle of table plane in degrees relative to horizontal plane [Gravity
     * plane]. Positive values indicate that the head of the table is upward.
     * 
     * Only meaningful if Table Type (0018,113A) is TILTING.
     * 
     * Type 3
     * 
     * @param ds
     */
    public void setTableAngle(String ds) {
        dcmobj.putString(Tag.TableAngle, VR.DS, ds);
    }

    public String getTableAngle() {
        return dcmobj.getString(Tag.TableAngle);
    }

    /**
     * The average thickness in mm of the body part examined when compressed, if
     * compression has been applied during exposure.
     * 
     * Type 3
     * 
     * @param ds
     */
    public void setBodyPartThickness(String ds) {
        dcmobj.putString(Tag.BodyPartThickness, VR.DS, ds);
    }

    public String getBodyPartThickness() {
        return dcmobj.getString(Tag.BodyPartThickness);
    }

    /**
     * The compression force applied to the body part during exposure, measured
     * in Newtons.
     * 
     * Type 3
     * 
     * @param ds
     */
    public void setCompressionForce(String ds) {
        dcmobj.putString(Tag.CompressionForce, VR.DS, ds);
    }

    public String getCompressionForce() {
        return dcmobj.getString(Tag.CompressionForce);
    }

}
