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

import javax.el.ArrayELResolver;
import javax.el.BeanELResolver;
import javax.el.CompositeELResolver;
import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.ExpressionFactory;
import javax.el.ListELResolver;
import javax.el.MapELResolver;

import org.jboss.el.ExpressionFactoryImpl;
import org.jboss.el.lang.FunctionMapperImpl;

/**
 * To EL with it.  This is an implementation of the expression language to be used for the 
 * multi-template library.  It is based on the JBoss implementation of the expression language, although
 * it should be possible to swap out for other implementations.
 * 
 * @author bwallace
 */
public class EL {

   public static final ExpressionFactory EXPRESSION_FACTORY = new ExpressionFactoryImpl();
   
   private static final MapELResolver mapELResolver = new MapELResolver();
   private static final ListELResolver listELResolver = new ListELResolver();
   private static final ArrayELResolver arrayELResolver = new ArrayELResolver();
   private static final BeanELResolver beanELResolver = new BeanELResolver();
   private static final TemplateELResolver templateELResolver = new TemplateELResolver();
   
   /**
    * This EL Resolver is used by many contexts to resolve the variables for the context, and is
    * considered thread safe for general use.
    */
   public static final ELResolver EL_RESOLVER = createELResolver();
   
   /**
    * This static EL_CONTEXT should only be used for compiling expressions etc, or if it is known
    * that only one thread will use the context at a time.
    */
   public static final ELContextImpl EL_CONTEXT = createELContext();
   
   private static ELResolver createELResolver()
   {
      CompositeELResolver resolver = new CompositeELResolver();
      resolver.add( templateELResolver );
      resolver.add( mapELResolver );
      resolver.add( listELResolver );
      resolver.add( arrayELResolver );
      resolver.add( beanELResolver );
      return resolver;
   }
   
   /**
    * Create a new el context using the default resolver and a new function mapper implementation.
    * @return ELContext to be used to evaluate expressions.
    */
   public static ELContextImpl createELContext() {
	  return createELContext(EL_RESOLVER, new FunctionMapperImpl() );  
   }

   public static ELContextImpl createELContext(ELResolver elResolver, FunctionMapperImpl functionMapperImpl) {
	  return new ELContextImpl(elResolver, functionMapperImpl);
   }

}
