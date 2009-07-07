// ***** BEGIN LICENSE BLOCK *****
// Version: MPL 1.1/GPL 2.0/LGPL 2.1
// 
// The contents of this file are subject to the Mozilla Public License Version 
// 1.1 (the "License"); you may not use this file except in compliance with 
// the License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
// 
// Software distributed under the License is distributed on an "AS IS" basis,
// WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
// for the specific language governing rights and limitations under the
// License.
// 
// The Original Code is part of dcm4che, an implementation of DICOM(TM) in Java(TM), hosted at http://sourceforge.net/projects/dcm4che
//  
// The Initial Developer of the Original Code is Agfa Healthcare.
// Portions created by the Initial Developer are Copyright (C) 2009 the Initial Developer. All Rights Reserved.
// 
// Contributor(s):
// Andrew Cowan <andrew.cowan@agfa.com>
// 
// Alternatively, the contents of this file may be used under the terms of
// either the GNU General Public License Version 2 or later (the "GPL"), or
// the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
// in which case the provisions of the GPL or the LGPL are applicable instead
// of those above. If you wish to allow use of your version of this file only
// under the terms of either the GPL or the LGPL, and not to allow others to
// use your version of this file under the terms of the MPL, indicate your
// decision by deleting the provisions above and replace them with the notice
// and other provisions required by the GPL or the LGPL. If you do not delete
// the provisions above, a recipient may use your version of this file under
// the terms of any one of the MPL, the GPL or the LGPL.
// 
// ***** END LICENSE BLOCK *****
package org.dcm4chee.xero.metadata.servlet;

import javax.servlet.http.HttpServletResponse;


/**
 * Simple factory class that will create an appropriate ErrorResponseItem for
 * an indicated Exception type.
 * <p>
 * TODO: Localize error codes that will be seen by the user.
 * TODO: Provide a way to turn off the printing of errors for users.
 * @author Andrew Cowan (andrew.cowan@agfa.com)
 */
public class ErrorResponseItemFactory
{
   /**
    * Generate an appropriate servlet response item for the indicated error.
    */
   public ErrorResponseItem getResponseItem(Exception e)
   {
      ErrorResponseItem response;
      if(e instanceof SecurityException)
         response = createForbiddenError(e);
      else if( e instanceof ResponseException)
          response = createResponseException((ResponseException) e);
      else
         response = createInternalServerError(e);
      
      return response;
   }
   
   public ErrorResponseItem createInternalServerError(Exception e)
   {
      int code = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
      String message = "Internal server error.  ";
      if(e!=null) message += e;
      return new ErrorResponseItem(code,message);
   }
   
   public ErrorResponseItem createForbiddenError(Exception e)
   {
      int code = HttpServletResponse.SC_FORBIDDEN;
      String message = "Access to resource has been denied.  ";
      if(e!=null) message += e;
      return new ErrorResponseItem(code,message);
   }
   
   public ErrorResponseItem createResponseException(ResponseException e)
   {
      int code = e.getCode();
      String message = e.getMessage();
      return new ErrorResponseItem(code,message);
   }
   
   public ErrorResponseItem createNotFoundError(String resource)
   {
      int code = HttpServletResponse.SC_NOT_FOUND;
      String message = "No content found for this request.  ";
      if(resource!=null) message += resource;
      return new ErrorResponseItem(code,message);
   }
}
