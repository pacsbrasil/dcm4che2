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

package org.dcm4che2.tool.dcmrcv;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.dcm4che2.config.TransferCapability;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;
import org.dcm4che2.data.VR;
import org.dcm4che2.io.DicomOutputStream;
import org.dcm4che2.net.ApplicationEntity;
import org.dcm4che2.net.Association;
import org.dcm4che2.net.Connector;
import org.dcm4che2.net.Device;
import org.dcm4che2.net.PDVInputStream;
import org.dcm4che2.net.service.StorageService;
import org.dcm4che2.net.service.VerificationService;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision$ $Date$
 * @since Oct 13, 2005
 */
public class DcmRcv extends StorageService {

    private static final int MEGABYTE = 1048576;
    private static final String USAGE = 
        "dcmrcv [Options] [<aet>[@<ip>]:]<port>";
    private static final String DESCRIPTION = 
        "DICOM Server listening on specified <port> for incoming association " +
        "requests. If no local IP address of the network interface is specified " +
        "connections on any/all local addresses are accepted. If <aet> is" +
        "specified, only requests with matching called AE title will be " +
        "accepted.\n" +
        "Options:";
    private static final String EXAMPLE = 
        "\nExample: dcmrcv DCMRCV:11112 --dest /tmp \n" +
        "=> Starts server listening on port 11112, accepting association " +
        "requests with DCMRCV as called AE title. Received objects " +
        "are stored to /tmp.";
    
    private static final String[] ONLY_DEF_TS =
    {
        UID.ImplicitVRLittleEndian
    };

    private static final String[] NATIVE_TS =
    {
        UID.ExplicitVRLittleEndian,
        UID.ExplicitVRBigEndian,
        UID.ImplicitVRLittleEndian
    };

    private static final String[] NATIVE_LE_TS =
    {
        UID.ExplicitVRLittleEndian,
        UID.ImplicitVRLittleEndian
    };
    
    private static final String[] NON_RETIRED_TS =
    {
        UID.JPEGLSLossless,
        UID.JPEGLossless,
        UID.JPEGLosslessNonHierarchical14,
        UID.JPEG2000LosslessOnly,
        UID.DeflatedExplicitVRLittleEndian,
        UID.RLELossless,
        UID.ExplicitVRLittleEndian,
        UID.ExplicitVRBigEndian,
        UID.ImplicitVRLittleEndian,
        UID.JPEGBaseline1,
        UID.JPEGExtended24,
        UID.JPEGLSLossyNearLossless,
        UID.JPEG2000,
        UID.MPEG2,
    };
    
    private static final String[] CUIDS =
    {
        UID.BasicStudyContentNotificationSOPClass,
        UID.StoredPrintStorageSOPClass,
        UID.HardcopyGrayscaleImageStorageSOPClass,
        UID.HardcopyColorImageStorageSOPClass,
        UID.ComputedRadiographyImageStorage,
        UID.DigitalXRayImageStorageForPresentation,
        UID.DigitalXRayImageStorageForProcessing,
        UID.DigitalMammographyXRayImageStorageForPresentation,
        UID.DigitalMammographyXRayImageStorageForProcessing,
        UID.DigitalIntraoralXRayImageStorageForPresentation,
        UID.DigitalIntraoralXRayImageStorageForProcessing,
        UID.StandaloneModalityLUTStorage,
        UID.EncapsulatedPDFStorage,
        UID.StandaloneVOILUTStorage,
        UID.GrayscaleSoftcopyPresentationStateStorageSOPClass,
        UID.ColorSoftcopyPresentationStateStorageSOPClass,
        UID.PseudoColorSoftcopyPresentationStateStorageSOPClass,
        UID.BlendingSoftcopyPresentationStateStorageSOPClass,
        UID.XRayAngiographicImageStorage,
        UID.EnhancedXAImageStorage,
        UID.XRayRadiofluoroscopicImageStorage,
        UID.EnhancedXRFImageStorage,
        UID.XRayAngiographicBiPlaneImageStorageRetired,
        UID.PositronEmissionTomographyImageStorage,
        UID.StandalonePETCurveStorage,
        UID.CTImageStorage,
        UID.EnhancedCTImageStorage,
        UID.NuclearMedicineImageStorage,
        UID.UltrasoundMultiframeImageStorageRetired,
        UID.UltrasoundMultiframeImageStorage,
        UID.MRImageStorage,
        UID.EnhancedMRImageStorage,
        UID.MRSpectroscopyStorage,
        UID.RTImageStorage,
        UID.RTDoseStorage,
        UID.RTStructureSetStorage,
        UID.RTBeamsTreatmentRecordStorage,
        UID.RTPlanStorage,
        UID.RTBrachyTreatmentRecordStorage,
        UID.RTTreatmentSummaryRecordStorage,
        UID.NuclearMedicineImageStorageRetired,
        UID.UltrasoundImageStorageRetired,
        UID.UltrasoundImageStorage,
        UID.RawDataStorage,
        UID.SpatialRegistrationStorage,
        UID.SpatialFiducialsStorage,
        UID.RealWorldValueMappingStorage,
        UID.SecondaryCaptureImageStorage,
        UID.MultiframeSingleBitSecondaryCaptureImageStorage,
        UID.MultiframeGrayscaleByteSecondaryCaptureImageStorage,
        UID.MultiframeGrayscaleWordSecondaryCaptureImageStorage,
        UID.MultiframeTrueColorSecondaryCaptureImageStorage,
        UID.VLImageStorageRetired,
        UID.VLEndoscopicImageStorage,
        UID.VideoEndoscopicImageStorage,
        UID.VLMicroscopicImageStorage,
        UID.VideoMicroscopicImageStorage,
        UID.VLSlideCoordinatesMicroscopicImageStorage,
        UID.VLPhotographicImageStorage,
        UID.VideoPhotographicImageStorage,
        UID.OphthalmicPhotography8BitImageStorage,
        UID.OphthalmicPhotography16BitImageStorage,
        UID.StereometricRelationshipStorage,
        UID.VLMultiframeImageStorageRetired,
        UID.StandaloneOverlayStorage,
        UID.BasicTextSR,
        UID.EnhancedSR,
        UID.ComprehensiveSR,
        UID.ProcedureLogStorage,
        UID.MammographyCADSR,
        UID.KeyObjectSelectionDocument,
        UID.ChestCADSR ,
        UID.StandaloneCurveStorage,
        UID._12leadECGWaveformStorage,
        UID.GeneralECGWaveformStorage,
        UID.AmbulatoryECGWaveformStorage,
        UID.HemodynamicWaveformStorage,
        UID.CardiacElectrophysiologyWaveformStorage,
        UID.BasicVoiceAudioWaveformStorage,
        UID.HangingProtocolStorage
    };

    
    private Device device = new Device("DCMRCV");
    private ApplicationEntity ae = new ApplicationEntity();
    private Connector connector = new Connector();
    private String[] tsuids = NON_RETIRED_TS;
    private File destination;
    private boolean devnull;
    private int fileBufferSize = 256;
    
    public DcmRcv()
    {
        super(CUIDS);
        device.addApplicationEntity(ae);
        ae.addConnector(connector);
        ae.setAssociationAcceptor(true);
        ae.register(new VerificationService());
        ae.register(this);
    }
       
    public final void setAEtitle(String aet)
    {
        ae.setAETitle(aet);
    }

    public final void setHostname(String hostname)
    {
        connector.setHostname(hostname);
    }
    
    public final void setPort(int port)
    {
        connector.setPort(port);
    }
    
    public final void setPackPDV(boolean packPDV)
    {
        ae.setPackPDV(packPDV);
    }

    public final void setAssociationReaperPeriod(int period)
    {
        device.setAssociationReaperPeriod(period);
    }
    
    public final void setTcpNoDelay(boolean tcpNoDelay)
    {
        connector.setTcpNoDelay(tcpNoDelay);
    }

    public final void setRequestTimeout(int timeout)
    {
        connector.setRequestTimeout(timeout);
    }

    public final void setReleaseTimeout(int timeout)
    {
        connector.setReleaseTimeout(timeout);
    }

    public final void setSocketCloseDelay(int timeout)
    {
        connector.setSocketCloseDelay(timeout);
    }

    public final void setIdleTimeout(int timeout)
    {
        ae.setIdleTimeout(timeout);        
    }
    
    public final void setDimseRspTimeout(int timeout)
    {
        ae.setDimseRspTimeout(timeout);        
    }
    
    public final void setMaxPDULengthSend(int maxLength)
    {
        ae.setMaxPDULengthSend(maxLength);        
    }
    
    public void setMaxPDULengthReceive(int maxLength)
    {
        ae.setMaxPDULengthReceive(maxLength);        
    }
    
    public final void setReceiveBufferSize(int bufferSize)
    {
        connector.setReceiveBufferSize(bufferSize);
    }

    public final void setSendBufferSize(int bufferSize)
    {
        connector.setSendBufferSize(bufferSize);
    }

    private static CommandLine parse(String[] args)
    {
        Options opts = new Options();
        Option dest = new Option("d", "dest", true,
                "store received objects into files in specified directory <dir>." +
                " Do not store received objects by default.");
        dest.setArgName("dir");
        opts.addOption(dest);
        opts.addOption("k", "pack-pdv", false, 
                "pack command and data PDV in one P-DATA-TF PDU, " +
                "send only one PDV in one P-Data-TF PDU by default.");
        opts.addOption("y", "tcp-no-delay", false, 
                "set TCP_NODELAY socket option to true, false by default");
        Option closeDelay = new Option("c", "close-delay", true,
                "delay in ms for Socket close after sending A-ABORT, 50ms by default");
        closeDelay.setArgName("delay");
        opts.addOption(closeDelay);
        Option acTimeout = new Option("T", "request-timeout", true,
                "timeout in ms for receiving A-ASSOCIATE-RQ, 5s by default");
        acTimeout.setArgName("timeout");
        opts.addOption(acTimeout);
        Option rpTimeout = new Option("t", "release-timeout", true,
                "timeout in ms for receiving A-RELEASE-RP, 5s by default");
        rpTimeout.setArgName("timeout");
        opts.addOption(rpTimeout);
        Option checkPeriod = new Option("D", "reaper-period", true,
                "period in ms to check idleness, 10s by default");
        checkPeriod.setArgName("period");
        Option idleTimeout = new Option("I", "idle-timeout", true,
                "timeout in ms for receiving DIMSE-RQ, 60s by default");
        acTimeout.setArgName("timeout");
        opts.addOption(idleTimeout);
        Option soRcvBufSize = new Option("s", "so-rcv-buf-size", true,
                "set SO_RCVBUF socket option to specified value");
        soRcvBufSize.setArgName("size");
        opts.addOption(soRcvBufSize);
        Option soSndBufSize = new Option("S", "so-snd-buf-size", true,
                "set SO_SNDBUF socket option to specified value");
        soSndBufSize.setArgName("size");
        opts.addOption(soSndBufSize);
        Option rcvPduLen = new Option("u", "rcv-pdu-len", true,
                "maximal length of received P-DATA-TF PDUs, 16384 by default");
        rcvPduLen.setArgName("max-len");
        opts.addOption(rcvPduLen);
        Option sndPduLen = new Option("U", "snd-pdu-len", true,
                "maximal length of sent P-DATA-TF PDUs, 16384 by default");
        sndPduLen.setArgName("max-len");
        opts.addOption(sndPduLen);
        Option fileBufSize = new Option("b", "file-buf-size", true,
                "minimal buffer size to write received object to file, 256 by default");
        fileBufSize.setArgName("size");
        opts.addOption(fileBufSize);
        Option maxOpsInvoked = new Option("a", "async", true,
                "maximum number of outstanding operations performed " +
                "asynchronously, 1 (=synchronous) by default.");
        maxOpsInvoked.setArgName("max-ops");
        opts.addOption(maxOpsInvoked);
        opts.addOption("h", "help", false, "print this message");
        opts.addOption("V", "version", false,
                "print the version information and exit");
        CommandLine cl = null;
        try
        {
            cl = new PosixParser().parse(opts, args);
        } catch (ParseException e)
        {
            exit("dcmrcv: " + e.getMessage());
        }
        if (cl.hasOption('V'))
        {
            Package p = DcmRcv.class.getPackage();
            System.out.println("dcmrcv v" + p.getImplementationVersion());
            System.exit(0);
        }
        if (cl.hasOption('h') || cl.getArgList().size() == 0)
        {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(USAGE, DESCRIPTION, opts, EXAMPLE);
            System.exit(0);
        }
        return cl;
    }    

    public static void main(String[] args)
    {
        CommandLine cl = parse(args);
        DcmRcv dcmrcv = new DcmRcv();
        final List argList = cl.getArgList();
        String port = (String) argList.get(0);
        String[] aetPort = split(port, ':', 1);
        dcmrcv.setPort(parseInt(aetPort[1], "illegal port number", 1, 0xffff));
        if (aetPort[0] != null)
        {
            String[] aetHost = split(aetPort[0], '@', 0);
            dcmrcv.setAEtitle(aetHost[0]);
            dcmrcv.setHostname(aetHost[1]);
        }

        if (cl.hasOption("d"))
            dcmrcv.setDestination(cl.getOptionValue("d"));
        if (cl.hasOption("T"))
            dcmrcv.setRequestTimeout(
                    parseInt(cl.getOptionValue("T"),
                    "illegal argument of option -T", 1, Integer.MAX_VALUE));
        if (cl.hasOption("t"))
            dcmrcv.setReleaseTimeout(
                    parseInt(cl.getOptionValue("t"),
                    "illegal argument of option -t", 1, Integer.MAX_VALUE));
        if (cl.hasOption("c"))
            dcmrcv.setSocketCloseDelay(
                    parseInt(cl.getOptionValue("c"),
                    "illegal argument of option -c", 1, 10000));
        if (cl.hasOption("D"))
            dcmrcv.setAssociationReaperPeriod(
                    parseInt(cl.getOptionValue("D"),
                    "illegal argument of option -D", 1, Integer.MAX_VALUE));
        if (cl.hasOption("I"))
            dcmrcv.setIdleTimeout(
                    parseInt(cl.getOptionValue("D"),
                    "illegal argument of option -I", 1, Integer.MAX_VALUE));
        if (cl.hasOption("u"))
            dcmrcv.setMaxPDULengthReceive(
                    parseInt(cl.getOptionValue("u"),
                    "illegal argument of option -u", 256, MEGABYTE));
        if (cl.hasOption("U"))
            dcmrcv.setMaxPDULengthSend(
                    parseInt(cl.getOptionValue("U"),
                    "illegal argument of option -U", 256, MEGABYTE));
        if (cl.hasOption("S"))
            dcmrcv.setSendBufferSize(
                    parseInt(cl.getOptionValue("S"),
                    "illegal argument of option -S", 256, MEGABYTE));
        if (cl.hasOption("s"))
            dcmrcv.setReceiveBufferSize(
                    parseInt(cl.getOptionValue("s"),
                    "illegal argument of option -s", 256, MEGABYTE));
        if (cl.hasOption("b"))
            dcmrcv.setFileBufferSize(
                    parseInt(cl.getOptionValue("b"),
                    "illegal argument of option -b", 256, MEGABYTE));
        dcmrcv.setPackPDV(cl.hasOption("k"));
        dcmrcv.setTcpNoDelay(cl.hasOption("y"));
        if (cl.hasOption("a"))
            dcmrcv.setMaxOpsPerformed(parseInt(cl.getOptionValue("a"),
                    "illegal max-opts", 1, 0xffff));
        dcmrcv.initTransferCapability();
        try
        {
            dcmrcv.start();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void initTransferCapability()
    {
        TransferCapability[] tc = new TransferCapability[CUIDS.length+1];
        tc[0] = new TransferCapability(UID.VerificationSOPClass, ONLY_DEF_TS, true);
        for (int i = 0; i < CUIDS.length; i++)
            tc[i+1] = new TransferCapability(CUIDS[i], tsuids, true);
        ae.setTransferCapability(tc);        
    }

    private void setFileBufferSize(int size)
    {
        fileBufferSize = size;        
    }

    private void setMaxOpsPerformed(int maxOps)
    {
        ae.setMaxOpsPerformed(maxOps);        
    }

    private void setDestination(String filePath)
    {
        this.destination = new File(filePath);
        this.devnull = "/dev/null".equals(filePath);
        if (!devnull)
            destination.mkdir();
    }

    public void start() throws IOException
    {        
        device.startListening();
        System.out.println("Start Server listening on port " + connector.getPort());
    }

    private static String[] split(String s, char delim, int defPos)
    {
        String[] s2 = new String[2];
        s2[defPos] = s;
        int pos = s.indexOf(delim);
        if (pos != -1)
        {
            s2[0] = s.substring(0, pos);
            s2[1] = s.substring(pos + 1);
        }
        return s2;
    }

    private static void exit(String msg)
    {
        System.err.println(msg);
        System.err.println("Try 'dcmrcv -h' for more information.");
        System.exit(1);
    }

    private static int parseInt(String s, String errPrompt, int min, int max) {
        try {
            int i = Integer.parseInt(s);
            if (i >= min && i <= max)
                return i;
        } catch (NumberFormatException e) {}
        exit(errPrompt);
        throw new RuntimeException();
    }
    
    protected void doCStore(Association as, int pcid, DicomObject rq,
            PDVInputStream dataStream, String tsuid, DicomObject rsp)
    {
        if (destination == null)
        {
            super.doCStore(as, pcid, rq, dataStream, tsuid, rsp);
        }
        else
        {
            try
            {
                String cuid = rq.getString(Tag.AffectedSOPClassUID);
                String iuid = rq.getString(Tag.AffectedSOPInstanceUID);
                BasicDicomObject fmi = new BasicDicomObject();
                fmi.initFileMetaInformation(cuid, iuid, tsuid);
                File file = devnull ? destination : new File(destination, iuid);
                FileOutputStream fos = new FileOutputStream(file);
                BufferedOutputStream bos = new BufferedOutputStream(fos, fileBufferSize);
                DicomOutputStream dos = new DicomOutputStream(bos);
                dos.writeFileMetaInformation(fmi);
                dataStream.copyTo(dos);
                dos.close();
            }
            catch (IOException e)
            {
                rsp.putInt(Tag.Status, VR.US, 0x110);
                rsp.putString(Tag.ErrorComment, VR.LO, e.getMessage());
            }
        }
    }
        
}
