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

package org.dcm4che.server;

import org.dcm4che.util.HandshakeFailedListener;
import java.io.IOException;
import java.net.Socket;
import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.HandshakeCompletedListener;

/**
 * <description>
 *
 * @see <related>
 * @author  <a href="mailto:{email}">{full name}</a>.
 * @author  <a href="mailto:gunter@tiani.com">Gunter Zeilinger</a>
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
public interface Server {
    interface Handler {
        void handle(Socket s) throws IOException;
        
        boolean isSockedClosedByHandler();
    }
    
    void addHandshakeCompletedListener(HandshakeCompletedListener listener);
    
    void addHandshakeFailedListener(HandshakeFailedListener listener);
    
    void removeHandshakeCompletedListener(HandshakeCompletedListener listener);
    
    void removeHandshakeFailedListener(HandshakeFailedListener listener);
    
    void setMaxClients(int max);
    
    int getMaxClients();
    
    int getNumClients();
    
    /**
     * @deprecated use {@link #setPort}, {@link #start()} 
     */    
    void start(int port) throws IOException;
    
    /**
     * @deprecated use {@link #setPort}, {@link #setServerSocketFactory},
     *                 {@link #start()}
     */    
    void start(int port, ServerSocketFactory ssf) throws IOException;
    
    void start() throws Exception;
    
    void stop();
    
    /** Getter for property port.
     * @return Value of property port.
     *
     */
    public int getPort();
    
    /** Setter for property port.
     * @param port New value of property port.
     *
     */
    public void setPort(int port);
    
    /** Getter for property serverSocketFactory.
     * @return Value of property serverSocketFactory.
     *
     */
    public ServerSocketFactory getServerSocketFactory();
    
    /** Setter for property serverSocketFactory.
     * @param serverSocketFactory New value of property serverSocketFactory.
     *
     */
    public void setServerSocketFactory(ServerSocketFactory serverSocketFactory);
    
}
