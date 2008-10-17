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
package org.dcm4chee.xero.rhino;

import java.util.Map;

import org.apache.struts.flow.sugar.SugarWrapFactory;
import org.dcm4chee.xero.metadata.MetaData;
import org.dcm4chee.xero.metadata.access.MapFactory;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

/**
 * A JavaScriptMapFactory is an object that knows how to export a JavaScript execution environment
 * into a MapWithDefaults.  Execution context for the JavaScript can be multi-layered.  The parent
 * layer is specified by name.
 * 
 * Additionally, the child script is executed with the current src model available as
 * JavaScriptMapFactory.  This allows variables from other contexts to be used.
 *
 * The base context that is created includes by default the Struts Sugar access.  This can be
 * turned off if required. 
 *  
 * @author bwallace
 *
 */
public class JavaScriptMapFactory implements MapFactory<JavaScriptObjectWrapper> {
	
	boolean useSugarWrap = true;
	
	String parentScopeName;
	
	boolean modifiable = true;
	
	String script;
	
	ContextFactory contextFactory = new ContextFactory();
	
	public static final SugarWrapFactory SUGAR_WRAP_FACTORY = new SugarWrapFactory();
	
	
	/** Creates a JavaScriptObjectWrapper containing a new scope in which the given script has 
	 * been executed (if provided), and for which the parent scope has been set.
	 * Adds the sugar wrap if the setting is true. 
	 */
	public JavaScriptObjectWrapper create(Map<String, Object> src) {
	   Context cx = contextFactory.enterContext();
	   if( useSugarWrap ) cx.setWrapFactory(SUGAR_WRAP_FACTORY);
	   Scriptable scope;
	   if( parentScopeName!=null ) {
	   	Scriptable parentScope = getParentScope(src);
	   	scope = cx.newObject(parentScope);
		   scope.setPrototype(parentScope);
	   } else {
	   	scope = cx.initStandardObjects();
	   }
	   
	   if( script!=null ) cx.evaluateString(scope, script, "<script>", 1, null);
	   if( !modifiable ) ((ScriptableObject)scope).sealObject();
	   return new JavaScriptObjectWrapper(scope);
   }

	/** Gets the parent context */
	protected Scriptable getParentScope(Map<String, Object> src) {
		Object ret = src.get(parentScopeName);
		if( ret instanceof JavaScriptObjectWrapper ) return ((JavaScriptObjectWrapper) ret).scriptable;
		return (Scriptable) ret;		
   }

	public boolean isUseSugarWrap() {
   	return useSugarWrap;
   }

	@MetaData(required=false)
	public void setUseSugarWrap(boolean useSugarWrap) {
   	this.useSugarWrap = useSugarWrap;
   }

	public String getParentScopeName() {
   	return parentScopeName;
   }

	@MetaData(required=false)
	public void setParentScopeName(String parentScopeName) {
   	this.parentScopeName = parentScopeName;
   }

	public boolean isModifiable() {
   	return modifiable;
   }

	@MetaData(required=false)
	public void setModifiable(boolean modifiable) {
   	this.modifiable = modifiable;
   }

	public String getScript() {
   	return script;
   }

	@MetaData(required=false)
	public void setScript(String script) {
   	this.script = script;
   }

}
