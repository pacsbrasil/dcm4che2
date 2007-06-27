package org.dcm4chee.xero.wado;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.jboss.mx.util.MBeanServerLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** This filter knows how to get the raw file location URL, from the file system management
 * name instance.
 * @author bwallace
 *
 */
public class FileLocationMgtFilter implements Filter<URL> {
	private static final Logger log = LoggerFactory.getLogger(FileLocationMgtFilter.class);
	
    private ObjectName fileSystemMgtName;
    private static MBeanServer server;


    public FileLocationMgtFilter() {
    		try {
				fileSystemMgtName = new ObjectName("dcm4chee.archive:service=FileSystemMgt");
			} catch (MalformedObjectNameException e) {
				e.printStackTrace();
				fileSystemMgtName = null;
			} catch (NullPointerException e) {
				fileSystemMgtName = null;
				e.printStackTrace();
			}
            server = MBeanServerLocator.locate();
    }
    

    /**
     * Returns the DICOM file for given arguments.
     * <p>
     * Use the FileSystemMgtService MBean to localize the DICOM file.
     * 
     * @param studyUID
     *            Unique identifier of the study.
     * @param seriesUID
     *            Unique identifier of the series.
     * @param instanceUID
     *            Unique identifier of the instance.
     * 
     * @return The File object or null if not found.
     * 
     * @throws IOException
     */
    public File getDICOMFile(String studyUID, String seriesUID,
            String instanceUID) throws IOException {
        Object dicomObject = null;
        try {
            dicomObject = server.invoke(fileSystemMgtName, "locateInstance",
                    new Object[] { instanceUID }, new String[] { String.class
                            .getName() });

        } catch (Exception e) {
            log.error("Failed to get DICOM file", e);
        }
        if (dicomObject == null)
            return null; // not found!
        if (dicomObject instanceof File)
            return (File) dicomObject; // We have the File!
        if (dicomObject instanceof String) {
            throw new RuntimeException("Need redirection?:"+(String) dicomObject);
        }
        return null;
    }

    /** Get the URL of the local file - may not be updated for DB changes etc */
	public URL filter(FilterItem filterItem, Map<String, Object> params) {
		if( fileSystemMgtName==null || server==null ) return (URL) filterItem.callNextFilter(params);
		String studyUID = (String) params.get("studyUID");
		String seriesUID = (String) params.get("seriesUID");
		String objectUID = (String) params.get("objectUID");
		File f;
		try {
			f = getDICOMFile(studyUID, seriesUID, objectUID);
			if( f==null ) return (URL) filterItem.callNextFilter(params);
			return f.toURI().toURL();
		} catch(RuntimeException e) { 
			throw e;
		} catch (Exception e) {
			log.warn("Caught exception getting dicom file location:"+e,e);
			return (URL) filterItem.callNextFilter(params);
		}
	}

}
