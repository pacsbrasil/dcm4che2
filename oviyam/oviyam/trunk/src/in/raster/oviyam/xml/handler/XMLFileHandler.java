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
 * The Original Code is part of Oviyam, an web viewer for DICOM(TM) images
 * hosted at http://skshospital.net/pacs/webviewer/oviyam_0.6-src.zip
 *
 * The Initial Developer of the Original Code is
 * Raster Images
 * Portions created by the Initial Developer are Copyright (C) 2014
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Babu Hussain A
 * Devishree V
 * Meer Asgar Hussain B
 * Prakash J
 * Suresh V
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

package in.raster.oviyam.xml.handler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import org.apache.log4j.Logger;

/**
 * 
 * @author asgar
 */
public class XMLFileHandler {

	// Initialize logger
	private static Logger log = Logger.getLogger(XMLFileHandler.class);

	public String getXMLFilePath(String tmpDir) {		
		String xmlFilePath = this.getClass().getResource("/conf/oviyam2-1-config.xml").getPath();
        String fname = "oviyam2-1-config.xml";
        String retValue = null;
        
       // if(xmlFilePath.indexOf("default") > 0) {
            try {
                File srcFile = new File(this.getClass().getResource("/conf/oviyam2-1-config.xml").toURI());
                //retValue = xmlFilePath.substring(0, xmlFilePath.indexOf("default")) + "default" + File.separator + fname;
                retValue = tmpDir + File.separator + fname;
                File destFile = new File(retValue);
                //check the exists of XML file. If not exists, copy the file to default folder.
                if (!destFile.exists()) {
                    copyFile(srcFile, destFile);
                }
            } catch (URISyntaxException ex) {
                log.error("Error while getting XML file path",ex);
                return "";
            }
        //}
                
        return retValue;
	}

	private void copyFile(File src, File dest) {
		FileInputStream in = null;
		FileOutputStream out = null;
		try {
			in = new FileInputStream(src);
			out = new FileOutputStream(dest);
			byte[] buffer = new byte[1024];
			int len;
			while ((len = in.read(buffer)) > 0) {
				out.write(buffer, 0, len);
			}
		} catch (Exception ex) {
			log.error("Error while copying XML File", ex);
		} finally {
			try {
				in.close();
				out.close();
			} catch (IOException ex) {
				log.error("Error while closing file", ex);
			}
		}
	}

	public void createXMLFile(String tmpDir) {
		try {			
			// Version 2.0 configuration file
			File config2_0 = new File(tmpDir + File.separator
					+ "oviyam2-config.xml");
			String retValue = tmpDir + File.separator + "oviyam2-1-config.xml";
			File destFile = new File(retValue); // version 2.1

			if (!destFile.exists()) {
				if (config2_0.exists()) { // Upgrade settings from 2.0 to 2.1
					log.info("Upgrading data from 2.0 to 2.1");
					copyFile(config2_0, destFile);
				} else { // New installation
					File srcFile = new File(this.getClass()
							.getResource("/conf/oviyam2-1-config.xml").toURI());
					// check the exists of XML file. If not exists, copy the
					// file to default folder.
					if (!destFile.exists()) {
						copyFile(srcFile, destFile);
					}
				}
			}
		} catch (URISyntaxException ex) {
			log.error("Error while getting XML file path", ex);
			System.out.println("Trying to get location : " + tmpDir
					+ File.separator + "oviyam2-1-config.xml");
		}
	}
	
	public boolean isInstallationRequired(String tmpDir) {
		File config2_1 = new File(tmpDir + File.separator + "oviyam2-1-config.xml");
		if(!config2_1.exists()) {
			return true;
		}
		LanguageHandler.source = config2_1;
		return false;
	}
	
	public boolean isUpgrade(String tmpDir) {
		File config2_0 = new File(tmpDir + File.separator + "oviyam2-config.xml");
		if(config2_0.exists()) {
			return true;
		}
		return false;
	}
	
	public void installNew(String tmpDir) {
		try {			
			File srcFile = new File(this.getClass().getResource("/conf/oviyam2-1-config.xml").toURI());
			File destFile = new File(tmpDir + File.separator + "oviyam2-1-config.xml");
			copyFile(srcFile, destFile);
			LanguageHandler.source = destFile;			
		} catch (URISyntaxException e) {
			System.out.println("Unable to install new version");
			e.printStackTrace();
		}
	}

	public void upgrade(String tmpDir) {
		File config2_0 = new File(tmpDir + File.separator + "oviyam2-config.xml");
		File config2_1 = new File(tmpDir + File.separator + "oviyam2-1-config.xml");
		copyFile(config2_0, config2_1);
		LanguageHandler.source = config2_1;
	}
}