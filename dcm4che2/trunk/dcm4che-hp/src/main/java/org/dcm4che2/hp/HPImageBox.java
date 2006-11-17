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

package org.dcm4che2.hp;

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision$ $Date$
 * @since Aug 8, 2005
 * 
 */
public class HPImageBox
{

    private final DicomObject dcmobj;

    public HPImageBox(DicomObject item, int tot)
    {
        if (item.getInt(Tag.IMAGE_BOX_NUMBER) != item.getItemPosition())
            throw new IllegalArgumentException(""
                    + item.get(Tag.IMAGE_BOX_NUMBER));
        if (tot > 1)
        {
            if (!CodeString.TILED.equals(item.getString(Tag.IMAGE_BOX_LAYOUT_TYPE)))
                throw new IllegalArgumentException(""
                        + item.get(Tag.IMAGE_BOX_LAYOUT_TYPE));
        }
        this.dcmobj = item;
    }

    public HPImageBox()
    {
        this.dcmobj = new BasicDicomObject();
    }
    
    public DicomObject getDicomObject()
    {
        return dcmobj;
    }

    public int getImageBoxNumber()
    {
        return dcmobj.getInt(Tag.IMAGE_BOX_NUMBER);
    }

    public void setImageBoxNumber(int value)
    {
        dcmobj.putInt(Tag.IMAGE_BOX_NUMBER, VR.US, value);
    }

    public double[] getDisplayEnvironmentSpatialPosition()
    {
        return dcmobj.getDoubles(Tag.DISPLAY_ENVIRONMENT_SPATIAL_POSITION);
    }

    public void setDisplayEnvironmentSpatialPosition(double[] values)
    {
        dcmobj.putDoubles(Tag.DISPLAY_ENVIRONMENT_SPATIAL_POSITION, VR.FD, values);
    }

    public String getImageBoxLayoutType()
    {
        return dcmobj.getString(Tag.IMAGE_BOX_LAYOUT_TYPE);
    }

    public void setImageBoxLayoutType(String type)
    {
        dcmobj.putString(Tag.IMAGE_BOX_LAYOUT_TYPE, VR.CS, type);
    }

    public int getImageBoxTileHorizontalDimension()
    {
        return dcmobj.getInt(Tag.IMAGE_BOX_TILE_HORIZONTAL_DIMENSION);
    }

    public void setImageBoxTileHorizontalDimension(int value)
    {
        dcmobj.putInt(Tag.IMAGE_BOX_TILE_HORIZONTAL_DIMENSION, VR.US, value);
    }

    public int getImageBoxTileVerticalDimension()
    {
        return dcmobj.getInt(Tag.IMAGE_BOX_TILE_VERTICAL_DIMENSION);
    }

    public void setImageBoxTileVerticalDimension(int value)
    {
        dcmobj.putInt(Tag.IMAGE_BOX_TILE_VERTICAL_DIMENSION, VR.US, value);
    }

    public String getImageBoxScrollDirection()
    {
        return dcmobj.getString(Tag.IMAGE_BOX_SCROLL_DIRECTION);
    }

    public void setImageBoxScrollDirection(String value)
    {
        dcmobj.putString(Tag.IMAGE_BOX_SCROLL_DIRECTION, VR.CS, value);
    }

    public String getImageBoxSmallScrollType()
    {
        return dcmobj.getString(Tag.IMAGE_BOX_SMALL_SCROLL_TYPE);
    }

    public void setImageBoxSmallScrollType(String value)
    {
        dcmobj.putString(Tag.IMAGE_BOX_SMALL_SCROLL_TYPE, VR.CS, value);
    }

    public int getImageBoxSmallScrollAmount()
    {
        return dcmobj.getInt(Tag.IMAGE_BOX_SMALL_SCROLL_AMOUNT);
    }

    public void setImageBoxSmallScrollAmount(int value)
    {
        dcmobj.putInt(Tag.IMAGE_BOX_SMALL_SCROLL_AMOUNT, VR.US, value);
    }

    public String getImageBoxLargeScrollType()
    {
        return dcmobj.getString(Tag.IMAGE_BOX_LARGE_SCROLL_TYPE);
    }

    public void setImageBoxLargeScrollType(String value)
    {
        dcmobj.putString(Tag.IMAGE_BOX_LARGE_SCROLL_TYPE, VR.CS, value);
    }

    public int getImageBoxOverlapPriority()
    {
        return dcmobj.getInt(Tag.IMAGE_BOX_OVERLAP_PRIORITY);
    }

    public void setImageBoxOverlapPriority(int value)
    {
        dcmobj.putInt(Tag.IMAGE_BOX_OVERLAP_PRIORITY, VR.US, value);
    }

    public int getPreferredPlaybackSequencing()
    {
        return dcmobj.getInt(Tag.PREFERRED_PLAYBACK_SEQUENCING);
    }

    public void setPreferredPlaybackSequencing(int value)
    {
        dcmobj.putInt(Tag.PREFERRED_PLAYBACK_SEQUENCING, VR.US, value);
    }

    public int getRecommendedDisplayFrameRate()
    {
        return dcmobj.getInt(Tag.RECOMMENDED_DISPLAY_FRAME_RATE);
    }

    public void setRecommendedDisplayFrameRate(int value)
    {
        dcmobj.putInt(Tag.RECOMMENDED_DISPLAY_FRAME_RATE, VR.IS, value);
    }

    public double getCineRelativetoRealTime()
    {
        return dcmobj.getDouble(Tag.CINE_RELATIVE_TO_REAL_TIME);
    }

    public void setCineRelativetoRealTime(double value)
    {
        dcmobj.putDouble(Tag.CINE_RELATIVE_TO_REAL_TIME, VR.FD, value);
    }

}
