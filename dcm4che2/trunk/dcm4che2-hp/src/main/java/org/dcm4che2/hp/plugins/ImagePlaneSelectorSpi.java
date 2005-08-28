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
import org.dcm4che2.hp.HPSelector;
import org.dcm4che2.hp.spi.HPSelectorSpi;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision$ $Date$
 * @since Aug 6, 2005
 *
 */
public class ImagePlaneSelectorSpi extends HPSelectorSpi {

    private static final String[] CATEGORIES = { "IMAGE_PLANE" };
    private static final float MIN_MIN_COSINE = 0.8f;
    private static final float DEF_MIN_COSINE = 0.9f;
    private float minCosine = DEF_MIN_COSINE;
    
    public ImagePlaneSelectorSpi() {
        super(CATEGORIES);
    }
    
    public void setProperty(String name, Object value) {
        if (!"MinCosine".equals(name))
            throw new IllegalArgumentException("Unsupported property: "
                    + name);
        float tmp = ((Float) value).floatValue();
        if (tmp < MIN_MIN_COSINE || tmp > 1f)
            throw new IllegalArgumentException("minCosine: " + value);
        minCosine = tmp;
     }
    
    public Object getProperty(String name) {
        if (!"MinCosine".equals(name))
            throw new IllegalArgumentException("Unsupported property: "
                    + name);
        return new Float(minCosine);
    }

    public HPSelector createHPSelector(DicomObject filterOp) {
        return new ImagePlaneSelector(filterOp, minCosine);
    }
    
    
    
    

}
