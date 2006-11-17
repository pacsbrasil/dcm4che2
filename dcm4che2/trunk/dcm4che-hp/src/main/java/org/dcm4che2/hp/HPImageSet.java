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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision$ $Date$
 * @since Jul 30, 2005
 * 
 */
public class HPImageSet
{

    private final DicomObject dcmobj;
    private final List selectors;

    HPImageSet(List selectors, DicomObject dcmobj)
    {
        this.selectors = selectors;
        this.dcmobj = dcmobj;
    }

    public HPImageSet()
    {
        this.selectors = new ArrayList();
        this.dcmobj = new BasicDicomObject();
        DicomObject is = new BasicDicomObject();
        is.putSequence(Tag.IMAGE_SET_SELECTOR_SEQUENCE);
        DicomElement tbissq = is.putSequence(Tag.TIME_BASED_IMAGE_SETS_SEQUENCE);
        tbissq.addDicomObject(dcmobj);
    }
    
    public HPImageSet(HPImageSet shareSelectors)
    {
        this.selectors = shareSelectors.selectors;
        this.dcmobj = new BasicDicomObject();
        DicomElement tbissq = shareSelectors.getTimeBasedImageSetsSequence();
        tbissq.addDicomObject(dcmobj);
    }
    
    public DicomObject getDicomObject()
    {
        return dcmobj;
    }

    public boolean contains(DicomObject o, int frame)
    {
        for (int i = 0, n = selectors.size(); i < n; i++)
        {
            HPSelector selector = (HPSelector) selectors.get(i);
            if (!selector.matches(o, frame))
                return false;
        }
        return true;
    }

    public int getImageSetNumber()
    {
        return dcmobj.getInt(Tag.IMAGE_SET_NUMBER);
    }

    public void setImageSetNumber(int imageSetNumber)
    {
        dcmobj.putInt(Tag.IMAGE_SET_NUMBER, VR.US, imageSetNumber);
    }

    public String getImageSetLabel()
    {
        return dcmobj.getString(Tag.IMAGE_SET_LABEL);
    }

    public void setImageSetLabel(String imageSetLabel)
    {
        dcmobj.putString(Tag.IMAGE_SET_LABEL, VR.LO, imageSetLabel);
    }

    public String getImageSetSelectorCategory()
    {
        return dcmobj.getString(Tag.IMAGE_SET_SELECTOR_CATEGORY);
    }

    public boolean hasRelativeTime()
    {
        return dcmobj.containsValue(Tag.RELATIVE_TIME);
    }
    
    public RelativeTime getRelativeTime()
    {
        RelativeTimeUnits units = 
                RelativeTimeUnits.valueOf(dcmobj.getString(Tag.RELATIVE_TIME_UNITS));
        return new RelativeTime(dcmobj.getInts(Tag.RELATIVE_TIME), units);
    }

    public void setRelativeTime(RelativeTime relativeTime)
    {
        dcmobj.putString(Tag.IMAGE_SET_SELECTOR_CATEGORY, VR.CS, 
                CodeString.RELATIVE_TIME);
        dcmobj.putInts(Tag.RELATIVE_TIME, VR.US, relativeTime.getValues());
        dcmobj.putString(Tag.RELATIVE_TIME_UNITS, VR.CS, 
                relativeTime.getUnits().getCodeString());
    }

    public boolean hasAbstractPriorValue()
    {
        return dcmobj.containsValue(Tag.ABSTRACT_PRIOR_VALUE);
    }

    public AbstractPriorValue getAbstractPriorValue()
    {
        return new AbstractPriorValue(dcmobj.getInts(Tag.ABSTRACT_PRIOR_VALUE));
    }

    public void setAbstractPriorValue(AbstractPriorValue abstractPriorValue)
    {
        dcmobj.putString(Tag.IMAGE_SET_SELECTOR_CATEGORY, VR.CS, 
                CodeString.ABSTRACT_PRIOR);
        dcmobj.putInts(Tag.ABSTRACT_PRIOR_VALUE, VR.US, abstractPriorValue.getValues());
    }

    public boolean hasAbstractPriorCode()
    {
        return dcmobj.containsValue(Tag.ABSTRACT_PRIOR_CODE_SEQUENCE);
    }

    public Code getAbstractPriorCode()
    {
        return new Code(
                dcmobj.getNestedDicomObject(Tag.ABSTRACT_PRIOR_CODE_SEQUENCE));
    }

    public void setAbstractPriorCode(Code code)
    {
        dcmobj.putString(Tag.IMAGE_SET_SELECTOR_CATEGORY, VR.CS, 
                CodeString.ABSTRACT_PRIOR);
        dcmobj.putNestedDicomObject(Tag.ABSTRACT_PRIOR_CODE_SEQUENCE,
                code.getDicomObject());
    }

    public DicomElement getImageSetSelectorSequence()
    {
        return dcmobj.getParent().get(Tag.IMAGE_SET_SELECTOR_SEQUENCE);
    }

    public DicomElement getTimeBasedImageSetsSequence()
    {
        return dcmobj.getParent().get(Tag.TIME_BASED_IMAGE_SETS_SEQUENCE);
    }

    public List getImageSetSelectors()
    {
        return Collections.unmodifiableList(selectors);
    }
    
    public void addImageSetSelector(HPSelector selector)
    {
        getImageSetSelectorSequence().addDicomObject(selector.getDicomObject());
        selectors.add(selector);
    }
}
