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

import org.dcm4che.server.DcmHandler;
import org.dcm4che.net.AAssociateAC;
import org.dcm4che.net.Association;
import org.dcm4che.net.AssociationListener;
import org.dcm4che.net.AcceptorPolicy;
import org.dcm4che.net.DcmServiceRegistry;
import org.dcm4che.net.AssociationFactory;

import java.io.IOException;
import java.net.Socket;
import java.util.Iterator;
import java.util.LinkedList;

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
class DcmHandlerImpl implements DcmHandler {
    
    // Constants -----------------------------------------------------
    
    // Attributes ----------------------------------------------------
    private final static AssociationFactory fact =
        AssociationFactory.getInstance();
    
    private final AcceptorPolicy policy;
    private final DcmServiceRegistry services;
    private final LinkedList listeners = new LinkedList();
    
    private int requestTO = 5000;
    
    // Static --------------------------------------------------------
    
    // Constructors --------------------------------------------------
    public DcmHandlerImpl(AcceptorPolicy policy, DcmServiceRegistry services) {
        if (policy == null)
            throw new NullPointerException();
        
        if (services == null)
            throw new NullPointerException();
        
        this.policy = policy;
        this.services = services;
    }
    
    // Public --------------------------------------------------------
    
    // DcmHandler implementation -------------------------------------
    public void handle(Socket s) throws IOException {
        Association assoc = fact.newAcceptor(s);
        for (Iterator it = listeners.iterator(); it.hasNext();) {
            assoc.addAssociationListener((AssociationListener)it.next());
        }
        if (assoc.accept(policy, requestTO) instanceof AAssociateAC)
            fact.newActiveAssociation(assoc, services).run();
    }
    
    public void addAssociationListener(AssociationListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    
    public void removeAssociationListener(AssociationListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }
    
    public boolean isSockedClosedByHandler() {
        return true;
    }
    
    // Package protected ---------------------------------------------
    
    // Protected -----------------------------------------------------
    
    // Private -------------------------------------------------------
    
    // Inner classes -------------------------------------------------
}
