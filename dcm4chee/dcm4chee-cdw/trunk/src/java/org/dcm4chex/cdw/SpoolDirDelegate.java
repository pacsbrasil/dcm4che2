/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.cdw;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.management.ObjectName;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.FileFormat;
import org.dcm4che.dict.Tags;
import org.jboss.system.ServiceMBeanSupport;

/**
 * @author gunter.zeilinter@tiani.com
 * @version $Revision$ $Date$
 * @since 23.06.2004
 *
 */
public class SpoolDirDelegate {
    
    private static final DcmObjectFactory dof = DcmObjectFactory.getInstance();

    private static final String GET_MEDIA_LAYOUTS_ROOT = "getMediaLayoutsRoot";

    private static final String GET_MEDIA_CREATION_REQUEST_FILE = "getMediaCreationRequestFile";
    
    private static final String GET_INSTANCE_FILE = "getInstanceFile";
    
    private static final String YES = "YES";
    
    private final ServiceMBeanSupport service;

    private ObjectName spoolDirName;

    public SpoolDirDelegate(ServiceMBeanSupport service) {
        this.service = service;
    }

    public final ObjectName getSpoolDirName() {
        return spoolDirName;
    }

    public final void setSpoolDirName(ObjectName spoolDirName) {
        this.spoolDirName = spoolDirName;
    }

    public File getInstanceFile(String iuid) {
        return getSpoolFile(GET_INSTANCE_FILE, iuid);
    }

    public Dataset getInstance(String iuid) throws IOException {
        return readDatasetFrom(getInstanceFile(iuid));
    }
    
    public File getMediaCreationRequestFile(String iuid) {
        return getSpoolFile(GET_MEDIA_CREATION_REQUEST_FILE, iuid);
    }

    public Dataset getMediaCreationRequest(String iuid) throws IOException {
        return readDatasetFrom(getMediaCreationRequestFile(iuid));
    }
    
    public File getMediaLayoutsRoot(String iuid) {
        return getSpoolFile(GET_MEDIA_LAYOUTS_ROOT, iuid);
    }
    
    public File[] getMediaLayouts(String iuid) {
        return getMediaLayoutsRoot(iuid).listFiles();
    }

    private File getSpoolFile(String op, String iuid) {
        try {
            return (File) service.getServer().invoke(spoolDirName, op, new Object[] { iuid},
                    new String[] { String.class.getName()});
        } catch (Exception e) {
            throw new ConfigurationException(e);
        }
    }

    public Dataset readDatasetFrom(File f) throws IOException {
        String prompt = "M-READ " + f;
        service.getLog().info(prompt);
        InputStream in = new BufferedInputStream(new FileInputStream(f));
        try {
            Dataset ds = dof.newDataset();
            ds.readFile(in, FileFormat.DICOM_FILE, -1);
            return ds;
        } catch (IOException e) {
            service.getLog().error("Failed to " + prompt, e);
            throw e;
        } finally {
            try { in.close(); } catch (IOException ignore) { }
        }
    }

    public void writeDatasetTo(Dataset ds, File f) throws IOException {
        String prompt = (f.exists() ? "M-UPDATE " : "M-WRITE ") + f;
        service.getLog().info(prompt);
        OutputStream out = new BufferedOutputStream(new FileOutputStream(f));
        try {
            ds.writeFile(out, null);
        } catch (IOException e) {
            service.getLog().error("Failed to " + prompt, e);
            throw e;
        } finally {
            try { out.close(); } catch (IOException ignore) {}
        }
    }

    public boolean delete(File fileOrDirectory) {
        if (!fileOrDirectory.exists()) return false;
        if (fileOrDirectory.isDirectory()) {
            File[] files = fileOrDirectory.listFiles();
            for (int i = 0; i < files.length; i++)
                delete(files[i]);
	    }
        String prompt = "M-DELETE " + fileOrDirectory;
        service.getLog().info(prompt);
        boolean success = fileOrDirectory.delete();
        if (!success)
            service.getLog().error("Failed to " + prompt);
        return success;
    }

    public void deleteRefInstances(Dataset ds) {
        if (YES.equals(ds.getString(Tags.PreserveCompositeInstancesAfterMediaCreation))) return;
        DcmElement seq = ds.get(Tags.RefSOPSeq);
        for (int i = 0, n = seq.vm(); i < n; ++i) {
            Dataset item = seq.getItem(i);
            String iuid = item.getString(Tags.RefSOPInstanceUID);
            delete(getInstanceFile(iuid));
        }
    }

    public File[] getRefInstanceFiles(Dataset ds) {
        DcmElement seq = ds.get(Tags.RefSOPSeq);
        File[] files = new File[seq.vm()];
        for (int i = 0; i < files.length; ++i) {
            Dataset item = seq.getItem(i);
            String iuid = item.getString(Tags.RefSOPInstanceUID);
            files[i] = getInstanceFile(iuid);
        }
        return files;
    }
    
    public boolean deleteMediaLayouts(String iuid) {
        return delete(getMediaLayoutsRoot(iuid));        
    }    
}
