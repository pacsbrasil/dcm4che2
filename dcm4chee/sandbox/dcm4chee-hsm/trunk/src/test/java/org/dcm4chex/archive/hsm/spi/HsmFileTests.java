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
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * @author Fuad Ibrahimov
 * @version $Id$
 * @since Feb 15, 2007
 */
public class HsmFileTests {
    private static final String FULL_PATH = "/2007/2/15/12/3E376689/6AA7F267-D9E55A2B.tar!2007/2/15/12/3E376689/6AA7F267/D9E55A2B"; // NON-NLS
    private static final String TAR_PATH = "/2007/2/15/12/3E376689/6AA7F267-D9E55A2B.tar"; // NON-NLS
    private static final String FILE_PATH = "2007/2/15/12/3E376689/6AA7F267/D9E55A2B"; // NON-NLS

    private static final String HSM_DIR = "hsm:/home/fuad"; // NON-NLS
    private static final String SERIES_IUID = "1.2.840.113619.2.5.2162465579.20.1171521530.601";


    @Test
    public void testExtractTarPath() {
        String tar = HsmFile.extractTarPath(HsmFileTests.FULL_PATH);
        assertEquals(tar, HsmFileTests.TAR_PATH);
    }

    @Test
    public void testExtractFilePath() {
        String file = HsmFile.extractFilePath(HsmFileTests.FULL_PATH);
        assertEquals(file, HsmFileTests.FILE_PATH);
    }

    @Test
    public void addEntryAddsACopyOfFileInfoAndChangesFileID() throws Exception {
        FileInfo fileInfo = TestUtils.newFileInfo(1,
                HsmFileTests.FULL_PATH,
                HsmFileTests.HSM_DIR,
                HsmFileTests.SERIES_IUID,
                0,
                "1.55.44.3",
                "123456789012345678901234567890ad", 0); // NON-NLS
        HsmFile hsmFile = new HsmFile(HsmFileTests.TAR_PATH, HsmFileTests.HSM_DIR);
        hsmFile.addEntry(fileInfo);

        assertNotSame(hsmFile.getEntries().get(0), fileInfo);
        assertTrue(hsmFile.getEntries().contains(fileInfo)); // Contains a copy with the same pk
        assertEquals(hsmFile.getEntries().get(0).fileID, HsmFile.extractFilePath(HsmFileTests.FULL_PATH));
    }
}
