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

import org.dcm4che.hl7.HL7Exception;
import org.dcm4che.hl7.HL7Factory;
import org.dcm4che.hl7.HL7Message;
import org.dcm4che.hl7.HL7Service;
import org.dcm4che.server.HL7Handler;
import org.dcm4che.server.Server;
import org.dcm4che.server.ServerFactory;
import org.dcm4che.util.MLLP_URL;
import org.dcm4che.util.SSLContextAdapter;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import org.apache.log4j.Logger;

import java.util.ResourceBundle;
import java.util.Locale;

/**
 * <description>
 *
 * @see <related>
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @version $Revision$ $Date$
 * @since September 7, 2002
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>yyyymmdd author:</b>
 * <ul>
 * <li> explicit fix description (no line numbers but methods) go
 *            beyond the cvs commit message
 * </ul>
 */
public class Hl7Rcv implements HL7Service {
    
    // Constants -----------------------------------------------------
    private static final LongOpt[] LONG_OPTS = new LongOpt[] {
        new LongOpt("max-clients", LongOpt.REQUIRED_ARGUMENT, null, 2),
        new LongOpt("so-timeout", LongOpt.REQUIRED_ARGUMENT, null, 2),
        new LongOpt("receiving-apps", LongOpt.REQUIRED_ARGUMENT, null, 2),
        new LongOpt("sending-apps", LongOpt.REQUIRED_ARGUMENT, null, 2),
        new LongOpt("mllp-tls", LongOpt.NO_ARGUMENT, null, 4),
        new LongOpt("mllp-tls.nodes", LongOpt.NO_ARGUMENT, null, 4),
        new LongOpt("mllp-tls.3des", LongOpt.NO_ARGUMENT, null, 4),
        new LongOpt("tls-key", LongOpt.REQUIRED_ARGUMENT, null, 2),
        new LongOpt("tls-key-passwd", LongOpt.REQUIRED_ARGUMENT, null, 2),
        new LongOpt("tls-cacerts", LongOpt.REQUIRED_ARGUMENT, null, 2),
        new LongOpt("tls-cacerts-passwd", LongOpt.REQUIRED_ARGUMENT, null, 2),
        new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h'),
        new LongOpt("version", LongOpt.NO_ARGUMENT, null, 'v'),
    };
    
    private static ResourceBundle messages = ResourceBundle.getBundle(
        "Hl7Rcv", Locale.getDefault());
    
    // Variables -----------------------------------------------------
    static final Logger log = Logger.getLogger(Hl7Rcv.class);
    static final ServerFactory sf = ServerFactory.getInstance();
    static final HL7Factory hl7f = HL7Factory.getInstance();

    private final HL7Handler handler = sf.newHL7Handler();
    private final Server server = sf.newServer(handler);

    private SSLContextAdapter tls = null;
    private String[] cipherSuites = null;
    private int port = 2300;
    
    // Constructors --------------------------------------------------
    Hl7Rcv(Configuration cfg) {
        port = Integer.parseInt(cfg.getProperty("port"));
        initServer(cfg);        
        initTLS(cfg);
    }
    
    private void initServer(Configuration cfg) {
        server.setMaxClients(
            Integer.parseInt(cfg.getProperty("max-clients", "10")));
        handler.setSoTimeout(
            Integer.parseInt(cfg.getProperty("so-timeout", "0")));
        handler.setReceivingApps(cfg.tokenize(
            cfg.getProperty("receiving-apps", null, "<any>", null)));
        handler.setSendingApps(cfg.tokenize(
            cfg.getProperty("sending-apps", null, "<any>", null)));
        
        handler.putService("ADT",this);
        handler.putService("ORM",this);
        handler.putService("ORU",this);
    }
    
    private void initTLS(Configuration cfg) {
        try {
            this.cipherSuites = MLLP_URL.toCipherSuites(
                cfg.getProperty("protocol", "mllp"));
            if (cipherSuites == null)
                return;
            
            tls = SSLContextAdapter.getInstance();
            char[] keypasswd = cfg.getProperty("key-passwd","dcm4che").toCharArray();
            tls.setKey(
                tls.loadKeyStore(
                Hl7Rcv.class.getResource(cfg.getProperty("tls-key","hl7rcv.key")),
                keypasswd),
                keypasswd);
            tls.setTrust(tls.loadKeyStore(
                Hl7Rcv.class.getResource(cfg.getProperty("tls-cacerts", "cacerts")),
                cfg.getProperty("tls-cacerts-passwd", "dcm4che").toCharArray()));
            tls.init();
        } catch (Exception ex) {
            throw new RuntimeException("Could not initalize TLS configuration - "
            + ex.getMessage());
        }
    }
        
    // Methods -------------------------------------------------------
    public void start() throws Exception {
        if (cipherSuites != null) {
            server.start(port, tls.getServerSocketFactory(cipherSuites));
        } else {
            server.start(port);
        }
    }

    public byte[] execute(byte[] msg) throws HL7Exception {
        HL7Message hl7 = hl7f.parse(msg);
        if (log.isDebugEnabled()) {
            log.debug("Received:\n" + hl7.toVerboseString());
        }
        byte[] ack = hl7.header().makeACK_AA();
        if (log.isDebugEnabled()) {
            log.debug("Send:\n" + hl7f.parse(ack).toVerboseString());
        }
        return ack;
    }
    
    public static void main(String args[]) throws Exception {
        Getopt g = new Getopt("hl7rcv", args, "", LONG_OPTS);
        
        Configuration cfg = new Configuration(
            Hl7Rcv.class.getResource("hl7rcv.cfg"));
        int c;
        while ((c = g.getopt()) != -1) {
            switch (c) {
                case 2:
                    cfg.put(LONG_OPTS[g.getLongind()].getName(), g.getOptarg());
                    break;
                case 4:
                    cfg.put("protocol", LONG_OPTS[g.getLongind()].getName());
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
            new Hl7Rcv(cfg).start();
        } catch (IllegalArgumentException e) {
            exit(e.getMessage(), true);
        }
    }

    private static void listConfig(Configuration cfg) {
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

    private static void exit(String prompt, boolean error) {
        if (prompt != null)
            System.err.println(prompt);
        if (error)
            System.err.println(messages.getString("try"));
        System.exit(1);
    }
    
}
