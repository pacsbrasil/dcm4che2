/*$Id$*/
/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2002 by TIANI MEDGRAPH AG                                  *
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

import org.dcm4che.net.Association;
import org.dcm4che.net.AAssociateRQ;
import org.dcm4che.net.AAssociateAC;
import org.dcm4che.net.AssociationFactory;
import org.dcm4che.net.Dimse;
import org.dcm4che.net.PDU;
import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.DcmParserFactory;
import org.dcm4che.data.DcmParser;
import org.dcm4che.dict.UIDs;
import org.dcm4che.util.DcmURL;

import java.io.*;
import java.net.*;
import java.util.*;
import gnu.getopt.*;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 */
public class DcmSnd {

    private static final AssociationFactory assocFact = 
            AssociationFactory.getInstance();
    private static final DcmObjectFactory dataFact = 
            DcmObjectFactory.getInstance();
    private static final DcmParserFactory pFact = 
            DcmParserFactory.getInstance();
    
    private static final String[] IMAGE_STORE_AS = {
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
        UIDs.NuclearMedicineImageStorageRetired,
        UIDs.UltrasoundImageStorageRetired,
        UIDs.UltrasoundImageStorage,
        UIDs.SecondaryCaptureImageStorage,
        UIDs.XRayAngiographicImageStorage,
        UIDs.XRayRadiofluoroscopicImageStorage,
        UIDs.XRayAngiographicBiPlaneImageStorageRetired,
        UIDs.NuclearMedicineImageStorage,
        UIDs.VLImageStorageRetired,
        UIDs.VLMultiframeImageStorageRetired,
        UIDs.VLEndoscopicImageStorage,
        UIDs.VLMicroscopicImageStorage,
        UIDs.VLSlideCoordinatesMicroscopicImageStorage,
        UIDs.VLPhotographicImageStorage,
        UIDs.PositronEmissionTomographyImageStorage,
        UIDs.RTImageStorage,
        UIDs.HardcopyGrayscaleImageStorage,
        UIDs.HardcopyColorImageStorage,
    };

    private static final String[] NON_IMAGE_STORE_AS = {
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
        UIDs.BasicTextSR,
        UIDs.EnhancedSR,
        UIDs.ComprehensiveSR,
        UIDs.KeyObjectSelectionDocument,
        UIDs.StandalonePETCurveStorage,
        UIDs.RTDoseStorage,
        UIDs.RTStructureSetStorage,
        UIDs.RTBeamsTreatmentRecordStorage,
        UIDs.RTPlanStorage,
        UIDs.RTTreatmentSummaryRecordStorage,
        UIDs.BasicStudyContentNotification,
        UIDs.StoredPrintStorage,
    };

    private static final String[] DEF_TS = {
        UIDs.ImplicitVRLittleEndian,
    };

    private static final String[] NATIVE_TS = {
        UIDs.ExplicitVRLittleEndian,
        UIDs.ImplicitVRLittleEndian,
    };
    
    private static final String[] JPEG_LL_TS = {
        UIDs.JPEGLossless,
    };
    
    private static final String[] JPEG_BL_TS = {
        UIDs.JPEGBaseline,
    };
    
    private DcmURL url = null;
    private int repeatSingle = 1;
    private int repeatWhole = 1;
    private int priority = Command.MEDIUM;
    private int assocTO = 0;
    private int dimseTO = 0;
    private int releaseTO = 0;
    private AAssociateRQ assocRQ = assocFact.newAAssociateRQ();

    /** Creates a new instance of DcmDir */
    public DcmSnd() {
    }
    
    public void setUrl(DcmURL url) {
        this.url = url;
        assocRQ.setCallingAET(url.getCallingAET());
        assocRQ.setCalledAET(url.getCalledAET());
    }        
    
    public void echo() throws IOException {
        final int pcid = 1;
        assocRQ.addPresContext(
            assocFact.newPresContext(pcid, UIDs.Verification, DEF_TS));
        long t1 = System.currentTimeMillis();
        int count = 0;
        for (int i = 0; i < repeatWhole; ++i) {
            Association assoc = assocFact.newRequestor(
                    new Socket(url.getHost(), url.getPort()), null);
            PDU assocAC = assoc.connect(assocRQ, assocTO);
            if (assocAC instanceof AAssociateAC) {
                if (assoc.getAcceptedTransferSyntaxUID(pcid) == null) {
                    System.out.println("Presentation Context for Verfication"
                          + " was not accepted by " + assocRQ.getCalledAET());
                }
                else for (int j = 0; j < repeatSingle; ++j, ++count) {
                    assoc.write(assocFact.newDimse(pcid,
                            dataFact.newCommand().initCEchoRQ(j)));
                    Dimse rsp = assoc.read(dimseTO);
                }
                PDU releaseRP = assoc.release(releaseTO);
            }
        }
        long dt = System.currentTimeMillis() - t1;
        System.out.println("" + count + " Verification(s) performed in " 
                              + dt + "ms.");
    }

    public void send(String[] args, int offset) throws IOException {
        int pcid = 1;
        for (int i = 0; i < IMAGE_STORE_AS.length; ++i) {
            assocRQ.addPresContext(assocFact.newPresContext(
                    pcid, IMAGE_STORE_AS[i], NATIVE_TS));
            ++pcid;
            ++pcid;
            assocRQ.addPresContext(assocFact.newPresContext(
                    pcid, IMAGE_STORE_AS[i], JPEG_LL_TS));
            ++pcid;
            ++pcid;
            assocRQ.addPresContext(assocFact.newPresContext(
                    pcid, IMAGE_STORE_AS[i], JPEG_BL_TS));
            ++pcid;
            ++pcid;
        }
        for (int i = 0; i < NON_IMAGE_STORE_AS.length; ++i) {
            assocRQ.addPresContext(assocFact.newPresContext(
                    pcid, NON_IMAGE_STORE_AS[i], NATIVE_TS));
            ++pcid;
            ++pcid;
        }

        long t1 = System.currentTimeMillis();
        Result res = new Result();
        for (int i = 0; i < repeatWhole; ++i) {
            Association assoc = assocFact.newRequestor(
                    new Socket(url.getHost(), url.getPort()), null);
            PDU assocAC = assoc.connect(assocRQ, assocTO);
            if (assocAC instanceof AAssociateAC) {
                for (int k = offset; k < args.length; ++k) {
                    send(assoc, new File(args[k]), res);
                }
                PDU releaseRP = assoc.release(releaseTO);
            }
        }
        long dt = System.currentTimeMillis() - t1;
        System.out.println();
        System.out.println("" + res.sentCount + " file(s) ("
            + res.sentBytes + " bytes) sent in " + dt + "ms (" 
            + (res.sentBytes/(1.024f*dt)) + " KB/s)");
        System.out.println("Wait " + res.wait4Rsp + "ms ("
            + (res.wait4Rsp * 100/ dt) + "%, ~"
            + (((float)res.wait4Rsp)/res.sentCount)
            + "ms per file) for DIMSE RSP");
    }
    
    private class Result {
        int sentCount;
        long sentBytes;
        long wait4Rsp;
    }
        
    private void send(Association assoc, File file, Result res)
            throws IOException {
        if (!file.isDirectory()) {
            for (int i = 0; i < repeatSingle; ++i) {                
                send(assoc, file, res);
            }
            return;
        }
        File[] list = file.listFiles();
        for (int i = 0; i < list.length; ++i) {
            send(assoc, list[i], res);
        }
    }
    
    private void sendFile(Association assoc, File file, Result res)
            throws IOException {        
    }

   /**
    * @param args the command line arguments
    */
    public static void main (String args[]) throws Exception {
        LongOpt[] longopts = new LongOpt[0];
        Getopt g = new Getopt("dcmsnd.jar", args, "L:P:r:R:", longopts, true);
        
        DcmSnd dcmsnd = new DcmSnd();
        int c;
        while ((c = g.getopt()) != -1) {
            switch (c) {                
                case 'L':
                    dcmsnd.assocRQ.setMaxLength(Integer.parseInt(g.getOptarg()));
                    break;
                case 'r':
                    dcmsnd.repeatSingle = Integer.parseInt(g.getOptarg());
                    break;
                case 'R':
                    dcmsnd.repeatWhole = Integer.parseInt(g.getOptarg());
                    break;
                case 'P':
                    dcmsnd.priority = Integer.parseInt(g.getOptarg());
                    if (dcmsnd.priority > 2) {
                        exit("Illegal priority - " + g.getOptarg());
                    }
                    break;
                case '?':
                    exit("");
                    break;
                }
        }
        int optind = g.getOptind();
        int argc = args.length - optind;
        if (argc == 0) {
            exit("missing argument");
        }
        
        try {
            dcmsnd.setUrl(new DcmURL(args[optind]));
        }
        catch (Exception ex) {
            exit("misformed DICOM URL: " + args[optind]);
        }

        if (argc == 1) {
            dcmsnd.echo();
        }
        else {
            dcmsnd.send(args, optind+1);
        }
    }
    
    private static void exit(String prompt) {
        System.err.println(prompt);
        System.err.println(USAGE);
        System.exit(1);
    }

    private static final String USAGE = 
"Usage: java -jar dcmsnd.jar [options] dicom-url\n" +
"            (to verify DICOM connection)\n" +
"   or  java -jar dcmsnd.jar [options] dicom-url source...\n" +
"            (to send DICOM instances in specified files/directories)\n" +
"dicom-url:\n" +
"  dicom://calledAET:callingAET@host:port\n" +
"    calledAET  Called AET in association request,\n" +
"    callingAET Calling AET in association request,\n" +
"    host       Name or IP address of host, where the server is running,\n" +
"    port       TCP port address, on which the server is listing for\n" +
"               incoming TCP Transport Connection Indication,\n" +
"\n" +
"Options:\n" +
" -L maxLength  Set maximal length of receiving PDUs [default=16352],\n" +
" -P priority   Set priority used in storage requests:\n" +
"                 0 - normal [=default]\n" +
"                 1 - high\n" +
"                 2 - low\n" +
" -r repeat     Set number of times to repeat single DIMSE requests [default=1]\n" +
" -R repeat     Set number of times to repeat whole operation [default=1],\n" +
"\n" +
"Example:\n" +
"java -jar dcmsnd.jar dicom://DCMRCV:DCMSND@localhost:2350 /cdrom/DICOM/\n" +
"=> Opens association to local server, listening on port 2350, with\n" +
"   Calling AET = DCMSND and Called AET = DCMRCV; and sends DICOM instances\n" +
"   in DICOM files in directory (and sub directories of) /cdrom/DICOM/\n";
}
