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
 *
 * The Initial Developer of the Original Code is
 * Raster Images
 * Portions created by the Initial Developer are Copyright (C) 2014
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Babu Hussain A
 * Devishree V
 * Meer Asgar Hussain B
 * Prakash J
 * Suresh V
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
package in.raster.mayam.delegates;

import in.raster.mayam.context.ApplicationContext;

/**
 *
 * @author BabuHussain
 * @version 0.6
 *
 */
public class ImageOrientation {

    public ImageOrientation() {
    }

    public static String getOrientation(double x, double y, double z) {
        String orientation = "";
        String orientationX = (x < 0) ? ApplicationContext.currentBundle.getString("ImageView.imageOrientation.right") : ApplicationContext.currentBundle.getString("ImageView.imageOrientation.left");
        String orientationY = (y < 0) ? ApplicationContext.currentBundle.getString("ImageView.imageOrientation.anterior") : ApplicationContext.currentBundle.getString("ImageView.imageOrientation.posterior");
        String orientationZ = (z < 0) ? ApplicationContext.currentBundle.getString("ImageView.imageOrientation.foot") : ApplicationContext.currentBundle.getString("ImageView.imageOrientation.head");
        double absX = Math.abs(x);
        double absY = Math.abs(y);
        double absZ = Math.abs(z);
        for (int i = 0; i < 3; ++i) {
            if ((absX > 0.0001) && (absX > absY) && (absX > absZ)) {
                orientation += orientationX;
                absX = 0;
            } else if ((absY > 0.0001) && (absY > absX) && (absY > absZ)) {
                orientation += orientationY;
                absY = 0;
            } else if (absZ > 0.0001 && absZ > absX && absZ > absY) {
                orientation += orientationZ;
                absZ = 0;
            } else {
                break;
            }
        }
        return orientation;
    }

    public static void getOrientation(String imageOrientation) {
        String imageOrientationArray[], columnRowArray[];
        imageOrientationArray = imageOrientation.split("\\\\");
        float _imgRowCosx = Float.parseFloat(imageOrientationArray[0]);
        float _imgRowCosy = Float.parseFloat(imageOrientationArray[1]);
        float _imgRowCosz = Float.parseFloat(imageOrientationArray[2]);
        float _imgColCosx = Float.parseFloat(imageOrientationArray[3]);
        float _imgColCosy = Float.parseFloat(imageOrientationArray[4]);
        float _imgColCosz = Float.parseFloat(imageOrientationArray[5]);
        columnRowArray = new String[2];
        columnRowArray[0] = ImageOrientation.getOrientation(_imgRowCosx, _imgRowCosy, _imgRowCosz);
        columnRowArray[1] = ImageOrientation.getOrientation(_imgColCosx, _imgColCosy, _imgColCosz);
    }

    public static String getOppositeOrientation(String orientation) {
        String oppositePrcl = "";
        char[] temp = orientation.toCharArray();
        for (char c : temp) {
            oppositePrcl += getOpposite(c);
        }
        return oppositePrcl;
    }

    public static char getOpposite(char c) {
        char opposite = ' ';
        switch (c) {
            case 'L':
                return 'R';
            case 'R':
                return 'L';
            case 'P':
                return 'A';
            case 'A':
                return 'P';
            case 'H':
                return 'F';
            case 'F':
                return 'H';
        }
        return opposite;
    }
}