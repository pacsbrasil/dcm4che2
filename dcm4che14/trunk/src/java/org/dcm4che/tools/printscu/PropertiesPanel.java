package org.dcm4che.tools.printscu;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import org.apache.log4j.Logger;

public class PropertiesPanel extends JPanel implements TableModelListener, MouseListener
{
    private Logger log;
    private Properties props;
    /*private String host;
    private String callingAET, calledAET;
    private int port;*/

    static final String[] KEYS =
    {
        "Host",
        "Port",
        "CalledAET",
        "CallingAET",
        "MaxPduSize", //ignored
        "Grouplens",  //ignored
        "SOP.Verification",
        "SOP.BasicGrayscalePrintManagement",
        "SOP.BasicColorPrintManagement",
        "SOP.BasicAnnotationBox",
        "SOP.BasicPrintImageOverlayBox",
        "SOP.PresentationLUT",
        "SOP.PrintJob",
        "SOP.PrinterConfigurationRetrieval",
        "Session.NumberOfCopies",
        "Session.PrintPriority",
        "Session.MediumType",
        "Session.FilmDestination",
        "Session.FilmSessionLabel",
        "Session.MemoryAllocation",
        "Session.OwnerID",
        "FilmBox.ImageDisplayFormat",
        "FilmBox.FilmOrientation",
        "FilmBox.FilmSizeID",
        "FilmBox.RequestedResolutionID",
        "FilmBox.AnnotationDisplayFormatID",
        "FilmBox.MagnificationType",
        "FilmBox.SmoothingType",
        "FilmBox.BorderDensity",
        "FilmBox.EmptyImageDensity",
        "FilmBox.MinDensity",
        "FilmBox.MaxDensity",
        "FilmBox.Trim",
        "FilmBox.ConfigurationInformation",
        "FilmBox.Illumination",
        "FilmBox.ReflectedAmbientLight",
        "ImageBox.Polarity",
        "ImageBox.MagnificationType",
        "ImageBox.SmoothingType",
        "ImageBox.MinDensity",
        "ImageBox.MaxDensity",
        "ImageBox.ConfigurationInformation",
        "ImageBox.RequestedDecimateCropBehavior",
        "ImageBox.RequestedImageSize",
        "LUT.Shape",
        "LUT.Gamma",
        "LUT.Level",
        "LUT.ScaleToFitBitDepth",
        "LUT.ApplyBySCU",
        "User.SendAspectRatio",
        "User.RequestedZoom",
        "User.BurnInInfo",
        "User.BurnInInfo.Properties",
        "User.BitDepth",
        "User.InflateBitsAlloc",
        "User.MinMaxWindowing",
        "Verbose",
        "DumpCmdsetIntoDir",
        "DumpDatasetIntoDir",
    };

    private static final Properties DEFAULTS;

    static { //initialize some defaults
        DEFAULTS = new Properties();
        DEFAULTS.put("Host", "localhost");
        DEFAULTS.put("CallingAET", "PrintSCU");
        DEFAULTS.put("CalledAET", "TIANI_PRINT");
        DEFAULTS.put("Port", "6104");
        DEFAULTS.put("FilmBox.ImageDisplayFormat", "STANDARD\\1,1");
        DEFAULTS.put("FilmBox.AnnotationDisplayFormatID", "TITLE");
    }

    private static final String[] PRINT_PRIORITY = {
      "","HIGH","MED","LOW"
    };
    private static final String[] MEDIUM_TYPE = {
      "","PAPER","CLEAR FILM","BLUE FILM"
    };
    private static final String[] FILM_DESTINATION = {
      "","MAGAZINE","PROCESSOR","BIN_1","BIN_2","BIN_3","BIN_4","BIN_5","BIN_6","BIN_7","BIN_8"
    };
    private static final String[] IMAGE_DISPLAY_FORMAT = {
      "STANDARD\\1,1","STANDARD\\2,3","ROW\\2","COL\\2","SLIDE","SUPERSLIDE","CUSTOM\\1"
    };
    private static final String[] FILM_ORIENTATION = {
      "","PORTRAIT","LANDSCAPE"
    };
    private static final String[] FILM_SIZE_ID = {
      "","8INX10IN","10INX12IN","10INX14IN","11INX14IN","14INX14IN","14INX17IN","24CMX24CM","24CMX30CM"
    };
    private static final String[] MAGNIFICATION_TYPE= {
      "","REPLICATE","BILINEAR","CUBIC","NONE"
    };
    private static final String[] DENSITY = {
      "","BLACK","WHITE"
    };
    private static final String[] YES_NO = {
      "","YES","NO"
    };
    private static final String[] REQUESTED_RESOLUTION_ID = {
      "","STANDARD","HIGH"
    };
    private static final String[] POLARITY = {
      "","NORMAL","REVERSE"
    };
    private static final String[] REQUESTED_DECIMATE_CROP_BEHAVIOR = {
      "","DECIMATE","CROP","FAIL"
    };
    private static final String[] SEND_ASPECTRATIO = {
      "Always","IfNot1/1"
    };
    private static final String[] BURNIN_INFO = {
      "No","IfNoOverlays","Always"
    };

    static final int LUT_FILE = 0;
    static final int LUT_GAMMA = 1;
    static final int LUT_IDENTITY = 2;
    static final int LUT_LIN_OD = 3;
    static final int LUT_INVERSE = 4;

    private static final String[] LUT_SHAPE = {
      "<file>","<gamma>","IDENTITY","LIN OD","INVERSE"
    };
    private static final String[] LUT_LEVEL = {
      "FilmSession","FilmBox","ImageBox"
    };
    private static final String[] INFLATE_BIT_DEPTH = {
      "Always","IfNonLinear","No"
    };
    private static final String[] VERBOSE = {
      "0","1","2","3","4","5","6"
    };

    private PrintSCUFrame printSCUFrame;
    private JTable table;
    private TableModel model;

    PropertiesPanel(PrintSCUFrame printSCUFrame, Logger log, String fileName)
    {
        super();
        this.printSCUFrame = printSCUFrame;
        this.log = log;
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        //load properties file
        File file = new File(fileName);
        props = loadProperties(file);
        //fill properties table
        table = new JTable(KEYS.length, 2);
        for (int i = 0; i < KEYS.length; i++) {
            table.setValueAt(KEYS[i], i, 0);
            table.setValueAt(props.getProperty(KEYS[i]), i, 1);
        }
        model = table.getModel();
        model.addTableModelListener(this);
        table.addMouseListener(this);
        add(table);
    }

    public void mouseClicked(MouseEvent e)
    {
        int column = table.getSelectedColumn();
        int row = table.getSelectedRow();
        if (column == 0) {
            String prop = (String)table.getValueAt(row, 0);
            String value = props.getProperty(prop);
            String[] choices;
            if (prop.equals("Session.FilmDestination"))
                choices = FILM_DESTINATION;
            else if (prop.equals("Session.FilmOrientation"))
                choices = FILM_ORIENTATION;
            else if (prop.equals("Session.MediumType"))
                choices = MEDIUM_TYPE;
            else if (prop.equals("Session.PrintPriority"))
                choices = PRINT_PRIORITY;
            else if (prop.equals("FilmBox.ImageDisplayFormat"))
                choices = IMAGE_DISPLAY_FORMAT;
            else if (prop.equals("FilmBox.FilmSizeID"))
                choices = FILM_SIZE_ID;
            else if (prop.equals("FilmBox.MagnificationType"))
                choices = MAGNIFICATION_TYPE;
            else if (prop.equals("FilmBox.RequestedResolutionID"))
                choices = REQUESTED_RESOLUTION_ID;
            else if (prop.equals("ImageBox.RequestedDecimateCropBehavior"))
                choices = REQUESTED_DECIMATE_CROP_BEHAVIOR;
            else if (prop.equals("ImageBox.Polarity"))
                choices = POLARITY;
            else if (prop.equals("User.SendAspectRatio"))
                choices = SEND_ASPECTRATIO;
            else if (prop.equals("User.BurnInInfo"))
                choices = BURNIN_INFO;
            else if (prop.equals("Verbose"))
                choices = VERBOSE;
            else
                return;
            value = (String)JOptionPane.showInputDialog(printSCUFrame,
                "Choose a value:", prop, JOptionPane.QUESTION_MESSAGE,
                null, choices, choices[0]);
            if (value != null) {
                table.setValueAt(value, row, 1);
                if (value.equals(""))
                    props.remove(prop);
                else
                    props.setProperty(prop, value);
            }
        }
    }
    public void mouseExited(MouseEvent e)
    {
    }
    public void mouseEntered(MouseEvent e)
    {
    }
    public void mousePressed(MouseEvent e)
    {
    }
    public void mouseReleased(MouseEvent e)
    {
    }

    public void tableChanged(TableModelEvent e)
    {
        int row = e.getFirstRow();
        int column = e.getColumn();
        if (column != 1)
            return;
        String prop = (String)model.getValueAt(row, 0);
        String data = (String)model.getValueAt(row, 1);
        props.put(prop, data);
        printSCUFrame.propertyChanged(prop);
    }

    private Properties loadProperties(File file) {
        Properties props = new Properties(DEFAULTS);
        try {
            props.load(new BufferedInputStream(new FileInputStream(file)));
        }
        catch (FileNotFoundException e) {
            log.warn("No properties file was found, using default settings");
        }
        catch (IOException e) {
            log.warn("Can not load properties file");
        }
        return props;
    }

    String getProperty(String key)
    {
        return props.getProperty(key);
    }
}
