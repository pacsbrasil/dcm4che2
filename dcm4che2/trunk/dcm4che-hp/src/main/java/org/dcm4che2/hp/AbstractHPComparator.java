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

import java.util.Date;

import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision$ $Date$
 * @since Aug 1, 2005
 *
 */
public abstract class AbstractHPComparator implements HPComparator {

    public abstract int compare(DicomObject o1, int frame1,
            DicomObject o2, int frame2);

    public static HPComparator valueOf(DicomObject sortingOp) {
        if (sortingOp.containsValue(Tag.SortbyCategory))
            return HangingProtocol.createSortByCategory(sortingOp);
        int tag = sortingOp.getInt(Tag.SelectorAttribute); 
        if (tag == 0) {
            throw new IllegalArgumentException(
                    "Missing (0072,0026) Selector Attribute");
        }
        String privateCreator = sortingOp.getPrivateCreator(
                Tag.SelectorAttributePrivateCreator);
        int valueNumber = sortingOp.getInt(Tag.SelectorValueNumber);
        if (valueNumber == 0) {
            throw new IllegalArgumentException(
                "Missing or invalid (0072,0028) Selector Value Number: "
                    + sortingOp.get(Tag.SelectorValueNumber));                
        }
        int sortingDirection = SortingDirection.toSign(
                sortingOp.getString(Tag.SortingDirection));
        SortByAttribute cmp = new SortByAttribute(tag, privateCreator, 
                valueNumber, sortingDirection);
        return addContext(cmp, sortingOp);
    }

    private static HPComparator addContext(HPComparator cmp, DicomObject ctx) {
        int seqTag = ctx.getInt(Tag.SelectorSequencePointer);
        if (seqTag != 0) {
            String seqTagPrivCreator = 
                    ctx.getString(Tag.SelectorSequencePointerPrivateCreator);
            cmp = new Seq(seqTag, seqTagPrivCreator, cmp);
        }
        int fgTag = ctx.getInt(Tag.FunctionalGroupPointer);
        if (fgTag != 0) {
            String fgTagPrivCreator = 
                    ctx.getString(Tag.FunctionalGroupPrivateCreator);
            cmp = new FctGrp(fgTag, fgTagPrivCreator, cmp);
        }
        return cmp;
    }
    
    private static abstract class AttributeComparator extends AbstractHPComparator {
        protected final int tag;
        protected final String privateCreator;
        AttributeComparator(int tag, String privateCreator) {
            this.tag = tag;
            this.privateCreator = privateCreator;
        }
        
        protected int resolveTag(DicomObject dcmobj) {
            return privateCreator == null ? tag
                    : dcmobj.resolveTag(tag, privateCreator);
        }
    }
    
    private static class SortByAttribute extends AttributeComparator {
        private final int valueNumber;
        private final int sortingDirection;
        
        SortByAttribute(int tag, String privateCreator, int valueNumber,
                int sortingDirection) {
            super(tag, privateCreator);
            this.valueNumber = valueNumber;
            this.sortingDirection = sortingDirection;
        }

        public int compare(DicomObject o1, int frame1,
                           DicomObject o2, int frame2) {
            DicomElement e1 = o1.get(resolveTag(o1));
            if (e1 == null) 
                return 0;
            DicomElement e2 = o2.get(resolveTag(o2));
            if (e2 == null) 
                return 0;
            if (e1.vr() != e2.vr()) 
                return 0;
            switch (e1.vr().code()) {
            case 0x4145: // AE
            case 0x4153: // AS
            case 0x4353: // CS
            case 0x4c4f: // LO
            case 0x4c54: // LT;
            case 0x504e: // PN;
            case 0x5348: // SH;
            case 0x5354: // ST;
            case 0x5549: // UI;
            case 0x5554: // UT;
                return strcmp(e1.getStrings(o1.getSpecificCharacterSet(), true),
                        e2.getStrings(o2.getSpecificCharacterSet(), true));
            case 0x4154: // AT
            case 0x554c: // UL;
            case 0x5553: // US;
                return uintcmp(e1.getInts(true), e2.getInts(true));
            case 0x4441: // DA
            case 0x4454: // DT
            case 0x544d: // TM;
                return datecmp(e1.getDates(true), e2.getDates(true));
            case 0x4453: // DS
            case 0x464c: // FL
                return fltcmp(e1.getFloats(true), e2.getFloats(true));
            case 0x4644: // FD
                return dblcmp(e1.getDoubles(true), e2.getDoubles(true));
            case 0x4953: // IS
            case 0x534c: // SL;
            case 0x5353: // SS;
                return intcmp(e1.getInts(true), e2.getInts(true));
            case 0x5351: // SQ;
                return codecmp(e1.getDicomObject(), e2.getDicomObject());
            }
            // no sort if VR = OB, OF, OW or UN
            return 0;
        }

        private int codecmp(DicomObject c1, DicomObject c2) {
            if (c1 == null || c2 == null)
                return 0;
            String v1 = c1.getString(Tag.CodeValue);
            String v2 = c2.getString(Tag.CodeValue);
            if (v1 == null || v2 == null)
                return 0;
            return v1.compareTo(v2) * sortingDirection;
        }

        private int intcmp(int[] v1, int[] v2) {
            if (v1 == null || v2 == null 
                    || v1.length < valueNumber
                    || v2.length < valueNumber)
                return 0;
            
            if (v1[valueNumber-1] < v2[valueNumber-1]) return sortingDirection;
            if (v1[valueNumber-1] > v2[valueNumber-1]) return -sortingDirection;
            return 0;
        }

        private int dblcmp(double[] v1, double[] v2) {
            if (v1 == null || v2 == null 
                    || v1.length < valueNumber
                    || v2.length < valueNumber)
                return 0;
            if (v1[valueNumber-1] < v2[valueNumber-1]) return sortingDirection;
            if (v1[valueNumber-1] > v2[valueNumber-1]) return -sortingDirection;
            return 0;
        }

        private int fltcmp(float[] v1, float[] v2) {
            if (v1 == null || v2 == null 
                    || v1.length < valueNumber
                    || v2.length < valueNumber)
                return 0;
            if (v1[valueNumber-1] < v2[valueNumber-1]) return sortingDirection;
            if (v1[valueNumber-1] > v2[valueNumber-1]) return -sortingDirection;
            return 0;
        }

        private int datecmp(Date[] v1, Date[] v2) {
            if (v1 == null || v2 == null 
                    || v1.length < valueNumber
                    || v2.length < valueNumber)
                return 0;
            return v1[valueNumber-1].compareTo(v2[valueNumber-1]) * sortingDirection;
        }

        private int uintcmp(int[] v1, int[] v2) {
            if (v1 == null || v2 == null 
                    || v1.length < valueNumber
                    || v2.length < valueNumber)
                return 0;
            long l1 = v1[valueNumber-1] & 0xffffffffL;
            long l2 = v2[valueNumber-1] & 0xffffffffL;
            if (l1 < l2) return sortingDirection;
            if (l1 > l2) return -sortingDirection;
            return 0;
        }

        private int strcmp(String[] v1, String[] v2) {
            if (v1 == null || v2 == null 
                    || v1.length < valueNumber
                    || v2.length < valueNumber)
                return 0;
            return v1[valueNumber-1].compareTo(v2[valueNumber-1]) * sortingDirection;
        }
    }

    private static class Seq extends AttributeComparator {
        private final HPComparator cmp;

        Seq(int tag, String privateCreator, HPComparator cmp) {
            super(tag, privateCreator);
            this.cmp = cmp;
        }

        public int compare(DicomObject o1, int frame1,
                           DicomObject o2, int frame2) {
            DicomObject v1 = o1.getNestedDicomObject(resolveTag(o1));
            if (v1 == null) 
                return 0;
            DicomObject v2 = o2.getNestedDicomObject(resolveTag(o2));
            if (v2 == null) 
                return 0;
            return cmp.compare(v1, frame1, v2, frame2);
        }        
    }
    
    private static class FctGrp extends AttributeComparator {
        private final HPComparator cmp;

        FctGrp(int tag, String privateCreator, HPComparator cmp) {
            super(tag, privateCreator);
            this.cmp = cmp;
        }

        public int compare(DicomObject o1, int frame1,
                           DicomObject o2, int frame2) {
            DicomObject fg1 = fctGrp(o1, frame1);
            if (fg1 == null)
                return 0;
            DicomObject fg2 = fctGrp(o1, frame1);
            if (fg2 == null)
                return 0;
            return cmp.compare(fg1, frame1, fg2, frame2);
        }
        
        private DicomObject fctGrp(DicomObject o, int frame) {
            DicomObject sharedFctGrp = o.getNestedDicomObject(Tag.SharedFunctionalGroupsSequence);
            if (sharedFctGrp != null) {
                DicomObject fctGrp = sharedFctGrp.getNestedDicomObject(resolveTag(sharedFctGrp));
                if (fctGrp != null) {
                    return fctGrp;
                }
            }
            DicomElement frameFctGrpSeq = o.get(Tag.PerframeFunctionalGroupsSequence);
            if (frameFctGrpSeq == null)
                return null;
            DicomObject frameFctGrp = frameFctGrpSeq.getDicomObject(frame-1);
                return frameFctGrp.getNestedDicomObject(resolveTag(frameFctGrp));
        }

    }
    
    
}
