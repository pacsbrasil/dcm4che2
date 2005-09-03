/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4che2.data;

import org.apache.log4j.or.ObjectRenderer;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Reversion$ $Date$
 * @since Sep 2, 2005
 *
 */
public class DicomObjectLog4jRenderer implements ObjectRenderer {

    private static final int DEF_MAX_WIDTH = 120;
    private static final int MIN_MAX_WIDTH = 32;
    private static final int MAX_MAX_WIDTH = 512;
    private static final int DEF_MAX_VAL_LEN = 64;
    private static final int MIN_MAX_VAL_LEN = 16;
    private static final int MAX_MAX_VAL_LEN = 512;
    private static final String TRUE = "true";
    
    private static final String indent = 
            System.getProperty("DicomObjectLog4jRenderer.indent", "");
    
    private static final int maxValLen = 
            getIntProperty("DicomObjectLog4jRenderer.maxValLen", 
                    DEF_MAX_VAL_LEN, MIN_MAX_VAL_LEN, MAX_MAX_VAL_LEN);
    
    private static final int maxWidth =
            getIntProperty("DicomObjectLog4jRenderer.maxWidth", 
                    DEF_MAX_WIDTH, MIN_MAX_WIDTH, MAX_MAX_WIDTH);
    
    private static final boolean withNames = TRUE.equalsIgnoreCase(
            System.getProperty("DicomObjectLog4jRenderer.withNames", TRUE));
    
    private static final String lineSeparator = 
            System.getProperty("line.separator", "\n");

    private static int getIntProperty(String key, int def, int min, int max) {
        String s = System.getProperty(key);
        if (s != null) {
            try {
                int i = Integer.parseInt(s);
                if (i >= min && i <= max)
                    return i;
            } catch (NumberFormatException e) {}
        }
        return def;
    }

    public String doRender(Object o) {
        DicomObject dcm = (DicomObject) o;
        return dcm.toStringBuffer(null, indent, maxValLen, maxWidth, withNames,
                lineSeparator).toString();
    }

}
