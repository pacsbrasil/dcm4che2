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
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import javax.net.SocketFactory;
import javax.net.ServerSocketFactory;

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
public abstract class SSLContextAdapter
{
   // Constants -----------------------------------------------------
   public static final int ENCRYPT_NONE   = 0;
   public static final int ENCRYPT_FORCE  = 1;
   public static final int ENCRYPT_ENABLE = 2;
  
   // Attributes ----------------------------------------------------
   
   // Static --------------------------------------------------------
   public static SSLContextAdapter getInstance() {
      return (SSLContextAdapter)Implementation.findFactory(
            "dcm4che.util.SSLContextAdapter");
   }

   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------
   public abstract void setEnabledCipherSuites(int encyption);

   public abstract void setEnabledCipherSuites(String[] cipherSuites);

   public abstract String[] getEnabledCipherSuites();

   public abstract String[] getSupportedCipherSuites()
   throws GeneralSecurityException;

   public abstract void setNeedClientAuth(boolean needClientAuth);
   
   public abstract void setStartHandshake(boolean startHandshake);

   public abstract void seedRandom(long seed)
   throws GeneralSecurityException;
 
   public abstract KeyStore loadKeyStore(InputStream in, char[] password)
   throws GeneralSecurityException, IOException;
   
   public abstract KeyStore loadKeyStore(File file, char[] password)
   throws GeneralSecurityException, IOException;
   
   public abstract KeyStore loadKeyStore(String systemId, char[] password)
   throws GeneralSecurityException, IOException;

   public abstract void setKey(KeyStore key, char[] password)
   throws GeneralSecurityException;

   public abstract void setTrust(KeyStore cacerts)
   throws GeneralSecurityException;

   public abstract SocketFactory getSocketFactory()
   throws GeneralSecurityException;

   public abstract ServerSocketFactory getServerSocketFactory()
   throws GeneralSecurityException;
      
   // Z implementation ----------------------------------------------
   
   // Y overrides ---------------------------------------------------
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
}
