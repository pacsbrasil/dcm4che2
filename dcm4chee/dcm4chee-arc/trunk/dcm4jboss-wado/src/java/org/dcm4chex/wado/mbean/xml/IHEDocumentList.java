/*
 * Created on 13.01.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.dcm4chex.wado.mbean.xml;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.PersonName;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;


/**
 * @author franz.willer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class IHEDocumentList implements XMLResponseObject{

	private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyyMMdd");
	private static final SimpleDateFormat DATETIME_FORMATTER = new SimpleDateFormat("yyyyMMddHHmmss");
	private static final AttributesImpl EMPTY_ATTRIBUTES = new AttributesImpl();
	
	private static Logger log = Logger.getLogger( IHEDocumentList.class.getName() );
	
	private List datasets = new ArrayList();
	private Dataset queryDS = null;
    private TransformerHandler th = null;
	
	private String docCode;
	private String docCodeSystem;
	private String docDisplayName;
	
	private Date lowerDateTime = null;
	private Date upperDateTime = null;
	private int mostRecentResults = 0;
	
	private String xslFile;
	/** the request URL which is used from client to get this document list (with query string!). */
	private String reqURL = "";
	private String docRIDUrl = "http://localhost:8080/dcm4jboss-wado";
	private URL xslt;
	
	public IHEDocumentList() {
		
	}
	
	public IHEDocumentList( Collection datasets ) {
		addAll( datasets );
	}
	
	public void setQueryDS( Dataset ds ) {
		queryDS = ds;
	}
	
	public boolean add( Dataset ds ) {
		if ( ds == null ) return false;
			return datasets.add( ds );
	}
	
	/**
	 * @param ds
	 * @return
	 */
	private void applyMostRecentResults() {
		Collections.sort( datasets, new DatasetDateComparator() );
		if ( mostRecentResults > 0 && datasets.size() > mostRecentResults ) {
			datasets.subList( mostRecentResults, datasets.size() ).clear();//Remains mostRecentResults items in list; removes all older dataset
		}
	}

	/**
	 * @param ds
	 * @return
	 */
	private Date getDateFromDS(Dataset ds) {
		Date d = ds.getDateTime( Tags.ContentDate, Tags.ContentTime );
		if ( d == null )
			d = ds.getDate( Tags.AcquisitionDatetime );
		return d;
	}

	public void addAll( Collection col ) {
		if ( col == null || col.isEmpty() ) return;
		for ( Iterator iter = col.iterator() ; iter.hasNext() ; ) {
			add( (Dataset) iter.next() );
		}
	}
	
	public int size() {
		return datasets.size();
	}
	
	/**
	 * @return Returns the docCode.
	 */
	public String getDocCode() {
		return docCode;
	}
	/**
	 * @param docCode The docCode to set.
	 */
	public void setDocCode(String docCode) {
		this.docCode = docCode;
	}
	/**
	 * @return Returns the docCodeSystem.
	 */
	public String getDocCodeSystem() {
		return docCodeSystem;
	}
	/**
	 * @param docCodeSystem The docCodeSystem to set.
	 */
	public void setDocCodeSystem(String docCodeSystem) {
		this.docCodeSystem = docCodeSystem;
	}
	/**
	 * @return Returns the docDisplayName.
	 */
	public String getDocDisplayName() {
		return docDisplayName;
	}
	/**
	 * @param docDisplayName The docDisplayName to set.
	 */
	public void setDocDisplayName(String docDisplayName) {
		this.docDisplayName = docDisplayName;
	}
	/**
	 * @return Returns the lowerDateTime.
	 */
	public Date getLowerDateTime() {
		return lowerDateTime;
	}
	/**
	 * @param lowerDateTime The lowerDateTime to set.
	 */
	public void setLowerDateTime(Date lowerDateTime) {
		this.lowerDateTime = lowerDateTime;
	}
	/**
	 * @return Returns the mostRecentResults.
	 */
	public int getMostRecentResults() {
		return mostRecentResults;
	}
	/**
	 * @param mostRecentResults The mostRecentResults to set.
	 */
	public void setMostRecentResults(int mostRecentResults) {
		this.mostRecentResults = mostRecentResults;
	}
	/**
	 * @return Returns the upperDateTime.
	 */
	public Date getUpperDateTime() {
		return upperDateTime;
	}
	/**
	 * @param upperDateTime The upperDateTime to set.
	 */
	public void setUpperDateTime(Date upperDateTime) {
		this.upperDateTime = upperDateTime;
	}
	/**
	 * @return Returns the xslFile.
	 */
	public String getXslFile() {
		return xslFile;
	}
	/**
	 * @param xslFile The xslFile to set.
	 */
	public void setXslFile(String xslFile) {
		this.xslFile = xslFile;
	}
	/**
	 * @return Returns the xslt.
	 */
	public URL getXslt() {
		return xslt;
	}
	/**
	 * Set the URL to an xsl file that is used to transform the xml result of this DocumentList.
	 * 
	 * @param xslt The xslt to set.
	 */
	public void setXslt(URL xslt) {
		this.xslt = xslt;
	}
	/**
	 * @param reqURL The reqURL to set.
	 */
	public void setReqURL(String reqURL) {
		this.reqURL = reqURL;
	}
	/**
	 * @return Returns the docRIDUrl.
	 */
	public String getDocRIDUrl() {
		return docRIDUrl;
	}
	/**
	 * @param docRIDUrl The docRIDUrl to set.
	 */
	public void setDocRIDUrl(String docRIDUrl) {
		this.docRIDUrl = docRIDUrl;
	}
	public void toXML( OutputStream out ) throws TransformerConfigurationException, SAXException {
			        SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory.newInstance();

        applyMostRecentResults();//sorts the list and ( if mostRecentResults > 0 ) shrink the list.
        
        if (xslt != null) {
        	try {
        		th = tf.newTransformerHandler(new StreamSource(xslt.openStream(),
                    xslt.toExternalForm()));
        	} catch ( IOException x ) {
        		log.error("Cant open xsl file:"+xslt, x );
        	}
            Transformer t = th.getTransformer();
        } else {
        	th = tf.newTransformerHandler();
        	th.getTransformer().setOutputProperty(OutputKeys.INDENT, "yes");
        }
        th.setResult( new StreamResult(out) );
        th.startDocument();
        if ( xslFile != null ) {
        	th.processingInstruction("xml-stylesheet", "href='"+xslFile+"' type='text/xsl'");
        }
        startElement("IHEDocumentList", EMPTY_ATTRIBUTES );
        addDocCode( );
        addActivityTime( );
        Dataset ds;
        if ( datasets.size() > 0 ) {
        	ds = (Dataset) datasets.get( 0 );
        } else {
        	if ( queryDS != null ) {
        		ds = queryDS;
        	} else {
        		ds = DcmObjectFactory.getInstance().newDataset();
        		ds.putLO(Tags.PatientID, "");
        	}
        }
    	addRecordTarget( ds );
        addAuthor();
        addDocuments();
        endElement("IHEDocumentList");
        th.endDocument();
	    try {
			out.flush();
			out.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
        
	}

	/**
	 * @throws SAXException
	 */
	private void addDocCode() throws SAXException {
		AttributesImpl attr = new AttributesImpl();
		if ( docCode != null ) 
			addAttribute(attr, "code", docCode );
		if ( docCodeSystem != null ) 
			addAttribute(attr, "codeSystem", docCodeSystem );
		if ( docDisplayName != null ) 
			addAttribute(attr, "displayName", docDisplayName );
		startElement("code", attr );
		endElement("code");
	}
	/**
	 * @throws SAXException
	 */
	private void addActivityTime() throws SAXException {
		AttributesImpl attr = new AttributesImpl();
		addAttribute( attr, "value", DATETIME_FORMATTER.format( new Date() ));
		startElement( "activityTime", attr );
		endElement( "activityTime");
	}

	/**
	 * @throws SAXException
	 */
	private void addRecordTarget(Dataset ds) throws SAXException {
        startElement("recordTarget", EMPTY_ATTRIBUTES );
         startElement("patient", EMPTY_ATTRIBUTES);
          //patient id
		  AttributesImpl attrsPatID = new AttributesImpl();
		  addAttribute( attrsPatID, "root", ds.getString( Tags.IssuerOfPatientID ) );//issuer id
		  addAttribute( attrsPatID, "extension", ds.getString( Tags.PatientID ));//patient id within issuer
		  startElement("id", attrsPatID );
		  endElement("id");
		  //patientPatient
		  addPatientPatient( ds );
		  startElement( "providerOrganization", EMPTY_ATTRIBUTES);
		  	AttributesImpl attrsOrgID = new AttributesImpl();
		  	addAttribute( attrsOrgID, "id", "");//TODO where can i get the id?
			startElement("id", attrsOrgID );
			endElement("id");
			startElement("name", EMPTY_ATTRIBUTES );
			  String orgName = ds.getString( Tags.InstitutionName );//TODO Institution name correct?
			  if ( orgName != null )
			  	th.characters( orgName.toCharArray(), 0, orgName.length() );
			endElement("name");			
		  endElement( "providerOrganization" );
		 endElement("patient");
        endElement("recordTarget" );
	}

	/**
	 * @throws SAXException
	 */
	private void addPatientPatient( Dataset ds ) throws SAXException {
        String familyName = "";
        String givenName = "";
        String genderCode = "121103"; // Code Value for Patient’s Sex 'O' (Undetermined sex)
        String birthDate = "";
        try {
        	PersonName pn = ds.getPersonName(Tags.PatientName );
        	if ( pn != null ) {
	        	familyName = pn.get( PersonName.FAMILY );
	        	givenName = pn.get( PersonName.GIVEN );
	        	if ( givenName == null ) givenName = "";
        	}
        	String s = ds.getString( Tags.PatientSex );
        	if ( "M".equals(s) || "F".equals(s) ) genderCode = s;
        	Date date = ds.getDate( Tags.PatientBirthDate  );
        	if ( date != null )
        		birthDate = DATE_FORMATTER.format( date );
        	
        } catch ( Exception x ) {
        	log.info("Exception getting person informations:", x);
        }
        startElement("patientPatient", EMPTY_ATTRIBUTES );
        //Names
        startElement("name", EMPTY_ATTRIBUTES );
        startElement("family", EMPTY_ATTRIBUTES );
        th.characters(familyName.toCharArray(),0,familyName.length());
        endElement("family" );       
        startElement("given", EMPTY_ATTRIBUTES );
        th.characters(givenName.toCharArray(),0,givenName.length());
        endElement("given" );       
        endElement("name" );
        //genderCode
        AttributesImpl attr = new AttributesImpl();
        addAttribute( attr, "code", genderCode );
        addAttribute( attr, "codeSystem", "1.2.840.10008.2.16.4" );//??
        startElement("administrativeGenderCode", attr );
        endElement("administrativeGenderCode" );
        //birth
        AttributesImpl attrBirth = new AttributesImpl();
        addAttribute( attrBirth, "value", birthDate );
        startElement("birthTime", attrBirth );
        endElement("birthTime" );
       endElement("patientPatient" );
	}
	
	private void addAuthor() throws SAXException {
		//TODO
        startElement("author", EMPTY_ATTRIBUTES );
        startElement("noteText", EMPTY_ATTRIBUTES );
          AttributesImpl attr = new AttributesImpl();
          addAttribute( attr, "value", reqURL );
          startElement("reference", attr );
          endElement("reference" );
        endElement("noteText" );       
        startElement("assignedAuthor", EMPTY_ATTRIBUTES );
		  AttributesImpl attrsID = new AttributesImpl();
		  addAttribute( attrsID, "root", "" );//TODO
		  addAttribute( attrsID, "extension", "");//TODO
		  startElement("id", attrsID );
		  endElement("id");
	      startElement("assignedDevice", EMPTY_ATTRIBUTES );
			  AttributesImpl attrsCode = new AttributesImpl();
			  addAttribute( attrsCode, "code", "" );//TODO
			  addAttribute( attrsCode, "codeSystem", "");//TODO
			  addAttribute( attrsCode, "displayName", "");//TODO
			  startElement("code", attrsCode );
			  endElement("code");
	          startElement("manufacturerModelName", EMPTY_ATTRIBUTES );
	          //TODO th.characters("TODO".toCharArray(),0,4);
	          endElement("manufacturerModelName" );       
	          startElement("softwareName", EMPTY_ATTRIBUTES );
	          //TODO th.characters("TODO".toCharArray(),0,4);
	          endElement("softwareName" );       
		  endElement("assignedDevice" );       
        endElement("assignedAuthor" );       
        endElement("author" );
	}
	
	private void addDocuments() throws SAXException {
		for ( Iterator iter = datasets.iterator() ; iter.hasNext() ; ) {
			addComponent( (Dataset) iter.next() );
		}
	}
	
	private void addComponent( Dataset ds ) throws SAXException {
		String uid = ds.getString( Tags.SOPInstanceUID );
		if ( uid == null ) uid = "---";
		Date date = null;
		if ( "SR".equals(ds.getString( Tags.Modality ) ) ) {
			date = ds.getDateTime( Tags.ContentDate, Tags.ContentTime );
		} else {
			date = ds.getDate( Tags.AcquisitionDatetime );
		}
		String acquisTime = "";
		
		if ( date != null ) acquisTime = DATETIME_FORMATTER.format( date );
		String title = "DocumentTitle";
		String link = docRIDUrl+"/IHERetrieveDocument?requestType=DOCUMENT&documentUID="+
						uid + "&preferredContentType=application/pdf";
        startElement("component", EMPTY_ATTRIBUTES );
        startElement("documentInformation", EMPTY_ATTRIBUTES );
        //id
        AttributesImpl attrID = new AttributesImpl();
        addAttribute( attrID, "root", uid );
        startElement("id", attrID );
        endElement("id" );
        //component code (SUMMARY, SUMMARY_RADIOLOGY,..)
        addComponentCode( ds );        
		//title
        startElement("title", EMPTY_ATTRIBUTES );
        th.characters(title.toCharArray(), 0, title.length() );
        endElement("title" );
        //text
        startElement("text", EMPTY_ATTRIBUTES );
        AttributesImpl attrTxt = new AttributesImpl();
        addAttribute( attrTxt, "value", link );
        startElement("reference", attrTxt );
        endElement("reference" );
        endElement("text" );
        //statusCode 
        addComponentStatusCode( ds );
        //effective	time
        AttributesImpl attrEff = new AttributesImpl();
        addAttribute( attrEff, "value", acquisTime );
        startElement("effectiveTime", attrEff );
        endElement("effectiveTime" );
       
        endElement("documentInformation" );
        endElement("component" );
		
	}
	
	/**
	 * @param ds
	 * @throws SAXException
	 */
	private void addComponentStatusCode(Dataset ds) throws SAXException {
        //statusCode /SR: CompletionFlag, Verification flag; ecg: ???
        String statusCode = "";
		if ( "SR".equals(ds.getString( Tags.Modality ) ) ) {
			statusCode = ds.getString( Tags.CompletionFlag);
	        if ( statusCode == null ) statusCode = "";
	        String verifyCode = ds.getString( Tags.VerificationFlag );
	        if ( verifyCode != null ) statusCode += ", "+verifyCode;
		} else {
			//TODO: where get the status of waveform storage!?
		}
        AttributesImpl attrStatusCode = new AttributesImpl();
        addAttribute( attrStatusCode, "code", statusCode );
        addAttribute( attrStatusCode, "codeSystem", "" );
        startElement("statusCode", attrStatusCode );
        endElement("statusCode" );
	}

	/**
	 * @param ds
	 * @throws SAXException
	 */
	private void addComponentCode(Dataset ds) throws SAXException {
        //code SR: aus (0008,1032)	ProcedureCodeSequence Code ?; ECG: from SOP Class UID
        String code = "";
        String codeSystem = "";
        String displayname = "";
		if ( "SR".equals(ds.getString( Tags.Modality ) ) ) {
	        DcmElement elem = ds.get( Tags.ConceptNameCodeSeq );
	        if ( elem != null ) {
	        	Dataset ds1 = elem.getItem(0);
	        	if ( ds1 != null ) {
		            code = ds1.getString(Tags.CodeValue);
		            codeSystem = ds1.getString(Tags.CodingSchemeDesignator);
		            displayname = ds1.getString(Tags.CodeMeaning);
	        	}
	        }
	        if ( displayname == null ) displayname = ds.getString( Tags.StudyDescription );
		} else { //ECG
			String cuid = ds.getString( Tags.SOPClassUID );
			if ( UIDs.TwelveLeadECGWaveformStorage.equals( cuid ) )
				displayname = "12-lead ECG";
			else if ( UIDs.GeneralECGWaveformStorage.equals( cuid ) )
				displayname = "General ECG";
			else if ( UIDs.AmbulatoryECGWaveformStorage.equals( cuid ) )
				displayname = "Ambulatory ECG";
			else if ( UIDs.HemodynamicWaveformStorage.equals( cuid ) )
				displayname = "Hemodynamic";
			else if ( UIDs.CardiacElectrophysiologyWaveformStorage.equals( cuid ) )
				displayname = "Cardiac Electrophysiology";
		}
        AttributesImpl attrCode = new AttributesImpl();
        addAttribute( attrCode, "code", code );
        addAttribute( attrCode, "codeSystem", codeSystem );
        addAttribute( attrCode, "displayName", displayname );
        startElement("code", attrCode );
        endElement("code" );
	}

	private void startElement( String name, Attributes attr ) throws SAXException {
	       th.startElement("", name, name, attr );
	}
	private void endElement( String name ) throws SAXException {
	       th.endElement("", name, name );
	}
	
	private void addAttribute( AttributesImpl attr, String name, String value ) {
		if ( value == null ) return;
		attr.addAttribute("", name, name, "", value);		
	}
	
	public class DatasetDateComparator implements Comparator {

		public DatasetDateComparator() {
			
		}

		/**
		 * Compares the modification time of two File objects.
		 * <p>
		 * Compares its two arguments for order. Returns a negative integer, zero, or a positive integer 
		 * as the first argument is less than, equal to, or greater than the second.
		 * <p>
		 * Throws an Exception if one of the arguments is null or not a Dataset object.
		 *  
		 * @param arg0 	First argument
		 * @param arg1	Second argument
		 * 
		 * @return <0 if arg0<arg1, 0 if equal and >0 if arg0>arg1
		 */
		public int compare( Object arg0, Object arg1 ) {
			Date d1 = getDateFromDS( (Dataset) arg0 );
			Date d2 = getDateFromDS( (Dataset) arg1 );
			return d2.compareTo( d1 );
		}
		
	}
	
}
