/*
 * Created on 09.02.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.dcm4chex.wado.mbean;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;
import org.dcm4chex.wado.common.RIDResponseObject;
import org.dcm4chex.wado.mbean.WADOSupport.NeedRedirectionException;
import org.dcm4chex.wado.mbean.ecg.WaveForm16Buffer;
import org.dcm4chex.wado.mbean.ecg.WaveForm8Buffer;
import org.dcm4chex.wado.mbean.ecg.WaveFormBuffer;
import org.dcm4chex.wado.mbean.ecg.WaveFormGroup;
import org.dcm4chex.wado.mbean.xml.SVGCreator;

/**
 * @author franz.willer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ECGSupport {

	public static final String CONTENT_TYPE_SVGXML = "Image/svg+xml";
	private static Logger log = Logger.getLogger( ECGSupport.class.getName() );
	
    private static final DcmObjectFactory factory = DcmObjectFactory.getInstance();
	private RIDSupport ridSupport;

    public ECGSupport( RIDSupport ridSupport ) {
    	this.ridSupport = ridSupport;
    }
	/**
	 * @param ds
	 * @return
	 */
	public RIDResponseObject getECGDocument(Dataset ds) {
		log.info("get ECG document!");
		try {
			Dataset fullDS = getDataset( ds );
			if ( fullDS == null ) {
				return new RIDStreamResponseObjectImpl( null, RIDSupport.CONTENT_TYPE_HTML, HttpServletResponse.SC_NOT_FOUND, "Requested document not found::"+ds.getString( Tags.SOPClassUID ) );
			} else {
				return handleECG( fullDS );
			}
		} catch (NeedRedirectionException e) {
			return new RIDStreamResponseObjectImpl( null, RIDSupport.CONTENT_TYPE_HTML, HttpServletResponse.SC_NOT_FOUND, "Requested Document is not on this Server! Try to get document from:"+e.getHostname() );
		} catch (Exception e) {
			return new RIDStreamResponseObjectImpl( null, RIDSupport.CONTENT_TYPE_HTML, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error:"+e.getMessage() );
		} 
	}
	
	/**
	 * @param string
	 * @return
     * @throws NeedRedirectionException
     * @throws IOException
	 */
	private Dataset getDataset(Dataset ds) throws IOException, NeedRedirectionException {
		File file = ridSupport.getWADOSupport().getDICOMFile(ds.getString( Tags.StudyInstanceUID),
															 ds.getString( Tags.SeriesInstanceUID),
															 ds.getString( Tags.SOPInstanceUID ) );
		if ( log.isDebugEnabled() ) log.debug("DCM file for "+ds.getString( Tags.SOPInstanceUID )+":"+file);
		if ( file == null ) return null;
		return loadDataset(file);
	}
	private Dataset loadDataset(File file) throws IOException {
	    BufferedInputStream bis = new BufferedInputStream( new FileInputStream(file));
	    Dataset ds = factory.newDataset();
	    try {
	        ds.readFile(bis, null, -1); 
	    } finally {
	        try {
	            bis.close();
	        } catch (IOException ignore) {
	        }
	    }
	    if ( log.isDebugEnabled() ) log.debug("Dataset for file "+file+" :"+ds);
	    return ds;
    }
	
    /**
	 * @param ds
	 * @return
	 */
	private RIDResponseObject handleECG(Dataset ds) {
		try { 
			DcmElement elem = ds.get( Tags.WaveformSeq);
			WaveFormGroup wfgrp = new WaveFormGroup( elem, 0 );//TODO all groups
			log.info( wfgrp );
			ByteBuffer bb = ds.get( Tags.WaveformSeq).getItem(0).getByteBuffer( Tags.WaveformData );
			log.info( "WafeFormData:"+bb.capacity());
			if ( log.isDebugEnabled() ) {
				StringBuffer sb1 = new StringBuffer();
				int len = 10;//number of samples to dump
				if ( len > wfgrp.getNrOfSamples() ) len = wfgrp.getNrOfSamples();
				int bytesPerRow = wfgrp.getBitsAlloc()/8 * wfgrp.getNrOfChannels();
				int bytelen = bytesPerRow * len;
				for ( int i = 0 ; i < bytelen ; i++ ) {
					if ( i % bytesPerRow == 0 ) sb1.append("\nSample ").append(i/bytesPerRow).append(" :");
					sb1.append( Integer.toHexString( ((int)bb.get()) & 0x0ff ) ).append("|");
				}
				log.info("ByteBuffer dump:"+sb1);
				for ( int i = 0; i < len ; i++) {
					StringBuffer sb = new StringBuffer();
					sb.append("Data(").append(i).append("):");
					for ( int j = 0 ; j < wfgrp.getNrOfChannels() ; j++) {
						sb.append( wfgrp.getChannel(j).getRawValue() ).append("|");
					}
					log.info(sb);
				}
			}
			SVGCreator svgCreator = new SVGCreator( wfgrp );
			return new RIDTransformResponseObjectImpl(svgCreator, CONTENT_TYPE_SVGXML, HttpServletResponse.SC_OK, null);
		} catch ( Throwable t ) {
			t.printStackTrace();
		}
		return new RIDStreamResponseObjectImpl( null, RIDSupport.CONTENT_TYPE_HTML, HttpServletResponse.SC_NOT_IMPLEMENTED, "ECG support not implemented yet!");
	}
	
	
	
}
