/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4che2.data;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Reversion$ $Date$
 * @since Sep 6, 2005
 *
 */
public final class DicomObjectToStringParam {
    
    private static DicomObjectToStringParam defParam = 
        new DicomObjectToStringParam(
                true,   // name
                64,     // valueLength;
                10,     // numItems;
                100,    // lineLength;
                100,    // numLines;
                "",     // indent
                System.getProperty("line.separator", "\n"));
    
    public static DicomObjectToStringParam getDefaultParam() {
        return defParam;
    }
    
    public static void setDefaultParam(DicomObjectToStringParam param) {
        if (param == null) {
            throw new NullPointerException();
        }
        DicomObjectToStringParam.defParam = param;
    }
    
    public final boolean name;
    public final int valueLength;
    public final int numItems;
    public final int lineLength;
    public final int numLines;
    public final String indent;
    public final String lineSeparator;
    
    public DicomObjectToStringParam(boolean name, int valueLength,
            int numItems, int lineLength, int numLines, String indent,
            String lineSeparator) {
        if (valueLength <= 0)
            throw new IllegalArgumentException("valueLength:" + valueLength);
        if (numItems <= 0)
            throw new IllegalArgumentException("numItems:" + numItems);
        if (lineLength <= 0)
            throw new IllegalArgumentException("lineLength:" + lineLength);
        if (numLines <= 0)
            throw new IllegalArgumentException("numLines:" + numLines);
        if (lineSeparator == null)
            throw new NullPointerException();
        if (indent == null)
            throw new NullPointerException();
        this.name = name;
        this.valueLength = valueLength;
        this.numItems = numItems;
        this.lineLength = lineLength;
        this.numLines = numLines;
        this.indent = indent;
        this.lineSeparator = lineSeparator;
    }
    
    public String toString() {
        return "DicomObjectToStringParam[name=" + name + ",valueLength="
                + valueLength + ",numItems=" + numItems + ",lineLength="
                + lineLength + ",numLines=" + numLines + ",indent=" + indent
                + ",lineSeparator=" + lineSeparator + "]";
    }
    
}
