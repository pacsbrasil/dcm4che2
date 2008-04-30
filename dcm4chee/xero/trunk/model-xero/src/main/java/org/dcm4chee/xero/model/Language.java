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
package org.dcm4chee.xero.model;

/**
 * Defines the language to use for a given request
 * 
 * @author bwallace
 */
public class Language {
   public static String LANGUAGE = "lang";

   String cannonicalLanguage;

   String originalLanguage;

   public String getCannonicalLanguage() {
      return cannonicalLanguage;
   }

   public String getOriginalLanguage() {
      return originalLanguage;
   }

   /**
    * Given an accept language string, define the canonical language - that is,
    * the one that is used for strings and rendering pre-defined objects, and
    * the original language - the one that is used for formatters. It is
    * entirely possible that there are more formatters than there are language
    * definitions, which is why there are the two string types. A language is
    * considered canonical if there is at least 1 definition in the specific
    * language, otherwise, the nearest parent language is chosen. For example,
    * suppose there is en_ca_on - an Ontario variant of Canadian English. There
    * might well be en_ca that defines the Canadian English, but there is
    * unlikely to be an Ontario variant, so the choice would be en_ca. Currently
    * the ONLY canonical variant is en.
    * 
    * @param acceptLanguages
    *            is the header comma separated language string.
    * @param language
    *            is a single string containing the selected language. This
    *            overrides the header.
    */
   public Language(String acceptLanguages, String language) {
	  cannonicalLanguage = "en";
	  if (language != null) {
		 originalLanguage = language;
	  } else {
		  if (acceptLanguages != null) {
			 int comma = acceptLanguages.indexOf(",");
			 if (comma > 0)
				acceptLanguages = acceptLanguages.substring(0, comma);
			 int semi = acceptLanguages.indexOf(';');
			 if (semi > 0)
				acceptLanguages = acceptLanguages.substring(0, semi);
			 cannonicalLanguage = acceptLanguages.trim();
		  }
		  else originalLanguage = cannonicalLanguage;
	  }
   }
}
