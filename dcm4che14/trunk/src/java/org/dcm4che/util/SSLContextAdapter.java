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

package org.dcm4che.util;

import org.dcm4che.Implementation;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import javax.net.SocketFactory;
import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;
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
public abstract class SSLContextAdapter {
    // Constants -----------------------------------------------------
    public static final String SSL_RSA_WITH_NULL_SHA =
        "SSL_RSA_WITH_NULL_SHA";
    public static final String SSL_RSA_WITH_3DES_EDE_CBC_SHA = 
        "SSL_RSA_WITH_3DES_EDE_CBC_SHA";
     
    // Attributes ----------------------------------------------------
    
    // Static --------------------------------------------------------
    public static SSLContextAdapter getInstance() {
        return (SSLContextAdapter)Implementation.findFactory(
            "dcm4che.util.SSLContextAdapter");
    }
    
    // Constructors --------------------------------------------------
    
    // Public --------------------------------------------------------
    public abstract SSLContext getSSLContext();
        
    public abstract String[] getSupportedCipherSuites();
    
    public abstract void setEnabledProtocols(String[] protocols);
    
    public abstract void setEnabledCipherSuites(String[] cipherSuites);
    
    public abstract String[] getEnabledCipherSuites();
    
    public abstract String[] getEnabledProtocols();
        
    public abstract String[] getSupportedProtocols();
    
    public abstract void setNeedClientAuth(boolean needClientAuth);
    
    public abstract boolean isNeedClientAuth();
    
    //   public abstract void setStartHandshake(boolean startHandshake);
    
    public abstract void seedRandom(long seed);
    
    public abstract KeyStore loadKeyStore(InputStream in, char[] password)
    throws GeneralSecurityException, IOException;
    
    public abstract KeyStore loadKeyStore(File file, char[] password)
    throws GeneralSecurityException, IOException;
    
    public abstract KeyStore loadKeyStore(URL url, char[] password)
    throws GeneralSecurityException, IOException;
    
    public abstract void setKey(KeyStore key, char[] password)
    throws GeneralSecurityException;
    
    public abstract KeyManager[] getKeyManagers();

    public abstract void setTrust(KeyStore cacerts)
    throws GeneralSecurityException;
    
    public abstract TrustManager[] getTrustManagers();

    public abstract void init()
    throws GeneralSecurityException;
    
    public abstract SocketFactory getSocketFactory();
    
    public abstract SocketFactory getSocketFactory(String[] cipherSuites);
    
    public abstract ServerSocketFactory getServerSocketFactory();

    public abstract ServerSocketFactory getServerSocketFactory(String[] cipherSuites);
    
    // Z implementation ----------------------------------------------
    
    // Y overrides ---------------------------------------------------
    
    // Package protected ---------------------------------------------
    
    // Protected -----------------------------------------------------
    
    // Private -------------------------------------------------------
    
    // Inner classes -------------------------------------------------
}
