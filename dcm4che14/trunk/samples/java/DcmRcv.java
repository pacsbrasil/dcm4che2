
import java.io.BufferedOutputStream;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import org.apache.log4j.Logger;
/*                                                                           *
 *  Copyright (c) 2002, 2003 by TIANI MEDGRAPH AG                            *
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
 */
import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmDecodeParam;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmEncodeParam;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.DcmParser;
import org.dcm4che.data.DcmParserFactory;
import org.dcm4che.data.FileMetaInfo;
import org.dcm4che.dict.Status;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4che.net.AcceptorPolicy;
import org.dcm4che.net.ActiveAssociation;
import org.dcm4che.net.AssociationFactory;
import org.dcm4che.net.DcmServiceBase;
import org.dcm4che.net.DcmServiceRegistry;
import org.dcm4che.net.Dimse;
import org.dcm4che.server.DcmHandler;
import org.dcm4che.server.Server;
import org.dcm4che.server.ServerFactory;
import org.dcm4che.util.DcmProtocol;
import org.dcm4che.util.SSLContextAdapter;

/**
 * <description>
 *
 * @author     <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @created    May, 2002
 * @version    $Revision$ $Date$
 */
public class DcmRcv extends DcmServiceBase
{
    // Constants -----------------------------------------------------
    final static Logger log = Logger.getLogger(DcmRcv.class);

    // Attributes ----------------------------------------------------
    private static ResourceBundle messages = ResourceBundle.getBundle(
            "DcmRcv", Locale.getDefault());

    private final static ServerFactory srvFact =
            ServerFactory.getInstance();
    private final static AssociationFactory fact =
            AssociationFactory.getInstance();
    private final static DcmParserFactory pFact =
            DcmParserFactory.getInstance();
    private final static DcmObjectFactory oFact =
            DcmObjectFactory.getInstance();

    private SSLContextAdapter tls = null;
    private DcmProtocol protocol = DcmProtocol.DICOM;

    private Dataset overwrite = oFact.newDataset();
    private AcceptorPolicy policy = fact.newAcceptorPolicy();
    private DcmServiceRegistry services = fact.newDcmServiceRegistry();
    private DcmHandler handler = srvFact.newDcmHandler(policy, services);
    private Server server = srvFact.newServer(handler);
    private int bufferSize = 512;
    private File dir = null;
    private DcmRcvFSU fsu = null;
    private long rspDelay = 0L;

    // Static --------------------------------------------------------
    private final static LongOpt[] LONG_OPTS = new LongOpt[]{
            new LongOpt("max-clients", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("rq-timeout", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("dimse-timeout", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("so-close-delay", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("called-aets", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("calling-aets", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("max-pdu-len", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("max-op-invoked", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("rsp-delay", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("dest", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("set", LongOpt.REQUIRED_ARGUMENT, null, 's'),
            new LongOpt("fs-id", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("fs-uid", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("fs-file-id", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("fs-lazy-update", LongOpt.NO_ARGUMENT, null, 3),
            new LongOpt("buf-len", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("dicom-tls", LongOpt.NO_ARGUMENT, null, 4),
            new LongOpt("dicom-tls.nodes", LongOpt.NO_ARGUMENT, null, 4),
            new LongOpt("dicom-tls.3des", LongOpt.NO_ARGUMENT, null, 4),
            new LongOpt("tls-key", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("tls-key-passwd", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("tls-cacerts", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("tls-cacerts-passwd", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h'),
            new LongOpt("version", LongOpt.NO_ARGUMENT, null, 'v'),
            };


    private static void set(Configuration cfg, String s)
    {
        int pos = s.indexOf(':');
        if (pos == -1) {
            cfg.put("set." + s, "");
        } else {
            cfg.put("set." + s.substring(0, pos), s.substring(pos + 1));
        }
    }


    /**
     *  Description of the Method
     *
     * @param  args           Description of the Parameter
     * @exception  Exception  Description of the Exception
     */
    public static void main(String args[])
        throws Exception
    {
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
                case 4:
                    cfg.put("protocol", LONG_OPTS[g.getLongind()].getName());
                    break;
                case 's':
                    set(cfg, g.getOptarg());
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
        switch (args.length - optind) {
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
    DcmRcv(Configuration cfg)
    {
        rspDelay = Integer.parseInt(cfg.getProperty("rsp-delay", "0")) * 1000L;
        bufferSize = Integer.parseInt(
                cfg.getProperty("buf-len", "2048")) & 0xfffffffe;
        initServer(cfg);
        initDest(cfg);
        initTLS(cfg);
        initPolicy(cfg);
        initOverwrite(cfg);
    }

    // Public --------------------------------------------------------
    /**
     *  Description of the Method
     *
     * @exception  IOException               Description of the Exception
     */
    public void start() throws IOException
    {
        if (fsu != null) {
            new Thread(fsu).start();
        }
        server.start();
    }

    // DcmServiceBase overrides --------------------------------------
    /**
     *  Description of the Method
     *
     * @param  assoc            Description of the Parameter
     * @param  rq               Description of the Parameter
     * @param  rspCmd           Description of the Parameter
     * @exception  IOException  Description of the Exception
     */
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
        rspCmd.putUS(Tags.Status, Status.Success);
    }


    private OutputStream openOutputStream(File file)
        throws IOException
    {
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
        throws IOException
    {
        OutputStream out = openOutputStream(
                new File(dir, fmi.getMediaStorageSOPInstanceUID()));
        try {
            fmi.write(out);
            copy(in, out);
        } finally {
            try {
                out.close();
            } catch (IOException ignore) {}
        }
    }


    private void storeToFileset(InputStream in, FileMetaInfo fmi)
        throws IOException
    {
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
            ds.writeFile(out, (DcmEncodeParam) decParam);
            if (parser.getReadTag() != Tags.PixelData) {
                return;
            }
            ds.writeHeader(out, (DcmEncodeParam) decParam,
                    parser.getReadTag(),
                    parser.getReadVR(),
                    parser.getReadLength());
            copy(in, out);
        } finally {
            try {
                out.close();
            } catch (IOException ignore) {}
        }
        fsu.schedule(file, ds);
    }


    private void doOverwrite(Dataset ds)
    {
        for (Iterator it = overwrite.iterator(); it.hasNext(); ) {
            DcmElement el = (DcmElement) it.next();
            ds.putXX(el.tag(), el.vr(), el.getByteBuffer());
        }
    }

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------
    private void copy(InputStream in, OutputStream out)
        throws IOException
    {
        if (bufferSize > 0) {
            byte[] buffer = new byte[bufferSize];
            int c;
            while ((c = in.read(buffer)) != -1) {
                out.write(buffer, 0, c);
            }
        } else {
            int ch;
            while ((ch = in.read()) != -1) {
                out.write(ch);
            }
        }
    }


    private static void listConfig(Configuration cfg)
    {
        StringBuffer msg = new StringBuffer();
        msg.append(messages.getString("cfg"));
        msg.append("\n\tprotocol=").append(cfg.getProperty("protocol"));
        for (int i = 0, n = LONG_OPTS.length - 2; i < n; ++i) {
            String opt = LONG_OPTS[i].getName();
            String val = cfg.getProperty(opt);
            if (val != null) {
                msg.append("\n\t").append(opt).append("=").append(val);
            }
        }
        log.info(msg.toString());
    }


    private static void exit(String prompt, boolean error)
    {
        if (prompt != null) {
            System.err.println(prompt);
        }
        if (error) {
            System.err.println(messages.getString("try"));
        }
        System.exit(1);
    }


    private final void initDest(Configuration cfg)
    {
        String dest = cfg.getProperty("dest", "", "<none>", "");
        if (dest.length() == 0) {
            return;
        }

        this.dir = new File(dest);
        if ("DICOMDIR".equals(dir.getName())) {
            this.fsu = new DcmRcvFSU(dir, cfg);
            handler.addAssociationListener(fsu);
            dir = dir.getParentFile();
        } else {
            if (!dir.exists()) {
                if (dir.mkdirs()) {
                    log.info(MessageFormat.format(messages.getString("mkdir"),
                            new Object[]{dir}));
                } else {
                    exit(MessageFormat.format(messages.getString("failmkdir"),
                            new Object[]{dest}), true);
                }
            } else {
                if (!dir.isDirectory()) {
                    exit(MessageFormat.format(messages.getString("errdir"),
                            new Object[]{dest}), true);
                }
            }
        }
    }


    private void initServer(Configuration cfg)
    {
        server.setPort(
                Integer.parseInt(cfg.getProperty("port")));
        server.setMaxClients(
                Integer.parseInt(cfg.getProperty("max-clients", "10")));
        handler.setRqTimeout(
                Integer.parseInt(cfg.getProperty("rq-timeout", "5000")));
        handler.setDimseTimeout(
                Integer.parseInt(cfg.getProperty("dimse-timeout", "0")));
        handler.setSoCloseDelay(
                Integer.parseInt(cfg.getProperty("so-close-delay", "500")));
    }


    private void initPolicy(Configuration cfg)
    {
        policy.setCalledAETs(cfg.tokenize(
                cfg.getProperty("called-aets", null, "<any>", null)));
        policy.setCallingAETs(cfg.tokenize(
                cfg.getProperty("calling-aets", null, "<any>", null)));
        policy.setMaxPDULength(
                Integer.parseInt(cfg.getProperty("max-pdu-len", "16352")));
        policy.setAsyncOpsWindow(
                Integer.parseInt(cfg.getProperty("max-op-invoked", "0")), 1);
        for (Enumeration it = cfg.keys(); it.hasMoreElements(); ) {
            String key = (String) it.nextElement();
            if (key.startsWith("pc.")) {
                initPresContext(key.substring(3),
                        cfg.tokenize(cfg.getProperty(key)));
            }
        }
    }


    private void initPresContext(String asName, String[] tsNames)
    {
        String as = UIDs.forName(asName);
        String[] tsUIDs = new String[tsNames.length];
        for (int i = 0; i < tsUIDs.length; ++i) {
            tsUIDs[i] = UIDs.forName(tsNames[i]);
        }
        policy.putPresContext(as, tsUIDs);
        services.bind(as, this);
    }


    private void initOverwrite(Configuration cfg)
    {
        for (Enumeration it = cfg.keys(); it.hasMoreElements(); ) {
            String key = (String) it.nextElement();
            if (key.startsWith("set.")) {
                try {
                    overwrite.putXX(Tags.forName(key.substring(4)),
                            cfg.getProperty(key));
                } catch (Exception e) {
                    throw new IllegalArgumentException(
                            "Illegal entry in dcmsnd.cfg - "
                             + key + "=" + cfg.getProperty(key));
                }
            }
        }
    }


    private void initTLS(Configuration cfg)
    {
        try {
            this.protocol = DcmProtocol.valueOf(
                    cfg.getProperty("protocol", "dicom"));
            if (!protocol.isTLS()) {
                return;
            }

            tls = SSLContextAdapter.getInstance();
            char[] keypasswd = cfg.getProperty("tls-key-passwd", "iheihe").toCharArray();
            tls.setKey(
                    tls.loadKeyStore(
                    DcmRcv.class.getResource(cfg.getProperty("tls-key", "test_sys_2.p12")),
                    keypasswd),
                    keypasswd);
            tls.setTrust(tls.loadKeyStore(
                    DcmRcv.class.getResource(cfg.getProperty("tls-cacerts", "cacerts.jks")),
                    cfg.getProperty("tls-cacerts-passwd", "iheihe").toCharArray()));
            this.server.setServerSocketFactory(
                    tls.getServerSocketFactory(protocol.getCipherSuites()));
        } catch (Exception ex) {
            throw new RuntimeException("Could not initalize TLS configuration: ", ex);
        }
    }
}

