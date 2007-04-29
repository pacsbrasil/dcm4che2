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

import java.util.List;
import java.io.File;

/**
 * This interface represents a gateway to various HSM systems. Used by
 * <code>HsmCopyService</code> and <code>HsmRetrieveService</code> to
 * archive and retrieve files.
 * <p>
 * Implementations of this interface can be as easy as a class delegating
 * everything to underlying OS level virtual HSM filesystem, as well as
 * a more sophisticated implementations using the API of the HSM tools. 
 *
 * @see org.dcm4chex.archive.hsm.spi.HsmCopyService
 * @see org.dcm4chex.archive.hsm.spi.HsmRetrieveService
 * @author Fuad Ibrahimov
 * @since Feb 9, 2007
 */
public interface HsmClient {

    /**
     * Retieves files from the HSM archive. 
     * @param filespace file space name to retrieve files from.
     * Must be in a system dependent format.
     * @param filePath full file path in a system depending format.
     * Must also include the file space name. 
     * @param destination the destination directory for retrieved files.
     * @throws Exception in case of retrieve errors
     * @see org.dcm4chex.archive.hsm.spi.HsmRetrieveService
     */
    public void retrieve(String filespace, String filePath, File destination) throws Exception;

    /**
     * Archives the passed in file to the HSM archive.
     *
     * @param filespace file space name to be used as archive destination.
     * Must be in a system dependent format.
     * @param file file to be archived
     * @throws Exception in case of archive errors
     * @see org.dcm4chex.archive.hsm.spi.HsmCopyService
     */
    public void archive(String filespace, File file) throws Exception;
}
