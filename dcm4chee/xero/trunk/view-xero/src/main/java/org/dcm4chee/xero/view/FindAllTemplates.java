package org.dcm4chee.xero.view;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.ASTExpr;
import org.antlr.stringtemplate.language.ActionEvaluatorTokenTypes;
import org.antlr.stringtemplate.language.StringRef;

import antlr.collections.AST;

/**
 * This class is used to find a list of all templates, starting with the given set of named templates.
 * 
 * @author bwallace
 */
public class FindAllTemplates {

   /** Recursively iterate through all the templates to find all possibly templates. */
   public static Map<String,StringTemplate> findAllTemplates(StringTemplateGroup stg, List<String> startingTemplates) {
	  Map<String,StringTemplate> ret = new HashMap<String,StringTemplate>();
	  for(String name : startingTemplates) {
		 addAllTemplates(stg, name,ret);
	  }
	  return ret;
   }
   
   /** Add the given template and all child templates to the template map */
   static void addAllTemplates(StringTemplateGroup stg, String name, Map<String,StringTemplate> templates) {
	  if( templates.containsKey(name) ) return;
	  StringTemplate st = stg.getInstanceOf(name);
	  templates.put(name,st);
	  
	  for(Object chunk : st.getChunks() ) {
		 if( chunk instanceof StringRef ) continue;
		 ASTExpr astexpr = (ASTExpr) chunk;
		 AST ast = astexpr.getAST();
		 addAllTemplates(stg, ast, templates);
	  }
   }
   
   static void addAllTemplates(StringTemplateGroup stg, AST ast, Map<String,StringTemplate> templates) {
	  if( ast==null ) return;
	  if( ast.getType()==ActionEvaluatorTokenTypes.INCLUDE || ast.getType()==ActionEvaluatorTokenTypes.TEMPLATE) {
		 AST includeNameAst = ast.getFirstChild();
		 if( includeNameAst.getType()==ActionEvaluatorTokenTypes.ID ) {
			addAllTemplates(stg,includeNameAst.getText(),templates);
			return;
		 }
	  }
	  addAllTemplates(stg,ast.getFirstChild(),templates);
	  addAllTemplates(stg,ast.getNextSibling(),templates);
   }
   
}
