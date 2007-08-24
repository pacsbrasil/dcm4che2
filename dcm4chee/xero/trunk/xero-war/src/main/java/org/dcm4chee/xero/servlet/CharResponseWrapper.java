package org.dcm4chee.xero.servlet;

import java.io.CharArrayWriter;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/** Copied (partially) from java.sun.com example. */
public class CharResponseWrapper extends HttpServletResponseWrapper {
   private CharArrayWriter output;

   public String toString() {
	  return output.toString();
   }

   public CharResponseWrapper(HttpServletResponse response) {
	  super(response);
	  output = new CharArrayWriter();
   }

   public PrintWriter getWriter() {
	  return new PrintWriter(output);
   }
}
