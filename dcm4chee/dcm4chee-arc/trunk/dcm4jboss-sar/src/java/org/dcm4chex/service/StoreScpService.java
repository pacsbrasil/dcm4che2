/*
 * Copyright (c) 2002,2003 by TIANI MEDGRAPH AG
 *
 * This file is part of dcm4che.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package org.dcm4chex.service;

import javax.management.ObjectName;

import org.dcm4che.dict.UIDs;
import org.dcm4che.net.AcceptorPolicy;
import org.dcm4che.net.DcmServiceRegistry;
import org.dcm4che.server.DcmHandler;
import org.jboss.system.ServiceMBeanSupport;

/**
 * @jmx.mbean
 *  extends="org.jboss.system.ServiceMBean"
 * 
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision$
 * @since 03.08.2003
 */
public class StoreScpService
    extends ServiceMBeanSupport
    implements
        org.dcm4chex.service.StoreScpServiceMBean {

    private final static String[] NATIVE_TS = {
            UIDs.ExplicitVRLittleEndian,
            UIDs.ImplicitVRLittleEndian
            };

    private final static String[] STORAGE_AS = {
            UIDs.ComputedRadiographyImageStorage,
            UIDs.DigitalXRayImageStorageForPresentation,
            UIDs.DigitalXRayImageStorageForProcessing,
            UIDs.DigitalMammographyXRayImageStorageForPresentation,
            UIDs.DigitalMammographyXRayImageStorageForProcessing,
            UIDs.DigitalIntraoralXRayImageStorageForPresentation,
            UIDs.DigitalIntraoralXRayImageStorageForProcessing,
            UIDs.CTImageStorage,
            UIDs.UltrasoundMultiframeImageStorageRetired,
            UIDs.UltrasoundMultiframeImageStorage,
            UIDs.MRImageStorage,
            UIDs.EnhancedMRImageStorage,
            UIDs.MRSpectroscopyStorage,
            UIDs.NuclearMedicineImageStorageRetired,
            UIDs.UltrasoundImageStorageRetired,
            UIDs.UltrasoundImageStorage,
            UIDs.SecondaryCaptureImageStorage,
            UIDs.MultiframeSingleBitSecondaryCaptureImageStorage,
            UIDs.MultiframeGrayscaleByteSecondaryCaptureImageStorage,
            UIDs.MultiframeGrayscaleWordSecondaryCaptureImageStorage,
            UIDs.MultiframeColorSecondaryCaptureImageStorage,
            UIDs.HardcopyGrayscaleImageStorage,
            UIDs.HardcopyColorImageStorage,
            UIDs.StandaloneOverlayStorage,
            UIDs.StandaloneCurveStorage,
            UIDs.TwelveLeadECGWaveformStorage,
            UIDs.GeneralECGWaveformStorage,
            UIDs.AmbulatoryECGWaveformStorage,
            UIDs.HemodynamicWaveformStorage,
            UIDs.CardiacElectrophysiologyWaveformStorage,
            UIDs.BasicVoiceAudioWaveformStorage,
            UIDs.StandaloneModalityLUTStorage,
            UIDs.StandaloneVOILUTStorage,
            UIDs.GrayscaleSoftcopyPresentationStateStorage,
            UIDs.XRayAngiographicImageStorage,
            UIDs.XRayRadiofluoroscopicImageStorage,
            UIDs.XRayAngiographicBiPlaneImageStorageRetired,
            UIDs.NuclearMedicineImageStorage,
            UIDs.RawDataStorage,
            UIDs.VLImageStorageRetired,
            UIDs.VLMultiframeImageStorageRetired,
            UIDs.VLEndoscopicImageStorage,
            UIDs.VLMicroscopicImageStorage,
            UIDs.VLSlideCoordinatesMicroscopicImageStorage,
            UIDs.VLPhotographicImageStorage,
            UIDs.BasicTextSR,
            UIDs.EnhancedSR,
            UIDs.ComprehensiveSR,
            UIDs.MammographyCADSR,
            UIDs.KeyObjectSelectionDocument,
            UIDs.PositronEmissionTomographyImageStorage,
            UIDs.StandalonePETCurveStorage,
            UIDs.RTImageStorage,
            UIDs.RTDoseStorage,
            UIDs.RTStructureSetStorage,
            UIDs.RTBeamsTreatmentRecordStorage,
            UIDs.RTPlanStorage,
            UIDs.RTBrachyTreatmentRecordStorage,
            UIDs.RTTreatmentSummaryRecordStorage
            };

    private ObjectName dcmServerName;
    private DcmHandler dcmHandler;
    private StoreScp scp = new StoreScp(this);

    /**
     * @jmx.managed-attribute
     */
    public ObjectName getDcmServerName()
    {
        return dcmServerName;
    }


    /**
     * @jmx.managed-attribute
     */
    public void setDcmServerName(ObjectName dcmServerName)
    {
        this.dcmServerName = dcmServerName;
    }

    /**
     * @jmx.managed-attribute
     */
    public String getProviderURL() {
        return scp.getProviderURL();
    }

    /**
     * @jmx.managed-attribute
     */
    public void setProviderURL(String providerURL) {
        scp.setProviderURL(providerURL);
    }


    /**
     * @jmx.managed-attribute
     */
    public String getMountPoint()
    {
        return scp.getMountPoint();
    }


    /**
     * @jmx.managed-attribute
     */
    public void setMountPoint(String mnt)
    {
        scp.setMountPoint(mnt);
    }
            
    protected void startService() throws Exception {
        if (scp.getMountPoint() == null) {
            throw new IllegalStateException("MountPoint not configured");
        }
        dcmHandler =
                (DcmHandler) server.getAttribute(dcmServerName, "DcmHandler");
        bindDcmServices();
        updatePolicy(NATIVE_TS);
    }

    protected void stopService() throws Exception {
        updatePolicy(null);
        unbindDcmServices();
        dcmHandler = null;
    }

    private void bindDcmServices()
    {
        DcmServiceRegistry services = dcmHandler.getDcmServiceRegistry();
        for (int i = 0; i < STORAGE_AS.length; ++i) {
            services.bind(STORAGE_AS[i], scp);
        }
        dcmHandler.addAssociationListener(scp);
    }


    private void unbindDcmServices()
    {
        DcmServiceRegistry services = dcmHandler.getDcmServiceRegistry();
        for (int i = 0; i < STORAGE_AS.length; ++i) {
            services.unbind(STORAGE_AS[i]);
        }
        dcmHandler.removeAssociationListener(scp);
    }

    private void updatePolicy(String[] tsuids)
    {
        AcceptorPolicy policy = dcmHandler.getAcceptorPolicy();
        for (int i = 0; i < STORAGE_AS.length; ++i) {
            policy.putPresContext(STORAGE_AS[i], tsuids);
        }
    }
}
