package org.dcm4chee.xero.xslt;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;

/** This class can be run to perform the XSLT transform specified by the arguments, in
 * order to validate the XSLT etc. 
 * Usage: java org.dcm4chee.xero.xslt.TestXslt [xmlurl] [xslturl] 
 */
public class TestXslt {

	    /**
	     * Performs an XSLT transformation, sending the results
	     * to System.out.
	     */
	    public static void main(String[] args) throws Exception {
	        if (args.length != 2) {
	            System.err.println(
	                "Usage: java Transform [xmlfile] [xsltfile]");
	            System.exit(1);
	        }

	        // JAXP reads data using the Source interface
	        Source xmlSource = new StreamSource(args[0]);
	        Source xsltSource = new StreamSource(args[1]);

	        // the factory pattern supports different XSLT processors
	        TransformerFactory transFact =
	                TransformerFactory.newInstance();
	        transFact.setErrorListener(new TestXsltErrorListener());
	        Transformer trans = transFact.newTransformer(xsltSource);

	        trans.transform(xmlSource, new StreamResult(System.out));
	    }
}

class TestXsltErrorListener implements ErrorListener
{

	public void error(TransformerException exception) throws TransformerException {
		exception.printStackTrace();
	}

	public void fatalError(TransformerException exception) throws TransformerException {
		exception.printStackTrace();
		throw exception;
	}

	public void warning(TransformerException exception) throws TransformerException {
		exception.printStackTrace();
	}
}