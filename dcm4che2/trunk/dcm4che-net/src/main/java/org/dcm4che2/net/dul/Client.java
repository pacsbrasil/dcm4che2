/*
 *   @(#) $Id$
 *
 *   Copyright 2004 The Apache Software Foundation
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */
package org.dcm4che2.net.dul;

import java.net.InetSocketAddress;

import org.apache.mina.io.filter.IoThreadPoolFilter;
import org.apache.mina.protocol.filter.ProtocolThreadPoolFilter;
import org.dcm4che2.net.pdu.AAbort;
import org.dcm4che2.net.pdu.AAssociateAC;
import org.dcm4che2.net.pdu.AAssociateRJ;
import org.dcm4che2.net.pdu.AAssociateRQ;
import org.dcm4che2.net.pdu.AReleaseRP;
import org.dcm4che2.net.pdu.AReleaseRQ;

/**
 * (<strong>Entry Point</strong>) Starts SumUp client.
 * 
 * @author The Apache Directory Project
 * @version $Rev: 264677 $, $Date$
 */
public class Client
{
    private static DULServiceUser user = new DULServiceUser(){

        public void onOpened(DULServiceProvider service) {
            service.write(makeAARQ());            
        }

        public void onAAssociateRQ(DULServiceProvider provider, AAssociateRQ associateRQ) {}

        public void onAAssociateRJ(DULServiceProvider provider, AAssociateRJ associateRJ) {}

        public void onAAssociateAC(DULServiceProvider service, AAssociateAC associateAC) {
            service.write(new AReleaseRQ());            
        }

        public void onAReleaseRQ(DULServiceProvider provider, AReleaseRQ releaseRQ) {
            provider.write(new AReleaseRP());
        }

        public void onAReleaseRP(DULServiceProvider provider, AReleaseRP releaseRP) {}

        public void onAbort(AAbort abort) {}
   };

    public static void main( String[] args ) throws Throwable
    {

        IoThreadPoolFilter ioThreadPoolFilter = new IoThreadPoolFilter();
        ProtocolThreadPoolFilter protocolThreadPoolFilter = new ProtocolThreadPoolFilter();

        ioThreadPoolFilter.start();
        protocolThreadPoolFilter.start();
        
        DULProtocolConnector connector = new DULProtocolConnector();
//        connector.setIoThreadPoolFilter(ioThreadPoolFilter);
//        connector.setProtocolThreadPoolFilter(protocolThreadPoolFilter);
        DULServiceProvider service = connector.connect(user, makeSocketAddress());
        System.out.println("Wait for STA1");
        synchronized (service) {
            while (service.getState() != DULServiceProvider.STA1)
                service.wait();
        }
        System.out.println("Exit main");
        ioThreadPoolFilter.stop();
        protocolThreadPoolFilter.stop();
    }

    private static InetSocketAddress makeSocketAddress() {
        return new InetSocketAddress("localhost", 11112);
    }

    private static AAssociateRQ makeAARQ() {
        AAssociateRQ aarq = new AAssociateRQ();
        return aarq;
    }
}
