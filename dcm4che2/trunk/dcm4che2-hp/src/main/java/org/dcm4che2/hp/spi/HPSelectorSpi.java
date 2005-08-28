/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4che2.hp.spi;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.hp.HPSelector;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision$ $Date$
 * @since Aug 6, 2005
 *
 */
public abstract class HPSelectorSpi extends HPCategorySpi {
    public HPSelectorSpi(String[] categories) {
        super(categories);
    }

    public abstract HPSelector createHPSelector(DicomObject filterOp);
}
