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
