/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4che2.hp.plugins;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.hp.HPComparator;
import org.dcm4che2.hp.spi.HPComparatorSpi;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Reversion$ $Date$
 * @since Aug 6, 2005
 *
 */
public class ByAcqTimeComparatorSpi extends HPComparatorSpi {

    private static final String[] CATEGORIES = { "BY_ACQ_TIME" };
    
    public ByAcqTimeComparatorSpi() {
        super(CATEGORIES);
    }

    public HPComparator createHPComparator(DicomObject sortOp) {
        return new ByAcqTimeComparator(sortOp);
    }
    
}
