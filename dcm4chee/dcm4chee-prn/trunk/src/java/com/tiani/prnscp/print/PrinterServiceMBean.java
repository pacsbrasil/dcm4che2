/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2002 by TIANI MEDGRAPH AG                                  *
 *                                                                           *
 *  This file is part of dcm4che.                                            *
 *                                                                           *
 *  This library is free software; you can redistribute it and/or modify it  *
 *  under the terms of the GNU Lesser General Public License as published    *
 *  by the Free Software Foundation; either version 2 of the License, or     *
 *  (at your option) any later version.                                      *
 *                                                                           *
 *  This library is distributed in the hope that it will be useful, but      *
 *  WITHOUT ANY WARRANTY; without even the implied warranty of               *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU        *
 *  Lesser General Public License for more details.                          *
 *                                                                           *
 *  You should have received a copy of the GNU Lesser General Public         *
 *  License along with this library; if not, write to the Free Software      *
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA  *
 *                                                                           *
 *****************************************************************************/

package com.tiani.prnscp.print;

import org.dcm4che.data.Dataset;
import org.jboss.system.ServiceMBean;

import javax.management.ObjectName;

/**
 * <description>
 *
 * @see <related>
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @version $Revision$
 * @since November 3, 2002
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>yyyymmdd author:</b>
 * <ul>
 * <li> explicit fix description (no line numbers but methods) go
 *            beyond the cvs commit message
 * </ul>
 */
public interface PrinterServiceMBean extends ServiceMBean {
   
   int NORMAL  = 1;
   int WARNING = 2;
   int FAILURE = 3;
      
   /** Getter for property printerCalibration.
    * @return Value of property printerCalibration.
    */
   ObjectName getPrinterCalibration();
   
   /** Setter for property printerCalibration.
    * @param printerCalibration New value of property printerCalibration.
    */
   void setPrinterCalibration(ObjectName printerCalibration);
   
   /** Getter for property status.
    * @return Value of property status.
    */
   int getStatusID();
   
   /** Getter for code string for property status.
    * @return String value of property status.
    */
   String getStatus();
       
   /** Getter for property statusInfo.
    * @return Value of property statusInfo.
    */
   String getStatusInfo();
   
   /** Getter for property queueName.
    * @return Value of property queueName.
    */
   String getQueueName();
   
   /** Setter for property queueName.
    * @param queueName New value of property queueName.
    */
   void setQueueName(String queueName);
   
   /** Getter for property printerConfiguration.
    * @return Value of property printerConfiguration.
    */
   Dataset getPrinterConfiguration();
   
   /** Returns number of image boxes on film box for specifed image display format.
    * @param imageDisplayFormat image display format
    * @return number of image boxes on film box.
    */
   int countImageBoxes(String imageDisplayFormat);
   
   /** Getter for property printerConfigurationFile.
    * @return Value of property printerConfigurationFile.
    */
   String getPrinterConfigurationFile();
   
   /** Setter for property printerConfigurationFile.
    * @param printerConfigurationFile New value of property printerConfigurationFile.
    */
   void setPrinterConfigurationFile(String printerConfigurationFile);
   
}
