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

import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.FileMetaInfo;
import org.dcm4che.data.DcmDecodeParam;
import org.dcm4che.data.DcmEncodeParam;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.DcmParser;
import org.dcm4che.data.DcmParseException;
import org.dcm4che.data.DcmParserFactory;
import org.dcm4che.data.FileFormat;
import org.dcm4che.dict.DictionaryFactory;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDDictionary;
import org.dcm4che.dict.UIDs;
import org.dcm4che.dict.VRs;
import org.dcm4che.net.ActiveAssociation;
import org.dcm4che.net.Association;
import org.dcm4che.net.AAssociateAC;
import org.dcm4che.net.AAssociateRQ;
import org.dcm4che.net.Factory;
import org.dcm4che.net.DataSource;
import org.dcm4che.net.DcmServiceBase;
import org.dcm4che.net.DcmServiceRegistry;
import org.dcm4che.net.Dimse;
import org.dcm4che.net.PDU;
import org.dcm4che.net.PresContext;
import org.dcm4che.util.SSLContextAdapter;
import org.dcm4che.util.DcmURL;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.nio.ByteOrder;
import java.net.Socket;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.PropertyResourceBundle;
import java.util.StringTokenizer;
import java.security.GeneralSecurityException;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 */
public class DcmSnd {
   
   // Constants -----------------------------------------------------
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
      UIDs.EnhancedMRImageStorage,
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
      UIDs.RawDataStorage,
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
   
   // Attributes ----------------------------------------------------
   private static ResourceBundle messages = ResourceBundle.getBundle(
         "resources/DcmSnd", Locale.getDefault());
   
   private static final UIDDictionary uidDict =
         DictionaryFactory.getInstance().getDefaultUIDDictionary();
   private static final Factory aFact = Factory.getInstance();
   private static final DcmObjectFactory dFact =
         DcmObjectFactory.getInstance();
   private static final DcmParserFactory pFact =
         DcmParserFactory.getInstance();

   private DcmURL url = null;
   private int repeatSingle = 1;
   private int repeatWhole = 1;
   private int priority = Command.MEDIUM;
   private int assocTO = 0;
   private int dimseTO = 0;
   private int releaseTO = 0;
   private AAssociateRQ assocRQ = aFact.newAAssociateRQ();
   private int bufferSize = 2048;
   private byte[] buffer = null;
   private SSLContextAdapter tls = null;
   
   // Static --------------------------------------------------------
   public static void main(String args[]) throws Exception {
      LongOpt[] longopts = new LongOpt[] {
         new LongOpt("async", LongOpt.REQUIRED_ARGUMENT, null, 'a'),
         new LongOpt("prior-high", LongOpt.NO_ARGUMENT, null, 'P'),
         new LongOpt("prior-low", LongOpt.NO_ARGUMENT, null, 'p'),
         new LongOpt("tls", LongOpt.NO_ARGUMENT, null, 's'),
         new LongOpt("buf-len", LongOpt.REQUIRED_ARGUMENT, null, 'b'),
         new LongOpt("max-pdu-len", LongOpt.REQUIRED_ARGUMENT, null, 'L'),
         new LongOpt("repeat-dimse", LongOpt.REQUIRED_ARGUMENT, null, 'r'),
         new LongOpt("repeat-assoc", LongOpt.REQUIRED_ARGUMENT, null, 'R'),
         new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h'),
         new LongOpt("version", LongOpt.NO_ARGUMENT, null, 'v'),
      };

      Getopt g = new Getopt("dcmsnd", args, "", longopts, true);
      
      DcmSnd dcmsnd = new DcmSnd();
      String num = null;
      try {
         int c;
         while ((c = g.getopt()) != -1) {
            switch (c) {
               case 'a':
                  dcmsnd.assocRQ.setAsyncOpsWindow(
                        aFact.newAsyncOpsWindow(
                           Integer.parseInt(num = g.getOptarg()), 1));
                  break;
               case 's':
                  dcmsnd.initTLS();
                  break;
               case 'L':
                  dcmsnd.assocRQ.setMaxLength(
                        Integer.parseInt(num = g.getOptarg()));
                  break;
               case 'b':
                  dcmsnd.bufferSize =
                     Integer.parseInt(num = g.getOptarg()) & 0xfffffffe;
                  break;
               case 'r':
                  dcmsnd.repeatSingle = Integer.parseInt(num = g.getOptarg());
                  break;
               case 'R':
                  dcmsnd.repeatWhole = Integer.parseInt(num = g.getOptarg());
                  break;
               case 'P':
                  dcmsnd.priority = Command.HIGH;
                  break;
               case 'p':
                  dcmsnd.priority = Command.LOW;
                  break;
               case 'v':
                  exit(messages.getString("version"), false);
               case 'h':
                  exit(messages.getString("usage"), false);
               case '?':
                  exit(null, true);
                  break;
            }
         }
      } catch (NumberFormatException nfe) {
         exit(MessageFormat.format(messages.getString("errnum"),
               new Object[]{ num }), true);
      }
      int optind = g.getOptind();
      int argc = args.length - optind;
      if (argc == 0) {
         exit(messages.getString("missing"), true);
      }
      
      try {
         dcmsnd.setUrl(new DcmURL(args[optind]));
      }
      catch (IllegalArgumentException iae) {
         exit(MessageFormat.format(messages.getString("errurl"),
               new Object[]{ args[optind] }), true);
      }
      
      if (argc == 1) {
         dcmsnd.echo();
      }
      else {
         dcmsnd.send(args, optind+1);
      }
    }
       
   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------
   public void setUrl(DcmURL url) {
      this.url = url;
      assocRQ.setCallingAET(url.getCallingAET());
      assocRQ.setCalledAET(url.getCalledAET());
   }
   
   public void echo()
   throws InterruptedException, IOException, GeneralSecurityException {
      final int pcid = 1;
      assocRQ.addPresContext(
      aFact.newPresContext(pcid, UIDs.Verification, DEF_TS));
      long t1 = System.currentTimeMillis();
      int count = 0;
      for (int i = 0; i < repeatWhole; ++i) {
         Association assoc = aFact.newRequestor(
            newSocket(url.getHost(), url.getPort()));
         PDU assocAC = assoc.connect(assocRQ, assocTO);
         if (assocAC instanceof AAssociateAC) {
            ActiveAssociation active = aFact.newActiveAssociation(assoc, null);
            active.start();
            if (assoc.getAcceptedTransferSyntaxUID(pcid) == null) {
               System.out.println(messages.getString("noPCEcho"));
            }
            else for (int j = 0; j < repeatSingle; ++j, ++count) {
               active.invoke(
                     aFact.newDimse(pcid, dFact.newCommand().initCEchoRQ(j)),
                     null);
//               Dimse rsp = assoc.read(dimseTO);
            }
            active.release(true);
//            PDU releaseRP = assoc.release(releaseTO);
         }
      }
      long dt = System.currentTimeMillis() - t1;
      System.out.println(
         MessageFormat.format(messages.getString("doneEcho"),
            new Object[]{ new Integer(count), new Long(dt) }));
   }
   
   public void send(String[] args, int offset)
   throws InterruptedException, IOException, GeneralSecurityException {
      if (bufferSize > 0) {
         buffer = new byte[bufferSize];
      }
      int pcid = 1;
      for (int i = 0; i < IMAGE_STORE_AS.length; ++i) {
         assocRQ.addPresContext(aFact.newPresContext(
         pcid, IMAGE_STORE_AS[i], NATIVE_TS));
         ++pcid;
         ++pcid;
         assocRQ.addPresContext(aFact.newPresContext(
         pcid, IMAGE_STORE_AS[i], JPEG_LL_TS));
         ++pcid;
         ++pcid;
         assocRQ.addPresContext(aFact.newPresContext(
         pcid, IMAGE_STORE_AS[i], JPEG_BL_TS));
         ++pcid;
         ++pcid;
      }
      for (int i = 0; i < NON_IMAGE_STORE_AS.length; ++i) {
         assocRQ.addPresContext(aFact.newPresContext(
         pcid, NON_IMAGE_STORE_AS[i], NATIVE_TS));
         ++pcid;
         ++pcid;
      }
      
      long t1 = System.currentTimeMillis();
      Result res = new Result();
      for (int i = 0; i < repeatWhole; ++i) {
         Association assoc = aFact.newRequestor(
               newSocket(url.getHost(), url.getPort()));
         PDU assocAC = assoc.connect(assocRQ, assocTO);
         if (assocAC instanceof AAssociateAC) {
            ActiveAssociation active = aFact.newActiveAssociation(assoc, null);
            active.start();
            for (int k = offset; k < args.length; ++k) {
               send(active, new File(args[k]), res);
            }
            active.release(true);
         }
      }
      long dt = System.currentTimeMillis() - t1;
      System.out.println();
      System.out.println(
         MessageFormat.format(messages.getString("doneStore"),
            new Object[]{
               new Integer(res.sentCount),
               new Long(res.sentBytes),
               new Long(dt),
               new Float(res.sentBytes/(1.024f*dt)),
      }));
   }
   
   // Private -------------------------------------------------------
   private class Result {
      int sentCount;
      long sentBytes;
//      long wait4Rsp;
   }
   
   private void send(ActiveAssociation active, File file, Result res)
   throws InterruptedException, IOException {
      if (!file.isDirectory()) {
         for (int i = 0; i < repeatSingle; ++i) {
            sendFile(active, file, res);
         }
         return;
      }
      File[] list = file.listFiles();
      for (int i = 0; i < list.length; ++i) {
         send(active, list[i], res);
      }
   }
   
   private void sendFile(ActiveAssociation active, File file, Result res)
   throws InterruptedException, IOException {
      InputStream in = null;
      DcmParser parser = null;
      Dataset ds = null;
      try {
         in = new BufferedInputStream(new FileInputStream(file));
         parser = pFact.newDcmParser(in);
         FileFormat format = parser.detectFileFormat();
         if (format != null) {
            ds = dFact.newDataset();
            parser.setDcmHandler(ds.getDcmHandler());
            parser.parseDcmFile(format, Tags.PixelData);
         } else {
            System.out.println(
               MessageFormat.format(messages.getString("failformat"),
               new Object[]{ file }));
         }
      } catch (IOException e) {
         System.out.println(
            MessageFormat.format(messages.getString("failraid"),
            new Object[]{ file, e }));
         ds = null;
      }
      try {
         if (ds != null) {
            sendDataset(active, file, parser, ds, res);
            
         }
      } finally {
         if (in != null) {
            try { in.close(); } catch (IOException ignore) {};
         }
      }
   }
   
   private boolean sendDataset(ActiveAssociation active, File file,
         DcmParser parser, Dataset ds, Result res)
   throws InterruptedException, IOException {
      String sopInstUID = ds.getString(Tags.SOPInstanceUID);
      if (sopInstUID == null) {
        System.out.println(
            MessageFormat.format(messages.getString("noSOPinst"),
               new Object[]{ file }));
         return false;
      }
      String sopClassUID = ds.getString(Tags.SOPClassUID);
      if (sopClassUID == null) {
        System.out.println(
            MessageFormat.format(messages.getString("noSOPclass"),
               new Object[]{ file }));
         return false;
      }
      PresContext pc = null;
      Association assoc = active.getAssociation();
      if (parser.getDcmDecodeParam().encapsulated) {
         String tsuid = ds.getFileMetaInfo().getTransferSyntaxUID();
         if ((pc = assoc.getAcceptedPresContext(sopClassUID, tsuid))
               == null) {
            System.out.println(
               MessageFormat.format(messages.getString("noPCStore3"),
                  new Object[]{ uidDict.lookup(sopClassUID),
                     uidDict.lookup(tsuid), file }));
            return false;
         }
      } else if ((pc = assoc.getAcceptedPresContext(sopClassUID,
            UIDs.ImplicitVRLittleEndian)) == null
            && (pc = assoc.getAcceptedPresContext(sopClassUID,
            UIDs.ExplicitVRLittleEndian)) == null
            && (pc = assoc.getAcceptedPresContext(sopClassUID,
            UIDs.ExplicitVRBigEndian)) == null) {
         System.out.println(
               MessageFormat.format(messages.getString("noPCStore2"),
                  new Object[]{ uidDict.lookup(sopClassUID),file }));
         return false;
         
      }
      active.invoke(aFact.newDimse(pc.pcid(),
            dFact.newCommand().initCStoreRQ(assoc.nextMsgID(),
                  sopClassUID, sopInstUID, priority),
            new MyDataSource(parser, ds, buffer)), null);
      res.sentBytes += parser.getStreamPosition();
      ++res.sentCount;
      System.out.print('.');
      return true;
   }
   
   private static final class MyDataSource implements DataSource {
      final DcmParser parser;
      final Dataset ds;
      final byte[] buffer;
      MyDataSource(DcmParser parser, Dataset ds, byte[] buffer) {
         this.parser = parser;
         this.ds = ds;
         this.buffer = buffer;
      }
      public void writeTo(OutputStream out, String tsUID)
      throws IOException {
         DcmEncodeParam netParam =
            (DcmEncodeParam)DcmDecodeParam.valueOf(tsUID);
         ds.writeDataset(out, netParam);
         if (parser.getReadTag() == Tags.PixelData) {
            writeTag(netParam.byteOrder, out, Tags.PixelData);
            if (netParam.explicitVR) {
               writeVR(out, parser.getReadVR());
            }
            int pxlen = parser.getReadLength();
            DcmDecodeParam fileParam = parser.getDcmDecodeParam();
            writeLen(netParam.byteOrder, out, pxlen);
            if (pxlen == -1) {
               parser.parseHeader();
               while (parser.getReadTag() == Tags.Item) {
                  writeTag(netParam.byteOrder, out, Tags.Item);
                  int itemLen = parser.getReadLength();
                  writeLen(netParam.byteOrder, out, itemLen);
                  copy(parser.getInputStream(), out, itemLen, false, buffer);
               }
               if (parser.getReadTag() != Tags.SeqDelimitationItem) {
                  throw new DcmParseException("Unexpected Tag:"
                  + Tags.toString(parser.getReadTag()));
               }
               if (parser.getReadLength() != 0) {
                  throw new DcmParseException("(fffe,e0dd), Length:"
                  + parser.getReadLength());
               }
               writeTag(netParam.byteOrder, out, Tags.SeqDelimitationItem);
               writeLen(netParam.byteOrder, out, 0);
            } else {
               copy(parser.getInputStream(), out, pxlen,
               fileParam.byteOrder != netParam.byteOrder
                  && parser.getReadVR() == VRs.OW, buffer);
            }
            ds.clear();
            parser.parseDataset(fileParam, -1);
            ds.writeDataset(out, netParam);
         }
      }
   }
   
   private static void writeTag(ByteOrder order, OutputStream out, int tag)
   throws IOException {
      if (order == ByteOrder.LITTLE_ENDIAN) {
         out.write(tag >> 16);
         out.write(tag >> 24);
         out.write(tag >> 0);
         out.write(tag >> 8);
      } else { // order == ByteOrder.BIG_ENDIAN
         out.write(tag >> 24);
         out.write(tag >> 16);
         out.write(tag >> 8);
         out.write(tag >> 0);
      }
   }
   
   private static void writeVR(OutputStream out, int vr) throws IOException {
      out.write(vr >> 8);
      out.write(vr >> 0);
      out.write(0);
      out.write(0);
   }
   
   private static void writeLen(ByteOrder order, OutputStream out, int len)
   throws IOException {
      if (order == ByteOrder.LITTLE_ENDIAN) {
         out.write(len >> 0);
         out.write(len >> 8);
         out.write(len >> 16);
         out.write(len >> 24);
      } else { // order == ByteOrder.BIG_ENDIAN
         out.write(len >> 24);
         out.write(len >> 16);
         out.write(len >> 8);
         out.write(len >> 0);
      }
   }
   
   private static void copy(InputStream in, OutputStream out, int len,
   boolean swap, byte[] buffer) throws IOException {
      if (swap && (len & 1) != 0) {
         throw new DcmParseException("Illegal length of OW Pixel Data: "
         + len);
      }
      if (buffer == null) {
         if (swap) {
            int tmp;
            for (int i = 0; i < len; ++i,++i) {
               tmp = in.read();
               out.write(in.read());
               out.write(tmp);
            }
         } else {
            for (int i = 0; i < len; ++i) {
               out.write(in.read());
            }
         }
      } else {
         byte tmp;
         int c, remain = len;
         while (remain > 0) {
            c = in.read(buffer, 0, Math.min(buffer.length, remain));
            if (swap) {
               if ((c & 1) != 0) {
                  buffer[c++] = (byte)in.read();
               }
               for (int i = 0; i < c; ++i,++i) {
                  tmp = buffer[i];
                  buffer[i] = buffer[i+1];
                  buffer[i+1] = tmp;
               }
            }
            out.write(buffer, 0, c);
            remain -= c;
         }
      }
   }
      
   private static void exit(String prompt, boolean error) {
       if (prompt != null)
         System.err.println(prompt);
       if (error)
          System.err.println(messages.getString("try"));
       System.exit(1);
    }
   
   private Socket newSocket(String host, int port)
   throws IOException, GeneralSecurityException {
      if (tls != null) {
         return tls.getSocketFactory().createSocket(host, port);
      } else {
         return new Socket(host, port);
      }
   }
   
   private void initTLS() throws GeneralSecurityException, IOException {
      InputStream in = DcmSnd.class.getResourceAsStream("dcmsnd.tls");
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
      DcmSnd.class.getResource(rb.getString("tls.key")), keypasswd),
      keypasswd);
      tls.setTrust(tls.loadKeyStore(
      DcmSnd.class.getResource(rb.getString("tls.cacerts")),
      rb.getString("tls.cacerts.passwd").toCharArray()));
   }
   
   private String[] tokenize(String s) {
      StringTokenizer stk = new StringTokenizer(s, ", ");
      String[] retval = new String[stk.countTokens()];
      for (int i = 0; i < retval.length; ++i)
         retval[i] = stk.nextToken();
      return retval;
   }
   
}
