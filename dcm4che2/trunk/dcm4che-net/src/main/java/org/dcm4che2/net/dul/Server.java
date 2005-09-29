package org.dcm4che2.net.dul;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.apache.mina.io.filter.IoThreadPoolFilter;
import org.apache.mina.protocol.filter.ProtocolThreadPoolFilter;
import org.dcm4che2.net.pdu.AAbort;
import org.dcm4che2.net.pdu.AAssociateAC;
import org.dcm4che2.net.pdu.AAssociateRJ;
import org.dcm4che2.net.pdu.AAssociateRQ;
import org.dcm4che2.net.pdu.AReleaseRP;
import org.dcm4che2.net.pdu.AReleaseRQ;

public class Server {
    
    static DULServiceUser user = new DULServiceUser() {

        public void onAAssociateRQ(DULServiceProvider provider, AAssociateRQ associateRQ) {
            provider.write(new AAssociateAC());            
        }

        public void onOpened(DULServiceProvider provider) {}

        public void onAAssociateAC(DULServiceProvider provider, AAssociateAC associateAC) {}

        public void onAAssociateRJ(DULServiceProvider provider, AAssociateRJ associateRJ) {}

        public void onAReleaseRQ(DULServiceProvider provider, AReleaseRQ releaseRQ) {
            provider.write(new AReleaseRP());
        }

        public void onAReleaseRP(DULServiceProvider provider, AReleaseRP releaseRP) {}

        public void onAbort(AAbort abort) {}
    };
    

    /**
     * @param args
     */
    public static void main(String[] args) {
        IoThreadPoolFilter ioThreadPoolFilter = new IoThreadPoolFilter();
        ProtocolThreadPoolFilter protocolThreadPoolFilter = new ProtocolThreadPoolFilter();

        ioThreadPoolFilter.start();
        protocolThreadPoolFilter.start();
        
        DULProtocolAcceptor acceptor = new DULProtocolAcceptor();
//        acceptor.setIoThreadPoolFilter(ioThreadPoolFilter);
//        acceptor.setProtocolThreadPoolFilter(protocolThreadPoolFilter);
        try {
            acceptor.bind(user, makeSocketAddress());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static SocketAddress makeSocketAddress() {
        return new InetSocketAddress(11112);
    }

}
