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
