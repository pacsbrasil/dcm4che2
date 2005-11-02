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
import java.net.SocketAddress;

import org.apache.mina.io.filter.IoThreadPoolFilter;
import org.apache.mina.io.socket.SocketConnector;
import org.apache.mina.protocol.ProtocolSession;
import org.apache.mina.protocol.filter.ProtocolThreadPoolFilter;
import org.apache.mina.protocol.io.IoProtocolConnector;
import org.dcm4che2.net.pdu.AAssociateRQ;
import org.dcm4che2.net.service.DicomServiceRegistry;

public class AssociationRequestor extends DULProtocolProvider
{
    private final SocketConnector socketIoConnector = new SocketConnector();

    private final IoProtocolConnector connector = new IoProtocolConnector(
            socketIoConnector);

    public AssociationRequestor(DicomServiceRegistry registry, Executor executor)
    {
        super(registry, executor, true);
    }

    public AssociationRequestor()
    {
        this(new DicomServiceRegistry(), new NewThreadExecutor("Association"));
    }

    public final void setIoThreadPoolFilter(
            IoThreadPoolFilter ioThreadPoolFilter)
    {
        if (ioThreadPoolFilter != null)
        {
            socketIoConnector.getFilterChain().addFirst("threadPool",
                    ioThreadPoolFilter);
        } else
        {
            socketIoConnector.getFilterChain().remove("threadPool");
        }
    }

    public final void setProtocolThreadPoolFilter(
            ProtocolThreadPoolFilter protocolThreadPoolFilter)
    {
        if (protocolThreadPoolFilter != null)
        {
            connector.getFilterChain().addFirst("threadPool",
                    protocolThreadPoolFilter);
        } else
        {
            connector.getFilterChain().remove("threadPool");
        }
    }

    public Association connect(AAssociateRQ aarq,
            SocketAddress address, SocketAddress localAddress, int timeout)
    throws IOException
    {
        ProtocolSession session = connector.connect(address, localAddress,
                timeout, this);
        Association a = (Association) session.getAttachment();
        a.write(aarq);
        synchronized (a)
        {
            while (a.getState() == Association.STA4 
                    || a.getState() == Association.STA5)
            {
                try
                {
                    a.wait();
                }
                catch (InterruptedException e)
                {
                    throw new RuntimeException(e);
                }
            }
        }
        if (a.getState() == Association.STA6)
            return a;
        
        if (a.getAssociateRJ() != null)
            throw new AssociateRJException(a.getAssociateRJ());
        else
            throw new AbortException(a.getAbort());
    }

    public Association connect(AAssociateRQ aarq,
            SocketAddress address)
    throws IOException
    {
        return connect(aarq, address, null, Integer.MAX_VALUE);
    }

    public Association connect(AAssociateRQ aarq,
            SocketAddress address, SocketAddress localAddress)
    throws IOException
    {
        return connect(aarq, address, localAddress, Integer.MAX_VALUE);
    }

    public Association connect(AAssociateRQ aarq,
            SocketAddress address, int timeout)
    throws IOException
    {
        return connect(aarq, address, null, timeout);
    }
}
