/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Gunter Zeilinger, Huetteldorferstr. 24/10, 1150 Vienna/Austria/Europe.
 * Portions created by the Initial Developer are Copyright (C) 2002-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunterze@gmail.com>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

package org.dcm4che2.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Arrays;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision$ $Date$
 * @since Nov 24, 2005
 *
 */
public class NetworkConnection
{
    static Logger log = LoggerFactory.getLogger(NetworkConnection.class);
    public static final int DEFAULT = -1;
    
    private String commonName;
    private String hostname;
    private int port;
    private String[] tlsCipherSuite = {};
    private Boolean installed;
    
    private int backlog = 50;
    private int connectTimeout = 5000;
    private int requestTimeout = 5000;
    private int acceptTimeout = 5000;
    private int releaseTimeout = 5000;
    private int socketCloseDelay = 50;
    private int sendBufferSize = DEFAULT;
    private int receiveBufferSize = DEFAULT;
    private boolean tcpNoDelay = false;
    private boolean tlsNeedClientAuth = true;
    private String[] tlsProtocol = { 
            "TLSv1",
            "SSLv3",
//            "SSLv2Hello"
    };
    
    private Device device;
    private ServerSocket server;

    public String toString()
    {
        StringBuffer sb = new StringBuffer("NetworkConnection[");
        sb.append(getSocketAddress());
        if (tlsCipherSuite.length != 0)
            sb.append(", TLS").append(Arrays.asList(tlsCipherSuite));
        if (installed != null)
            sb.append(", installed=").append(installed);
        if (commonName != null)
            sb.append(", cn=").append(commonName);
        sb.append(']');
        return sb.toString();
    }
    
    public final Device getDevice()
    {
        return device;
    }

    final void setDevice(Device device)
    {
        this.device = device;
    }

    public final String getHostname()
    {
        return hostname;
    }

    public final void setHostname(String hostname)
    {
        this.hostname = hostname;
    }

    public final String getCommonName()
    {
        return commonName;
    }

    public final void setCommonName(String name)
    {
        this.commonName = name;
    }

    public final int getPort()
    {
        return port;
    }

    public final void setPort(int port)
    {
        this.port = port;
    }

    public final String[] getTlsCipherSuite()
    {
        return tlsCipherSuite;
    }

    public final void setTlsCipherSuite(String[] tlsCipherSuite)
    {
        checkNotNull("tlsCipherSuite", tlsCipherSuite);
        this.tlsCipherSuite = tlsCipherSuite;
    }

    private static void checkNotNull(String name, Object[] a)
    {
        if (a == null)
            throw new NullPointerException(name);
        for (int i = 0; i < a.length; i++)
            if (a[i] == null)
                throw new NullPointerException(name + '[' + i + ']');        
    }
    
    public final boolean isInstalled()
    {
        return installed != null ? installed.booleanValue() 
                                 : device == null || device.isInstalled();
    }

    public final void setInstalled(boolean installed)
    {
        this.installed = Boolean.valueOf(installed);
    }
    
    public boolean isListening()
    {
        return port > 0;
    }

    public boolean isTLS()
    {
        return tlsCipherSuite.length > 0;
    }

    public final int getBacklog()
    {
        return backlog;
    }

    public final void setBacklog(int backlog)
    {
        this.backlog = backlog;
    }

    public final int getAcceptTimeout()
    {
        return acceptTimeout;
    }

    public final void setAcceptTimeout(int timeout)
    {
        this.acceptTimeout = timeout;
    }

    public final int getConnectTimeout()
    {
        return connectTimeout;
    }

    public final void setConnectTimeout(int timeout)
    {
        this.connectTimeout = timeout;
    }

    public final int getRequestTimeout()
    {
        return requestTimeout;
    }

    public final void setRequestTimeout(int timeout)
    {
        this.requestTimeout = timeout;
    }

    public final int getReleaseTimeout()
    {
        return releaseTimeout;
    }

    public final void setReleaseTimeout(int timeout)
    {
        this.releaseTimeout = timeout;
    }

    public final int getSocketCloseDelay()
    {
        return socketCloseDelay;
    }

    public final void setSocketCloseDelay(int delay)
    {
        this.socketCloseDelay = delay;
    }

    public final int getReceiveBufferSize()
    {
        return receiveBufferSize;
    }

    public final void setReceiveBufferSize(int size)
    {
        this.receiveBufferSize = size;
    }

    public final int getSendBufferSize()
    {
        return sendBufferSize;
    }

    public final void setSendBufferSize(int size)
    {
        this.sendBufferSize = size;
    }
    
    public final boolean isTcpNoDelay()
    {
        return tcpNoDelay;
    }

    public final void setTcpNoDelay(boolean tcpNoDelay)
    {
        this.tcpNoDelay = tcpNoDelay;        
    }

    public final boolean isTlsNeedClientAuth()
    {
        return tlsNeedClientAuth;
    }

    public final void setTlsNeedClientAuth(boolean tlsNeedClientAuth)
    {
        this.tlsNeedClientAuth = tlsNeedClientAuth;
    }

    public final String[] getTlsProtocol()
    {
        return tlsProtocol;
    }

    public final void setTlsProtocol(String[] tlsProtocol)
    {
        this.tlsProtocol = tlsProtocol;
    }

    private InetSocketAddress getSocketAddress()
    {
        return getSocketAddress(port);
    }
    
    private InetSocketAddress getSocketAddress(int port)
    {
        return hostname == null ? new InetSocketAddress(port)
                                : new InetSocketAddress(hostname, port);
    }
    
    public Socket connect(NetworkConnection peerConfig)
    throws IOException
    {
        if (device == null)
            throw new IllegalStateException("Device not initalized");
        if (!peerConfig.isListening())
            throw new IllegalArgumentException("Only initiates associations - " 
                    + peerConfig);
        Socket s = isTLS() ? createTLSSocket() : new Socket();
        if (receiveBufferSize != DEFAULT)
            server.setReceiveBufferSize(receiveBufferSize);
        if (sendBufferSize != DEFAULT)
            s.setSendBufferSize(sendBufferSize);
        s.setTcpNoDelay(tcpNoDelay);
        s.bind(getSocketAddress(0));
        log.debug("Initiate connection to {}", peerConfig.getSocketAddress());
        s.connect(peerConfig.getSocketAddress(), connectTimeout);
        return s;
    }
    
    public synchronized void bind(final Executor executor)
    throws IOException
    {
        if (device == null)
            throw new IllegalStateException("Device not initalized");
        if (!isListening())
            throw new IllegalStateException("Only initiates associations - " 
                    + this);
        if (server != null)
            throw new IllegalStateException("Already listening - " + server);
        server = isTLS() ? createTLSServerSocket() : new ServerSocket();
        if (receiveBufferSize != DEFAULT)
            server.setReceiveBufferSize(receiveBufferSize);
        server.bind(getSocketAddress(), getBacklog());
        executor.execute(new Runnable(){

            public void run()
            {
                SocketAddress addr = server.getLocalSocketAddress();
                log.info("Start listening on {}", addr);
                try
                {
                   for (;;)
                   {
                        log.debug("Wait for connection on {}", addr);
                        Socket s = server.accept();
                        if (sendBufferSize != DEFAULT)
                            s.setSendBufferSize(sendBufferSize);
                        s.setTcpNoDelay(tcpNoDelay);
                        Association a = Association.accept(s, NetworkConnection.this);
                        executor.execute(a);
                   }
                }
                catch (Throwable e)
                {
                    // assume exception was raised by graceful stop of server
                }
                log.info("Stop listening on {}", addr);
            }});
    }

    public synchronized void unbind()
    {
        if (server == null)
            return;
        try { server.close(); }
        catch (Throwable e) { }
        server = null;
    }
    
    private Socket createTLSSocket() throws IOException
    {
        SSLSocketFactory sf = device.getSSLContext().getSocketFactory();
        SSLSocket s = (SSLSocket) sf.createSocket();
        s.setEnabledProtocols(tlsProtocol);
        s.setEnabledCipherSuites(tlsCipherSuite);
        return s;
    }

    private ServerSocket createTLSServerSocket() throws IOException
    {
        SSLServerSocketFactory ssf = device.getSSLContext().getServerSocketFactory();
        SSLServerSocket ss = (SSLServerSocket) ssf.createServerSocket();
        ss.setEnabledProtocols(tlsProtocol);
        ss.setEnabledCipherSuites(tlsCipherSuite);
        ss.setNeedClientAuth(tlsNeedClientAuth);
        return ss;
    }

}
