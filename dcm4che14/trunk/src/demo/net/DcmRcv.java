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
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmEncodeParam;
import org.dcm4che.data.DcmParser;
import org.dcm4che.data.DcmParserFactory;
import org.dcm4che.data.DcmObjectFactory;
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
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Date;
import java.util.StringTokenizer;
import java.security.GeneralSecurityException;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import org.apache.log4j.Logger;

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
   static final Logger log = Logger.getLogger("DcmRcv");
   
   // Attributes ----------------------------------------------------
   private static ResourceBundle messages = ResourceBundle.getBundle(
         "resources/DcmRcv", Locale.getDefault());
   
   private static final ServerFactory srvFact = ServerFactory.getInstance();
   private static final Factory fact = Factory.getInstance();
   private static final DcmParserFactory pFact =
         DcmParserFactory.getInstance();
   private static final DcmObjectFactory oFact =
         DcmObjectFactory.getInstance();
   
   private SSLContextAdapter tls = null;
   private Dataset overwrite = oFact.newDataset();
   private AcceptorPolicy policy = fact.newAcceptorPolicy();
   private DcmServiceRegistry services = fact.newDcmServiceRegistry();
   private DcmHandler handler = srvFact.newDcmHandler(policy, services);
   private Server server = srvFact.newServer(handler);
   private int port = 104;
   private int bufferSize = 2048;
   private byte[] buffer = null;
   private File dir = null;
   private DcmRcvFSU fsu = null;
   private long rspDelay = 0L;
      
   // Static --------------------------------------------------------
   private static final LongOpt[] LONG_OPTS = new LongOpt[] {
      new LongOpt("called-aets", LongOpt.REQUIRED_ARGUMENT, null, 2),
      new LongOpt("calling-aets", LongOpt.REQUIRED_ARGUMENT, null, 2),
      new LongOpt("max-pdu-len", LongOpt.REQUIRED_ARGUMENT, null, 2),
      new LongOpt("max-op-invoked", LongOpt.REQUIRED_ARGUMENT, null, 2),
      new LongOpt("rsp-delay", LongOpt.REQUIRED_ARGUMENT, null, 2),
      new LongOpt("dest", LongOpt.REQUIRED_ARGUMENT, null, 2),
      new LongOpt("set.PatientID", LongOpt.REQUIRED_ARGUMENT, null, 2),
      new LongOpt("set.PatientName", LongOpt.REQUIRED_ARGUMENT, null, 2),
      new LongOpt("fs-id", LongOpt.REQUIRED_ARGUMENT, null, 2),
      new LongOpt("fs-uid", LongOpt.REQUIRED_ARGUMENT, null, 2),
      new LongOpt("fs-file-id", LongOpt.REQUIRED_ARGUMENT, null, 2),
      new LongOpt("fs-lazy-update", LongOpt.NO_ARGUMENT, null, 3),
      new LongOpt("buf-len", LongOpt.REQUIRED_ARGUMENT, null, 2),
      new LongOpt("tls", LongOpt.REQUIRED_ARGUMENT, null, 2),
      new LongOpt("tls-key", LongOpt.REQUIRED_ARGUMENT, null, 2),
      new LongOpt("tls-key-passwd", LongOpt.REQUIRED_ARGUMENT, null, 2),
      new LongOpt("tls-cacerts", LongOpt.REQUIRED_ARGUMENT, null, 2),
      new LongOpt("tls-cacerts-passwd", LongOpt.REQUIRED_ARGUMENT, null, 2),
      new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h'),
      new LongOpt("version", LongOpt.NO_ARGUMENT, null, 'v'),
   };
   
   public static void main(String args[]) throws Exception {
      Getopt g = new Getopt("dcmrcv", args, "", LONG_OPTS);
      
      Configuration cfg = new Configuration(
            DcmRcv.class.getResource("dcmrcv.cfg"));
      int c;
      while ((c = g.getopt()) != -1) {
         switch (c) {
            case 2:
               cfg.put(LONG_OPTS[g.getLongind()].getName(), g.getOptarg());
               break;
            case 3:
               cfg.put(LONG_OPTS[g.getLongind()].getName(), "<yes>");
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
      switch(args.length - optind) {
         case 0:
            exit(messages.getString("missing"), true);
         case 1:
            cfg.put("port", args[optind]);
            break;
         default:
            exit(messages.getString("many"), true);
      }
      listConfig(cfg);
      try {
         new DcmRcv(cfg).start();
      } catch (IllegalArgumentException e) {
         exit(e.getMessage(), true);
      }
   }
   
   // Constructors --------------------------------------------------
   DcmRcv(Configuration cfg) {
      port = Integer.parseInt(cfg.getProperty("port"));
      rspDelay = Integer.parseInt(cfg.getProperty("rsp-delay", "0")) * 1000L;
      bufferSize = Integer.parseInt(
            cfg.getProperty("buf-len", "2048")) & 0xfffffffe;
      initDest(cfg);
      initTLS(cfg);
      initPolicy(cfg);
      initOverwrite(cfg);
   }
      
   // Public --------------------------------------------------------
   public void start() throws GeneralSecurityException, IOException {
      if (fsu != null) {
         new Thread(fsu).start();
      }
      if (bufferSize > 0) {
         buffer = new byte[bufferSize];
      }
      log.info(MessageFormat.format(messages.getString("start"),
            new Object[]{ new Date(), "" + port }));
      if (tls != null) {
         server.start(port, tls.getServerSocketFactory());
      } else {
         server.start(port);
      }
   }
   
   // DcmServiceBase overrides --------------------------------------
   protected void doCStore(ActiveAssociation assoc, Dimse rq, Command rspCmd)
   throws IOException {
      InputStream in = rq.getDataAsStream();
      try {
         if (dir != null) {
            Command rqCmd = rq.getCommand();
            FileMetaInfo fmi = objFact.newFileMetaInfo(
                  rqCmd.getAffectedSOPClassUID(),
                  rqCmd.getAffectedSOPInstanceUID(),
                  rq.getTransferSyntaxUID());
            if (fsu == null) {
               storeToDir(in, fmi);
            } else {
               storeToFileset(in, fmi);
            }
         }
      } catch (IOException ioe) {
         ioe.printStackTrace();
      } finally {
         in.close();
      }
      if (rspDelay > 0L) {
         try {
            Thread.sleep(rspDelay);
         } catch (InterruptedException ie) {
            ie.printStackTrace();
         }
      }
      rspCmd.setUS(Tags.Status, SUCCESS);
   }
   
   private OutputStream openOutputStream(File file)
   throws IOException {
      File parent = file.getParentFile();
      if (!parent.exists()) {
         if (!parent.mkdirs()) {
            throw new IOException("Could not create " + parent);
         }
         log.info("M-WRITE " + parent);
      }
      log.info("M-WRITE " + file);
      return new BufferedOutputStream(new FileOutputStream(file));
   }
   
   private void storeToDir(InputStream in, FileMetaInfo fmi)
   throws IOException {
      OutputStream out = openOutputStream(
                  new File(dir, fmi.getMediaStorageSOPInstanceUID()));
      try {
         fmi.write(out);
         copy(in, out);
      } finally {
         try { out.close(); } catch (IOException ignore) {}
      }
   }
   
   private void storeToFileset(InputStream in, FileMetaInfo fmi)
   throws IOException {
      Dataset ds = oFact.newDataset();
      DcmParser parser = pFact.newDcmParser(in);
      parser.setDcmHandler(ds.getDcmHandler());
      DcmDecodeParam decParam =
            DcmDecodeParam.valueOf(fmi.getTransferSyntaxUID());
      parser.parseDataset(decParam, Tags.PixelData);
      doOverwrite(ds);
      File file = fsu.toFile(ds);
      OutputStream out = openOutputStream(file);
      try {
         ds.setFileMetaInfo(fmi);
         ds.writeFile(out, (DcmEncodeParam)decParam);
         if (parser.getReadTag() != Tags.PixelData) {
            return;
         }
         ds.writeHeader(out, (DcmEncodeParam)decParam,
               parser.getReadTag(),
               parser.getReadVR(),
               parser.getReadLength());
         copy(in, out);
      } finally {
         try { out.close(); } catch (IOException ignore) {}
      }
      fsu.schedule(file, ds);
   }
   
   private void doOverwrite(Dataset ds) {
      for (Iterator it = overwrite.iterator(); it.hasNext();) {
         DcmElement el = (DcmElement)it.next();
         ds.setXX(el.tag(), el.vr(), el.getByteBuffer());
      }
   }
         
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------   
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
      
   private static void listConfig(Configuration cfg) {
      for (int i = 0, n = LONG_OPTS.length - 2; i < n; ++i) {
         String opt = LONG_OPTS[i].getName();
         String val = cfg.getProperty(opt);
         if (val != null) {
            log.info(opt + "=" + val);
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
   
   private final void initDest(Configuration cfg) {
      String dest = cfg.getProperty("dest", "", "<none>", "");
      if (dest.length() == 0)
         return;
      
      this.dir = new File(dest);
      if ("DICOMDIR".equals(dir.getName())) {
         this.fsu = new DcmRcvFSU(dir, cfg);
         handler.addAssociationListener(fsu);
         dir = dir.getParentFile();
      } else {
         if (!dir.exists()) {
            if (dir.mkdirs()) {
               log.info(MessageFormat.format(messages.getString("mkdir"),
                  new Object[]{ dir }));
            } else {
               exit(MessageFormat.format(messages.getString("failmkdir"),
               new Object[]{ dest }), true);
            }
         } else {
            if (!dir.isDirectory())
               exit(MessageFormat.format(messages.getString("errdir"),
               new Object[]{ dest }), true);
         }
      }
   }

   private void initPolicy(Configuration cfg) {
      policy.setCalledAETs(cfg.tokenize(
            cfg.getProperty("called-aets", null, "<any>", null)));
      policy.setCallingAETs(cfg.tokenize(
                  cfg.getProperty("calling-aets", null, "<any>", null)));
      policy.setMaxPDULength(
            Integer.parseInt(cfg.getProperty("max-pdu-len", "16352")));
      policy.setAsyncOpsWindow(fact.newAsyncOpsWindow(
            Integer.parseInt(cfg.getProperty("max-op-invoked", "0")),1));
      for (Enumeration it = cfg.keys(); it.hasMoreElements();) {
         String key = (String)it.nextElement();
         if (key.startsWith("pc.")) {
            initPresContext(key.substring(3),
                  cfg.tokenize(cfg.getProperty(key)));
         }
      }
   }
   
   private void initPresContext(String asName, String[] tsNames) {
      try {
         String as = UIDs.forName(asName);
         String[] tsUIDs = new String[tsNames.length];
         for (int i = 0; i < tsUIDs.length; ++i) {
            tsUIDs[i] = UIDs.forName(tsNames[i]);
         }
         policy.addPresContext(as, tsUIDs);
         services.bind(as, this);
      } catch (NoSuchFieldException nfe) {
         throw new IllegalArgumentException(
            "Illegal entry in dcmrcv.cfg - " + nfe.getMessage());
      }
   }
   
   private void initOverwrite(Configuration cfg) {
      for (Enumeration it = cfg.keys(); it.hasMoreElements();) {
         String key = (String)it.nextElement();
         if (key.startsWith("set.")) {
            try {
               overwrite.setXX(Tags.forName(key.substring(4)),
                  cfg.getProperty(key));
            } catch (Exception e) {
               throw new IllegalArgumentException(
                  "Illegal entry in dcmsnd.cfg - "
                     + key + "=" + cfg.getProperty(key));
            }
         }
      }
   }

   private void initTLS(Configuration cfg) {
      try {
         String[] chiperSuites = cfg.tokenize(
               cfg.getProperty("tls", "", "<none>", ""));
         if (chiperSuites.length == 0)
            return;

         tls = SSLContextAdapter.getInstance();
         tls.setEnabledCipherSuites(chiperSuites);
         char[] keypasswd = cfg.getProperty("key-passwd","dcm4che").toCharArray();
         tls.setKey(
            tls.loadKeyStore(
               DcmRcv.class.getResource(cfg.getProperty("tls-key","dcmrcv.key")),
               keypasswd),
            keypasswd);
         tls.setTrust(tls.loadKeyStore(
            DcmRcv.class.getResource(cfg.getProperty("tls-cacerts", "cacerts")),
            cfg.getProperty("tls-cacerts-passwd", "dcm4che").toCharArray()));
      } catch (Exception ex) {
         throw new RuntimeException("Could not initalize TLS configuration - "
               + ex.getMessage());
      }
   }
   // Inner classes -------------------------------------------------
}
