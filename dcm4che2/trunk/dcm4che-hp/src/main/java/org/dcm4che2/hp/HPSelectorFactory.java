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

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4che2.data.VRMap;
import org.dcm4che2.hp.plugins.ImagePlaneSelector;
import org.dcm4che2.util.TagUtils;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision$ $Date$
 * @since Jul 30, 2005
 * 
 */
public class HPSelectorFactory
{

    /**
     * Create Image Set Selector from Image Set Selector Sequence (0072,0022) item.
     * The created Image Set Selector is backed by the given item 
     * {@link #getDicomObject DicomObject}.
     * 
     * @param item DicomObject of Image Set Selector Sequence (0072,0022)
     * @return the new Image Set Selector
     */
    public static HPSelector createImageSetSelector(DicomObject item)
    {
        String usageFlag = item.getString(Tag.ImageSetSelectorUsageFlag);
        if (usageFlag == null)
            throw new IllegalArgumentException(
                    "Missing (0072,0024) Image Set Selector Usage Flag");
        
        ImageSetSelectorUsageFlag flag;
        try
        {
            flag = ImageSetSelectorUsageFlag.valueOf(usageFlag);
        }
        catch (IllegalArgumentException e)
        {
            throw new IllegalArgumentException(
                    "Invalid (0072,0024) Image Set Selector Usage Flag: "
                    + usageFlag);
        }
        HPSelector sel = createAttributeValueSelector(item, flag, FilterOp.MEMBER_OF);
        sel = addSequencePointer(sel);
        sel = addFunctionalGroupPointer(sel);
        return sel;
    }

    /**
     * Create Image Set Selector with {@link String} Selector Attribute Values.
     * A new {@link #getDicomObject DicomObject}, representing the according
     * Image Set Selector Sequence (0072,0022) item is allocated and initialized.
     * 
     * @param usageFlag Image Set Selector Usage Flag
     * @param privateCreator Selector Attribute Private Creator, if Selector
     *   Attribute is contained by a Private Group, otherwise <code>null</code>.
     * @param tag Selector Attribute
     * @param valueNumber Selector Value Number
     * @param vr Selector Attribute VR: CS, LO, LT, PN, SH, ST or UT
     * @param values Selector Values
     * @return new Image Set Selector
     */
    public static HPSelector createAttributeValueSelector(
            ImageSetSelectorUsageFlag usageFlag, String privateCreator, int tag,
            int valueNumber, VR vr, String[] values)
    {
        return createAttributeValueSelector(usageFlag, privateCreator, tag,
                valueNumber, vr, values, FilterOp.MEMBER_OF);
    }
    
    /**
     * Create Image Set Selector with <code>int</code> Selector Attribute Values.
     * A new {@link #getDicomObject DicomObject}, representing the according
     * Image Set Selector Sequence (0072,0022) item is allocated and initialized.
     * 
     * @param usageFlag Image Set Selector Usage Flag
     * @param privateCreator Selector Attribute Private Creator, if Selector
     *   Attribute is contained by a Private Group, otherwise <code>null</code>.
     * @param tag Selector Attribute
     * @param valueNumber Selector Value Number
     * @param vr Selector Attribute VR: AT, IS, SL, SS, UL, or US
     * @param values Selector Values
     * @return new Image Set Selector
     */
    public static HPSelector createAttributeValueSelector(
            ImageSetSelectorUsageFlag usageFlag, String privateCreator, int tag,
            int valueNumber, VR vr, int[] values)
    {
        return createAttributeValueSelector(usageFlag, privateCreator, tag,
                valueNumber, vr, values, FilterOp.MEMBER_OF);
    }
    
    /**
     * Create Image Set Selector with <code>float</code> Selector Attribute Values.
     * A new {@link #getDicomObject DicomObject}, representing the according
     * Image Set Selector Sequence (0072,0022) item is allocated and initialized.
     * 
     * @param usageFlag Image Set Selector Usage Flag
     * @param privateCreator Selector Attribute Private Creator, if Selector
     *   Attribute is contained by a Private Group, otherwise <code>null</code>.
     * @param tag Selector Attribute
     * @param valueNumber Selector Value Number
     * @param vr Selector Attribute VR: DS or FL
     * @param values Selector Values
     * @return new Image Set Selector
     */
    public static HPSelector createAttributeValueSelector(
            ImageSetSelectorUsageFlag usageFlag, String privateCreator, int tag,
            int valueNumber, VR vr, float[] values)
    {
        return createAttributeValueSelector(usageFlag, privateCreator, tag,
                valueNumber, vr, values, FilterOp.MEMBER_OF);
    }
    
    /**
     * Create Image Set Selector with <code>double</code> Selector Attribute Values.
     * A new {@link #getDicomObject DicomObject}, representing the according
     * Image Set Selector Sequence (0072,0022) item is allocated and initialized.
     * 
     * @param usageFlag Image Set Selector Usage Flag
     * @param privateCreator Selector Attribute Private Creator, if Selector
     *   Attribute is contained by a Private Group, otherwise <code>null</code>.
     * @param tag Selector Attribute
     * @param valueNumber Selector Value Number
     * @param values Selector Values
     * @return new Image Set Selector
     */
    public static HPSelector createAttributeValueSelector(
            ImageSetSelectorUsageFlag usageFlag, String privateCreator, int tag,
            int valueNumber, double[] values)
    {
        return createAttributeValueSelector(usageFlag, privateCreator, tag,
                valueNumber, values, FilterOp.MEMBER_OF);
    }
    
    /**
     * Create Image Set Selector with {@link Code} Selector Attribute Values.
     * A new {@link #getDicomObject DicomObject}, representing the according
     * Image Set Selector Sequence (0072,0022) item is allocated and initialized.
     * 
     * @param usageFlag Image Set Selector Usage Flag
     * @param privateCreator Selector Attribute Private Creator, if Selector
     *   Attribute is contained by a Private Group, otherwise <code>null</code>.
     * @param tag Selector Attribute
     * @param values Selector Values
     * @param valueNumber Selector Value Number
     * @return new Image Set Selector
     */
    public static HPSelector createCodeValueSelector(
            ImageSetSelectorUsageFlag usageFlag, String privateCreator, int tag,
            Code[] values)
    {
        return new CodeValueSelector(usageFlag, privateCreator, tag, values,
                FilterOp.MEMBER_OF);
    }
    
    /**
     * Create Display Set Filter from Filter Operations Sequence (0072,0400) item.
     * The created Display Set Filter is backed by the given item 
     * {@link #getDicomObject DicomObject}.
     * 
     * @param item DicomObject of Filter Operations Sequence (0072,0400)
     * @return the new Display Set Filter
     */
    public static HPSelector createDisplaySetFilter(DicomObject item)
    {
        if (item.containsValue(Tag.FilterbyCategory))
        {
            return HangingProtocol.createFilterByCategory(item);
        }
        HPSelector sel = createDisplaySetSelector(item);
        sel = addSequencePointer(sel);
        sel = addFunctionalGroupPointer(sel);
        return sel;
    }

    /**
     * Create Display Set Filter with Filter-by Category IMAGE_PLANE.
     * A new {@link #getDicomObject DicomObject}, representing the according
     * Filter Operations Sequence (0072,0400) item, is allocated and initialized.
     * 
     * @param imagePlanes array of matching {@link ImagePlane ImagePlanes}.
     * @return new Display Set Filter
     */
    public static HPSelector createImagePlaneSelector(ImagePlane[] imagePlanes)
    {
        return new ImagePlaneSelector(imagePlanes);
    }

    /**
     * Create Display Set Filter with Filter-by Attribute Presence.
     * A new {@link #getDicomObject DicomObject}, representing the according
     * Filter Operations Sequence (0072,0400) item, is allocated and initialized.
     * 
     * @param privateCreator Selector Attribute Private Creator, if Selector
     *   Attribute is contained by a Private Group, otherwise <code>null</code>.
     * @param tag Selector Attribute
     * @param filter {@link FilterbyAttributePresence#PRESENT} or 
     *               {@link FilterbyAttributePresence#NOT_PRESENT} 
     * @return new Display Set Filter
     */
    public static HPSelector createAttributePresenceSelector(
            String privateCreator, int tag, FilterbyAttributePresence filter)
    {
        return new AttributePresenceSelector(filter, tag, privateCreator);
    }

    /**
     * Create Display Set Filter with {@link String} Selector Attribute Values.
     * A new {@link #getDicomObject DicomObject}, representing the according
     * Image Set Selector Sequence (0072,0022) item is allocated and initialized.
     * 
     * @param privateCreator Selector Attribute Private Creator, if Selector
     *   Attribute is contained by a Private Group, otherwise <code>null</code>.
     * @param tag Selector Attribute
     * @param valueNumber Selector Value Number
     * @param vr Selector Attribute VR: CS, LO, LT, PN, SH, ST or UT
     * @param values Selector Values
     * @param filterOp Filter-by Operator
     * @return new Display Set Filter
     */
    public static HPSelector createAttributeValueSelector(String privateCreator,
            int tag, int valueNumber, VR vr, String[] values, FilterOp filterOp)
    {
        return createAttributeValueSelector(null, privateCreator, tag,
                valueNumber, vr, values, filterOp);
    }
    
    /**
     * Create Display Set Filter with <code>int</code> Selector Attribute Values.
     * A new {@link #getDicomObject DicomObject}, representing the according
     * Image Set Selector Sequence (0072,0022) item is allocated and initialized.
     * 
     * @param privateCreator Selector Attribute Private Creator, if Selector
     *   Attribute is contained by a Private Group, otherwise <code>null</code>.
     * @param tag Selector Attribute
     * @param valueNumber Selector Value Number
     * @param vr Selector Attribute VR: AT, IS, SL, SS, UL, or US
     * @param values Selector Values
     * @param filterOp Filter-by Operator
     * @return new Display Set Filter
     */
    public static HPSelector createAttributeValueSelector(String privateCreator,
            int tag, int valueNumber, VR vr, int[] values,
            FilterOp filterOp)
    {
        return createAttributeValueSelector(null, privateCreator, tag,
                valueNumber, vr, values, filterOp);
    }
    
    /**
     * Create Display Set Filter with <code>float</code> Selector Attribute Values.
     * A new {@link #getDicomObject DicomObject}, representing the according
     * Image Set Selector Sequence (0072,0022) item is allocated and initialized.
     * 
     * @param privateCreator Selector Attribute Private Creator, if Selector
     *   Attribute is contained by a Private Group, otherwise <code>null</code>.
     * @param tag Selector Attribute
     * @param valueNumber Selector Value Number
     * @param vr Selector Attribute VR: DS or FL
     * @param values Selector Values
     * @param filterOp Filter-by Operator
     * @return new Display Set Filter
     */
    public static HPSelector createAttributeValueSelector(String privateCreator,
            int tag, int valueNumber, VR vr, float[] values, FilterOp filterOp)
    {
        return createAttributeValueSelector(null, privateCreator, tag,
                valueNumber, vr, values, filterOp);
    }
    
    /**
     * Create Display Set Filter with <code>double</code> Selector Attribute Values.
     * A new {@link #getDicomObject DicomObject}, representing the according
     * Image Set Selector Sequence (0072,0022) item is allocated and initialized.
     * 
     * @param privateCreator Selector Attribute Private Creator, if Selector
     *   Attribute is contained by a Private Group, otherwise <code>null</code>.
     * @param tag Selector Attribute
     * @param valueNumber Selector Value Number
     * @param values Selector Values
     * @param filterOp Filter-by Operator
     * @return new Display Set Filter
     */
    public static HPSelector createAttributeValueSelector(String privateCreator,
            int tag, int valueNumber, double[] values, FilterOp filterOp)
    {
        return createAttributeValueSelector(null, privateCreator, tag,
                valueNumber, values, filterOp);
    }
    
    /**
     * Create Display Set Filter with {@link Code} Selector Attribute Values.
     * A new {@link #getDicomObject DicomObject}, representing the according
     * Image Set Selector Sequence (0072,0022) item, is allocated and initialized.
     * 
     * @param privateCreator Selector Attribute Private Creator, if Selector
     *   Attribute is contained by a Private Group, otherwise <code>null</code>.
     * @param tag Selector Attribute
     * @param valueNumber Selector Value Number
     * @param values Selector Values
     * @param filterOp Filter-by Operator
     * @return new Display Set Filter
     */
    public static HPSelector createCodeValueSelector(String privateCreator,
            int tag, Code[] values, FilterOp filterOp)
    {
        return new CodeValueSelector(null, privateCreator, tag, values, filterOp);
    }

    /**
     * Decorate Image Set Filter or Display Set Filter with Selector Sequence
     * Pointer, defining the Selector Attribute as nested in a Sequence. If the
     * Sequence is itself nested in a Frunctional Group Sequence, the returned
     * HPComparator has to be additional decorated by {@link #addFunctionalGroupPointer}.
     * The associated {@link #getDicomObject DicomObject} is updated correspondingly.
     * If <code>tag = 0</tag>, the given Image Set Filter or Display Set Filter
     * is returned unmodified.
     * 
     * @param privateCreator Selector Sequence Pointer Private Creator, if 
     *        Selector Sequence Pointer is contained by a Private Group,
     *        otherwise <code>null</code>.
     * @param tag Selector Sequence Pointer
     * @param selector Image Set Filter or Display Set Filter to decorate
     * @return decorated Image Set Filter or Display Set Filter
     */
    public static HPSelector addSequencePointer(String privCreator, int tag, 
            HPSelector selector)
    {
        if (tag == 0)
            return selector;
        
        AttributeSelector attrSel = (AttributeSelector) selector;
        
        if (selector.getSelectorSequencePointer() != 0)
            throw new IllegalArgumentException("Sequence Pointer already added");
        
        if (selector.getFunctionalGroupPointer() != 0)
            throw new IllegalArgumentException(
                    "Functional Group Pointer already added");
        
        selector.getDicomObject().putInt(Tag.SelectorSequencePointer, VR.AT, tag);
        if (privCreator != null)
        {
            selector.getDicomObject().putString(
                    Tag.SelectorSequencePointerPrivateCreator, VR.LO, privCreator);
        }
           
        return new Seq(tag, privCreator, attrSel);
    }

    /**
     * Decorate Image Set Filter or Display Set Filter with Functional Group
     * Pointer, defining the Selector Attribute as nested in a Functional Group Sequence. 
     * The associated {@link #getDicomObject DicomObject} is updated correspondingly.
     * If <code>tag = 0</tag>, the given Image Set Filter or Display Set Filter
     * is returned unmodified.
     * 
     * @param privateCreator Functional Group Private Creator, if Functional
     *        Group Pointer is contained by a Private Group,
     *        otherwise <code>null</code>.
     * @param tag Functional Group Pointer
     * @param selector Image Set Filter or Display Set Filter to decorate
     * @return decorated Image Set Filter or Display Set Filter
     */
    public static HPSelector addFunctionalGroupPointer(String privCreator,
            int tag, HPSelector selector)
    {
        if (tag == 0)
            return selector;
        
        AttributeSelector attrSel = (AttributeSelector) selector;
        
        if (selector.getFunctionalGroupPointer() != 0)
            throw new IllegalArgumentException(
                    "Functional Group Pointer already added");
        
        selector.getDicomObject().putInt(Tag.FunctionalGroupPointer, VR.AT, tag);
        if (privCreator != null)
        {
            selector.getDicomObject().putString(
                    Tag.FunctionalGroupPrivateCreator, VR.LO, privCreator);
        }
           
        return new FctGrp(tag, privCreator, attrSel);
    }

    private static HPSelector addSequencePointer(HPSelector sel)
    {
        int seqTag = sel.getSelectorSequencePointer();
        if (seqTag != 0)
        {
            String privCreator = sel.getSelectorSequencePointerPrivateCreator();
            sel = new Seq(seqTag, privCreator, (AttributeSelector) sel);
        }
        return sel;
    }
    
    private static HPSelector addFunctionalGroupPointer(HPSelector sel)
    {
        int fgTag = sel.getFunctionalGroupPointer();
        if (fgTag != 0)
        {
            String privCreator = sel.getFunctionalGroupPrivateCreator();
            sel = new FctGrp(fgTag, privCreator, (AttributeSelector) sel);
        }
        return sel;
    }

    
    private static HPSelector createAttributeValueSelector(DicomObject item,
            ImageSetSelectorUsageFlag usageFlag, FilterOp filterOp)
    {
        String vrStr = item.getString(Tag.SelectorAttributeVR);
        if (vrStr == null)
        {
            throw new IllegalArgumentException(
                    "Missing (0072,0050) Selector Attribute VR");
        }
        if (vrStr.length() == 2)
        {
            switch (vrStr.charAt(0) << 8 | vrStr.charAt(1))
            {
                case 0x4154:
                    return new Int(item, Tag.SelectorATValue, usageFlag, filterOp);
                case 0x4353:
                    return new Str(item, Tag.SelectorCSValue, usageFlag, filterOp);
                case 0x4453:
                    return new Flt(item, Tag.SelectorDSValue, usageFlag, filterOp);
                case 0x4644:
                    return new Dbl(item, Tag.SelectorFDValue, usageFlag, filterOp);
                case 0x464c:
                    return new Flt(item, Tag.SelectorFLValue, usageFlag, filterOp);
                case 0x4953:
                    return new Int(item, Tag.SelectorISValue, usageFlag, filterOp);
                case 0x4c4f:
                    return new Str(item, Tag.SelectorLOValue, usageFlag, filterOp);
                case 0x4c54:
                    return new Str(item, Tag.SelectorLTValue, usageFlag, filterOp);
                case 0x504e:
                    return new Str(item, Tag.SelectorPNValue, usageFlag, filterOp);
                case 0x5348:
                    return new Str(item, Tag.SelectorSHValue, usageFlag, filterOp);
                case 0x534c:
                    return new Int(item, Tag.SelectorSLValue, usageFlag, filterOp);
                case 0x5351:
                    return new CodeValueSelector(item, usageFlag, filterOp);
                case 0x5353:
                    return new Int(item, Tag.SelectorSSValue, usageFlag, filterOp);
                case 0x5354:
                    return new Str(item, Tag.SelectorSTValue, usageFlag, filterOp);
                case 0x554c:
                    return new UInt(item, Tag.SelectorULValue, usageFlag, filterOp);
                case 0x5553:
                    return new Int(item, Tag.SelectorUSValue, usageFlag, filterOp);
                case 0x5554:
                    return new Str(item, Tag.SelectorUTValue, usageFlag, filterOp);
            }
        }
        throw new IllegalArgumentException(
                "(0072,0050) Selector Attribute VR: " + vrStr);
    }

    private static HPSelector createDisplaySetSelector(DicomObject item)
    {
        String presence = item.getString(Tag.FilterbyAttributePresence);
        if (presence != null)
        {
            try
            {
                FilterbyAttributePresence filter = 
                        FilterbyAttributePresence.valueOf(presence);
                return new AttributePresenceSelector(item, filter);
            }
            catch (IllegalArgumentException e)
            {
                throw new IllegalArgumentException(
                        "(0072,0404) Filter-by Attribute Presence Attribute: "
                                + presence);
            }
        }
        String filterOp = item.getString(Tag.FilterbyOperator);
        if (filterOp == null)
            throw new IllegalArgumentException(
                "Missing (0072,0406) Filter-by Operator");
        try
        {
            FilterOp filter = FilterOp.valueOf(filterOp);
            return createAttributeValueSelector(item, null, filter);
        }
        catch (IllegalArgumentException e)
        {
            throw new IllegalArgumentException(
                    "Illegal (0072,0406) Filter-by Operator: " + filterOp);
        }
    }

    private abstract static class AttributeSelector
    extends AbstractHPSelector
    {
        protected final int tag;
        protected final String privateCreator;
        protected final boolean match;

        AttributeSelector(int tag, String privateCreator, boolean match)
        {
            this.tag = tag;
            this.privateCreator = privateCreator;
            this.match = match;
        }
        
        public final boolean isMatchIfNotPresent()
        {
            return match;
        }

        protected int resolveTag(DicomObject dcmobj)
        {
            return privateCreator == null ? tag : dcmobj.resolveTag(tag,
                    privateCreator);
        }

    }
    
    private static int getSelectorAttribute(DicomObject item)
    {
        int tag = item.getInt(Tag.SelectorAttribute);
        if (tag == 0)
            throw new IllegalArgumentException(
                    "Missing (0072,0026) Selector Attribute");
        return tag;
    }

    private abstract static class BaseAttributeSelector
    extends AttributeSelector
    {
        protected final DicomObject item;

        BaseAttributeSelector(DicomObject item, boolean match)
        {
            super(HPSelectorFactory.getSelectorAttribute(item),
                    item.getString(Tag.SelectorAttributePrivateCreator), match);
            this.item = item;
        }

        BaseAttributeSelector(int tag, String privateCreator, boolean match)
        {
            super(tag, privateCreator, match);
            item = new BasicDicomObject();
            item.putInt(Tag.SelectorAttribute, VR.AT, tag);
            if (privateCreator != null)
                item.putString(Tag.SelectorAttributePrivateCreator,
                        VR.LO, privateCreator);
        }
        
        public final DicomObject getDicomObject()
        {
            return item;
        }        
        
    }
    
    private static class AttributePresenceSelector
    extends BaseAttributeSelector
    {
        AttributePresenceSelector(DicomObject item, 
                FilterbyAttributePresence filter)
        {
            super(item, filter.isMatchIfNotPresent());
        }

        AttributePresenceSelector(FilterbyAttributePresence filter, int tag,
                String privateCreator)
        {
            super(tag, privateCreator, filter.isMatchIfNotPresent());
            item.putString(Tag.FilterbyAttributePresence, VR.CS,
                    filter.getCodeString());
        }
        
        public boolean matches(DicomObject dcmobj, int frame)
        {
            return dcmobj.containsValue(resolveTag(dcmobj)) ? !match : match;
        }
    }

    private static abstract class AttributeValueSelector
            extends BaseAttributeSelector
    {
        protected final int valueNumber;
        protected final FilterOp filterOp;

        AttributeValueSelector(DicomObject item,
                ImageSetSelectorUsageFlag usageFlag, FilterOp filterOp)
        {
            super(item, usageFlag == null || usageFlag.match());
            this.valueNumber = item.getInt(Tag.SelectorValueNumber);
            this.filterOp = filterOp;
        }

        AttributeValueSelector(int tag, String privateCreator, int valueNumber,
                ImageSetSelectorUsageFlag usageFlag, FilterOp filterOp, VR vr)
        {
            super(tag, privateCreator, usageFlag == null || usageFlag.match());
            this.valueNumber = valueNumber;
            this.filterOp = filterOp;
            item.putInt(Tag.SelectorValueNumber, VR.US, valueNumber);
            item.putString(Tag.FilterbyOperator, VR.CS, filterOp.getCodeString());
            item.putString(Tag.SelectorAttributeVR, VR.CS, vr.toString());
            if (usageFlag != null)
            {
                item.putString(Tag.ImageSetSelectorUsageFlag, VR.CS,
                        usageFlag.getCodeString());
            }
        }
        
    }

    private static HPSelector createAttributeValueSelector(ImageSetSelectorUsageFlag usageFlag,
            String privateCreator, int tag,
            int valueNumber, VR vr, String[] values,
            FilterOp filterOp)
    {
        VR vr1 = vr != null ? vr : VRMap.getPrivateVRMap(privateCreator).vrOf(tag);
        int valueTag;
        switch (vr1.code())
        {
            case 0x4353:
                valueTag = Tag.SelectorCSValue; break;
            case 0x4c4f:
                valueTag = Tag.SelectorLOValue; break;
            case 0x4c54:
                valueTag = Tag.SelectorLTValue; break;
            case 0x504e:
                valueTag = Tag.SelectorPNValue; break;
            case 0x5348:
                valueTag = Tag.SelectorSHValue; break;
            case 0x5354:
                valueTag = Tag.SelectorSTValue; break;
            case 0x5554:
                valueTag = Tag.SelectorUTValue; break;
            default:
                throw new IllegalArgumentException("vr: " + vr);
        }
        return new Str(tag, privateCreator, valueNumber, valueTag, usageFlag,
                filterOp, vr, values);
    }

    private static class Str extends AttributeValueSelector
    {
        protected final String[] params;

        Str(DicomObject item, int valueTag, ImageSetSelectorUsageFlag usageFlag,
                FilterOp filterOp)
        {
            super(item, usageFlag, filterOp);
            if (filterOp.isNumeric())
                throw new IllegalArgumentException("Filter-by Operator: "
                        + item.get(Tag.FilterbyOperator)
                        + " conflicts with non-numeric VR: "
                        + item.get(Tag.SelectorAttributeVR));

            this.params = item.getStrings(valueTag);
            if (params == null || params.length == 0)
                throw new IllegalArgumentException("Missing "
                        + TagUtils.toString(valueTag));
        }

        Str(int tag, String privateCreator, int valueNumber, int valueTag,
                ImageSetSelectorUsageFlag usageFlag, FilterOp filterOp, VR vr,
                String[] params)
        {
            super(tag, privateCreator, valueNumber, usageFlag, filterOp, vr);
            this.params = (String[]) params.clone();
            item.putStrings(valueTag, vr, params);
        }

        public boolean matches(DicomObject dcmobj, int frame)
        {
            String[] values = dcmobj.getStrings(resolveTag(dcmobj));
            if (values == null || values.length < Math.max(valueNumber, 1))
                return match;
            return filterOp.op(values, valueNumber, params);
        }
    }

    private static HPSelector createAttributeValueSelector(ImageSetSelectorUsageFlag usageFlag,
            String privateCreator, int tag,
            int valueNumber, VR vr, int[] values,
            FilterOp filterOp)
    {
        VR vr1 = vr != null ? vr : VRMap.getPrivateVRMap(privateCreator).vrOf(tag);
        int valueTag; 
        boolean uint = false;
        switch (vr1.code())
        {
            case 0x4154:
                valueTag = Tag.SelectorATValue; break;
            case 0x4953:
                valueTag = Tag.SelectorISValue; break;
            case 0x534c:
                valueTag = Tag.SelectorSLValue; break;
            case 0x5353:
                valueTag = Tag.SelectorSSValue; break;
            case 0x554c:
                valueTag = Tag.SelectorULValue; uint = true; break;
            case 0x5553:
                valueTag = Tag.SelectorUSValue; break;
            default:
                throw new IllegalArgumentException("vr: " + vr);
        }
        return uint
                ? (HPSelector) new UInt(tag, privateCreator, valueNumber,
                        valueTag, usageFlag, filterOp, vr, values)
                : (HPSelector) new Int(tag, privateCreator, valueNumber, 
                        valueTag, usageFlag, filterOp, vr, values);
    }
    
    private static class Int extends AttributeValueSelector
    {
        private final int[] params;

        Int(DicomObject item, int valueTag, ImageSetSelectorUsageFlag usageFlag,
                FilterOp filterOp)
        {
            super(item, usageFlag, filterOp);
            this.params = item.getInts(valueTag);
            if (params == null || params.length == 0)
                throw new IllegalArgumentException("Missing "
                        + TagUtils.toString(valueTag));
            if (filterOp.isNumeric()
                    && filterOp.getNumParams() != params.length)
            {
                throw new IllegalArgumentException("Illegal Number of values: "
                        + item.get(valueTag));
            }
        }

        Int(int tag, String privateCreator, int valueNumber, int valueTag,
                ImageSetSelectorUsageFlag usageFlag, FilterOp filterOp, VR vr,
                int[] params)
        {
            super(tag, privateCreator, valueNumber, usageFlag, filterOp, vr);
            this.params = (int[]) params.clone();
            item.putInts(valueTag, vr, params);
        }
        
        public boolean matches(DicomObject dcmobj, int frame)
        {
            int[] values = dcmobj.getInts(resolveTag(dcmobj));
            if (values == null || values.length < Math.max(valueNumber, 1))
                return match;
            return filterOp.op(values, valueNumber, params);
        }
    }

    private static long[] toLong(int[] is)
    {
        long[] ls = new long[is.length];
        for (int i = 0; i < is.length; i++)
        {
            ls[i] = is[i] & 0xffffffffL;
        }
        return ls;
    }

    private static class UInt extends AttributeValueSelector
    {
        private final long[] params;

        UInt(DicomObject item, int valueTag, ImageSetSelectorUsageFlag usageFlag,
                FilterOp filterOp)
        {
            super(item, usageFlag, filterOp);
            int[] tmp = item.getInts(valueTag);
            if (tmp == null || tmp.length == 0)
                throw new IllegalArgumentException("Missing "
                        + TagUtils.toString(valueTag));
            if (filterOp.isNumeric() && filterOp.getNumParams() != tmp.length)
            {
                throw new IllegalArgumentException("Illegal Number of values: "
                        + item.get(valueTag));
            }
            this.params = toLong(tmp);
        }

        UInt(int tag, String privateCreator, int valueNumber, int valueTag,
                ImageSetSelectorUsageFlag usageFlag, FilterOp filterOp, VR vr,
                int[] params)
        {
            super(tag, privateCreator, valueNumber, usageFlag, filterOp, vr);
            this.params = toLong(params);
            item.putInts(valueTag, vr, params);
        }
        
       public boolean matches(DicomObject dcmobj, int frame)
        {
            int[] values = dcmobj.getInts(resolveTag(dcmobj));
            if (values == null || values.length < Math.max(valueNumber, 1))
                return match;
            return filterOp.op(values, valueNumber, params);
        }
    }

    private static HPSelector createAttributeValueSelector(ImageSetSelectorUsageFlag usageFlag,
            String privateCreator, int tag,
            int valueNumber, VR vr, float[] params,
            FilterOp filterOp)
    {
        VR vr1 = vr != null ? vr : VRMap.getPrivateVRMap(privateCreator).vrOf(tag);
        int valueTag;
        switch (vr1.code())
        {
            case 0x4453:
                valueTag = Tag.SelectorDSValue; break;
            case 0x464c:
                valueTag = Tag.SelectorFLValue; break;
            default:
                throw new IllegalArgumentException("vr: " + vr);
        }
        return new Flt(tag, privateCreator, valueNumber, valueTag, usageFlag,
                filterOp, vr, params);
    }
    
    private static class Flt extends AttributeValueSelector
    {
        private final float[] params;

        Flt(DicomObject item, int valueTag, ImageSetSelectorUsageFlag usageFlag,
                FilterOp filterOp)
        {
            super(item, usageFlag, filterOp);
            this.params = item.getFloats(valueTag);
            if (params == null || params.length == 0)
                throw new IllegalArgumentException("Missing "
                        + TagUtils.toString(valueTag));
            if (filterOp.isNumeric()
                    && filterOp.getNumParams() != params.length)
            {
                throw new IllegalArgumentException("Illegal Number of values: "
                        + item.get(valueTag));
            }
        }

        Flt(int tag, String privateCreator, int valueNumber, int valueTag,
                ImageSetSelectorUsageFlag usageFlag, FilterOp filterOp, VR vr,
                float[] params)
        {
            super(tag, privateCreator, valueNumber, usageFlag, filterOp, vr);
            this.params = (float[]) params.clone();
            item.putFloats(valueTag, vr, params);
        }        
        
        public boolean matches(DicomObject dcmobj, int frame)
        {
            float[] values = dcmobj.getFloats(resolveTag(dcmobj));
            if (values == null || values.length < Math.max(valueNumber, 1))
                return match;
            return filterOp.op(values, valueNumber, params);
        }
    }

    private static HPSelector createAttributeValueSelector(ImageSetSelectorUsageFlag usageFlag,
            String privateCreator, int tag,
            int valueNumber, double[] params, FilterOp filterOp)
    {
       return new Dbl(tag, privateCreator, valueNumber, Tag.SelectorFDValue,
               usageFlag, filterOp, VR.FD, params);
    }
    
    private static class Dbl extends AttributeValueSelector
    {
        private final double[] params;

        Dbl(DicomObject item, int valueTag, ImageSetSelectorUsageFlag usageFlag,
                FilterOp filterOp)
        {
            super(item, usageFlag, filterOp);
            this.params = item.getDoubles(valueTag);
            if (params == null || params.length == 0)
                throw new IllegalArgumentException("Missing "
                        + TagUtils.toString(valueTag));
            if (filterOp.isNumeric()
                    && filterOp.getNumParams() != params.length)
            {
                throw new IllegalArgumentException("Illegal Number of values: "
                        + item.get(valueTag));
            }
        }

        Dbl(int tag, String privateCreator, int valueNumber, int valueTag,
                ImageSetSelectorUsageFlag usageFlag, FilterOp filterOp, VR vr,
                double[] params)
        {
            super(tag, privateCreator, valueNumber, usageFlag, filterOp, vr);
            this.params = (double[]) params.clone();
            item.putDoubles(valueTag, vr, params);
        }        
                
        public boolean matches(DicomObject dcmobj, int frame)
        {
            double[] values = dcmobj.getDoubles(resolveTag(dcmobj));
            if (values == null || values.length < Math.max(valueNumber, 1))
                return match;
            return filterOp.op(values, valueNumber, params);
        }
    }

    private static class CodeValueSelector extends AttributeValueSelector
    {
        private final DicomElement params;

        CodeValueSelector(DicomObject item, ImageSetSelectorUsageFlag usageFlag,
                FilterOp filterOp)
        {
            super(item, usageFlag, filterOp);
            if (filterOp.isNumeric())
                throw new IllegalArgumentException("Filter-by Operator: "
                        + item.get(Tag.FilterbyOperator)
                        + " conflicts with non-numeric VR: SQ");
            this.params = item.get(Tag.SelectorCodeSequenceValue);
            if (params == null || params.countItems() == 0)
                throw new IllegalArgumentException(
                        "Missing (0072,0080) Selector Code Sequence Value");
        }

        CodeValueSelector(ImageSetSelectorUsageFlag usageFlag, String privateCreator,
                int tag, Code[] params,
                FilterOp filterOp)
        {
            super(tag, privateCreator, 0, usageFlag, filterOp, VR.SQ);
            this.params = item.putSequence(Tag.SelectorCodeSequenceValue,
                    params.length);
            for (int i = 0; i < params.length; i++)
            {
                this.params.addDicomObject(params[i].getDicomObject());
            }
        }        
        
        
        public boolean matches(DicomObject dcmobj, int frame)
        {
            DicomElement values = dcmobj.get(resolveTag(dcmobj));
            if (values == null || values.isEmpty())
                return match;
            return filterOp.op(values, params);
        }
    }

    private static abstract class AttributeSelectorDecorator
    extends AttributeSelector
    {
        protected final HPSelector selector;

        AttributeSelectorDecorator(int tag, String privateCreator,
                boolean match, HPSelector selector)
        {
            super(tag, privateCreator, match);
            this.selector = selector;
        }

        public DicomObject getDicomObject()
        {
            return selector.getDicomObject();
        }
    }
    
    private static class Seq
    extends AttributeSelectorDecorator
    {
        Seq(int tag, String privateCreator, AttributeSelector selector)
        {
            super(tag, privateCreator,
                    selector.isMatchIfNotPresent(), selector);
        }

        public boolean matches(DicomObject dcmobj, int frame)
        {
            DicomElement values1 = dcmobj.get(resolveTag(dcmobj));
            if (values1 == null || values1.isEmpty())
                return match;
            for (int i = 0, n = values1.countItems(); i < n; i++)
            {
                if (selector.matches(values1.getDicomObject(i), frame))
                    return true;
            }
            return false;
        }

    }

    private static class FctGrp
    extends AttributeSelectorDecorator
    {
        FctGrp(int tag, String privateCreator, AttributeSelector selector)
        {
            super(tag, privateCreator,
                    selector.isMatchIfNotPresent(), selector);
        }

        public boolean matches(DicomObject dcmobj, int frame)
        {
            DicomObject sharedFctGrp = dcmobj.getNestedDicomObject(Tag.SharedFunctionalGroupsSequence);
            if (sharedFctGrp != null)
            {
                DicomElement fctGrp = sharedFctGrp.get(resolveTag(sharedFctGrp));
                if (fctGrp != null)
                {
                    return matches(fctGrp, frame);
                }
            }
            DicomElement frameFctGrpSeq = dcmobj.get(Tag.PerframeFunctionalGroupsSequence);
            if (frameFctGrpSeq == null)
                return match;
            if (frame != 0)
            {
                return op(frameFctGrpSeq.getDicomObject(frame - 1), frame);
            }
            for (int i = 0, n = frameFctGrpSeq.countItems(); i < n; i++)
            {
                if (op(frameFctGrpSeq.getDicomObject(i), frame))
                    return true;
            }
            return false;
        }

        private boolean op(DicomObject frameFctGrp, int frame)
        {
            if (frameFctGrp == null)
                return match;
            DicomElement fctGrp = frameFctGrp.get(resolveTag(frameFctGrp));
            if (fctGrp == null)
                return match;
            return matches(fctGrp, frame);
        }

        private boolean matches(DicomElement fctGrp, int frame)
        {
            for (int i = 0, n = fctGrp.countItems(); i < n; i++)
            {
                if (selector.matches(fctGrp.getDicomObject(i), frame))
                    return true;
            }
            return false;
        }

    }
}
