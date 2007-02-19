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
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;
import org.dcm4che2.io.DicomOutputStream;
import org.dcm4che2.net.Association;
import org.dcm4che2.net.Device;
import org.dcm4che2.net.DicomServiceException;
import org.dcm4che2.net.Executor;
import org.dcm4che2.net.NetworkApplicationEntity;
import org.dcm4che2.net.NetworkConnection;
import org.dcm4che2.net.NewThreadExecutor;
import org.dcm4che2.net.PDVInputStream;
import org.dcm4che2.net.Status;
import org.dcm4che2.net.TransferCapability;
import org.dcm4che2.net.service.StorageService;
import org.dcm4che2.net.service.VerificationService;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision$ $Date$
 * @since Oct 13, 2005
 */
public class DcmRcv extends StorageService {

    private static final String DEST = "dest";
    private static final String DEF_TS = "defts";
    private static final String NATIVE = "native";
    private static final String BIG_ENDIAN = "bigendian";
    private static final String BUFSIZE = "bufsize";
    private static final String PDV1 = "pdv1";
    private static final String ASYNC = "async";
    private static final String REQUEST_TO = "requestTO";
    private static final String RELEASE_TO = "releaseTO";
    private static final String SO_CLOSEDELAY = "soclosedelay";
    private static final String TCP_DELAY = "tcpdelay";
    private static final String SO_RCVBUF = "sorcvbuf";
    private static final String SO_SNDBUF = "sosndbuf";
    private static final String SND_PDULEN = "sndpdulen";
    private static final String RCV_PDULEN = "rcvpdulen";
    private static final String RSP_DELAY = "rspdelay";
    private static final String IDLE_TO = "idleTO";
    private static final String REAPER = "reaper";
    private static final String VERSION = "V";
    private static final String HELP = "h";
    
    private static final int KB = 1024;
    private static final String USAGE = 
        "dcmrcv [Options] [<aet>[@<ip>]:]<port>";
    private static final String DESCRIPTION = 
        "DICOM Server listening on specified <port> for incoming association " +
        "requests. If no local IP address of the network interface is specified " +
        "connections on any/all local addresses are accepted. If <aet> is " +
        "specified, only requests with matching called AE title will be " +
        "accepted.\n" +
        "Options:";
    private static final String EXAMPLE = 
        "\nExample: dcmrcv DCMRCV:11112 -dest /tmp \n" +
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

    private static final String[] NON_RETIRED_LE_TS =
    {
        UID.JPEGLSLossless,
        UID.JPEGLossless,
        UID.JPEGLosslessNonHierarchical14,
        UID.JPEG2000LosslessOnly,
        UID.DeflatedExplicitVRLittleEndian,
        UID.RLELossless,
        UID.ExplicitVRLittleEndian,
        UID.ImplicitVRLittleEndian,
        UID.JPEGBaseline1,
        UID.JPEGExtended24,
        UID.JPEGLSLossyNearLossless,
        UID.JPEG2000,
        UID.MPEG2,
    };
    
    private static final String[] CUIDS =
    {
        UID.BasicStudyContentNotificationSOPClassRetired,
        UID.StoredPrintStorageSOPClassRetired,
        UID.HardcopyGrayscaleImageStorageSOPClassRetired,
        UID.HardcopyColorImageStorageSOPClassRetired,
        UID.ComputedRadiographyImageStorage,
        UID.DigitalXRayImageStorageForPresentation,
        UID.DigitalXRayImageStorageForProcessing,
        UID.DigitalMammographyXRayImageStorageForPresentation,
        UID.DigitalMammographyXRayImageStorageForProcessing,
        UID.DigitalIntraoralXRayImageStorageForPresentation,
        UID.DigitalIntraoralXRayImageStorageForProcessing,
        UID.StandaloneModalityLUTStorageRetired,
        UID.EncapsulatedPDFStorage,
        UID.StandaloneVOILUTStorageRetired,
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
        UID.StandalonePETCurveStorageRetired,
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
        UID.StandaloneOverlayStorageRetired,
        UID.BasicTextSR,
        UID.EnhancedSR,
        UID.ComprehensiveSR,
        UID.ProcedureLogStorage,
        UID.MammographyCADSR,
        UID.KeyObjectSelectionDocument,
        UID.ChestCADSR ,
        UID.StandaloneCurveStorageRetired,
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
    private String[] tsuids = NON_RETIRED_LE_TS;
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
        OptionBuilder.withArgName("dir");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "store received objects into files in specified directory <dir>." +
                " Do not store received objects by default.");
        opts.addOption(OptionBuilder.create(DEST));
        
        opts.addOption(DEF_TS, false, 
                "accept only default transfer syntax.");
        opts.addOption(BIG_ENDIAN, false, 
                "accept also Explict VR Big Endian transfer syntax.");
        opts.addOption(NATIVE, false, 
                "accept only transfer syntax with uncompressed pixel data.");

        OptionBuilder.withArgName("maxops");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "maximum number of outstanding operations performed " +
                "asynchronously, unlimited by default.");
        opts.addOption(OptionBuilder.create(ASYNC));
                
        opts.addOption(PDV1, false, 
                "send only one PDV in one P-Data-TF PDU, " +
                "pack command and data PDV in one P-DATA-TF PDU by default.");
        opts.addOption(TCP_DELAY, false, 
                "set TCP_NODELAY socket option to false, true by default");
        
        OptionBuilder.withArgName("ms");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "delay in ms for Socket close after sending A-ABORT, 50ms by default");
        opts.addOption(OptionBuilder.create(SO_CLOSEDELAY));
        
        OptionBuilder.withArgName("ms");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "delay in ms for DIMSE-RSP; useful for testing asynchronous mode");
        opts.addOption(OptionBuilder.create(RSP_DELAY));
        
        OptionBuilder.withArgName("ms");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "timeout in ms for receiving -ASSOCIATE-RQ, 5s by default");
        opts.addOption(OptionBuilder.create(REQUEST_TO));

        OptionBuilder.withArgName("ms");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "timeout in ms for receiving A-RELEASE-RP, 5s by default");
        opts.addOption(OptionBuilder.create(RELEASE_TO));

        OptionBuilder.withArgName("ms");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "period in ms to check for outstanding DIMSE-RSP, 10s by default");
        opts.addOption(OptionBuilder.create(REAPER));
        
        OptionBuilder.withArgName("ms");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "timeout in ms for receiving DIMSE-RQ, 60s by default");
        opts.addOption(OptionBuilder.create(IDLE_TO));
        
        OptionBuilder.withArgName("KB");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "maximal length in KB of received P-DATA-TF PDUs, 16KB by default");
        opts.addOption(OptionBuilder.create(RCV_PDULEN));
        
        OptionBuilder.withArgName("KB");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "maximal length in KB of sent P-DATA-TF PDUs, 16KB by default");
        opts.addOption(OptionBuilder.create(SND_PDULEN));
        
        OptionBuilder.withArgName("KB");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "set SO_RCVBUF socket option to specified value in KB");
        opts.addOption(OptionBuilder.create(SO_RCVBUF));
        
        OptionBuilder.withArgName("KB");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "set SO_SNDBUF socket option to specified value in KB");
        opts.addOption(OptionBuilder.create(SO_SNDBUF));
        
        OptionBuilder.withArgName("KB");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "minimal buffer size to write received object to file, 1KB by default");
        opts.addOption(OptionBuilder.create(BUFSIZE));
        
        opts.addOption(HELP, "help", false, "print this message");
        opts.addOption(VERSION, "version", false,
                "print the version information and exit");
        CommandLine cl = null;
        try
        {
            cl = new GnuParser().parse(opts, args);
        } catch (ParseException e)
        {
            exit("dcmrcv: " + e.getMessage());
        }
        if (cl.hasOption(VERSION))
        {
            Package p = DcmRcv.class.getPackage();
            System.out.println("dcmrcv v" + p.getImplementationVersion());
            System.exit(0);
        }
        if (cl.hasOption(HELP) || cl.getArgList().size() == 0)
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

        if (cl.hasOption(DEST))
            dcmrcv.setDestination(cl.getOptionValue(DEST));
        if (cl.hasOption(DEF_TS))
            dcmrcv.setTransferSyntax(ONLY_DEF_TS);
        else if (cl.hasOption(NATIVE))
            dcmrcv.setTransferSyntax(cl.hasOption(BIG_ENDIAN) ? NATIVE_TS : NATIVE_LE_TS);
        else if (cl.hasOption(BIG_ENDIAN))
            dcmrcv.setTransferSyntax(NON_RETIRED_TS);
        if (cl.hasOption(REAPER))
            dcmrcv.setAssociationReaperPeriod(
                    parseInt(cl.getOptionValue(REAPER),
                    "illegal argument of option -reaper", 1, Integer.MAX_VALUE));
        if (cl.hasOption(IDLE_TO))
            dcmrcv.setIdleTimeout(
                    parseInt(cl.getOptionValue(IDLE_TO),
                    "illegal argument of option -idleTO", 1, Integer.MAX_VALUE));        
        if (cl.hasOption(REQUEST_TO))
            dcmrcv.setRequestTimeout(
                    parseInt(cl.getOptionValue(REQUEST_TO),
                    "illegal argument of option -requestTO", 1, Integer.MAX_VALUE));
        if (cl.hasOption(RELEASE_TO))
            dcmrcv.setReleaseTimeout(
                    parseInt(cl.getOptionValue(RELEASE_TO),
                    "illegal argument of option -releaseTO", 1, Integer.MAX_VALUE));
        if (cl.hasOption(SO_CLOSEDELAY))
            dcmrcv.setSocketCloseDelay(
                    parseInt(cl.getOptionValue(SO_CLOSEDELAY),
                    "illegal argument of option -soclosedelay", 1, 10000));
        if (cl.hasOption(RSP_DELAY))
            dcmrcv.setDimseRspDelay(
                    parseInt(cl.getOptionValue(RSP_DELAY),
                    "illegal argument of option -rspdelay", 0, 10000));
        if (cl.hasOption(RCV_PDULEN))
            dcmrcv.setMaxPDULengthReceive(
                    parseInt(cl.getOptionValue(RCV_PDULEN),
                    "illegal argument of option -rcvpdulen", 1, 10000) * KB);
        if (cl.hasOption(SND_PDULEN))
            dcmrcv.setMaxPDULengthSend(
                    parseInt(cl.getOptionValue(SND_PDULEN),
                    "illegal argument of option -sndpdulen", 1, 10000) * KB);
        if (cl.hasOption(SO_SNDBUF))
            dcmrcv.setSendBufferSize(
                    parseInt(cl.getOptionValue(SO_SNDBUF),
                    "illegal argument of option -sosndbuf", 1, 10000) * KB);
        if (cl.hasOption(SO_RCVBUF))
            dcmrcv.setReceiveBufferSize(
                    parseInt(cl.getOptionValue(SO_RCVBUF),
                    "illegal argument of option -sorcvbuf", 1, 10000) * KB);
        if (cl.hasOption(BUFSIZE))
            dcmrcv.setFileBufferSize(
                    parseInt(cl.getOptionValue(BUFSIZE),
                    "illegal argument of option -bufsize", 1, 10000) * KB);
        
        dcmrcv.setPackPDV(!cl.hasOption(PDV1));
        dcmrcv.setTcpNoDelay(!cl.hasOption(TCP_DELAY));
        if (cl.hasOption(ASYNC))
            dcmrcv.setMaxOpsPerformed(parseInt(cl.getOptionValue(ASYNC), 
                    "illegal argument of option -async", 0, 0xffff));
        dcmrcv.initTransferCapability();
        try
        {
            dcmrcv.start();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void setTransferSyntax(String[] tsuids)
    {
        this.tsuids = tsuids;        
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
            throws IOException, DicomServiceException
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
                throw new DicomServiceException(rq, Status.ProcessingFailure, 
                        e.getMessage());
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
