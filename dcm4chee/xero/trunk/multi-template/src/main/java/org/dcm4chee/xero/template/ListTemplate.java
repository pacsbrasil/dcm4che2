package org.dcm4chee.xero.template;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/** Handle a list of template items */
public class ListTemplate extends Template {

   List<Template> templates = new ArrayList<Template>();
   
   /** Merge all the templates in the list of templates into the output response. */
   public void merge(TemplateContext context, Writer writer) throws IOException {
	  for(Template t : templates) {
		 t.merge(context,writer);
	  }
   }
   
   /** Returns the list of child-templates to render.  This should be setup as part of intiialization
    * and then not touched, as it isn't thread safe.
    * @return
    */
   public List<Template> getTemplates() {
	  return templates;
   }
   
   /** Crushes the child templates, combining text templates and list templates */
   public Template crush() {
	  int n = templates.size();
	  int lastText = 0;
	  for(int i=0; i<n; i++) {
		 Template child = templates.get(i);
		 if( child instanceof ListTemplate ) {
			ListTemplate lchild = (ListTemplate) child;
		    templates.remove(i);
			n--;
			templates.addAll(i, lchild.templates);
			n += lchild.templates.size();
			// redo the element at this position, if there is still anything here.
			i--;
			continue;
		 }
		 if( ! ( child instanceof TextTemplate ) ) {
			if( lastText<i-1 ) {
			   int c = combineText(lastText,i);
			   i = i-c;
			   n = n-c;
			}
			lastText = i+1;
		 }
	  }
	  
	  if( lastText < n-1 ) {
		 int c = combineText(lastText,n);
		 n = n-c;
	  }
	  
	  if( n==1 ) return templates.get(0);
	  if( n==0 ) return null;
	  return this;
   }

   /** This method combines text nodes from [i,j) and returns the number of nodes removed j-i-2 */
   public int combineText(int i, int j) {
	  if( i<0 ) i = 0;
	  if( j > templates.size() ) j = templates.size();
	  if( j<=i+2 ) return 0;
	  StringBuffer buf = new StringBuffer(((TextTemplate) templates.get(i)).getText());
	  for(int k=i+1; k<j; k++ ) {
		 buf.append( ((TextTemplate) templates.get(k)).getText() );
	  }
	  TextTemplate combined = new TextTemplate(buf.toString());
	  templates.subList(i+1,j).clear();
	  templates.set(i,combined);
	  return j-i-2;
   }
}
