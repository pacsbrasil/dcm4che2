package com.tiani.prnscp.client.plutgui;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import org.apache.log4j.*;

public class PLutGUIFrame extends JFrame
{
    private final int DEF_WIDTH = 800, DEF_HEIGHT = 600;
    PLutGUIPanel guiPanel;
    
    PLutGUIFrame()
    {
        Container panel = this.getContentPane();
        guiPanel = new PLutGUIPanel();
        panel.add(guiPanel);
        //menu
        JMenuBar mnubar = new JMenuBar();
        JMenu mnuFile = new JMenu("File");
        Action actOpenImg = new AbstractAction()
            {
                public void actionPerformed(ActionEvent e)
                {
                    JFileChooser chooser = new JFileChooser();
                    int returnVal = chooser.showOpenDialog(PLutGUIFrame.this);
                    if(returnVal == JFileChooser.APPROVE_OPTION) {
                        guiPanel.setImage(chooser.getSelectedFile());
                    }
                }
            };
        actOpenImg.putValue(Action.NAME,"Open");
        Action actExit = new AbstractAction()
            {
                public void actionPerformed(ActionEvent e)
                {
                    System.exit(0);
                }
            };
        actExit.putValue(Action.NAME,"Exit");
        JMenuItem mnuOpenImg = new JMenuItem(actOpenImg);
        mnuFile.add(mnuOpenImg);
        JMenuItem mnuExit = new JMenuItem(actExit);
        mnuFile.add(mnuExit);
        mnubar.add(mnuFile);
        setJMenuBar(mnubar);
        setSize(new Dimension(DEF_WIDTH, DEF_HEIGHT));
    }
    PLutGUIFrame(String title)
    {
        this();
        setTitle(title);
    }
    
    public static void main(String[] args)
    {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.WARN);
        PLutGUIFrame fr = new PLutGUIFrame("P-LUT Viewer");
        fr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        fr.show();
    }
}
