package org.dcm4chee.xero.search;

/** This interface is used to indicate that the number of results allowed has been exceeded. */
public interface HasMaxResults {

   /** Call this method when the number of allowed results has been exceeded */
   public void tooManyResults();
}
