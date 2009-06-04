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
package org.dcm4chee.xero.dicom;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import org.dcm4che2.util.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class that handles the parsing and formatting of DICOM date/time fields.
 * <p>
 * @author Andrew Cowan (andrew.cowan@agfa.com)
 */
public class DicomDateTimeHandler
{
   private static Logger log = LoggerFactory.getLogger(DicomDateTimeHandler.class);
   
   private final DateFormat dateOnlyFormat;
   private final DateFormat dateTimeFormat;
   
   public DicomDateTimeHandler(DateFormat dateTimeFormat, DateFormat dateOnlyFormat)
   {
      if(dateTimeFormat == null || dateOnlyFormat == null) 
         throw new IllegalArgumentException("DateFormatS must be specified");
      
      this.dateTimeFormat = dateTimeFormat;
      this.dateOnlyFormat = dateOnlyFormat;
   }
   
   public DicomDateTimeHandler()
   {
      this(DateFormat.getDateTimeInstance(),DateFormat.getDateInstance());
   }
   
   /** Formats a dateTimeString which is a concatenation of a DICOM date and time.
    * Returns null if unable to properly parse dateTimeString.
    */
   public String formatDicomDateTime(String dateTimeString) {
      if(dateTimeString == null)
         return null;
      
      String formattedDateTime;
      try {
         Date dateTime = DateUtils.parseDT(dateTimeString, false);
         formattedDateTime = formatDateOrDateTime(dateTime, dateTimeString.length() > 8);
      } catch (Exception nfe) {
         log.warn("Illegal study date or time:" + nfe);
         formattedDateTime = null;
      }
      
      return formattedDateTime;
   }
   
   /**
    * Format the indicated Date instance with either a date or date time formatter.
    * <p>
    * This method will handle synchronization of the internal DateFormat instances internally.
    */
   private String formatDateOrDateTime(Date dateTime, boolean includeTime)
   {
      String formattedDateTime = null;
      DateFormat format = includeTime ? dateTimeFormat : dateOnlyFormat;
      synchronized(format)
      {
         formattedDateTime = format.format(dateTime);
      }
      
      return formattedDateTime;
   }

   /** Simple concatenation of date & time parameters.
    * @return Date string if time is null, null if both arguments are null.
    */
   public String createDateTime(String date, String time) {
      String dateTime = null;
      if (date!= null) {
         dateTime = date + (time == null ? "" : time);
      }
      return dateTime;
   }
   
   /**
    * Format the indicated Java Date instance as a DICOM date.
    * @return DICOM formatted date or "" if null
    */
   public String toDicomDate(Date date) {
      if(date == null) return "";

      return DateUtils.formatDA(date);
   }
   
   /**
    * Format the indicated Java Calendar instance as a DICOM date.
    * @return DICOM formatted date or "" if null.
    */
   public String toDicomDate(Calendar cal) {
      if(cal == null) return "";
      
      return toDicomDate(cal.getTime());
   }
}
