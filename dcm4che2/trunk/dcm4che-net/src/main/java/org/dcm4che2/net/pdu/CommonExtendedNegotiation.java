/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4che2.net.pdu;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Reversion$ $Date$
 * @since Sep 16, 2005
 *
 */
public class CommonExtendedNegotiation {

    private String sopCUID;
    private String serviceCUID;
    private final Set relSopCUIDs = new LinkedHashSet();

    public final String getSOPClassUID() {
        return sopCUID;
    }
    
    public final void setSOPClassUID(String sopCUID) {
        this.sopCUID = sopCUID;
    }
    
    public final String getServiceClassUID() {
        return serviceCUID;
    }
    
    public final void setServiceClassUID(String serviceCUID) {
        this.serviceCUID = serviceCUID;
    }
    
    public final Collection getRelatedGeneralSOPClassUIDs() {
        return Collections.unmodifiableCollection(relSopCUIDs);
    }
    
    public final boolean addRelatedGeneralSOPClassUID(String relSopCUID) {
        if (relSopCUID == null)
            throw new NullPointerException();
        return relSopCUIDs.add(relSopCUID);
    }
    
    public final boolean removeRelatedGeneralSOPClassUID(String relSopCUID) {
        return relSopCUIDs.remove(relSopCUID);
    }
    
    public int length() {
        if (sopCUID == null || serviceCUID == null)
            throw new IllegalStateException();
        
        int len = 4 + sopCUID.length() + serviceCUID.length();
        for (Iterator it = relSopCUIDs.iterator(); it.hasNext();)
            len += 2 + ((String) it.next()).length();        
        return len;
    }    
    
}
