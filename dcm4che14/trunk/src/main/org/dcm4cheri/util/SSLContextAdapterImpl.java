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
import java.util.logging.Level;
import java.util.logging.Logger;
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
public class SSLContextAdapterImpl extends SSLContextAdapter {
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   static final Logger log = Logger.getLogger("dcm4che.net");

   private String[] cipherSuites = {
      "SSL_RSA_WITH_NULL_SHA"
   };

   private String[] protocols = {
      "TLSv1",
//    "SSLv3",
//    "SSLv2Hello"
   };
   
   private SecureRandom random = null;
   private KeyManager[] kms = null;
   private TrustManager[] tms = null;
   private boolean needClientAuth = true;
   private boolean startHandshake = true;
   
   // Static --------------------------------------------------------
   public static void main(String[] args) throws Exception {
      SSLContextAdapter inst = new SSLContextAdapterImpl();
      System.out.println(Arrays.asList(inst.getSupportedCipherSuites()));
   }
   
   // Constructors --------------------------------------------------
   public SSLContextAdapterImpl() {
      Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
   }
   
   // Public --------------------------------------------------------
   public String toString() {
      return "CipherSuites" + Arrays.asList(cipherSuites);
   }
   
   // SSLContextAdapter implementation ------------------------------
   public void setKey(KeyStore key, char[] password)
   throws GeneralSecurityException {
      KeyManagerFactory kmf = KeyManagerFactory.getInstance(
            KeyManagerFactory.getDefaultAlgorithm());
      kmf.init(key, password);
      kms = kmf.getKeyManagers();
   }
   
   public void setTrust(KeyStore cacerts)
   throws GeneralSecurityException {
      TrustManagerFactory tmf = TrustManagerFactory.getInstance(
           TrustManagerFactory.getDefaultAlgorithm());
      tmf.init(cacerts);
      tms = tmf.getTrustManagers();
   }
   
   public void seedRandom(long seed)
   throws GeneralSecurityException {
      random = SecureRandom.getInstance("SHA1PRNG");
      random.setSeed(seed);
   }
   
   public void setNeedClientAuth(boolean needClientAuth) {
      this.needClientAuth = needClientAuth;
   }
   
   public void setStartHandshake(boolean startHandshake) {
      this.startHandshake = startHandshake;
   }

   public void setEnabledCipherSuites(String[] cipherSuites) {
      this.cipherSuites = (String[])cipherSuites.clone();
   }

   public String[] getEnabledCipherSuites() {
      return (String[])cipherSuites.clone();
   }

   public void setEnabledProtocols(String[] protocols) {
      this.protocols = (String[])protocols.clone();
   }

   public String[] getEnabledProtocols() {
      return (String[])protocols.clone();
   }
   
   public String[] getSupportedCipherSuites()
   throws GeneralSecurityException {
      SSLContext ctx = SSLContext.getInstance("TLS");
      SSLSocketFactory factory =
            (SSLSocketFactory)ctx.getSocketFactory();
      return factory.getSupportedCipherSuites();
   }

   public ServerSocketFactory getServerSocketFactory()
   throws GeneralSecurityException {
      return new SSLServerSocketFactoryAdapter();
   }
   
   public SocketFactory getSocketFactory()
   throws GeneralSecurityException {
      return new SSLSocketFactoryAdapter();
   }
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   private SSLContext getContext() throws GeneralSecurityException
   {
      SSLContext ctx = SSLContext.getInstance("TLS");
      ctx.init(kms, tms, random);
      return ctx;
   }
   
   private ServerSocket prepare(SSLServerSocket ss) {
      ss.setNeedClientAuth(needClientAuth);
      ss.setEnabledCipherSuites(cipherSuites);
      ss.setEnabledProtocols(protocols);
      return ss;
   }
   
   private Socket prepare(SSLSocket s) throws IOException {
      s.setEnabledCipherSuites(cipherSuites);
      s.setEnabledProtocols(protocols);
      if (startHandshake) {
         s.startHandshake();
      }
      if (log.isLoggable(Level.INFO)) {
         log.info(toInfoMsg(s, true));
         log.info(toInfoMsg(s, false));
      }
      return s;
   }
   
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

   public KeyStore loadKeyStore(URL url, char[] password) throws GeneralSecurityException, IOException {
      InputStream in = url.openStream();
      try {
         return loadKeyStore(in, password);
      } finally {
         try { in.close(); } catch (IOException ignore) {}
      }
   }
   
   public KeyStore loadKeyStore(File file, char[] password)
   throws GeneralSecurityException, IOException {
      InputStream in = new BufferedInputStream(new FileInputStream(file));
      try {
         return loadKeyStore(in, password);
      } finally {
         try { in.close(); } catch (IOException ignore) {}
      }
   }
   
   public KeyStore loadKeyStore(InputStream in, char[] password)
   throws GeneralSecurityException, IOException {
      KeyStore key = KeyStore.getInstance(KeyStore.getDefaultType());
      key.load(in, password);
      return key;
   }
   
   // Inner classes -------------------------------------------------
   private class SSLServerSocketFactoryAdapter extends ServerSocketFactory {
      
      final SSLServerSocketFactory ssf;
      
      SSLServerSocketFactoryAdapter() throws GeneralSecurityException {
         ssf = getContext().getServerSocketFactory();
      }
      
      public ServerSocket createServerSocket(int port, int backlog)
      throws IOException {
         return prepare((SSLServerSocket)ssf.createServerSocket(port, backlog));
      }
      
      public ServerSocket createServerSocket(int port, int backlog,
            InetAddress ia) throws IOException {
         return prepare(
               (SSLServerSocket)ssf.createServerSocket(port, backlog, ia));
      }
      
      public ServerSocket createServerSocket(int port) throws IOException {
         return prepare((SSLServerSocket)ssf.createServerSocket(port));
      }
   }
   
   private class SSLSocketFactoryAdapter extends SocketFactory {
      
      final SSLSocketFactory sf;
      
      SSLSocketFactoryAdapter() throws GeneralSecurityException {
         sf = getContext().getSocketFactory();
      }
      
      public Socket createSocket(InetAddress ia, int port)
      throws IOException {
         return prepare((SSLSocket)sf.createSocket(ia, port));
      }
      
      public Socket createSocket(InetAddress ia, int port,
            InetAddress clientIA, int clientPort) 
      throws IOException {
         return prepare(
               (SSLSocket)sf.createSocket(ia, port, clientIA, clientPort));
      }
      
      public Socket createSocket(String host, int port,
            InetAddress clientIA, int clientPort) throws IOException {
         return prepare(
               (SSLSocket)sf.createSocket(host, port, clientIA, clientPort));
      }
      
      public Socket createSocket(String host, int port) throws IOException {
         return prepare((SSLSocket)sf.createSocket(host, port));
      }
   }
}
