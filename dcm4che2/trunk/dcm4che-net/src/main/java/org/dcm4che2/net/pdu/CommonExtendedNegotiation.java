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
import java.util.Collections;
import java.util.List;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Reversion$ $Date$
 * @since Sep 16, 2005
 *
 */
public class CommonExtendedNegotiation {

    private String sopCUID;
    private String serviceCUID;
    private final List relSopCUIDs = new ArrayList();

    public boolean isValid() {       
        return sopCUID != null && serviceCUID != null;
    }
    
    public int itemLength() {
        if (!isValid())
            throw new IllegalStateException();
        int len = 4 + sopCUID.length() + serviceCUID.length();
        for (int i = 0, n = relSopCUIDs.size(); i < n; i++) {
            len += 2 + ((String) relSopCUIDs.get(i)).length();
        }
        return len;
    }
    
    public final String getSOPClassUID() {
        return sopCUID;
    }
    
    public final String getServiceClassUID() {
        return serviceCUID;
    }
    
    public final List getRelatedSOPClassUIDs() {
        return Collections.unmodifiableList(relSopCUIDs);
    }
}
