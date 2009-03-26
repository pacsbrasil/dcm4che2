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
 * Portions created by the Initial Developer are Copyright (C) 2008
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
package org.dcm4chee.xero.wado;

/**
 * Contains public static constants for the various parameters for WADO and
 * for search queries generally.
 * 
 * @author bwallace
 */
public class WadoParams {
	// Keys that have UID values
	public static final String STUDY_UID = "studyUID";
	public static final String SERIES_UID = "seriesUID";
	public static final String OBJECT_UID = "objectUID";
	public static final String TRANSFER_SYNTAX="transferSyntax";
	public static final String PRESENTATION_UID="presentationUID";

	// Common/standard keys that are other types of values
   public static final String CONTENT_TYPE = "contentType";
	public static final String ROWS = "rows";
	public static final String COLUMNS = "cols";
	public static final String REGION = "region";
	public static final String SIMPLE_FRAME_LIST="simpleFrameList";
	public static final String FRAME_NUMBER="frameNumber";
	
	// Custom keys 
	public static final String MULTIPART_KEY = "multipart";

	/** The separator to use when specifying a colour */
	public static final String COLOUR_SEPARATOR = ":";
	
   /** Keys for headers to return */
   public static final String CONTENT_DISPOSITION="Content-Disposition";
   public static final String AE = "ae";
   public static final String MODALITY = "modality";
   
   /** Use the original, raw format.  Disallows changes to the returned object
    * such as size/rotation etc.
    */
   public static final String USE_ORIG = "useOrig";
}
