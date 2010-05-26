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
 * Agfa-Gevaert AG.
 * Portions created by the Initial Developer are Copyright (C) 2008
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

package org.dcm4chee.web.war.folder;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.Application;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WebRequestCycle;
import org.dcm4che2.data.UID;
import org.dcm4chee.web.common.delegate.BaseMBeanDelegate;
import org.dcm4chee.web.war.folder.model.InstanceModel;
import org.dcm4chee.web.war.folder.model.SeriesModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Franz Willer <franz.willer@gmail.com>
 * @version $Revision$ $Date$
 * @since Aug 18, 2009
 */
public class WADODelegate extends BaseMBeanDelegate {

    public static final int NOT_RENDERABLE = -1;
    public static final int IMAGE = 1;
    public static final int TEXT = 2;
    public static final int VIDEO = 3;
    public static final int ENCAPSULATED = 4;
    
    private static WADODelegate delegate;
    private List<String> imageCuids = Arrays.asList( 
            UID.CTImageStorage, UID.ComputedRadiographyImageStorage, 
            UID.DigitalIntraoralXRayImageStorageForPresentation, 
            UID.DigitalIntraoralXRayImageStorageForProcessing, 
            UID.DigitalMammographyXRayImageStorageForPresentation, 
            UID.DigitalMammographyXRayImageStorageForProcessing, 
            UID.DigitalXRayImageStorageForPresentation, 
            UID.DigitalXRayImageStorageForProcessing, 
            UID.EnhancedCTImageStorage, 
            UID.EnhancedMRImageStorage, 
            UID.EnhancedXAImageStorage, 
            UID.EnhancedXRFImageStorage, 
            UID.HardcopyColorImageStorageSOPClassRetired, 
            UID.HardcopyGrayscaleImageStorageSOPClassRetired, 
            UID.MRImageStorage, 
            UID.MultiframeGrayscaleByteSecondaryCaptureImageStorage, 
            UID.MultiframeGrayscaleWordSecondaryCaptureImageStorage, 
            UID.MultiframeSingleBitSecondaryCaptureImageStorage, 
            UID.MultiframeTrueColorSecondaryCaptureImageStorage, 
            UID.NuclearMedicineImageStorage, 
            UID.NuclearMedicineImageStorageRetired, 
            UID.OphthalmicPhotography16BitImageStorage, 
            UID.OphthalmicPhotography8BitImageStorage, 
            UID.PositronEmissionTomographyImageStorage, 
            UID.RTImageStorage, 
            UID.SecondaryCaptureImageStorage, 
            UID.UltrasoundImageStorage, 
            UID.UltrasoundImageStorageRetired, 
            UID.UltrasoundMultiframeImageStorage, 
            UID.UltrasoundMultiframeImageStorageRetired, 
            UID.VLEndoscopicImageStorage, 
            UID.VLImageStorageTrialRetired, 
            UID.VLMicroscopicImageStorage, 
            UID.VLMultiframeImageStorageTrialRetired, 
            UID.VLPhotographicImageStorage, 
            UID.VLSlideCoordinatesMicroscopicImageStorage, 
            UID.XRayAngiographicBiPlaneImageStorageRetired, 
            UID.XRayAngiographicImageStorage, 
            UID.XRayRadiofluoroscopicImageStorage );
    private List<String> videoCuids = Arrays.asList(
            UID.VideoEndoscopicImageStorage,
            UID.VideoMicroscopicImageStorage,
            UID.VideoPhotographicImageStorage );
    private List<String> textCuids = Arrays.asList(
            UID.EnhancedSRStorage,
            UID.KeyObjectSelectionDocumentStorage,
            UID.MammographyCADSRStorage,
            UID.ProcedureLogStorage,
            UID.XRayRadiationDoseSRStorage);
    private List<String> encapsCuids = Arrays.asList(
            UID.EncapsulatedCDAStorage,
            UID.EncapsulatedPDFStorage);

    private static Logger log = LoggerFactory.getLogger(WADODelegate.class);

    private WADODelegate() {
        super();
    }

    private String getWadoBaseUrl() {
        String wadoBaseURL = ((WebApplication)Application.get()).getInitParameter("wadoBaseURL");
        if (wadoBaseURL==null) {
            HttpServletRequest request = ((WebRequestCycle)RequestCycle.get()).getWebRequest()
            .getHttpServletRequest();
            try {
                URL wadoURL = new URL( request.isSecure() ? "https" : "http", request.getServerName(),
                        request.getServerPort(), "/wado?requestType=WADO");
                wadoBaseURL = wadoURL.toString();
            } catch (MalformedURLException e) {
                log.warn("Cant build WADO Base URL for request! use http://localhost:8080/wado?requestType=WADO");
                wadoBaseURL = "http://localhost:8080/wado?requestType=WADO";
            }
        }
        return wadoBaseURL;
    }

    public int getRenderType(String sopClassUid) {
        // TODO SOPClassUIDs configurable!
        if (imageCuids.contains(sopClassUid))
            return IMAGE;
        if (textCuids.contains(sopClassUid))
            return TEXT;
        if (videoCuids.contains(sopClassUid))
            return VIDEO;
        if (encapsCuids.contains(sopClassUid))
            return ENCAPSULATED;
        return NOT_RENDERABLE;
    }
    
    public String getURL(InstanceModel instModel) {
        SeriesModel seriesModel = instModel.getParent();
        log.info("seriesModel:"+seriesModel);
        log.info("ppsModel:"+seriesModel.getParent());
        log.info("studyModel:"+seriesModel.getParent().getParent());
        return getWadoBaseUrl()+"&studyUID="+seriesModel.getParent().getParent().getStudyInstanceUID()+"&seriesUID="+
            seriesModel.getSeriesInstanceUID()+"&objectUID="+instModel.getSOPInstanceUID();
    }
    
    @Override
    public String getInitParameterName() {
        return "wadoServiceName";
    }

    public static WADODelegate getInstance() {
        if (delegate==null)
            delegate = new WADODelegate();
        return delegate;
    }
}
