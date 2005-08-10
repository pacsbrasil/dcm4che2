/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.dcm.qrscp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.dcm4che.dict.UIDs;
import org.dcm4che.net.AAssociateRQ;
import org.dcm4che.net.AssociationFactory;
import org.dcm4che.net.DcmServiceException;
import org.dcm4chex.archive.ejb.jdbc.FileInfo;

/**
 * @author gunter.zeilinger@tiani.com
 * @version Revision $Date$
 * @since 23.03.2005
 */

final class RetrieveInfo {

    private static final String[] NATIVE_LE_TS = { UIDs.ExplicitVRLittleEndian,
        UIDs.ImplicitVRLittleEndian };
    
    private static final boolean isNativeLE_TS(String uid) {
        return UIDs.ExplicitVRLittleEndian.equals(uid)
            || UIDs.ImplicitVRLittleEndian.equals(uid);
    }

    private static class IuidsAndTsuids {
        final HashSet iuids = new HashSet();
        final HashSet tsuids = new HashSet();
    }
    
    private final int size;
    private final HashMap iuidsAndTsuidsByCuid = new HashMap();
    private final LinkedHashMap localFilesByIuid = new LinkedHashMap();
    private final HashMap iuidsByRemoteAET = new HashMap();
    private final HashMap iuidsByExternalAET = new HashMap();
    private final HashSet notAvailableIuids = new HashSet();
    private Entry curMoveForward;
    
    RetrieveInfo(QueryRetrieveScpService service, FileInfo[][] instInfos)
            throws DcmServiceException {
        FileInfo[] fileInfos;
        FileInfo fileInfo;
        String iuid, cuid;
        IuidsAndTsuids iuidsAndTsuids;
        List localFiles;
        this.size = instInfos.length;
        for (int i = 0; i < size; ++i) {
            fileInfos = instInfos[i];
            iuid = fileInfos[0].sopIUID;
            cuid = fileInfos[0].sopCUID;
            iuidsAndTsuids = (IuidsAndTsuids) iuidsAndTsuidsByCuid.get(cuid);
            if (iuidsAndTsuids == null) {
                iuidsAndTsuids = new IuidsAndTsuids();
                iuidsAndTsuidsByCuid.put(cuid, iuidsAndTsuids);
            }
            iuidsAndTsuids.iuids.add(iuid);
            notAvailableIuids.add(iuid);
            for (int j = 0; j < fileInfos.length; j++) {
                fileInfo = fileInfos[j];
                if (service.isLocalFileSystem(fileInfo.basedir)) {
                    putLocalFile(iuid, fileInfo);
                    iuidsAndTsuids.tsuids.add(fileInfo.tsUID);
                } else {
                    if (fileInfo.fileRetrieveAET != null) {
                        putIuid(iuidsByRemoteAET, fileInfo.fileRetrieveAET, iuid);
                    } else if (fileInfo.extRetrieveAET != null) {
                        putIuid(iuidsByExternalAET, fileInfo.extRetrieveAET, iuid);
                    }
                }
            }
        }
    }

    private void putLocalFile(String iuid, FileInfo fileInfo) {
        List localFiles = (List) localFilesByIuid.get(iuid);
        if (localFiles == null) {
            localFiles = new ArrayList();
            localFilesByIuid.put(iuid, localFiles);                        
        }
        localFiles.add(fileInfo);
        notAvailableIuids.remove(iuid);
    }

    private void putIuid(HashMap iuidsByAET, String aet, String iuid) {
        Set iuids = (Set) iuidsByAET.get(aet);
        if (iuids == null) {
            iuids = new LinkedHashSet();
            iuidsByAET.put(aet, iuids);                            
        }
        iuids.add(iuid);
        notAvailableIuids.remove(iuid);
    }
    
    public final boolean isAnyLocal() {
        return !localFilesByIuid.isEmpty();
    }

    public final boolean isAllLocal() {
        return localFilesByIuid.size() == size;
    }
    
    public final boolean isAetWithAllIuids() {        
        Iterator it = iuidsByRemoteAET.entrySet().iterator();
        Map.Entry entry;
        Collection iuids;
        while (it.hasNext()) {
            entry = (Entry) it.next();
            iuids = (Collection) entry.getValue();
            if (iuids.size() == size)
                return true;
        }
        return false;
    }
    
    public boolean isRetrieveFromLocal() {
        return  isAllLocal() || isAnyLocal() && !isAetWithAllIuids();  
    }
   
    public void addPresContext(AAssociateRQ rq) {
        String cuid;
        String tsuid;
        IuidsAndTsuids iuidsAndTsuids;
        AssociationFactory asf = AssociationFactory.getInstance();
        Iterator it = iuidsAndTsuidsByCuid.entrySet().iterator();        
        while (it.hasNext()) {
            Map.Entry entry = (Entry) it.next();
            cuid = (String) entry.getKey();
            iuidsAndTsuids = (IuidsAndTsuids) entry.getValue();
            rq.addPresContext(asf.newPresContext(rq.nextPCID(), cuid, 
                    NATIVE_LE_TS)); 
            Iterator it2 = iuidsAndTsuids.tsuids.iterator();
            while (it2.hasNext()) {
                tsuid = (String) it2.next();
                if (!isNativeLE_TS(tsuid)) {
                    rq.addPresContext(asf.newPresContext(rq.nextPCID(), cuid, 
                            new String[] { tsuid }));
                }
            }
        }        
    }

    public Iterator getCUIDs() {
        return iuidsAndTsuidsByCuid.keySet().iterator();
    }
    
    public Set removeInstancesOfClass(String cuid) {
        IuidsAndTsuids iuidsAndTsuids = 
            (IuidsAndTsuids) iuidsAndTsuidsByCuid.remove(cuid);
        Iterator it = iuidsAndTsuids.iuids.iterator();
        String iuid;
        while (it.hasNext()) {
            iuid = (String) it.next();
            localFilesByIuid.remove(iuid);
            removeIuid(iuidsByRemoteAET, iuid);
            removeIuid(iuidsByExternalAET, iuid);
        }
        return iuidsAndTsuids.iuids;
    }

    private void removeIuid(Map iuidsByAET, String iuid) {
        Iterator it = iuidsByAET.values().iterator();
        Set iuids;
        while (it.hasNext()) {
            iuids = (Set) it.next();
            iuids.remove(iuid);
            if (iuids.isEmpty())
                it.remove();
        }
    }

    private static final Comparator ASC_IUIDS_SIZE = new Comparator() {

        public int compare(Object o1, Object o2) {
            Map.Entry e1 = (Map.Entry) o1;
            Set iuids1 = (Set) e1.getValue();
            Map.Entry e2 = (Map.Entry) o2;
            Set iuids2 = (Set) e2.getValue();
            return iuids1.size() - iuids2.size();
        }
    };
    
    public boolean nextMoveForward() {
        if (!iuidsByRemoteAET.isEmpty()) {
            curMoveForward = removeNextRemoteAET(iuidsByRemoteAET);
            return true;
        }
        if (!iuidsByExternalAET.isEmpty()) {
            curMoveForward = removeNextRemoteAET(iuidsByExternalAET);
            return true;
        }
        curMoveForward = null;
        return false;
    }

    private Map.Entry removeNextRemoteAET(Map iuidsByAET) {
        Map.Entry entry = (Map.Entry) Collections.max(iuidsByAET.entrySet(), ASC_IUIDS_SIZE);
        iuidsByAET.remove(entry.getKey());
        Set iuids = (Set) entry.getValue();
        String iuid;
        Iterator it = iuids.iterator();
        while (it.hasNext()) {
            iuid = (String) it.next();
            removeIuid(iuidsByRemoteAET, iuid);
            removeIuid(iuidsByExternalAET, iuid);            
        }
        return entry;
    }

    public final Collection getLocalFiles() {
        return localFilesByIuid.values();
    }

    public final Collection getLocalIUIDs() {
        return localFilesByIuid.keySet();
    }

    public final String getMoveForwardAET() {
        return (String) (curMoveForward != null ? curMoveForward.getKey() : null);
    }

    public final Collection getMoveForwardUIDs() {
        return (Collection) (curMoveForward != null ? curMoveForward.getValue() : null);
    }
        
}
