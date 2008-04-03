package org.dcm4chee.xero.template;

import java.beans.FeatureDescriptor;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.el.ELContext;
import javax.el.ELResolver;

import org.jboss.el.lang.EvaluationContext;

public class TemplateELResolver extends ELResolver {
  
   public TemplateELResolver() {
   }
   
   @Override
   public Class<?> getCommonPropertyType(ELContext ctx, Object base) {
	  if( base==null ) return null;
	  return Object.class;
   }

   @Override
   public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext arg0, Object arg1) {
	  return null;
   }

   @Override
   public Class<?> getType(ELContext ctx, Object base, Object property) {
	  Object value = getValue (ctx, base, property);
	  if( value==null ) return null;
	  ctx.setPropertyResolved(true);
	  return value.getClass();
   }

   @Override
   public Object getValue(ELContext ctx, Object base, Object property) {
	  if( base!=null ) return null;
	  Map<String,Object> map = ((ELContextImpl) ((EvaluationContext) ctx).getELContext()).getVariables();
	  Object val = map.get(property);
	  if( val!=null ) {
		 ctx.setPropertyResolved(true);
		 return val;
	  }
	  return null;
   }

   @Override
   public boolean isReadOnly(ELContext ctx, Object base, Object property) {
	  if( !(base==null && (property instanceof String))) return false;
	  ctx.setPropertyResolved(true);
	  return true;
   }

   @Override
   public void setValue(ELContext ctx, Object base, Object prop, Object value) {
	  if( base!=null ) return;
	  Map<String,Object> map = ((ELContextImpl) ((EvaluationContext) ctx).getELContext()).getVariables();
	  map.put((String) prop, value);
	  ctx.setPropertyResolved(true);
   }

}
