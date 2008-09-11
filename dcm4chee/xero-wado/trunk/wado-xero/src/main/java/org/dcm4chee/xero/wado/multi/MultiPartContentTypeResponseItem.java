/**
 * 
 */
package org.dcm4chee.xero.wado.multi;

import java.io.IOException;
import java.util.Iterator;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dcm4chee.xero.metadata.servlet.ServletResponseItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  This class stores an array of object UID's and uses a set of filters to determine what responses to encode.
 *  
 *  The filters are called once for every requested object, with some of them being called for every frame of every requested
 *  object.  This does require some intimate knowledge of the details of the DICOM objects being returned.
 *  
 *  In general, all the headers are passed to the child objects, except the ones explicitly listed in the child filter's meta-data
 *  to exclude or replace (exclude is done by specifying "_exclude" as the parameter value.)  Filter that return "null" will 
 *  write no data, otherwise an error will be written as a mime element, and the rest of the items will continue to be written.
 *  
 *  This class is fairly tightly bound with MultiPartContentTypeFilter, with most of the non-multipart logic being contained in that
 *  filter rather than here.  This design allows other types of multi-part responses to be created in the future such as ZIP encoded files.
 */
public class MultiPartContentTypeResponseItem implements MultiPartHandler
{
	private static Logger log = 
		LoggerFactory.getLogger(MultiPartContentTypeResponseItem.class);

	private static final String boundaryStr = "boundaryID78871780-c7c5-41bc-9aae-347a307ab660";
	protected static final String BOUNDARY_START = "--" + boundaryStr;
	protected static final String BOUNDARY_END = "--" + boundaryStr + "--";
	/** Store the iterator that is used to get the responses.
	 */
	private Iterator<ServletResponseItem> multiResponses;
	
	/** Store the iterator that is used to get the responses.  An iterator is used instead of a list because the memory size and computational
	 * complexity of computing the entire list up front may exceed the available memory size or allowed response time for the output.  As well, 
	 * asynchronous item generation and writing of the thread can be implemented by a set of these response items.
	 * @return 
	 */
	public void setResponseIterator(Iterator<ServletResponseItem> multiResponses) {
		this.multiResponses = multiResponses;
	}
	
	/** Get the response iterator for the Servlet responses - don't touch this iterator 
	 * in terms of using the next method as that will remove responses from the queue.
	 * @return
	 */
	public Iterator<ServletResponseItem> getResponseIterator() {
		return this.multiResponses;
	}
	
	/**
	 * Write the multi-part response to the provided stream.
	 * 
	 * @param httpRequest
	 *            unused
	 * @param response
	 *            that the multipart/mixed results are written to. Also sets the content type.
	 */
	public void writeResponse(HttpServletRequest httpRequest, HttpServletResponse response) throws IOException {
		
		if (multiResponses==null || ! multiResponses.hasNext())
		{
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "Requested content not found.");
			log.warn("Requested content not found.");
			return;
		}

		response.setContentType("multipart/mixed;boundary=" + boundaryStr);

		ServletOutputStream os = response.getOutputStream();

		int errors = 0;
		// each child *can't* be allowed to modify the response headers by itself.
		while( multiResponses.hasNext())  
		{
			ServletResponseItem sri = multiResponses.next();
			// This is perfectly acceptable to return a null response - it just means that particular content type isn't found.
			if( sri==null ) continue;
			
			writeStartBoundary( os );
			try {

				BodyResponseWrapper bodyResponseWrapper = new BodyResponseWrapper(response);
				sri.writeResponse(httpRequest, bodyResponseWrapper);
				bodyResponseWrapper.flushBuffer();

			} catch ( Exception e ) {
				log.warn("response for content type=\"" + response.getContentType() + "has failed");
				errors++;
				if( errors>3 ) {
					log.warn("3rd error on request - returning without completing request.");
					os.close();
					return;
				}
			}
		}

		writeEndBoundary( os );
		
		if( os != null )
			os.close();
	}

	protected void writeStartBoundary( ServletOutputStream os ) throws IOException
	{			
		os.println();
		os.println(BOUNDARY_START);
	}
	
	protected void writeEndBoundary( ServletOutputStream os ) throws IOException
	{
		os.println();
		os.println(BOUNDARY_END);
		os.flush();		
	}
}
