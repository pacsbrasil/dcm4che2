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
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Properties;
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
   
   // Attributes ----------------------------------------------------
   private static ResourceBundle messages = ResourceBundle.getBundle(
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
   private long rspDelay = 0L;
      
   // Static --------------------------------------------------------
   private static final LongOpt[] LONG_OPTS = new LongOpt[] {
      new LongOpt("called-aets", LongOpt.REQUIRED_ARGUMENT, null, 0),
      new LongOpt("calling-aets", LongOpt.REQUIRED_ARGUMENT, null, 0),
      new LongOpt("max-pdu-len", LongOpt.REQUIRED_ARGUMENT, null, 0),
      new LongOpt("max-op-invoked", LongOpt.REQUIRED_ARGUMENT, null, 0),
      new LongOpt("rsp-delay", LongOpt.REQUIRED_ARGUMENT, null, 0),
      new LongOpt("dest", LongOpt.REQUIRED_ARGUMENT, null, 0),
      new LongOpt("buf-len", LongOpt.REQUIRED_ARGUMENT, null, 0),
      new LongOpt("tls", LongOpt.REQUIRED_ARGUMENT, null, 0),
      new LongOpt("tls-key", LongOpt.REQUIRED_ARGUMENT, null, 0),
      new LongOpt("tls-key-passwd", LongOpt.REQUIRED_ARGUMENT, null, 0),
      new LongOpt("tls-cacerts", LongOpt.REQUIRED_ARGUMENT, null, 0),
      new LongOpt("tls-cacerts-passwd", LongOpt.REQUIRED_ARGUMENT, null, 0),
      new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h'),
      new LongOpt("version", LongOpt.NO_ARGUMENT, null, 'v'),
   };
   
   public static void main(String args[]) throws Exception {
      Getopt g = new Getopt("dcmrcv", args, "", LONG_OPTS);
      
      Properties cfg = loadConfig();
      int c;
      while ((c = g.getopt()) != -1) {
         switch (c) {
            case 0:
               cfg.put(LONG_OPTS[g.getLongind()].getName(), g.getOptarg());
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
   DcmRcv(Properties cfg) {
      port = Integer.parseInt(cfg.getProperty("port"));
      rspDelay = Integer.parseInt(cfg.getProperty("rsp-delay", "0")) * 1000L;
      bufferSize = Integer.parseInt(
            cfg.getProperty("buf-len", "2048")) & 0xfffffffe;
      initDest(cfg);
      initTLS(cfg);
      initPolicy(cfg);
   }
      
   // Public --------------------------------------------------------
   public void start() throws GeneralSecurityException, IOException {
      if (bufferSize > 0) {
         buffer = new byte[bufferSize];
      }
      System.out.println(MessageFormat.format(messages.getString("start"),
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
      if (rspDelay > 0L) {
         try {
            Thread.sleep(rspDelay);
         } catch (InterruptedException ie) {
            ie.printStackTrace();
         }
      }
      rspCmd.setUS(Tags.Status, SUCCESS);
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
      
   private static Properties loadConfig() {
      InputStream in = DcmRcv.class.getResourceAsStream("dcmrcv.cfg");
      try {
         Properties retval = new Properties();
         retval.load(in);
         return retval;
      } catch (Exception e) {
         throw new RuntimeException("Could not read dcmrcv.cfg", e);
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
   
   private static final String[] EMPTY_STRING_ARRAY = {};
   private static String[] tokenize(Properties cfg, String s) {
      return s != null ? (String[])tokenize(cfg, s, new LinkedList())
                                       .toArray(EMPTY_STRING_ARRAY)
                       : null;
   }
   
   private static String replace(String val, String from, String to) {
      return from.equals(val) ? to : val;
   }
   
   private final void initDest(Properties cfg) {
      String dest = replace(cfg.getProperty("dest"), "<none>", "");
      if (dest.length() == 0)
         return;
      
      this.dir = new File(dest);
      if (!dir.exists()) {
         if (dir.mkdirs()) {
            System.out.println(
            MessageFormat.format(messages.getString("mkdir"),
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

   private final void initPolicy(Properties cfg) {
      policy.setCalledAETs(tokenize(cfg,
            replace(cfg.getProperty("called-aets"), "<any>", null)));
      policy.setCallingAETs(tokenize(cfg,
                  replace(cfg.getProperty("calling-aets"), "<any>", null)));
      policy.setMaxPDULength(
            Integer.parseInt(cfg.getProperty("max-pdu-len", "16352")));
      policy.setAsyncOpsWindow(fact.newAsyncOpsWindow(
            Integer.parseInt(cfg.getProperty("max-op-invoked", "0")),1));
      for (Enumeration it = cfg.keys(); it.hasMoreElements();) {
         String key = (String)it.nextElement();
         if (key.startsWith("pc.")) {
            initPresContext(
               tokenize(cfg, cfg.getProperty(key), new LinkedList()));
         }
      }
   }
   
   private final void initPresContext(List val) {
      try {
         Iterator it = val.iterator();
         String as = UIDs.forName((String)it.next());
         String[] tsUIDs = new String[val.size()-1];
         for (int i = 0; i < tsUIDs.length; ++i) {
            tsUIDs[i] = UIDs.forName((String)it.next());
         }
         policy.addPresContext(as, tsUIDs);
         services.bind(as, this);
      } catch (NoSuchFieldException nfe) {
         throw new IllegalArgumentException(
            "Illegal entry in dcmrcv.cfg - " + nfe.getMessage());
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
