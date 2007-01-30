/*
 * Created on Sep 20, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.dcm4chex.webview;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;
import org.dcm4che2.data.UIDDictionary;
import org.dcm4che2.data.VR;
import org.dcm4che2.net.Association;
import org.dcm4che2.net.CommandUtils;
import org.dcm4che2.net.Device;
import org.dcm4che2.net.DimseRSP;
import org.dcm4che2.net.Executor;
import org.dcm4che2.net.ExtQueryTransferCapability;
import org.dcm4che2.net.NetworkApplicationEntity;
import org.dcm4che2.net.NetworkConnection;
import org.dcm4che2.net.NewThreadExecutor;
import org.dcm4che2.net.NoPresentationContextException;
import org.dcm4che2.net.TransferCapability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author franz.willer@agfa.com
 * @version $Revision:$ $Date:$
 * @since 04.10.2006
 */
public class UIDQuery {

    private static final String[] NATIVE_LE_TS = {
        UID.ImplicitVRLittleEndian,
        UID.ExplicitVRLittleEndian  };
 
    private static final String[] FIND_CUID = {
        UID.StudyRootQueryRetrieveInformationModelFIND,
        UID.PatientRootQueryRetrieveInformationModelFIND, };
    
    private Executor executor = new NewThreadExecutor("WebViewLauncher");

    private NetworkApplicationEntity remoteAE = new NetworkApplicationEntity();

    private NetworkConnection remoteConn = new NetworkConnection();

    private Device device = new Device("WebViewLauncher");

    private NetworkApplicationEntity ae = new NetworkApplicationEntity();

    private NetworkConnection conn = new NetworkConnection();

    private Association assoc;

    static Logger log = LoggerFactory.getLogger(UIDQuery.class);
    
    public UIDQuery(String calling, String called, String host, int port) {
        remoteAE.setInstalled(true);
        remoteAE.setAssociationAcceptor(true);
        remoteAE.setNetworkConnection(new NetworkConnection[] { remoteConn });
        device.setNetworkApplicationEntity(ae);
        device.setNetworkConnection(conn);
        ae.setNetworkConnection(conn);
        ae.setAssociationInitiator(true);

        ae.setAETitle(calling);
        remoteAE.setAETitle(called);
        remoteConn.setHostname(host);
        remoteConn.setPort(port);

    }
    
    /**
     * @param l
     * @return
     */
    private Map splitSeries(List l, String patID) {
        Map m = new HashMap();
        DicomObject dcm;
        String suid;
        for ( Iterator iter = l.iterator() ; iter.hasNext() ;) {
            dcm = (DicomObject) iter.next();
            suid = dcm.getString(Tag.SeriesInstanceUID);
            if ( patID == null || patID.equals(dcm.getString(Tag.PatientID))) {
                l = (List) m.get(suid);
                if ( l == null ) {
                    l = new ArrayList();
                    m.put(suid, l);
                }
	            l.add(dcm);
            }
        }
        return m;
    }

    public List query(DicomObject keys) { 
        keys.putString(Tag.QueryRetrieveLevel, VR.CS, "IMAGE");
        if ( ! keys.contains(Tag.SeriesInstanceUID)) keys.putNull(Tag.SeriesInstanceUID, null);
        if ( ! keys.contains(Tag.SOPClassUID)) keys.putNull(Tag.SOPClassUID, null);
        if ( ! keys.contains(Tag.SOPInstanceUID)) keys.putNull(Tag.SOPInstanceUID, null);
        
        configureTransferCapability();
        List l;
        try {
            assoc = ae.connect(remoteAE, executor);
            log.info("assoc:"+assoc);
            l = doQuery(keys);
            log.info("query:"+l);
        } catch (Exception e) {
            log.error("Query failed:",e);
            l = new ArrayList();
        }
        return l;
    }

    public Map queryAndSort(DicomObject keys) { 
     return splitSeries( query( keys ), null );
    }
    
    private void configureTransferCapability() {
        TransferCapability[] tc = new TransferCapability[2];
        tc[0] = new ExtQueryTransferCapability(FIND_CUID[0],
                NATIVE_LE_TS, TransferCapability.SCU);
        tc[1] = new ExtQueryTransferCapability(FIND_CUID[1],
                NATIVE_LE_TS, TransferCapability.SCU);
        ae.setTransferCapability(tc);
    }

    private List doQuery(DicomObject keys) throws IOException, InterruptedException {
        TransferCapability tc = selectFindTransferCapability();
        String cuid = tc.getSopClass();
        String tsuid = selectTransferSyntax(tc);
        log.info("Send Query Request using "
                + UIDDictionary.getDictionary().prompt(cuid) + ":");
        log.info(keys.toString());
        DimseRSP rsp = assoc.cfind(cuid, 0, keys, tsuid, Integer.MAX_VALUE);
        List result = new ArrayList();
        while (rsp.next()) {
            DicomObject cmd = rsp.getCommand();
            if (CommandUtils.isPending(cmd)) {
                DicomObject data = rsp.getDataset();
                result.add(data);
                log.info("\nReceived Query Response #" 
                        + result.size() + ":");
                log.info(data.toString());
            }
        }
        return result;
    }

    private TransferCapability selectFindTransferCapability()
            throws NoPresentationContextException {
        TransferCapability tc;
        if ((tc = selectTransferCapability(FIND_CUID)) != null)
            return tc;
        throw new NoPresentationContextException(UIDDictionary.getDictionary()
                .prompt(FIND_CUID[0])
                + " not supported by" + remoteAE.getAETitle());
    }

    private TransferCapability selectTransferCapability(String[] cuid) {
        TransferCapability tc;
        for (int i = 0; i < cuid.length; i++) {
            tc = assoc.getTransferCapabilityAsSCU(cuid[i]);
            if (tc != null)
                return tc;
        }
        return null;
    }
   
    private String selectTransferSyntax(TransferCapability tc) {
        String[] tcuids = tc.getTransferSyntax();
        if (Arrays.asList(tcuids).indexOf(UID.DeflatedExplicitVRLittleEndian) != -1)
            return UID.DeflatedExplicitVRLittleEndian;
        return tcuids[0];
    }

}
