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
package org.dcm4chex.archive.hsm.spi;

import org.dcm4chex.archive.ejb.jdbc.FileInfo;
import org.dcm4chex.archive.hsm.spi.utils.HsmUtils;
import org.dcm4che.dict.UIDs;

import java.io.IOException;
import java.io.File;

/**
 * @author Fuad Ibrahimov
 * @since Apr 8, 2007
 */
public class TestUtils {
    public static FileInfo newFileInfo(int pk,
                                       String name,
                                       String baseDir,
                                       String seriesIUID,
                                       long fileLength,
                                       String sopIUID,
                                       String md5, int status) throws IOException {
        return new FileInfo(pk,
                "123",
                "Peter Mustermann", // NON-NLS
                null,
                "1.2.3.4.5",
                seriesIUID,
                null,
                null,
                null,
                sopIUID,
                "1.44.33.22",
                "TEST_AET", // NON-NLS
                "TEST_AET", // NON-NLS
                0,
                baseDir,
                name,
                UIDs.ExplicitVRBigEndian,
                md5,
                fileLength,
                status);
    }

    public static void cleanup(File tarFile, String tempDir) throws IOException {
        if (tarFile != null) {
            tarFile.delete();
            if (tempDir != null)
                HsmUtils.deleteParentsTill(tarFile, tempDir);
        }
    }


}
