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
 * Bill Wallace, Agfa HealthCare Inc., 
 * Portions created by the Initial Developer are Copyright (C) 2008
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Bill Wallace <bill.wallace@agfa.com>
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

package org.dcm4chee.xero.wado.multi;

import static org.dcm4chee.xero.wado.WadoParams.OBJECT_UID;

import java.util.Iterator;
import java.util.Map;

import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.metadata.servlet.ServletResponseItem;
import org.dcm4chee.xero.util.FilterCombineIterator;
import org.dcm4chee.xero.util.StringUtil;

/**
 * The ObjectUidItFilter creates an iterator over all the specified UID's For
 * each UID, it calls the next filter, setting objectUID to the single, current
 * objectUID. It will restore objectUID when the calls are all done.
 * 
 * @author bwallace
 * 
 */
public class ObjectUidItFilter implements Filter<Iterator<ServletResponseItem>> {
    /** Split out the object UID if necessary */
    public Iterator<ServletResponseItem> filter(FilterItem<Iterator<ServletResponseItem>> filterItem, Map<String, Object> params) {
        Object objectUID = params.get(OBJECT_UID);
        if (objectUID == null)
            return null;
        String[] arrUID;
        if (objectUID instanceof String) {
            String sobjectUID = (String) objectUID;
            if (sobjectUID.indexOf('\\') == -1)
                return filterItem.callNextFilter(params);
            arrUID = StringUtil.split(sobjectUID, '\\', true);
        } else
            arrUID = (String[]) objectUID;

        Iterator<ServletResponseItem> it = new ObjectUidIterator(arrUID, filterItem, params);
        return it;
    }

    /** Iterate over the string UID's specified in the original parameters. */
    class ObjectUidIterator extends FilterCombineIterator<String, ServletResponseItem> {
        String origObjectUID;

        public ObjectUidIterator(String[] arrUid, FilterItem<Iterator<ServletResponseItem>> filterItem, Map<String, Object> params) {
            super(arrUid, filterItem, params);
            origObjectUID = (String) params.get(OBJECT_UID);
        }

        /** Sets the object UID to the current object UID */
        @Override
        protected void updateParams(String item, Map<String, Object> params) {
            params.put(OBJECT_UID, item);
        }

        /** Restore the original UID */
        @Override
        protected void restoreParams(String item, Map<String, Object> params) {
            params.put(OBJECT_UID, origObjectUID);
        }

    }
}
