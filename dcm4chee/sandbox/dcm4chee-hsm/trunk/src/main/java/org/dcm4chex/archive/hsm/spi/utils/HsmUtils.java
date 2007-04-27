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
 * Fuad Ibrahimov, Diagnoseklinik Muenchen.de GmbH,
 * Portions created by the Initial Developer are Copyright (C) 2007
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Fuad Ibrahimov <fuad@ibrahimov.de>
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
package org.dcm4chex.archive.hsm.spi.utils;

import org.dcm4chex.archive.util.FileUtils;
import org.dcm4chex.archive.ejb.jdbc.FileInfo;
import org.dcm4chex.archive.common.FileStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;
import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.text.MessageFormat;
import java.net.URL;

/**
 * @author Fuad Ibrahimov
 * @version $Id$
 * @since Mar 29, 2007
 */
public abstract class HsmUtils extends FileUtils {
    private static final Log logger = LogFactory.getLog(HsmUtils.class);
    private static final String COULDNT_DELETE_DIRECTORY = "Couldn''t delete directory [{0}]"; // NON-NLS
    private static final String DIRECTORY_IS_NOT_EMPTY_SKIPPING = "Directory is not empty: [{0}]. Skipping from delete."; // NON-NLS
    private static final String M_DELETE = "M-DELETE [{0}]"; // NON-NLS
    private static final String FILE_WAS_NOT_FOUND_IN_CLASSPATH = "File <{0}> was not found in classpath"; // NON-NLS
    public static final String FS_PREFIX_REGEXP = "\\w{3}:"; // NON-NLS


    public static void deleteParentsTill(File file, String topParent) throws IOException {
        Assert.notNull(file, "file"); // NON-NLS
        Assert.hasText(topParent, "topParent"); // NON-NLS
        File top = toFile(topParent);
        if(!file.getCanonicalPath().startsWith(top.getCanonicalPath()))
            throw new IOException(top + " is not a parent directory of " + file);
        
        File parent = file.getParentFile();
        while (!top.equals(parent)) {
            String[] children = parent.list();
            if (children == null || children.length == 0) {
                if (parent.delete()) {
                    if (logger.isInfoEnabled()) {
                        logger.info(MessageFormat.format(M_DELETE, parent));
                    }
                } else {
                    logger.warn(MessageFormat.format(COULDNT_DELETE_DIRECTORY, parent));
                }
            } else {
                logger.warn(MessageFormat.format(DIRECTORY_IS_NOT_EMPTY_SKIPPING, parent));
            }
            parent = parent.getParentFile();
        }
    }

    public static File classpathResource(String path) throws FileNotFoundException {
        URL resource = HsmUtils.class.getClassLoader().getResource(path);
        if(resource == null) throw new FileNotFoundException(MessageFormat.format(FILE_WAS_NOT_FOUND_IN_CLASSPATH, path));
        return new File(resource.getFile());
    }

    public static String extractFileSpaceName(String destPath) {
        return destPath.replaceFirst(FS_PREFIX_REGEXP, "");
    }

    public static String resolveFileSpacePath(String fileSpaceName) throws IOException {
        return toFile(HsmUtils.extractFileSpaceName(fileSpaceName)).getCanonicalPath();
    }
}
