package org.dcm4che2.iod.composite;

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.iod.module.CRImageModule;
import org.dcm4che2.iod.module.CRSeriesModule;
import org.dcm4che2.iod.module.ContrastBolusModule;
import org.dcm4che2.iod.module.DeviceModule;
import org.dcm4che2.iod.module.GeneralImageModule;
import org.dcm4che2.iod.module.ImagePixelModule;
import org.dcm4che2.iod.module.ModalityLUTModule;
import org.dcm4che2.iod.module.OverlayPlaneModule;
import org.dcm4che2.iod.module.VOILUTModule;

public class CRImage extends Composite {

    protected final CRSeriesModule crSeriesModule;
    protected final GeneralImageModule generalImageModule;
    protected final ImagePixelModule imagePixelModule;
    protected final ContrastBolusModule contrastBolusModule;
    protected final DeviceModule deviceModule;
    protected final CRImageModule crImageModule;
    protected final OverlayPlaneModule overlayPlaneModule;
    protected final ModalityLUTModule modalityLUTModule;
    protected final VOILUTModule voiLUTModule;
    
    public CRImage(DicomObject dcmobj) {
        super(dcmobj);
        this.crSeriesModule = new CRSeriesModule(dcmobj);
        this.generalImageModule = new GeneralImageModule(dcmobj);
        this.imagePixelModule = new ImagePixelModule(dcmobj);
        this.contrastBolusModule = new ContrastBolusModule(dcmobj);
        this.deviceModule = new DeviceModule(dcmobj);
        this.crImageModule = new CRImageModule(dcmobj);
        this.overlayPlaneModule = new OverlayPlaneModule(dcmobj);
        this.modalityLUTModule = new ModalityLUTModule(dcmobj);
        this.voiLUTModule = new VOILUTModule(dcmobj);
    }

    public CRImage() {
        this(new BasicDicomObject());
    }

    public final CRSeriesModule getCrSeriesModule() {
        return crSeriesModule;
    }
    
    public final GeneralImageModule getGeneralImageModule() {
        return generalImageModule;
    }
    
    public final ImagePixelModule getImagePixelModule() {
        return imagePixelModule;
    }
    
    public final ContrastBolusModule getContrastBolusModule() {
        return contrastBolusModule;
    }

    public final DeviceModule getDeviceModule() {
        return deviceModule;
    }
    
    public final CRImageModule getCRImageModule() {
        return crImageModule;
    }

    public final OverlayPlaneModule getOverlayPlaneModule() {
        return overlayPlaneModule;
    }

    public final ModalityLUTModule getModalityLUTModule() {
        return modalityLUTModule;
    }
    
    public final VOILUTModule getVOILUTModule() {
        return voiLUTModule;
    }
}
