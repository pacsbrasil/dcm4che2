package com.tiani.prnscp.client.plutgui;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;

import javax.management.InstanceNotFoundException;
import javax.management.ObjectName;
import javax.naming.InitialContext;
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

import org.jboss.jmx.adaptor.rmi.RMIAdaptor;

import com.tiani.prnscp.print.PLutBuilder;

public class PLutGUIFrame extends JFrame
{
    private final static String MBEAN_PRINTER_SERVICE_PREFIX = "dcm4chex:service=Printer,calledAET=";
    private final int DEF_WIDTH = 800, DEF_HEIGHT = 600;

    private String printingCalledAet = "TIANI_PRINT";
    PLutGUIPanel guiPanel;
    File lastFile = null; //for JFileChooser to remember last dir
    JFileChooser chooser = new JFileChooser();
    Action actOpenImg, actExit, actExportDcmPres, actImportDcmPres,
           actDisplayImageInfo, actHistoEq, actExportTxtPres,
           actImportTxtPres, actHistoScale, actPrint;

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
        actOpenImg.putValue(Action.NAME,"Open DICOM Image...");
        actPrint = new AbstractAction()
            {
                public void actionPerformed(ActionEvent e)
                {
                    if (!guiPanel.getPLutPanel().isShowingPLutParams()) {
                        JOptionPane.showMessageDialog(PLutGUIFrame.this,
                            "Printing is only possible using a manually generated P-LUT.");
                        return;
                    }
                    try {
                        print(lastFile);
                    }
                    catch (InstanceNotFoundException infe) {
                        JOptionPane.showMessageDialog(PLutGUIFrame.this,
                            "Problem with printing: the called AET may not be registered");
                    }
                    catch (Exception ex) {
                        JOptionPane.showMessageDialog(PLutGUIFrame.this,
                            "Problem with printing: " + ex);
                        //ex.printStackTrace();
                    }
                }
            };
        actPrint.putValue(Action.NAME,"Print");
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
                    int returnVal = chooser.showSaveDialog(PLutGUIFrame.this);
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
        actExportTxtPres = new AbstractAction()
            {
                public void actionPerformed(ActionEvent e)
                {
                    chooser.setCurrentDirectory(lastFile);
                    int returnVal = chooser.showSaveDialog(PLutGUIFrame.this);
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
                    int returnVal = chooser.showOpenDialog(PLutGUIFrame.this);
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
        mnuFile.addSeparator();
        JMenuItem mnuPrint = new JMenuItem(actPrint);
        mnuFile.add(mnuPrint);
        mnuFile.addSeparator();
        JMenuItem mnuDisplayImageInfo = new JMenuItem(actDisplayImageInfo);
        mnuFile.add(mnuDisplayImageInfo);
        mnuFile.addSeparator();
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

    void print(File file)
        throws Exception
    {
        PLutBuilder builder = guiPanel.getPLutPanel().getBuilder();
        String cfg = "shape=IDENTITY";
        cfg = "center=" + Double.toString(builder.getCenter())
              + ",gamma=" + Double.toString(builder.getGamma())
              + ",slope=" + Double.toString(builder.getSlope());
        boolean color = guiPanel.getImagePanel().isApplyingPLutToRGB();
        
        Hashtable env = new Hashtable();
        env.put("java.naming.factory.initial", "org.jnp.interfaces.NamingContextFactory");
        env.put("java.naming.factory.url.pkgs", "org.jboss.naming:org.jnp.interfaces");
        env.put("java.naming.provider.url", "localhost");
        InitialContext ic = new InitialContext(env);
        RMIAdaptor server = (RMIAdaptor) ic.lookup("jmx/rmi/RMIAdaptor");
        server.invoke(new ObjectName(MBEAN_PRINTER_SERVICE_PREFIX + printingCalledAet), "printImage",
                new Object[]{
                    file.getCanonicalPath(),
                    cfg,
                    new Boolean(color)
                },
                new String[]{
                    String.class.getName(),
                    String.class.getName(),
                    Boolean.class.getName(),
                });
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
            " -a --aet         The called AET of a print server for printing images\n" +
            " -h --help        show this help and exit\n";

    public static void main(String[] args)
    {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.WARN);
        
        LongOpt[] longopts = {
                new LongOpt("gray-threshold", LongOpt.OPTIONAL_ARGUMENT, null, 't'),
                new LongOpt("aet", LongOpt.OPTIONAL_ARGUMENT, null, 'a'),
                new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h')
            };

        Getopt g = new Getopt("view-plut", args, "t:a:h", longopts, true);
        try {
            PLutGUIFrame plutViewer = new PLutGUIFrame("P-LUT Viewer");
            int c;
            while ((c = g.getopt()) != -1) {
                switch (c) {
                    case 't':
                        plutViewer.guiPanel.getImagePanel().setRgbToGrayThreshold(Integer.parseInt(g.getOptarg()));
                        break;
                    case 'a':
                        plutViewer.setPrintingCalledAet(g.getOptarg());
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
            plutViewer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            plutViewer.show();
        }
        catch (IllegalArgumentException e) {
            exit("view-plut: illegal argument - " + e.getMessage() + "\n");
        }
    }

    public String getPrintingCalledAet() {
        return printingCalledAet;
    }

    public void setPrintingCalledAet(String string) {
        if (string == null || string.length() == 0 || string.length() > 16)
            throw new IllegalArgumentException(
                "AET titles must be between 1 to 16 characters");
        printingCalledAet = string;
    }
}
