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
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.dcm4che2.config.NetworkConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision$ $Date$
 * @since Nov 24, 2005
 *
 */
class Connector
{
    static Logger log = LoggerFactory.getLogger(Connector.class);
    
    private final Device device;
    private final NetworkConnection config;
    private ServerSocket server;


    public Connector(Device device, NetworkConnection config)
    {
        if (device == null)
            throw new NullPointerException("device");
        if (config == null)
            throw new NullPointerException("config");
        this.device = device;
        this.config = config;
    }

    public final Device getDevice()
    {
        return device;
    }

    public final NetworkConnection getConfiguration()
    {
        return config;
    }
    
    public Socket connect(NetworkConnection peerConfig)
    throws IOException
    {
        if (!peerConfig.isListening())
            throw new IllegalArgumentException("Only initiates associations - " 
                    + peerConfig);
        Socket s = config.isTLS() ? createTLSSocket() : new Socket();
        if (config.getReceiveBufferSize() != NetworkConnection.DEFAULT)
            server.setReceiveBufferSize(config.getReceiveBufferSize());
        if (config.getSendBufferSize() != NetworkConnection.DEFAULT)
            s.setSendBufferSize(config.getSendBufferSize());
        s.setTcpNoDelay(config.isTcpNoDelay());
        s.bind(config.getSocketAddress(0));
        s.connect(peerConfig.getSocketAddress(), config.getConnectTimeout());
        return s;
    }
    
    public synchronized void bind()
    throws IOException
    {
        if (!config.isListening())
            throw new IllegalStateException("Only initiates associations - " 
                    + config);
        if (server != null)
            throw new IllegalStateException("Already listening - " + server);
        server = config.isTLS() ? createTLSServerSocket() : new ServerSocket();
        if (config.getReceiveBufferSize() != NetworkConnection.DEFAULT)
            server.setReceiveBufferSize(config.getReceiveBufferSize());
        server.bind(config.getSocketAddress(), config.getBacklog());
        device.getExecutor().execute(new Runnable(){

            public void run()
            {
                SocketAddress addr = server.getLocalSocketAddress();
                log.info("Start listening on " + addr);
                try
                {
                   for (;;)
                   {
                        Socket s = server.accept();
                        if (config.getSendBufferSize() != NetworkConnection.DEFAULT)
                            s.setSendBufferSize(config.getSendBufferSize());
                        s.setTcpNoDelay(config.isTcpNoDelay());
                        Association a = Association.accept(s, Connector.this);
                        device.getExecutor().execute(a);
                   }
                }
                catch (Throwable e)
                {
                    // assume exception was raised by graceful stop of server
                }
                log.info("Stop listening on " + addr);
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
        s.setEnabledProtocols(config.getTlsProtocol());
        s.setEnabledCipherSuites(config.getTlsCipherSuite());
        return s;
    }

    private ServerSocket createTLSServerSocket() throws IOException
    {
        SSLServerSocketFactory ssf = device.getSSLContext().getServerSocketFactory();
        SSLServerSocket ss = (SSLServerSocket) ssf.createServerSocket();
        ss.setEnabledProtocols(config.getTlsProtocol());
        ss.setEnabledCipherSuites(config.getTlsCipherSuite());
        ss.setNeedClientAuth(config.isTlsNeedClientAuth());
        return ss;
    }

    public final int getRequestTimeout()
    {
        return config.getRequestTimeout();
    }

    public final long getSocketCloseDelay()
    {
        return config.getSocketCloseDelay();
    }

    public final int getAcceptTimeout()
    {
        return config.getAcceptTimeout();
    }

}
