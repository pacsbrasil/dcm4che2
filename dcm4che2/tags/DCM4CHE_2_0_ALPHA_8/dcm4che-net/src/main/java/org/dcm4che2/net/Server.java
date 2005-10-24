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
import java.net.SocketAddress;
import java.util.Collection;
import java.util.Iterator;

import org.apache.mina.io.filter.IoThreadPoolFilter;
import org.apache.mina.protocol.filter.ProtocolThreadPoolFilter;
import org.dcm4che2.data.UID;
import org.dcm4che2.net.pdu.AAssociateAC;
import org.dcm4che2.net.pdu.AAssociateRQ;
import org.dcm4che2.net.pdu.PDU;
import org.dcm4che2.net.pdu.PresentationContext;
import org.dcm4che2.net.service.DicomServiceRegistry;
import org.dcm4che2.net.service.VerificationService;

public class Server
{
    private static AcceptorPolicy policy = new AcceptorPolicy(){

        public PDU negotiate(AAssociateRQ rq)
        {
            AAssociateAC ac = new AAssociateAC();
            ac.setCallingAET(rq.getCallingAET());
            ac.setCalledAET(rq.getCalledAET());
            Collection pcs = rq.getPresentationContexts();
            for (Iterator iter = pcs.iterator(); iter.hasNext();)
            {
                PresentationContext rqpc = (PresentationContext) iter.next();
                PresentationContext acpc = new PresentationContext();
                acpc.setPCID(rqpc.getPCID());
                acpc.setResult(
                        UID.VerificationSOPClass.equals(rqpc.getAbstractSyntax())
                                ? PresentationContext.ACCEPTANCE
                                : PresentationContext.ABSTRACT_SYNTAX_NOT_SUPPORTED);
                acpc.addTransferSyntax(rqpc.getTransferSyntax());
                ac.addPresentationContext(acpc);
            }
            return ac ;
        }};

    private static SocketAddress makeSocketAddress()
    {
        return new InetSocketAddress(11112);
    }
    
    /**
     * @param args
     */
    public static void main(String[] args)
    {
        IoThreadPoolFilter ioThreadPoolFilter = new IoThreadPoolFilter();
        ProtocolThreadPoolFilter protocolThreadPoolFilter = new ProtocolThreadPoolFilter();

        ioThreadPoolFilter.start();
        protocolThreadPoolFilter.start();

        AssociationAcceptor acceptor = new AssociationAcceptor();
        DicomServiceRegistry registry = new DicomServiceRegistry();
        registry.register(new VerificationService());
        AssociationAcceptorHandler handler = new AssociationAcceptorHandler(policy, registry);
        // acceptor.setIoThreadPoolFilter(ioThreadPoolFilter);
        // acceptor.setProtocolThreadPoolFilter(protocolThreadPoolFilter);
        try
        {
            acceptor.bind(handler, makeSocketAddress());
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

}
