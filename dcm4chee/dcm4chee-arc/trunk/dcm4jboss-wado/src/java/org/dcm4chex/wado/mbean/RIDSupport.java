/*
 * Created on 12.01.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.dcm4chex.wado.mbean;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.fop.apps.Driver;
import org.apache.log4j.Logger;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.DcmParser;
import org.dcm4che.data.DcmParserFactory;
import org.dcm4che.dict.DictionaryFactory;
import org.dcm4che.dict.Tags;
import org.dcm4che.srom.SRDocumentFactory;
import org.dcm4chex.archive.ejb.jdbc.QueryCmd;
import org.dcm4chex.wado.common.RIDRequestObject;
import org.dcm4chex.wado.common.RIDResponseObject;
import org.dcm4chex.wado.mbean.cache.WADOCache;
import org.dcm4chex.wado.mbean.cache.WADOCacheImpl;
import org.dcm4chex.wado.mbean.xml.IHEDocumentList;
import org.jboss.mx.util.MBeanServerLocator;
import org.xml.sax.SAXException;

/**
 * @author franz.willer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class RIDSupport {

	private static Logger log = Logger.getLogger( RIDService.class.getName() );
    private static final DcmObjectFactory factory = DcmObjectFactory.getInstance();
    private static final SRDocumentFactory srFact = SRDocumentFactory.getInstance();

    private static final String FOBSR_XSL_URI = "resource:xsl/fobsr.xsl";
    private final Driver fop = new Driver();

    private static MBeanServer server;
	private Set ecgSopCuids;
	private String ridSummaryXsl;
	private static List conceptNameCodes;
	private boolean useXSLInstruction;
	private static ObjectName fileSystemMgtName;

	public RIDSupport( MBeanServer mbServer ) {
		if ( server != null ) {
			server = mbServer;
		} else {
			server = MBeanServerLocator.locate();
		}
	}

	/**
	 * @return Returns the sopCuids.
	 */
	public Set getECGSopCuids() {
		return ecgSopCuids;
	}
	/**
	 * @param sopCuids The sopCuids to set.
	 */
	public void setECGSopCuids(Set sopCuids) {
		this.ecgSopCuids = sopCuids;
	}
	
	private static List getConceptNameCodes() {
		if ( conceptNameCodes == null ) {
			conceptNameCodes = new ArrayList();
			conceptNameCodes.add( createCodeDS( "18745-0" ) );//Cardiac Catheteization Report
			conceptNameCodes.add( createCodeDS( "11540-2" ) );
			conceptNameCodes.add( createCodeDS( "11538-6" ) );
			conceptNameCodes.add( createCodeDS( "11539-4" ) );
			conceptNameCodes.add( createCodeDS( "18747-6" ) );//CT Report
			conceptNameCodes.add( createCodeDS( "18748-4" ) );
			conceptNameCodes.add( createCodeDS( "11522-0" ) );
			conceptNameCodes.add( createCodeDS( "18760-9" ) );
			conceptNameCodes.add( createCodeDS( "11541-0" ) );
			conceptNameCodes.add( createCodeDS( "18755-9" ) );
			conceptNameCodes.add( createCodeDS( "18756-7" ) );
			conceptNameCodes.add( createCodeDS( "18757-5" ) );
			conceptNameCodes.add( createCodeDS( "11525-3" ) );
			conceptNameCodes.add( createCodeDS( "18758-3" ) );
			conceptNameCodes.add( createCodeDS( "11528-7" ) );
		}
		return conceptNameCodes;
	}
	
	private static Dataset createCodeDS( String value ) {
		Dataset ds = factory.newDataset();
        ds.putSH(Tags.CodeValue, value);
        ds.putSH(Tags.CodingSchemeDesignator, "LN");
        return ds;
	}
	/**
	 * @return Returns the ridSummaryXsl.
	 */
	public String getRIDSummaryXsl() {
		return ridSummaryXsl;
	}
	/**
	 * @param ridSummaryXsl The ridSummaryXsl to set.
	 */
	public void setRIDSummaryXsl(String ridSummaryXsl) {
		this.ridSummaryXsl = ridSummaryXsl;
	}
	/**
	 * @return Returns the useXSLInstruction.
	 */
	public boolean isUseXSLInstruction() {
		return useXSLInstruction;
	}
	/**
	 * @param useXSLInstruction The useXSLInstruction to set.
	 */
	public void setUseXSLInstruction(boolean useXSLInstruction) {
		this.useXSLInstruction = useXSLInstruction;
	}
	
	/**
	 * Set the name of the FileSystemMgtBean.
	 * <p>
	 * This bean is used to retrieve the DICOM object.
	 * 
	 * @param fileSystemMgtName The fileSystemMgtName to set.
	 */
	public void setFileSystemMgtName(ObjectName fileSystemMgtName) {
		RIDSupport.fileSystemMgtName = fileSystemMgtName;
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
	
	/**
	 * @param reqObj
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 * @throws SAXException
	 * @throws TransformerConfigurationException
	 */
	public RIDResponseObject getRIDSummary(RIDRequestObject reqObj) throws SQLException, IOException, TransformerConfigurationException, SAXException {
		Dataset queryDS;
		if (log.isDebugEnabled() ) log.debug(" Summary request type:"+reqObj.getRequestType());
		if ( "SUMMARY-CARDIOLOGY-ECG".equals( reqObj.getRequestType() ) ) {
			return getECGSummary( reqObj );
		} else {
			queryDS = getRadiologyQueryDS( reqObj );
		}
	    IHEDocumentList docList= new IHEDocumentList();
	    initDocList( docList, reqObj, queryDS );
		for ( Iterator iter = getConceptNameCodes().iterator() ; iter.hasNext() ; ) {
        	queryDS.remove( Tags.ConceptNameCodeSeq );//remove for next search.
        	DcmElement cnSq = queryDS.putSQ(Tags.ConceptNameCodeSeq);
        	cnSq.addItem( (Dataset) iter.next() );
        	fillDocList( docList, queryDS );
		}
		queryDS.get( Tags.ConceptNameCodeSeq ).getItem().putCS( Tags.CodeMeaning );//we want code meaning in result (filtering)
		if ( useXSLInstruction ) docList.setXslFile( ridSummaryXsl );
		return new RIDTransformResponseObjectImpl(docList, "text/xml", HttpServletResponse.SC_OK, null);
	}
	
	private RIDResponseObject getECGSummary( RIDRequestObject reqObj ) throws SQLException {
		Dataset queryDS = getECGQueryDS( reqObj );
	    IHEDocumentList docList= new IHEDocumentList();
	    initDocList( docList, reqObj, queryDS );
	    for ( Iterator iter = getECGSopCuids().iterator() ; iter.hasNext() ; ) {
	    	queryDS.putUI( Tags.SOPClassUID, (String) iter.next() );
	    }
    	fillDocList( docList, queryDS );//TODO Is it ok to put all SOP Class UIDs in one query?
		if ( useXSLInstruction ) docList.setXslFile( ridSummaryXsl );
		return new RIDTransformResponseObjectImpl(docList, "text/xml", HttpServletResponse.SC_OK, null);
	}
	
	private void initDocList( IHEDocumentList docList, RIDRequestObject reqObj, Dataset queryDS ) {
	    String reqURL = reqObj.getRequestURL();
	    docList.setReqURL(reqURL);
	    int pos = reqURL.indexOf('?');
	    if ( pos != -1 ) reqURL = reqURL.substring(0, pos);
	    docList.setDocRIDUrl( reqURL.substring( 0, reqURL.lastIndexOf("/") ) );//last path element should be the servlet name! 
	    docList.setQueryDS( queryDS );
	    String docCode = reqObj.getRequestType();
		docList.setDocCode( docCode );
		if ( "SUMMARY".equalsIgnoreCase( docCode )) 
			docList.setDocDisplayName( "List of radiology and cardiology reports");
		else if ("SUMMARY-RADIOLOGY".equals( docCode ) )
			docList.setDocDisplayName( "List of radiology reports");
		else if ("SUMMARY-CARDIOLOGY".equals( docCode ) )
			docList.setDocDisplayName( "List of cardiology reports");
		else if ("SUMMARY-CARDIOLOGY-ECG".equals( docCode ) )
			docList.setDocDisplayName( "List of ECG's");
		
	}
	
	private void fillDocList( IHEDocumentList docList, Dataset queryDS ) throws SQLException {
		QueryCmd qCmd = QueryCmd.create( queryDS, false );
		try {
			qCmd.execute();
		    Dataset ds = factory.newDataset();
			while ( qCmd.next() ) {
				ds = qCmd.getDataset();
				if ( log.isDebugEnabled() ) log.debug("ds:"+ds);
				docList.add( ds );
			}
		} catch ( SQLException x ) {
			qCmd.close();
			throw x;
		}
		qCmd.close();
	}

	/**
	 * @param reqObj
	 * @return
	 */
	private Dataset getRadiologyQueryDS(RIDRequestObject reqObj) {
		String patID = reqObj.getParam( "patientID" );
		Dataset ds = factory.newDataset();
        ds.putCS(Tags.QueryRetrieveLevel, "IMAGE");
		ds.putLO(Tags.PatientID, patID);
		ds.putCS( Tags.Modality, "SR" );
		//Concept name sequence will be used as search criteria. -> within a loop over all radiology specific concept names.
		return ds;
	}


	/**
	 * @param reqObj
	 * @return
	 */
	private Dataset getECGQueryDS(RIDRequestObject reqObj) {
		String patID = reqObj.getParam( "patientID" );
		Dataset ds = factory.newDataset();
        ds.putCS(Tags.QueryRetrieveLevel, "IMAGE");
		ds.putLO(Tags.PatientID, patID);
		return ds;
	}

	/**
	 * @param reqVO
	 * @return
	 */
	public RIDResponseObject getRIDDocument(RIDRequestObject reqObj) {
		Dataset queryDS;
		String docUID = reqObj.getParam("documentUID");
		if ( log.isDebugEnabled() ) log.debug(" Document UID:"+docUID);
		String contentType = reqObj.getParam("preferredContentType");
		if ( contentType == null || ( reqObj.getAllowedContentTypes() != null && ! reqObj.getAllowedContentTypes().contains( contentType))){
			contentType = "application/pdf";
		}
		WADOCache cache = WADOCacheImpl.getRIDCache();
		File outFile = cache.getFileObject(null, null, reqObj.getParam("documentUID"), contentType );
		try {
			if ( !outFile.exists() ) {
				File inFile = getDICOMFile( docUID );
				if ( inFile == null ) {
					return new RIDStreamResponseObjectImpl( null, "text/html", HttpServletResponse.SC_NOT_FOUND, "Object with documentUID="+docUID+ "not found!");
				}
				outFile = renderSRFile( inFile, outFile );
			}
			return new RIDStreamResponseObjectImpl( new FileInputStream( outFile ),contentType, HttpServletResponse.SC_OK, null);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @param f
	 * @return
	 * @throws IOException
	 * @throws TransformerException
	 */
	private File renderSRFile(File inFile, File outFile ) throws IOException, TransformerException {
        outFile.getParentFile().mkdirs();
        File tmpFile = new File( outFile.toString()+".tmp");
        tmpFile.deleteOnExit();
        DataInputStream in = new DataInputStream(new BufferedInputStream(
                new FileInputStream(inFile)));
        SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory.newInstance();
        TransformerHandler th = tf.newTransformerHandler();
        OutputStream tmpOut = new FileOutputStream( tmpFile);
        th.setResult(new StreamResult( tmpOut ));
        DcmParser parser = DcmParserFactory.getInstance().newDcmParser(in);
        parser.setSAXHandler2(th,DictionaryFactory.getInstance().getDefaultTagDictionary(), null, 4000, null );//4000 ~ one text page.
        parser.parseDcmFile(null, -1);
        tmpOut.close();
        
        OutputStream out = new FileOutputStream( outFile );
        fop.setRenderer(Driver.RENDER_PDF);
        fop.setOutputStream( out );
        Templates template = tf.newTemplates(new StreamSource(FOBSR_XSL_URI));
        
        Transformer t = template.newTransformer();
        t.transform(new StreamSource( new FileInputStream(tmpFile)), new SAXResult( fop.getContentHandler() ) );
        out.close();
        tmpFile.delete();
        if ( ! outFile.exists() ) return null;
        return outFile;
	}

	/**
	 * Returns the DICOM file for given arguments.
	 * <p>
	 * Use the FileSystemMgtService MBean to localize the DICOM file.
	 * 
	 * @param instanceUID	Unique identifier of the instance.
	 * 
	 * @return The File object or null if not found.
	 * 
	 * @throws IOException
	 */
	private File getDICOMFile( String instanceUID ) throws IOException {
	    File file;
	    Object dicomObject = null;
		try {
	        dicomObject = server.invoke(fileSystemMgtName,
	                "locateInstance",
	                new Object[] { instanceUID },
	                new String[] { String.class.getName() } );
	        
	    } catch (Exception e) {
	        log.error("Failed to get DICOM file", e);
	    }
	    if ( dicomObject == null ) return null; //not found!
	    if ( dicomObject instanceof File ) return (File) dicomObject; //We have the File!
	    if ( dicomObject instanceof String ) {
	    	log.info("Requested DICOM file is not local! You can retrieve it from:"+dicomObject);
	    }
		return null;
	}
	
}
