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

import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.FileMetaInfo;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4che.net.Factory;
import org.dcm4che.net.AcceptorPolicy;
import org.dcm4che.net.ActiveAssociation;
import org.dcm4che.net.DcmServiceBase;
import org.dcm4che.net.DcmServiceRegistry;
import org.dcm4che.net.Dimse;
import org.dcm4che.server.DcmHandler;
import org.dcm4che.server.Server;
import org.dcm4che.server.ServerFactory;
import org.dcm4che.util.SSLContextAdapter;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.PropertyResourceBundle;
import java.util.Date;
import java.util.StringTokenizer;
import java.security.GeneralSecurityException;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

/**
 * <description>
 *
 * @see <related>
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @version $Revision$ $Date$
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>yyyymmdd author:</b>
 * <ul>
 * <li> explicit fix description (no line numbers but methods) go
 *            beyond the cvs commit message
 * </ul>
 */
public class DcmRcv extends DcmServiceBase {
   // Constants -----------------------------------------------------
   private static final int SUCCESS = 0x0000;

   private static final String[] IMAGE_SOP_CLASS_UIDs = {
      UIDs.HardcopyGrayscaleImageStorage,
      UIDs.HardcopyColorImageStorage,
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
   };

   private static final String[] OTHER_STORAGE_SOP_CLASS_UIDs = {
      UIDs.BasicStudyContentNotification,
      UIDs.StoredPrintStorage,
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
      UIDs.RawDataStorage,
      UIDs.BasicTextSR,
      UIDs.EnhancedSR,
      UIDs.ComprehensiveSR,
      UIDs.MammographyCADSR,
      UIDs.KeyObjectSelectionDocument,
      UIDs.StandalonePETCurveStorage,
      UIDs.RTDoseStorage,
      UIDs.RTStructureSetStorage,
      UIDs.RTBeamsTreatmentRecordStorage,
      UIDs.RTTreatmentSummaryRecordStorage,
   };

   private static final String[] NATIVE_TS_UIDs = {
      UIDs.ExplicitVRLittleEndian,
      UIDs.ExplicitVRBigEndian,
      UIDs.ImplicitVRLittleEndian,
   };
   
   private static final String[] SUPPORTED_TS_UIDs = {
      UIDs.JPEGBaseline,
      UIDs.JPEGExtended,
      UIDs.JPEGLossless14,
      UIDs.JPEGLossless,
      UIDs.JPEGLSLossless,
      UIDs.JPEGLSLossy,
      UIDs.JPEG2000Lossless,
      UIDs.JPEG2000Lossy,
      UIDs.RLELossless,
      UIDs.ExplicitVRLittleEndian,
      UIDs.ExplicitVRBigEndian,
      UIDs.ImplicitVRLittleEndian,
   };
   
   // Attributes ----------------------------------------------------
   private static ResourceBundle messages = PropertyResourceBundle.getBundle(
      "resources/DcmRcv", Locale.getDefault());
   
   private static ServerFactory srvFact = ServerFactory.getInstance();
   private static Factory fact = Factory.getInstance();
   
   private SSLContextAdapter tls = null;
   private AcceptorPolicy policy = fact.newAcceptorPolicy();
   private DcmServiceRegistry services = fact.newDcmServiceRegistry();
   private DcmHandler handler = srvFact.newDcmHandler(policy, services);
   private Server server = srvFact.newServer(handler);
   private int port = 104;
   private int bufferSize = 2048;
   private byte[] buffer = null;
   private File dir = null;
   
   
   // Static --------------------------------------------------------
   public static void main(String args[]) throws Exception {
      LongOpt[] longopts = new LongOpt[] {
         new LongOpt("tls", LongOpt.NO_ARGUMENT, null, 's'),
         new LongOpt("buf-len", LongOpt.REQUIRED_ARGUMENT, null, 'b'),
         new LongOpt("max-pdu-len", LongOpt.REQUIRED_ARGUMENT, null, 'L'),
         new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h'),
         new LongOpt("version", LongOpt.NO_ARGUMENT, null, 'v'),
      };
      Getopt g = new Getopt("dcmrcv", args, "", longopts, true);
      
      DcmRcv dcmrcv = new DcmRcv();
      dcmrcv.policy.setAsyncOpsWindow(fact.newAsyncOpsWindow(0,0));
      int c;
      while ((c = g.getopt()) != -1) {
         switch (c) {
            case 's':
              dcmrcv.initTLS();
              break;
            case 'L':
              dcmrcv.policy.setReceivedPDUMaxLength(
                  Integer.parseInt(g.getOptarg()));
              break;
            case 'b':
              dcmrcv.bufferSize =
                      Integer.parseInt(g.getOptarg()) & 0xfffffffe;
              break;
            case 'v':
               exit(messages.getString("version"));
            case 'h':
            case '?':
               exit(messages.getString("usage"));
               break;
         }
      }
        int optind = g.getOptind();
        switch(args.length - optind) {
          case 2:
            dcmrcv.dir = new File(args[optind+1]);
            if (!dcmrcv.dir.exists())
               dcmrcv.dir.mkdirs();
            else
               if (!dcmrcv.dir.isDirectory())
                  throw new IllegalArgumentException("" + dcmrcv.dir);
          case 1:
            dcmrcv.port = Integer.parseInt(args[optind]);
            break;
          case 0:
            exit(messages.getString("missing"));
           default:
            exit(messages.getString("many"));
        }

      dcmrcv.enable(IMAGE_SOP_CLASS_UIDs, SUPPORTED_TS_UIDs);
      dcmrcv.enable(OTHER_STORAGE_SOP_CLASS_UIDs, NATIVE_TS_UIDs);
      dcmrcv.start();
   }
       
   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------
   public void enable(String[] sopClassUIDs, String[] tsUIDs)
   {
      for (int i = 0; i < sopClassUIDs.length; ++i) {
         policy.addPresContext(sopClassUIDs[i], tsUIDs);
         services.bind(sopClassUIDs[i], this);
      }
   }

   public void start() throws GeneralSecurityException, IOException
   {
      if (bufferSize > 0) {
         buffer = new byte[bufferSize];
      }
      System.out.println(MessageFormat.format(messages.getString("start"),
         new Object[]{ new Date(), "" + port }));
      if (tls != null) {
         System.out.println(MessageFormat.format(messages.getString("tls"),
            new Object[] { Arrays.asList(tls.getEnabledCipherSuites()) }));
         server.start(port, tls.getServerSocketFactory());
      } else {
         server.start(port);
      }      
   }
      
   // DcmServiceBase overrides --------------------------------------
   protected void doCStore(ActiveAssociation assoc, Dimse rq, Command rspCmd)
   throws IOException
   {
      InputStream in = rq.getDataAsStream();
      try {
         if (dir != null) {
            Command rqCmd = rq.getCommand();
            FileMetaInfo fmi = objFact.newFileMetaInfo(
                  rqCmd.getAffectedSOPClassUID(),
                  rqCmd.getAffectedSOPInstanceUID(),
                  rq.getTransferSyntaxUID());
            OutputStream out = new BufferedOutputStream(
                  new FileOutputStream(
                     new File(dir, rqCmd.getAffectedSOPInstanceUID())));
            try {
               fmi.write(out);
               copy(in, out);
           } catch (IOException ioe) {
              ioe.printStackTrace();
           } finally {
               try { out.close(); } catch (IOException ignore) {}
            }
         }
      } finally {
         in.close();
      }
      rspCmd.setUS(Tags.Status, SUCCESS);
   }
         
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
    private static void exit(String prompt) {
        System.err.println(prompt);
        System.exit(1);
    }
   
    private void copy(InputStream in, OutputStream out)
    throws IOException {
       if (buffer == null) {
          int ch;
          while ((ch = in.read()) != -1) {
             out.write(ch);
          }
       } else {
          int c;
          while ((c = in.read(buffer)) != -1) {
             out.write(buffer, 0, c);
          }
       }
    }
    
    private void initTLS() throws GeneralSecurityException, IOException {
       InputStream in = DcmRcv.class.getResourceAsStream("dcmrcv.tls");
       ResourceBundle rb;
       try {
         rb = new PropertyResourceBundle(in);
       } finally {
         try { in.close(); } catch (IOException ignore) {}
       }
       tls = SSLContextAdapter.getInstance();
       tls.setEnabledCipherSuites(tokenize(rb.getString("tls.cipher")));
       char[] keypasswd = rb.getString("tls.key.passwd").toCharArray();
       tls.setKey(tls.loadKeyStore(
            DcmRcv.class.getResource(rb.getString("tls.key")).toString(),
            keypasswd),
            keypasswd);
       tls.setTrust(tls.loadKeyStore(
            DcmRcv.class.getResource(rb.getString("tls.cacerts")).toString(),
            rb.getString("tls.cacerts.passwd").toCharArray()));
    }
    
    private String[] tokenize(String s) {
       StringTokenizer stk = new StringTokenizer(s, ", ");
       String[] retval = new String[stk.countTokens()];
       for (int i = 0; i < retval.length; ++i)
          retval[i] = stk.nextToken();
       return retval;
    } 
    
    // Inner classes -------------------------------------------------
}
