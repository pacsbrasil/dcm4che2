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
 * Accurate Software Design, LLC.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Damien Evans <damien.daddy@gmail.com>
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

package org.dcm4che.archive.util.ejb;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * Helper class to lookup and cache EJB references from JNDI.
 * 
 * @author <a href="mailto:damien.daddy@gmail.com">Damien Evans</a>
 */
public class EJBReferenceCache {

    private static final String EJB_JNDI_PROPERTIES = "ejb-jndi.properties";

    private static EJBReferenceCache factory;

    private Map<String, Object> ejbs = new ConcurrentHashMap<String, Object>();

    private Context ctx;

    public static EJBReferenceCache getFactory() {
        if (EJBReferenceCache.factory == null) {
            try {
                EJBReferenceCache.factory = new EJBReferenceCache();
            }
            catch (Exception e) {
                // Rethrow it as RuntimeException as there is really no need to
                // continue if this exception happens and we don't want to catch
                // it everywhere.
                throw new RuntimeException(e);
            }
        }
        return EJBReferenceCache.factory;
    }

    private EJBReferenceCache() {
        Properties env = new Properties();
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try {
            env.load(cl.getResourceAsStream(EJB_JNDI_PROPERTIES));
        }
        catch (IOException e) {
            throw new RuntimeException("Could not load " + EJB_JNDI_PROPERTIES,
                    e);
        }
        try {
            ctx = new InitialContext(env);
        }
        catch (NamingException e) {
            throw new RuntimeException("Could not create Context", e);
        }
    }

    public Object lookup(String jndiName) {
        Object ejb = ejbs.get(jndiName);
        if (ejb == null) {
            try {
                ejb = ctx.lookup(jndiName);
            }
            catch (NamingException e) {
                throw new RuntimeException("Could not lookup " + jndiName, e);
            }
            ejbs.put(jndiName, ejb);
        }
        return ejb;
    }
}
