package org.dcm4chee.xero.wado;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dcm4chee.xero.metadata.servlet.ServletResponseItem;

public class ErrorServletResponseItem implements ServletResponseItem {

   String message;
   int code;
   
   /** Setup a response indicating an error. */
   public ErrorServletResponseItem(int code, String message) {
	  this.message = message;
	  this.code = code;
   }
   
   /** Actually write the error response. */
   public void writeResponse(HttpServletRequest request, HttpServletResponse response) throws IOException {
	  response.sendError(code, message);
   }

}
