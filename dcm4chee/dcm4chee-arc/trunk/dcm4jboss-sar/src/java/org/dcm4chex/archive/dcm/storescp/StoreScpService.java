/* $Id$ Copyright
 * (c) 2002,2003 by TIANI MEDGRAPH AG
 * 
 * This file is part of dcm4che.
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package org.dcm4chex.archive.dcm.storescp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.dcm4che.dict.UIDs;
import org.dcm4che.net.AcceptorPolicy;
import org.dcm4che.net.DcmServiceRegistry;

import org.dcm4chex.archive.dcm.AbstractScpService;
import org.dcm4chex.archive.util.EJBHomeFactory;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision$ $Date$
 * @since 03.08.2003
 */
public class StoreScpService extends AbstractScpService {
    
    private static final String[] IMAGE_CUIDS = {
            UIDs.HardcopyGrayscaleImageStorage, UIDs.HardcopyColorImageStorage,
            UIDs.ComputedRadiographyImageStorage,
            UIDs.DigitalXRayImageStorageForPresentation,
            UIDs.DigitalXRayImageStorageForProcessing,
            UIDs.DigitalMammographyXRayImageStorageForPresentation,
            UIDs.DigitalMammographyXRayImageStorageForProcessing,
            UIDs.DigitalIntraoralXRayImageStorageForPresentation,
            UIDs.DigitalIntraoralXRayImageStorageForProcessing,
            UIDs.CTImageStorage, UIDs.UltrasoundMultiframeImageStorageRetired,
            UIDs.UltrasoundMultiframeImageStorage, UIDs.MRImageStorage,
            UIDs.EnhancedMRImageStorage,
            UIDs.NuclearMedicineImageStorageRetired,
            UIDs.UltrasoundImageStorageRetired, UIDs.UltrasoundImageStorage,
            UIDs.SecondaryCaptureImageStorage,
            UIDs.MultiframeSingleBitSecondaryCaptureImageStorage,
            UIDs.MultiframeGrayscaleByteSecondaryCaptureImageStorage,
            UIDs.MultiframeGrayscaleWordSecondaryCaptureImageStorage,
            UIDs.MultiframeColorSecondaryCaptureImageStorage,
            UIDs.XRayAngiographicImageStorage,
            UIDs.XRayRadiofluoroscopicImageStorage,
            UIDs.XRayAngiographicBiPlaneImageStorageRetired,
            UIDs.NuclearMedicineImageStorage, UIDs.VLImageStorageRetired,
            UIDs.VLMultiframeImageStorageRetired,
            UIDs.VLEndoscopicImageStorage, UIDs.VLMicroscopicImageStorage,
            UIDs.VLSlideCoordinatesMicroscopicImageStorage,
            UIDs.VLPhotographicImageStorage,
            UIDs.PositronEmissionTomographyImageStorage, UIDs.RTImageStorage,};

    private static final String[] OTHER_CUIDS = { UIDs.BasicTextSR,
            UIDs.EnhancedSR, UIDs.ComprehensiveSR, UIDs.MammographyCADSR,
            UIDs.GrayscaleSoftcopyPresentationStateStorage,
            UIDs.KeyObjectSelectionDocument, UIDs.RTDoseStorage,
            UIDs.RTStructureSetStorage, UIDs.RTBeamsTreatmentRecordStorage,
            UIDs.RTPlanStorage, UIDs.RTBrachyTreatmentRecordStorage,
            UIDs.RTTreatmentSummaryRecordStorage,};

    private Set retrieveAETSet;

    private boolean acceptStorageCommitment = true;

    private boolean acceptJPEGBaseline = true;

    private boolean acceptJPEGExtended = false;

    private boolean acceptJPEGLossless14 = true;

    private boolean acceptJPEGLossless = true;

    private boolean acceptJPEGLSLossless = false;

    private boolean acceptJPEGLSLossy = false;

    private boolean acceptJPEG2000Lossless = false;

    private boolean acceptJPEG2000Lossy = false;

    private boolean acceptRLELossless = false;

    private int acTimeout = 5000;

    private int dimseTimeout = 0;

    private int soCloseDelay = 500;

    private StoreScp scp = new StoreScp(this);

    private StgCmtScp stgCmtScp = new StgCmtScp(this);

    public String getEjbProviderURL() {
        return EJBHomeFactory.getEjbProviderURL();
    }        

    public void setEjbProviderURL(String ejbProviderURL) {
        EJBHomeFactory.setEjbProviderURL(ejbProviderURL);
    }

    public String[] getMaskWarningAsSuccessForCallingAETs() {
        return scp.getMaskWarningAsSuccessForCallingAETs();
    }

    public void setMaskWarningAsSuccessForCallingAETs(String[] aets) {
        scp.setMaskWarningAsSuccessForCallingAETs(aets);
    }

    public String[] getStorageDirs() {
        return scp.getStorageDirs();
    }

    public void setStorageDirs(String[] dirs) throws IOException {
        scp.setStorageDirs(dirs);
    }

    public final String getForwardAETs() {
        return scp.getForwardAETs().toString();
    }

    public void setForwardAETs(String aets) {
        scp.setForwardAETs(new ForwardAETs(aets));
    }

    public int getForwardPriority() {
        return scp.getForwardPriority();
    }

    public void setForwardPriority(int forwardPriority) {
        scp.setForwardPriority(forwardPriority);
    }

    public final int getUpdateDatabaseMaxRetries() {
        return scp.getUpdateDatabaseMaxRetries();
    }

    public final void setUpdateDatabaseMaxRetries(int updateDatabaseMaxRetries) {
        scp.setUpdateDatabaseMaxRetries(updateDatabaseMaxRetries);
    }

    public final int getAcTimeout() {
        return acTimeout;
    }

    public final void setAcTimeout(int acTimeout) {
        this.acTimeout = acTimeout;
    }

    public final int getDimseTimeout() {
        return dimseTimeout;
    }

    public final void setDimseTimeout(int dimseTimeout) {
        this.dimseTimeout = dimseTimeout;
    }

    public final int getSoCloseDelay() {
        return soCloseDelay;
    }

    public final void setSoCloseDelay(int soCloseDelay) {
        this.soCloseDelay = soCloseDelay;
    }

    public final String[] getRetrieveAETs() {
        return (String[]) retrieveAETSet.toArray(new String[retrieveAETSet
                .size()]);
    }

    public final void setRetrieveAETs(String[] aets) {
        if (aets.length == 0) { throw new IllegalArgumentException(
                "Missing Retrieve AET"); }
        this.retrieveAETSet = Collections.unmodifiableSet(new HashSet(Arrays
                .asList(aets)));
    }

    final Set getRetrieveAETSet() {
        return retrieveAETSet;
    }

    final String getRetrieveAET() {
         return (String) retrieveAETSet.iterator().next();
    }
    
    public boolean isAcceptStorageCommitment() {
        return acceptStorageCommitment;
    }

    public void setAcceptStorageCommitment(boolean storageCommitmentPushModel) {
        this.acceptStorageCommitment = storageCommitmentPushModel;
    }

    public final boolean isAcceptJPEG2000Lossless() {
        return acceptJPEG2000Lossless;
    }

    public final void setAcceptJPEG2000Lossless(boolean acceptJPEG2000Lossless) {
        this.acceptJPEG2000Lossless = acceptJPEG2000Lossless;
    }

    public final boolean isAcceptJPEG2000Lossy() {
        return acceptJPEG2000Lossy;
    }

    public final void setAcceptJPEG2000Lossy(boolean acceptJPEG2000Lossy) {
        this.acceptJPEG2000Lossy = acceptJPEG2000Lossy;
    }

    public final boolean isAcceptJPEGBaseline() {
        return acceptJPEGBaseline;
    }

    public final void setAcceptJPEGBaseline(boolean acceptJPEGBaseline) {
        this.acceptJPEGBaseline = acceptJPEGBaseline;
    }

    public final boolean isAcceptJPEGExtended() {
        return acceptJPEGExtended;
    }

    public final void setAcceptJPEGExtended(boolean acceptJPEGExtended) {
        this.acceptJPEGExtended = acceptJPEGExtended;
    }

    public final boolean isAcceptJPEGLossless() {
        return acceptJPEGLossless;
    }

    public final void setAcceptJPEGLossless(boolean acceptJPEGLossless) {
        this.acceptJPEGLossless = acceptJPEGLossless;
    }

    public final boolean isAcceptJPEGLossless14() {
        return acceptJPEGLossless14;
    }

    public final void setAcceptJPEGLossless14(boolean acceptJPEGLossless14) {
        this.acceptJPEGLossless14 = acceptJPEGLossless14;
    }

    public final boolean isAcceptJPEGLSLossless() {
        return acceptJPEGLSLossless;
    }

    public final void setAcceptJPEGLSLossless(boolean acceptJPEGLSLossless) {
        this.acceptJPEGLSLossless = acceptJPEGLSLossless;
    }

    public final boolean isAcceptJPEGLSLossy() {
        return acceptJPEGLSLossy;
    }

    public final void setAcceptJPEGLSLossy(boolean acceptJPEGLSLossy) {
        this.acceptJPEGLSLossy = acceptJPEGLSLossy;
    }

    public final boolean isAcceptRLELossless() {
        return acceptRLELossless;
    }

    public final void setAcceptRLELossless(boolean acceptRLELossless) {
        this.acceptRLELossless = acceptRLELossless;
    }

    protected void startService() throws Exception {
        scp.checkReadyToStart();
        super.startService();
    }

    protected void bindDcmServices(DcmServiceRegistry services) {
        for (int i = 0; i < IMAGE_CUIDS.length; ++i) {
            services.bind(IMAGE_CUIDS[i], scp);
        }
        for (int i = 0; i < OTHER_CUIDS.length; ++i) {
            services.bind(OTHER_CUIDS[i], scp);
        }
        services.bind(UIDs.StorageCommitmentPushModel, stgCmtScp);
        dcmHandler.addAssociationListener(scp);
    }

    protected void unbindDcmServices(DcmServiceRegistry services) {
        for (int i = 0; i < IMAGE_CUIDS.length; ++i) {
            services.unbind(IMAGE_CUIDS[i]);
        }
        for (int i = 0; i < OTHER_CUIDS.length; ++i) {
            services.unbind(OTHER_CUIDS[i]);
        }
        services.unbind(UIDs.StorageCommitmentPushModel);
        dcmHandler.removeAssociationListener(scp);
    }

    private String[] getImageTS() {
        ArrayList list = new ArrayList();
        if (acceptJPEGBaseline) {
            list.add(UIDs.JPEGBaseline);
        }
        if (acceptJPEGExtended) {
            list.add(UIDs.JPEGExtended);
        }
        if (acceptJPEGLSLossy) {
            list.add(UIDs.JPEGLSLossy);
        }
        if (acceptJPEG2000Lossy) {
            list.add(UIDs.JPEG2000Lossy);
        }
        if (acceptJPEGLSLossless) {
            list.add(UIDs.JPEGLSLossless);
        }
        if (acceptJPEG2000Lossless) {
            list.add(UIDs.JPEG2000Lossless);
        }
        if (acceptJPEGLossless14) {
            list.add(UIDs.JPEGLossless14);
        }
        if (acceptJPEGLossless) {
            list.add(UIDs.JPEGLossless);
        }
        if (acceptRLELossless) {
            list.add(UIDs.RLELossless);
        }
        if (acceptExplicitVRLE) {
            list.add(UIDs.ExplicitVRLittleEndian);
        }
        list.add(UIDs.ImplicitVRLittleEndian);
        return (String[]) list.toArray(new String[list.size()]);
    }

    protected void initPresContexts(AcceptorPolicy policy) {
        addPresContexts(policy, IMAGE_CUIDS, getImageTS());
        addPresContexts(policy, OTHER_CUIDS, getTransferSyntaxUIDs());
        if (acceptStorageCommitment)
                policy.putPresContext(UIDs.StorageCommitmentPushModel,
                        getTransferSyntaxUIDs());
    }
}
