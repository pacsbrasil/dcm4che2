/*$Id$*/
/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2002 by TIANI MEDGRAPH AG <gunter.zeilinger@tiani.com>     *
 *                                                                           *
 *  This file is part of dcm4che.                                            *
 *                                                                           *
 *  This library is free software; you can redistribute it and/or modify it  *
 *  under the terms of the GNU Lesser General Public License as published    *
 *  by the Free Software Foundation; either version 2 of the License, or     *
 *  (at your option) any later version.                                      *
 *                                                                           *
 *  This library is distributed in the hope that it will be useful, but      *
 *  WITHOUT ANY WARRANTY; without even the implied warranty of               *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU        *
 *  Lesser General Public License for more details.                          *
 *                                                                           *
 *  You should have received a copy of the GNU Lesser General Public         *
 *  License along with this library; if not, write to the Free Software      *
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA  *
 *                                                                           *
 *****************************************************************************/

package org.dcm4che.media;

import org.dcm4che.Implementation;
import org.dcm4che.data.DcmEncodeParam;
import org.dcm4che.data.FileMetaInfo;
import org.dcm4che.dict.UIDs;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.imageio.stream.ImageInputStream;

/**
 * A DirBuilderFactory instance can be used to create
 * {@link org.dcm4che.media.DirReader},
 * {@link org.dcm4che.media.DirWriter},
 * {@link org.dcm4che.media.DirBuilder} and
 * {@link org.dcm4che.media.DirBuilderPref} objects.
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
public abstract class DirBuilderFactory {

    private static HashMap REC_TYPE_MAP = new HashMap(79);
    
    static {
        REC_TYPE_MAP.put(UIDs.StoredPrintStorage, DirRecord.STORED_PRINT);
        REC_TYPE_MAP.put(UIDs.HardcopyGrayscaleImageStorage, DirRecord.IMAGE);
        REC_TYPE_MAP.put(UIDs.HardcopyGrayscaleImageStorage, DirRecord.IMAGE);
        REC_TYPE_MAP.put(UIDs.HardcopyColorImageStorage, DirRecord.IMAGE);
        REC_TYPE_MAP.put(UIDs.ComputedRadiographyImageStorage, DirRecord.IMAGE);
        REC_TYPE_MAP.put(UIDs.DigitalXRayImageStorageForPresentation, DirRecord.IMAGE);
        REC_TYPE_MAP.put(UIDs.DigitalXRayImageStorageForProcessing, DirRecord.IMAGE);
        REC_TYPE_MAP.put(UIDs.DigitalMammographyXRayImageStorageForPresentation, DirRecord.IMAGE);
        REC_TYPE_MAP.put(UIDs.DigitalMammographyXRayImageStorageForProcessing, DirRecord.IMAGE);
        REC_TYPE_MAP.put(UIDs.DigitalIntraoralXRayImageStorageForPresentation, DirRecord.IMAGE);
        REC_TYPE_MAP.put(UIDs.DigitalIntraoralXRayImageStorageForProcessing, DirRecord.IMAGE);
        REC_TYPE_MAP.put(UIDs.CTImageStorage, DirRecord.IMAGE);
        REC_TYPE_MAP.put(UIDs.UltrasoundMultiframeImageStorageRetired, DirRecord.IMAGE);
        REC_TYPE_MAP.put(UIDs.UltrasoundMultiframeImageStorage, DirRecord.IMAGE);
        REC_TYPE_MAP.put(UIDs.MRImageStorage, DirRecord.IMAGE);
        REC_TYPE_MAP.put(UIDs.NuclearMedicineImageStorageRetired, DirRecord.IMAGE);
        REC_TYPE_MAP.put(UIDs.UltrasoundImageStorageRetired, DirRecord.IMAGE);
        REC_TYPE_MAP.put(UIDs.UltrasoundImageStorage, DirRecord.IMAGE);
        REC_TYPE_MAP.put(UIDs.SecondaryCaptureImageStorage, DirRecord.IMAGE);
        REC_TYPE_MAP.put(UIDs.MultiframeSingleBitSecondaryCaptureImageStorage, DirRecord.IMAGE);
        REC_TYPE_MAP.put(UIDs.MultiframeGrayscaleByteSecondaryCaptureImageStorage, DirRecord.IMAGE);
        REC_TYPE_MAP.put(UIDs.MultiframeGrayscaleWordSecondaryCaptureImageStorage, DirRecord.IMAGE);
        REC_TYPE_MAP.put(UIDs.MultiframeColorSecondaryCaptureImageStorage, DirRecord.IMAGE);
        REC_TYPE_MAP.put(UIDs.StandaloneOverlayStorage, DirRecord.OVERLAY);
        REC_TYPE_MAP.put(UIDs.StandaloneCurveStorage, DirRecord.CURVE);
        REC_TYPE_MAP.put(UIDs.TwelveLeadECGWaveformStorage, DirRecord.WAVEFORM);
        REC_TYPE_MAP.put(UIDs.GeneralECGWaveformStorage, DirRecord.WAVEFORM);
        REC_TYPE_MAP.put(UIDs.AmbulatoryECGWaveformStorage, DirRecord.WAVEFORM);
        REC_TYPE_MAP.put(UIDs.HemodynamicWaveformStorage, DirRecord.WAVEFORM);
        REC_TYPE_MAP.put(UIDs.CardiacElectrophysiologyWaveformStorage, DirRecord.WAVEFORM);
        REC_TYPE_MAP.put(UIDs.BasicVoiceAudioWaveformStorage, DirRecord.WAVEFORM);
        REC_TYPE_MAP.put(UIDs.StandaloneModalityLUTStorage, DirRecord.MODALITY_LUT);
        REC_TYPE_MAP.put(UIDs.StandaloneVOILUTStorage, DirRecord.VOI_LUT);
        REC_TYPE_MAP.put(UIDs.GrayscaleSoftcopyPresentationStateStorage, DirRecord.PRESENTATION);
        REC_TYPE_MAP.put(UIDs.XRayAngiographicImageStorage, DirRecord.IMAGE);
        REC_TYPE_MAP.put(UIDs.XRayRadiofluoroscopicImageStorage, DirRecord.IMAGE);
        REC_TYPE_MAP.put(UIDs.XRayAngiographicBiPlaneImageStorageRetired, DirRecord.IMAGE);
        REC_TYPE_MAP.put(UIDs.NuclearMedicineImageStorage, DirRecord.IMAGE);
        REC_TYPE_MAP.put(UIDs.VLImageStorageRetired, DirRecord.IMAGE);
        REC_TYPE_MAP.put(UIDs.VLMultiframeImageStorageRetired, DirRecord.IMAGE);
        REC_TYPE_MAP.put(UIDs.VLEndoscopicImageStorage, DirRecord.IMAGE);
        REC_TYPE_MAP.put(UIDs.VLMicroscopicImageStorage, DirRecord.IMAGE);
        REC_TYPE_MAP.put(UIDs.VLSlideCoordinatesMicroscopicImageStorage, DirRecord.IMAGE);
        REC_TYPE_MAP.put(UIDs.VLPhotographicImageStorage, DirRecord.IMAGE);
        REC_TYPE_MAP.put(UIDs.BasicTextSR, DirRecord.SR_DOCUMENT);
        REC_TYPE_MAP.put(UIDs.EnhancedSR, DirRecord.SR_DOCUMENT);
        REC_TYPE_MAP.put(UIDs.ComprehensiveSR, DirRecord.SR_DOCUMENT);
        REC_TYPE_MAP.put(UIDs.MammographyCADSR, DirRecord.SR_DOCUMENT);
        REC_TYPE_MAP.put(UIDs.KeyObjectSelectionDocument, DirRecord.KEY_OBJECT_DOC);
        REC_TYPE_MAP.put(UIDs.PositronEmissionTomographyImageStorage, DirRecord.IMAGE);
        REC_TYPE_MAP.put(UIDs.StandalonePETCurveStorage, DirRecord.CURVE);
        REC_TYPE_MAP.put(UIDs.RTImageStorage, DirRecord.IMAGE);
        REC_TYPE_MAP.put(UIDs.RTDoseStorage, DirRecord.RT_DOSE);
        REC_TYPE_MAP.put(UIDs.RTStructureSetStorage, DirRecord.RT_STRUCTURE_SET);
        REC_TYPE_MAP.put(UIDs.RTBeamsTreatmentRecordStorage, DirRecord.RT_TREAT_RECORD);
        REC_TYPE_MAP.put(UIDs.RTPlanStorage, DirRecord.RT_PLAN);
        REC_TYPE_MAP.put(UIDs.RTBrachyTreatmentRecordStorage, DirRecord.RT_TREAT_RECORD);
        REC_TYPE_MAP.put(UIDs.RTTreatmentSummaryRecordStorage, DirRecord.RT_TREAT_RECORD);
    }

    public static String getRecordType(String classUID) {
        String type = (String) REC_TYPE_MAP.get(classUID);
        if (type == null) {
            throw new UnsupportedOperationException("classUID:" + classUID);
        }
        return type;
    }
    
   /**
    * Obtain a new instance of a <code>DirBuilderFactory</code>.
    * This static method creates a new factory instance.
    *
    * @return new DirBuilderFactory instance, never null.
    */
    public static DirBuilderFactory getInstance() {
        return (DirBuilderFactory) Implementation.findFactory(
            "dcm4che.media.DirBuilderFactory");
    }
    
   /**
    * Construct a DirReader from a File. Normally a File reference should be
    * used rather than an ImageInputStream reference, so
    * {@link org.dcm4che.media.DirReader#getRefFile(String[])} can resolve
    * fileIDs to an absolute File reference.
    *
    * @param file A file reference to an existing DICOM dictionary (DICOMDIR).  
    * @throws IOException if file does not exist or is not a valid DICOM
    *                        dictionary
    * @return DirReader to read content of specified DICOM dictionary.
    */   
    public abstract DirReader newDirReader(File file) throws IOException;

   /**
    * Construct a DirReader from an ImageInputStream. Normally a File reference
    * should be used rather than an ImageInputStream reference, so
    * {@link org.dcm4che.media.DirReader#getRefFile(String[])} can resolve
    * a fileID to a absolute File reference.
    *
    * @param in A ImageInputStream reference to a DICOM dictionary (DICOMDIR).  
    * @throws IOException if the ImageInputStream does not reference a valid
    *                        DICOM dictionary
    * @return DirReader to read content of specified DICOM dictionary.
    */   
    public abstract DirReader newDirReader(ImageInputStream in)
            throws IOException;

   /**
    * Construct a DirWriter to update an existing DICOM dictionary.
    *  
    * @param file A file reference to an existing DICOM dictionary (DICOMDIR).
    * @param encParam Specifies encoding options, for new added directory
    *                 records. May be <code>null</code>, in which case
    *                 default encoding options will be used.
    *
    * @throws IOException if file does not reference a valid DICOM dictionary.
    * @return  DirWriter to update content of specified DICOM dictionary.
    */    
    public abstract DirWriter newDirWriter(File file, DcmEncodeParam encParam)
            throws IOException;

   /**
    * Create new DICOM dictionary specified by the File argument and the
    * Media Storage SOP Instance UID. Other attributes of its File Meta
    * Information will be initalized with default values. 
    *
    * @param file Specifies the new created DICOM dictionary (DICOMDIR).
    * @param uid Media Storage SOP Instance UID
    * @param filesetID File-set ID. May be <code>null</code>.
    * @param descriptorFile File-set Descriptor. May be <code>null</code>.
    * @param specCharSet Specific Character Set of File-set Descriptor File.
    *                    May be <code>null</code>.
    * @param encParam Specifies encoding options. May be <code>null</code>,
    *                 in which case default encoding options will be used.
    * 
    * @throws IOException if the creation of the new DICOM dictionary failed
    *                     caused by an i/o releated error.
    * @return  DirWriter to insert content into the new created
    *          DICOM dictionary.
    */    
    public abstract DirWriter newDirWriter(File file, String uid,
            String filesetID, File descriptorFile, String specCharSet,
            DcmEncodeParam encParam) throws IOException;

   /**
    * Create new DICOM dictionary specified by the File argument and explicit
    * specified File Meta Information. 
    *
    * @param file Specifies the new created DICOM dictionary (DICOMDIR).
    * @param fmi explicit specified File Meta Information
    * @param filesetID File-set ID. May be <code>null</code>.
    * @param descriptorFile File-set Descriptor. May be <code>null</code>.
    * @param specCharSet Specific Character Set of File-set Descriptor File.
    *                    May be <code>null</code>.
    * @param encParam Specifies encoding options. May be <code>null</code>,
    *                 in which case default encoding options will be used.
    * 
    * @throws IOException if the creation of the new DICOM dictionary failed
    *                     caused by an i/o releated error.
    * @return  DirWriter to insert content into the new created
    *          DICOM dictionary.
    */    
    public abstract DirWriter newDirWriter(File file, FileMetaInfo fmi,
            String filesetID, File descriptorFile, String specCharSet,
            DcmEncodeParam encParam) throws IOException;

   /**
    * Create empty preferences for DirBuilder. 
    * @return empty preferences for DirBuilder.*/    
    public abstract DirBuilderPref newDirBuilderPref();

    /**
     * Creates new DirBuilder associated with specified DirWriter and with
     * specified preferences.
     *
     * @param writer associated DirWriter.
     * @param pref Preferences for generated directory records.
     * @return  new DirBuilder associated with specified DirWriter.
     */    
    public abstract DirBuilder newDirBuilder(DirWriter writer,
            DirBuilderPref pref);
    
}
