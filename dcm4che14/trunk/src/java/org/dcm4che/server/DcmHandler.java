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

import org.dcm4che.net.AcceptorPolicy;
import org.dcm4che.net.AssociationListener;
import org.dcm4che.net.DcmServiceRegistry;

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
 * <p><b>20020810 gunter:</b>
 * <ul>
 * <li> add properties rqTimeout, dimseTimeout, soCloseDelay
 * </ul>
 */
public interface DcmHandler extends Server.Handler
{
   void addAssociationListener(AssociationListener l);
   
   void removeAssociationListener(AssociationListener l);

   void setAcceptorPolicy(AcceptorPolicy policy);
   
   AcceptorPolicy getAcceptorPolicy();

   void setDcmServiceRegistry(DcmServiceRegistry services);
   
   DcmServiceRegistry getDcmServiceRegistry();
   
   /** Getter for property rqTimeout.
    * @return Value of property rqTimeout.
    */
   int getRqTimeout();
   
   /** Setter for property rqTimeout.
    * @param rqTimeout New value of property rqTimeout.
    */
   void setRqTimeout(int timeout);
   
   /** Getter for property dimseTimeout.
    * @return Value of property dimseTimeout.
    */
   int getDimseTimeout();
   
   /** Setter for property dimseTimeout.
    * @param dimseTimeout New value of property dimseTimeout.
    */
   void setDimseTimeout(int dimseTimeout);
   
   /** Getter for property soCloseDelay.
    * @return Value of property soCloseDelay.
    */
   int getSoCloseDelay();
   
   /** Setter for property soCloseDelay.
    * @param soCloseDelay New value of property soCloseDelay.
    */
   void setSoCloseDelay(int soCloseDelay);
   
}
