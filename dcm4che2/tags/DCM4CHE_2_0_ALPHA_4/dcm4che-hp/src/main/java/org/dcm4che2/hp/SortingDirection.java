/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4che2.hp;


/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision$ $Date$
 * @since Aug 7, 2005
 *
 */
public class SortingDirection {

    public static final int INCREASING = 1;
    public static final int DECREASING = -1;
    
    public static int toSign(String sortingDirection) {
        if (sortingDirection == null)
            throw new IllegalArgumentException(
                "Missing (0072,0604) Sorting Direction");            
        if ("INCREASING".equals(sortingDirection))
            return INCREASING;
        if ("DECREASING".equals(sortingDirection))
            return DECREASING;
        throw new IllegalArgumentException(
                "Illegal (0072,0604) Sorting Direction:" + sortingDirection);        
    }
}
