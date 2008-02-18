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
 * TIANI Medgraph AG.
 * Portions created by the Initial Developer are Copyright (C) 2002-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunter.zeilinger@tiani.com>
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

package com.tiani.prnscp.client.ddl2odgui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public class ODCurve
{
    static interface ODCurveOperation
    {
        float operate(float v1, float v2);
    }
    
    private float[] ods;
    private float max;
    private Color color;
    private boolean selected = false;
    
    ODCurve(float[] ods, Color color)
    {
        if (ods == null)
            throw new NullPointerException("Curve data can not be null");
        //set curve data
        this.ods = ods;
        max = ods[0];
        for (int i=1; i<ods.length; i++)
            if (ods[i] > max)
                max = ods[i];
        //color
        if (color == null)
            this.color = Color.BLACK;
        else
            this.color = color;
    }
    
    static ODCurve newCurveFromComparison(ODCurve curve1, ODCurve curve2,
                                   ODCurveOperation cmp, Color color)
    {
        final int maxind = Math.min(curve1.ods.length, curve2.ods.length) - 1;
        float[] result = new float[maxind + 1];
        
        for (int i = 0; i < maxind; i++) {
            result[i] = cmp.operate(curve1.ods[i], curve2.ods[i]);
        }
        
        return new ODCurve(result, color);
    }
    
    public float getMax()
    {
        return max;
    }
    
    public float[] getData()
    {
        return ods;
    }
    
    public void setSelected(boolean selected)
    {
        this.selected = selected;
    }
    
    public boolean isSelected()
    {
        return selected;
    }
    
    public void draw(Graphics2D g, Rectangle rect, float maxOD)
    {
        final int x0 = (int)rect.getX();
        final int y0 = (int)rect.getY();
        final int width = (int)rect.getWidth();
        final int height = (int)rect.getHeight();
        final float fx = (float)(width - 1)/(ods.length - 1);
        final float fy = (float)(height - 1)/maxOD;
        int lastx = x0 + (int)(width - 1 + 0.5f);
        int lasty = y0 + (int)(height - 1 - (ods[0] * fy + 0.5f));
        int x, y;
        
        g.setColor(color);
        g.setStroke(new BasicStroke(2));
        for (int i=0; i<ods.length; i++) {
            x = x0 + (int)((ods.length - 1 - i) * fx + 0.5f);
            y = y0 + (int)(height - 1 - (ods[i] * fy + 0.5f));
            g.drawLine(lastx, lasty, x, y);
            lastx = x;
            lasty = y;
        }
        g.setStroke(new BasicStroke(1));
        g.setColor(Color.BLACK);
        if (selected) {
            for (int i=0; i<ods.length; i++) {
                x = x0 + (int)((ods.length - 1 - i) * fx + 0.5f);
                y = y0 + (int)(height - 1 - (ods[i] * fy + 0.5f));
                g.drawRect(x, y, 1, 1);
            }
        }
    }
}
