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

package com.tiani.prnscp.client.plutgui;

import java.awt.*;
import javax.swing.*;

import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;

import java.io.*;

public class PLutGUIPanel extends JPanel
{
    ImagePanel imgPanel;
    PLutPanel plutPanel;
    
    PLutGUIPanel()
    {
        super();
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        //set layout
        setLayout(gridbag);
        //
        imgPanel = new ImagePanel(null);
        plutPanel = new PLutPanel(imgPanel);
        //add image panel
        //plutPanel.setPreferredSize(new Dimension(300,600));
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 2;
        c.weighty = 1;
        gridbag.setConstraints(imgPanel,c);
        add(imgPanel);
        //add plut panel
        //c.fill = GridBagConstraints.NONE;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.weightx = 1;
        c.weighty = 1;
        //c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(plutPanel,c);
        add(plutPanel);
    }
    
    public ImagePanel getImagePanel() { return imgPanel; }
    
    public PLutPanel getPLutPanel() { return plutPanel; }
    
    public void equalize()
    {
        plutPanel.equalize();
    }
    
    public void setImage(File newImg)
    {
        try {
            imgPanel.setImage(newImg);
        }
        catch (UnsupportedOperationException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                                          "Image could not be read or loaded:\n"
                                          + "- May not be a DICOM image file, or\n"
                                          + "- The color model may not be applicable"
                                          + "to a P-LUT transformation.");
        }
        imgPanel.repaint();
        plutPanel.buildHisto();
        plutPanel.repaint();
    }
    
    public void displayImageInfo()
    {
        Dataset ds = imgPanel.getDS();
        if (ds == null) {
            JOptionPane.showMessageDialog(this, "No image has been loaded.");
            return;
        }
        int width = ds.getInt(Tags.Columns, 0);
        int height = ds.getInt(Tags.Rows, 0);
        int bitsStored = ds.getInt(Tags.BitsStored, 0);
        boolean signed = (ds.getInt(Tags.PixelRepresentation, 0) == 1);
        String pmi = ds.getString(Tags.PhotometricInterpretation);
        JOptionPane.showMessageDialog(this, "Width: " + width + "\n"
                                      + "Height: " + height + "\n"
                                      + "Bits: " + bitsStored + " ("
                                      + ((signed)?"signed":"unsigned") + ")\n"
                                      + "Color model: " + pmi + "\n"
                                      + "");
    }
    
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
    }
}
