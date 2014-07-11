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
package in.raster.mayam.models;

import java.io.Serializable;

/**
 *
 * @author BabuHussain
 * @version 0.5
 *
 */
public class AnnotationObj implements Serializable {

    //original X,Y coordinates
    private int x1;
    private int y1;
    private int x2;
    private int y2;
    // Component center based X,Y coordinates
    private int centerBasedX1;
    private int centerBasedY1;
    private int centerBasedX2;
    private int centerBasedY2;
    //Mid point of the component    
    /*
     * All the calculations are based on the following SHAPEORIGIN value.So it
     * should not be changed. Zoom level can be adjusted with out changing the
     * component size and SHAPEORIGIN. So there is no need of changing this
     * SHAPEORIGIN value and component size.
     */
    private int shapeOrigin = 256;
    // Calculated Measurement value for the current shape
    private String length;
    private String area;
    private String mean;
    private String stdDev;
    // Type of the shape
    private String type = "";
    //Text assisgned.
    private String text = "";

    //Construct the Annotation object
    public AnnotationObj() {
    }

    //Getter and Setter Methods
    public int getX1() {
        return x1;
    }

    public void setX1(int x1) {
        this.x1 = x1;
    }

    public int getX2() {
        return x2;
    }

    public void setX2(int x2) {
        this.x2 = x2;
    }

    public int getY1() {
        return y1;
    }

    public void setY1(int y1) {
        this.y1 = y1;
    }

    public int getY2() {
        return y2;
    }

    public void setY2(int y2) {
        this.y2 = y2;
    }

    public int getCenterBasedX1() {
        return centerBasedX1;
    }

    public void setCenterBasedX1(int centerBasedX1) {
        this.centerBasedX1 = centerBasedX1;
    }

    public int getCenterBasedX2() {
        return centerBasedX2;
    }

    public void setCenterBasedX2(int centerBasedX2) {
        this.centerBasedX2 = centerBasedX2;
    }

    public int getCenterBasedY1() {
        return centerBasedY1;
    }

    public void setCenterBasedY1(int centerBasedY1) {
        this.centerBasedY1 = centerBasedY1;
    }

    public int getCenterBasedY2() {
        return centerBasedY2;
    }

    public void setCenterBasedY2(int centerBasedY2) {
        this.centerBasedY2 = centerBasedY2;
    }

    public int getShapeOrigin() {
        return shapeOrigin;
    }

    public void setShapeOrigin(int shapeOrigin) {
        this.shapeOrigin = shapeOrigin;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }

    public String getMean() {
        return mean;
    }

    public void setMean(String mean) {
        this.mean = mean;
    }

    public String getStdDev() {
        return stdDev;
    }

    public void setStdDev(String stdDev) {
        this.stdDev = stdDev;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    // To set the location of the shape
    public void setLocation(int x1, int y1, int x2, int y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.centerBasedX1 = centerBasedX(this.x1);
        this.centerBasedX2 = centerBasedX(this.x2);
        this.centerBasedY1 = centerBasedY(this.y1);
        this.centerBasedY2 = centerBasedY(this.y2);
    }
    //This subroutine used to set the component center based coordinates

    public void setCenterBasedLocation(int x1, int y1, int x2, int y2) {
        this.centerBasedX1 = x1;
        this.centerBasedX2 = x2;
        this.centerBasedY1 = y1;
        this.centerBasedY2 = y2;
        this.x1 = originalX(this.centerBasedX1);
        this.y1 = originalY(this.centerBasedY1);
        this.x2 = originalX(this.centerBasedX2);
        this.y2 = originalY(this.centerBasedY2);
    }
    //To find the center based x value for the given original X

    private int centerBasedX(int x) {
        return (x - shapeOrigin);
    }
    //To find the center based y value for the given original Y

    private int centerBasedY(int y) {
        return (y - shapeOrigin);
    }
    //To find the original x value for the given center based X

    private int originalX(int x) {
        return (x + shapeOrigin);
    }
    //To find the original x value for the given center based Y

    private int originalY(int y) {
        return (y + shapeOrigin);
    }
}