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

package org.dcm4che2.iod.composite;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.iod.module.composite.ContrastBolusModule;
import org.dcm4che2.iod.module.composite.DeviceModule;
import org.dcm4che2.iod.module.composite.GeneralImageModule;
import org.dcm4che2.iod.module.dx.DXAnatomyImagedModule;
import org.dcm4che2.iod.module.dx.DXDetectorModule;
import org.dcm4che2.iod.module.dx.DXImageModule;
import org.dcm4che2.iod.module.dx.DXPositioningModule;
import org.dcm4che2.iod.module.dx.DXSeriesModule;
import org.dcm4che2.iod.module.lut.VOILUTModule;
import org.dcm4che2.iod.module.overlay.OverlayPlaneModule;

/**
 * The Digital X-Ray (DX) Image Information Object Definition specifies an image
 * that has been created by a digital projection radiography imaging device.
 * 
 * Notes:
 * 
 * 1. This includes but is not limited to: chest radiography, linear and
 * multi-directional tomography, orthopantomography and skeletal radiography.
 * Acquisition of image data may include but is not limited to: CCD-based
 * sensors, stimulable phosphor imaging plates, amorphous selenium,
 * scintillation based amorphous silicon and secondary capture of film-based
 * images.
 * 
 * 2. Specific IODs are defined for intra-oral radiography and mammography that
 * further specialize the DX IOD.
 * 
 * A DX image shall consist of the result of a single X-Ray exposure, in order
 * to ensure that the anatomical and orientation attributes are meaningful for
 * the image, permitting safe annotation, appropriate image processing and
 * appropriate dissemination.
 * 
 * Notes:
 * 
 * 1. This requirement specifically deprecates the common film/screen and
 * Computed Radiography practice of making multiple exposures on different areas
 * of a cassette or plate by using lead occlusion between exposures. Such
 * acquisitions could be separated and transformed into multiple DX images
 * during an appropriate quality assurance step by an operator.
 * 
 * 2. This requirement does not deprecate the acquisition of multiple paired
 * structures during a single exposure, provided that they can be described by
 * the relevant orientation Attributes. For example, an AP or PA projection of
 * both hands side by side is typically obtained in a single exposure, and can
 * be described by a Patient Orientation (0020,0020) of R\H or L\H since both
 * hands are in the same traditional Anatomical Position. See PS 3.17 annex on
 * Explanation of Patient Orientation.
 * 
 * The DX Image IOD is used in two SOP Classes as defined in PS 3.4 Storage
 * Service Class, a SOP Class for storage of images intended for presentation,
 * and a SOP Class for storage of images intended for further processing before
 * presentation. These are distinguished by their SOP Class UID and by the
 * Enumerated Value of the mandatory Attribute in the DX Series Module,
 * Presentation Intent Type (0008,0068).
 * 
 * @author Antonio Magni
 * 
 */
public class DXImage extends Composite {

    protected final DXSeriesModule dxSeriesModule;

    protected final GeneralImageModule generalImageModule;

    protected final ContrastBolusModule contrastBolusModule;

    //TODO DisplayShutter
    
    protected final DeviceModule deviceModule;

    //TODO Intervention Module
    
    protected final DXAnatomyImagedModule dxAnatomyImagedModule;
    
    protected final DXImageModule dxImageModule;
    
    protected final DXDetectorModule dxDetectorModule;
    
    //TODO x-ray collimator
    
    protected final DXPositioningModule dxPositioningModule;
    
    //TODO x-ray tomo axquisition
    
    //TODO x-ray acquisition dose
    
    //TODO x-ray generation
    
    //TODO x-ray filtration
    
    //TODO x-ray grid
    
    protected final OverlayPlaneModule overlayPlaneModule;

    protected final VOILUTModule voiLUTModule;
    
    //TODO Image Histogram
    
    //TODO Acquisition Context THIS IS MANDATORY!!

    public DXImage(DicomObject dcmobj) {
        super(dcmobj);

        this.dxSeriesModule = new DXSeriesModule(dcmobj);
        this.generalImageModule = new GeneralImageModule(dcmobj);
        this.contrastBolusModule = new ContrastBolusModule(dcmobj);
        
        this.deviceModule = new DeviceModule(dcmobj);
        this.dxAnatomyImagedModule = new DXAnatomyImagedModule(dcmobj);
        this.dxImageModule = new DXImageModule(dcmobj);
        this.dxDetectorModule = new DXDetectorModule(dcmobj);
        this.dxPositioningModule = new DXPositioningModule(dcmobj);
        this.overlayPlaneModule = new OverlayPlaneModule(dcmobj);
        this.voiLUTModule = new VOILUTModule(dcmobj);
    }

    public final DXSeriesModule getDXSeriesModule() {
        return dxSeriesModule;
    }

    public GeneralImageModule getGeneralImageModule(){
        return generalImageModule;
    }
    
    public ContrastBolusModule getContrastBolusModule(){
        return contrastBolusModule;
    }
    
    public DeviceModule getDeviceModule(){
        return deviceModule;
    }
    
    public DXAnatomyImagedModule getDXAnatomyImageModule(){
        return dxAnatomyImagedModule;
    }
    
    public DXImageModule getDXImageModule(){
        return dxImageModule;
    }
    
    public DXDetectorModule getDXDetectorModule(){
        return dxDetectorModule;
    }

    public DXPositioningModule getDXPositioningModule() {
        return dxPositioningModule;
    }

    public OverlayPlaneModule getOverlayPlaneModule(){
        return overlayPlaneModule;
    }
    
    public VOILUTModule getVOILUTModule(){
        return voiLUTModule;
    }

}
