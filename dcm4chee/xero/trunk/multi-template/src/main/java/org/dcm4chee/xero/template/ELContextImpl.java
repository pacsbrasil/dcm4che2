package org.dcm4chee.xero.template;

import java.util.HashMap;
import java.util.Map;

import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.FunctionMapper;
import javax.el.VariableMapper;

import org.jboss.el.lang.VariableMapperImpl;

/**
 * A simple context implementation.
 * @author bwallace
 */
public class ELContextImpl extends ELContext {

   final ELResolver resolver;
   final FunctionMapper functionMapper;
   final VariableMapperImpl variableMapper = new VariableMapperImpl();
   final Map<String,Object> variables = new HashMap<String,Object>();
   
   public ELContextImpl(ELResolver resolver, FunctionMapper functionMapper) {
	  this.resolver = resolver;
	  this.functionMapper = functionMapper;
   }
   
   public Map<String,Object> getVariables() {
	  return variables;
   }
   
   @Override
   public ELResolver getELResolver() 
   {
	  return resolver;
   }

   @Override
   public FunctionMapper getFunctionMapper() {
	  return functionMapper;
   }

   @Override
   public VariableMapper getVariableMapper() {
	  return variableMapper;
   }
   
}
