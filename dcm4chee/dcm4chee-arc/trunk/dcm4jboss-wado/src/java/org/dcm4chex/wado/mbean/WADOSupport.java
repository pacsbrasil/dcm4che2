/*
 * Created on 10.12.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.dcm4chex.wado.mbean;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.dcm4chex.wado.common.WADORequestObject;
import org.dcm4chex.wado.common.WADOResponseObject;
import org.dcm4chex.wado.mbean.cache.IconCache;
import org.dcm4chex.wado.mbean.cache.IconCacheImpl;
import org.jboss.mx.util.MBeanServerLocator;

/**
 * @author franz.willer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WADOSupport {
	
public static final String CONTENT_TYPE_JPEG = "image/jpeg";

private static Logger log = Logger.getLogger( WADOService.class.getName() );

private static ObjectName fileSystemMgtName = null;

private static MBeanServer server;

public WADOSupport( MBeanServer mbServer ) {
	if ( server != null ) {
		server = mbServer;
	} else {
		server = MBeanServerLocator.locate();
	}
}

/**
 * Handles a WADO request and returns a WADO response object.
 * <p>
 * </DL>
 * <DT>If the request was successfull:</DT>
 * <DD>The WADO response object contains a File and a corresponding content type.</DD>
 * <DD>the return code was OK and the error message <code>null</code>.</DD>
 * <DT>If the request was not successfull (not found or an error):</DT>
 * <DD>The return code of the WADO response object is set to a http error code and an error message was set.</DD>
 * <DD>The file of the WADO response is <code>null</code>. The content type is not specified for this case.</DD>
 * </DL> 
 * @param req The WADO request object.
 * 
 * @return The WADO response object.
 */
public WADOResponseObject getWADOObject( WADORequestObject req ) {
	String studyUID = req.getStudyUID();
	String seriesUID = req.getSeriesUID();
	String instanceUID = req.getObjectUID();
	List contentTypes = req.getContentTypes();
	if ( contentTypes == null || contentTypes.contains( CONTENT_TYPE_JPEG ) ) {
		return this.handleJpg( studyUID, seriesUID, instanceUID, req.getRows(), req.getColumns() );
	} else {
		return new WADOResponseObjectImpl( null, CONTENT_TYPE_JPEG, HttpServletResponse.SC_NOT_IMPLEMENTED, "This method is not implemented!");
		
	}
}

/**
 * Handles a request for content type image/jpeg.
 * <p>
 * Use this method first if conetnt type jpeg is possible to get advantage of the cache.
 * <p>
 * 
 * @param studyUID		The unique id of a study.
 * @param seriesUID		The unique id of a series.
 * @param instanceUID	The unique id of an instance.
 * @param rows			The number of pixel rows (integer String)
 * @param columns		the number of pixel columns (integer String)
 * 
 * @return	The WADO response object containing the file of the image.
 */
public WADOResponseObject handleJpg( String studyUID, String seriesUID, String instanceUID, String rows, String columns ){
	try {
		IconCache cache = IconCacheImpl.getInstance();
		File file;
		BufferedImage bi = null;
		if ( rows == null ) {
			file = cache.getIconFile( studyUID, seriesUID, instanceUID );
		} else {
			file = cache.getIconFile( studyUID, seriesUID, instanceUID, rows, columns );
		}
		if ( file == null ) {
			File dicomFile = getDICOMFile( studyUID, seriesUID, instanceUID );
			if ( dicomFile != null ) {
				bi = getImage( dicomFile, rows, columns );
			} else {
				return new WADOResponseObjectImpl( null, CONTENT_TYPE_JPEG, HttpServletResponse.SC_NOT_FOUND, "DICOM object not found!");
			}
			if ( bi != null ) {
				if ( rows == null ) {
					file = cache.putIcon( bi, studyUID, seriesUID, instanceUID );
				} else {
					file = cache.putIcon( bi, studyUID, seriesUID, instanceUID, rows, columns );
				}
			}
		}
		if ( file != null ) {
			return new WADOResponseObjectImpl( file, CONTENT_TYPE_JPEG, HttpServletResponse.SC_OK, null);
			
		}
		
	} catch ( Exception x ) {
		log.error("Exception in handleJpg: "+x.getMessage(), x);
		return new WADOResponseObjectImpl( null, CONTENT_TYPE_JPEG, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unexpected error! Cant get jpeg");
	}
	return null;
}
/*_*/

/**
 * Returns the DICOM file for given arguments.
 * <p>
 * Use the FileSystemMgtService MBean to localize the DICOM file.
 * 
 * @param studyUID		Unique identifier of the study.
 * @param seriesUID		Unique identifier of the series.
 * @param instanceUID	Unique identifier of the instance.
 * 
 * @return The File object or null if not found.
 * 
 * @throws IOException
 */
private File getDICOMFile( String studyUID, String seriesUID, String instanceUID ) throws IOException {
    File file;
	try {
        Object o = server.invoke(fileSystemMgtName,
                "locateInstance",
                new Object[] { instanceUID },
                new String[] { String.class.getName() } );
        
        if ( o == null ) return null; //not found!
        if ( o instanceof File ) return (File) o; //We have the File!
        if ( o instanceof String ) {
        	return getRemoteDICOMFile( (String) o, studyUID, seriesUID, instanceUID);
        }
    } catch (Exception e) {
        System.out.println("Failed to get DICOM file:"+ e);
    }
	return null;
}

/**
 * Tries to get the DICOM file from an external WADO service.
 * 
 * @param string		Hostname of remote WADO service.
 * @param studyUID		Unique identifier of the study.
 * @param seriesUID		Unique identifier of the series.
 * @param instanceUID	Unique identifier of the instance.
 * 
 * @return The File object or null if not found.
 */
private File getRemoteDICOMFile(String string, String studyUID, String seriesUID, String instanceUID) {
	System.out.println("Warning: getRemoteDICOMFile not implemented!");
	return null;
}

/**
 * Get the image from DICOM file.
 * <p>
 * If <code>rows or columns</code> not null, the original image will be scaled.
 * 
 * @param file		A DICOM file.
 * @param rows			Image height in pixel.
 * @param columns		Image width in pixel.
 *
 * @return
 * @throws IOException
 */
private BufferedImage getImage(File file, String rows, String columns) throws IOException {
    Iterator it = ImageIO.getImageReadersByFormatName("DICOM");
    if (!it.hasNext())
            return null; //TODO more usefull stuff
    ImageReader reader = (ImageReader) it.next();
    ImageInputStream in = new FileImageInputStream( file );
    reader.setInput( in );
	BufferedImage bi = reader.read(0);
	//bi = ImageIO.read(new File(testImage));
	if ( rows != null || columns != null ) {
		bi = resize( bi, rows, columns );
	}
	return bi;
}

/**
 * Resize the given image.
 * 
 * @param bi		The image as BufferedImage.
 * @param rows			Image height in pixel.
 * @param columns		Image width in pixel.
 * 
 * @return
 */
private BufferedImage resize( BufferedImage bi, String rows, String columns ) {
	int h0 = bi.getHeight();
	int w0 = bi.getWidth();
	double ratio = (double)w0 / h0;
	int newH = -1 , newW = -1;
	if ( rows != null ) {
		newH = Integer.parseInt( rows );
	}
	if ( columns != null) {
		newW = Integer.parseInt( columns );
	}
	if ( newW == -1 ) 
		newW = (int) (newH * ratio);
	else if ( newH == -1 ) 
		newH = (int) (newW / ratio);
//    int w = Math.min(w0, newW);
//    int h = Math.min(h0, newH);
	int w = newW;
	int h = newH;
    w = (int) Math.min(w, h * ratio);
    h = (int) Math.min(h, w / ratio);
	if ( w != w0 || h != h0 ) {
        AffineTransform scale = AffineTransform.getScaleInstance((double) w
                / w0, (double) h / h0);
        AffineTransformOp scaleOp = new AffineTransformOp(scale,
                AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        BufferedImage biDest = scaleOp.filter( bi, null );
		return biDest;
    } else {
    	return bi;
    }
}

/**
 * Set the name of the FileSystemMgtBean.
 * <p>
 * This bean is used to retrieve the DICOM object.
 * 
 * @param fileSystemMgtName The fileSystemMgtName to set.
 */
public void setFileSystemMgtName(ObjectName fileSystemMgtName) {
	WADOSupport.fileSystemMgtName = fileSystemMgtName;
}

/**
 * Get the name of the FileSystemMgtBean.
 * <p>
 * This bean is used to retrieve the DICOM object.
 * 
 * @return Returns the fileSystemMgtName.
 */
public ObjectName getFileSystemMgtName() {
	return fileSystemMgtName;
}

}
