package com.tiani.prnscp.client.plutgui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;

import org.apache.log4j.*;

public class PLutGUIFrame extends JFrame
{
    private final int DEF_WIDTH = 800, DEF_HEIGHT = 600;
    PLutGUIPanel guiPanel;
    File lastFile = null; //for JFileChooser to remember last dir
    JFileChooser chooser = new JFileChooser();
    
    PLutGUIFrame()
    {
        Container panel = this.getContentPane();
        guiPanel = new PLutGUIPanel();
        panel.add(guiPanel);
        //menu
        JMenuBar mnubar = new JMenuBar();
        JMenu mnuFile = new JMenu("File");
        JMenu mnuProccess = new JMenu("Process");
        Action actOpenImg = new AbstractAction()
            {
                public void actionPerformed(ActionEvent e)
                {
                    chooser.setCurrentDirectory(lastFile);
                    int returnVal = chooser.showOpenDialog(PLutGUIFrame.this);
                    if(returnVal == JFileChooser.APPROVE_OPTION) {
                        guiPanel.setImage(lastFile = chooser.getSelectedFile());
                    }
                }
            };
        actOpenImg.putValue(Action.NAME,"Open DICOM Image");
        Action actExit = new AbstractAction()
            {
                public void actionPerformed(ActionEvent e)
                {
                    System.exit(0);
                }
            };
        actExit.putValue(Action.NAME,"Exit");
        Action actExportDcmPres = new AbstractAction()
            {
                public void actionPerformed(ActionEvent e)
                {
                    chooser.setCurrentDirectory(lastFile);
                    int returnVal = chooser.showOpenDialog(PLutGUIFrame.this);
                    if(returnVal == JFileChooser.APPROVE_OPTION) {
                        try {
                            guiPanel.getPLutPanel().exportPLutDicom(lastFile = chooser.getSelectedFile());
                        }
                        catch (IOException ioe) {
                            JOptionPane.showMessageDialog(PLutGUIFrame.this,"Problem with export");
                        }
                    }
                }
            };
        actExportDcmPres.putValue(Action.NAME,"Export DICOM Presentation...");
        Action actImportDcmPres = new AbstractAction()
            {
                public void actionPerformed(ActionEvent e)
                {
                    chooser.setCurrentDirectory(lastFile);
                    int returnVal = chooser.showOpenDialog(PLutGUIFrame.this);
                    if(returnVal == JFileChooser.APPROVE_OPTION) {
                        try {
                            guiPanel.getPLutPanel().importPLutDicom(lastFile = chooser.getSelectedFile());
                        }
                        catch (IOException ioe) {
                            JOptionPane.showMessageDialog(PLutGUIFrame.this,"Problem with import");
                        }
                    }
                }
            };
        actImportDcmPres.putValue(Action.NAME,"Import DICOM Presentation...");
        Action actDisplayImageInfo = new AbstractAction()
            {
                public void actionPerformed(ActionEvent e)
                {
                    guiPanel.displayImageInfo();
                }
            };
        actDisplayImageInfo.putValue(Action.NAME,"Image Info...");
        JMenuItem mnuOpenImg = new JMenuItem(actOpenImg);
        mnuFile.add(mnuOpenImg);
        JMenuItem mnuImportDcmPres = new JMenuItem(actImportDcmPres);
        mnuFile.add(mnuImportDcmPres);
        JMenuItem mnuExportDcmPres = new JMenuItem(actExportDcmPres);
        mnuFile.add(mnuExportDcmPres);
        JMenuItem mnuDisplayImageInfo = new JMenuItem(actDisplayImageInfo);
        mnuFile.add(mnuDisplayImageInfo);
        JMenuItem mnuExit = new JMenuItem(actExit);
        mnuFile.add(mnuExit);
        Action actHistoEq = new AbstractAction()
            {
                public void actionPerformed(ActionEvent e)
                {
                    guiPanel.equalize();
                }
            };
        actHistoEq.putValue(Action.NAME,"Histo-Eq");
        JMenuItem mnuHistoEq = new JMenuItem(actHistoEq);
        mnuProccess.add(mnuHistoEq);
        mnubar.add(mnuFile);
        mnubar.add(mnuProccess);
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
