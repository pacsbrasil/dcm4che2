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

 /** 
  * Contains method to read objects etc.constructor
  * Defines readFile(path [, characterCoding]) if not already defined, as well as
  * readXml(path), returning an XML object.
  */

var mvnPath = "";
var jsDir = new java.io.File("src/test/js");
if( jsDir.isDirectory() ) {
	mvnPath = "src/test/js/";
} else {
	jsDir = new java.io.File("xero-war/src/test/js");
	if( jsDir.isDirectory() ) {
		mvnPath = "xero-war/src/test/js/";
	}
}
/** Returns the contents of path as XML
 */
function readXml(path) {
	var fin = new java.io.LineNumberReader(new java.io.FileReader(mvnPath+path));
    var str = "";
    var ch = fin.readLine();
    while(ch!==null) {
    	str = str+ch;
    	ch = fin.readLine();
    }
    fin.close();
	var parser = new DOMParser();
	var doc=parser.parseFromString(str,"text/xml");
	return doc;
};

