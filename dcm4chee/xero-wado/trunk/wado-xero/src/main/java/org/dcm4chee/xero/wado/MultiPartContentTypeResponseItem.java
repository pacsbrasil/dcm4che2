/**
 * 
 */
package org.dcm4chee.xero.wado;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dcm4chee.xero.metadata.servlet.ServletResponseItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  This class stores a collection of ServletResponseItems and will
 *  write out a multi-part response when {@link #writeResponse(HttpServletRequest, HttpServletResponse)}
 *  is called.
 *
 */
public class MultiPartContentTypeResponseItem implements ServletResponseItem 
{
	private static Logger log = 
		LoggerFactory.getLogger(MultiPartContentTypeResponseItem.class);

	private static final String boundaryStr = "boundaryID78871780-c7c5-41bc-9aae-347a307ab660";
	protected static final String BOUNDARY_START = "--" + boundaryStr;
	protected static final String BOUNDARY_END = "--" + boundaryStr + "--";

	private List<ServletResponseItem> responseList = new ArrayList<ServletResponseItem>();

	public void add(ServletResponseItem sri) 
	{
		if ( sri == null ) 
		{
			log.warn("response item for multipart response is null");
			return;
		}
		
		responseList.add(sri);
	}
	
	public List<ServletResponseItem> get() 
	{
		return Collections.unmodifiableList(responseList);
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
		
		if (responseList.isEmpty())
		{
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "Requested content not found.");
			log.warn("Requested content not found.");
			return;
		}

		response.setContentType("multipart/mixed;boundary=" + boundaryStr);

		ServletOutputStream os = response.getOutputStream();

		// each child *can't* be allowed to modify the response headers by itself.
		for ( ServletResponseItem sri: responseList ) 
		{
			writeStartBoundary( os );
			try {
				sri.writeResponse(httpRequest, new BodyResponseWrapper(response));
			} catch ( Exception e ) {
				log.warn("response for content type=\"" + response.getContentType() + "has failed");
			}
		}

		writeEndBoundary( os );
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
