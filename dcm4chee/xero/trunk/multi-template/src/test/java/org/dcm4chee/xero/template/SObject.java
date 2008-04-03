package org.dcm4chee.xero.template;

public class SObject {
   public String s;
   public SObject so;
   
   public String getS() {
	  return s;
   }
   public void setS(String s) {
	  this.s = s;
   }
   
   public SObject getSo() {
	  return so;
   }
   public void setSo(SObject so) {
	  this.so = so;
   }
}
