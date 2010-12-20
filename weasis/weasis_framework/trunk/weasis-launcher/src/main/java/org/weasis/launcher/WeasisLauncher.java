/*******************************************************************************
 * Copyright (c) 2010 Nicolas Roduit.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Nicolas Roduit - initial API and implementation
 ******************************************************************************/
package org.weasis.launcher;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import org.apache.felix.framework.Felix;
import org.apache.felix.framework.util.FelixConstants;
import org.apache.felix.framework.util.Util;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.util.tracker.ServiceTracker;

public class WeasisLauncher {

    /**
     * Switch for specifying bundle directory.
     **/
    public static final String BUNDLE_DIR_SWITCH = "-b"; //$NON-NLS-1$

    /**
     * The property name used to specify whether the launcher should install a shutdown hook.
     **/
    public static final String SHUTDOWN_HOOK_PROP = "felix.shutdown.hook"; //$NON-NLS-1$
    /**
     * The property name used to specify an URL to the system property file.
     **/
    public static final String SYSTEM_PROPERTIES_PROP = "felix.system.properties"; //$NON-NLS-1$
    /**
     * The default name used for the system properties file.
     **/
    public static final String SYSTEM_PROPERTIES_FILE_VALUE = "system.properties"; //$NON-NLS-1$
    /**
     * The property name used to specify an URL to the configuration property file to be used for the created the
     * framework instance.
     **/
    public static final String CONFIG_PROPERTIES_PROP = "felix.config.properties"; //$NON-NLS-1$
    /**
     * The default name used for the configuration properties file.
     **/
    public static final String CONFIG_PROPERTIES_FILE_VALUE = "config.properties"; //$NON-NLS-1$
    /**
     * Name of the configuration directory.
     */
    public static final String CONFIG_DIRECTORY = "conf"; //$NON-NLS-1$

    private static HostActivator m_activator = null;
    private static Felix m_felix = null;
    protected static ServiceTracker m_tracker = null;

    private static String APP_PROPERTY_FILE = "weasis.properties"; //$NON-NLS-1$
    public final static String P_WEASIS_VERSION = "weasis.version"; //$NON-NLS-1$
    public final static String P_WEASIS_PATH = "weasis.path"; //$NON-NLS-1$
    static Properties modulesi18n = null;
    private static String look = null;

    /**
     * <p>
     * This method performs the main task of constructing an framework instance and starting its execution. The
     * following functions are performed when invoked:
     * </p>
     * <ol>
     * <li><i><b>Examine and verify command-line arguments.</b></i> The launcher accepts a "<tt>-b</tt>" command line
     * switch to set the bundle auto-deploy directory and a single argument to set the bundle cache directory.</li>
     * <li><i><b>Read the system properties file.</b></i> This is a file containing properties to be pushed into
     * <tt>System.setProperty()</tt> before starting the framework. This mechanism is mainly shorthand for people
     * starting the framework from the command line to avoid having to specify a bunch of <tt>-D</tt> system property
     * definitions. The only properties defined in this file that will impact the framework's behavior are the those
     * concerning setting HTTP proxies, such as <tt>http.proxyHost</tt>, <tt>http.proxyPort</tt>, and
     * <tt>http.proxyAuth</tt>. Generally speaking, the framework does not use system properties at all.</li>
     * <li><i><b>Read the framework's configuration property file.</b></i> This is a file containing properties used to
     * configure the framework instance and to pass configuration information into bundles installed into the framework
     * instance. The configuration property file is called <tt>config.properties</tt> by default and is located in the
     * <tt>conf/</tt> directory of the Felix installation directory, which is the parent directory of the directory
     * containing the <tt>felix.jar</tt> file. It is possible to use a different location for the property file by
     * specifying the desired URL using the <tt>felix.config.properties</tt> system property; this should be set using
     * the <tt>-D</tt> syntax when executing the JVM. If the <tt>config.properties</tt> file cannot be found, then
     * default values are used for all configuration properties. Refer to the <a href="Felix.html#Felix(java.util.Map)">
     * <tt>Felix</tt></a> constructor documentation for more information on framework configuration properties.</li>
     * <li><i><b>Copy configuration properties specified as system properties into the set of configuration
     * properties.</b></i> Even though the Felix framework does not consult system properties for configuration
     * information, sometimes it is convenient to specify them on the command line when launching Felix. To make this
     * possible, the Felix launcher copies any configuration properties specified as system properties into the set of
     * configuration properties passed into Felix.</li>
     * <li><i><b>Add shutdown hook.</b></i> To make sure the framework shutdowns cleanly, the launcher installs a
     * shutdown hook; this can be disabled with the <tt>felix.shutdown.hook</tt> configuration property.</li>
     * <li><i><b>Create and initialize a framework instance.</b></i> The OSGi standard <tt>FrameworkFactory</tt> is
     * retrieved from <tt>META-INF/services</tt> and used to create a framework instance with the configuration
     * properties.</li>
     * <li><i><b>Auto-deploy bundles.</b></i> All bundles in the auto-deploy directory are deployed into the framework
     * instance.</li>
     * <li><i><b>Start the framework.</b></i> The framework is started and the launcher thread waits for the framework
     * to shutdown.</li>
     * </ol>
     * <p>
     * It should be noted that simply starting an instance of the framework is not enough to create an interactive
     * session with it. It is necessary to install and start bundles that provide a some means to interact with the
     * framework; this is generally done by bundles in the auto-deploy directory or specifying an "auto-start" property
     * in the configuration property file. If no bundles providing a means to interact with the framework are installed
     * or if the configuration property file cannot be found, the framework will appear to be hung or deadlocked. This
     * is not the case, it is executing correctly, there is just no way to interact with it.
     * </p>
     * <p>
     * The launcher provides two ways to deploy bundles into a framework at startup, which have associated configuration
     * properties:
     * </p>
     * <ul>
     * <li>Bundle auto-deploy - Automatically deploys all bundles from a specified directory, controlled by the
     * following configuration properties:
     * <ul>
     * <li><tt>felix.auto.deploy.dir</tt> - Specifies the auto-deploy directory from which bundles are automatically
     * deploy at framework startup. The default is the <tt>bundle/</tt> directory of the current directory.</li>
     * <li><tt>felix.auto.deploy.action</tt> - Specifies the auto-deploy actions to be found on bundle JAR files found
     * in the auto-deploy directory. The possible actions are <tt>install</tt>, <tt>update</tt>, <tt>start</tt>, and
     * <tt>uninstall</tt>. If no actions are specified, then the auto-deploy directory is not processed. There is no
     * default value for this property.</li>
     * </ul>
     * </li>
     * <li>Bundle auto-properties - Configuration properties which specify URLs to bundles to install/start:
     * <ul>
     * <li><tt>felix.auto.install.N</tt> - Space-delimited list of bundle URLs to automatically install when the
     * framework is started, where <tt>N</tt> is the start level into which the bundle will be installed (e.g.,
     * felix.auto.install.2).</li>
     * <li><tt>felix.auto.start.N</tt> - Space-delimited list of bundle URLs to automatically install and start when the
     * framework is started, where <tt>N</tt> is the start level into which the bundle will be installed (e.g.,
     * felix.auto.start.2).</li>
     * </ul>
     * </li>
     * </ul>
     * <p>
     * These properties should be specified in the <tt>config.properties</tt> so that they can be processed by the
     * launcher during the framework startup process.
     * </p>
     * 
     * @param args
     *            Accepts arguments to set the auto-deploy directory and/or the bundle cache directory.
     * @throws Exception
     *             If an error occurs.
     **/
    public static void main(String[] argv) throws Exception {
        launch(argv);
    }

    public static void launch(String[] argv) throws Exception {
        // Set system property for dynamically loading only native libraries corresponding of the current platform
        setSystemSpecification();

        final List<StringBuffer> commandList = splitCommand(argv);
        // Look for bundle directory and/or cache directory.
        // We support at most one argument, which is the bundle
        // cache directory.
        String bundleDir = null;
        String cacheDir = null;
        for (StringBuffer c : commandList) {
            String command = c.toString();
            if (command.startsWith("felix")) { //$NON-NLS-1$
                String[] params = command.split(" "); //$NON-NLS-1$
                if (params.length < 3 || params.length > 4) {
                    System.err.println("Usage: [$felix -b <bundle-deploy-dir>] [<bundle-cache-dir>]"); //$NON-NLS-1$
                } else {
                    bundleDir = params[2];
                    if (params.length > 3) {
                        cacheDir = params[3];
                    }
                }
            }
        }

        // Load system properties.
        WeasisLauncher.loadSystemProperties();

        // Read configuration properties.
        Properties configProps = WeasisLauncher.loadConfigProperties();
        // If no configuration properties were found, then create
        // an empty properties object.
        if (configProps == null) {
            System.err.println("No " + CONFIG_PROPERTIES_FILE_VALUE + " found."); //$NON-NLS-1$ //$NON-NLS-2$
            configProps = new Properties();
        }

        // Copy framework properties from the system properties.
        WeasisLauncher.copySystemProperties(configProps);

        // If there is a passed in bundle auto-deploy directory, then
        // that overwrites anything in the config file.
        if (bundleDir != null) {
            configProps.setProperty(AutoProcessor.AUTO_DEPLOY_DIR_PROPERY, bundleDir);
        }

        // If there is a passed in bundle cache directory, then
        // that overwrites anything in the config file.
        if (cacheDir != null) {
            configProps.setProperty(Constants.FRAMEWORK_STORAGE, cacheDir);
        }

        // Load local properties and clean if necessary the previous version
        WebStartLoader loader = loadProperties(configProps);

        // If enabled, register a shutdown hook to make sure the framework is
        // cleanly shutdown when the VM exits.
        Runtime.getRuntime().addShutdownHook(new Thread("Felix Shutdown Hook") { //$NON-NLS-1$

                @Override
                public void run() {
                    int exitStatus = 0;
                    try {
                        if (m_felix != null) {
                            m_felix.stop();
                            // wait asynchronous stop (max 25 seconds)
                            m_felix.waitForStop(25000);
                        }
                    } catch (Exception ex) {
                        exitStatus = -1;
                        System.err.println("Error stopping framework: " + ex); //$NON-NLS-1$
                    } finally {
                        // Clean temp folder.
                        FileUtil.deleteDirectoryContents(FileUtil.getApplicationTempDir()); //$NON-NLS-1$
                        Runtime.getRuntime().halt(exitStatus);
                    }
                }
            });

        System.out.println("\nWeasis Starting..."); //$NON-NLS-1$
        System.out.println("========================\n"); //$NON-NLS-1$
        int exitStatus = 0;
        // Create host activator;
        m_activator = new HostActivator();

        List list = new ArrayList();
        list.add(m_activator);
        configProps.put(FelixConstants.SYSTEMBUNDLE_ACTIVATORS_PROP, list);

        try {
            // Now create an instance of the framework with our configuration properties.
            m_felix = new Felix(configProps);
            // Initialize the framework, but don't start it yet.
            m_felix.init();

            // Use the system bundle context to process the auto-deploy
            // and auto-install/auto-start properties.
            loader.setFelix(configProps, m_activator.getBundleContext());
            loader.writeLabel("Starting... Weasis"); //$NON-NLS-1$
            m_tracker =
                new ServiceTracker(m_activator.getBundleContext(), "org.apache.felix.service.command.CommandProcessor", //$NON-NLS-1$
                    null);
            m_tracker.open();

            // Start the framework.
            m_felix.start();

            // End of splash screen
            loader.close();
            loader = null;
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    Object commandSession = getCommandSession(m_tracker.getService());
                    if (commandSession != null) {
                        // execute the commands from main argv
                        for (StringBuffer command : commandList) {
                            commandSession_execute(commandSession, command);
                        }
                        commandSession_close(commandSession);
                    }

                    m_tracker.close();
                }
            });

            boolean uiStarted = false;

            for (Bundle b : m_felix.getBundleContext().getBundles()) {
                if (b.getSymbolicName().equals("weasis-base-ui")) { //$NON-NLS-1$
                    uiStarted = true;
                    break;
                }
            }
            // TODO Handle Weasis version without ui
            if (!uiStarted) {
                throw new Exception("Main User Interface bundle cannot be started"); //$NON-NLS-1$
            }
            // Wait for framework to stop to exit the VM.
            m_felix.waitForStop(0);
            System.exit(0);

        } catch (Exception ex) {
            exitStatus = -1;
            System.err.println("Could not create framework: " + ex); //$NON-NLS-1$
            ex.printStackTrace();
        } finally {
            Runtime.getRuntime().halt(exitStatus);
        }
    }

    public Bundle[] getInstalledBundles() {
        // Use the system bundle activator to gain external
        // access to the set of installed bundles.
        return m_activator.getBundles();
    }

    public static List<StringBuffer> splitCommand(String[] args) {
        int length = args.length;
        ArrayList<StringBuffer> list = new ArrayList<StringBuffer>(5);
        for (int i = 0; i < length; i++) {
            if (args[i].startsWith("$") && args[i].length() > 1) { //$NON-NLS-1$
                StringBuffer command = new StringBuffer(args[i].substring(1));
                // look for parameters
                while (i + 1 < length && !args[i + 1].startsWith("$")) { //$NON-NLS-1$
                    i++;
                    command.append(" "); //$NON-NLS-1$
                    if (args[i].indexOf(" ") != -1) { //$NON-NLS-1$
                        command.append("\""); //$NON-NLS-1$
                        command.append(args[i]);
                        command.append("\""); //$NON-NLS-1$
                    } else {
                        command.append(args[i]);
                    }
                }
                list.add(command);
            }
        }
        // System.out.println("Arguments:" + result.toString());
        return list;
    }

    public static Object getCommandSession(Object commandProcessor) {
        if (commandProcessor == null) {
            return null;
        }
        Class[] parameterTypes = new Class[] { InputStream.class, PrintStream.class, PrintStream.class };

        Object[] arguments = new Object[] { System.in, System.out, System.err };

        try {
            Method nameMethod = commandProcessor.getClass().getMethod("createSession", parameterTypes); //$NON-NLS-1$
            Object commandSession = nameMethod.invoke(commandProcessor, arguments);
            return commandSession;
        } catch (Exception ex) {
            // Since the services returned by the tracker could become
            // invalid at any moment, we will catch all exceptions, log
            // a message, and then ignore faulty services.
            System.err.println(ex);
        }

        return null;
    }

    public static boolean commandSession_close(Object commandSession) {
        if (commandSession == null) {
            return false;
        }
        try {
            Method nameMethod = commandSession.getClass().getMethod("close", null); //$NON-NLS-1$
            nameMethod.invoke(commandSession, null);
            return true;
        } catch (Exception ex) {
            // Since the services returned by the tracker could become
            // invalid at any moment, we will catch all exceptions, log
            // a message, and then ignore faulty services.
            System.err.println(ex);
        }

        return false;
    }

    public static boolean commandSession_execute(Object commandSession, CharSequence charSequence) {
        if (commandSession == null) {
            return false;
        }
        Class[] parameterTypes = new Class[] { CharSequence.class };

        Object[] arguments = new Object[] { charSequence };

        try {
            Method nameMethod = commandSession.getClass().getMethod("execute", parameterTypes); //$NON-NLS-1$
            nameMethod.invoke(commandSession, arguments);
            return true;
        } catch (Exception ex) {
            // Since the services returned by the tracker could become
            // invalid at any moment, we will catch all exceptions, log
            // a message, and then ignore faulty services.
            System.err.println(ex);
            ex.printStackTrace();
        }

        return false;
    }

    /**
     * This following part has been copied from the Main class of the Felix project
     * 
     **/

    /**
     * <p>
     * Loads the properties in the system property file associated with the framework installation into
     * <tt>System.setProperty()</tt>. These properties are not directly used by the framework in anyway. By default, the
     * system property file is located in the <tt>conf/</tt> directory of the Felix installation directory and is called
     * "<tt>system.properties</tt>". The installation directory of Felix is assumed to be the parent directory of the
     * <tt>felix.jar</tt> file as found on the system class path property. The precise file from which to load system
     * properties can be set by initializing the "<tt>felix.system.properties</tt>" system property to an arbitrary URL.
     * </p>
     **/
    public static void loadSystemProperties() {
        // The system properties file is either specified by a system
        // property or it is in the same directory as the Felix JAR file.
        // Try to load it from one of these places.

        // See if the property URL was specified as a property.
        URL propURL = null;
        String custom = System.getProperty(SYSTEM_PROPERTIES_PROP);
        if (custom != null) {
            try {
                propURL = new URL(custom);
            } catch (MalformedURLException ex) {
                System.err.print("Main: " + ex); //$NON-NLS-1$
                return;
            }
        } else {
            // Determine where the configuration directory is by figuring
            // out where felix.jar is located on the system class path.
            File confDir = null;
            String classpath = System.getProperty("java.class.path"); //$NON-NLS-1$
            int index = classpath.toLowerCase().indexOf("felix.jar"); //$NON-NLS-1$
            int start = classpath.lastIndexOf(File.pathSeparator, index) + 1;
            if (index >= start) {
                // Get the path of the felix.jar file.
                String jarLocation = classpath.substring(start, index);
                // Calculate the conf directory based on the parent
                // directory of the felix.jar directory.
                confDir = new File(new File(new File(jarLocation).getAbsolutePath()).getParent(), CONFIG_DIRECTORY);
            } else {
                // Can't figure it out so use the current directory as default.
                confDir = new File(System.getProperty("user.dir"), CONFIG_DIRECTORY); //$NON-NLS-1$
            }

            try {
                propURL = new File(confDir, SYSTEM_PROPERTIES_FILE_VALUE).toURL();
            } catch (MalformedURLException ex) {
                System.err.print("Main: " + ex); //$NON-NLS-1$
                return;
            }
        }

        // Read the properties file.
        Properties props = new Properties();
        InputStream is = null;
        try {
            is = propURL.openConnection().getInputStream();
            props.load(is);
            is.close();
        } catch (FileNotFoundException ex) {
            // Ignore file not found.
        } catch (Exception ex) {
            System.err.println("Main: Error loading system properties from " + propURL); //$NON-NLS-1$
            System.err.println("Main: " + ex); //$NON-NLS-1$
            FileUtil.safeClose(is);
            return;
        }

        // Perform variable substitution on specified properties.
        for (Enumeration e = props.propertyNames(); e.hasMoreElements();) {
            String name = (String) e.nextElement();
            System.setProperty(name, Util.substVars(props.getProperty(name), name, null, null));
        }
    }

    /**
     * <p>
     * Loads the configuration properties in the configuration property file associated with the framework installation;
     * these properties are accessible to the framework and to bundles and are intended for configuration purposes. By
     * default, the configuration property file is located in the <tt>conf/</tt> directory of the Felix installation
     * directory and is called "<tt>config.properties</tt>". The installation directory of Felix is assumed to be the
     * parent directory of the <tt>felix.jar</tt> file as found on the system class path property. The precise file from
     * which to load configuration properties can be set by initializing the "<tt>felix.config.properties</tt>" system
     * property to an arbitrary URL.
     * </p>
     * 
     * @return A <tt>Properties</tt> instance or <tt>null</tt> if there was an error.
     **/
    public static Properties loadConfigProperties() {
        // The config properties file is either specified by a system
        // property or it is in the conf/ directory of the Felix
        // installation directory. Try to load it from one of these
        // places.

        // See if the property URL was specified as a property.
        URL propURL = null;
        String custom = System.getProperty(CONFIG_PROPERTIES_PROP);
        if (custom != null) {
            try {
                propURL = new URL(custom);
            } catch (MalformedURLException ex) {
                System.err.print("Main: " + ex); //$NON-NLS-1$
                return null;
            }
        } else {
            // Determine where the configuration directory is by figuring
            // out where felix.jar is located on the system class path.
            File confDir = null;
            String classpath = System.getProperty("java.class.path"); //$NON-NLS-1$
            int index = classpath.toLowerCase().indexOf("felix.jar"); //$NON-NLS-1$
            int start = classpath.lastIndexOf(File.pathSeparator, index) + 1;
            if (index >= start) {
                // Get the path of the felix.jar file.
                String jarLocation = classpath.substring(start, index);
                // Calculate the conf directory based on the parent
                // directory of the felix.jar directory.
                confDir = new File(new File(new File(jarLocation).getAbsolutePath()).getParent(), CONFIG_DIRECTORY);
            } else {
                // Can't figure it out so use the current directory as default.
                confDir = new File(System.getProperty("user.dir"), CONFIG_DIRECTORY); //$NON-NLS-1$
            }

            try {
                propURL = new File(confDir, CONFIG_PROPERTIES_FILE_VALUE).toURL();
            } catch (MalformedURLException ex) {
                System.err.print("Main: " + ex); //$NON-NLS-1$
                return null;
            }
        }

        // Read the properties file.
        Properties props = readProperties(propURL);

        if (props != null) {
            // Perform variable substitution for system properties.
            for (Enumeration e = props.propertyNames(); e.hasMoreElements();) {
                String name = (String) e.nextElement();
                props.setProperty(name, Util.substVars(props.getProperty(name), name, null, props));
            }
        }
        return props;
    }

    public static Properties readProperties(URL propURL) {
        // Read the properties file.
        Properties props = new Properties();
        InputStream is = null;
        try {
            // Try to load config.properties.
            is = propURL.openConnection().getInputStream();
            props.load(is);
            is.close();
        } catch (Exception ex) {
            FileUtil.safeClose(is);
            return null;
        }
        return props;
    }

    public static void copySystemProperties(Properties configProps) {
        for (Enumeration e = System.getProperties().propertyNames(); e.hasMoreElements();) {
            String key = (String) e.nextElement();
            if (key.startsWith("felix.") || key.startsWith("org.osgi.framework.")) { //$NON-NLS-1$ //$NON-NLS-2$
                configProps.setProperty(key, System.getProperty(key));
            }
        }
    }

    public static void setSystemSpecification() {
        // Follows the OSGI specification to use Bundle-NativeCode in the bundle fragment :
        // http://www.osgi.org/Specifications/Reference
        String osName = System.getProperty("os.name"); //$NON-NLS-1$
        String osArch = System.getProperty("os.arch"); //$NON-NLS-1$
        if (osName != null && !osName.trim().equals("") && osArch != null && !osArch.trim().equals("")) { //$NON-NLS-1$ //$NON-NLS-2$
            if (osName.startsWith("Win")) { //$NON-NLS-1$
                // All Windows versions with a specific processor architecture (x86 or x86-64) are grouped under
                // windows. If you need to make different native libraries for the Windows versions, define it in the
                // Bundle-NativeCode tag of the bundle fragment.
                osName = "windows"; //$NON-NLS-1$
            } else if (osName.equals("Mac OS X")) { //$NON-NLS-1$
                osName = "macosx"; //$NON-NLS-1$
            } else if (osName.equals("SymbianOS")) { //$NON-NLS-1$
                osName = "epoc32"; //$NON-NLS-1$
            } else if (osName.equals("hp-ux")) { //$NON-NLS-1$
                osName = "hpux"; //$NON-NLS-1$
            } else if (osName.equals("Mac OS")) { //$NON-NLS-1$
                osName = "macos"; //$NON-NLS-1$
            } else if (osName.equals("OS/2")) { //$NON-NLS-1$
                osName = "os2"; //$NON-NLS-1$
            } else if (osName.equals("procnto")) { //$NON-NLS-1$
                osName = "qnx"; //$NON-NLS-1$
            } else {
                osName = osName.toLowerCase();
            }

            if (osArch.equals("pentium") || osArch.equals("i386") || osArch.equals("i486") || osArch.equals("i586") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                || osArch.equals("i686")) { //$NON-NLS-1$
                osArch = "x86"; //$NON-NLS-1$
            } else if (osArch.equals("amd64") || osArch.equals("em64t") || osArch.equals("x86_64")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                osArch = "x86-64"; //$NON-NLS-1$
            } else if (osArch.equals("power ppc")) { //$NON-NLS-1$
                osArch = "powerpc"; //$NON-NLS-1$
            } else if (osArch.equals("psc1k")) { //$NON-NLS-1$
                osArch = "ignite"; //$NON-NLS-1$
            } else {
                osArch = osArch.toLowerCase();
            }
            System.setProperty("native.library.spec", osName + "-" + osArch); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    public static WebStartLoader loadProperties(Properties config) {
        String dir = new File(config.getProperty(Constants.FRAMEWORK_STORAGE)).getParent();
        System.setProperty(P_WEASIS_PATH, dir);

        String user = System.getProperty("weasis.user", null); //$NON-NLS-1$
        File basdir;
        if (user == null) {
            basdir = new File(dir); //$NON-NLS-1$
        } else {
            basdir = new File(dir + File.separator + "preferences" + File.separator //$NON-NLS-1$
                + user);
            try {
                basdir.mkdirs();
            } catch (Exception e) {
                basdir = new File(dir);
                e.printStackTrace();
            }
        }
        File common_file = new File(basdir, APP_PROPERTY_FILE);
        Properties s_prop = readProperties(common_file);

        // Get locale from system properties otherwise set en_US (only for the first launch of Weasis on a user session)
        String lang = System.getProperty("weasis.language", "en"); //$NON-NLS-1$ //$NON-NLS-2$
        String country = System.getProperty("weasis.country", "US"); //$NON-NLS-1$ //$NON-NLS-2$
        String variant = System.getProperty("weasis.variant", ""); //$NON-NLS-1$ //$NON-NLS-2$
        // Set the locale of the previous launch if exists
        lang = s_prop.getProperty("locale.language", lang); //$NON-NLS-1$
        if (!lang.equals("en")) {
            String translation_modules = System.getProperty("weasis.i18n", null);
            if (translation_modules != null) {

                try {
                    translation_modules +=
                        translation_modules.endsWith("/") ? "buildNumber.properties" : "/buildNumber.properties";
                    modulesi18n = readProperties(new URL(translation_modules));
                } catch (MalformedURLException ex) {
                    System.err.print("Cannot find translation modules: " + ex); //$NON-NLS-1$
                }
            }
        }
        country = s_prop.getProperty("locale.country", country); //$NON-NLS-1$ 
        variant = s_prop.getProperty("locale.variant", variant); //$NON-NLS-1$ 
        Locale.setDefault(new Locale(lang, country, variant));

        boolean update = false;

        look = System.getProperty("swing.defaultlaf", null); //$NON-NLS-1$
        if (look == null) {
            look = s_prop.getProperty("weasis.look", null); //$NON-NLS-1$
        }
        if (look == null) {
            String sys_spec = System.getProperty("native.library.spec", "unknown"); //$NON-NLS-1$ //$NON-NLS-2$
            int index = sys_spec.indexOf("-"); //$NON-NLS-1$
            if (index > 0) {
                String sys = sys_spec.substring(0, index);
                look = config.getProperty("weasis.look." + sys, null); //$NON-NLS-1$
            }
            if (look == null) {
                look = UIManager.getSystemLookAndFeelClassName();
            }
        }

        String versionNew = config.getProperty("weasis.version"); //$NON-NLS-1$

        // Force changing Look and Feel when upgrade version
        if (LookAndFeels.installSubstanceLookAndFeels() && versionNew != null && versionNew.startsWith("1.0.8")) {
            look = "org.pushingpixels.substance.api.skin.SubstanceTwilightLookAndFeel";
        }

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                // Set look and feels after downloading plug-ins (allows installing Substance and other lafs)
                JFrame.setDefaultLookAndFeelDecorated(true);
                JDialog.setDefaultLookAndFeelDecorated(true);
                look = setLookAndFeel(look);
            }
        });

        // Splash screen that shows bundles loading
        final WebStartLoader loader = new WebStartLoader();
        // Display splash screen
        loader.open();

        Properties common_prop;
        if (basdir.getPath().equals(dir)) {
            common_prop = s_prop;
        } else {
            common_file = new File(dir, APP_PROPERTY_FILE);
            common_prop = readProperties(common_file);
        }

        String versionOld = common_prop.getProperty("weasis.version"); //$NON-NLS-1$

        if (versionNew != null) {
            // Add also to java properties for the about
            System.setProperty(P_WEASIS_VERSION, versionNew);
            common_prop.put(P_WEASIS_VERSION, versionNew);
            if (versionOld == null || !versionOld.equals(versionNew)) {
                update = true;
            }
        }
        if (update) {
            common_prop.put("weasis.look", look); //$NON-NLS-1$
            FileOutputStream fout = null;
            try {
                fout = new FileOutputStream(common_file);
                common_prop.store(fout, null);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                FileUtil.safeClose(fout);
            }
        }

        boolean cleanCache = Boolean.parseBoolean(config.getProperty("weasis.clean.previous.version")); //$NON-NLS-1$
        // Save if not exist or could not be read
        if (cleanCache && versionNew != null) {
            if (!versionNew.equals(versionOld)) {
                System.out.printf("Clean previous Weasis version: %s \n", versionOld); //$NON-NLS-1$
                config.setProperty(Constants.FRAMEWORK_STORAGE_CLEAN, Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);
            }
        }
        final File file = common_file;
        // Test if it is the first time launch
        if (versionOld == null) {
            EventQueue.invokeLater(new Runnable() {

                public void run() {
                    Object[] options =
                        { Messages.getString("WeasisLauncher.ok"), Messages.getString("WeasisLauncher.no") }; //$NON-NLS-1$ //$NON-NLS-2$

                    int response =
                        JOptionPane.showOptionDialog(
                            loader.getWindow(),
                            Messages.getString("WeasisLauncher.msg"), //$NON-NLS-1$
                            Messages.getString("WeasisLauncher.first"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, //$NON-NLS-1$
                            null, options, null);
                    if (response == 1) {
                        // delete the properties file to ask again
                        file.delete();
                        System.err.println("Refusing the disclaimer"); //$NON-NLS-1$
                        System.exit(-1);
                    }
                }
            });
        } else if (versionNew != null && !versionNew.equals(versionOld)) {
            final String str = Messages.getString("WeasisLauncher.news");
            if (!"".equals(str.trim())) {
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        JTextPane jTextPane1 = new JTextPane();
                        jTextPane1.setContentType("text/html"); //$NON-NLS-1$
                        jTextPane1.setEditable(false);
                        jTextPane1.setBackground(Color.WHITE);
                        StyleSheet ss = ((HTMLEditorKit) jTextPane1.getEditorKit()).getStyleSheet();
                        ss.addRule("p {font-size:12}");
                        jTextPane1.setText(str);
                        jTextPane1.setPreferredSize(new Dimension(550, 300));
                        JScrollPane sp = new JScrollPane(jTextPane1);
                        JOptionPane.showMessageDialog(loader.getWindow(), sp, "News", JOptionPane.PLAIN_MESSAGE);
                    }
                });
            }
        }
        return loader;
    }

    private static Properties readProperties(File propsFile) {
        Properties properties = new Properties();

        if (propsFile.canRead()) {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(propsFile);
                properties.load(fis);

            } catch (Throwable t) {
            } finally {
                try {
                    if (fis != null) {
                        fis.close();
                    }
                } catch (IOException e) {
                }
            }
        } else {
            File appFoler = new File(System.getProperty(P_WEASIS_PATH, "")); //$NON-NLS-1$
            appFoler.mkdirs();
        }
        return properties;
    }

    /**
     * Changes the look and feel for the whole GUI
     */

    public static String setLookAndFeel(String look) {
        // Do not display metal LAF in bold, it is ugly
        UIManager.put("swing.boldMetal", Boolean.FALSE); //$NON-NLS-1$
        // Display slider value is set to false (already in all LAF by the panel title), used by GTK LAF
        UIManager.put("Slider.paintValue", Boolean.FALSE); //$NON-NLS-1$
        UIManager.LookAndFeelInfo lafs[] = UIManager.getInstalledLookAndFeels();
        laf_exist: if (look != null) {
            for (int i = 0, n = lafs.length; i < n; i++) {
                if (lafs[i].getClassName().equals(look)) { //$NON-NLS-1$
                    break laf_exist;
                }
            }
            look = null;
        }
        if (look == null) {
            // Try to set Nimbus, concurrent thread issue
            // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6785663
            for (int i = 0, n = lafs.length; i < n; i++) {
                if (lafs[i].getName().equals("Nimbus")) { //$NON-NLS-1$
                    look = lafs[i].getClassName();
                    break;
                }
            }
            // Should never happen
            if (look == null) {
                look = UIManager.getSystemLookAndFeelClassName();
            }

        }

        if (look != null) {
            try {
                UIManager.setLookAndFeel(look);

            } catch (Exception e) {
                System.err.println("WARNING : Unable to set the Look&Feel"); //$NON-NLS-1$
                e.printStackTrace();
            }
        }
        return look;
    }
}
