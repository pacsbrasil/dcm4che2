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

import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.util.TagUtils;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision$ $Date$
 * @since Jul 30, 2005
 * 
 */
public class HPSelectorFactory
{

    public static HPSelector createImageSetSelector(DicomObject iss)
    {
        boolean match = getImageSetSelectorUsageFlag(iss);
        return addContext(createAttributeValueSelector(iss, match,
                FilterOp.MEMBER_OF), iss, match);
    }

    private static boolean getImageSetSelectorUsageFlag(DicomObject iss)
    {
        final String usageFlag = iss.getString(Tag.ImageSetSelectorUsageFlag);
        if (usageFlag == null)
            throw new IllegalArgumentException(
                    "Missing (0072,0024) Image Set Selector Usage Flag");
        if ("MATCH".equals(usageFlag))
            return true;
        if ("NO_MATCH".equals(usageFlag))
            return false;
        throw new IllegalArgumentException(""
                + iss.get(Tag.ImageSetSelectorUsageFlag));
    }

    public static HPSelector createDisplaySetFilter(DicomObject item)
    {
        if (item.containsValue(Tag.FilterbyCategory))
        {
            return HangingProtocol.createFilterByCategory(item);
        }
        return addContext(createDisplaySetSelector(item), item, true);
    }

    private static HPSelector addContext(HPSelector sel, DicomObject ctx,
            boolean match)
    {
        sel = addSequencePointer(sel, ctx, match);
        sel = addFunctionalGroupPointer(sel, ctx, match);
        return sel;
    }

    private static HPSelector addSequencePointer(HPSelector sel,
            DicomObject ctx, boolean match)
    {
        int seqTag = ctx.getInt(Tag.SelectorSequencePointer);
        if (seqTag != 0)
        {
            String privCreator = ctx.getString(Tag.SelectorSequencePointerPrivateCreator);
            sel = new Seq(seqTag, privCreator, match, sel);
        }
        return sel;
    }

    private static HPSelector addFunctionalGroupPointer(HPSelector sel,
            DicomObject ctx, boolean match)
    {
        int fgTag = ctx.getInt(Tag.FunctionalGroupPointer);
        if (fgTag != 0)
        {
            String privCreator = ctx.getString(Tag.FunctionalGroupPrivateCreator);
            sel = new FctGrp(fgTag, privCreator, match, sel);
        }
        return sel;
    }

    private static HPSelector createAttributeValueSelector(DicomObject item,
            boolean match, FilterOp filterOp)
    {
        String vrStr = item.getString(Tag.SelectorAttributeVR);
        if (vrStr == null)
        {
            throw new IllegalArgumentException(
                    "Missing (0072,0050) Selector Attribute VR in "
                            + "Item of (0072,0022) Image Set Selector Sequence");
        }
        if (vrStr.length() == 2)
        {
            switch (vrStr.charAt(0) << 8 | vrStr.charAt(1))
            {
                case 0x4154:
                    return new UInt(item, Tag.SelectorATValue, match, filterOp);
                case 0x4353:
                    return new Str(item, Tag.SelectorCSValue, match, filterOp);
                case 0x4453:
                    return new Flt(item, Tag.SelectorDSValue, match, filterOp);
                case 0x4644:
                    return new Dbl(item, Tag.SelectorFDValue, match, filterOp);
                case 0x464c:
                    return new Flt(item, Tag.SelectorFLValue, match, filterOp);
                case 0x4953:
                    return new Int(item, Tag.SelectorISValue, match, filterOp);
                case 0x4c4f:
                    return new Str(item, Tag.SelectorLOValue, match, filterOp);
                case 0x4c54:
                    return new Str(item, Tag.SelectorLTValue, match, filterOp);
                case 0x504e:
                    return new Str(item, Tag.SelectorPNValue, match, filterOp);
                case 0x5348:
                    return new Str(item, Tag.SelectorSHValue, match, filterOp);
                case 0x534c:
                    return new Int(item, Tag.SelectorSLValue, match, filterOp);
                case 0x5351:
                    return new Code(item, match, filterOp);
                case 0x5353:
                    return new Int(item, Tag.SelectorSSValue, match, filterOp);
                case 0x5354:
                    return new Str(item, Tag.SelectorSTValue, match, filterOp);
                case 0x554c:
                    return new UInt(item, Tag.SelectorULValue, match, filterOp);
                case 0x5553:
                    return new Int(item, Tag.SelectorUSValue, match, filterOp);
                case 0x5554:
                    return new Str(item, Tag.SelectorUTValue, match, filterOp);
            }
        }
        throw new IllegalArgumentException(
                "(0072,0050) Selector Attribute VR: " + vrStr
                        + " in Item of (0072,0022) Image Set Selector Sequence");
    }

    private static HPSelector createDisplaySetSelector(DicomObject item)
    {
        String presence = item.getString(Tag.FilterbyAttributePresence);
        if (presence != null)
        {
            boolean not = "NOT_PRESENT".equals(presence);
            if (!not && !"PRESENT".equals(presence))
            {
                throw new IllegalArgumentException(
                        "(0072,0404) Filter-by Attribute Presence Attribute: "
                                + presence);
            }
            return new AttributePresenceSelector(item, not);
        }
        return createAttributeValueSelector(item, true, FilterOp.valueOf(item));
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

        protected int resolveTag(DicomObject dcmobj)
        {
            return privateCreator == null ? tag : dcmobj.resolveTag(tag,
                    privateCreator);
        }

    }
    
    private abstract static class BaseAttributeSelector
    extends AttributeSelector
    {
        protected final DicomObject item;

        BaseAttributeSelector(DicomObject item, boolean match)
        {
            super(item.getInt(Tag.SelectorAttribute),
                    item.getString(Tag.SelectorAttributePrivateCreator), match);
            if (tag == 0)
                throw new IllegalArgumentException(
                        "Missing (0072,0026) Selector Attribute");
            this.item = item;
        }

        public final DicomObject getDicomObject()
        {
            return item;
        }        
        
    }

    private static class AttributePresenceSelector
    extends BaseAttributeSelector
    {
        AttributePresenceSelector(DicomObject item, boolean not)
        {
            super(item, not);
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

        AttributeValueSelector(DicomObject item, boolean match,
                FilterOp filterOp)
        {
            super(item, match);
            this.valueNumber = item.getInt(Tag.SelectorValueNumber);
            this.filterOp = filterOp;
        }
    }

    private static class Str extends AttributeValueSelector
    {
        protected final String[] params;

        Str(DicomObject item, int valueTag, boolean match, FilterOp filterOp)
        {
            super(item, match, filterOp);
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

        public boolean matches(DicomObject dcmobj, int frame)
        {
            String[] values = dcmobj.getStrings(resolveTag(dcmobj));
            if (values == null || values.length < Math.max(valueNumber, 1))
                return match;
            return filterOp.op(values, valueNumber, params);
        }
    }

    private static class Int extends AttributeValueSelector
    {
        private final int[] params;

        Int(DicomObject item, int valueTag, boolean match, FilterOp filterOp)
        {
            super(item, match, filterOp);
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

        public boolean matches(DicomObject dcmobj, int frame)
        {
            int[] values = dcmobj.getInts(resolveTag(dcmobj));
            if (values == null || values.length < Math.max(valueNumber, 1))
                return match;
            return filterOp.op(values, valueNumber, params);
        }
    }

    private static class UInt extends AttributeValueSelector
    {
        private final long[] params;

        UInt(DicomObject item, int valueTag, boolean match, FilterOp filterOp)
        {
            super(item, match, filterOp);
            int[] tmp = item.getInts(valueTag);
            if (tmp == null || tmp.length == 0)
                throw new IllegalArgumentException("Missing "
                        + TagUtils.toString(valueTag));
            if (filterOp.isNumeric() && filterOp.getNumParams() != tmp.length)
            {
                throw new IllegalArgumentException("Illegal Number of values: "
                        + item.get(valueTag));
            }
            this.params = new long[tmp.length];
            for (int i = 0; i < params.length; i++)
            {
                this.params[i] = tmp[i] & 0xffffffffL;
            }
        }

        public boolean matches(DicomObject dcmobj, int frame)
        {
            int[] values = dcmobj.getInts(resolveTag(dcmobj));
            if (values == null || values.length < Math.max(valueNumber, 1))
                return match;
            return filterOp.op(values, valueNumber, params);
        }
    }

    private static class Flt extends AttributeValueSelector
    {
        private final float[] params;

        Flt(DicomObject item, int valueTag, boolean match, FilterOp filterOp)
        {
            super(item, match, filterOp);
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

        public boolean matches(DicomObject dcmobj, int frame)
        {
            float[] values = dcmobj.getFloats(resolveTag(dcmobj));
            if (values == null || values.length < Math.max(valueNumber, 1))
                return match;
            return filterOp.op(values, valueNumber, params);
        }
    }

    private static class Dbl extends AttributeValueSelector
    {
        private final double[] params;

        Dbl(DicomObject item, int valueTag, boolean match, FilterOp filterOp)
        {
            super(item, match, filterOp);
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

        public boolean matches(DicomObject dcmobj, int frame)
        {
            double[] values = dcmobj.getDoubles(resolveTag(dcmobj));
            if (values == null || values.length < Math.max(valueNumber, 1))
                return match;
            return filterOp.op(values, valueNumber, params);
        }
    }

    private static class Code extends AttributeValueSelector
    {
        private final DicomElement params;

        Code(DicomObject item, boolean match, FilterOp filterOp)
        {
            super(item, match, filterOp);
            if (filterOp.isNumeric())
                throw new IllegalArgumentException("Filter-by Operator: "
                        + item.get(Tag.FilterbyOperator)
                        + " conflicts with non-numeric VR: SQ");
            this.params = item.get(Tag.SelectorCodeSequenceValue);
            if (params == null || params.countItems() == 0)
                throw new IllegalArgumentException(
                        "Missing (0072,0080) Selector Code Sequence Value");
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
        Seq(int tag, String privateCreator, boolean match, HPSelector selector)
        {
            super(tag, privateCreator, match, selector);
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
        FctGrp(int tag, String privateCreator, boolean match,
                HPSelector selector)
        {
            super(tag, privateCreator, match, selector);
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
