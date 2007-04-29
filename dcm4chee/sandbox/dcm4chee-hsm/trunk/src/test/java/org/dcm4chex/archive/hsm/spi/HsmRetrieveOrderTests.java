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
import org.dcm4chex.archive.common.FileStatus;
import org.testng.annotations.Test;
import org.testng.Assert;

import java.util.List;
import java.util.Map;

/**
 * @author Fuad Ibrahimov
 * @since Feb 15, 2007
 */
public class HsmRetrieveOrderTests {
    private static final String FULL_PATH_1 = "/2007/2/15/12/3E376689/6AA7F267-D9E55A2B.tar!2007/2/15/12/3E376689/6AA7F267/D9E55A2B"; // NON-NLS
    private static final String TAR_PATH_1 = "/2007/2/15/12/3E376689/6AA7F267-D9E55A2B.tar"; // NON-NLS
    private static final String FULL_PATH_2 = "/2007/2/15/12/3E376689/6AA7F267-D9E55A2B.tar!2007/2/15/12/3E376689/3AA7F26A/A9E55A2B"; // NON-NLS
    private static final String FULL_PATH_3 = "/2007/2/15/12/3E376689/6AA7F267-D9E55A2B.tar!2007/2/15/12/3E376689/3AA7F26A/A9E55A2B"; // NON-NLS
    private static final String TAR_PATH_2 = "/2007/2/15/12/3E376689/6AA7F267-D9E55A2B.tar";  // NON-NLS
    private static final String HSM_DIR = "hsm:/home/fuad";  // NON-NLS
    private static final String SERIES_IUID_1 = "1.2.840.113619.2.5.2162465579.20.1171521530.601";
    private static final String SERIES_IUID_2 = "1.2.3.113619.2.5.2162465579.20.1171521530.601";
    private static final String MD5_1 = "123456789012345678901234567890ad"; // NON-NLS
    private static final String TEST_AET = "TEST_AET"; // NON-NLS

    @Test
    public void canExtractHsmFiles() throws Exception {
        FileInfo[][] finfos = new FileInfo[1][4]; // One entry intentionally is left null

        finfos[0][0] = TestUtils.newFileInfo(1, FULL_PATH_1, HSM_DIR, SERIES_IUID_1, 0, "1.55.44.3", MD5_1, FileStatus.ARCHIVED);
        finfos[0][1] = TestUtils.newFileInfo(2, FULL_PATH_2, HSM_DIR, SERIES_IUID_1, 0, "1.55.44.3", MD5_1, FileStatus.ARCHIVED);
        finfos[0][2] = TestUtils.newFileInfo(3, FULL_PATH_3, HSM_DIR, SERIES_IUID_2, 0, "1.55.44.3", MD5_1, FileStatus.ARCHIVED);


        HsmRetrieveOrder order = new HsmRetrieveOrder(finfos, TEST_AET);
        Map<String, List<HsmFile>> hsmFiles = order.getHsmFiles();
        Assert.assertEquals(hsmFiles.get(SERIES_IUID_1).size(), 1);
        Assert.assertEquals(hsmFiles.get(SERIES_IUID_1).get(0), new HsmFile(TAR_PATH_1, HSM_DIR));
        Assert.assertEquals(hsmFiles.get(SERIES_IUID_2).size(), 1);
        Assert.assertEquals(hsmFiles.get(SERIES_IUID_2).get(0), new HsmFile(TAR_PATH_2, HSM_DIR));

        Assert.assertTrue(hsmFiles.get(SERIES_IUID_1).get(0).getEntries().contains(finfos[0][0]));
        Assert.assertTrue(hsmFiles.get(SERIES_IUID_1).get(0).getEntries().contains(finfos[0][1]));
        Assert.assertTrue(hsmFiles.get(SERIES_IUID_2).get(0).getEntries().contains(finfos[0][2]));

    }

//    @Test
//    public void canExtractNotPackedHsmFiles() throws Exception {
//        FileInfo[][] finfos = new FileInfo[1][3];
//
//        finfos[0][0] = TestUtils.newFileInfo(1, "2007/2/15/12/3E376689/6AA7F267/D9E55A2B", HSM_DIR, SERIES_IUID_1, 0, "1.55.44.3", MD5_1, FileStatus.ARCHIVED);
//        finfos[0][1] = TestUtils.newFileInfo(2, "2007/2/15/12/3E376689/3AA7F26A/A9E55A2B", HSM_DIR, SERIES_IUID_1, 0, "1.55.44.3", MD5_1, FileStatus.ARCHIVED);
//        finfos[0][2] = TestUtils.newFileInfo(3, "2007/2/15/12/3E376689/3AA7F26A/A9E55A2B", HSM_DIR, SERIES_IUID_2, 0, "1.55.44.3", MD5_1, FileStatus.ARCHIVED);
//
//        HsmRetrieveOrder order = new HsmRetrieveOrder(finfos, TEST_AET);
//        Map<String, List<HsmFile>> hsmFiles = order.getHsmFiles();
//        Assert.assertEquals(hsmFiles.get(SERIES_IUID_1).size(), 2);
//        Assert.assertEquals(hsmFiles.get(SERIES_IUID_1).get(0), new HsmFile(finfos[0][0].fileID, HSM_DIR));
//        Assert.assertEquals(hsmFiles.get(SERIES_IUID_1).get(1), new HsmFile(finfos[0][1].fileID, HSM_DIR));
//        Assert.assertEquals(hsmFiles.get(SERIES_IUID_2).size(), 1);
//        Assert.assertEquals(hsmFiles.get(SERIES_IUID_2).get(0), new HsmFile(finfos[0][2].fileID, HSM_DIR));
//
//        Assert.assertTrue(hsmFiles.get(SERIES_IUID_1).get(0).getEntries().contains(finfos[0][0]));
//        Assert.assertTrue(hsmFiles.get(SERIES_IUID_1).get(0).getEntries().contains(finfos[0][1]));
//        Assert.assertTrue(hsmFiles.get(SERIES_IUID_2).get(0).getEntries().contains(finfos[0][2]));
//    }

}
