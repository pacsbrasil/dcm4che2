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

import org.dcm4che2.data.Tag;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision$ $Date$
 * @since Oct 19, 2005
 *
 */
public abstract class AbstractHPSelector
implements HPSelector
{
    
    public String getImageSetSelectorUsageFlag()
    {
        return getDicomObject().getString(Tag.IMAGE_SET_SELECTOR_USAGE_FLAG);
    }

    public String getFilterbyCategory()
    {
        return getDicomObject().getString(Tag.FILTER_BY_CATEGORY);
    }
    
    public String getFilterbyAttributePresence()
    {
        return getDicomObject().getString(Tag.FILTER_BY_ATTRIBUTE_PRESENCE);
    }
    
    public int getSelectorAttribute()
    {
        return getDicomObject().getInt(Tag.SELECTOR_ATTRIBUTE);
    }

    public String getSelectorAttributeVR()
    {
        return getDicomObject().getString(Tag.SELECTOR_ATTRIBUTE_VR);
    }

    public int getSelectorSequencePointer()
    {
        return getDicomObject().getInt(Tag.SELECTOR_SEQUENCE_POINTER);
    }

    public int getFunctionalGroupPointer()
    {
        return getDicomObject().getInt(Tag.FUNCTIONAL_GROUP_POINTER);
    }

    public String getSelectorSequencePointerPrivateCreator()
    {
        return getDicomObject().getString(Tag.SELECTOR_SEQUENCE_POINTER_PRIVATE_CREATOR);
    }

    public String getFunctionalGroupPrivateCreator()
    {
        return getDicomObject().getString(Tag.FUNCTIONAL_GROUP_PRIVATE_CREATOR);
    }

    public String getSelectorAttributePrivateCreator()
    {
        return getDicomObject().getString(Tag.SELECTOR_ATTRIBUTE_PRIVATE_CREATOR);
    }
    
    public Object getSelectorValue()
    {
        String vrStr =  getSelectorAttributeVR();
        if (vrStr == null || vrStr.length() != 2)
            return null;
        
        switch (vrStr.charAt(0) << 8 | vrStr.charAt(1))
        {
            case 0x4154:
                return getDicomObject().getInts(Tag.SELECTOR_AT_VALUE);
            case 0x4353:
                return getDicomObject().getStrings(Tag.SELECTOR_CS_VALUE);
            case 0x4453:
                return getDicomObject().getFloats(Tag.SELECTOR_DS_VALUE);
            case 0x4644:
                return getDicomObject().getDoubles(Tag.SELECTOR_FD_VALUE);
            case 0x464c:
                return getDicomObject().getFloats(Tag.SELECTOR_FL_VALUE);
            case 0x4953:
                return getDicomObject().getInts(Tag.SELECTOR_IS_VALUE);
            case 0x4c4f:
                return getDicomObject().getStrings(Tag.SELECTOR_LO_VALUE);
            case 0x4c54:
                return getDicomObject().getStrings(Tag.SELECTOR_LT_VALUE);
            case 0x504e:
                return getDicomObject().getStrings(Tag.SELECTOR_PN_VALUE);
            case 0x5348:
                return getDicomObject().getStrings(Tag.SELECTOR_SH_VALUE);
            case 0x534c:
                return getDicomObject().getInts(Tag.SELECTOR_SL_VALUE);
            case 0x5351:
                return Code.toArray(getDicomObject().get(Tag.SELECTOR_CODE_SEQUENCE_VALUE));
            case 0x5353:
                return getDicomObject().getInts(Tag.SELECTOR_SS_VALUE);
            case 0x5354:
                return getDicomObject().getStrings(Tag.SELECTOR_ST_VALUE);
            case 0x554c:
                return getDicomObject().getInts(Tag.SELECTOR_UL_VALUE);
            case 0x5553:
                return getDicomObject().getInts(Tag.SELECTOR_US_VALUE);
            case 0x5554:
                return getDicomObject().getStrings(Tag.SELECTOR_UT_VALUE);
        }
        return null;
    }

    public int getSelectorValueNumber()
    {
        return getDicomObject().getInt(Tag.SELECTOR_VALUE_NUMBER);
    }

    public String getFilterbyOperator()
    {
        return getDicomObject().getString(Tag.FILTER_BY_OPERATOR);
    }


}
