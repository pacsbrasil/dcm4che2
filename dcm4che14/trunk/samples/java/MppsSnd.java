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
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmEncodeParam;
import org.dcm4che.data.DcmObjectFactory;
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
import org.dcm4che.net.AssociationFactory;
import org.dcm4che.net.Dimse;
import org.dcm4che.net.FutureRSP;
import org.dcm4che.net.PDU;
import org.dcm4che.net.PresContext;
import org.dcm4che.server.PollDirSrv;
import org.dcm4che.server.PollDirSrvFactory;
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
import java.security.GeneralSecurityException;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import org.apache.log4j.Logger;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 */
public class MppsSnd implements PollDirSrv.Handler {
    
    // Constants -----------------------------------------------------
    private static final String[] DEF_TS = { UIDs.ImplicitVRLittleEndian };
    private static final int PCID_ECHO = 1;
    private static final int PCID_MPPS = 3;
    
    // Attributes ----------------------------------------------------
    static final Logger log = Logger.getLogger("MppsSnd");
    private static ResourceBundle messages = ResourceBundle.getBundle(
        "MppsSnd", Locale.getDefault());
    
    private static final UIDDictionary uidDict =
        DictionaryFactory.getInstance().getDefaultUIDDictionary();
    private static final AssociationFactory aFact =
        AssociationFactory.getInstance();
    private static final DcmObjectFactory oFact =
        DcmObjectFactory.getInstance();
    
    private static final int ECHO = 0;
    private static final int SEND = 1;
    private static final int POLL = 2;
    
    private final int mode;
    private DcmURL url = null;
    private int assocTO = 0;
    private int dimseTO = 0;
    private int releaseTO = 0;
    private AAssociateRQ assocRQ = aFact.newAAssociateRQ();
    private SSLContextAdapter tls = null;
    private PollDirSrv pollDirSrv = null;
    private File pollDir = null;
    private long pollPeriod = 5000L;
    private ActiveAssociation activeAssociation = null;
    
    // Static --------------------------------------------------------
    private static final LongOpt[] LONG_OPTS = new LongOpt[] {
        new LongOpt("max-pdu-len", LongOpt.REQUIRED_ARGUMENT, null, 2),
        new LongOpt("max-op-invoked", LongOpt.REQUIRED_ARGUMENT, null, 2),
        new LongOpt("tls", LongOpt.REQUIRED_ARGUMENT, null, 2),
        new LongOpt("tls-key", LongOpt.REQUIRED_ARGUMENT, null, 2),
        new LongOpt("tls-key-passwd", LongOpt.REQUIRED_ARGUMENT, null, 2),
        new LongOpt("tls-cacerts", LongOpt.REQUIRED_ARGUMENT, null, 2),
        new LongOpt("tls-cacerts-passwd", LongOpt.REQUIRED_ARGUMENT, null, 2),
        new LongOpt("ts", LongOpt.REQUIRED_ARGUMENT, null, 2),
        new LongOpt("poll-dir", LongOpt.REQUIRED_ARGUMENT, null, 2),
        new LongOpt("poll-period", LongOpt.REQUIRED_ARGUMENT, null, 2),
        new LongOpt("poll-retry-open", LongOpt.REQUIRED_ARGUMENT, null, 2),
        new LongOpt("poll-delta-last-modified", LongOpt.REQUIRED_ARGUMENT, null, 2),
        new LongOpt("poll-done-dir", LongOpt.REQUIRED_ARGUMENT, null, 2),
        new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h'),
        new LongOpt("version", LongOpt.NO_ARGUMENT, null, 'v'),
    };
    
    public static void main(String args[]) throws Exception {
        Getopt g = new Getopt("mppssnd", args, "", LONG_OPTS);
        
        Configuration cfg = new Configuration(
        MppsSnd.class.getResource("mppssnd.cfg"));
        
        int c;
        while ((c = g.getopt()) != -1) {
            switch (c) {
                case 2:
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
        int argc = args.length - optind;
        if (argc == 0) {
            exit(messages.getString("missing"), true);
        }
        //      listConfig(cfg);
        try {
            MppsSnd mppssnd = new MppsSnd(cfg, new DcmURL(args[optind]), argc);
            mppssnd.execute(args, optind+1);
        } catch (IllegalArgumentException e) {
            exit(e.getMessage(), true);
        }
    }
    
    // Constructors --------------------------------------------------
    
    MppsSnd(Configuration cfg, DcmURL url, int argc) {
        this.url = url;
        this.mode = argc > 1 ? SEND : initPollDirSrv(cfg) ? POLL : ECHO;
        initAssocRQ(cfg, url, mode == ECHO);
        initTLS(cfg);
    }
    
    // Public --------------------------------------------------------
    public void execute(String[] args, int offset)
    throws InterruptedException, IOException, GeneralSecurityException {
        switch (mode) {
            case ECHO:
                echo();
                break;
            case SEND:
                send(args, offset);
                break;
            case POLL:
                poll();
                break;
            default:
                throw new RuntimeException("Illegal mode: " + mode);
        }
    }
    private ActiveAssociation openAssoc()
    throws IOException, GeneralSecurityException {
        Association assoc = aFact.newRequestor(
            newSocket(url.getHost(), url.getPort()));
        PDU assocAC = assoc.connect(assocRQ, assocTO);
        if (!(assocAC instanceof AAssociateAC)) {
            return null;
        }
        ActiveAssociation retval = aFact.newActiveAssociation(assoc, null);
        retval.start();
        return retval;
    }
    
    public void echo()
    throws InterruptedException, IOException, GeneralSecurityException {
        long t1 = System.currentTimeMillis();
        ActiveAssociation active = openAssoc();
        if (active != null) {
            if (active.getAssociation().getAcceptedTransferSyntaxUID(PCID_ECHO)
                    == null) {
                log.error(messages.getString("noPCEcho"));
            } else {
                active.invoke( aFact.newDimse(PCID_ECHO,
                oFact.newCommand().initCEchoRQ(1)), null);
            }
            active.release(true);
        }
        long dt = System.currentTimeMillis() - t1;
        log.info(
            MessageFormat.format(messages.getString("echoDone"),
            new Object[]{ new Long(dt) }));
    }
    
    public void send(String[] args, int offset)
    throws InterruptedException, IOException, GeneralSecurityException {
        long t1 = System.currentTimeMillis();
        int count = 0;
        ActiveAssociation active = openAssoc();
        if (active != null) {
            if (active.getAssociation().getAcceptedTransferSyntaxUID(PCID_MPPS)
            == null) {
                log.error(messages.getString("noPCMpps"));
            } else for (int k = offset; k < args.length; ++k) {
                if (sendFile(active, new File(args[k]))) {
                    ++count;
                }
            }
            active.release(true);
        }
        long dt = System.currentTimeMillis() - t1;
        log.info(
            MessageFormat.format(messages.getString("sendDone"),
            new Object[]{
                new Integer(count),
                new Long(dt),
            }));
    }
    
    public void poll() {
        pollDirSrv.start(pollDir, pollPeriod);
    }
    
    // PollDirSrv.Handler implementation --------------------------------
    public void openSession() throws Exception {
        activeAssociation = openAssoc();
        if (activeAssociation == null) {
            throw new IOException("Could not open association");
        }
    }
    
    public boolean process(File file) throws Exception {
        return sendFile(activeAssociation, file);
    }
    
    public void closeSession() {
        if (activeAssociation != null) {
            try {
                activeAssociation.release(true);
            } catch (Exception e) {
                log.warn("release association throws:", e);
            }
            activeAssociation = null;
        }
    }
    
    // Private -------------------------------------------------------
    private boolean sendFile(ActiveAssociation active, File file)
    throws InterruptedException, IOException {
        String fname = file.getName();
        if (fname.endsWith(".create")) {
            return evalRSP(sendCreate(active, file));
        }
        if (fname.endsWith(".set")) {
            return evalRSP(sendSet(active, file));
        }
        
        log.error(MessageFormat.format(messages.getString("errfname"),
            new Object[]{ file }));
        return false;
    }
    
    private boolean evalRSP(FutureRSP futureRSP)
    throws InterruptedException, IOException {
        if (futureRSP == null) {
            return false;
        }
        Dimse rsp = futureRSP.get();
        switch (rsp.getCommand().getStatus()) {
            case 0:
                return true;
            default:
                throw new IOException("" + rsp);
        }
    }
    
    private FutureRSP sendSet(ActiveAssociation active, File file)
    throws InterruptedException, IOException {
        Dataset ds = loadDataset(file);
        if (ds == null) {
            return null;
        }
        FileMetaInfo fmi = ds.getFileMetaInfo();
        if (fmi == null) {
            log.error(
                MessageFormat.format(messages.getString("noFMI"),
                new Object[]{ file }));
            return null;
        }
        if (!UIDs.ModalityPerformedProcedureStep.equals(
        fmi.getMediaStorageSOPClassUID())) {
            log.error(
                MessageFormat.format(messages.getString("errSOPClass"),
                new Object[]{
                    file,
                    uidDict.toString(fmi.getMediaStorageSOPClassUID())
                }));    
            return null;
        }
        return active.invoke(aFact.newDimse(PCID_MPPS,
        oFact.newCommand().initNSetRQ(
        active.getAssociation().nextMsgID(),
        UIDs.ModalityPerformedProcedureStep,
        fmi.getMediaStorageSOPInstanceUID()), ds));
    }
    
    private FutureRSP sendCreate(ActiveAssociation active, File file)
    throws InterruptedException, IOException {
        Dataset ds = loadDataset(file);
        if (ds == null) {
            return null;
        }
        FileMetaInfo fmi = ds.getFileMetaInfo();
        String instUID = null;
        if (fmi != null) {
            if (!UIDs.ModalityPerformedProcedureStep.equals(
            fmi.getMediaStorageSOPClassUID())) {
                log.error(
                    MessageFormat.format(messages.getString("errSOPClass"),
                        new Object[]{
                            file,
                            uidDict.toString(fmi.getMediaStorageSOPClassUID())
                    }));
                return null;
            }
            instUID = fmi.getMediaStorageSOPInstanceUID();
        }
        return active.invoke(aFact.newDimse(PCID_MPPS,
        oFact.newCommand().initNCreateRQ(
        active.getAssociation().nextMsgID(),
        UIDs.ModalityPerformedProcedureStep,
        instUID),
        ds));
    }
    
    private Dataset loadDataset(File file) {
        InputStream in = null;
        try {
            in = new BufferedInputStream(new FileInputStream(file));
            Dataset retval = oFact.newDataset();
            retval.readFile(in, null, -1);
            log.info(
                MessageFormat.format(messages.getString("readDone"),
                new Object[]{ file }));
            return retval;
        } catch (IOException e) {
            log.error(
                MessageFormat.format(messages.getString("failread"),
                new Object[]{ file, e }));
            return null;
        } finally {
            try { in.close(); } catch (IOException ignore) {};
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
    
    private static void exit(String prompt, boolean error) {
        if (prompt != null)
            System.err.println(prompt);
        if (error)
            System.err.println(messages.getString("try"));
        System.exit(1);
    }
    
    private final void initAssocRQ(Configuration cfg, DcmURL url, boolean echo) {
        assocRQ.setCalledAET(url.getCalledAET());
        assocRQ.setCallingAET(url.getCallingAET());
        assocRQ.setMaxPDULength(
            Integer.parseInt(cfg.getProperty("max-pdu-len", "16352")));
        assocRQ.setAsyncOpsWindow(aFact.newAsyncOpsWindow(
            Integer.parseInt(cfg.getProperty("max-op-invoked", "0")),1));
        if (echo) {
            assocRQ.addPresContext(
            aFact.newPresContext(PCID_ECHO, UIDs.Verification, DEF_TS));
        } else {
            assocRQ.addPresContext(aFact.newPresContext(
            PCID_MPPS, UIDs.ModalityPerformedProcedureStep,
            cfg.tokenize(cfg.getProperty("ts", ""))));
        }
    }
    
    private boolean initPollDirSrv(Configuration cfg) {
        String pollDirName = cfg.getProperty("poll-dir", "", "<none>", "");
        if (pollDirName.length() == 0) {
            return false;
        }
        
        pollDir = new File(pollDirName);
        if (!pollDir.isDirectory()) {
            throw new IllegalArgumentException("Not a directory - " + pollDirName);
        }
        pollPeriod = 1000L * Integer.parseInt(
        cfg.getProperty("poll-period", "5"));
        pollDirSrv = PollDirSrvFactory.getInstance().newPollDirSrv(this);
        pollDirSrv.setOpenRetryPeriod(1000L * Integer.parseInt(
        cfg.getProperty("poll-retry-open", "60")) * 1000L);
        pollDirSrv.setDeltaLastModified(1000L * Integer.parseInt(
        cfg.getProperty("poll-delta-last-modified", "3")));
        String doneDirName = cfg.getProperty("poll-done-dir", "", "<none>", "");
        if (doneDirName.length() != 0) {
            File doneDir = new File(doneDirName);
            if (!doneDir.isDirectory()) {
                throw new IllegalArgumentException("Not a directory - " + doneDirName);
            }
            pollDirSrv.setDoneDir(doneDir);
        }
        return true;
    }
    
    private void initTLS(Configuration cfg) {
        try {
            String[] chiperSuites = cfg.tokenize(
            cfg.getProperty("tls", "", "<none>", ""));
            if (chiperSuites.length == 0)
                return;
            
            tls = SSLContextAdapter.getInstance();
            tls.setEnabledCipherSuites(chiperSuites);
            char[] keypasswd = 
                cfg.getProperty("key-passwd","dcm4che").toCharArray();
            tls.setKey(
                tls.loadKeyStore(
                    MppsSnd.class.getResource(
                        cfg.getProperty("tls-key","mppssnd.key")),
                    keypasswd),
                keypasswd);
            tls.setTrust(tls.loadKeyStore(
                MppsSnd.class.getResource(
                    cfg.getProperty("tls-cacerts", "cacerts")),
                cfg.getProperty("tls-cacerts-passwd", "dcm4che").toCharArray()));
            tls.init();
        } catch (Exception ex) {
            throw new RuntimeException("Could not initalize TLS configuration - "
            + ex.getMessage());
        }
    }
    
}
