package org.weasis.launcher;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.infohazard.maverick.flow.ControllerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.launcher.jnlp.ApplicationDesc;
import org.weasis.launcher.jnlp.Jnlp;
import org.weasis.launcher.jnlp.JnlpBuilder;
import org.weasis.launcher.jnlp.JnlpException;
import org.weasis.launcher.wado.Patient;
import org.weasis.launcher.wado.WadoParameters;
import org.weasis.launcher.wado.WadoQuery;
import org.weasis.launcher.wado.WadoQueryException;
import org.weasis.launcher.wado.xml.FileUtil;

public class WeasisLauncher {

    private static Logger logger = LoggerFactory.getLogger(WeasisLauncher.class);

    private final static Properties pacsProperties = new Properties();

    static {
        try {
            pacsProperties
                .load(WeasisLauncher.class.getClassLoader().getResourceAsStream("viewer/launcher.properties"));
            pacsProperties.put("profile.name", "dcm4chee");
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    private final String scheme;

    private final String serverName;

    private final String serverPort;

    public WeasisLauncher(ControllerContext controllerContext) {
        HttpServletRequest request;
        if (controllerContext == null || (request = controllerContext.getRequest()) == null) {
            throw new IllegalArgumentException("server parameter cannot be null");
        }

        this.scheme = request.getScheme();
        this.serverName = request.getServerName();
        this.serverPort = "" + request.getServerPort();

        System.setProperty("server.base.url", scheme + "://" + serverName + ":" + serverPort);
        for (Enumeration e = pacsProperties.propertyNames(); e.hasMoreElements();) {
            String name = (String) e.nextElement();
            pacsProperties.setProperty(name,
                FileUtil.substVars(pacsProperties.getProperty(name), name, null, pacsProperties));
        }

        FileOutputStream jnlpStream = null;
        try {
            // Build extension jnlp file for substance Look and Feel.
            String jnlpTmpFilePath = pacsProperties.getProperty("jnlp.tmp.path");
            String fileExt = "/extensions/substance.jnlp";
            File jnlpTmpFile = new File(controllerContext.getServletContext().getRealPath(fileExt));
            jnlpTmpFile.getParentFile().mkdirs();

            jnlpStream = new FileOutputStream(jnlpTmpFile);
            String jnlpContent = pacsProperties.getProperty("jnlp.substance.extension");
            jnlpStream.write(jnlpContent.getBytes());
            jnlpStream.flush();

        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            try {
                if (jnlpStream != null) {
                    jnlpStream.close();
                }
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        }
    }

    public void displayImages(ArrayList<Patient> patients, ControllerContext controllerContext) {
        String jnlpFile = null;
        String wadoQueryFile = "";
        String launcherContextPath = controllerContext.getRequest().getContextPath();
        // Creates the Wado Query and save it to a temporary file
        if (patients.size() > 0) {
            try {
                String wadoQueriesURL = pacsProperties.getProperty("pacs.wado.url");

                // If the web server requires a login for accessing to http (pacs.web.login=user:pwd)
                String webLogin = pacsProperties.getProperty("pacs.web.login", null);
                if (webLogin != null) {

                    webLogin =
                        new String(org.apache.commons.codec.binary.Base64.encodeBase64(webLogin.trim().getBytes()));
                }
                boolean onlysopuid = Boolean.valueOf(pacsProperties.getProperty("wado.onlysopuid"));
                String addparams = pacsProperties.getProperty("wado.addparams", "");
                String overrideTags = pacsProperties.getProperty("wado.override.tags", null);

                WadoParameters wado = new WadoParameters(wadoQueriesURL, onlysopuid, addparams, overrideTags, webLogin);
                WadoQuery wadoQuery =
                    new WadoQuery(patients, wado, pacsProperties.getProperty("pacs.db.encoding", "utf-8"));
                String wadoTmpFilePath = pacsProperties.getProperty("wado.xml.tmp.path");

                String wadoQueryFilePath = controllerContext.getServletContext().getRealPath("/" + wadoTmpFilePath);
                wadoQueryFile = wadoQuery.saveToTmpFile(wadoQueryFilePath);
                wadoQueryFile =
                    scheme + "://" + serverName + ":" + serverPort + launcherContextPath + "/" + wadoTmpFilePath + "/"
                        + wadoQueryFile;
            } catch (WadoQueryException e) {
                logger.error(e.getMessage());
                return;
            }
        }

        // Customizes the Jnlp File and save it to a temporary file
        Jnlp jnlp = JnlpBuilder.createJnlpFromProperties(pacsProperties);
        ApplicationDesc appDesk = (ApplicationDesc) jnlp.getDesc();
        appDesk.addApplicationArg("$dicom:get -w " + wadoQueryFile);
        logger.debug("jnlp: " + jnlp.toXml());

        try {
            // pacsProperties.put("weasis.user", request.getLogin());
            // pacsProperties.put("preferences.server.url", launcherURL +
            // "preferences");
            String jnlpTmpFilePath = pacsProperties.getProperty("jnlp.tmp.path");

            String jnlpFilePath = controllerContext.getServletContext().getRealPath("/" + jnlpTmpFilePath);
            jnlpFile = jnlp.saveToTmpFile(jnlpFilePath);
            jnlpFile =
                scheme + "://" + serverName + ":" + serverPort + launcherContextPath + "/" + jnlpTmpFilePath + "/"
                    + jnlpFile;
            // logger.debug("jnlp File = " + jnlpFile);
        } catch (JnlpException e) {
            logger.error(e.getMessage());
        }

        if (jnlpFile != null) {
            // Creates the response returned to the HTTP client
            HttpServletResponse response = controllerContext.getResponse();
            response.setContentType("text/html");
            try {
                response.sendRedirect(jnlpFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String getServerName() {
        return serverName;
    }

    public String getServerPort() {
        return serverPort;
    }

    public String getScheme() {
        return scheme;
    }

    public static String getWadoCompressionProfile(String modality) {
        int mode = 1;
        try {
            mode = Integer.parseInt(pacsProperties.getProperty("wado.compression.mode", "1"));
        } catch (NumberFormatException e) {
            logger.error("Invalid compression mode value:" + pacsProperties.getProperty("wado.compression.mode"));
        }
        String wadotTsuid =
            pacsProperties.getProperty(mode + "." + modality, pacsProperties.getProperty(mode + ".DEFAULT", null));
        return wadotTsuid;
    }
}
