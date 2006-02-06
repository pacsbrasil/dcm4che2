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
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;
import org.dcm4che2.data.VR;
import org.dcm4che2.io.DicomOutputStream;
import org.dcm4che2.net.Executor;
import org.dcm4che2.net.NetworkApplicationEntity;
import org.dcm4che2.net.Association;
import org.dcm4che2.net.NetworkConnection;
import org.dcm4che2.net.Device;
import org.dcm4che2.net.NewThreadExecutor;
import org.dcm4che2.net.PDVInputStream;
import org.dcm4che2.net.TransferCapability;
import org.dcm4che2.net.service.StorageService;
import org.dcm4che2.net.service.VerificationService;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision$ $Date$
 * @since Oct 13, 2005
 */
public class DcmRcv extends StorageService {

    private static final int KB = 1024;
    private static final int MB = KB * KB;
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

    
    private Executor executor = new NewThreadExecutor("DCMRCV");
    private Device device = new Device("DCMRCV");
    private NetworkApplicationEntity ae = new NetworkApplicationEntity();
    private NetworkConnection nc = new NetworkConnection();
    private String[] tsuids = NON_RETIRED_TS;
    private File destination;
    private boolean devnull;
    private int fileBufferSize = 256;
    private int rspdelay = 0;
    
    public DcmRcv()
    {
        super(CUIDS);
        device.setNetworkApplicationEntity(ae);
        device.setNetworkConnection(nc);
        ae.setNetworkConnection(nc);
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
        nc.setHostname(hostname);
    }
    
    public final void setPort(int port)
    {
        nc.setPort(port);
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
        nc.setTcpNoDelay(tcpNoDelay);
    }

    public final void setRequestTimeout(int timeout)
    {
        nc.setRequestTimeout(timeout);
    }

    public final void setReleaseTimeout(int timeout)
    {
        nc.setReleaseTimeout(timeout);
    }

    public final void setSocketCloseDelay(int delay)
    {
        nc.setSocketCloseDelay(delay);
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
        nc.setReceiveBufferSize(bufferSize);
    }

    public final void setSendBufferSize(int bufferSize)
    {
        nc.setSendBufferSize(bufferSize);
    }

    private void setDimseRspDelay(int delay)
    {
        rspdelay = delay;        
    }


    private static CommandLine parse(String[] args)
    {
        Options opts = new Options();
        Option dest = new Option("d", "dest", true,
                "store received objects into files in specified directory <dir>." +
                " Do not store received objects by default.");
        dest.setArgName("dir");
        opts.addOption(dest);
        OptionGroup ts = new OptionGroup();
        ts.addOption(new Option(" ", "def-ts", false,
                "accept only default transfer syntax."));
        ts.addOption(new Option(" ", "unc-ts", false,
                "accept only transfer syntax with uncompressed pixel data"));
        ts.addOption(new Option(" ", "unc-le-ts", false,
                "accept only transfer syntax with uncompressed pixel data and" +
                "Little Endian byte order."));
        opts.addOptionGroup(ts);
        opts.addOption(" ", "pack-pdv", false, 
                "pack command and data PDV in one P-DATA-TF PDU, " +
                "send only one PDV in one P-Data-TF PDU by default.");
        opts.addOption(" ", "tcp-no-delay", false, 
                "set TCP_NODELAY socket option to true, false by default");
        Option closeDelay = new Option("c", "close-delay", true,
                "delay in ms for Socket close after sending A-ABORT, 50ms by default");
        closeDelay.setArgName("delay");
        opts.addOption(closeDelay);
        Option rspDelay = new Option(" ", "rsp-delay", true,
                "delay in ms for DIMSE-RSP; useful for testing asynchronous mode");
        rspDelay.setArgName("delay");
        opts.addOption(rspDelay);
        Option acTimeout = new Option(" ", "request-timeout", true,
                "timeout in ms for receiving A-ASSOCIATE-RQ, 5s by default");
        acTimeout.setArgName("timeout");
        opts.addOption(acTimeout);
        Option rpTimeout = new Option(" ", "release-timeout", true,
                "timeout in ms for receiving A-RELEASE-RP, 5s by default");
        rpTimeout.setArgName("timeout");
        opts.addOption(rpTimeout);
        Option checkPeriod = new Option(" ", "reaper-period", true,
                "period in ms to check idleness, 10s by default");
        checkPeriod.setArgName("period");
        Option idleTimeout = new Option(" ", "idle-timeout", true,
                "timeout in ms for receiving DIMSE-RQ, 60s by default");
        idleTimeout.setArgName("timeout");
        opts.addOption(idleTimeout);
        Option soRcvBufSize = new Option(" ", "so-rcv-buf-size", true,
                "set SO_RCVBUF socket option to specified value");
        soRcvBufSize.setArgName("size");
        opts.addOption(soRcvBufSize);
        Option soSndBufSize = new Option(" ", "so-snd-buf-size", true,
                "set SO_SNDBUF socket option to specified value");
        soSndBufSize.setArgName("size");
        opts.addOption(soSndBufSize);
        Option rcvPduLen = new Option(" ", "rcv-pdu-len", true,
                "maximal length of received P-DATA-TF PDUs, 16384 by default");
        rcvPduLen.setArgName("max-len");
        opts.addOption(rcvPduLen);
        Option sndPduLen = new Option(" ", "snd-pdu-len", true,
                "maximal length of sent P-DATA-TF PDUs, 16384 by default");
        sndPduLen.setArgName("max-len");
        opts.addOption(sndPduLen);
        Option fileBufSize = new Option(" ", "buf-size", true,
                "minimal buffer size to write received object to file, 256 by default");
        fileBufSize.setArgName("size");
        opts.addOption(fileBufSize);
        Option maxOpsInvoked = new Option("a", "async", true,
                "maximum number of outstanding operations performed " +
                "asynchronously, unlimited by default.");
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
        if (cl.hasOption("def-ts"))
            dcmrcv.setTransferSyntax(ONLY_DEF_TS);
        else if (cl.hasOption("unc-ts"))
            dcmrcv.setTransferSyntax(NATIVE_TS);
        else if (cl.hasOption("unc-le-ts"))
            dcmrcv.setTransferSyntax(NATIVE_LE_TS);
        if (cl.hasOption("reaper-period"))
            dcmrcv.setAssociationReaperPeriod(
                    parseInt(cl.getOptionValue("reaper-period"),
                    "illegal argument of option --reaper-period", 1, Integer.MAX_VALUE));
        if (cl.hasOption("idle-timeout"))
            dcmrcv.setIdleTimeout(
                    parseInt(cl.getOptionValue("idle-timeout"),
                    "illegal argument of option --idle-timeout", 1, Integer.MAX_VALUE));
        if (cl.hasOption("accept-timeout"))
            dcmrcv.setRequestTimeout(
                    parseInt(cl.getOptionValue("request-timeout"),
                    "illegal argument of option --request-timeout", 1, Integer.MAX_VALUE));
        if (cl.hasOption("release-timeout"))
            dcmrcv.setReleaseTimeout(
                    parseInt(cl.getOptionValue("release-timeout"),
                    "illegal argument of option --release-timeout", 1, Integer.MAX_VALUE));
        if (cl.hasOption("close-delay"))
            dcmrcv.setSocketCloseDelay(
                    parseInt(cl.getOptionValue("close-delay"),
                    "illegal argument of option --close-delay", 1, 10000));
        if (cl.hasOption("rsp-timeout"))
            dcmrcv.setDimseRspTimeout(
                    parseInt(cl.getOptionValue("rsp-timeout"),
                    "illegal argument of option --rsp-timeout", 1, Integer.MAX_VALUE));
        if (cl.hasOption("rsp-delay"))
            dcmrcv.setDimseRspDelay(
                    parseInt(cl.getOptionValue("rsp-delay"),
                    "illegal argument of option --rsp-delay", 0, 10000));
        if (cl.hasOption("rcv-pdu-len"))
            dcmrcv.setMaxPDULengthReceive(
                    parseInt(cl.getOptionValue("rcv-pdu-len"),
                    "illegal argument of option --rcv-pdu-len", 1, 10000) * KB);
        if (cl.hasOption("snd-pdu-len"))
            dcmrcv.setMaxPDULengthSend(
                    parseInt(cl.getOptionValue("snd-pdu-len"),
                    "illegal argument of option --snd-pdu-len", 1, 10000) * KB);
        if (cl.hasOption("so-snd-buf"))
            dcmrcv.setSendBufferSize(
                    parseInt(cl.getOptionValue("so-snd-buf"),
                    "illegal argument of option --so-snd-buf", 1, 10000) * KB);
        if (cl.hasOption("so-rcv-buf"))
            dcmrcv.setReceiveBufferSize(
                    parseInt(cl.getOptionValue("so-rcv-buf"),
                    "illegal argument of option --so-rcv-buf", 1, 10000) * KB);
        if (cl.hasOption("buf-size"))
            dcmrcv.setFileBufferSize(
                    parseInt(cl.getOptionValue("buf-size"),
                    "illegal argument of option --buf-size", 256, MB));
        dcmrcv.setPackPDV(cl.hasOption("pack-pdv"));
        dcmrcv.setTcpNoDelay(cl.hasOption("tcp-no-delay"));
        if (cl.hasOption("a"))
            dcmrcv.setMaxOpsPerformed(zeroAsMaxInt(parseInt(
                    cl.getOptionValue("a"), "illegal max-opts", 0, 0xffff)));
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

    private void setTransferSyntax(String[] tsuids)
    {
        this.tsuids = tsuids;        
    }

    private static int zeroAsMaxInt(int val)
    {
        return val > 0 ? val : Integer.MAX_VALUE;
    }
    
    private void initTransferCapability()
    {
        TransferCapability[] tc = new TransferCapability[CUIDS.length+1];
        tc[0] = new TransferCapability(UID.VerificationSOPClass, ONLY_DEF_TS,
                TransferCapability.SCP);
        for (int i = 0; i < CUIDS.length; i++)
            tc[i+1] = new TransferCapability(CUIDS[i], tsuids, TransferCapability.SCP);
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
        device.startListening(executor );
        System.out.println("Start Server listening on port " + nc.getPort());
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
        if (rspdelay > 0)
            try
            {
                Thread.sleep(rspdelay);
            }
            catch (InterruptedException e) {}
    }
        
}
