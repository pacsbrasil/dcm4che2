package com.tiani.prnscp.client.plutgui;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class PLutGUIFrame extends JFrame
{
    private final int DEF_WIDTH = 800, DEF_HEIGHT = 600;
    PLutGUIPanel guiPanel;
    File lastFile = null; //for JFileChooser to remember last dir
    JFileChooser chooser = new JFileChooser();
    Action actOpenImg,actExit, actExportDcmPres, actImportDcmPres,
           actDisplayImageInfo, actHistoEq, actExportTxtPres,
           actImportTxtPres, actHistoScale;
    
    PLutGUIFrame()
    {
        Container panel = this.getContentPane();
        guiPanel = new PLutGUIPanel();
        panel.add(guiPanel);
        //Main Menus
        JMenuBar mnubar = new JMenuBar();
        JMenu mnuFile = new JMenu("File");
        JMenu mnuView = new JMenu("View");
        JMenu mnuProccess = new JMenu("Process");
        mnubar.add(mnuFile);
        mnubar.add(mnuView);
        mnubar.add(mnuProccess);
        //File Menu
        actOpenImg = new AbstractAction()
            {
                public void actionPerformed(ActionEvent e)
                {
                    chooser.setCurrentDirectory(lastFile);
                    int returnVal = chooser.showOpenDialog(PLutGUIFrame.this);
                    if(returnVal == JFileChooser.APPROVE_OPTION) {
                        guiPanel.setImage(lastFile = chooser.getSelectedFile());
                        actHistoEq.setEnabled(!guiPanel.getImagePanel().isApplyingPLutToRGB());
                    }
                }
            };
        actOpenImg.putValue(Action.NAME,"Open DICOM Image");
        actExit = new AbstractAction()
            {
                public void actionPerformed(ActionEvent e)
                {
                    System.exit(0);
                }
            };
        actExit.putValue(Action.NAME,"Exit");
        actExportDcmPres = new AbstractAction()
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
        actImportDcmPres = new AbstractAction()
            {
                public void actionPerformed(ActionEvent e)
                {
                    chooser.setCurrentDirectory(lastFile);
                    int returnVal = chooser.showSaveDialog(PLutGUIFrame.this);
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
        actExportTxtPres = new AbstractAction()
            {
                public void actionPerformed(ActionEvent e)
                {
                    chooser.setCurrentDirectory(lastFile);
                    int returnVal = chooser.showOpenDialog(PLutGUIFrame.this);
                    if(returnVal == JFileChooser.APPROVE_OPTION) {
                        try {
                            guiPanel.getPLutPanel().exportPLutText(lastFile = chooser.getSelectedFile());
                        }
                        catch (IOException ioe) {
                            JOptionPane.showMessageDialog(PLutGUIFrame.this,"Problem with export");
                        }
                    }
                }
            };
        actExportTxtPres.putValue(Action.NAME,"Export Text Presentation...");
        actImportTxtPres = new AbstractAction()
            {
                public void actionPerformed(ActionEvent e)
                {
                    chooser.setCurrentDirectory(lastFile);
                    int returnVal = chooser.showSaveDialog(PLutGUIFrame.this);
                    if(returnVal == JFileChooser.APPROVE_OPTION) {
                        try {
                            guiPanel.getPLutPanel().importPLutText(lastFile = chooser.getSelectedFile());
                        }
                        catch (IOException ioe) {
                            JOptionPane.showMessageDialog(PLutGUIFrame.this,"Problem with import");
                        }
                    }
                }
            };
        actImportTxtPres.putValue(Action.NAME,"Import Text Presentation...");
        actDisplayImageInfo = new AbstractAction()
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
        JMenuItem mnuImportTxtPres = new JMenuItem(actImportTxtPres);
        mnuFile.add(mnuImportTxtPres);
        JMenuItem mnuExportTxtPres = new JMenuItem(actExportTxtPres);
        mnuFile.add(mnuExportTxtPres);
        JMenuItem mnuDisplayImageInfo = new JMenuItem(actDisplayImageInfo);
        mnuFile.add(mnuDisplayImageInfo);
        JMenuItem mnuExit = new JMenuItem(actExit);
        mnuFile.add(mnuExit);
        //View Menu
        actHistoScale = new AbstractAction()
            {
                public void actionPerformed(ActionEvent e)
                {
                    PLutPanel panel = guiPanel.getPLutPanel();
                    panel.setLogHisto(!panel.isLogHisto());
                    panel.repaint();
                }
            };
        JCheckBoxMenuItem mnuHistoScale = new JCheckBoxMenuItem(actHistoScale);
        mnuHistoScale.setSelected(true);
        mnuView.add(mnuHistoScale);
        actHistoScale.putValue(Action.NAME,"Log Histogram Scale");
        //Process Menu
        actHistoEq = new AbstractAction()
            {
                public void actionPerformed(ActionEvent e)
                {
                    guiPanel.equalize();
                }
            };
        actHistoEq.putValue(Action.NAME,"Equalize Histogram");
        JMenuItem mnuHistoEq = new JMenuItem(actHistoEq);
        mnuProccess.add(mnuHistoEq);
        //
        setJMenuBar(mnubar);
        setSize(new Dimension(DEF_WIDTH, DEF_HEIGHT));
    }

    PLutGUIFrame(String title)
    {
        this();
        setTitle(title);
    }

    /* for abnormal exit */
    private static void exit(String msg)
    {
        System.out.println(msg);
        System.out.println(USAGE);
        System.exit(1);
    }

    private final static String USAGE =
            "Usage: java -jar view-plut.jar [OPTIONS]\n\n" +
            "Used to view the effect of presentation LUTs on DICOM images.\n" +
            "Options:\n" +
            " -t --threshold   Specifies the maximum threshold of differences\n" +
            "                  to treat the components of RGB values as gray values.\n" +
            "                  Specified as an integer from 0..255.\n" +
            " -h --help        show this help and exit\n";

    public static void main(String[] args)
    {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.WARN);
        
        LongOpt[] longopts = {
                new LongOpt("gray-threshold", LongOpt.OPTIONAL_ARGUMENT, null, 't'),
                new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h')
                };

        Getopt g = new Getopt("view-plut", args, "t:h", longopts, true);
        try {
            PLutGUIFrame fr = new PLutGUIFrame("P-LUT Viewer");
            int c;
            while ((c = g.getopt()) != -1) {
                switch (c) {
                    case 't':
                        fr.guiPanel.getImagePanel().setRgbToGrayThreshold(Integer.parseInt(g.getOptarg()));
                        break;
                    case 'h':
                    case '?':
                        exit("");
                        break;
                }
            }
            int optind = g.getOptind();
            int argc = args.length - optind;
            if (argc != 0) {
                exit("view-plut: wrong number of arguments\n");
            }
            fr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            fr.show();
        }
        catch (IllegalArgumentException e) {
            exit("view-plut: illegal argument - " + e.getMessage() + "\n");
        }
        /*catch (IOException e) {
            System.err.println("view-plut: i/o error - " + e.getMessage() + "\n");
            System.exit(1);
        }*/
    }
}
