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

package org.dcm4che2.hp;

import java.util.List;

import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision$ $Date$
 * @since Jul 30, 2005
 *
 */
public class HPImageSet {

    public static final String ABSTRACT_PRIOR = "ABSTRACT_PRIOR";
    public static final String RELATIVE_TIME = "RELATIVE_TIME";
    
    private final DicomObject dcmobj;
    private final List selectors;

    HPImageSet(List selectors, DicomObject dcmobj) {
        this.selectors = selectors; 
        this.dcmobj = dcmobj; 
    }
    
    public DicomObject getDicomObject() {
        return dcmobj;
    }
    
    public boolean contains(DicomObject o, int frame) {
        for (int i = 0, n = selectors.size(); i < n; i++) {
            HPSelector selector = (HPSelector) selectors.get(i);
            if (!selector.matches(o, frame))
                return false;
        }
        return true;
    }
    
    public int getImageSetNumber() {
        return dcmobj.getInt(Tag.ImageSetNumber);
    }
     
    public String getImageSetLabel() {
        return dcmobj.getString(Tag.ImageSetLabel);
    }
    
    public String getImageSetSelectorCategory() {
        return dcmobj.getString(Tag.ImageSetSelectorCategory);
    }
    
    public boolean isRelativeTime() {
        return RELATIVE_TIME.equals(getImageSetSelectorCategory());
    }
    
    public boolean isAbstractPrior() {
        return ABSTRACT_PRIOR.equals(getImageSetSelectorCategory());
    }
    
    public int[] getRelativeTime() {
        return dcmobj.getInts(Tag.RelativeTime);
    }
 
    public String getRelativeTimeUnits() {
        return dcmobj.getString(Tag.RelativeTimeUnits);
    }

    public boolean hasAbstractPriorValue() {
        return dcmobj.containsValue(Tag.AbstractPriorValue);
    }
 
    public int[] getAbstractPriorValue() {
        return dcmobj.getInts(Tag.AbstractPriorValue);
    }
 
    public boolean hasAbstractPriorCode() {
        return dcmobj.containsValue(Tag.AbstractPriorCodeSequence);
    }
 
    public Code getAbstractPriorCode() {
        return new Code(dcmobj.getNestedDicomObject(Tag.AbstractPriorCodeSequence));
    }

    public DicomElement getImageSetSelectorSequence() {
        return dcmobj.getParent().get(Tag.ImageSetSelectorSequence);
    }
}
