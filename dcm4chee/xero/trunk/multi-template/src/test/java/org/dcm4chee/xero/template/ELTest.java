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
package org.dcm4chee.xero.template;

import java.util.HashMap;
import java.util.Map;

import javax.el.ELContext;
import javax.el.ValueExpression;
import javax.el.VariableMapper;

import org.testng.annotations.Test;

/**
 * Tests some features of El
 * @author bwallace
 *
 */
public class ELTest {

   static ELContext ctx = EL.createELContext();
   static ValueExpression veHello = EL.EXPRESSION_FACTORY.createValueExpression(ctx,"Hello, world.", String.class);
   static ValueExpression veSum =EL.EXPRESSION_FACTORY.createValueExpression(ctx,"${1+2}", Integer.class);
   
   VariableMapper vm = ctx.getVariableMapper();
   
   /**
    * Test creation of simple expressions
    */
   @Test
   public void simpleExpressionTest() {
	  String result = (String) veHello.getValue(ctx);
	  assert result!=null;
	  assert result.equals("Hello, world.");
	  
	  int i = (Integer) veSum.getValue(ctx);
	  assert i==3;
	  
	  // Checks both that we can get a variable and that an expression created in one context can
	  // be evaluated in another
	  SObject s1 = new SObject();
	  s1.s = "Goodbye";
	  
	  ValueExpression veVarS = EL.EXPRESSION_FACTORY.createValueExpression(ctx,"${var.s}", String.class);
	  ValueExpression veVar = EL.EXPRESSION_FACTORY.createValueExpression(ctx,"${var}", SObject.class);
	  
	  veVar.setValue(ctx,s1);
	  
	  result = (String) veVarS.getValue(ctx);
	  assert result!=null;
	  assert result.equals("Goodbye");
   }
   
   /**
    * Performance test EL expressions
    */
   @Test(sequential=true,suiteName="performance")
   public void performanceELTest() {
	 performanceELTestRun(false,100,true);
	 performanceELTestRun(true,20000,false);
   }
   
   protected void performanceELTestRun(boolean printResults, int cnt, boolean warmUp) {
	 float div = (1000000f*cnt);
     ELContextImpl[] contexts = new ELContextImpl[cnt];
     Map<String,Object>[] maps = new Map[cnt];
     
     long start = System.nanoTime();
     for(int i=0; i<cnt; i++) {
    	contexts[i] = EL.createELContext();
    	maps[i] = contexts[i].getVariables();
     }
     float avg = (System.nanoTime()-start)/div;
     if(printResults) System.out.println("Context creation time avg="+avg+" ms");
     assert avg < 0.015 || warmUp;
     
     
     start = System.nanoTime();
     ValueExpression veConst = EL.EXPRESSION_FACTORY.createValueExpression(ctx,"${veConst}",Integer.class);
     for(int i=0; i<cnt; i++) {
    	veConst.setValue(contexts[i],i);
    	//maps[i].put("veConst", i);
     }
     avg = (System.nanoTime()-start)/div;
     if(printResults) System.out.println("Constants creation/assignment time avg="+avg+" ms");
     assert avg < 0.005 || warmUp;

     ValueExpression sobj = EL.EXPRESSION_FACTORY.createValueExpression(ctx,"${sobj}",SObject.class);
     ValueExpression s3objs = EL.EXPRESSION_FACTORY.createValueExpression(ctx,"${sobj.so.so.s}",String.class);
     ValueExpression mobj = EL.EXPRESSION_FACTORY.createValueExpression(ctx,"${mobj}",SObject.class);
     ValueExpression m3objs = EL.EXPRESSION_FACTORY.createValueExpression(ctx,"${mobj.so.so.s}",String.class);
     String[] alls = new String[cnt]; 
     for(int i=0; i<cnt; i++) {
    	SObject so = new SObject();
    	so.so = new SObject();
    	so.so.so = new SObject();
    	alls[i] = Integer.toString(i);
    	so.so.so.s = alls[i];
    	sobj.setValue(contexts[i],so);
    	Map<String,Object> m1 = new HashMap<String,Object>();
    	Map<String,Object> m2 = new HashMap<String,Object>();
    	m1.put("so",m2);
    	Map<String,Object> m3 = new HashMap<String,Object>();
    	m2.put("so",m3);
    	m3.put("s", alls[i]);
    	mobj.setValue(contexts[i],m1);
     }

     start = System.nanoTime();
     for(int i=0; i<cnt; i++) {
    	String sResult = (String) s3objs.getValue(contexts[i]);
    	assert sResult!=null;
    	assert alls[i]==sResult;
     }
     avg = (System.nanoTime()-start)/div;
     if(printResults) System.out.println("sobj.so.so.s Evaluation time="+avg+" ms");
     assert avg < 0.015 || warmUp;

     start = System.nanoTime();
     for(int i=0; i<cnt; i++) {
    	String sResult = (String) m3objs.getValue(contexts[i]);
    	assert sResult!=null;
    	assert alls[i]==sResult;
     }
     avg = (System.nanoTime()-start)/div;
     if(printResults) System.out.println("mobj.so.so.s Evaluation time="+avg+" ms");
     assert avg < 0.01 || warmUp;
     
     start = System.nanoTime();
     for(int i=0; i<cnt; i++) {
    	ValueExpression eval = EL.EXPRESSION_FACTORY.createValueExpression(contexts[i],"${veConst}",Integer.class);
    	Integer iResult = (Integer) eval.getValue(contexts[i]);
    	assert iResult!=null;
    	assert iResult.intValue()==i;
     }
     avg = (System.nanoTime()-start)/div;
     if(printResults) System.out.println("Evaluation time="+avg+" ms");
     assert avg < 0.005 || warmUp;
     
     
     start = System.nanoTime();
     for(int i=0; i<cnt; i++) {
    	Integer iResult = (Integer) veConst.getValue(contexts[i]);
    	assert iResult!=null;
    	assert iResult.intValue()==i;
     }
     avg = (System.nanoTime()-start)/div;
     if(printResults) System.out.println("Re-use Evaluation time="+avg+" ms");
     assert avg < 0.002 || warmUp;
     }
}

