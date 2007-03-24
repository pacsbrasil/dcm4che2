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
 
package org.dcm4che2.hp.plugins;

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4che2.hp.AbstractHPComparator;
import org.dcm4che2.hp.CodeString;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Mar 24, 2007
 */
public class NMFrameComparator extends AbstractHPComparator {

    private final DicomObject sortOp;
    private final Category cat;
    private final int sign;
    
    public static final class Category {
        public static final Category ENERGY_WINDOW = 
                new Category("ENERGY_WINDOW", Tag.EnergyWindowVector);
        public static final Category DETECTOR = 
                new Category("DETECTOR", Tag.DetectorVector);
        public static final Category PHASE = 
                new Category("PHASE", Tag.PhaseVector);
        public static final Category ROTATION = 
                new Category("ROTATION", Tag.RotationVector);
        public static final Category RR_INTERVAL = 
                new Category("RR_INTERVAL", Tag.RRIntervalVector);
        public static final Category TIME_SLOT = 
                new Category("TIME_SLOT", Tag.TimeSlotVector);
        public static final Category ANGULAR_VIEW = 
                new Category("ANGULAR_VIEW", Tag.AngularViewVector);
        public static final Category SLICE = 
                new Category("SLICE", Tag.SliceVector);
        public static final Category TIME_SLICE = 
                new Category("TIME_SLICE", Tag.TimeSliceVector);
        
        public final String name;
        public final int tag;

        private Category(String name, int tag) {
            this.name = name;
            this.tag = tag;
        }
        
        public final String toString() {
            return name;
        }
        
        public static Category valueOf(String name) {
            try {
                return (Category) Category.class.getField(name).get(null);
            } catch (IllegalAccessException e) {
                throw new Error(e);
            } catch (NoSuchFieldException e) {
                throw new IllegalArgumentException(
                        "(0072,0602) Sort-by Category: " + name);
            }
        }
    }
      
    public NMFrameComparator(DicomObject sortOp) {
        this.sortOp = sortOp;
        String catStr = sortOp.getString(Tag.SortbyCategory);
        if (catStr == null) {
            throw new IllegalArgumentException(
                    "Missing (0072,0602) Sort-by Category");                            
        }
        this.cat = Category.valueOf(catStr);
        String cs = sortOp.getString(Tag.SortingDirection);
        if (cs == null)
        {
            throw new IllegalArgumentException(
                    "Missing (0072,0604) Sorting Direction");
        }
        this.sign = CodeString.sortingDirectionToSign(cs);
    }

    public NMFrameComparator(Category cat, String sortingDirection) {
        this.cat = cat;
        this.sign = CodeString.sortingDirectionToSign(sortingDirection);
        this.sortOp = new BasicDicomObject();
        sortOp.putString(Tag.SortbyCategory, VR.CS, cat.name);
        sortOp.putString(Tag.SortingDirection, VR.CS, sortingDirection);
    }

    public DicomObject getDicomObject() {
        return sortOp;
    }
    
    public int compare(DicomObject o1, int frame1, DicomObject o2, int frame2) {
        int[] v1 = o1.getInts(cat.tag);
        int[] v2 = o2.getInts(cat.tag);
        return (v1 != null && v2 != null
                && frame1 < v1.length && frame2 < v2.length)
                ? (v1[frame1] - v2[frame2]) * sign : 0;
    }
   
}
