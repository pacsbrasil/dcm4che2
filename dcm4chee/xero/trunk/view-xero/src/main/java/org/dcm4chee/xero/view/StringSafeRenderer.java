package org.dcm4chee.xero.view;

import java.util.HashMap;
import java.util.Map;

import org.antlr.stringtemplate.AttributeRenderer;
import org.antlr.stringtemplate.language.NewlineRef;
import org.antlr.stringtemplate.language.StringRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StringSafeRenderer implements AttributeRenderer {
   private static final Logger log = LoggerFactory.getLogger(StringSafeRenderer.class);
   
   String defaultFormat;
   
   public StringSafeRenderer() {
   }
   
   public StringSafeRenderer(String defaultFormat) {
	  this.defaultFormat = defaultFormat;
   }
   
   /** A common set of renderers that render to JS safe strings */
   public static Map<Class<?>,AttributeRenderer> RENDERERS = new HashMap<Class<?>,AttributeRenderer>();
   static {
	  StringSafeRenderer j = new StringSafeRenderer();
	  RENDERERS.put(String.class, j);
	  RENDERERS.put(StringRef.class, j);
	  RENDERERS.put(NewlineRef.class, j);
   }

   public static Map<Class<?>,AttributeRenderer> JS_RENDERERS = new HashMap<Class<?>,AttributeRenderer>();
   static {
	  StringSafeRenderer j = new StringSafeRenderer("js");
	  JS_RENDERERS.put(String.class, j);
	  JS_RENDERERS.put(StringRef.class, j);
	  JS_RENDERERS.put(NewlineRef.class, j);
   }

   public String toString(Object o) {
	  return toString(o,defaultFormat);
   }
   
   /** JavaScript safe version */
   public static String toJSString(Object o) {
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
	  if( formatName==null ) return o.toString();
	  if( formatName.equals("js") ) return toJSString(o);
	  if( formatName.equals("xml") ) return toXmlString(o);
	  log.warn("Unknown format name {}", formatName);
	  return o.toString();
   }

   public static String toXmlString(Object o) {
	  String src = o.toString();
	  boolean changed = false;
	  for(int i=0, n=src.length(); i<n; i++) {
		 char ch = src.charAt(i);
		 if( ch=='&' || ch=='<' ) {
			changed=true;
			break;
		 }
	  }
	  if( !changed ) return src;
	  StringBuffer ret = new StringBuffer(src.length()+2);
	  for(int i=0, n=src.length(); i<n; i++) {
		 char ch = src.charAt(i);
		 switch(ch) {
		 case '&':
			changed = true;
			ret.append("&amp;");
			break;
		 case '<':
			changed =true;
			ret.append("&lt;");
			break;
		 default:
			ret.append(ch);
		 }
	  }
	  return ret.toString();
   }

}
