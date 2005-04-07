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

package org.dcm4cheri.server;

import org.dcm4che.server.Server;
import org.dcm4che.util.HandshakeFailedEvent;
import org.dcm4che.util.HandshakeFailedListener;

import org.dcm4cheri.util.LF_ThreadPool;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;

import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;

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
 * <p><b>20021105 Gunter Zeilinger:</b>
 * <ul>
 * <li> add property port and serverSocketFactory
 * <li> deprecate use of {@link #start(int)} 
 *                   and {@link #start(int,ServerSocketFactory)}
 * </ul>
 */
class ServerImpl implements LF_ThreadPool.Handler, Server {
    // Constants -----------------------------------------------------
    
    // Attributes ----------------------------------------------------
    static final Logger log = Logger.getLogger(ServerImpl.class);
    private static int instCount = 0;
    private final String name = "TCPServer-" + ++instCount;
    
    private final Handler handler;
    private LF_ThreadPool threadPool = new LF_ThreadPool(this, name);
    private ServerSocket ss;
    private int port = 104;
    private List hcl = null;
    private List hfl = null;
    private ServerSocketFactory ssf = ServerSocketFactory.getDefault();
    
    // Static --------------------------------------------------------
    
    // Constructors --------------------------------------------------
    public ServerImpl(Handler handler) {
        if (handler == null)
            throw new NullPointerException();
        
        if (handler == null)
            throw new NullPointerException();
        
        this.handler = handler;
    }
    
    // Public --------------------------------------------------------
    
    // Server implementation -----------------------------------------
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
    
    public void setMaxClients(int max) {
        threadPool.setMaxRunning(max);
    }
    
    public int getMaxClients() {
        return threadPool.getMaxRunning();
    }
    
    public int getNumClients() {
        return threadPool.running()-1;
    }
    
    public void setMaxIdleThreads(int max) {
        threadPool.setMaxWaiting(max);
    }

    public int getMaxIdleThreads() {
        return threadPool.getMaxWaiting();
    }

    public int getNumIdleThreads() {
        return threadPool.waiting();
    }
    
    public void start(int port) throws IOException {
        start(port, ServerSocketFactory.getDefault());
    }
    
    public void start(int port, ServerSocketFactory ssf) throws IOException {
        setPort(port);
        setServerSocketFactory(ssf);
        start();
    }
    
    public void start() throws IOException {
        checkNotRunning();
        if (log.isInfoEnabled())
            log.info("Start Server listening at port " + port);
        ss = ssf.createServerSocket(port);
        new Thread(new Runnable() {
            public void run() { threadPool.join(); }
        }, name).start();
    }
    
    public void stop() {
        if (ss == null)
            return;
        
        InetAddress ia = ss.getInetAddress();
        int port = ss.getLocalPort();
        if (log.isInfoEnabled())
            log.info("Stop Server listening at port " + port);
        try {
            ss.close();
        } catch (IOException ignore) {}
        
        // try to connect to server port to ensure to leave blocking accept
        /*try {
            new Socket(ia, port).close();
        } catch (IOException ignore) {}*/
        ss = null;
        threadPool.shutdown();
        threadPool = new LF_ThreadPool(this, name);
    }
    
    // LF_ThreadPool.Handler implementation --------------------------
    public void run(LF_ThreadPool pool) {
        if (ss == null)
            return;
        
        Socket s = null;
        try {
            s = ss.accept();
            if (log.isInfoEnabled()) {
                log.info("handle - " + s);
            }
            if (s instanceof SSLSocket) {
                init((SSLSocket) s);
            }
            
            pool.promoteNewLeader();
            handler.handle(s);
            if (!handler.isSockedClosedByHandler() && s != null) {
                try { s.close(); } catch (IOException ignore) {}
            }
        } catch (Exception e) {
            log.error(e);
            if (s != null) {
                try { s.close(); } catch (IOException ignore) {};
            }
        }
        if (log.isInfoEnabled()) {
            log.info("finished - " + s);
        }
    }
    
    // Y overrides ---------------------------------------------------
    
    // Package protected ---------------------------------------------
    
    // Protected -----------------------------------------------------
    
    // Private -------------------------------------------------------
    private void checkNotRunning() {
        if (ss != null) {
            throw new IllegalStateException("Already Running - " + threadPool);
        }
    }
    
    private void init(SSLSocket s) throws IOException {
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
                    cert.checkValidity();
                    log.info(s.getInetAddress().toString() + 
                        ": accept " + se.getCipherSuite() + " with "
                        + cert.getSubjectDN()+" valid from "+cert.getNotBefore()+" to "+cert.getNotAfter());
                } catch (SSLPeerUnverifiedException e) {
                    log.error("SSL peer not verified:",e);
                } catch ( CertificateException ce ) {
                	throw new IOException(ce.getMessage());
                }
            }
        } catch (IOException e) {
            if (hfl != null) {
                HandshakeFailedEvent event = new HandshakeFailedEvent(s,e);
                for (int i = 0, n = hfl.size(); i < n; ++i) {
                    ((HandshakeFailedListener) hfl.get(i)).handshakeFailed(event);
                }
                throw e;
            }
        }
    }
        
    /** Getter for property port.
     * @return Value of property port.
     *
     */
    public int getPort() {
        return port;
    }
    
    /** Setter for property port.
     * @param port New value of property port.
     *
     */
    public void setPort(int port) {
        if (port <= 0) {
            throw new IllegalArgumentException("port: " + port);
        }
        this.port = port;
    }
    
    /** Getter for property serverSocketFactory.
     * @return Value of property serverSocketFactory.
     *
     */
    public ServerSocketFactory getServerSocketFactory() {
        return ssf;
    }
    
    /** Setter for property serverSocketFactory.
     * @param serverSocketFactory New value of property serverSocketFactory.
     *
     */
    public void setServerSocketFactory(ServerSocketFactory ssf) {
        if (ssf == null) {
            throw new NullPointerException();
        }
        this.ssf = ssf;
    }
    
}
