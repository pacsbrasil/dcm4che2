package org.weasis.launcher.jnlp;

import java.util.Properties;

/**
 * This class represent the "information" section of a Jnlp file
 * 
 * @author jlrz
 * 
 */
public class Information {

    private Properties jnlpProp;

    /**
     * Creates an empty instance of Information class
     * 
     * @param props
     */
    public Information(Properties jnlpProp) {
        this.jnlpProp = jnlpProp;
    }

    /**
     * Converts to a string representing the "information" section of a Jnlp file in XML format
     * 
     * @return a string representing the "information" section of a Jnlp file in XML format
     */
    public String toXml() {
        StringBuffer result;

        result = new StringBuffer();

        result.append("<information>\n");
        if (jnlpProp != null) {
            result.append("<title>");
            result.append(jnlpProp.getProperty("information.title"));
            result.append("</title>\n");
            result.append("<vendor>");
            result.append(jnlpProp.getProperty("information.vendor"));
            result.append("</vendor>\n");
            result.append("<homepage href=\"");
            result.append(jnlpProp.getProperty("information.homepage"));
            result.append("\"/>\n");
            result.append("<description>");
            result.append(jnlpProp.getProperty("information.description"));
            result.append("</description>\n");
            result.append("<description kind=\"short\">");
            result.append(jnlpProp.getProperty("information.description.short"));
            result.append("</description>\n");
            result.append("<description kind=\"one-line\">");
            result.append(jnlpProp.getProperty("information.description.online"));
            result.append("</description>\n");
            result.append("<description kind=\"tooltip\">");
            result.append(jnlpProp.getProperty("information.tooltip"));
            result.append("</description>\n");
            result.append("<icon href=\"");
            result.append(jnlpProp.getProperty("information.icon"));
            result.append("\" kind=\"default\" />\n");
            String splash = jnlpProp.getProperty("information.splash");
            if (splash != null && !splash.trim().equals("")) {
                result.append("<icon href=\"");
                result.append(splash);
                result.append("\" kind=\"splash\" />\n");
            }
            if (Boolean.valueOf(jnlpProp.getProperty("information.offline.allowed"))) {
                result.append("<offline-allowed/>\n");
            }
            result.append("<shortcut online=\"");
            result.append(jnlpProp.getProperty("information.shortcut.online"));
            result.append("\">\n");
            result.append("<desktop/>\n");
            result.append("<menu submenu=\"");
            result.append(jnlpProp.getProperty("information.shortcut.menu"));
            result.append("\"/>\n");
            result.append("</shortcut>\n");
        }
        result.append("</information>\n");

        return result.toString();
    }

}
