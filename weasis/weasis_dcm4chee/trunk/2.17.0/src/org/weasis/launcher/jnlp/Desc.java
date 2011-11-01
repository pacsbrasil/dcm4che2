package org.weasis.launcher.jnlp;

/**
 * This abstract class represent the "*-desc" section of a Jnlp file. It is a superclass of classes ApplicationDesc,
 * AppletDesc, ComponentDesc and InstallerDesc. Subclasses of Desc must implement the toXml() method.
 * 
 * @author jlrz
 * 
 */
public abstract class Desc {

    /**
     * Converts to a string representing the "*-desc" section of a Jnlp file in XML format
     * 
     * @return a string representing the "*-desc" section of a Jnlp file in XML format
     */
    abstract String toXml();

}
