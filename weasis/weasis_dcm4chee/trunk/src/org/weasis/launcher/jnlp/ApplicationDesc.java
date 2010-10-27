package org.weasis.launcher.jnlp;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represent the "application-desc" section of a Jnlp file
 * 
 * @author jlrz
 * 
 */
public class ApplicationDesc extends Desc {

    private static Logger logger = LoggerFactory.getLogger(ApplicationDesc.class);

    private String applicationMain;
    private ArrayList<String> applicationArg;

    public ApplicationDesc() {
        logger.debug("Creates an empty instance of ApplicationDesc class");

        applicationMain = "";
        applicationArg = new ArrayList<String>();
    }

    public String getApplicationMain() {
        return applicationMain;
    }

    public void setApplicationMain(String applicationMain) {
        this.applicationMain = applicationMain;
    }

    public ArrayList<String> getApplicationArg() {
        return applicationArg;
    }

    public void addApplicationArg(String arg) {
        this.applicationArg.add(arg);
    }

    /**
     * Converts to a string representing the "application-desc" section of a Jnlp file in XML format
     * 
     * @return a string representing the "application-desc" section of a Jnlp file in XML format
     */
    @Override
    public String toXml() {
        StringBuffer result = new StringBuffer();
        result.append("<application-desc main-class=\"");
        result.append(applicationMain);
        result.append("\">\n");
        for (int i = 0; i < applicationArg.size(); i++) {
            result.append("<argument>");
            result.append(this.applicationArg.get(i));
            result.append("</argument>\n");
        }
        result.append("</application-desc>\n");

        return result.toString();
    }

}
