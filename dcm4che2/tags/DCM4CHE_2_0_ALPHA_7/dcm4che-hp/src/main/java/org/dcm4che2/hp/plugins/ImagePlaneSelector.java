/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4che2.hp.plugins;

import java.util.Arrays;
import java.util.List;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.hp.HPSelector;

class ImagePlaneSelector implements HPSelector {
    private static final String[] VALUES = {
        "SAGITTAL", "CORONAL", "AXIAL", "OBLIQUE"
    };
    private static final int SAGITTAL = 0;
    private static final int CORONAL = 1;
    private static final int AXIAL = 2;
    private static final int OBLIQUE = 3;
    private static final int R = 0;
    private static final int C = 1;
    private static final int RX = 0;
    private static final int RY = 1;
    private static final int RZ = 2;
    private static final int CX = 3;
    private static final int CY = 4;
    private static final int CZ = 5;

    private final float minCosine;
    private final int[] values;
    public ImagePlaneSelector(DicomObject filterOp, float minCosine) {
        String vrStr = filterOp.getString(Tag.SelectorAttributeVR);
        if (vrStr == null) {
            throw new IllegalArgumentException(
                    "Missing (0072,0050) AbstractHPSelector Attribute VR in " +
                    "Item of (0072,0022) Image Set AbstractHPSelector Sequence");
        }
        if (!"CS".equals(vrStr)) {
            throw new IllegalArgumentException(
                "(0072,0050) AbstractHPSelector Attribute VR: " + vrStr + 
                " in Item of (0072,0400) Filter Operations Sequence");                
        }
        String[] ss = filterOp.getStrings(Tag.SelectorCSValue);
        if (ss == null || ss.length == 0)
            throw new IllegalArgumentException(
                    "Missing (0072,0062) AbstractHPSelector CS Value");
        values = new int[ss.length];
        List VALUE_LIST = Arrays.asList(VALUES);
        for (int i = 0; i < ss.length; i++) {
            if ((values[i] = VALUE_LIST.indexOf(ss[i])) == -1)
                throw new IllegalArgumentException(
                        "" + filterOp.get(Tag.SelectorCSValue));
        }
        this.minCosine = minCosine;
    }

    public boolean matches(DicomObject dcmobj, int frame) {
        int value1;
        float[] iop = dcmobj.getFloats(Tag.ImageOrientationPatient);
        if (iop != null && iop.length == 6) {
            value1 = fromImageOrientation(iop);
        } else {
            String[] po = dcmobj.getStrings(Tag.PatientOrientation);
            if (po != null && po.length == 2) {
                value1 = fromPatientOrientation(po);                 
            } else {
                return true;
            }
        }
        for (int i = 0; i < values.length; i++) {
            if (value1 == values[i])
                return true;
        }
        return false;
    }

    private int fromPatientOrientation(String[] po) {
        final String r = po[R];
        final String c = po[C];
        if (r.indexOf('H') == -1 && r.indexOf('F') == -1 
                && c.indexOf('H') == -1 && c.indexOf('F') == -1)
            return AXIAL;
        if (r.indexOf('A') == -1 && r.indexOf('P') == -1 
                && c.indexOf('A') == -1 && c.indexOf('P') == -1)
            return CORONAL;
        if (r.indexOf('L') == -1 && r.indexOf('R') == -1
                && c.indexOf('L') == -1 && c.indexOf('R') == -1)
            return SAGITTAL;
        return OBLIQUE;
    }

    private int fromImageOrientation(float[] iop) {
        if (Math.abs(iop[RX] * iop[CY] - iop[RY] * iop[CX]) >= minCosine)
            return AXIAL;
        if (Math.abs(iop[RY] * iop[CZ] - iop[RZ] * iop[CY]) >= minCosine)
            return SAGITTAL;
        if (Math.abs(iop[RZ] * iop[CX] - iop[RX] * iop[CZ]) >= minCosine)
            return CORONAL;
        return OBLIQUE;
    }

}