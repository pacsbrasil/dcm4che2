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
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Properties;
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
   private static final String[] DEF_TS = { UIDs.ImplicitVRLittleEndian };
   private static final int PCID_ECHO = 1;
   
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
   private static final LongOpt[] LONG_OPTS = new LongOpt[] {
      new LongOpt("prior-high", LongOpt.NO_ARGUMENT, null, 'P'),
      new LongOpt("prior-low", LongOpt.NO_ARGUMENT, null, 'p'),
      new LongOpt("max-pdu-len", LongOpt.REQUIRED_ARGUMENT, null, 2),
      new LongOpt("max-op-invoked", LongOpt.REQUIRED_ARGUMENT, null, 2),
      new LongOpt("buf-len", LongOpt.REQUIRED_ARGUMENT, null, 2),
      new LongOpt("tls", LongOpt.REQUIRED_ARGUMENT, null, 2),
      new LongOpt("tls-key", LongOpt.REQUIRED_ARGUMENT, null, 2),
      new LongOpt("tls-key-passwd", LongOpt.REQUIRED_ARGUMENT, null, 2),
      new LongOpt("tls-cacerts", LongOpt.REQUIRED_ARGUMENT, null, 2),
      new LongOpt("tls-cacerts-passwd", LongOpt.REQUIRED_ARGUMENT, null, 2),
      new LongOpt("repeat-dimse", LongOpt.REQUIRED_ARGUMENT, null, 2),
      new LongOpt("repeat-assoc", LongOpt.REQUIRED_ARGUMENT, null, 2),
      new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h'),
      new LongOpt("version", LongOpt.NO_ARGUMENT, null, 'v'),
   };
   
   public static void main(String args[]) throws Exception {
      Getopt g = new Getopt("dcmsnd", args, "", LONG_OPTS);
      
      Properties cfg = loadConfig();
      int c;
      while ((c = g.getopt()) != -1) {
         switch (c) {
            case 2:
               cfg.put(LONG_OPTS[g.getLongind()].getName(), g.getOptarg());
               break;
            case 'P':
               cfg.put("prior", "1");
               break;
            case 'p':
               cfg.put("prior", "2");
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
      int optind = g.getOptind();
      int argc = args.length - optind;
      if (argc == 0) {
         exit(messages.getString("missing"), true);
      }
//      listConfig(cfg);
      try {
         DcmSnd dcmsnd = new DcmSnd(cfg, new DcmURL(args[optind]), argc == 1);
         if (argc == 1) {
            dcmsnd.echo();
         }
         else {
            dcmsnd.send(args, optind+1);
         }
      } catch (IllegalArgumentException e) {
         exit(e.getMessage(), true);
      }
   }

   // Constructors --------------------------------------------------
   
   DcmSnd(Properties cfg, DcmURL url, boolean echo) {
      this.url = url;
      this.priority = Integer.parseInt(cfg.getProperty("prior", "0"));
      this.bufferSize = Integer.parseInt(cfg.getProperty("buf-len", "2048"))
            & 0xfffffffe;
      this.repeatWhole = Integer.parseInt(cfg.getProperty("repeat-assoc", "1"));
      this.repeatSingle = Integer.parseInt(cfg.getProperty("repeat-dimse", "1"));
      initAssocRQ(cfg, url, echo);
      initTLS(cfg);
   }
       
   // Public --------------------------------------------------------
   public void echo()
   throws InterruptedException, IOException, GeneralSecurityException {
      long t1 = System.currentTimeMillis();
      int count = 0;
      for (int i = 0; i < repeatWhole; ++i) {
         Association assoc = aFact.newRequestor(
            newSocket(url.getHost(), url.getPort()));
         PDU assocAC = assoc.connect(assocRQ, assocTO);
         if (assocAC instanceof AAssociateAC) {
            ActiveAssociation active = aFact.newActiveAssociation(assoc, null);
            active.start();
            if (assoc.getAcceptedTransferSyntaxUID(PCID_ECHO) == null) {
               System.out.println(messages.getString("noPCEcho"));
            }
            else for (int j = 0; j < repeatSingle; ++j, ++count) {
               active.invoke(
                     aFact.newDimse(PCID_ECHO, dFact.newCommand().initCEchoRQ(j)),
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
      
   private Socket newSocket(String host, int port)
   throws IOException, GeneralSecurityException {
      if (tls != null) {
         return tls.getSocketFactory().createSocket(host, port);
      } else {
         return new Socket(host, port);
      }
   }
   
   private static Properties loadConfig() {
      InputStream in = DcmSnd.class.getResourceAsStream("dcmsnd.cfg");
      try {
         Properties retval = new Properties();
         retval.load(in);
         return retval;
      } catch (Exception e) {
         throw new RuntimeException("Could not read dcmsnd.cfg", e);
      } finally {
         if (in != null) {
            try { in.close(); } catch (IOException ignore) {}
         }
      }
   }            

   private static void listConfig(Properties cfg) {
      for (int i = 0, n = LONG_OPTS.length - 2; i < n; ++i) {
         System.out.print(LONG_OPTS[i].getName());
         System.out.print('=');
         System.out.println(cfg.getProperty(LONG_OPTS[i].getName(),""));
      }
   }

   private static void exit(String prompt, boolean error) {
      if (prompt != null)
         System.err.println(prompt);
      if (error)
         System.err.println(messages.getString("try"));
      System.exit(1);
   }
   
   private static List tokenize(Properties cfg, String s, List result) {
      StringTokenizer stk = new StringTokenizer(s, ", ");
      while (stk.hasMoreTokens()) {
         String tk = stk.nextToken();
         if (tk.startsWith("$")) {
            tokenize(cfg, cfg.getProperty(tk.substring(1),""), result);
         } else {
            result.add(tk);
         }
      }
      return result;
   }
   
   private static String replace(String val, String from, String to) {
      return from.equals(val) ? to : val;
   }
   
   private static final String[] EMPTY_STRING_ARRAY = {};
   private static String[] tokenize(Properties cfg, String s) {
      return s != null ? (String[])tokenize(cfg, s, new LinkedList())
                                       .toArray(EMPTY_STRING_ARRAY)
                       : null;
   }
   
   private final void initAssocRQ(Properties cfg, DcmURL url, boolean echo) {      
      assocRQ.setCalledAET(url.getCalledAET());
      assocRQ.setCallingAET(url.getCallingAET());
      assocRQ.setMaxPDULength(
            Integer.parseInt(cfg.getProperty("max-pdu-len", "16352")));
      assocRQ.setAsyncOpsWindow(aFact.newAsyncOpsWindow(
            Integer.parseInt(cfg.getProperty("max-op-invoked", "0")),1));
      if (echo) {
         assocRQ.addPresContext(
            aFact.newPresContext(PCID_ECHO, UIDs.Verification, DEF_TS));
         return;
      }
      for (Enumeration it = cfg.keys(); it.hasMoreElements();) {
         String key = (String)it.nextElement();
         if (key.startsWith("pc.")) {
            initPresContext(Integer.parseInt(key.substring(3)),
               tokenize(cfg, cfg.getProperty(key), new LinkedList()));
         }
      }
   }
      
   private final void initPresContext(int pcid, List val) {
      try {
         Iterator it = val.iterator();
         String as = UIDs.forName((String)it.next());
         String[] tsUIDs = new String[val.size()-1];
         for (int i = 0; i < tsUIDs.length; ++i) {
            tsUIDs[i] = UIDs.forName((String)it.next());
         }
         assocRQ.addPresContext(aFact.newPresContext(pcid, as, tsUIDs));
      } catch (NoSuchFieldException nfe) {
         throw new IllegalArgumentException(
            "Illegal entry in dcmsnd.cfg - " + nfe.getMessage());
      }
   }
   
   private final void initTLS(Properties cfg) {
      try {
         String[] chiperSuites = tokenize(cfg,
               replace(cfg.getProperty("tls", ""), "<none>", ""));
         if (chiperSuites.length == 0)
            return;

         tls = SSLContextAdapter.getInstance();
         tls.setEnabledCipherSuites(chiperSuites);
         char[] keypasswd = cfg.getProperty("key-passwd","dcm4che").toCharArray();
         tls.setKey(
            tls.loadKeyStore(
               DcmSnd.class.getResource(cfg.getProperty("tls-key","dcmsnd.key")),
               keypasswd),
            keypasswd);
         tls.setTrust(tls.loadKeyStore(
            DcmSnd.class.getResource(cfg.getProperty("tls-cacerts", "cacerts")),
            cfg.getProperty("tls-cacerts-passwd", "dcm4che").toCharArray()));
      } catch (Exception ex) {
         throw new RuntimeException("Could not initalize TLS configuration - "
               + ex.getMessage());
      }
   }
}
