package org.dcm4chee.xero.wado;

import static org.dcm4chee.xero.wado.WadoParams.CONTENT_DISPOSITION;

import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dcm4chee.xero.metadata.servlet.ServletResponseItem;

/** Returns the entire byte array as specified */
public class ByteServletResponseItem implements ServletResponseItem {

	protected byte[] data;
	protected String mimeType;
	protected String filename;
	
	
	public ByteServletResponseItem(byte[] data, String mimeType,String filename) {
	    this(data,mimeType);
	    this.filename = filename;
	}
	
	public ByteServletResponseItem(byte[] data, String mimeType) {
		this.data = data;
		this.mimeType = mimeType;
	}
	
	public void writeResponse(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		response.setContentType(mimeType);
	    if( filename!=null ) response.setHeader(CONTENT_DISPOSITION, "inline;filename="+filename);
		response.setContentLength(this.data.length);
		ServletOutputStream os = response.getOutputStream();
		os.write(data);
		os.close();
	}

}
