/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Bill Wallace, Agfa HealthCare Inc., 
 * Portions created by the Initial Developer are Copyright (C) 2007
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Bill Wallace <bill.wallace@agfa.com>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

package org.dcm4chee.xero.dicom;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.net.CommandUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that will read the DICOM response code and throw a detailed exception if the status is
 * currently in error.
 * <p>
 * This class borrows heavily from the Agility client classes that are listed below.
 * <p>
 * 
 * @see {@link com.agfa.pacs.data.dicom.comm.DicomSCU#handleStatus}
 * @see {@link com.agfa.pacs.data.dicom.comm.DicomCMoveSCU#handleStatus}
 * @author Andrew Cowan (amidx)
 */
public class DicomCommandChecker
{
   private static Logger log = LoggerFactory.getLogger(DicomCommandChecker.class);

   /**
    * Determine whether the command has completed successfully or throw an exception if there is an
    * error.
    * 
    * @param cmd
    *           The DICOM response command from the Response object
    * @return Whether the command has completed successfully. False means that more data is pending.
    * @throws DicomException
    *            If there is an error code.
    */
   public boolean isPending(DicomObject cmd) throws DicomException
   {
      checkStatusCode(cmd);
      return CommandUtils.isPending(cmd);
   }


   /**
    * Determine if the the command was successful or not.
    * 
    * @param cmd
    *           DICOM response command.
    * @return Whether the command was successful.
    * @throws DicomException
    *            thrown if the command is in error.
    */
   public boolean isSuccess(DicomObject cmd) throws DicomException
   {
      int status = checkStatusCode(cmd);
      return status == CommandUtils.SUCCESS;
   }


   /**
    * Check the status code embedded in the indicated DicomObject.
    * 
    * @param cmd
    *           DICOM command object
    * @return The status code.
    * @throws DicomException
    *            if there is a non-successful status code.
    */
   protected int checkStatusCode(DicomObject cmd) throws DicomException
   {
      if (cmd == null)
         throw new IllegalArgumentException("Dicom command is null");

      if (!cmd.contains(Tag.Status))
         throw new IllegalArgumentException("No status tag in DICOM command");

      int statusCode = cmd.getInt(Tag.Status);
      if(statusCode != CommandUtils.SUCCESS && !CommandUtils.isPending(cmd))
      {
         checkBasicErrorCodes(cmd);
         checkCMoveErrorCodes(cmd);
         
         // Unknown error.  Throw what information we have....
         String errorComment = cmd.getString(Tag.ErrorComment);
         log.error("FAILURE: Unable to process.");
         log.error("Error Comment: {}",errorComment );
         String hexValue = Integer.toHexString(statusCode);
         throw new DicomException(statusCode, "Unknown error code: 0x"+hexValue+", error comment="+errorComment);
      }
      
      return statusCode;
   }
   
   /**
    * Check for the basic DICOM error codes.
    */
   protected void checkBasicErrorCodes(DicomObject cmd)
      throws DicomException
   {
      int statusCode = cmd.getInt(Tag.Status);
      
      switch (statusCode)
      {
      case 0x0105:
      {
         log.error("Status: " + Integer.toHexString(statusCode));
         log.error("FAILURE: No such attribute");
         throw new DicomException(statusCode, "FAILURE: No such attribute");
      }
      case 0x0106:
      {
         log.error("Status: " + Integer.toHexString(statusCode));
         log.error("FAILURE: Invalid attribute value");
         throw new DicomException(statusCode, "FAILURE: Invalid attribute value");
      }
      case 0x0107:
      {
         log.warn("Status: " + Integer.toHexString(statusCode));
         log.warn("WARN: Attribute list error");
         log.warn("Affected SOP Instance UID: " + cmd.getString(Tag.AffectedSOPInstanceUID));
         break;
      }
      case 0x0110:
      {
         log.error("Status: " + Integer.toHexString(statusCode));
         log.error("FAILURE: Processing failure");
         log.error("Error Comment: " + cmd.getString(Tag.ErrorComment));
         log.error("Affected SOP Instance UID: " + cmd.getString(Tag.AffectedSOPInstanceUID));
         log.error("Error ID: " + cmd.getString(Tag.ErrorID));
         throw new DicomException(statusCode, "FAILURE: Processing failure");
      }
      case 0x0111:
      {
         log.error("Status: " + Integer.toHexString(statusCode));
         log.error("FAILURE: Duplicate SOP instance");
         log.error("Affected SOP Instance UID: " + cmd.getString(Tag.AffectedSOPInstanceUID));
         throw new DicomException(statusCode, "FAILURE: Duplicate SOP instance");
      }
      case 0x0112:
      {
         log.error("Status: " + Integer.toHexString(statusCode));
         log.error("FAILURE: No such object instance");
         log.error("Affected SOP Instance UID: " + cmd.getString(Tag.AffectedSOPInstanceUID));
         log.error("Event Type ID: " + cmd.getString(Tag.EventTypeID));
         throw new DicomException(statusCode, "FAILURE: No such object instance");
      }
      case 0x0113:
      {
         log.error("Status: " + Integer.toHexString(statusCode));
         log.error("FAILURE: No such event type");
         log.error("Affected SOP Class UID: " + cmd.getString(Tag.AffectedSOPClassUID));
         log.error("Event Type ID: " + cmd.getString(Tag.EventTypeID));
         throw new DicomException(statusCode, "FAILURE: No such event type");
      }
      case 0x0114:
      {
         log.error("Status: " + Integer.toHexString(statusCode));
         log.error("FAILURE: No such argument");
         log.error("Affected SOP Class UID: " + cmd.getString(Tag.AffectedSOPClassUID));
         log.error("Event Type ID: " + cmd.getString(Tag.EventTypeID));
         log.error("Action Type ID: " + cmd.getString(Tag.ActionTypeID));
         throw new DicomException(statusCode, "FAILURE: No such argument");
      }
      case 0x0115:
      {
         log.error("Status: " + Integer.toHexString(statusCode));
         log.error("FAILURE: Invalid argument value");
         log.error("Affected SOP Class UID: " + cmd.getString(Tag.AffectedSOPClassUID));
         log.error("Affected SOP Instance UID: " + cmd.getString(Tag.AffectedSOPInstanceUID));
         log.error("Event Type ID: " + cmd.getString(Tag.EventTypeID));
         log.error("Action Type ID: " + cmd.getString(Tag.ActionTypeID));
         throw new DicomException(statusCode, "FAILURE: Invalid argument value");
      }
      case 0x0116:
      {
         log.warn("Status: " + Integer.toHexString(statusCode));
         log.warn("WARN: Attribute Value out of range");
         break;
      }
      case 0x0117:
      {
         log.error("Status: " + Integer.toHexString(statusCode));
         log.error("FAILURE: Invalid object instance");
         log.error("Affected SOP Instance UID: " + cmd.getString(Tag.AffectedSOPInstanceUID));
         throw new DicomException(statusCode, "FAILURE: Invalid object instance");
      }
      case 0x0118:
      {
         log.error("Status: " + Integer.toHexString(statusCode));
         log.error("FAILURE: No such SOP class");
         log.error("Affected SOP Class UID: " + cmd.getString(Tag.AffectedSOPClassUID));
         throw new DicomException(statusCode, "FAILURE: No such SOP class");
      }
      case 0x0119:
      {
         log.error("Status: " + Integer.toHexString(statusCode));
         log.error("FAILURE: Class-instance conflict");
         log.error("Error Comment: " + cmd.getString(Tag.ErrorComment));
         log.error("Requested SOP Instance UID: " + cmd.getString(Tag.RequestedSOPInstanceUID));
         throw new DicomException(statusCode, "FAILURE: Class-instance conflict");
      }
      case 0x0120:
      {
         log.error("Status: " + Integer.toHexString(statusCode));
         log.error("FAILURE: Missing attribute");
         throw new DicomException(statusCode, "FAILURE: Missing attribute");
      }
      case 0x0121:
      {
         log.error("Status: " + Integer.toHexString(statusCode));
         log.error("FAILURE: Missing attribute value");
         throw new DicomException(statusCode, "FAILURE: Missing attribute value");
      }
      case 0x0122:
      {
         log.error("Status: " + Integer.toHexString(statusCode));
         log.error("FAILURE: Refused: SOP class not supported");
         log.error("Error Comment: " + cmd.getString(Tag.ErrorComment));
         throw new DicomException(statusCode, "FAILURE: Refused: SOP class not supported");
      }
      case 0x0123:
      {
         log.error("Status: " + Integer.toHexString(statusCode));
         log.error("FAILURE: No such action type");
         log.error("Affected SOP Class UID: " + cmd.getString(Tag.AffectedSOPClassUID));
         log.error("Action Type ID: " + cmd.getString(Tag.ActionTypeID));
         throw new DicomException(statusCode, "FAILURE: No such action type");
      }
      case 0x0210:
      {
         log.error("Status: " + Integer.toHexString(statusCode));
         log.error("FAILURE: Duplicate invocation");
         throw new DicomException(statusCode, "FAILURE: Duplicate invocation");
      }
      case 0x0211:
      {
         log.error("Status: " + Integer.toHexString(statusCode));
         log.error("FAILURE: Unrecognized operation");
         throw new DicomException(statusCode, "FAILURE: Unrecognized operation");
      }
      case 0x0212:
      {
         log.error("Status: " + Integer.toHexString(statusCode));
         log.error("FAILURE: Mistyped argument");
         throw new DicomException(statusCode, "FAILURE: Mistyped argument");
      }
      case 0x0213:
      {
         log.error("Status: " + Integer.toHexString(statusCode));
         log.error("FAILURE: Resource limmitation");
         throw new DicomException(statusCode, "FAILURE: Resource limmitation");
      }
      case 0xC001:
      {
         log.error("Status: " + Integer.toHexString(statusCode));
         log.error("FAILURE: unable to process");
         throw new DicomException(statusCode, "FAILURE: Unable to process.");
      
      }
      }
   }


   /**
    * Check for C-MOVE error codes in the indicated DicomObject.
    * @param cmd DicomObject containing the DICOM command.
    * @throws DicomException
    */
   protected void checkCMoveErrorCodes(DicomObject cmd) throws DicomException
   {
      int status = cmd.getInt(Tag.Status);
      switch (status)
      {
      case 0xA701:
      {
         log.error("Status: {} ",Integer.toHexString(status));
         log.error("FAILURE: Out of Resources - unable to calculate number of matches");
         log.error("Error Comment: {}",cmd.getString(Tag.ErrorComment));
         throw new DicomException(status, "FAILURE: Out of Resources - unable to calculate number of matches");
      }
      case 0xA702:
      {
         log.error("Status: {}", Integer.toHexString(status));
         log.error("FAILURE: Out of Resources - unable to perform sub-operations");
         log.error("Error Comment: {}", cmd.getString(Tag.ErrorComment));
         throw new DicomException(status, "FAILURE: Out of Resources - unable to calculate number of matches");
      }
      case 0xA801:
      {
         log.error("Status: " + Integer.toHexString(status));
         log.error("FAILURE: Refused: Move destination unknown");
         log.error("Error Comment: " + cmd.getString(Tag.ErrorComment));
         throw new DicomException(status, "FAILURE: Refused: Move destination unknown");
      }
      case 0xA900:
      {
         log.error("Status: " + Integer.toHexString(status));
         log.error("FAILURE: Identifier does not match SOP Class.");
         log.error("Error Comment: " + cmd.getString(Tag.ErrorComment));
         throw new DicomException(status, "FAILURE: Identifier does not match SOP Class.");
      }
      case 0xFE00:
      {
         log.info("Status: " + Integer.toHexString(status));
         log.info("CANCEL: Request was cancelled.");
         break;
      }
      }
   }

//   private void showTags(int[] tags)
//   {
//     if (tags == null) return;
//     for (int tag : tags)
//     {
//        StringBuffer sb = new StringBuffer();
//        sb.append("Tag: ");
//        sb.append(TagUtils.toString(tag));
//        sb.append(" ");
//        sb.append(ElementDictionary.getDictionary().nameOf(tag));
//        log.info(sb.toString());
//     }
//   }
//   
//   private void showNumbers(DicomObject cmd)
//   {
//       if (log.isDebugEnabled())
//       {
//           if (cmd.contains(Tag.NumberOfRemainingSuboperations)) log.debug("Remaining Sub-operations: " + cmd.getInt(Tag.NumberOfRemainingSuboperations));
//           if (cmd.contains(Tag.NumberOfCompletedSuboperations)) log.debug("Completed Sub-operations: " + cmd.getInt(Tag.NumberOfCompletedSuboperations));
//           if (cmd.contains(Tag.NumberOfFailedSuboperations))
//           {
//               if (cmd.getInt(Tag.NumberOfFailedSuboperations)!= 0)
//                   log.debug("Failed Sub-operations: " + cmd.getInt(Tag.NumberOfFailedSuboperations));
//           }
//           if (cmd.contains(Tag.NumberOfWarningSuboperations)) 
//           {
//               if (cmd.getInt(Tag.NumberOfWarningSuboperations)!= 0)
//                   log.debug("Warning Sub-operations: " + cmd.getInt(Tag.NumberOfWarningSuboperations));
//           }
//       }
//   }
}
