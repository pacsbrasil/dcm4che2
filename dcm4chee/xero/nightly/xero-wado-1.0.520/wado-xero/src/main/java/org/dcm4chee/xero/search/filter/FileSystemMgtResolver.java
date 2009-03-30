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
 * David Smith, Laura Peters, Agfa HealthCare Inc., 
 * Portions created by the Initial Developer are Copyright (C) 2007
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * David Smith <david.smith@agfa.com>
 * Laura Peters <laura.peters@agfa.com>
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

import java.rmi.RemoteException;
import java.util.NoSuchElementException;

import javax.ejb.CreateException;
import javax.ejb.EJBHome;
import javax.ejb.EJBObject;
import javax.ejb.FinderException;
import javax.naming.NameNotFoundException;

import org.dcm4chex.archive.ejb.interfaces.FileDTO;
import org.dcm4chex.archive.ejb.interfaces.FileSystemMgt;
import org.dcm4chex.archive.ejb.interfaces.FileSystemMgtHome;
import org.dcm4chex.archive.ejb.interfaces.FileSystemMgt2;
import org.dcm4chex.archive.ejb.interfaces.FileSystemMgt2Home;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used to get the file path(s) from the correct FileSystemMgt
 * EJB.
 * 
 * @author dsmith1547
 * 
 */
public class FileSystemMgtResolver {
    private static final Logger log = LoggerFactory.getLogger(FileSystemMgtResolver.class);

    static boolean isFileSystem2NotFound = false;

    public static FileDTO[] getDTOs(String host, String port, String instanceUID) throws Exception, CreateException,
            RemoteException, FinderException {
        if (!isFileSystem2NotFound) {
         try {
             EJBHome home = (EJBHome) EJBServiceLocator.getInstance().getRemoteHome(host, port, "ejb/FileSystemMgt2",
                     FileSystemMgt2Home.class);
log.warn("Using FileSystemMgt2.");
             EJBObject fileMgt = ((FileSystemMgt2Home) home).create();
             FileDTO[] dtos = ((FileSystemMgt2) fileMgt).getFilesOfInstance(instanceUID);
             return dtos;
         } catch (NoClassDefFoundError e) {
                isFileSystem2NotFound = true;
             log.warn("Using old file system management.");
         } catch(ClassNotFoundException e) {
                isFileSystem2NotFound = true;
             log.warn("Using old file system management.");
         } catch(NameNotFoundException e) {
                isFileSystem2NotFound = true;
             log.warn("Using old file system management.");
         }
            
        }
        log.warn("Could not find FileSystemMgt2 ejb, falling back to the dcm4chee 2.13.6 version");
        EJBHome home = (EJBHome) EJBServiceLocator.getInstance().getRemoteHome(host, port, "ejb/FileSystemMgt",
                FileSystemMgtHome.class);

        EJBObject fileMgt = ((FileSystemMgtHome) home).create();
        FileDTO[] dtos = ((FileSystemMgt) fileMgt).getFilesOfInstance(instanceUID);

        if (dtos == null)
            throw new NoSuchElementException("FileSystemMgt EJB not found");

        return dtos;
    }
}
