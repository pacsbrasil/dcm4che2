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

package org.dcm4cheri.util;

import org.dcm4che.util.SSLContextAdapter;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.Arrays;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.Security;
import javax.net.SocketFactory;
import javax.net.ServerSocketFactory;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;

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
 * <p><b>20020804 gunter:</b>
 * <ul>
 * <li> make enabledCipherSuites param of SocketFactory instead of Adapter
 * </ul>
 */
public class SSLContextAdapterImpl extends SSLContextAdapter {
    // Constants -----------------------------------------------------
    
    // Attributes ----------------------------------------------------
    static final Logger log = Logger.getLogger(SSLContextAdapterImpl.class);
    
    private String[] protocols = {
        "TLSv1",
        //    "SSLv3",
        //    "SSLv2Hello"
    };

    private String[] cipherSuites = {
        // "SSL_RSA_WITH_3DES_EDE_CBC_SHA",
        "SSL_RSA_WITH_NULL_SHA"
    };
    
    private final SSLContext ctx;
    private final KeyManagerFactory kmf;
    private final TrustManagerFactory tmf;
    private SecureRandom random = null;
    private KeyManager[] kms = null;
    private TrustManager[] tms = null;
    private boolean needClientAuth = true;
    private boolean startHandshake = true;
    private SSLServerSocket unboundSSLServerSocket = null;
    
    // Static --------------------------------------------------------
    public static void main(String[] args) throws Exception {
        SSLContextAdapter inst = new SSLContextAdapterImpl();
        System.out.println("SupportedCipherSuites"
            + Arrays.asList(inst.getSupportedCipherSuites()));
        System.out.println("SupportedProtocols"
            + Arrays.asList(inst.getSupportedProtocols()));
    }
    
    // Constructors --------------------------------------------------
    public SSLContextAdapterImpl() {
        Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
        try {
            ctx = SSLContext.getInstance("TLS");
            kmf = KeyManagerFactory.getInstance(
                KeyManagerFactory.getDefaultAlgorithm());
            tmf = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm());
        } catch (GeneralSecurityException e) {
            throw new ConfigurationError("could not instantiate SSLContext", e);
        }
    }
    
    // Public --------------------------------------------------------
    public String toString() {
        return ctx.toString();
    }
    
    // SSLContextAdapter implementation ------------------------------
    public final SSLContext getSSLContext() {
        return ctx;
    }
    
    public void setKey(KeyStore key, char[] password)
    throws GeneralSecurityException {
        kmf.init(key, password);
        kms = kmf.getKeyManagers();
    }
        
    public void setTrust(KeyStore cacerts)
    throws GeneralSecurityException {
        tmf.init(cacerts);
        tms = tmf.getTrustManagers();
    }
    
    public KeyManager[] getKeyManagers() {
        return kms;
    }
        
    public TrustManager[] getTrustManagers() {
        return tms;
    }

    public void seedRandom(long seed) {
        try {
            random = SecureRandom.getInstance("SHA1PRNG");
            random.setSeed(seed);
        } catch (GeneralSecurityException e) {
            throw new ConfigurationError(
                "could not instantiate SecureRandom", e);
        }
    }
    
    public void setNeedClientAuth(boolean needClientAuth) {
        this.needClientAuth = needClientAuth;
    }
    
    public boolean isNeedClientAuth() {
        return needClientAuth;
    }
    
    public void setStartHandshake(boolean startHandshake) {
        this.startHandshake = startHandshake;
    }
    
    public void setEnabledProtocols(String[] protocols) {
        this.protocols = (String[])protocols.clone();
    }
    
    public String[] getEnabledProtocols() {
        return (String[])protocols.clone();
    }
    
    public void setEnabledCipherSuites(String[] cipherSuites) {
        this.cipherSuites = (String[])cipherSuites.clone();
    }
    
    public String[] getEnabledCipherSuites() {
        return (String[])cipherSuites.clone();
    }
    
    private SSLServerSocket getUnboundSSLServerSocket() {
        if (unboundSSLServerSocket != null) {
            return unboundSSLServerSocket;
        }
        try {
            ServerSocketFactory factory = ctx.getServerSocketFactory();
            unboundSSLServerSocket = (SSLServerSocket)factory.createServerSocket();
        } catch (IOException e) {
            throw new ConfigurationError(
                "could not create unbounded ServerSocket", e);
        }
        return unboundSSLServerSocket;
    }
    
    public String[] getSupportedCipherSuites() {
        return getUnboundSSLServerSocket().getSupportedCipherSuites();
    }
    
    public String[] getSupportedProtocols() {
        return getUnboundSSLServerSocket().getSupportedProtocols();
    }
    
    public ServerSocketFactory getServerSocketFactory() {
        return new SSLServerSocketFactoryAdapter(cipherSuites);
    }

    public ServerSocketFactory getServerSocketFactory(String[] cipherSuites) {
        return new SSLServerSocketFactoryAdapter(cipherSuites);
    }
    
    public SocketFactory getSocketFactory() {
        return new SSLSocketFactoryAdapter(cipherSuites);
    }
    
    public SocketFactory getSocketFactory(String[] cipherSuites) {
        return new SSLSocketFactoryAdapter(cipherSuites);
    }
    
    public void init() throws GeneralSecurityException {
        ctx.init(kms, tms, random);
    }
    
    // Package protected ---------------------------------------------
    
    // Protected -----------------------------------------------------
    
    // Private -------------------------------------------------------
    
    public static String toInfoMsg(SSLSocket ssl, boolean local)
    throws IOException {
        return toInfoMsg(ssl, local, new StringBuffer()).toString();
    }
    
    private static StringBuffer toInfoMsg(SSLSocket ssl, boolean local,
    StringBuffer sb)
    throws IOException {
        sb.append(ssl.getInetAddress());
        sb.append(local ? " << " : " >> ");
        SSLSession se = ssl.getSession();
        sb.append(se.getCipherSuite());
        sb.append("[");
        Certificate[] certs = local ? se.getLocalCertificates()
        : se.getPeerCertificates();
        if (certs.length == 0) {
            sb.append("null");
        } else if (certs[0] instanceof X509Certificate) {
            sb.append(((X509Certificate)certs[0]).getSubjectX500Principal());
        } else {
            sb.append(certs[0].getType());
        }
        sb.append("]");
        return sb;
    }
    
    private String toKeyStoreType(String fname) {
        return fname.endsWith(".p12") 
            || fname.endsWith(".P12") 
            ? "PKCS12" : "JKS";
    }
    
    public KeyStore loadKeyStore(URL url, char[] password) throws GeneralSecurityException, IOException {
        InputStream in = url.openStream();
        try {
            return loadKeyStore(in, password, toKeyStoreType(url.getFile()));
        } finally {
            try { in.close(); } catch (IOException ignore) {}
        }
    }
    
    public KeyStore loadKeyStore(File file, char[] password)
    throws GeneralSecurityException, IOException {
        InputStream in = new BufferedInputStream(new FileInputStream(file));
        try {
            return loadKeyStore(in, password, toKeyStoreType(file.getName()));
        } finally {
            try { in.close(); } catch (IOException ignore) {}
        }
    }
    
    public KeyStore loadKeyStore(InputStream in, char[] password, String type)
    throws GeneralSecurityException, IOException {
        KeyStore key = KeyStore.getInstance(type);
        key.load(in, password);
        return key;
    }
    
    // Inner classes -------------------------------------------------
    private class SSLServerSocketFactoryAdapter extends ServerSocketFactory {
        
        final SSLServerSocketFactory ssf;
        final String[] cipherSuites;
        
        SSLServerSocketFactoryAdapter(String[] cipherSuites) {
            ssf = getSSLContext().getServerSocketFactory();
            this.cipherSuites = cipherSuites != null
                ? (String[]) cipherSuites.clone() : null;
        }
        
        public ServerSocket createServerSocket(int port, int backlog)
        throws IOException {
            return init((SSLServerSocket)ssf.createServerSocket(port, backlog));
        }
        
        public ServerSocket createServerSocket(int port, int backlog,
        InetAddress ia) throws IOException {
            return init(
                (SSLServerSocket)ssf.createServerSocket(port, backlog, ia));
        }
        
        public ServerSocket createServerSocket(int port) throws IOException {
            return init((SSLServerSocket)ssf.createServerSocket(port));
        }
        
        private ServerSocket init(SSLServerSocket ss) {
            ss.setNeedClientAuth(isNeedClientAuth());
            ss.setEnabledProtocols(getEnabledProtocols());
            if (cipherSuites != null) {
                ss.setEnabledCipherSuites(cipherSuites);
            }
            return ss;
        }
    }
    
        
    private class SSLSocketFactoryAdapter extends SocketFactory {
        
        final SSLSocketFactory sf;
        final String[] cipherSuites;
        
        SSLSocketFactoryAdapter(String[] cipherSuites) {
            sf = getSSLContext().getSocketFactory();
            this.cipherSuites = cipherSuites != null
                ? (String[]) cipherSuites.clone() : null;
        }
        
        public Socket createSocket(InetAddress ia, int port)
        throws IOException {
            return init((SSLSocket)sf.createSocket(ia, port));
        }
        
        public Socket createSocket(InetAddress ia, int port,
            InetAddress clientIA, int clientPort)
        throws IOException {
            return init(
                (SSLSocket)sf.createSocket(ia, port, clientIA, clientPort));
        }
        
        public Socket createSocket(String host, int port,
            InetAddress clientIA, int clientPort)
        throws IOException {
            return init(
                (SSLSocket)sf.createSocket(host, port, clientIA, clientPort));
        }
        
        public Socket createSocket(String host, int port) throws IOException {
            return init((SSLSocket)sf.createSocket(host, port));
        }
        
        private Socket init(SSLSocket s) throws IOException {
            if (cipherSuites != null) {
                s.setEnabledCipherSuites(cipherSuites);
            }
            s.setEnabledProtocols(getEnabledProtocols());
            if (startHandshake) {
                s.startHandshake();
            }
            if (log.isInfoEnabled()) {
                log.info(toInfoMsg(s, true));
                log.info(toInfoMsg(s, false));
            }
            return s;
        }
    }

    static class ConfigurationError extends Error {
        ConfigurationError(String msg, Exception x) {
            super(msg,x);
        }
    }
}
