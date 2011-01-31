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
 * Java(TM), available at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * TIANI Medgraph AG.
 * Portions created by the Initial Developer are Copyright (C) 2003-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunter.zeilinger@tiani.com>
 * Franz Willer <franz.willer@gwi-ag.com>
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

package org.dcm4chee.dashboard.service.webcfg;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;

import org.jboss.system.ServiceMBeanSupport;
import org.jboss.system.server.ServerConfigLocator;

/**
 * @author franz.willer@gmail.com
 * @version $Revision$ $Date$
 * @since July 26, 2010
 */
public class WebCfgService extends ServiceMBeanSupport {

    private static final long serialVersionUID = 1L;

    private String webConfigPath;
    
    private String loginAllowedRolename;
    
    private boolean manageUsers;
    private boolean webStudyPermissions;
    
    private static final String NONE = "NONE";
    
    public WebCfgService() {
    }

    public String getWebConfigPath() {
        return webConfigPath;
    }

    public void setWebConfigPath(String webConfigPath) {
        this.webConfigPath = webConfigPath;
    }

    public void setLoginAllowedRolename(String loginAllowedRolename) {
        this.loginAllowedRolename = loginAllowedRolename;
    }

    public String getLoginAllowedRolename() {
        return loginAllowedRolename;
    }

    public String getRolesFilename() {
        return System.getProperty("dcm4chee-usr.cfg.roles-filename", NONE);
    }

    public void setRolesFilename(String name) {
        if (NONE.equals(name)) {
            System.getProperties().remove("dcm4chee-usr.cfg.roles-filename");
        } else {
            String old = System.getProperty("dcm4chee-usr.cfg.roles-filename");
            System.setProperty("dcm4chee-usr.cfg.roles-filename", name);
            if (old == null) {
                initDefaultFile();
            }
        }
    }
    
    private void initDefaultFile() {
        File mappingFile = new File(System.getProperty("dcm4chee-usr.cfg.roles-filename", "conf/dcm4chee-web3/roles.json"));
        if (!mappingFile.isAbsolute())
            mappingFile = new File(ServerConfigLocator.locate().getServerHomeDir(), mappingFile.getPath());
        log.info("Init default Role Mapping file! mappingFile:"+mappingFile);
        if (mappingFile.getParentFile().mkdirs())
            log.info("M-WRITE dir:" +mappingFile.getParent());
        FileChannel fos = null;
        InputStream is = null;
        try {
            URL url = getClass().getResource("/META-INF/roles-default.json");
            log.info("Use default Mapping File content of url:"+url);
            is = url.openStream();
            ReadableByteChannel inCh = Channels.newChannel(is);
            fos = new FileOutputStream(mappingFile).getChannel();
            int pos = 0;
            while (is.available() > 0)
                pos += fos.transferFrom(inCh, pos, is.available());
        } catch (Exception e) {
            log.error("Roles file doesn't exist and the default can't be created!", e);
        } finally {
            close(is);
            close(fos);
        }
    }
    
    private void close(Closeable toClose) {
        if (toClose != null) {
            try {
                toClose.close();
            } catch (IOException ignore) {
                log.debug("Error closing : "+toClose.getClass().getName(), ignore);
            }
        }
    }
}
