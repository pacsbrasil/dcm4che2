package com.tiani.prnscp.client.ddl2odgui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;

import org.apache.log4j.*;

import com.tiani.prnscp.print.CalibrationException;

public class ODCurveGUIFrame extends JFrame
{
    private final int DEF_WIDTH = 800, DEF_HEIGHT = 600;
    
    private ODCurveGUIPanel curvePanel;
    private File lastFile = null; //for JFileChooser to remember last dir
    private JFileChooser chooser = new JFileChooser();
    
    private ODCurveGUIFrame()
    {
        Container contentPane = getContentPane();
        //menu
        JMenuBar mnubar = new JMenuBar();
        JMenu mnuCurve = new JMenu("Curve");
        Action actLoadCurve = new AbstractAction()
            {
                public void actionPerformed(ActionEvent e)
                {
                    chooser.setCurrentDirectory(lastFile);
                    int returnVal = chooser.showOpenDialog(ODCurveGUIFrame.this);
                    if(returnVal == JFileChooser.APPROVE_OPTION) {
                        try {
                            curvePanel.loadScannedImageCurve(lastFile = chooser.getSelectedFile());
                        }
                        catch (CalibrationException ce) {
                            showMsgDialog("There is a problem with analyzing the selected image ("
                                          + lastFile + "):\n"
                                          + ce.getMessage(),
                                          "Calibration Error");
                        }
                        catch (IOException ioe) {
                            showMsgDialog("There is a problem with reading the selected image ("
                                          + lastFile + "):\n"
                                          + ioe.getMessage(),
                                          "File Error");
                        }
                        curvePanel.repaint();
                    }
                }
            };
        actLoadCurve.putValue(Action.NAME,"Load...");
        JMenuItem mnuLoadCurve = new JMenuItem(actLoadCurve);
        mnuCurve.add(mnuLoadCurve);
        mnubar.add(mnuCurve);
        setJMenuBar(mnubar);
        //set layout
        contentPane.setLayout(new BorderLayout());
        //curve panel
        curvePanel = new ODCurveGUIPanel();
        contentPane.add(curvePanel, BorderLayout.CENTER);
        //set size
        setSize(new Dimension(DEF_WIDTH, DEF_HEIGHT));
    }
    
    private ODCurveGUIFrame(String title)
    {
        this();
        setTitle(title);
    }
    
    private void showMsgDialog(String msg, String title)
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
