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
 * Sebastian Mohan, Agfa HealthCare Inc., 
 * Portions created by the Initial Developer are Copyright (C) 2007
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Sebastian Mohan <sebastian.mohan@agfa.com>
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

package org.dcm4chee.xero.search.filter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.ejb.EJBHome;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class to locate the EJBHome interface and cache them
 * @author smohan
 * 
 */
public class EJBServiceLocator {

    private static final Logger log = LoggerFactory
    .getLogger(EJBServiceLocator.class);
    
   /**
    * jndi home cache
    */
   private Map<String, EJBHome> cache;

   /**
    * Singleton instance of <tt>EJBServiceLocator<\tt>
    */
   private static final EJBServiceLocator INSTANCE = new EJBServiceLocator();

   /**
    * initialize <tt>InitialContext</tt> with default host name.
    */
   private EJBServiceLocator() {
         cache = Collections.synchronizedMap(new HashMap<String, EJBHome>());
   }

   /**
    * @return InitialContext
    * @throws NamingException
    */
   public static InitialContext getInitialContext(String host, String port) throws NamingException {
       if (host.equals("localhost") && (port == null || port.equals(""))) {
           log.debug("getInitialContext using default context.");
            return new InitialContext();
       }
       log.debug("getInitialContext host "+host+", port "+port);
       Properties prop = new Properties();
       prop.put(Context.INITIAL_CONTEXT_FACTORY,
               "org.jnp.interfaces.NamingContextFactory");
       prop.put(Context.PROVIDER_URL, "jnp://" + host + ":" + port);
       prop.put(Context.URL_PKG_PREFIXES, "jboss.naming:org.jnp.interfaces");
       return new InitialContext(prop);
   }

   /**
     * @return this instance
     */
   static public EJBServiceLocator getInstance() {
      return INSTANCE;
   }

   /**
    * locate home interface from cache if not lookup from context.
    * 
    * @param jndiHomeName
    * @param className
    * @return EJBHome for the given JNDI name
    * @throws Exception
    */
   public EJBHome getRemoteHome(String host, String port, String jndiHomeName,
         Class<? extends EJBHome> className) throws Exception {
      EJBHome home = null;
      try {
         if (cache.containsKey(jndiHomeName)) {
            home = (EJBHome) cache.get(jndiHomeName);
         } else {
            InitialContext ic = getInitialContext(host, port);
            log.warn("Looked up host "+host+" port "+port);
            Object objref = ic.lookup(jndiHomeName);
            log.warn("Found object "+jndiHomeName);
            Object obj = PortableRemoteObject.narrow(objref, className);
            home = (EJBHome) obj;
            cache.put(jndiHomeName, home);
         }
      } catch (NamingException ne) {
         log.warn("Unable to lookup "+jndiHomeName+" on "+host+":"+port );
         throw ne;
      }
      return home;
   }
}
