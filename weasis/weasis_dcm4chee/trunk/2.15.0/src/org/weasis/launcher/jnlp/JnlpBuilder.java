package org.weasis.launcher.jnlp;

import java.util.Properties;

/**
 * This class provides some methods to create Jnlp documents.
 * 
 * @author jlrz
 */
public class JnlpBuilder {

    /**
     * 
     * @param props
     * @return
     */
    public static Jnlp createJnlpFromProperties(Properties props) {

        // Jnlp Main Section
        Jnlp jnlp = new Jnlp(props);
        // Adds sections to Jnlp
        jnlp.setInformation(new Information(props));
        jnlp.setResources(new Resources(props));

        // Application-desc Section
        ApplicationDesc applicationDesc = new ApplicationDesc();
        applicationDesc.setApplicationMain(props.getProperty("application.main"));
        int num = 1;
        String key = "application.argument.";
  
        while (props.containsKey(key + num)) {
            applicationDesc.addApplicationArg(props.getProperty(key + num));
            num++;
        }
        jnlp.setDesc(applicationDesc);

        return jnlp;
    }

}
