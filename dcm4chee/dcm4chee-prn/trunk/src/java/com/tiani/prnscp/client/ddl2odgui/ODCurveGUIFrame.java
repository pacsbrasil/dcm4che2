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

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.tiani.prnscp.print.CalibrationException;

public class ODCurveGUIFrame extends JFrame
{
    private final int DEF_WIDTH = 800, DEF_HEIGHT = 600;
    
    private ODCurveGUIPanel curvePanel;
    private ButtonLegendPanel legendPanel;
    private File lastFile = null; //for JFileChooser to remember last dir
    private JFileChooser chooser = new JFileChooser();
    
    private ODCurveGUIFrame()
    {
        Container contentPane = getContentPane();
        //set layout
        contentPane.setLayout(new BorderLayout());
        //curve panel
        curvePanel = new ODCurveGUIPanel(this);
        legendPanel = new ButtonLegendPanel(curvePanel);
        legendPanel.setMinimumSize(new Dimension(DEF_WIDTH / 4, DEF_HEIGHT));
        JLabel lbl = new JLabel("Loaded Curves");
        //legendPanel.add(lbl);
        curvePanel.setLegend(legendPanel);
        contentPane.add(curvePanel, BorderLayout.CENTER);
        contentPane.add(legendPanel, BorderLayout.SOUTH);
        //setup menus
        JMenuBar mnubar = new JMenuBar();
        JMenu mnuCurve = new JMenu("Curve");
        //create "curve -> load" curve menu
        Action actLoadCurve = new AbstractAction()
            {
                public void actionPerformed(ActionEvent e)
                {
                    if (promptUserForFile() != null) {
                        try {
                            curvePanel.loadScannedImageCurve(lastFile = chooser.getSelectedFile());
                        }
                        catch (CalibrationException ce) {
                            showMsgDialog("There is a problem analyzing the selected image ("
                                          + lastFile + "):\n"
                                          + ce.getMessage(),
                                          "Calibration Error");
                        }
                        catch (IOException ioe) {
                            showMsgDialog("There is a problem reading the selected image ("
                                          + lastFile + "):\n"
                                          + ioe.getMessage(),
                                          "File Error");
                        }
                        ODCurveGUIFrame.this.validate();
                        ODCurveGUIFrame.this.repaint();
                    }
                }
            };
        actLoadCurve.putValue(Action.NAME, "Load...");
        JMenuItem mnuLoadCurve = new JMenuItem(actLoadCurve);
        mnuCurve.add(mnuLoadCurve);
        //create "curve -> reset" curve menu
        Action actReset = new AbstractAction()
            {
                public void actionPerformed(ActionEvent e)
                {
                    curvePanel.reset();
                    validate();
                }
            };
        actReset.putValue(Action.NAME, "Reset");
        JMenuItem mnuReset = new JMenuItem(actReset);
        mnuCurve.add(mnuReset);
        //create "curve -> exit" curve menu
        Action actExit = new AbstractAction()
            {
                public void actionPerformed(ActionEvent e)
                {
                    System.exit(0);
                }
            };
        actExit.putValue(Action.NAME, "Exit");
        JMenuItem mnuExit = new JMenuItem(actExit);
        mnuCurve.add(mnuExit);
        //add curve
        mnubar.add(mnuCurve);
        //set menubar
        setJMenuBar(mnubar);
        //set size
        setSize(new Dimension(DEF_WIDTH, DEF_HEIGHT));
        validate();
    }
    
    private ODCurveGUIFrame(String title)
    {
        this();
        setTitle(title);
    }
    
    File promptUserForFile()
    {
        chooser.setCurrentDirectory(lastFile);
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION)
            return (lastFile = chooser.getSelectedFile());
        else
            return null;
    }
    
    void showMsgDialog(String msg, String title)
    {
        JOptionPane.showMessageDialog(this, msg, title, JOptionPane.ERROR_MESSAGE);
    }
    
    public static void main(String[] args)
    {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.WARN);
        ODCurveGUIFrame fr = new ODCurveGUIFrame("OD Curve Viewer");
        fr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        fr.show();
    }
}
