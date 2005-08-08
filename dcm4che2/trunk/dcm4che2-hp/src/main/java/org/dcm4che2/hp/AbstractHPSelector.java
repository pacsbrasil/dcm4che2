/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4che2.hp;

import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.util.TagUtils;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Reversion$ $Date$
 * @since Jul 30, 2005
 *
 */
abstract class AbstractHPSelector implements HPSelector {

    public abstract boolean matches(DicomObject dcmobj, int frame);

    public static HPSelector createImageSetSelector(DicomObject iss) {
        boolean match = getImageSetSelectorUsageFlag(iss);
        return addContext(
                createAttributeValueSelector(iss, match, FilterOp.MEMBER_OF),
                iss, match);
    }

    private static boolean getImageSetSelectorUsageFlag(DicomObject iss) {
        final String usageFlag = iss.getString(Tag.ImageSetSelectorUsageFlag);
        if (usageFlag == null)
            throw new IllegalArgumentException(
                    "Missing (0072,0024) Image Set Selector Usage Flag");            
        if ("MATCH".equals(usageFlag))
            return true;
        if ("NO_MATCH".equals(usageFlag))
            return false;
        throw new IllegalArgumentException(
                "" + iss.get(Tag.ImageSetSelectorUsageFlag));                        
    }

    public static HPSelector createDisplaySetFilter(DicomObject item) {
        if (item.containsValue(Tag.FilterbyCategory)) {
            return HangingProtocol.createFilterByCategory(item);
        }
        return addContext(createDisplaySetSelector(item), item, true);
    }
        
    private static HPSelector addContext(HPSelector sel, DicomObject ctx,
            boolean match) {
        int seqTag = ctx.getInt(Tag.SelectorSequencePointer);
        if (seqTag != 0) {
            String seqTagPrivCreator = 
                    ctx.getString(Tag.SelectorSequencePointerPrivateCreator);
            sel = new Seq(seqTag, seqTagPrivCreator, match, sel);
        }
        int fgTag = ctx.getInt(Tag.FunctionalGroupPointer);
        if (fgTag != 0) {
            String fgTagPrivCreator = 
                    ctx.getString(Tag.FunctionalGroupPrivateCreator);
            sel = new FctGrp(fgTag, fgTagPrivCreator, match, sel);
        }
        return sel;
    }
    
    private static HPSelector createAttributeValueSelector(DicomObject item,
            boolean match, FilterOp filterOp) {
        String vrStr = item.getString(Tag.SelectorAttributeVR);
        if (vrStr == null) {
            throw new IllegalArgumentException(
                    "Missing (0072,0050) Selector Attribute VR in " +
                    "Item of (0072,0022) Image Set Selector Sequence");
        }
        if (vrStr.length() == 2) {
            switch (vrStr.charAt(0) << 8 | vrStr.charAt(1)) {
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
                "(0072,0050) Selector Attribute VR: " + vrStr + 
                        " in Item of (0072,0022) Image Set Selector Sequence");
    }
    
    private static HPSelector createDisplaySetSelector(DicomObject item) {
        String presence = item.getString(Tag.FilterbyAttributePresence);
        if (presence != null) {
            boolean not = "NOT_PRESENT".equals(presence);
            if (!not && !"PRESENT".equals(presence)) {
                throw new IllegalArgumentException(
                        "(0072,0404) Filter-by Attribute Presence Attribute: " + 
                        presence);                        
            }
            return new AttributePresenceSelector(item, not);
        }
        return createAttributeValueSelector(item, true, FilterOp.valueOf(item));
    }

    private abstract static class AttributeSelector extends AbstractHPSelector {        
        protected final int tag;
        protected final String privateCreator;
        protected final boolean match;
        AttributeSelector(int tag, String privateCreator, boolean match) {
            this.tag = tag;
            this.privateCreator = privateCreator;
            this.match = match;
        }

        AttributeSelector(DicomObject item, boolean match) {
            this(item.getInt(Tag.SelectorAttribute),
                    item.getString(Tag.SelectorAttributePrivateCreator),
                    match); 
            if (tag == 0)
                throw new IllegalArgumentException(
                        "Missing (0072,0026) Selector Attribute");
        }
        
        protected int resolveTag(DicomObject dcmobj) {
            return privateCreator == null ? tag
                    : dcmobj.resolvePrivateTag(tag, privateCreator);
        }

    }
    
    private static class AttributePresenceSelector extends AttributeSelector {        
        AttributePresenceSelector(DicomObject item, boolean not) {
            super(item, not);
        }
        
        public boolean matches(DicomObject dcmobj, int frame) {
            return dcmobj.containsValue(resolveTag(dcmobj)) ? !match : match;
        }
    }
    
    private static abstract class AttributeValueSelector extends AttributeSelector {
        protected final int valueNumber;
        protected final FilterOp filterOp;
        AttributeValueSelector(DicomObject item, boolean match,
                FilterOp filterOp) {
            super(item, match);
            this.valueNumber = item.getInt(Tag.SelectorValueNumber);
            this.filterOp = filterOp;
        }        
    }
    
    private static class Str extends AttributeValueSelector {        
        protected final String[] params;
        
        Str(DicomObject item, int valueTag, boolean match, FilterOp filterOp) {
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

        public boolean matches(DicomObject dcmobj, int frame) {
            String[] values = dcmobj.getStrings(resolveTag(dcmobj));
            if (values == null || values.length < Math.max(valueNumber, 1))
                return match;
            return filterOp.op(values, valueNumber, params);
        }
    }
    
    private static class Int extends AttributeValueSelector {        
        private final int[] params;
        
        Int(DicomObject item, int valueTag, boolean match, FilterOp filterOp) {
            super(item, match, filterOp);
            this.params = item.getInts(valueTag);
            if (params == null || params.length == 0)
                throw new IllegalArgumentException(
                        "Missing " + TagUtils.toString(valueTag));
            if (filterOp.isNumeric() && filterOp.getNumParams() != params.length) {
                throw new IllegalArgumentException(
                        "Illegal Number of values: " + item.get(valueTag));           
            }
        }

        public boolean matches(DicomObject dcmobj, int frame) {
            int[] values = dcmobj.getInts(resolveTag(dcmobj));
            if (values == null || values.length < Math.max(valueNumber, 1))
                return match;
            return filterOp.op(values, valueNumber, params);
        }
    }
    
    private static class UInt extends AttributeValueSelector {        
        private final long[] params;
        
        UInt(DicomObject item, int valueTag, boolean match, FilterOp filterOp) {
            super(item, match, filterOp);
            int[] tmp = item.getInts(valueTag);
            if (tmp == null || tmp.length == 0)
                throw new IllegalArgumentException(
                        "Missing " + TagUtils.toString(valueTag));
            if (filterOp.isNumeric() && filterOp.getNumParams() != tmp.length) {
                throw new IllegalArgumentException(
                        "Illegal Number of values: " + item.get(valueTag));           
            }
            this.params = new long[tmp.length];
            for (int i = 0; i < params.length; i++) {
                this.params[i] = tmp[i] & 0xffffffffL;
            }
        }

        public boolean matches(DicomObject dcmobj, int frame) {
            int[] values = dcmobj.getInts(resolveTag(dcmobj));
            if (values == null || values.length < Math.max(valueNumber, 1))
                return match;
            return filterOp.op(values, valueNumber, params);
        }
    }
    
    private static class Flt extends AttributeValueSelector {       
        private final float[] params;
        
        Flt(DicomObject item, int valueTag, boolean match, FilterOp filterOp) {
            super(item, match, filterOp);
            this.params = item.getFloats(valueTag);
            if (params == null || params.length == 0)
                throw new IllegalArgumentException(
                        "Missing " + TagUtils.toString(valueTag));
            if (filterOp.isNumeric() && filterOp.getNumParams() != params.length) {
                throw new IllegalArgumentException(
                        "Illegal Number of values: " + item.get(valueTag));           
            }
        }

        public boolean matches(DicomObject dcmobj, int frame) {
            float[] values = dcmobj.getFloats(resolveTag(dcmobj));
            if (values == null || values.length < Math.max(valueNumber, 1))
                return match;
            return filterOp.op(values, valueNumber, params);
        }        
    }
    
    private static class Dbl extends AttributeValueSelector {       
        private final double[] params;
        
        Dbl(DicomObject item, int valueTag, boolean match, FilterOp filterOp) {
            super(item, match, filterOp);
            this.params = item.getDoubles(valueTag);
            if (params == null || params.length == 0)
                throw new IllegalArgumentException(
                        "Missing " + TagUtils.toString(valueTag));
            if (filterOp.isNumeric() && filterOp.getNumParams() != params.length) {
                throw new IllegalArgumentException(
                        "Illegal Number of values: " + item.get(valueTag));           
            }
        }

        public boolean matches(DicomObject dcmobj, int frame) {
            double[] values = dcmobj.getDoubles(resolveTag(dcmobj));
            if (values == null || values.length < Math.max(valueNumber, 1))
                return match;
            return filterOp.op(values, valueNumber, params);
        }        
    }
    
    private static class Code extends AttributeValueSelector {
        private final DicomElement params;
        
        Code(DicomObject item, boolean match, FilterOp filterOp) {
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

        public boolean matches(DicomObject dcmobj, int frame) {
            DicomElement values = dcmobj.get(resolveTag(dcmobj));
            if (values == null || values.isNull() )
                return match;
            return filterOp.op(values, params);
        }
    }

    private static class Seq extends AttributeSelector {
        private final HPSelector selector;

        Seq(int tag, String privateCreator, boolean match,
                HPSelector selector) {
            super(tag, privateCreator, match);
            this.selector = selector;
        }

        public boolean matches(DicomObject dcmobj, int frame) {
            DicomElement values1 = dcmobj.get(resolveTag(dcmobj));
            if (values1 == null || values1.isNull() )
                return match;
            for (int i = 0, n = values1.countItems(); i < n; i++) {
                if (selector.matches(values1.getItem(i), frame))
                    return true;
            }
            return false;
        }
        
    }
    
    private static class FctGrp extends AttributeSelector {
        private final HPSelector selector;

        FctGrp(int tag, String privateCreator, boolean match,
                HPSelector selector) {
            super(tag, privateCreator, match);
            this.selector = selector;
        }

        public boolean matches(DicomObject dcmobj, int frame) {
            DicomObject sharedFctGrp = 
                    dcmobj.getItem(Tag.SharedFunctionalGroupsSequence);
            if (sharedFctGrp != null) {
                DicomElement fctGrp = sharedFctGrp.get(resolveTag(sharedFctGrp));
                if (fctGrp != null) {
                    return matches(fctGrp, frame);
                }
            }
            DicomElement frameFctGrpSeq = 
                    dcmobj.get(Tag.PerframeFunctionalGroupsSequence);
            if (frameFctGrpSeq == null)
                return match;
            if (frame != 0) {
                return op(frameFctGrpSeq.getItem(frame-1), frame);
            }
            for (int i = 0, n = frameFctGrpSeq.countItems(); i < n; i++) {
                if (op(frameFctGrpSeq.getItem(i), frame))
                    return true;
            }
            return false;
        }

        private boolean op(DicomObject frameFctGrp, int frame) {
            if (frameFctGrp == null)
                return match;
            DicomElement fctGrp = frameFctGrp.get(resolveTag(frameFctGrp));
            if (fctGrp == null)
                return match;
            return matches(fctGrp, frame);
        }
        
        private boolean matches(DicomElement fctGrp, int frame) {
            for (int i = 0, n = fctGrp.countItems(); i < n; i++) {
                if (selector.matches(fctGrp.getItem(i), frame))
                    return true;
            }
            return false;
        }
        
    }    
 }