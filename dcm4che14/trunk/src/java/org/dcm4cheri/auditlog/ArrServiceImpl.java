package org.dcm4cheri.auditlog;

import java.io.*;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;

import javax.swing.text.DateFormatter;

//import javax.xml.*;
import javax.xml.parsers.*;

import org.xml.sax.*;
import org.xml.sax.ext.*;
import org.xml.sax.helpers.DefaultHandler;

import org.dcm4che.auditlog.ArrService;
import org.dcm4che.auditlog.ArrInputException;
import org.dcm4che.util.ISO8601DateFormat;

/**
 * @author joseph foraci
 *
 * ArrServiceImpl
 */
final class ArrServiceImpl extends DefaultHandler implements ArrService {
	private static final HashSet eventSet = new HashSet(Arrays.asList(new String[]
		{
			"Import","InstancesStored","ProcedureRecord","ActorStartStop","ActorConfig","Export",
			"DICOMInstancesDeleted","PatientRecord","OrderRecord","BeginStoringInstances",
			"InstancesSent","DICOMInstancesUsed","StudyDeleted","DicomQuery","SecurityAlert",
			"UserAuthenticated","AuditLogUsed","NetworkEntry"
		}));

	private static final String E_ROOT = "IHEYr4";
	private static final String E_TIMESTAMP = "TimeStamp";
	private static final String E_HOST = "Host";
    
	private SAXParserFactory f = SAXParserFactory.newInstance();
	private SAXParser parser = null;

    private int retcode;

	private String type = null;
	private String host = null;
	private Date timeStamp = null;

	private StringBuffer chrBuff = null;
	private String lastElem = null;

	//for xml schema, does not work with the crimson sax ref impl
	/*private static final String JAXP_SCHEMA_LANGUAGE =
	"http://java.sun.com/xml/jaxp/properties/schemaLanguage";
	private static final String W3C_XML_SCHEMA =
	"http://www.w3.org/2001/XMLSchema";*/

	public ArrServiceImpl(boolean validating)
		throws ArrInputException
	{
		try {
			f.setNamespaceAware(true);
			f.setValidating(validating);
				parser = f.newSAXParser();
			//parser.setProperty(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
		}
		catch (SAXException se) {
			throw new ArrInputException("SAX parser exception", se);
		}
		catch (ParserConfigurationException pce) {
			throw new ArrInputException("SAX parser conf exception", pce);
		}
	}

	/**
	* @see org.dcm4che.arr.ArrService#parse(File)
	*/
	public int parse(File file)
		throws ArrInputException
	{
		try {
			return parse(new FileInputStream(file));
		}
		catch (FileNotFoundException fnf) {
			throw new ArrInputException("File not found", fnf);
		}
	}

	/**
	* @see org.dcm4che.arr.ArrService#parse(String)
	*/
	public int parse(String str)
		throws ArrInputException
	{
		return parse(new ByteArrayInputStream(str.getBytes()));
	}

	/**
	* Handles most of the parsing details.
	*
	* @see org.dcm4che.arr.ArrService#parse(InputStream)
	*/
	public int parse(InputStream is)
		throws ArrInputException
	{
        timeStamp = null;
        host = null;
        type = null;
        retcode = 0;
		try {
			parser.parse(is, this);
		}
		catch (IOException ioe) {
			throw new ArrInputException("I/O exception", ioe);
		}
		catch (SAXException se) {
			throw new ArrInputException("SAX parser exception", se);
		}
        finally {
            //check for unparsed elements and set bitfields appropriately
            if (type==null || host==null || timeStamp==null)
                retcode = retcode | ArrService.INVALID_INCOMPLETE;
            return retcode;
        }
	}

	public void startDocument()
		throws SAXException
	{
        //printCharBuff();
	}
	public void endDocument()
		throws SAXException
	{
        //printCharBuff();
	}

	public void startElement(String nsURI,
		String sname, String qname,
		Attributes attrs)
		throws SAXException
	{
        //printCharBuff();
		String el = sname;
		if ("".equals(el))
			el = qname;
		if (lastElem != null && lastElem.equals(E_ROOT) &&
			eventSet.contains(el))
			type = el;
		lastElem = el;
		/*System.out.print("<" + el);
		if (attrs != null) {
			for (int i=0; i<attrs.getLength(); i++) {
				System.out.print(" " + attrs.getQName(i) + "=\"" + attrs.getValue(i) + "\"");
			}
		}
		System.out.print(">");*/
	}
	public void endElement(String nsURI,
		String sname, String qname)
		throws SAXException
	{
        //printCharBuff();
        lastElem = null;
		String el = sname;
		if ("".equals(el))
			el = qname;
		//System.out.print("</" + el + ">");
	}

    public void characters(char[] buf, int offset, int len)
        throws SAXException
    {
    	String s = new String(buf, offset, len);
		if (lastElem != null && lastElem.equals(E_TIMESTAMP)) {
			ISO8601DateFormat isoDateFmt = new ISO8601DateFormat();
			isoDateFmt.setLenient(true); //should be default anyway
			timeStamp = isoDateFmt.parse(s.trim(), new ParsePosition(0));
		}
		if (lastElem != null && lastElem.equals(E_HOST)) {
			host = s.trim();
		}
        /*if (chrBuff == null)
            chrBuff = new StringBuffer();
        chrBuff.append(s);*/
    }

    private void printCharBuff()
    {
        if (chrBuff != null)
            System.out.print(chrBuff);
        chrBuff = null;
    }

    /**
     * Treat errors as invalid schema
     */
    public void error(SAXParseException spe)
        throws SAXException
    {
        //System.out.println("\n<<"+e.toString()+">>");
        retcode = retcode | ArrService.INVALID_SCHEMA;
    }
    
    /**
     * Treat any fatal error as violation of well-formed contraint.
     * Throw SAX parser exception back up to SAXParser::parse() invoker
     */
    public void fatalError(SAXParseException spe)
        throws SAXException
    {
        retcode = retcode | ArrService.INVALID_XML;
        throw spe;
    }

    public String getType()
    {
    	return type;
    }

    public String getHost()
    {
    	return host;
    }

    public Date getTimeStamp()
    {
    	return timeStamp;
    }
}
