/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4che2.hp.spi;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Reversion$ $Date$
 * @since Aug 6, 2005
 *
 */
abstract class HPCategorySpi {
    
    protected String[] categories;
    
    protected HPCategorySpi(String[] categories) {
        this.categories = (String[]) categories.clone();
    }
    
    public final String[] getCategories() {
        return (String[]) categories.clone();
    }
    
    public void setProperty(String name, Object value) {
        throw new IllegalArgumentException("Unsupported property: "
                + name);
    }
    
    public Object getProperty(String name) {
        throw new IllegalArgumentException("Unsupported property: "
                + name);
    }
}
