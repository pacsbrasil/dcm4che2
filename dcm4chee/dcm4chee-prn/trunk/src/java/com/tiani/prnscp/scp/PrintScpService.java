/*                                                                           *
 *  Copyright (c) 2002 by TIANI MEDGRAPH AG                                  *
 *                                                                           *
 *  This file is part of dcm4che.                                            *
 *                                                                           *
 *  This library is free software; you can redistribute it and/or modify it  *
 *  under the terms of the GNU Lesser General Public License as published    *
 *  by the Free Software Foundation; either version 2 of the License, or     *
 *  (at your option) any later version.                                      *
 *                                                                           *
 *  This library is distributed in the hope that it will be useful, but      *
 *  WITHOUT ANY WARRANTY; without even the implied warranty of               *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU        *
 *  Lesser General Public License for more details.                          *
 *                                                                           *
 *  You should have received a copy of the GNU Lesser General Public         *
 *  License along with this library; if not, write to the Free Software      *
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA  *
 */
package com.tiani.prnscp.scp;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import com.tiani.prnscp.print.PrinterServiceMBean;
import com.tiani.prnscp.print.PrinterStatus;
import com.tiani.prnscp.print.PrinterStatusInfo;
import com.tiani.util.license.LicenseStore;

import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmEncodeParam;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Status;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4che.net.AcceptorPolicy;
import org.dcm4che.net.ActiveAssociation;
import org.dcm4che.net.Association;
import org.dcm4che.net.AssociationFactory;
import org.dcm4che.net.DcmServiceBase;
import org.dcm4che.net.DcmServiceException;
import org.dcm4che.net.DcmServiceRegistry;
import org.dcm4che.net.Dimse;
import org.dcm4che.server.DcmHandler;

import org.jboss.system.ServiceMBeanSupport;
import org.jboss.system.server.ServerConfigLocator;

/**
 *  <description>
 *
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @created  November 3, 2002
 * @version  $Revision$
 */
public class PrintScpService
         extends ServiceMBeanSupport
         implements PrintScpServiceMBean
{

    // Constants -----------------------------------------------------
    final static String LICENSE_FILE = "conf/license.pem";
    final static String PRNSCP_PRODUCT_UID = "1.2.40.0.13.2.1.1";
    final static int SHUTDOWN_DELAY_MINUTES = 20;

    // Attributes ----------------------------------------------------
    private String spoolDirectory;
    private File spoolDir;
    private boolean keepSpoolFiles = false;
    private FilmSessionService filmSessionService = new FilmSessionService(this);
    private FilmBoxService filmBoxService = new FilmBoxService(this);
    private ImageBoxService imageBoxService = new ImageBoxService(this);
    private AnnotationBoxService annotationBoxService =
            new AnnotationBoxService(this);

    private ObjectName dcmServer;
    private DcmHandler dcmHandler;
    private int numCreatedJobs = 0;
    private int numStoredPrints = 0;

    /**  Holds value of property license. */
    private X509Certificate license;

    // Static --------------------------------------------------------
    final static DcmObjectFactory dof = DcmObjectFactory.getInstance();
    final static AssociationFactory asf = AssociationFactory.getInstance();


    // Constructors --------------------------------------------------

    // Public --------------------------------------------------------

    // PrintScpMBean implementation ----------------------------------
    /**
     *  Description of the Method
     *
     * @param  job Description of the Parameter
     */
    public void onJobStartPrinting(String job)
    {
    }


    /**
     *  Description of the Method
     *
     * @param  job Description of the Parameter
     */
    public void onJobFailed(String job)
    {
        deleteJob(new File(job));
    }


    /**
     *  Description of the Method
     *
     * @param  job Description of the Parameter
     */
    public void onJobDone(String job)
    {
        deleteJob(new File(job));
    }


    /**
     *  Getter for property dcmServer.
     *
     * @return  Value of property dcmServer.
     */
    public ObjectName getDcmServer()
    {
        return dcmServer;
    }


    /**
     *  Setter for property dcmServer.
     *
     * @param  dcmServer New value of property dcmServer.
     */
    public void setDcmServer(ObjectName dcmServer)
    {
        this.dcmServer = dcmServer;
    }


    /**
     *  Getter for property spoolDirPath.
     *
     * @return  Value of property spoolDirPath.
     */
    public String getSpoolDirectory()
    {
        return spoolDirectory;
    }


    /**
     *  Setter for property spoolDirPath.
     *
     * @param  spoolDirectory The new spoolDirectory value
     */
    public void setSpoolDirectory(String spoolDirectory)
    {
        this.spoolDirectory = spoolDirectory;
    }


    /**
     *  Getter for property keepSpoolFiles.
     *
     * @return  Value of property keepSpoolFiles.
     */
    public boolean isKeepSpoolFiles()
    {
        return keepSpoolFiles;
    }


    /**
     *  Setter for property keepSpoolFiles.
     *
     * @param  keepSpoolFiles New value of property keepSpoolFiles.
     */
    public void setKeepSpoolFiles(boolean keepSpoolFiles)
    {
        this.keepSpoolFiles = keepSpoolFiles;
    }


    /**
     *  Getter for property numCreatedJobs.
     *
     * @return  Value of property numCreatedJobs.
     */
    public int getNumCreatedJobs()
    {
        return numCreatedJobs;
    }


    /**
     *  Getter for property numStoredPrints.
     *
     * @return  Value of property numStoredPrints.
     */
    public int getNumStoredPrints()
    {
        return numStoredPrints;
    }


    /**
     *  Getter for property license.
     *
     * @return  Value of property license.
     */
    public X509Certificate getLicense()
    {
        return this.license;
    }


    /**
     *  Setter for property license.
     *
     * @param  license New value of property license.
     */
    public void setLicense(X509Certificate license)
    {
        this.license = license;
    }


    /**
     *  Description of the Method
     *
     * @param  aet Description of the Parameter
     * @param  policy Description of the Parameter
     */
    public void putAcceptorPolicy(String aet, AcceptorPolicy policy)
    {
        dcmHandler.getAcceptorPolicy().putPolicyForCalledAET(aet, policy);
    }


    // ServiceMBeanSupport overrides -----------------------------------
    /**
     *  Description of the Method
     *
     * @exception  Exception Description of the Exception
     */
    public void startService()
        throws Exception
    {
        File systemHomeDir = ServerConfigLocator.locate().getServerHomeDir();
        checkLicense(new File(systemHomeDir, LICENSE_FILE));
        spoolDir = new File(spoolDirectory);
        if (!spoolDir.isAbsolute()) {
            spoolDir = new File(systemHomeDir, spoolDirectory);
        }
        if (!spoolDir.exists()) {
            log.info("Creating spool directory - " + spoolDir.getCanonicalPath());
            spoolDir.mkdirs();
        }
        if (!spoolDir.isDirectory() || !spoolDir.canWrite()) {
            throw new IOException("No writeable spool directory - " + spoolDir);
        }
        cleardir(spoolDir);
        dcmHandler = (DcmHandler) server.getAttribute(dcmServer, "DcmHandler");
        bindDcmServices();
    }


    /**
     *  Description of the Method
     *
     * @exception  Exception Description of the Exception
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


    private void checkLicense(File licenseFile)
    {
        try {
            LicenseStore store = new LicenseStore(licenseFile);
            license = store.getLicenseFor(PRNSCP_PRODUCT_UID);
            if (license != null) {
                license.checkValidity();
                if (LicenseStore.countSubjectIDs(license) == 0
                         || LicenseStore.countMatchingSubjectIDs(license) > 0) {
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

    String checkAttributeValue(String aet, String test, Dataset ds, int tag,
            boolean type1)
        throws DcmServiceException
    {
        String val = ds.getString(tag);
        if (val == null) {
            if (type1) {
                log.warn("Missing attribute " + Tags.toString(tag));
                throw new DcmServiceException(Status.MissingAttributeValue);
            }
            return null;
        }
        try {
            if (isPrinter(aet, test,
                    new Object[]{val},
                    new String[]{String.class.getName()})) {
                return val;
            }
        } catch (Exception e) {
            log.error("Failed to checkAttributeValue " + test, e);
            throw new DcmServiceException(Status.ProcessingFailure);
        }
        if (type1) {
            log.warn("Invalid attribute value " + ds.get(tag));
            throw new DcmServiceException(Status.InvalidAttributeValue);
        } else {
            log.warn("Ignore invalid attribute value " + ds.get(tag));
            ds.remove(tag);
            return null;
        }
    }


    String checkImageDisplayFormat(String aet, String val, String orientation)
        throws DcmServiceException
    {
        if (val == null) {
            throw new DcmServiceException(Status.MissingAttributeValue);
        }
        try {
            if (orientation == null) {
                orientation =
                        (String) getPrinterAttribute(aet, "DefaultFilmOrientation");
            }
            if (isPrinter(aet, "isSupportsDisplayFormat",
                    new Object[]{val, orientation},
                    new String[]{String.class.getName(), String.class.getName()})) {
                return val;
            }
        } catch (Exception e) {
            log.error("Failed to checkImageDisplayFormat:", e);
            throw new DcmServiceException(Status.ProcessingFailure);
        }
        throw new DcmServiceException(Status.InvalidAttributeValue);
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


    boolean isPrinter(String aet, String methode, String arg)
        throws Exception
    {
        return isPrinter(aet, methode,
                new Object[]{arg},
                new String[]{String.class.getName()});
    }


    boolean isPrinter(String aet, String methode,
            String arg1, String arg2)
        throws Exception
    {
        return isPrinter(aet, methode,
                new Object[]{arg1, arg2},
                new String[]{String.class.getName(), String.class.getName()});
    }


    boolean isPrinter(String aet, String methode,
            Object[] arg, String[] type)
        throws Exception
    {
        Boolean b = (Boolean)
                server.invoke(makePrinterName(aet), methode, arg, type);
        return b.booleanValue();
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


    void createPrintJob(String aet, FilmSession session, boolean all)
        throws DcmServiceException
    {
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
                        new Boolean(session.isColor()),
                        jobdir.getPath(),
                        sessionAttr
                        },
                        new String[]{
                        Boolean.class.getName(),
                        String.class.getName(),
                        Dataset.class.getName()
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

    private final static int[] PRINTER_MODULE_ATTRS = {
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
                String aet = as.getAssociation().getCalledAET();
                int[] tags = rq.getCommand().getTags(Tags.AttributeIdentifierList);
                try {
                    return getPrinterAttributes(aet,
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


    private Dataset getPrinterAttributes(String aet, int[] tags)
        throws Exception
    {
        Dataset attrs = dof.newDataset();
        for (int i = 0; i < tags.length; ++i) {
            switch (tags[i]) {
                case Tags.PrinterStatus:
                    attrs.putCS(Tags.PrinterStatus,
                            ((PrinterStatus) getPrinterAttribute(aet, "Status"))
                            .toString());
                    break;
                case Tags.PrinterStatusInfo:
                    attrs.putCS(Tags.PrinterStatusInfo,
                            ((PrinterStatusInfo) getPrinterAttribute(aet, "StatusInfo"))
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
                    attrs.putDA(Tags.DateOfLastCalibration,
                            (String) getPrinterAttribute(aet, "DateOfLastCalibration"));
                    break;
                case Tags.TimeOfLastCalibration:
                    attrs.putTM(Tags.TimeOfLastCalibration,
                            (String) getPrinterAttribute(aet, "TimeOfLastCalibration"));
                    break;
            }
        }
        return attrs;
    }


    /**
     *  Description of the Method
     *
     * @return  Description of the Return Value
     */
    public String showLicense()
    {
        return "" + license;
    }

}

