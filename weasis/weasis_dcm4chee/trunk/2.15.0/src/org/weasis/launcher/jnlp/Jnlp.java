package org.weasis.launcher.jnlp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * This class represent a JNLP file
 * 
 * @author jlrz
 * 
 */
public class Jnlp {

    private static Logger logger = LoggerFactory.getLogger(Jnlp.class);
    public static final String FILE_PREFIX = "launcher";
    public static final String FILE_EXTENSION = ".jnlp";

    private final Properties jnlpProp;

    private Information information;
    private Resources resources;
    private Desc desc;

    /**
     * Creates an empty instance of Jnlp class
     */
    public Jnlp(Properties jnlpProp) {
        this.jnlpProp = jnlpProp;
        information = null;
        resources = null;
        desc = null;
    }

    public Information getInformation() {
        return information;
    }

    public void setInformation(Information information) {
        this.information = information;
    }

    public Resources getResources() {
        return resources;
    }

    public void setResources(Resources resources) {
        this.resources = resources;
    }

    public Desc getDesc() {
        return desc;
    }

    public void setDesc(Desc desc) {
        this.desc = desc;
    }

    /**
     * Converts to a string representing the jnlp file in XML format
     * 
     * @return a string representing the jnlp file in XML format
     */
    public String toXml() {
        StringBuffer result;

        result = new StringBuffer();

        result.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n");
        result
            .append("<!DOCTYPE jnlp PUBLIC \"-//Sun Microsystems, Inc//DTD JNLP Descriptor 6.0//EN\" \"http://java.sun.com/dtd/JNLP-6.0.dtd\">\n");
        if (jnlpProp != null) {
            // TODO do not write the entry if the property is null
            result.append("<jnlp spec=\"");
            result.append(jnlpProp.getProperty("jnlp.spec"));
            result.append("\" version=\"");
            result.append(jnlpProp.getProperty("jnlp.version"));
            result.append("\" codebase=\"");
            result.append(jnlpProp.getProperty("jnlp.codebase", ""));
            result.append("\" href=\"");
            result.append(jnlpProp.getProperty("jnlp.href", ""));
            result.append("\">\n");
            result.append(information.toXml());
            result.append("<security>");
            result.append(jnlpProp.getProperty("security", "<all-permissions/>"));
            result.append("</security>\n");
            // only for JNLP version 6 (means java 6 plugin for browser must be installed)
            // result.append("<update check=\"");
            // result.append(jnlpProp.getProperty("update.check"));
            // result.append("\" policy=\"");
            // result.append(jnlpProp.getProperty("update.policy"));
            // result.append("\" />\n");
            result.append(resources.toXml());
            result.append(desc.toXml());
        }
        result.append("</jnlp>");

        return result.toString();
    }

    /**
     * Save current Jnlp to a temporary file and returns the name of the created file.
     * 
     * @param path
     *            path of the temporary file to create
     * @param prefix
     *            prefix of the temporary file to create
     * @param suffix
     *            suffix of the temporary file to create
     * @return the name of the created temporary file
     * @throws JnlpException
     *             if an error occurs
     */
    public String saveToTmpFile(String path) throws JnlpException {
        File jnlpTmpFile = null;
        FileOutputStream jnlpStream = null;

        try {
            File folderTemp = new File(path);
            if (!folderTemp.exists()) {
                if (!folderTemp.mkdirs()) {
                    logger.error("Cannot make folder : " + folderTemp);
                    throw new JnlpException(JnlpException.CANNOT_CREATE_JNLP_FILE);
                }
            }
            jnlpTmpFile = File.createTempFile(FILE_PREFIX, FILE_EXTENSION, folderTemp);
        } catch (IOException e) {
            logger.error(e.getMessage());
            throw new JnlpException(JnlpException.CANNOT_CREATE_JNLP_FILE);
        }

        try {
            jnlpStream = new FileOutputStream(jnlpTmpFile);
            jnlpStream.write(toXml().getBytes());
            jnlpStream.flush();
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new JnlpException(JnlpException.CANNOT_WRITE_TO_JNLP_FILE);
        } finally {
            try {
                if (jnlpStream != null) {
                    jnlpStream.close();
                }
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        }
        logger.info("JNLP saved to temporary file: " + jnlpTmpFile);
        return jnlpTmpFile.getName();
    }

}
