/*
 *  *
 *  Copyright (c) 2002 by TIANI MEDGRAPH AG                                  *
 *  *
 *  This file is part of dcm4che.                                            *
 *  *
 *  This library is free software; you can redistribute it and/or modify it  *
 *  under the terms of the GNU Lesser General Public License as published    *
 *  by the Free Software Foundation; either version 2 of the License, or     *
 *  (at your option) any later version.                                      *
 *  *
 *  This library is distributed in the hope that it will be useful, but      *
 *  WITHOUT ANY WARRANTY; without even the implied warranty of               *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU        *
 *  Lesser General Public License for more details.                          *
 *  *
 *  You should have received a copy of the GNU Lesser General Public         *
 *  License along with this library; if not, write to the Free Software      *
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA  *
 */
package com.tiani.prnscp.scp;

import com.tiani.license.LicenseStore;
import com.tiani.prnscp.print.PrinterServiceMBean;
import com.tiani.prnscp.print.PrinterStatus;
import com.tiani.prnscp.print.PrinterStatusInfo;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import org.dcm4che.auditlog.AuditLogger;
import org.dcm4che.auditlog.AuditLoggerFactory;
import org.dcm4che.auditlog.MediaDescription;
import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Status;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4che.net.AAssociateRQ;
import org.dcm4che.net.AcceptorPolicy;
import org.dcm4che.net.ActiveAssociation;
import org.dcm4che.net.Association;
import org.dcm4che.net.AssociationFactory;
import org.dcm4che.net.DcmServiceBase;
import org.dcm4che.net.DcmServiceException;
import org.dcm4che.net.DcmServiceRegistry;
import org.dcm4che.net.Dimse;
import org.dcm4che.net.PresContext;
import org.dcm4che.server.DcmHandler;
import org.jboss.system.server.ServerConfigLocator;
import org.jboss.system.ServiceMBeanSupport;

/**
 *  Description of the Class
 *
 * @author     <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @since      November 3, 2002
 * @version    $Revision$
 */
public class PrintScpService
         extends ServiceMBeanSupport
         implements PrintScpServiceMBean
{

    // Constants -----------------------------------------------------
    private static final String UNKNOWN = "unknown";
    private static final String PRODUCT_UID = "1.2.40.0.13.2.1.1.2";
    private static final String DEF_LICENSE = "conf/license.jks";
    private static final int SHUTDOWN_DELAY_MINUTES = 20;
    private static final Map dumpParam = new HashMap(5);
    static {
        dumpParam.put("maxlen", new Integer(128));
        dumpParam.put("vallen", new Integer(64));
        dumpParam.put("prefix", "\t");
    }

    // Attributes ----------------------------------------------------
    private ObjectName auditLogName;
    private AuditLogger auditLog;
    private AuditLoggerFactory alf = AuditLoggerFactory.getInstance();
    private File spoolDir;
    private File licenseFile;
    private char[] licensePasswd;
    private boolean maskWarningAsSuccess = false;
    private boolean keepSpoolFiles = false;
    private boolean auditCreateSession = true;
    private boolean auditCreateFilmBox = true;
    private boolean auditPrintJob = false;
    private FilmSessionService filmSessionService = new FilmSessionService(this);
    private FilmBoxService filmBoxService = new FilmBoxService(this);
    private ImageBoxService imageBoxService = new ImageBoxService(this);
    private AnnotationBoxService annotationBoxService =
            new AnnotationBoxService(this);

    private ObjectName dcmServerName;
    private DcmHandler dcmHandler;
    private int numCreatedJobs = 0;
    private int numStoredPrints = 0;

    private X509Certificate license;

    // Static --------------------------------------------------------
    static final DcmObjectFactory dof = DcmObjectFactory.getInstance();
    static final AssociationFactory asf = AssociationFactory.getInstance();


    // Constructors --------------------------------------------------
    /**Constructor for the PrintScpService object */
    public PrintScpService()
    {
        setLicenseFile(DEF_LICENSE);
    }

    // Public --------------------------------------------------------

    // PrintScpMBean implementation ----------------------------------
    /**
     *  Gets the dcmServerName attribute of the PrintScpService object
     *
     * @return    The dcmServerName value
     */
    public ObjectName getDcmServerName()
    {
        return dcmServerName;
    }


    /**
     *  Sets the dcmServerName attribute of the PrintScpService object
     *
     * @param  dcmServerName  The new dcmServerName value
     */
    public void setDcmServerName(ObjectName dcmServerName)
    {
        this.dcmServerName = dcmServerName;
    }


    /**
     *  Gets the auditLoggerName attribute of the PrintScpService object
     *
     * @return    The auditLoggerName value
     */
    public ObjectName getAuditLoggerName()
    {
        return auditLogName;
    }


    /**
     *  Sets the auditLoggerName attribute of the PrintScpService object
     *
     * @param  auditLogName  The new auditLoggerName value
     */
    public void setAuditLoggerName(ObjectName auditLogName)
    {
        this.auditLogName = auditLogName;
    }


    /**
     *  Gets the spoolDirectory attribute of the PrintScpService object
     *
     * @return    The spoolDirectory value
     */
    public String getSpoolDirectory()
    {
        return spoolDir.getPath();
    }


    /**
     *  Sets the spoolDirectory attribute of the PrintScpService object
     *
     * @param  dname  The new spoolDirectory value
     */
    public void setSpoolDirectory(String dname)
    {
        this.spoolDir = toFile(dname);
    }


    /**
     *  Gets the licenseFile attribute of the PrintScpService object
     *
     * @return    The licenseFile value
     */
    public final String getLicenseFile()
    {
        return licenseFile.getPath();
    }


    /**
     *  Sets the licenseFile attribute of the PrintScpService object
     *
     * @param  fname  The new licenseFile value
     */
    public final void setLicenseFile(String fname)
    {
        this.licenseFile = toFile(fname);
    }


    /**
     *  Sets the licensePasswd attribute of the PrintScpService object
     *
     * @param  passwd  The new licensePasswd value
     */
    public void setLicensePasswd(String passwd)
    {
        this.licensePasswd = passwd.length() > 0 ? passwd.toCharArray() : null;
    }


    /**
     *  Gets the license attribute of the PrintScpService object
     *
     * @return    The license value
     */
    public X509Certificate getLicense()
    {
        return this.license;
    }


    /**
     *  Gets the keepSpoolFiles attribute of the PrintScpService object
     *
     * @return    The keepSpoolFiles value
     */
    public boolean isKeepSpoolFiles()
    {
        return keepSpoolFiles;
    }


    /**
     *  Sets the keepSpoolFiles attribute of the PrintScpService object
     *
     * @param  keepSpoolFiles  The new keepSpoolFiles value
     */
    public void setKeepSpoolFiles(boolean keepSpoolFiles)
    {
        this.keepSpoolFiles = keepSpoolFiles;
    }

    /**
     *  Gets the maskWarningAsSuccess attribute of the PrintScpService object
     *
     * @return    The maskWarningAsSuccess value
     */
    public boolean isMaskWarningAsSuccess()
    {
        return maskWarningAsSuccess;
    }


    /**
     *  Sets the maskWarningAsSuccess attribute of the PrintScpService object
     *
     * @param  maskWarningAsSuccess  The new maskWarningAsSuccess value
     */
    public void setMaskWarningAsSuccess(boolean maskWarningAsSuccess)
    {
        this.maskWarningAsSuccess = maskWarningAsSuccess;
    }

    /**
     *  Gets the auditCreateSession attribute of the PrintScpService object
     *
     * @return    The auditCreateSession value
     */
    public boolean isAuditCreateSession()
    {
        return auditCreateSession;
    }


    /**
     *  Sets the auditCreateSession attribute of the PrintScpService object
     *
     * @param  auditCreateSession  The new auditCreateSession value
     */
    public void setAuditCreateSession(boolean auditCreateSession)
    {
        this.auditCreateSession = auditCreateSession;
    }


    /**
     *  Gets the auditCreateFilmBox attribute of the PrintScpService object
     *
     * @return    The auditCreateFilmBox value
     */
    public boolean isAuditCreateFilmBox()
    {
        return auditCreateFilmBox;
    }


    /**
     *  Sets the auditCreateFilmBox attribute of the PrintScpService object
     *
     * @param  auditCreateFilmBox  The new auditCreateFilmBox value
     */
    public void setAuditCreateFilmBox(boolean auditCreateFilmBox)
    {
        this.auditCreateFilmBox = auditCreateFilmBox;
    }


    /**
     *  Gets the auditPrintJob attribute of the PrintScpService object
     *
     * @return    The auditPrintJob value
     */
    public boolean isAuditPrintJob()
    {
        return auditPrintJob;
    }


    /**
     *  Sets the auditPrintJob attribute of the PrintScpService object
     *
     * @param  auditPrintJob  The new auditPrintJob value
     */
    public void setAuditPrintJob(boolean auditPrintJob)
    {
        this.auditPrintJob = auditPrintJob;
    }


    /**
     *  Gets the numCreatedJobs attribute of the PrintScpService object
     *
     * @return    The numCreatedJobs value
     */
    public int getNumCreatedJobs()
    {
        return numCreatedJobs;
    }


    /**
     *  Gets the numStoredPrints attribute of the PrintScpService object
     *
     * @return    The numStoredPrints value
     */
    public int getNumStoredPrints()
    {
        return numStoredPrints;
    }


    /**
     *  Description of the Method
     *
     * @param  aet     Description of the Parameter
     * @param  policy  Description of the Parameter
     */
    public void putAcceptorPolicy(String aet, AcceptorPolicy policy)
    {
        dcmHandler.getAcceptorPolicy().putPolicyForCalledAET(aet, policy);
    }


    /**
     *  Description of the Method
     *
     * @param  job  Description of the Parameter
     */
    public void onJobStartPrinting(String job)
    {
    }


    /**
     *  Description of the Method
     *
     * @param  job  Description of the Parameter
     */
    public void onJobDone(String job)
    {
        deleteJob(new File(job));
    }


    /**
     *  Description of the Method
     *
     * @param  job  Description of the Parameter
     */
    public void onJobFailed(String job)
    {
        deleteJob(new File(job));
    }


    private File toFile(String fname)
    {
        File f = new File(fname);
        return f.isAbsolute()
                 ? f
                 : new File(ServerConfigLocator.locate().getServerHomeDir(), fname);
    }

    // ServiceMBeanSupport overrides -----------------------------------
    /**
     *  Description of the Method
     *
     * @exception  Exception  Description of the Exception
     */
    public void startService()
        throws Exception
    {
        auditLog = (AuditLogger) server.getAttribute(
                auditLogName, "AuditLogger");
        checkLicense();
        if (!spoolDir.exists()) {
            log.info("Creating spool directory - " + spoolDir.getCanonicalPath());
            spoolDir.mkdirs();
        }
        if (!spoolDir.isDirectory() || !spoolDir.canWrite()) {
            throw new IOException("No writeable spool directory - " + spoolDir);
        }
        cleardir(spoolDir);
        dcmHandler = (DcmHandler) server.getAttribute(dcmServerName, "DcmHandler");
        bindDcmServices();
    }


    /**
     *  Description of the Method
     *
     * @exception  Exception  Description of the Exception
     */
    public void stopService()
        throws Exception
    {
        unbindDcmServices();
        dcmHandler = null;
        if (!keepSpoolFiles) {
            cleardir(spoolDir);
        }
    }


    private void checkLicense()
    {
        try {
            LicenseStore store = new LicenseStore(licenseFile, licensePasswd);
            license = store.getLicenseFor(PRODUCT_UID);
            if (license != null) {
                license.checkValidity();
                if (LicenseStore.countSubjectIDs(license) == 0
                         || LicenseStore.countMatchingSubjectIDs(license) > 0) {
                    log.info("Detect valid License for " + license.getSubjectDN());
                    return;
                    // OK
                }
            }
        } catch (Exception e) {
            log.debug(e, e);
        }
        log.warn("No valid License detected - shutdown server in "
                 + SHUTDOWN_DELAY_MINUTES + " minutes!");
        new Timer().schedule(
            new TimerTask()
            {
                public void run()
                {
                    org.jboss.Main.systemExit(null);
                }
            },
                SHUTDOWN_DELAY_MINUTES * 60000L);
    }


    private void bindDcmServices()
    {
        DcmServiceRegistry services = dcmHandler.getDcmServiceRegistry();
        services.bind(UIDs.BasicFilmSession, filmSessionService);
        services.bind(UIDs.BasicFilmBoxSOP, filmBoxService);
        services.bind(UIDs.BasicColorImageBox, imageBoxService);
        services.bind(UIDs.BasicGrayscaleImageBox, imageBoxService);
        services.bind(UIDs.Printer, printerService);
        services.bind(UIDs.PresentationLUT, plutService);
        services.bind(UIDs.BasicAnnotationBox, annotationBoxService);
    }


    private void unbindDcmServices()
    {
        DcmServiceRegistry services = dcmHandler.getDcmServiceRegistry();
        services.unbind(UIDs.BasicFilmSession);
        services.unbind(UIDs.BasicFilmBoxSOP);
        services.unbind(UIDs.BasicColorImageBox);
        services.unbind(UIDs.BasicGrayscaleImageBox);
        services.unbind(UIDs.Printer);
        services.unbind(UIDs.PresentationLUT);
        services.unbind(UIDs.BasicAnnotationBox);
    }


    // Package protected ---------------------------------------------
    void logDataset(String prompt, Dataset ds)
    {
        if (!log.isDebugEnabled()) {
            return;
        }
        try {
            StringWriter w = new StringWriter();
            w.write(prompt);
            ds.dumpDataset(w, dumpParam);
            log.debug(w.toString());
        } catch (Exception e) {
            log.warn("Failed to dump dataset", e);
        }
    }


    private ObjectName makePrinterName(String aet)
        throws MalformedObjectNameException
    {
        return new ObjectName(PrinterServiceMBean.OBJECT_NAME_PREFIX + aet);
    }


    Object getPrinterAttribute(String aet, String attribute)
        throws Exception
    {
        return server.getAttribute(makePrinterName(aet), attribute);
    }


    Object getPrinterAttribute(String aet, String getter, boolean color)
        throws Exception
    {
        return server.invoke(makePrinterName(aet), getter,
                new Object[]{new Boolean(color)},
                new String[]{Boolean.class.getName()});
    }


    int getIntPrinterAttribute(String aet, String getter, boolean color)
        throws Exception
    {
        Integer i = (Integer) getPrinterAttribute(aet, getter, color);
        return i.intValue();
    }


    boolean getBooleanPrinterAttribute(String aet, String attribute)
        throws Exception
    {
        Boolean b = (Boolean) getPrinterAttribute(aet, attribute);
        return b.booleanValue();
    }


    int getIntPrinterAttribute(String aet, String attribute)
        throws Exception
    {
        Integer i = (Integer) getPrinterAttribute(aet, attribute);
        return i.intValue();
    }


    boolean isPrinter(String aet, String method, String arg)
        throws Exception
    {
        return isPrinter(aet, method,
                new Object[]{arg},
                new String[]{String.class.getName()});
    }


    boolean isPrinter(String aet, String method, String arg1, String arg2)
        throws Exception
    {
        return isPrinter(aet, method,
                new Object[]{arg1, arg2},
                new String[]{String.class.getName(), String.class.getName()});
    }


    boolean isPrinter(String aet, String method, Object[] arg, String[] type)
        throws Exception
    {
        Boolean b = (Boolean)
                server.invoke(makePrinterName(aet), method, arg, type);
        return b.booleanValue();
    }


    void checkAttribute(Dataset ds, int tag, String aet, String test, Command rsp)
        throws Exception
    {
        String s = ds.getString(tag);
        if (s == null) {
            return;
        }
        if (isPrinter(aet, test, s)) {
            return;
        }
        log.warn("Attribute Value Out Of Range: " + ds.get(tag));
        if (!maskWarningAsSuccess) {
            rsp.putUS(Tags.Status, Status.AttributeValueOutOfRange);
        }
        ds.remove(tag);
    }


    void checkAttribute(Dataset ds, int tag, String[] enum, Command rsp)
    {
        String s = ds.getString(tag);
        if (s == null) {
            return;
        }
        for (int i = 0; i < enum.length; ++i) {
            if (enum[i].equals(s)) {
                return;
            }
        }
        log.warn("Attribute Value Out Of Range: " + ds.get(tag));
        if (!maskWarningAsSuccess) {
            rsp.putUS(Tags.Status, Status.AttributeValueOutOfRange);
        }
        ds.remove(tag);
    }


    void ignoreAttribute(Dataset ds, int tag, Command rsp)
    {
        ignoreAttribute(ds, tag, rsp, Status.AttributeListError);
    }


    void ignoreAttribute(Dataset ds, int tag, Command rsp, int errcode)
    {
        DcmElement e = ds.get(tag);
        if (e == null) {
            return;
        }
        log.warn("Attribute not supported: " + e);
        if (!maskWarningAsSuccess) {
            rsp.putUS(Tags.Status, errcode);
        }
        ds.remove(tag);
    }


    void checkAttributeLen(Dataset ds, int tag, int maxlen, Command rsp)
    {
        String s = ds.getString(tag);
        if (s == null) {
            return;
        }
        if (s.length() <= maxlen) {
            return;
        }
        log.warn("Value Out Of Range: " + ds.get(tag));
        if (!maskWarningAsSuccess) {
            rsp.putUS(Tags.Status, Status.AttributeValueOutOfRange);
        }
        ds.remove(tag);
    }


    void checkNumberOfCopies(Dataset ds, String aet, Command rsp)
        throws Exception
    {
        Integer copies = ds.getInteger(Tags.NumberOfCopies);
        if (copies == null) {
            return;
        }
        int i = copies.intValue();
        if (i > 0 && i <= getIntPrinterAttribute(aet, "MaxNumberOfCopies")) {
            return;
        }
        log.warn("Number Of Copies Value Out Of Range: " + i);
        if (!maskWarningAsSuccess) {
            rsp.putUS(Tags.Status, Status.AttributeValueOutOfRange);
        }
        ds.remove(Tags.NumberOfCopies);
    }


    void checkMinMaxDensity(Dataset ds, String aet, boolean color, Command rsp)
        throws Exception
    {
        int minDensityPrinter = getIntPrinterAttribute(aet, "getMinDensity", color);
        int minDensity = ds.getInt(Tags.MinDensity, minDensityPrinter);
        if (minDensity < minDensityPrinter) {
            log.warn("Min Density Value: " + minDensity
                     + " < Min Density Printer: " + minDensityPrinter);
            if (!maskWarningAsSuccess) {
                rsp.putUS(Tags.Status, Status.MinMaxDensityOutOfRange);
            }
            ds.remove(Tags.MinDensity);
            minDensity = minDensityPrinter;
        }
        
        int maxDensityPrinter = getIntPrinterAttribute(aet, "getMaxDensity", color);
        int maxDensity = ds.getInt(Tags.MaxDensity, maxDensityPrinter);
        if (maxDensity > maxDensityPrinter) {
            log.warn("Max Density Value: " + maxDensity
                     + " > Max Density Printer: " + maxDensityPrinter);
            if (!maskWarningAsSuccess) {
                rsp.putUS(Tags.Status, Status.MinMaxDensityOutOfRange);
            }
            ds.remove(Tags.MaxDensity);
            maxDensity = maxDensityPrinter;
        }
        if (minDensity > maxDensity) {
            log.warn("Min Density Value: " + minDensity
                     + " < Max Density Value: " + maxDensity);
            if (!maskWarningAsSuccess) {
                rsp.putUS(Tags.Status, Status.MinMaxDensityOutOfRange);
            }
            ds.remove(Tags.MinDensity);
            ds.remove(Tags.MaxDensity);
        }
    }


    void checkImageDisplayFormat(Dataset ds, String aet, Command rsp)
        throws Exception
    {
        String s = ds.getString(Tags.ImageDisplayFormat);
        if (s == null) {
            log.error("Missing Image Display Format");
            throw new DcmServiceException(Status.MissingAttribute);
        }
        if (isPrinter(aet, "isSupportsDisplayFormat",
                s, ds.getString(Tags.FilmOrientation))) {
            return;
        }
        log.error("Invalid Image Display Format Value: " + s);
        throw new DcmServiceException(Status.InvalidAttributeValue);
    }


    int countAnnotationBoxes(String aet, String annotationID)
        throws DcmServiceException
    {
        try {
            Integer i = (Integer)
                    server.invoke(makePrinterName(aet),
                    "countAnnotationBoxes",
                    new Object[]{annotationID},
                    new String[]{String.class.getName()});
            return i.intValue();
        } catch (Exception e) {
            throw new DcmServiceException(Status.ProcessingFailure,
                    "Failed to count Annotation Boxes for format ID - " + annotationID);
        }
    }


    FilmSession getFilmSession(ActiveAssociation as)
    {
        return (FilmSession) as.getAssociation().getProperty("FilmSession");
    }


    HashMap getPresentationLUTs(ActiveAssociation as)
    {
        Association a = as.getAssociation();
        HashMap result = (HashMap) a.getProperty("PresentationLUTs");
        if (result == null) {
            a.putProperty("PresentationLUTs", result = new HashMap());
        }
        return result;
    }


    File getSessionSpoolDir(Association a, String uid)
    {
        File dir = new File(spoolDir, a.getCalledAET());
        dir = new File(dir, a.getCallingAET());
        return new File(dir, uid);
    }


    void initSessionSpoolDir(File dir)
        throws DcmServiceException
    {
        log.info("Create Spool Directory for Film Session[uid="
                 + dir.getName() + "]");
        if (!dir.mkdirs() || !lockSessionSpoolDir(dir)
                 || !new File(dir, SPOOL_HARDCOPY_DIR_SUFFIX).mkdir()
                 || !new File(dir, SPOOL_JOB_DIR_SUFFIX).mkdir()) {
            deltree(dir);
            throw new DcmServiceException(Status.ProcessingFailure,
                    "Failed to initalize spool directory: " + dir);
        }
    }


    private boolean lockSessionSpoolDir(File dir)
    {
        try {
            new File(dir, SPOOL_SESSION_LOCK_SUFFIX).createNewFile();
            return true;
        } catch (IOException e) {
            return false;
        }
    }


    private int countJobsInSession(File dir)
    {
        return new File(dir, SPOOL_JOB_DIR_SUFFIX).list().length;
    }


    void purgeSessionSpoolDir(File dir, boolean unlock)
    {
        File lock = new File(dir, SPOOL_SESSION_LOCK_SUFFIX);
        if (unlock) {
            lock.delete();
        } else if (lock.exists()) {
            return;
        }
        if (!keepSpoolFiles && countJobsInSession(dir) == 0) {
            log.info("Delete Spool Directory for Film Session[uid="
                     + dir.getName() + "]");
            deltree(dir);
        }
    }


    void cleardir(File dir)
    {
        File[] files = dir.listFiles();
        for (int i = 0; i < files.length; ++i) {
            deltree(files[i]);
        }
    }


    boolean deltree(File dir)
    {
        if (dir.isDirectory()) {
            cleardir(dir);
        }
        return dir.delete();
    }


    void doAuditLog(Association as, FilmSession session)
    {
        try {
            String aet = as.getCalledAET();
            Dataset sessionAttr = session.getAttributes();
            Dataset proposedStudy =
                    sessionAttr.getItem(Tags.ProposedStudySeq);
            String patID = proposedStudy != null
                     ? proposedStudy.getString(Tags.PatientID, UNKNOWN)
                     : UNKNOWN;
            String patName = proposedStudy != null
                     ? proposedStudy.getString(Tags.PatientName, UNKNOWN)
                     : UNKNOWN;
            MediaDescription mediaDesc = alf.newMediaDescription(
                    alf.newPatient(patID, patName));
            mediaDesc.setMediaType(sessionAttr.getString(Tags.MediumType,
                    (String) getPrinterAttribute(aet, "DefaultMediumType")));
            mediaDesc.setDestination(alf.newLocalPrinter(
                    (String) getPrinterAttribute(aet, "PrinterName")));
            auditLog.logExport(mediaDesc,
                    alf.newRemoteUser(alf.newRemoteNode(
                    as.getSocket(), as.getCallingAET())));
        } catch (Exception e) {
            log.warn("Failed to send audit log:", e);
        }
    }


    void createPrintJob(Association as, FilmSession session, boolean all)
        throws DcmServiceException
    {
        if (auditPrintJob) {
            doAuditLog(as, session);
        }
        String aet = as.getCalledAET();
        String jobID = "J-" + ++numCreatedJobs;
        File jobdir = new File(new File(session.dir(), SPOOL_JOB_DIR_SUFFIX), jobID);
        if (!jobdir.mkdir()) {
            throw new DcmServiceException(Status.ProcessingFailure,
                    "Failed write access to spool directory: " + spoolDir);
        }
        log.info("Create job: " + jobID);
        try {
            if (all) {
                Iterator it = session.getFilmBoxes().values().iterator();
                while (it.hasNext()) {
                    storePrint(jobdir, session, (FilmBox) it.next());
                }
            } else {
                storePrint(jobdir, session, session.getCurrentFilmBox());
            }
            Dataset sessionAttr = dof.newDataset();
            sessionAttr.putAll(session.getAttributes());
            try {
                server.invoke(makePrinterName(aet), "scheduleJob",
                        new Object[]{
                        jobdir.getPath(),
                        sessionAttr,
                        new Boolean(session.isColor())
                        },
                        new String[]{
                        String.class.getName(),
                        Dataset.class.getName(),
                        Boolean.class.getName()
                        });
            } catch (Exception e) {
                throw new DcmServiceException(Status.ProcessingFailure, e);
            }
        } catch (DcmServiceException e) {
            deltree(jobdir);
            throw e;
        }
    }


    void deleteJob(File job)
    {
        log.info("Deleting job - " + job.getName());
        if (!job.exists()) {
            log.warn("No such job - " + job.getName());
            return;
        }
        if (keepSpoolFiles) {
            return;
        }
        if (!deltree(job)) {
            log.warn("Failed to delete job - " + job.getName());
        }
        purgeSessionSpoolDir(job.getParentFile().getParentFile(), false);
    }


    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------
    private void storePrint(File job, FilmSession session, FilmBox filmBox)
        throws DcmServiceException
    {
        String spID = "SP-" + ++numStoredPrints;
        try {
            Dataset storedPrint = filmBox.createStoredPrint(session);
            File f = new File(job, spID);
            OutputStream out = new BufferedOutputStream(new FileOutputStream(f));
            try {
                storedPrint.writeFile(out, null);
            } finally {
                try {
                    out.close();
                } catch (IOException ignore) {}
            }
        } catch (IOException e) {
            throw new DcmServiceException(Status.ProcessingFailure, e);
        }
    }


    // Inner classes -------------------------------------------------
    private DcmServiceBase plutService =
        new DcmServiceBase()
        {
            protected Dataset doNCreate(ActiveAssociation as, Dimse rq, Command rspCmd)
                throws IOException, DcmServiceException
            {
                try {
                    Dataset ds = rq.getDataset();
                    logDataset("Create P-LUT:\n", ds);
                    // read out dataset
                    String uid = rspCmd.getAffectedSOPInstanceUID();
                    log.info("Creating Presentation LUT[uid=" + uid + "]");
                    HashMap pluts = getPresentationLUTs(as);
                    if (pluts.get(uid) != null) {
                        throw new DcmServiceException(Status.DuplicateSOPInstance);
                    }
                    // add SOP Instane UID for use as Presentation LUT Content Seq Item
                    // in Stored Print Object
                    ds.putUI(Tags.SOPInstanceUID, uid);
                    pluts.put(uid, ds);
                    log.info("Created Presentation LUT[uid=" + uid + "]");
                    return null;
                } catch (DcmServiceException e) {
                    log.warn("Failed to create Presentation LUT SOP Instance", e);
                    throw e;
                }
            }


            protected Dataset doNDelete(ActiveAssociation as, Dimse rq, Command rspCmd)
                throws IOException, DcmServiceException
            {
                try {
                    String uid = rq.getCommand().getRequestedSOPInstanceUID();
                    HashMap pluts = getPresentationLUTs(as);
                    if (pluts.get(uid) == null) {
                        throw new DcmServiceException(Status.NoSuchObjectInstance);
                    }
                    pluts.remove(uid);
                    return null;
                } catch (DcmServiceException e) {
                    log.warn("Failed to delete Presentation LUT SOP Instance", e);
                    throw e;
                }
            }
        };

    private static final int[] PRINTER_MODULE_ATTRS = {
            Tags.PrinterStatus,
            Tags.PrinterStatusInfo,
            Tags.PrinterName,
            Tags.Manufacturer,
            Tags.ManufacturerModelName,
            Tags.DeviceSerialNumber,
            Tags.SoftwareVersion,
            Tags.DateOfLastCalibration,
            Tags.TimeOfLastCalibration,
            };

    private DcmServiceBase printerService =
        new DcmServiceBase()
        {
            protected Dataset doNGet(ActiveAssociation as, Dimse rq, Command rspCmd)
                throws IOException, DcmServiceException
            {
                Association a = as.getAssociation();
                String aet = a.getCalledAET();
                AAssociateRQ aarq = a.getAAssociateRQ();
                PresContext pc = aarq.getPresContext(rq.pcid());
                String asuid = pc.getAbstractSyntaxUID();
                boolean color = asuid.equals(UIDs.BasicColorPrintManagement);
                int[] tags = rq.getCommand().getTags(Tags.AttributeIdentifierList);
                try {
                    return getPrinterAttributes(aet, color,
                            tags != null ? tags : PRINTER_MODULE_ATTRS);
                } catch (Exception e) {
                    log.error("Failed to access printer status", e);
                    throw new DcmServiceException(Status.ProcessingFailure,
                            "Failed to access printer status");
                }
            }
        };


    private String toLO(String src)
    {
        return src != null ? src.replace('\\', '/') : null;
    }


    private Dataset getPrinterAttributes(String aet, boolean color, int[] tags)
        throws Exception
    {
        Dataset attrs = dof.newDataset();
        for (int i = 0; i < tags.length; ++i) {
            switch (tags[i]) {
                case Tags.PrinterStatus:
                    attrs.putCS(Tags.PrinterStatus, ((PrinterStatus)
                            getPrinterAttribute(aet, "getStatus", color))
                            .toString());
                    break;
                case Tags.PrinterStatusInfo:
                    attrs.putCS(Tags.PrinterStatusInfo, ((PrinterStatusInfo)
                            getPrinterAttribute(aet, "getStatusInfo", color))
                            .toString());
                    break;
                case Tags.PrinterName:
                    attrs.putLO(Tags.PrinterName,
                            toLO((String) getPrinterAttribute(aet, "PrinterName")));
                    break;
                case Tags.Manufacturer:
                    attrs.putLO(Tags.Manufacturer,
                            toLO((String) getPrinterAttribute(aet, "Manufacturer")));
                    break;
                case Tags.ManufacturerModelName:
                    attrs.putLO(Tags.ManufacturerModelName,
                            toLO((String) getPrinterAttribute(aet, "ManufacturerModelName")));
                    break;
                case Tags.DeviceSerialNumber:
                    attrs.putLO(Tags.DeviceSerialNumber,
                            toLO((String) getPrinterAttribute(aet, "DeviceSerialNumber")));
                    break;
                case Tags.SoftwareVersion:
                    attrs.putLO(Tags.SoftwareVersion,
                            toLO((String) getPrinterAttribute(aet, "SoftwareVersion")));
                    break;
                case Tags.DateOfLastCalibration:
                    attrs.putDA(Tags.DateOfLastCalibration, (String)
                            getPrinterAttribute(aet, "getDateOfLastCalibration", color));
                    break;
                case Tags.TimeOfLastCalibration:
                    attrs.putTM(Tags.TimeOfLastCalibration, (String)
                            getPrinterAttribute(aet, "getTimeOfLastCalibration", color));
                    break;
            }
        }
        return attrs;
    }
}

