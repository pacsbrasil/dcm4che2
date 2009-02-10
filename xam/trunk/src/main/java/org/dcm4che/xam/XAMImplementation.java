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
 * Agfa Healthcare.
 * Portions created by the Initial Developer are Copyright (C) 2002-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See @authors listed below
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

package org.dcm4che.xam;

import org.snia.xam.XAMException;
import org.snia.xam.XAMLibrary;

/**
 * Patched org.snia.xam.base.XAMImplementation, to unify configuration
 * lookup with C_XAM_Library
 * 
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Feb 9, 2009
 */
public class XAMImplementation extends org.snia.xam.base.XAMImplementation {

    private static final String DEF_XAM_CONFIG_PATH = "xam.properties";

    private static XAMLibrary s_library;

    public XAMImplementation() throws XAMException {
        super(nullToDefault(System.getenv("XAM_CONFIG_PATH"),
                DEF_XAM_CONFIG_PATH));
    }

    public static XAMLibrary getLibrary() throws XAMException
    {
       if( s_library == null ) {
          s_library = new XAMImplementation();
       }
       return s_library;
    }

    private static String nullToDefault(String env, String def) {
        return env != null ? env : def;
    }

}
