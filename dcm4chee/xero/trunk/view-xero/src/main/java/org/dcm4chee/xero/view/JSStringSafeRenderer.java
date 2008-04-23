package org.dcm4chee.xero.view;

import java.util.HashMap;
import java.util.Map;

import org.antlr.stringtemplate.AttributeRenderer;
import org.antlr.stringtemplate.language.NewlineRef;
import org.antlr.stringtemplate.language.StringRef;

public class JSStringSafeRenderer implements AttributeRenderer {
   
   /** A common set of renderers that render to JS safe strings */
   public static Map RENDERERS = new HashMap();
   static {
	  JSStringSafeRenderer j = new JSStringSafeRenderer();
	  RENDERERS.put(String.class, j);
	  RENDERERS.put(StringRef.class, j);
	  RENDERERS.put(NewlineRef.class, j);
   }

   public String toString(Object o) {
	  String src = o.toString();
	  StringBuffer ret = new StringBuffer(src.length()+2);
	  for(int i=0, n=src.length(); i<n; i++) {
		 char ch = src.charAt(i);
		 switch(ch) {
		 case '\n':
			ret.append("\\n");
			break;
		 case '\r':
			break;
		 case '\\':
			ret.append("\\\\");
			break;
		 case '"':
			ret.append("\\\"");
			break;
		 case '\'':
			ret.append("\\'");
			break;
		 default:
			ret.append(ch);
		 }
	  }
	  return ret.toString();
   }

   public String toString(Object o, String formatName) {
	  return toString(o);
   }

}
