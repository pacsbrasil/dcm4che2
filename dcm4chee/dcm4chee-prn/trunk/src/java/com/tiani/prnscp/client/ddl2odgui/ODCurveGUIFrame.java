package com.tiani.prnscp.client.ddl2odgui;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;

import org.apache.log4j.*;

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
                            curvePanel.loadCurve(lastFile = chooser.getSelectedFile());
                        }
                        catch (FileNotFoundException fnf) {
                        }
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
    
    public static void main(String[] args)
    {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.WARN);
        ODCurveGUIFrame fr = new ODCurveGUIFrame("OD Curve Viewer");
        fr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        fr.show();
    }
}
