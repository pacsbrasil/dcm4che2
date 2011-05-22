/*******************************************************************************
 * Copyright (c) 2010 Weasis Team.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Nicolas Roduit - initial API and implementation
 ******************************************************************************/

package org.weasis.servlet;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.dcm4che.dicom.DicomNode;
import org.weasis.dcm4che.dicom.Manifest;
import org.weasis.launcher.wado.Patient;
import org.weasis.launcher.wado.WadoParameters;
import org.weasis.launcher.wado.WadoQuery;
import org.weasis.launcher.wado.WadoQueryException;
import org.weasis.launcher.wado.xml.FileUtil;

public class Weasis_Launcher extends HttpServlet {
    private static final long serialVersionUID = 8946852726380985736L;
    /**
     * Logger for this class
     */
    private static final Logger logger = LoggerFactory.getLogger(Weasis_Launcher.class);

    static final String DEFAULT_JNLP_TEMPLATE_NAME = "launcher.jnlp";
    static final String JNLP_EXTENSION = ".jnlp";
    static final String JNLP_MIME_TYPE = "application/x-java-jnlp-file";

    static final String PatientID = "patientID";
    static final String StudyUID = "studyUID";
    static final String SeriesUID = "seriesUID";
    static final String ObjectUID = "objectUID";

    static final Properties pacsProperties = new Properties();

    /**
     * Constructor of the object.
     */
    public Weasis_Launcher() {
        super();
    }

    /**
     * Initialization of the servlet. <br>
     * 
     * @throws ServletException
     *             if an error occurs
     */
    @Override
    public void init() throws ServletException {
        logger.debug("init() - getServletContext : {} ", getServletConfig().getServletContext());
        logger.debug("init() - getRealPath : {}", getServletConfig().getServletContext().getRealPath("/"));
        try {
            URL config = this.getClass().getResource("/weasis-pacs-connector.properties");
            if (config == null) {
                config = this.getClass().getResource("/weasis-connector-default.properties");
                logger.info("Default configuration file : {}", config);
            } else {
                logger.info("External configuration file : {}", config);
            }
            if (config != null) {
                pacsProperties.load(config.openStream());
            } else {
                logger.error("Cannot find  a configuration file for weasis-pacs-connector");
            }
            URL jnlpTemplate = this.getClass().getResource("/weasis-jnlp.xml");
            if (jnlpTemplate == null) {
                jnlpTemplate = this.getClass().getResource("/weasis-jnlp-default.xml");
                logger.info("Default  Weasis template  : {}", jnlpTemplate);
            } else {
                logger.info("External Weasis template : {}", jnlpTemplate);
            }
            if (jnlpTemplate == null) {
                logger.error("Cannot find  JNLP template");
            } else {
                pacsProperties.put("weasis.jnlp", jnlpTemplate.toString());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * The doGet method of the servlet. <br>
     * 
     * This method is called when a form has its tag value method equals to get.
     * 
     * @param request
     *            the request send by the client to the server
     * @param response
     *            the response send by the server to the client
     * @throws ServletErrorException
     * @throws IOException
     * @throws ServletException
     * @throws ServletException
     *             if an error occurred
     * @throws IOException
     *             if an error occurred
     */

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Test if this client is allowed
        String hosts = pacsProperties.getProperty("hosts.allow");
        if (hosts != null && !hosts.trim().equals("")) {
            String clintHost = request.getRemoteHost();
            String clientIP = request.getRemoteAddr();
            boolean accept = false;
            for (String host : hosts.split(",")) {
                if (host.equals(clintHost) || host.equals(clientIP)) {
                    accept = true;
                    break;
                }
            }
            if (!accept) {
                logger.warn("The request from {} is not allowed.", clintHost);
                return;
            }
        }
        try {
            logRequestInfo(request);
            response.setContentType(JNLP_MIME_TYPE);

            String baseURL = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
            System.setProperty("server.base.url", baseURL);

            // Perform variable substitution for system properties.
            for (Enumeration e = pacsProperties.propertyNames(); e.hasMoreElements();) {
                String name = (String) e.nextElement();
                pacsProperties.setProperty(name,
                    FileUtil.substVars(pacsProperties.getProperty(name), name, null, pacsProperties));
            }

            String wadoQueriesURL = pacsProperties.getProperty("pacs.wado.url", "http://localhost:8080/wado");
            String pacsAET = pacsProperties.getProperty("pacs.aet", "DCM4CHEE");
            String pacsHost = pacsProperties.getProperty("pacs.host", "localhost");
            int pacsPort = Integer.parseInt(pacsProperties.getProperty("pacs.port", "11112"));
            DicomNode dicomSource = new DicomNode(pacsAET, pacsHost, pacsPort);
            String componentAET = pacsProperties.getProperty("aet", "WEASIS");
            List<Patient> patients = getPatientList(request, dicomSource, componentAET);

            String wadoQueryFile = "";

            if (patients == null || patients.size() < 1) {
                logger.warn("No data has been found!");
                response.sendError(HttpServletResponse.SC_NO_CONTENT, "No data has been found!");
                return;
            }
            try {
                // If the web server requires an authentication (pacs.web.login=user:pwd)
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
                ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                WadoQuery.gzipCompress(new ByteArrayInputStream(wadoQuery.toString().getBytes()), outStream);
                wadoQueryFile = Base64.encodeBase64String(outStream.toByteArray());

            } catch (WadoQueryException e) {
                logger.error(e.getMessage());
                return;
            }

            InputStream is = null;
            try {
                URL jnlpTemplate = new URL((String) pacsProperties.get("weasis.jnlp"));
                is = jnlpTemplate.openStream();
                BufferedReader dis = new BufferedReader(new InputStreamReader(is));
                // response.setContentLength(launcherStr.length());
                String weasisBaseURL = pacsProperties.getProperty("weasis.base.url", baseURL);

                PrintWriter outWriter = response.getWriter();
                String s;
                while ((s = dis.readLine()) != null) {
                    if (s.trim().equals("</resources>")) {
                        outWriter.println(s);
                        outWriter.println("\t<application-desc main-class=\"org.weasis.launcher.WebstartLauncher\">");
                        outWriter.print("\t\t<argument>$dicom:get -i ");
                        outWriter.print(wadoQueryFile);
                        outWriter.println("</argument>");
                        outWriter.println("\t</application-desc>");
                    } else {
                        s = s.replace("${weasis.base.url}", weasisBaseURL);
                        outWriter.println(s);
                    }
                }
                outWriter.close();

            } catch (MalformedURLException mue) {
                mue.printStackTrace();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            } finally {
                try {
                    is.close();
                } catch (IOException ioe) {
                    // just going to ignore this
                }

            }
        } catch (Exception e) {
            logger.error("doGet(HttpServletRequest, HttpServletResponse)", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * The doPost method of the servlet. <br>
     * 
     * This method is called when a form has its tag value method equals to post.
     * 
     * @param request
     *            the request send by the client to the server
     * @param response
     *            the response send by the server to the client
     * @throws ServletException
     *             if an error occurred
     * @throws IOException
     *             if an error occurred
     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doGet(request, response);
    }

    @Override
    protected void doHead(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            response.setContentType(JNLP_MIME_TYPE);

        } catch (Exception e) {
            logger.error("doHead(HttpServletRequest, HttpServletResponse)", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Destruction of the servlet. <br>
     */
    @Override
    public void destroy() {
        super.destroy();
    }

    /**
     * @param request
     */
    protected void logRequestInfo(HttpServletRequest request) {
        logger.debug("logRequestInfo(HttpServletRequest) - getRequestQueryURL : {}{}", request.getRequestURL()
            .toString(), request.getQueryString() != null ? ("?" + request.getQueryString().trim()) : "");
        logger.debug("logRequestInfo(HttpServletRequest) - getContextPath : {}", request.getContextPath());
        logger.debug("logRequestInfo(HttpServletRequest) - getRequestURI : {}", request.getRequestURI());
        logger.debug("logRequestInfo(HttpServletRequest) - getServletPath : {}", request.getServletPath());
    }

    private List<Patient> getPatientList(HttpServletRequest request, DicomNode dicomSource, String componentAET) {
        String pat = request.getParameter(PatientID);
        String stu = request.getParameter(StudyUID);
        String ser = request.getParameter(SeriesUID);
        String obj = request.getParameter(ObjectUID);
        List<Patient> patients = null;
        try {
            if (obj != null) {
                patients = Manifest.buildFromSopInstanceUID(dicomSource, componentAET, obj);
            } else if (ser != null) {
                patients = Manifest.buildFromSeriesInstanceUID(dicomSource, componentAET, ser);
            } else if (stu != null) {
                patients = Manifest.buildFromStudyInstanceUID(dicomSource, componentAET, stu);
            } else if (pat != null) {
                patients = Manifest.buildFromPatientID(dicomSource, componentAET, pat);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return patients;
    }
}
