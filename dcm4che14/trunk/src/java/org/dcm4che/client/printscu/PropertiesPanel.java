package org.dcm4che.client.printscu;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import org.apache.log4j.Logger;

public class PropertiesPanel extends JPanel implements TableModelListener
{
    private Logger log;
    private Properties props;
    private String host;
    private String callingAET, calledAET;
    private int port;

    public static final String[] KEYS =
    {
        "Host",
        "Port",
        "CalledAET",
        "CallingAET",
        "MaxPduSize",
        "Grouplens",
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

    static {
        DEFAULTS = new Properties();
        DEFAULTS.put("Host", "localhost");
        DEFAULTS.put("CallingAET", "PrintSCU");
        DEFAULTS.put("CalledAET", "TIANI_PRINT");
        DEFAULTS.put("Port", "6104");
        DEFAULTS.put("FilmBox.ImageDisplayFormat", "STANDARD\\1,1");
    }

    private JTable table;
    private TableModel model;

    PropertiesPanel(Logger log, String fileName)
    {
        super();
        this.log = log;
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        //load properties file
        File file = new File(fileName);
        props = loadProperties(file);
        //set some members
        setHost(props.getProperty("Host", "localhost"));
        setCallingAET(props.getProperty("CallingAET"));
        setCalledAET(props.getProperty("CalledAET"));
        setPort(Integer.parseInt(props.getProperty("Port")));
        //
        table = new JTable(KEYS.length, 2);
        for (int i = 0; i < KEYS.length; i++) {
            table.setValueAt(KEYS[i], i, 0);
            table.setValueAt(props.getProperty(KEYS[i]), i, 1);
        }
        model = table.getModel();
        model.addTableModelListener(this);
        add(table);
    }

    public void tableChanged(TableModelEvent e)
    {
        int row = e.getFirstRow();
        int column = e.getColumn();
        if (column != 1)
            return;
        String columnName = model.getColumnName(column);
        String data = (String)model.getValueAt(row, column);
        props.put(KEYS[row], data);
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

    private int parseInt(String string, int defaultValue)
    {
        try {
            return Integer.parseInt(string);
        }
        catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    private boolean parseBool(String string, boolean defaultValue)
    {
        string = string.trim();
        if (string != null)
            return "true".equalsIgnoreCase(string)
                   || "yes".equalsIgnoreCase(string)
                   || "1".equals(string);
        else
            return defaultValue;
    }

    public String getProperty(String key)
    {
        return props.getProperty(key);
    }

    protected int getIntProperty(String key) {
        return Integer.parseInt(props.getProperty(key));
    }

    public String getCalledAET() {
        return calledAET;
    }

    public String getCallingAET() {
        return callingAET;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public void setCalledAET(String string) {
        calledAET = string;
    }

    public void setCallingAET(String string) {
        callingAET = string;
    }

    public void setHost(String string) {
        host = string;
    }

    public void setPort(int i) {
        port = i;
    }
}
