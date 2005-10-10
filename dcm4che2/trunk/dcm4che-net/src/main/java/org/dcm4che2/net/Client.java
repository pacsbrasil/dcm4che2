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

import java.net.InetSocketAddress;

import org.apache.mina.io.filter.IoThreadPoolFilter;
import org.apache.mina.protocol.filter.ProtocolThreadPoolFilter;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.UID;
import org.dcm4che2.net.pdu.AAssociateRQ;
import org.dcm4che2.net.pdu.PresentationContext;

/**
 * (<strong>Entry Point</strong>) Starts SumUp client.
 * 
 * @author The Apache Directory Project
 * @version $Rev: 264677 $, $Date$
 */
public class Client
{

    private static final int PCID = 1;


    private static InetSocketAddress makeSocketAddress()
    {
        return new InetSocketAddress("localhost", 11112);
    }
    
    private static AAssociateRQ makeAARQ()
    {
        AAssociateRQ aarq = new AAssociateRQ();
        PresentationContext pc = new PresentationContext();
        pc.setPCID(PCID);
        pc.setAbstractSyntax(UID.VerificationSOPClass);
        pc.addTransferSyntax(UID.ImplicitVRLittleEndian);
        aarq.addPresentationContext(pc);
        return aarq;
    }

    public static void main(String[] args) throws Throwable
    {
//        IoThreadPoolFilter ioThreadPoolFilter = new IoThreadPoolFilter();
//        ProtocolThreadPoolFilter protocolThreadPoolFilter = new ProtocolThreadPoolFilter();

//        assocThreadPool.start();
//        ioThreadPoolFilter.start();
//        protocolThreadPoolFilter.start();

        AssociationRequestor connector = new AssociationRequestor();
        // connector.setIoThreadPoolFilter(ioThreadPoolFilter);
        // connector.setProtocolThreadPoolFilter(protocolThreadPoolFilter);
        Association a = connector.connect(makeAARQ(), makeSocketAddress());
        DimseRSP rsp = a.invoke(PCID, CommandFactory.newCEchoRQ(a.nextMessageID()), null);
        rsp.next();
        DicomObject cechorsp = rsp.getCommand();
        a.release(true);
//        System.out.println("Stop Threads");
//        ioThreadPoolFilter.stop();
//        protocolThreadPoolFilter.stop();
        System.out.println("Exit main");
    }
}
