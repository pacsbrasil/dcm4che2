package org.dcm4chee.xero.view;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Set;

import org.antlr.stringtemplate.language.ASTExpr;
import org.antlr.stringtemplate.language.NewlineRef;
import org.antlr.stringtemplate.language.StringRef;
import org.antlr.stringtemplate.language.StringTemplateAST;
import org.antlr.stringtemplate.language.ActionEvaluatorTokenTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This names the template to use to handle the given object node type.
 * Unfortunately, nodes aren't sufficiently distinguishable by the view to always call the
 * right template -there isn't a choose method, so this creates one.
 * @author bwallace
 *
 */
public class NameJSTemplate extends HashMap<Object,String> {
   private static final Logger log = LoggerFactory.getLogger(NameJSTemplate.class);

   public NameJSTemplate() {
	  put(ActionEvaluatorTokenTypes.ANONYMOUS_TEMPLATE,"anonymousTemplate");
	  put(ActionEvaluatorTokenTypes.CONDITIONAL, "conditionalTemplate");
   }
   
   /** Gets the name of the template to use for the given object type. */
   @Override
   public String get(Object key) {
	  String ret = super.get(key);
	  if( ret!=null ) return ret;
	  return "ast";
   }
   
   @Override
   public boolean containsKey(Object key) {
	  return true;
   }
   
   public void put(int key, String value) {
	  this.put(Integer.toString(key),value);
   }
}
