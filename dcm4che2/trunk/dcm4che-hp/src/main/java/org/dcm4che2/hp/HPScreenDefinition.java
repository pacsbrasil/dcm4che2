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
public class HPScreenDefinition
{
    private final DicomObject dcmobj;

    public HPScreenDefinition(DicomObject item)
    {
        this.dcmobj = item;
    }

    public HPScreenDefinition()
    {
        this.dcmobj = new BasicDicomObject();
    }

    public DicomObject getDicomObject()
    {
        return dcmobj;
    }

    public int getNumberOfVerticalPixels()
    {
        return dcmobj.getInt(Tag.NUMBER_OF_VERTICAL_PIXELS);
    }

    public void setNumberOfVerticalPixels(int value)
    {
        dcmobj.putInt(Tag.NUMBER_OF_VERTICAL_PIXELS, VR.US, value);
    }

    public int getNumberOfHorizontalPixels()
    {
        return dcmobj.getInt(Tag.NUMBER_OF_HORIZONTAL_PIXELS);
    }

    public void setNumberofHorizontalPixels(int value)
    {
        dcmobj.putInt(Tag.NUMBER_OF_HORIZONTAL_PIXELS, VR.US, value);
    }

    public double[] getDisplayEnvironmentSpatialPosition()
    {
        return dcmobj.getDoubles(Tag.DISPLAY_ENVIRONMENT_SPATIAL_POSITION);
    }

    public void setDisplayEnvironmentSpatialPosition(double[] values)
    {
        dcmobj.putDoubles(Tag.DISPLAY_ENVIRONMENT_SPATIAL_POSITION, VR.FD, values);
    }

    public int getScreenMinimumColorBitDepth()
    {
        return dcmobj.getInt(Tag.SCREEN_MINIMUM_COLOR_BIT_DEPTH);
    }

    public void setScreenMinimumColorBitDepth(int value)
    {
        dcmobj.putInt(Tag.SCREEN_MINIMUM_COLOR_BIT_DEPTH, VR.US, value);
    }

    public int getScreenMinimumGrayscaleBitDepth()
    {
        return dcmobj.getInt(Tag.SCREEN_MINIMUM_GRAYSCALE_BIT_DEPTH);
    }

    public void setScreenMinimumGrayscaleBitDepth(int value)
    {
        dcmobj.putInt(Tag.SCREEN_MINIMUM_GRAYSCALE_BIT_DEPTH, VR.US, value);
    }

    public int getApplicationMaximumRepaintTime()
    {
        return dcmobj.getInt(Tag.APPLICATION_MAXIMUM_REPAINT_TIME);
    }

    public void setApplicationMaximumRepaintTime(int value)
    {
        dcmobj.putInt(Tag.APPLICATION_MAXIMUM_REPAINT_TIME, VR.US, value);
    }

}
