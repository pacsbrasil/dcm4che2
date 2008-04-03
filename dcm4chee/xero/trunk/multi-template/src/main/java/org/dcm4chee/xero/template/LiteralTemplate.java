package org.dcm4chee.xero.template;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.xml.sax.Attributes;

/**
 * A literal object is an in-line defined object, mostly used for testing, but useable for other phases as well.
 * The arguments to the object are literal arguments.
 * @author bwallace
 *
 */
public class LiteralTemplate extends TemplateSelfCreator<LiteralTemplate> {

   Map<String,Object> literalSrc = new HashMap<String,Object>();
   String name = "_literal";
   int phase = 0;
   
   /** Assign the current name value the literal object value, at the correct phase */
   @Override
   void merge(TemplateContext context, Writer writer) throws IOException {
	  if( context.getPhase() < phase ) {
		 context.put(name,Phase.LATER);
		 delayMerge(context,writer);
		 return;
	  }
	  Map<String,Object> literal = new HashMap<String,Object>(literalSrc);
	  context.put(name, literal);
	  // TODO - handle child elements and computed values
   }

   @Override
   public LiteralTemplate createInitialTemplate(String uri, String localName, String name, Attributes atts) {
	  LiteralTemplate ret = super.createInitialTemplate(uri, localName, name, atts);
	  for(int i=0, n=atts.getLength(); i<n; i++) {
		 String atName = atts.getLocalName(i);
		 String atValue = atts.getValue(i);
		 ret.literalSrc.put(atName, atValue);
	  }
	  String atPhase = (String) ret.literalSrc.get("phase");
	  if( atPhase!=null ) ret.phase = Integer.parseInt(atPhase);
	  String atName = (String) ret.literalSrc.get("name");
	  if( atName!=null ) ret.name = atName;
	  return ret;
   }

}
