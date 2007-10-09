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
 * See listed authors below.
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

package org.dcm4che2.audit.message;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Mar 1, 2007
 */
public class InstanceSorter {
    
    private LinkedHashMap suids = new LinkedHashMap();
    
    public void clear() {
        suids.clear();
    }
    
    public Object addInstance(String suid, String cuid, String iuid,
            Object obj) {
        LinkedHashMap cuids = (LinkedHashMap) suids.get(suid);
        if (cuids == null) {
            cuids = new LinkedHashMap();
            suids.put(suid, cuids);
        }
        LinkedHashMap iuids = (LinkedHashMap) cuids.get(cuid);
        if (iuids == null) {
            iuids = new LinkedHashMap();
            cuids.put(cuid, iuids);
        }
        return iuids.put(iuid, obj);
    }
    
    public Iterator iterateSUIDs() {
        return suids.keySet().iterator();
    }    

    public Iterator iterateCUIDs(String suid) {
        LinkedHashMap cuids = (LinkedHashMap) suids.get(suid);
        if (cuids == null) {
            return Collections.EMPTY_LIST.iterator();
        }
        return cuids.keySet().iterator();
    }

    public Iterator iterateIUIDs(String suid, String cuid) {
        LinkedHashMap cuids = (LinkedHashMap) suids.get(suid);
        if (cuids == null) {
            return Collections.EMPTY_LIST.iterator();
        }
        LinkedHashMap iuids = (LinkedHashMap) cuids.get(cuid);
        if (iuids == null) {
            return Collections.EMPTY_LIST.iterator();
        }
        return iuids.keySet().iterator();
    }
    
    public int countInstances(String suid, String cuid) {
        LinkedHashMap cuids = (LinkedHashMap) suids.get(suid);
        if (cuids == null) {
            return 0;
        }
        LinkedHashMap iuids = (LinkedHashMap) cuids.get(cuid);
        if (iuids == null) {
            return 0;
        }
        return iuids.size();
    }
    
    public Object getInstance(String suid, String cuid, String iuid) {
        LinkedHashMap cuids = (LinkedHashMap) suids.get(suid);
        if (cuids == null) {
            return null;
        }
        LinkedHashMap iuids = (LinkedHashMap) cuids.get(cuid);
        if (iuids == null) {
            return null;
        }
        return iuids.get(iuid);       
    }
}
