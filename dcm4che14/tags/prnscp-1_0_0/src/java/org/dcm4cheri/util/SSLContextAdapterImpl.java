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
import org.dcm4che.util.HandshakeFailedListener;
import org.dcm4che.util.HandshakeFailedEvent;

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
import java.util.ArrayList;
import java.util.List;
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
import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLPeerUnverifiedException;
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
/*
    private String[] cipherSuites = {
        "SSL_RSA_WITH_3DES_EDE_CBC_SHA",
        "SSL_RSA_WITH_NULL_SHA"
    };
  */  
    private final SSLContext ctx;
    private final KeyManagerFactory kmf;
    private final TrustManagerFactory tmf;
    private SecureRandom random = null;
    private KeyManager[] kms = null;
    private TrustManager[] tms = null;
    private boolean needClientAuth = true;
    private SSLServerSocket unboundSSLServerSocket = null;
    private List hcl = null;
    private List hfl = null;
    
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
    public void addHandshakeCompletedListener(
            HandshakeCompletedListener listener) {
         hcl = addToList(hcl, listener);
    }

    public void addHandshakeFailedListener(
            HandshakeFailedListener listener) {
         hfl = addToList(hfl, listener);
    }
    
    public void removeHandshakeCompletedListener(
            HandshakeCompletedListener listener) {
         hcl = removeFromList(hcl, listener);
    }

    public void removeHandshakeFailedListener(
            HandshakeFailedListener listener) {
         hfl = removeFromList(hfl, listener);
    }
     // Listeners

    // Add an element to a list, creating a new list if the
    // existing list is null, and return the list.
    static List addToList(List l, Object elt) {
        if (l == null) {
            l = new ArrayList();
        }
        l.add(elt);
        return l;
    }


    // Remove an element from a list, discarding the list if the
    // resulting list is empty, and return the list or null.
    static List removeFromList(List l, Object elt) {
        if (l == null) {
            return l;
        }
        l.remove(elt);
        if (l.size() == 0) {
            l = null;
        }
        return l;
    }
    
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
    
    public void setEnabledProtocols(String[] protocols) {
        this.protocols = (String[])protocols.clone();
    }
    
    public String[] getEnabledProtocols() {
        return (String[])protocols.clone();
    }
/*    
    public void setEnabledCipherSuites(String[] cipherSuites) {
        this.cipherSuites = (String[])cipherSuites.clone();
    }
    
    public String[] getEnabledCipherSuites() {
        return (String[])cipherSuites.clone();
    }
  */  
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
/*    
    public ServerSocketFactory getServerSocketFactory() {
        return new SSLServerSocketFactoryAdapter(cipherSuites);
    }
*/
    public ServerSocketFactory getServerSocketFactory(String[] cipherSuites) {
        return new SSLServerSocketFactoryAdapter(cipherSuites);
    }
/*    
    public SocketFactory getSocketFactory() {
        return new SSLSocketFactoryAdapter(cipherSuites);
    }
*/    
    public SocketFactory getSocketFactory(String[] cipherSuites) {
        return new SSLSocketFactoryAdapter(cipherSuites);
    }
    
    public void init() throws GeneralSecurityException {
        ctx.init(kms, tms, random);
    }
    
    // Package protected ---------------------------------------------
    
    // Protected -----------------------------------------------------
    
    // Private -------------------------------------------------------    
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
            if (hcl != null) {
                for (int i = 0, n = hcl.size(); i < n; ++i) {
                    s.addHandshakeCompletedListener(
                        (HandshakeCompletedListener) hcl.get(i));
                }
            }
            try {
                s.startHandshake();
                if (log.isInfoEnabled()) {
                    SSLSession se = s.getSession();
                    try {
                        X509Certificate cert = (X509Certificate)
                            se.getPeerCertificates()[0];
                        log.info(s.getInetAddress().toString() + 
                            ": accept " + se.getCipherSuite() + " with "
                            + cert.getSubjectDN());
                    } catch (SSLPeerUnverifiedException e) {
                        log.error("SSL peer not verified:",e);
                    }
                }
            } catch (IOException e) {
                if (hfl != null) {
                    HandshakeFailedEvent event = new HandshakeFailedEvent(s,e);
                    for (int i = 0, n = hfl.size(); i < n; ++i) {
                        ((HandshakeFailedListener) hfl.get(i)).handshakeFailed(event);
                    }
                }
                throw e;
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
