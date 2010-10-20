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

package org.dcm4che2.util;

public class GenericNumericArray {
    public enum TYPE {
        BYTE, SHORT, INT, LONG, FLOAT, DOUBLE
    };

    private TYPE type;
    private byte[] byteArr = null;
    private short[] shortArr = null;
    private int[] intArr = null;
    private long[] longArr = null;
    private float[] floatArr = null;
    private double[] doubleArr = null;

    public GenericNumericArray(TYPE type, int size) {
        this.type = type;

        switch (type) {
        case BYTE:
            byteArr = new byte[size];
            break;
        case SHORT:
            shortArr = new short[size];
            break;
        case INT:
            intArr = new int[size];
            break;
        case LONG:
            longArr = new long[size];
            break;
        case FLOAT:
            floatArr = new float[size];
            break;
        case DOUBLE:
            doubleArr = new double[size];
            break;
        }
    }

    public GenericNumericArray(Object array) {
        if (array == null)
            throw new NullPointerException("Array is NULL");

        if (array instanceof byte[]) {
            type = TYPE.BYTE;
            byteArr = (byte[]) array;
        } else if (array instanceof short[]) {
            type = TYPE.SHORT;
            shortArr = (short[]) array;
        } else if (array instanceof int[]) {
            type = TYPE.INT;
            intArr = (int[]) array;
        } else if (array instanceof long[]) {
            type = TYPE.LONG;
            longArr = (long[]) array;
        } else if (array instanceof float[]) {
            type = TYPE.FLOAT;
            floatArr = (float[]) array;
        } else if (array instanceof double[]) {
            type = TYPE.DOUBLE;
            doubleArr = (double[]) array;
        } else {
            throw new IllegalArgumentException("Unknown array type: "+ array.getClass().getName());
        }
    }

    public static GenericNumericArray getByteArray(int size) {
        return new GenericNumericArray(TYPE.BYTE, size);
    }

    public static GenericNumericArray getShortArray(int size) {
        return new GenericNumericArray(TYPE.SHORT, size);
    }

    public static GenericNumericArray getIntArray(int size) {
        return new GenericNumericArray(TYPE.INT, size);
    }

    public static GenericNumericArray getLongArray(int size) {
        return new GenericNumericArray(TYPE.LONG, size);
    }

    public static GenericNumericArray getFloatArray(int size) {
        return new GenericNumericArray(TYPE.FLOAT, size);
    }

    public static GenericNumericArray getDoubleArray(int size) {
        return new GenericNumericArray(TYPE.DOUBLE, size);
    }

    public TYPE getType() {
        return type;
    }

    public Object getArray() {
        switch (type) {
        case BYTE:
            return byteArr;
        case SHORT:
            return shortArr;
        case INT:
            return intArr;
        case LONG:
            return longArr;
        case FLOAT:
            return floatArr;
        case DOUBLE:
            return doubleArr;
        }
        return null;
    }

    public Number getArrayItem(int index) {
        switch (type) {
        case BYTE:
            return byteArr[index];
        case SHORT:
            return shortArr[index];
        case INT:
            return intArr[index];
        case LONG:
            return longArr[index];
        case FLOAT:
            return floatArr[index];
        case DOUBLE:
            return doubleArr[index];
        }

        return null;
    }

    public void setArrayItem(int index, Number data) {
        switch (type) {
        case BYTE:
            byteArr[index] = data.byteValue();
            break;
        case SHORT:
            shortArr[index] = data.shortValue();
            break;
        case INT:
            intArr[index] = data.intValue();
            break;
        case LONG:
            longArr[index] = data.longValue();
            break;
        case FLOAT:
            floatArr[index] = data.floatValue();
            break;
        case DOUBLE:
            doubleArr[index] = data.doubleValue();
            break;
        }
    }
}
