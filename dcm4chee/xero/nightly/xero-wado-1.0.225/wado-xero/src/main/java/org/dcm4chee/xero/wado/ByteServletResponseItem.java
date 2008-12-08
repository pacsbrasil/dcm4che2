package org.dcm4chee.xero.wado;

import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dcm4chee.xero.metadata.servlet.ServletResponseItem;

/** Returns the entire byte array as specified */
public class ByteServletResponseItem implements ServletResponseItem {

	protected byte[] data;
	protected String mimeType;
	
	public ByteServletResponseItem(byte[] data, String mimeType) {
		this.data = data;
		this.mimeType = mimeType;
	}
	
	public void writeResponse(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		response.setContentType(mimeType);
		response.setContentLength(this.data.length);
		ServletOutputStream os = response.getOutputStream();
		os.write(data);
		os.close();
	}

}
