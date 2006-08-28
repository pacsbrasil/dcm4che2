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
 * Damien Evans <damien.daddy@gmail.com>
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
import java.net.SocketException;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A DICOM Part 15, Annex H compliant class, <code>NetworkConnection</code>
 * encapsulates the properties associated with a connection to a TCP/IP network.
 * <p>
 * The <i>network connection</i> describes one TCP port on one network device. This
 * can be used for a TCP connection over which a DICOM association can be
 * negotiated with one or more Network AEs. It specifies 8 the hostname and TCP
 * port number. A network connection may support multiple Network AEs. The
 * Network AE selection takes place during association negotiation based on the
 * called and calling AE-titles.
 * 
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision$ $Date$
 * @since Nov 24, 2005
 */
public class NetworkConnection
{
    static Logger log = LoggerFactory.getLogger(NetworkConnection.class);
    public static final int DEFAULT = 0;

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
    private boolean tcpNoDelay = true;
    private boolean tlsNeedClientAuth = true;
    private String[] tlsProtocol = { "TLSv1", "SSLv3",
    // "SSLv2Hello"
    };

    private Device device;
    private ServerSocket server;

    // Limiting factors
    private List excludeConnectionsFrom;
    private int maxScpAssociations = 50;
    private int associationCount;

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
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

    /**
     * Get the <code>Device</code> object that this Network Connection belongs to.
     * 
     * @return Device
     */
    public final Device getDevice() {
        return device;
    }

    /**
     * Set the <code>Device</code> object that this Network Connection belongs to.
     * 
     * @param device The owning <code>Device</code> object.
     */
    final void setDevice(Device device) {
        this.device = device;
    }

    public final String getHostname() {
        return hostname;
    }

    public final void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public final String getCommonName() {
        return commonName;
    }

    public final void setCommonName(String name) {
        this.commonName = name;
    }

    public final int getPort() {
        return port;
    }

    public final void setPort(int port) {
        this.port = port;
    }

    public final String[] getTlsCipherSuite() {
        return tlsCipherSuite;
    }

    public final void setTlsCipherSuite(String[] tlsCipherSuite) {
        checkNotNull("tlsCipherSuite", tlsCipherSuite);
        this.tlsCipherSuite = tlsCipherSuite;
    }

    private static void checkNotNull(String name, Object[] a) {
        if (a == null)
            throw new NullPointerException(name);
        for (int i = 0; i < a.length; i++)
            if (a[i] == null)
                throw new NullPointerException(name + '[' + i + ']');
    }

    public final boolean isInstalled() {
        return installed != null ? installed.booleanValue() : device == null
                || device.isInstalled();
    }

    public final void setInstalled(boolean installed) {
        this.installed = Boolean.valueOf(installed);
    }

    public boolean isListening() {
        return port > 0;
    }

    public boolean isTLS() {
        return tlsCipherSuite.length > 0;
    }

    public final int getBacklog() {
        return backlog;
    }

    public final void setBacklog(int backlog) {
        if (backlog < 1)
            throw new IllegalArgumentException("backlog: " + backlog);
        this.backlog = backlog;
    }

    public final int getAcceptTimeout() {
        return acceptTimeout;
    }

    public final void setAcceptTimeout(int timeout) {
        if (timeout < 0)
            throw new IllegalArgumentException("timeout: " + timeout);
        this.acceptTimeout = timeout;
    }

    public final int getConnectTimeout() {
        return connectTimeout;
    }

    public final void setConnectTimeout(int timeout) {
        if (timeout < 0)
            throw new IllegalArgumentException("timeout: " + timeout);
        this.connectTimeout = timeout;
    }

    /**
     * Timeout in ms for receiving A-ASSOCIATE-RQ, 5000 by default
     * 
     * @param An int value containing the milliseconds.
     */
    public final int getRequestTimeout() {
        return requestTimeout;
    }

    /**
     * Timeout in ms for receiving A-ASSOCIATE-RQ, 5000 by default
     * 
     * @param timeout An int value containing the milliseconds.
     */
    public final void setRequestTimeout(int timeout) {
        if (timeout < 0)
            throw new IllegalArgumentException("timeout: " + timeout);
        this.requestTimeout = timeout;
    }

    /**
     * Timeout in ms for receiving A-RELEASE-RP, 5000 by default.
     * 
     * @return An int value containing the milliseconds.
     */
    public final int getReleaseTimeout() {
        return releaseTimeout;
    }

    /**
     * Timeout in ms for receiving A-RELEASE-RP, 5000 by default.
     * 
     * @param timeout An int value containing the milliseconds.
     */
    public final void setReleaseTimeout(int timeout) {
        if (timeout < 0)
            throw new IllegalArgumentException("timeout: " + timeout);
        this.releaseTimeout = timeout;
    }

    /**
     * Delay in ms for Socket close after sending A-ABORT, 50ms by default.
     * 
     * @return An int value containing the milliseconds.
     */
    public final int getSocketCloseDelay() {
        return socketCloseDelay;
    }

    /**
     * Delay in ms for Socket close after sending A-ABORT, 50ms by default.
     * 
     * @param delay An int value containing the milliseconds.
     */
    public final void setSocketCloseDelay(int delay) {
        if (delay < 0)
            throw new IllegalArgumentException("delay: " + delay);
        this.socketCloseDelay = delay;
    }

    /**
     * Get the SO_RCVBUF socket value in KB.
     * 
     * @return An int value containing the buffer size in KB.
     */
    public final int getReceiveBufferSize() {
        return receiveBufferSize;
    }

    /**
     * Set the SO_RCVBUF socket option to specified value in KB.
     * 
     * @param bufferSize An int value containing the buffer size in KB.
     */
    public final void setReceiveBufferSize(int size) {
        if (size < 0)
            throw new IllegalArgumentException("size: " + size);
        this.receiveBufferSize = size;
    }

    /**
     * Get the SO_SNDBUF socket option value in KB,
     * 
     * @return An int value containing the buffer size in KB.
     */
    public final int getSendBufferSize() {
        return sendBufferSize;
    }

    /**
     * Set the SO_SNDBUF socket option to specified value in KB,
     * 
     * @param bufferSize An int value containing the buffer size in KB.
     */
    public final void setSendBufferSize(int size) {
        if (size < 0)
            throw new IllegalArgumentException("size: " + size);
        this.sendBufferSize = size;
    }

    /**
     * Determine if this network connection is using Nagle's algorithm as part of its
     * network communication.
     * 
     * @return boolean True if TCP no delay (Nagle's algorithm) is being used.
     */
    public final boolean isTcpNoDelay() {
        return tcpNoDelay;
    }

    /**
     * Set whether or not this network connection should use Nagle's algorithm as part of its
     * network communication.
     * 
     * @param tcpNoDelay boolean True if TCP no delay (Nagle's algorithm) should be used.
     */
    public final void setTcpNoDelay(boolean tcpNoDelay) {
        this.tcpNoDelay = tcpNoDelay;
    }

    public final boolean isTlsNeedClientAuth() {
        return tlsNeedClientAuth;
    }

    public final void setTlsNeedClientAuth(boolean tlsNeedClientAuth) {
        this.tlsNeedClientAuth = tlsNeedClientAuth;
    }

    public final String[] getTlsProtocol()  {
        return tlsProtocol;
    }

    public final void setTlsProtocol(String[] tlsProtocol) {
        this.tlsProtocol = tlsProtocol;
    }

    private InetSocketAddress getSocketAddress() {
        return getSocketAddress(port);
    }

    private InetSocketAddress getSocketAddress(int newPort) {
        return hostname == null ? new InetSocketAddress(newPort)
                : new InetSocketAddress(hostname, newPort);
    }

    public Socket connect(NetworkConnection peerConfig) throws IOException {
        if (device == null)
            throw new IllegalStateException("Device not initalized");
        if (!peerConfig.isListening())
            throw new IllegalArgumentException("Only initiates associations - "
                    + peerConfig);
        Socket s = isTLS() ? createTLSSocket() : new Socket();
        s.bind(getSocketAddress(0));
        setSocketOptions(s);
        log.debug("Initiate connection to {}", peerConfig.getSocketAddress());
        s.connect(peerConfig.getSocketAddress(), connectTimeout);
        return s;
    }

    protected void setSocketOptions(Socket s) throws SocketException {
        int size;
        size = s.getReceiveBufferSize();
        if (receiveBufferSize == DEFAULT) {
            receiveBufferSize = size;
        } else if (receiveBufferSize != size) {
            s.setReceiveBufferSize(size);
            receiveBufferSize = s.getReceiveBufferSize();
        }
        size = s.getSendBufferSize();
        if (sendBufferSize == DEFAULT) {
            sendBufferSize = size;
        } else if (sendBufferSize != size) {
            s.setSendBufferSize(size);
            sendBufferSize = s.getSendBufferSize();
        }
        if (s.getTcpNoDelay() != tcpNoDelay) {
            s.setTcpNoDelay(tcpNoDelay);
        }
    }

    public synchronized void bind(final Executor executor) throws IOException {
        if (device == null)
            throw new IllegalStateException("Device not initalized");
        if (!isListening())
            throw new IllegalStateException("Only initiates associations - "
                    + this);
        if (server != null)
            throw new IllegalStateException("Already listening - " + server);
        server = isTLS() ? createTLSServerSocket() : new ServerSocket();
        server.bind(getSocketAddress(), backlog);
        executor.execute(new Runnable() {

            public void run() {
                SocketAddress addr = server.getLocalSocketAddress();
                log.info("Start listening on {}", addr);
                try {
                    for (;;) {
                        log.debug("Wait for connection on {}", addr);
                        Socket s = server.accept();
                        setSocketOptions(s);
                        if (checkConnection(s)) {
                            Association a = Association.accept(s,
                                    NetworkConnection.this);
                            executor.execute(a);
                            incListenerConnectionCount();
                        }
                    }
                } catch (Throwable e)
                {
                    // assume exception was raised by graceful stop of server
                }
                log.info("Stop listening on {}", addr);
            }
        });
    }

    /**
     * Check the incoming socket connection against the limitations set up for
     * this Network Connection.
     * 
     * @param s The socket connection.
     * @return boolean True if association negotiation should proceed.
     */
    protected boolean checkConnection(Socket s) {
        if (excludeConnectionsFrom == null
                || excludeConnectionsFrom.size() == 0)
            return true;
        
        String ip = null;
        try {
            // Check to see if this connection attempt is just a keep alive
            // ping from the CSS. Use a list of possible pingers in the case
            // of a high-availability network.
            for (int i = 0; i < excludeConnectionsFrom.size(); i++) {
                ip = (String) excludeConnectionsFrom.get(i);
                if (s.getInetAddress().getHostAddress().equals(ip)) {
                    log.debug("Rejecting connection from {}", ip);
                    s.close();
                    return false;
                }
            }
        } catch (IOException e)
        {
            log.debug("Caught IOException closing socket from {}", ip);
            return false;
        }
        
        return true;
    }

    /**
     * Increment the number of active associations.
     */
    protected void incListenerConnectionCount() {
        ++associationCount;
    }

    /**
     * Decrement the number of active associations.
     */
    protected void decListenerConnectionCount() {
        --associationCount;
    }

    /**
     * Check to see if the specified number of associations has been exceeded.
     * 
     * @param maxAssociations An int containing the maximum number of
     *            associations allowed.
     * @return boolean True if the max association count has not been exceeded.
     */
    public boolean checkConnectionCountWithinLimit() {
        return true ? associationCount <= maxScpAssociations : false;
    }

    public synchronized void unbind()
    {
        if (server == null)
            return;
        try
        {
            server.close();
        } catch (Throwable e)
        {
            // Ignore errors when closing the server socket.
        }
        associationCount = 0;
        server = null;
    }

    private Socket createTLSSocket() throws IOException {
        SSLSocketFactory sf = device.getSSLContext().getSocketFactory();
        SSLSocket s = (SSLSocket) sf.createSocket();
        s.setEnabledProtocols(tlsProtocol);
        s.setEnabledCipherSuites(tlsCipherSuite);
        return s;
    }

    private ServerSocket createTLSServerSocket() throws IOException {
        SSLServerSocketFactory ssf = device.getSSLContext()
                .getServerSocketFactory();
        SSLServerSocket ss = (SSLServerSocket) ssf.createServerSocket();
        ss.setEnabledProtocols(tlsProtocol);
        ss.setEnabledCipherSuites(tlsCipherSuite);
        ss.setNeedClientAuth(tlsNeedClientAuth);
        return ss;
    }

    /**
     * Get a list of IP addresses from which we should ignore connections.
     * Useful in an environment that utilizes a load balancer. In the case of a
     * TCP ping from a load balancing switch, we don't want to spin off a new
     * thread and try to negotiate an association.
     * 
     * @return Returns the list of IP addresses which should be ignored.
     */
    public List getExcludeConnectionsFrom()
    {
        return excludeConnectionsFrom;
    }

    /**
     * Set a list of IP addresses from which we should ignore connections.
     * Useful in an environment that utilizes a load balancer. In the case of a
     * TCP ping from a load balancing switch, we don't want to spin off a new
     * thread and try to negotiate an association.
     * 
     * @param excludeConnectionsFrom the list of IP addresses which should be
     *            ignored.
     */
    public void setExcludeConnectionsFrom(List excludeConnectionsFrom)
    {
        this.excludeConnectionsFrom = excludeConnectionsFrom;
    }

    /**
     * Get the maximum number of incoming associations that this Network
     * Connection will allow.
     * 
     * @return int An int which defines the max associations.
     */
    public int getMaxScpAssociations()
    {
        return maxScpAssociations;
    }

    /**
     * Set the maximum number of incoming associations that this Network
     * Connection will allow.
     * 
     * @param maxScpAssociations An int which defines the max associations.
     */
    public void setMaxScpAssociations(int maxListenerAssociations)
    {
        this.maxScpAssociations = maxListenerAssociations;
    }

}
