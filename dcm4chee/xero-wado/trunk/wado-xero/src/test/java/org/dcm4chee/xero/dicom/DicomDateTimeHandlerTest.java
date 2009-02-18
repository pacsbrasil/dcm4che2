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

import static org.testng.Assert.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class DicomDateTimeHandlerTest
{
   private DicomDateTimeHandler handler;
   
   @BeforeMethod
   public void setup()
   {
      DateFormat dateOnlyFormat = new SimpleDateFormat("yyyy.MM.dd");
      DateFormat dateTimeFormat = new SimpleDateFormat("yyyy.MM.dd HH.mm.ss SSS");
      handler = new DicomDateTimeHandler(dateTimeFormat,dateOnlyFormat);
   }
   
   /**
    * Test method for {@link org.dcm4chee.xero.dicom.DicomDateTimeHandler#formatDicomDateTime(java.lang.String)}.
    */
   @Test
   public void testFormatDicomDateTime()
   {
      String dateTime = "20020107091828.046000";
      String dateTimeF = handler.formatDicomDateTime(dateTime);
      assertEquals(dateTimeF,"2002.01.07 09.18.28 046", 
            "Must format the date according to the format passed in constructor");
   }

   /**
    * Test method for {@link org.dcm4chee.xero.dicom.DicomDateTimeHandler#createDateTime(java.lang.String, java.lang.String)}.
    */
   @Test
   public void testCreateDateTime()
   {
      String date = "20020107";
      String time = "091828.046000";
      
      String dateTime = handler.createDateTime(date, time);
      assertEquals(dateTime,"20020107091828.046000");
   }

   @Test
   public void testCreateDateTime_ShouldReturnNull_IfDateIsNull()
   {
      String time = "091828.046000";
      
      assertNull(handler.createDateTime(null, time));
   }
   
   @Test
   public void testCreateDateTime_ShouldReturnDate_IfTimeIsNull()
   {
      String date = "20020107";
      
      assertEquals(handler.createDateTime(date, null),date);
   }
   
   @Test(expectedExceptions=IllegalArgumentException.class)
   public void testConstructor_ShouldRejectNullDateFormat()
   {
      new DicomDateTimeHandler(null,null);
   }
   
   @Test
   public void testFormatDicomDateTime_ShouldOmitTheFormattedTime_WhenNoTimeField()
   {
      String dateTime = "20020107";
      String dateTimeF = handler.formatDicomDateTime(dateTime);
      assertEquals(dateTimeF,"2002.01.07", 
            "Must format the date according to the format passed in constructor");
   }
   
}
