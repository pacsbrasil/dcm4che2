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
 * Agfa-Gevaert AG.
 * Portions created by the Initial Developer are Copyright (C) 2002-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See listed authors below.
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

package org.dcm4chee.usr.entity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.Properties;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.jboss.system.server.ServerConfigLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Robert David <robert.david@agfa.com>
 * @version $Revision$ $Date$
 * @since 07.09.2010
 */
@Entity
@Table(name = "role")
public class Role implements Serializable, Comparable<Role> {

    private static final long serialVersionUID = 1L;
    
    private static Logger log = LoggerFactory.getLogger(Role.class);
    
    private static String mappingFilename;

    @Id
    @GeneratedValue
    protected long pk;

    @Column(name = "rolename", nullable=false)
    private String rolename;

    @Column(name = "isSystemRole")
    private boolean isSystemRole;

    @Column(name = "type")
    private String type;

    @Column(name = "description")
    private String description;

    @Column(name = "color")
    private String color;

    public Role() {        
        // TODO: put the filename into the config service
        mappingFilename = 
            ServerConfigLocator.locate().getServerConfigURL().getPath() + 
            "rolesMapping-roles.properties";
        
        this.color = "white";
        this.isSystemRole = false;
        
        File file = new File(Role.mappingFilename);
        if (!file.exists())
            try {
                file.createNewFile();
            } catch (IOException e) {
                log.error(getClass().getName() + ": " + e.getLocalizedMessage());
            }
    }
    
    public Role(String rolename) {
        this();
        this.rolename = rolename;
    }

    public Role(String rolename, boolean isSystemRole) {
        this(rolename);
        this.isSystemRole = isSystemRole;
    }

    public long getPk() {
        return pk;
    }

    public String getRolename() {
        return rolename;     
    }
    
    public void setRolename(String rolename) {
        this.rolename = rolename;
    }

    public void setSystemRole(boolean isSystemRole) {
        this.isSystemRole = isSystemRole;
    }

    public boolean isSystemRole() {
        return isSystemRole;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getColor() {
        return color;
    }

    public Properties getSwarmPrincipals() {
        Properties swarmPrincipals = null;
        try {
            swarmPrincipals = new Properties();
            BufferedReader in = new BufferedReader(new FileReader(Role.mappingFilename));
            swarmPrincipals.load(in);
            in.close();
        } catch (IOException e) {
            log.error(getClass().getName() + ": " + e.getLocalizedMessage());
        }
        return swarmPrincipals;
    }

    public void setSwarmPrincipals(Properties swarmPrincipals) {
        if (swarmPrincipals == null) return;
        try {
            FileOutputStream out = new FileOutputStream(new File(Role.mappingFilename));
            swarmPrincipals.store(out, null);
            out.close();
        } catch (FileNotFoundException e) {
            log.error(getClass().getName() + ": " + e.getLocalizedMessage());
        } catch (IOException e) {
            log.error(getClass().getName() + ": " + e.getLocalizedMessage());
        }
    }

    @Override
    public String toString() {
        return getRolename();   
    }

    public int compareTo(Role role) {
        return rolename.toUpperCase().compareTo(role.getRolename().toUpperCase());
    }    
}