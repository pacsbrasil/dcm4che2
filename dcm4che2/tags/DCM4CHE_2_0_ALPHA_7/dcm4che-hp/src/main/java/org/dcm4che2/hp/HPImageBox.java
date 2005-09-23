/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4che2.hp;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision$ $Date$
 * @since Aug 8, 2005
 *
 */
public class HPImageBox {
    public static final String TILED = "TILED";
    public static final String STACK = "STACK";
    public static final String CINE = "CINE";
    public static final String PROCESSED = "PROCESSED";
    public static final String SINGLE = "SINGLE";
    
    public static final String VERTICAL = "VERTICAL";
    public static final String HORIZONTAL = "HORIZONTAL";
    
    public static final String PAGE = "PAGE";
    public static final String ROW_COLUMN = "ROW_COLUMN";
    public static final String IMAGE = "IMAGE";
    
    private final DicomObject dcmobj;
    
    public HPImageBox(DicomObject item, int tot) {
        if (item.getInt(Tag.ImageBoxNumber) != item.getItemPosition())
            throw new IllegalArgumentException(
                    "" + item.get(Tag.ImageBoxNumber));
        if (tot > 1) {
            if (!TILED.equals(item.getString(Tag.ImageBoxLayoutType)))
                throw new IllegalArgumentException(
                        "" + item.get(Tag.ImageBoxLayoutType));
        }
        this.dcmobj = item;
    }
    
    public DicomObject getDicomObject() {
        return dcmobj;        
    }
    
    public int getImageBoxNumber() {
        return dcmobj.getInt(Tag.ImageBoxNumber);
    }    

    public double[] getDisplayEnvironmentSpatialPosition() {
        return dcmobj.getDoubles(Tag.DisplayEnvironmentSpatialPosition);
    }

    public String getImageBoxLayoutType() {
        return dcmobj.getString(Tag.ImageBoxLayoutType);
    }

    public boolean isImageBoxLayoutType(String type) {
        return type.equals(getImageBoxLayoutType());
    }

    public int getImageBoxTileHorizontalDimension() {
        return dcmobj.getInt(Tag.ImageBoxTileHorizontalDimension);
    }    

    public int getImageBoxTileVerticalDimension() {
        return dcmobj.getInt(Tag.ImageBoxTileVerticalDimension);
    }    

    public String getImageBoxScrollDirection() {
        return dcmobj.getString(Tag.ImageBoxScrollDirection);
    }

    public boolean isImageBoxScrollDirection(String direction) {
        return direction.equals(getImageBoxScrollDirection());
    }

    public String getImageBoxSmallScrollType() {
        return dcmobj.getString(Tag.ImageBoxSmallScrollType);
    }

    public boolean isImageBoxSmallScrollType(String type) {
        return type.equals(getImageBoxSmallScrollType());
    }

    public int getImageBoxSmallScrollAmount() {
        return dcmobj.getInt(Tag.ImageBoxSmallScrollAmount);
    }    

    public String getImageBoxLargeScrollType() {
        return dcmobj.getString(Tag.ImageBoxLargeScrollType);
    }

    public boolean isImageBoxLargeScrollType(String type) {
        return type.equals(getImageBoxLargeScrollType());
    }

    public int getImageBoxLargeScrollAmount() {
        return dcmobj.getInt(Tag.ImageBoxLargeScrollAmount);
    }    

    public int getImageBoxOverlapPriority() {
        return dcmobj.getInt(Tag.ImageBoxOverlapPriority);
    }    

    public int getPreferredPlaybackSequencing() {
        return dcmobj.getInt(Tag.PreferredPlaybackSequencing);
    }    

    public int getRecommendedDisplayFrameRate() {
        return dcmobj.getInt(Tag.RecommendedDisplayFrameRate);
    }    

    public double getCineRelativetoRealTime() {
        return dcmobj.getDouble(Tag.CineRelativetoRealTime);
    }    

 }
