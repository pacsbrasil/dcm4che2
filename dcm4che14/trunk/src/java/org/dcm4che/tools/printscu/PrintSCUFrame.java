package org.dcm4che.tools.printscu;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dcm4che.client.AssociationRequestor;
import org.dcm4che.client.PrintSCU;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.DcmParser;
import org.dcm4che.data.DcmParserFactory;
import org.dcm4che.dict.Tags;
import org.dcm4che.net.DcmServiceException;
import org.dcm4che.util.UIDGenerator;

public class PrintSCUFrame extends JFrame
{
    public static final String DEFAULT_PROPERTIES_FILE = "PrintSCU.properties";

    private static final int DEF_WIDTH = 600, DEF_HEIGHT = 500;

    private final Logger log = Logger.getLogger("PrintSCU Client");

    private AssociationRequestor assocRq = new AssociationRequestor();
    private PrintSCU printSCU;
    private String curPLutUid;
    private int nextImageBoxIndex = 0;
    private boolean colorMode = false;
    private Action actConnect, actRelease, actCreateFilmSession, actDeleteFilmSession,
        actCreateFilmBox, actDeleteFilmBox, actCreateImageBox, actCreatePlut,
        actDeletePlut, actPrintFilmSession, actPrintFilmBox, actExit;
    private File lastFile = null; //for JFileChooser to remember last dir
    private JFileChooser chooser = new JFileChooser();
    private DcmObjectFactory dcmFactory = DcmObjectFactory.getInstance();
    private UIDGenerator uidGen = UIDGenerator.getInstance();
    private JSplitPane panel;
    private JPanel btnPanel;
    private PropertiesPanel propPanel;

    public static final class PrintSCUConfigurationException extends RuntimeException
    {
        PrintSCUConfigurationException() { super(); }
        PrintSCUConfigurationException(String msg) { super(msg); }
    }

    PrintSCUFrame()
    {
        Container contentPane = this.getContentPane();
        btnPanel = new JPanel();
        btnPanel.setLayout(new GridLayout(2, 3));
        propPanel = new PropertiesPanel(this, log, DEFAULT_PROPERTIES_FILE);
        JScrollPane scrollingPanel = new JScrollPane(propPanel);
        contentPane.add(panel = new JSplitPane(
            JSplitPane.VERTICAL_SPLIT, btnPanel, scrollingPanel));
        btnPanel.setMinimumSize(new Dimension(DEF_WIDTH, DEF_HEIGHT/4));
        propPanel.setMinimumSize(new Dimension(DEF_WIDTH, DEF_HEIGHT/8));
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
                Integer anInt;
                String aString;
                
                try {
                    if ((anInt = getIntegerFromProperty("MaxPduSize")) != null)
                        assocRq.setMaxPDULength(anInt.intValue());
                    if ((aString = getStringFromProperty("CallingAET")) == null)
                        throw new PrintSCUConfigurationException();
                    assocRq.setCallingAET(aString);
                    if ((aString = getStringFromProperty("CalledAET")) == null)
                        throw new PrintSCUConfigurationException();
                    assocRq.setCalledAET(aString);
                    if ((aString = getStringFromProperty("Host")) == null)
                        throw new PrintSCUConfigurationException();
                    assocRq.setHost(aString);
                    if ((anInt = getIntegerFromProperty("Port")) == null)
                        throw new PrintSCUConfigurationException();
                    assocRq.setPort(anInt.intValue());
                }
                catch (PrintSCUConfigurationException e1) {
                    JOptionPane.showMessageDialog(PrintSCUFrame.this, e1);
                }
                
                printSCU = new PrintSCU(assocRq);
                printSCU.setAutoRefPLUT(true); //always create P-LUT when Film Box is created
                printSCU.setCreateRQwithIUID(true);
                printSCU.setNegotiatePLUT(true);
                printSCU.setNegotiateAnnotation(true);
                printSCU.setNegotiateColorPrint(colorMode);
                printSCU.setNegotiateGrayscalePrint(!colorMode);
                curPLutUid = new String();
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
                String prop;
                
                if ((prop = getStringFromProperty("Session.NumberOfCopies")) != null)
                    attr.putIS(Tags.NumberOfCopies, propPanel.getIntProperty("Session.NumberOfCopies"));
                if ((prop = getStringFromProperty("Session.PrintPriority")) != null)
                    attr.putCS(Tags.PrintPriority, prop);
                if ((prop = getStringFromProperty("Session.MediumType")) != null)
                    attr.putCS(Tags.MediumType, prop);
                if ((prop = getStringFromProperty("Session.FilmDestination")) != null)
                    attr.putCS(Tags.FilmDestination, prop);
                if ((prop = getStringFromProperty("Session.FilmSessionLabel")) != null)
                    attr.putLO(Tags.FilmSessionLabel, prop);
                if ((prop = getStringFromProperty("Session.MemoryAllocation")) != null)
                    attr.putIS(Tags.MemoryAllocation, propPanel.getIntProperty("Session.MemoryAllocation"));
                if ((prop = getStringFromProperty("Session.OwnerID")) != null)
                    attr.putSH(Tags.OwnerID, prop);

                //dump to log
                dump(attr, "Film Session");
                
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
        actCreateFilmSession.putValue(Action.NAME, "Create FilmSession");
        //Create FilmBox
        actCreateFilmBox = new AbstractAction()
        {
            public void actionPerformed(ActionEvent e)
            {
                Dataset attr = dcmFactory.newDataset();
                String prop;
                Integer propInt;
                
                //printSCU.setAutoRefPLUT(true);
                if ((prop = getStringFromProperty("FilmBox.ImageDisplayFormat")) != null)
                    attr.putST(Tags.ImageDisplayFormat, prop);
                if ((prop = getStringFromProperty("FilmBox.FilmOrientation")) != null)
                    attr.putCS(Tags.FilmOrientation, prop);
                if ((prop = getStringFromProperty("FilmBox.FilmSizeID")) != null)
                    attr.putCS(Tags.FilmSizeID, prop);
                if ((prop = getStringFromProperty("FilmBox.RequestedResolutionID")) != null)
                    attr.putCS(Tags.RequestedResolutionID, prop);
                if ((prop = getStringFromProperty("FilmBox.AnnotationDisplayFormatID")) != null)
                    attr.putCS(Tags.AnnotationDisplayFormatID, prop);
                if ((prop = getStringFromProperty("FilmBox.MagnificationType")) != null)
                    attr.putCS(Tags.MagnificationType, prop);
                if ((prop = getStringFromProperty("FilmBox.SmoothingType")) != null)
                    attr.putCS(Tags.SmoothingType, prop);
                if ((prop = getStringFromProperty("FilmBox.BorderDensity")) != null)
                    attr.putCS(Tags.BorderDensity, prop);
                if ((prop = getStringFromProperty("FilmBox.EmptyImageDensity")) != null)
                    attr.putCS(Tags.EmptyImageDensity, prop);
                if ((propInt = getIntegerFromProperty("FilmBox.MinDensity")) != null)
                    attr.putUS(Tags.MinDensity, propInt.intValue());
                if ((propInt = getIntegerFromProperty("FilmBox.MaxDensity")) != null)
                    attr.putUS(Tags.MaxDensity, propInt.intValue());
                if ((prop = getStringFromProperty("FilmBox.Trim")) != null)
                    attr.putCS(Tags.Trim, prop);
                if ((prop = getStringFromProperty("FilmBox.ConfigurationInformation")) != null)
                    attr.putST(Tags.ConfigurationInformation, prop);
                if ((propInt = getIntegerFromProperty("FilmBox.Illumination")) != null)
                    attr.putUS(Tags.Illumination, propInt.intValue());
                if ((propInt = getIntegerFromProperty("FilmBox.ReflectedAmbientLight")) != null)
                    attr.putUS(Tags.ReflectedAmbientLight, propInt.intValue());

                //dump to log
                dump(attr, "Film Box");
                
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
                actCreatePlut.setEnabled(false);
                actDeletePlut.setEnabled(false);
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
                    String prop;
                    Integer propInt;
                    String configInfo;

                    if ((prop = getStringFromProperty("FilmBox.Polarity")) != null)
                        attr.putCS(Tags.Polarity, prop);
                    if ((prop = getStringFromProperty("FilmBox.MagnificationType")) != null)
                        attr.putCS(Tags.MagnificationType, prop);
                    if ((prop = getStringFromProperty("FilmBox.SmoothingType")) != null)
                        attr.putCS(Tags.SmoothingType, prop);
                    if ((propInt = getIntegerFromProperty("FilmBox.MinDensity")) != null)
                        attr.putUS(Tags.MinDensity, propInt.intValue());
                    if ((propInt = getIntegerFromProperty("FilmBox.MaxDensity")) != null)
                        attr.putUS(Tags.MaxDensity, propInt.intValue());
                    if ((prop = getStringFromProperty("FilmBox.RequestedDecimateCropBehavior")) != null)
                        attr.putCS(Tags.RequestedDecimateCropBehavior, prop);
                    if ((prop = getStringFromProperty("FilmBox.RequestedImageSize")) != null)
                        attr.putDS(Tags.RequestedImageSize, prop);
                    configInfo = getStringFromProperty("FilmBox.ConfigurationInformation");
                    
                    try {
                        if (curPLutUid == null) {
                            if ((prop = getStringFromProperty("LUT.Gamma")) != null) {
                                if (configInfo == null)
                                    configInfo = "gamma=" + prop;
                                else
                                    configInfo = configInfo + "\\gamma=" + prop;
                            }
                            else if ((prop = getStringFromProperty("LUT.Shape")) != null) {
                                curPLutUid = printSCU.createPLUT(prop);
                            }
                            else
                                throw new PrintSCUConfigurationException(
                                    "You need to either create a P-LUT, set LUT.Shape, or LUT.Gamma");
                        }
                        //finally write config info (with the plut gamma placed, if it exists)
                        if (configInfo != null)
                            attr.putST(Tags.ConfigurationInformation, configInfo);
                        //dump to log
                        dump(attr, "Image Box");
                        //create image box
                        printSCU.setImageBox(nextImageBoxIndex++, file, attr);
                    }
                    catch (PrintSCUConfigurationException e1) {
                        JOptionPane.showMessageDialog(PrintSCUFrame.this, e1);
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
                    if (nextImageBoxIndex >= printSCU.countImageBoxes())
                        setEnabled(false);
                }
            }
        };
        actCreateImageBox.putValue(Action.NAME, "Create ImageBox");
        //Create P-LUT
        actCreatePlut = new AbstractAction()
        {
            public void actionPerformed(ActionEvent e)
            {
                String shape;
                Dataset ds = dcmFactory.newDataset();
                
                if (chooser.showOpenDialog(PrintSCUFrame.this) != JFileChooser.APPROVE_OPTION)
                    return;
                File file = chooser.getSelectedFile();
                try {
                    DcmParser parser = DcmParserFactory.getInstance().newDcmParser(
                        new BufferedInputStream(new FileInputStream(file)));
                    parser.setDcmHandler(ds.getDcmHandler());
                    parser.parseDcmFile(null, -1);
                    if (ds.vm(Tags.PresentationLUTSeq) == -1)
                        throw new IOException();
                }
                catch (FileNotFoundException e1) {
                    JOptionPane.showMessageDialog(PrintSCUFrame.this,
                        "Could not open file: " + file);
                }
                catch (IOException e1) {
                    JOptionPane.showMessageDialog(PrintSCUFrame.this,
                        "Could not read file: " + file);
                }
                
                try {
                    curPLutUid = printSCU.createPLUT(ds);
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
                setEnabled(false);
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
        actDeleteFilmSession.putValue(Action.NAME, "Delete FilmSession");
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
                actCreatePlut.setEnabled(true);
                actDeletePlut.setEnabled(true);
            }
        };
        actDeleteFilmBox.putValue(Action.NAME, "Delete FilmBox");
        //Delete P-LUT
        actDeletePlut = new AbstractAction()
        {
            public void actionPerformed(ActionEvent e)
            {
                try {
                    printSCU.deletePLUT(curPLutUid);
                    curPLutUid = null;
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
                setEnabled(false);
                actCreatePlut.setEnabled(true);
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
        
        JPanel subBtnPanel = new JPanel();
        subBtnPanel.setLayout(new GridLayout(3, 1));
        btnPanel.add(subBtnPanel);
        //
        subBtnPanel.add(new JLabel("Print Server"));
        JButton btnConnect = new JButton(actConnect);
        subBtnPanel.add(btnConnect);
        JButton btnRelease = new JButton(actRelease);
        subBtnPanel.add(btnRelease);
        
        subBtnPanel = new JPanel();
        subBtnPanel.setLayout(new GridLayout(3, 1));
        btnPanel.add(subBtnPanel);
        //
        subBtnPanel.add(new JLabel("Film Session"));
        JButton btnCreateFilmSession = new JButton(actCreateFilmSession);
        subBtnPanel.add(btnCreateFilmSession);
        JButton btnDeleteFilmSession = new JButton(actDeleteFilmSession);
        subBtnPanel.add(btnDeleteFilmSession);

        subBtnPanel = new JPanel();
        subBtnPanel.setLayout(new GridLayout(3, 1));
        btnPanel.add(subBtnPanel);
        //
        subBtnPanel.add(new JLabel("Film Box"));
        JButton btnCreateFilmBox = new JButton(actCreateFilmBox);
        subBtnPanel.add(btnCreateFilmBox);
        JButton btnDeleteFilmBox = new JButton(actDeleteFilmBox);
        subBtnPanel.add(btnDeleteFilmBox);

        subBtnPanel = new JPanel();
        subBtnPanel.setLayout(new GridLayout(3, 1));
        btnPanel.add(subBtnPanel);
        //
        subBtnPanel.add(new JLabel("Image Box"));
        JButton btnCreateImageBox = new JButton(actCreateImageBox);
        subBtnPanel.add(btnCreateImageBox);
        
        subBtnPanel = new JPanel();
        subBtnPanel.setLayout(new GridLayout(3, 1));
        btnPanel.add(subBtnPanel);
        //
        subBtnPanel.add(new JLabel("Print"));
        JButton btnPrintFilmSession = new JButton(actPrintFilmSession);
        subBtnPanel.add(btnPrintFilmSession);
        JButton btnPrintFilmBox = new JButton(actPrintFilmBox);
        subBtnPanel.add(btnPrintFilmBox);
        
        subBtnPanel = new JPanel();
        subBtnPanel.setLayout(new GridLayout(3, 1));
        btnPanel.add(subBtnPanel);
        //
        subBtnPanel.add(new JLabel("Presentation LUT"));
        JButton btnCreatePlut = new JButton(actCreatePlut);
        subBtnPanel.add(btnCreatePlut);
        JButton btnDeletePlut = new JButton(actDeletePlut);
        subBtnPanel.add(btnDeletePlut);
        
        updateFromProperties();
    }

    public void updateFromProperties()
    {
        //Verbose
        Boolean verbose;
        if ((verbose = (Boolean)getFromProperty("Verbose", Boolean.class)) != null)
            log.setLevel((verbose.booleanValue()) ? Level.ALL : Level.WARN);
        else
            log.setLevel(Level.WARN);
        //
    }

    PrintSCUFrame(String title)
    {
        this();
        setTitle(title);
    }

    protected void dump(Dataset ds, String from)
    {
        StringWriter out = new StringWriter();
        try {
            ds.dumpDataset(out, null);
        }
        catch (IOException ioe) {
            log.warn("Could not dump attributes for " + from);
        }
        log.info(out.toString());
    }

    protected String getStringFromProperty(String propertyName)
    {
        return (String)getFromProperty(propertyName, String.class);
    }
    protected Integer getIntegerFromProperty(String propertyName)
    {
        return (Integer)getFromProperty(propertyName, Integer.class);
    }

    private final Object getFromProperty(String propertyName, Class argType)
    {
        String prop;
        Object ret = null; //sending an unknown Class returns null to caller
        
        if ((prop = propPanel.getProperty(propertyName)) != null) {
            try {
                if (argType == String.class)
                    ret = prop;
                else if (argType == Integer.class)
                    ret = Integer.valueOf(prop);
                else if (argType == Boolean.class)
                    ret = Boolean.valueOf("true".equalsIgnoreCase(prop)
                                          || "yes".equalsIgnoreCase(prop)
                                          || "1".equals(prop));
            }
            catch (NumberFormatException e) {
                log.warn(propertyName + " is an invalid number");
            }
        }
        if (ret != null) {
            log.debug("Setting property " + propertyName + " = " + ret);
        }
        return ret;
    }

    /*protected void setFromProperty(Method method, String propertyName, Optionality optionality)
    {
        if (method.getParameterTypes().length > 1)
            throw new IllegalArgumentException("method passed to \"setFromProp\""
                + "must take only one parameter");
        boolean missing = true;
        String prop;
        
        if ((prop = propPanel.getProperty(propertyName)) != null) {
            Class argType = method.getParameterTypes()[0];
            Class[] args;
            try {
                if (argType == String.class)
                    method.invoke(assocRq, new Object[] { prop });
                else if (argType == int.class)
                    method.invoke(assocRq, new Object[] { Integer.valueOf(prop) });
                missing = false;
            }
            catch (NumberFormatException e) {
                log.warn(propertyName + " is an invalid integer");
            }
            catch (InvocationTargetException e) {
                log.error(e.getCause());
            }
            catch (IllegalAccessException e) {
                log.error(e.getCause());
            }
        }
        if (missing && optionality == Optionality.Required) {
            log.debug(propertyName + " is a required property!");
            throw new PrintSCUConfigurationException(propertyName + " is a required property!");
        }
        else {
            log.debug("setting " + propertyName + " = " + prop);
        }
    }*/

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
            printSCU.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            printSCU.show();
        }
        catch (IllegalArgumentException e) {
            exit("printSCU: illegal argument - " + e.getMessage() + "\n");
        }
    }
}
