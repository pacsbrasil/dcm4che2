package org.dcm4che.client.printscu;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dcm4che.client.AssociationRequestor;
import org.dcm4che.client.PrintSCU;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;
import org.dcm4che.net.DcmServiceException;
import org.dcm4che.util.UIDGenerator;

public class PrintSCUFrame extends JFrame
{
    public static final String DEFAULT_PROPERTIES_FILE = "PrintSCU.properties";

    private static final int DEF_WIDTH = 600, DEF_HEIGHT = 500;

    private final Logger log = Logger.getRootLogger();

    private AssociationRequestor assocRq = new AssociationRequestor();
    private PrintSCU printSCU;
    private List plutUidList;
    private int nextImageBoxIndex = 0;
    private boolean colorMode = false;
    private Action actConnect, actRelease, actCreateFilmSession, actDeleteFilmSession,
        actCreateFilmBox, actDeleteFilmBox, actCreateImageBox, actCreatePlut,
        actDeletePlut, actPrintFilmSession, actPrintFilmBox, actExit;
    private File lastFile = null; //for JFileChooser to remember last dir
    private JFileChooser chooser = new JFileChooser();
    private DcmObjectFactory dcmFactory = DcmObjectFactory.getInstance();
    private UIDGenerator uidGen = UIDGenerator.getInstance();
    private JPanel panel;
    private JPanel btnPanel;
    private PropertiesPanel propPanel;

    PrintSCUFrame()
    {
        Container contentPane = this.getContentPane();
        contentPane.add(panel = new JPanel());
        panel.setLayout(new BorderLayout());
        btnPanel = new JPanel();
        btnPanel.setLayout(new GridLayout(4, 3));
        panel.add(btnPanel, BorderLayout.NORTH);
        propPanel = new PropertiesPanel(log, DEFAULT_PROPERTIES_FILE);
        JScrollPane scrollingPanel = new JScrollPane(propPanel);
        panel.add(scrollingPanel, BorderLayout.CENTER);
        //Main Menus
        JMenuBar mnubar = new JMenuBar();
        setJMenuBar(mnubar);
        JMenu mnuFile = new JMenu("File");
        mnubar.add(mnuFile);
        // File menu
        actExit = new AbstractAction()
            {
                public void actionPerformed(ActionEvent e)
                {
                    System.exit(0);
                }
            };
        actExit.putValue(Action.NAME,"Exit");
        JMenuItem mnuExit = new JMenuItem(actExit);
        mnuFile.add(mnuExit);
        //set size
        setSize(new Dimension(DEF_WIDTH, DEF_HEIGHT));

        //Print SCP related actions
        
        //Connect
        actConnect = new AbstractAction()
        {
             public void actionPerformed(ActionEvent e)
             {
                 //connect
                 printSCU = new PrintSCU(assocRq);
                 plutUidList = new LinkedList();
                 assocRq.setCallingAET(propPanel.getProperty("CallingAET"));
                 assocRq.setCalledAET(propPanel.getProperty("CalledAET"));
                 assocRq.setHost(propPanel.getProperty("Host"));
                 assocRq.setPort(propPanel.getIntProperty("Port"));
                 try {
                     assocRq.connect();
                 }
                 catch (IOException e1) {
                     e1.printStackTrace();
                     return;
                 }
                 actCreateFilmSession.setEnabled(true);
                 actRelease.setEnabled(true);
             }
        };
        actConnect.putValue(Action.NAME, "Connect");
        //Release
        actRelease = new AbstractAction()
        {
            public void actionPerformed(ActionEvent e)
            {
                //release
                try {
                    assocRq.release();
                }
                catch (InterruptedException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                    return;
                }
                catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                    return;
                }
                printSCU = null;
                nextImageBoxIndex = 0;
                onDisconnect();
            }
        };
        actRelease.putValue(Action.NAME, "Release");
        //Create Session
        actCreateFilmSession = new AbstractAction()
        {
            public void actionPerformed(ActionEvent e)
            {
                Dataset attr = dcmFactory.newDataset();
                
                try {
                    printSCU.createFilmSession(attr, colorMode);
                }
                catch (InterruptedException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                    return;
                }
                catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                    return;
                }
                catch (DcmServiceException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                    return;
                }
                actCreateFilmBox.setEnabled(true);
                actCreatePlut.setEnabled(true);
                setEnabled(false);
                actDeleteFilmSession.setEnabled(true);
            }
        };
        actCreateFilmSession.putValue(Action.NAME, "Create Session");
        //Create FilmBox
        actCreateFilmBox = new AbstractAction()
        {
            public void actionPerformed(ActionEvent e)
            {
                Dataset attr = dcmFactory.newDataset();
                
                //printSCU.setAutoRefPLUT(true);
                attr.putST(Tags.ImageDisplayFormat, propPanel.getProperty("FilmBox.ImageDisplayFormat"));
                try {
                    printSCU.createFilmBox(attr);
                }
                catch (InterruptedException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                    return;
                }
                catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                    return;
                }
                catch (DcmServiceException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                    return;
                }
                actCreateImageBox.setEnabled(true);
                setEnabled(false);
                actDeleteFilmBox.setEnabled(true);
            }
        };
        actCreateFilmBox.putValue(Action.NAME, "Create FilmBox");
        //Create ImageBox
        actCreateImageBox = new AbstractAction()
        {
            public void actionPerformed(ActionEvent e)
            {
                File file;
                if (chooser.showOpenDialog(PrintSCUFrame.this)
                        == JFileChooser.APPROVE_OPTION) {
                    file = chooser.getSelectedFile();
                    Dataset attr = dcmFactory.newDataset();
                    try {
                        printSCU.setImageBox(nextImageBoxIndex++, file, attr);
                    }
                    catch (InterruptedException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                        return;
                    }
                    catch (IOException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                        return;
                    }
                    catch (DcmServiceException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                        return;
                    }
                    actPrintFilmSession.setEnabled(true);
                    actPrintFilmBox.setEnabled(true);
                }
            }
        };
        actCreateImageBox.putValue(Action.NAME, "Create ImageBox");
        //Create P-LUT
        actCreatePlut = new AbstractAction()
        {
            public void actionPerformed(ActionEvent e)
            {
                String shape = "IDENTITY";
                try {
                    plutUidList.add(printSCU.createPLUT(shape));
                }
                catch (InterruptedException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                    return;
                }
                catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                    return;
                }
                catch (DcmServiceException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                    return;
                }
                actDeletePlut.setEnabled(true);
            }
        };
        actCreatePlut.putValue(Action.NAME, "Create P-LUT");
        //Delete FilmSession
        actDeleteFilmSession = new AbstractAction()
        {
            public void actionPerformed(ActionEvent e)
            {
                try {
                    printSCU.deleteFilmSession();
                } catch (InterruptedException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                } catch (DcmServiceException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                nextImageBoxIndex = 0;
                setEnabled(false);
                actCreateFilmBox.setEnabled(false);
                actDeleteFilmBox.setEnabled(false);
                actCreateImageBox.setEnabled(false);
                actCreatePlut.setEnabled(false);
                actDeletePlut.setEnabled(false);
                actPrintFilmSession.setEnabled(false);
                actPrintFilmBox.setEnabled(false);
                actCreateFilmSession.setEnabled(true);
            }
        };
        actDeleteFilmSession.putValue(Action.NAME, "Delete Session");
        //Delete FilmBox
        actDeleteFilmBox = new AbstractAction()
        {
            public void actionPerformed(ActionEvent e)
            {
                try {
                    printSCU.deleteFilmBox();
                } catch (InterruptedException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                } catch (DcmServiceException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                nextImageBoxIndex = 0;
                setEnabled(false);
                actCreateFilmBox.setEnabled(true);
                actCreateImageBox.setEnabled(false);
                actPrintFilmBox.setEnabled(false);
                actPrintFilmSession.setEnabled(false);
            }
        };
        actDeleteFilmBox.putValue(Action.NAME, "Delete FilmBox");
        //Delete P-LUT
        actDeletePlut = new AbstractAction()
        {
            public void actionPerformed(ActionEvent e)
            {
                try {
                    String uid = (String)plutUidList.remove(plutUidList.size() - 1);
                    printSCU.deletePLUT(uid);
                } catch (InterruptedException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                } catch (DcmServiceException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                if (plutUidList.size() == 0) {
                    actDeletePlut.setEnabled(false);
                }
            }
        };
        actDeletePlut.putValue(Action.NAME, "Delete P-LUT");
        //Print FilmSession
        actPrintFilmSession = new AbstractAction()
        {
            public void actionPerformed(ActionEvent e)
            {
                try {
                    printSCU.printFilmSession();
                }
                catch (InterruptedException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                    return;
                }
                catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                    return;
                }
                catch (DcmServiceException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                    return;
                }
            }
        };
        actPrintFilmSession.putValue(Action.NAME, "Print FilmSession");
        //Print FilmBox
        actPrintFilmBox = new AbstractAction()
        {
            public void actionPerformed(ActionEvent e)
            {
                try {
                    printSCU.printFilmBox();
                }
                catch (InterruptedException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                    return;
                }
                catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                    return;
                }
                catch (DcmServiceException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                    return;
                }
            }
        };
        actPrintFilmBox.putValue(Action.NAME, "Print FilmBox");
        
        //disable all buttons
        onDisconnect();
        
        //set up buttons for commands
        btnPanel.add(new JLabel(""));
        JButton btnConnect = new JButton(actConnect);
        btnPanel.add(btnConnect);
        JButton btnRelease = new JButton(actRelease);
        btnPanel.add(btnRelease);
        
        JButton btnCreateFilmSession = new JButton(actCreateFilmSession);
        btnPanel.add(btnCreateFilmSession);
        JButton btnCreateFilmBox = new JButton(actCreateFilmBox);
        btnPanel.add(btnCreateFilmBox);
        JButton btnCreateImageBox = new JButton(actCreateImageBox);
        btnPanel.add(btnCreateImageBox);
        
        JButton btnDeleteFilmSession = new JButton(actDeleteFilmSession);
        btnPanel.add(btnDeleteFilmSession);
        JButton btnDeleteFilmBox = new JButton(actDeleteFilmBox);
        btnPanel.add(btnDeleteFilmBox);
        JButton btnPrintFilmSession = new JButton(actPrintFilmSession);
        btnPanel.add(btnPrintFilmSession);
        
        JButton btnCreatePlut = new JButton(actCreatePlut);
        btnPanel.add(btnCreatePlut);
        JButton btnDeletePlut = new JButton(actDeletePlut);
        btnPanel.add(btnDeletePlut);
        JButton btnPrintFilmBox = new JButton(actPrintFilmBox);
        btnPanel.add(btnPrintFilmBox);
    }

    PrintSCUFrame(String title)
    {
        this();
        setTitle(title);
    }

    private void onDisconnect()
    {
        actRelease.setEnabled(false);
        actCreateFilmSession.setEnabled(false);
        actCreateFilmBox.setEnabled(false);
        actCreateImageBox.setEnabled(false);
        actCreatePlut.setEnabled(false);
        actPrintFilmSession.setEnabled(false);
        actPrintFilmBox.setEnabled(false);
        actDeleteFilmSession.setEnabled(false);
        actDeleteFilmBox.setEnabled(false);
        actDeletePlut.setEnabled(false);
    }

    /* for abnormal exit */
    private static void exit(String msg)
    {
        System.out.println(msg);
        System.out.println(USAGE);
        System.exit(1);
    }

    private final static String USAGE =
            "Usage: java -jar printSCU.jar [OPTIONS]\n\n" +
            "Connects to a DICOM Print Service Class Provider.\n" +
            "Options:\n" +
            " -h --help        show this help and exit\n";

    public static void main(String[] args)
    {
        BasicConfigurator.configure();
        LongOpt[] longopts = {
                new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h')
            };

        Getopt g = new Getopt("printSCU", args, "t:a:h", longopts, true);
        try {
            int c;
            while ((c = g.getopt()) != -1) {
                switch (c) {
                    case 'h':
                    case '?':
                        exit("");
                        break;
                }
            }
            int optind = g.getOptind();
            int argc = args.length - optind;
            if (argc != 0) {
                exit("printSCU: wrong number of arguments\n");
            }
            PrintSCUFrame printSCU = new PrintSCUFrame("Print SCU Client");
            printSCU.log.setLevel(Level.WARN);
            printSCU.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            printSCU.show();
        }
        catch (IllegalArgumentException e) {
            exit("printSCU: illegal argument - " + e.getMessage() + "\n");
        }
    }
}
