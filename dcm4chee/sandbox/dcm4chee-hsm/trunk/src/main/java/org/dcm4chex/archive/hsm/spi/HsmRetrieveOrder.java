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

import org.dcm4chex.archive.common.BaseJmsOrder;
import org.dcm4chex.archive.ejb.jdbc.FileInfo;
import org.dcm4chex.archive.hsm.spi.utils.Assert;

import java.util.*;

/**
 * @author Fuad Ibrahimov
 * @version $Id$
 * @since Feb 14, 2007
 */
public class HsmRetrieveOrder extends BaseJmsOrder {
    private final Map<String, List<HsmFile>> hsmFiles;
    private final String destination;
    private static final long serialVersionUID = 5152979080490966440L;

    public HsmRetrieveOrder(FileInfo[][] archivedFiles, String destination) {
        this(new HashMap<String, List<HsmFile>>(), destination);
        Assert.notNull(archivedFiles, "archivedFiles"); // NON-NLS
        Map<String, HsmFile> extracted = new HashMap<String, HsmFile>();
        for(FileInfo[] finfos : archivedFiles) {
            for(FileInfo finfo: finfos) {
                if (finfo != null) {
                    if(!hsmFiles.containsKey(finfo.seriesIUID)) {
                        hsmFiles.put(finfo.seriesIUID, new ArrayList<HsmFile>());
                        extracted.clear();
                    }
                    String tarPath = HsmFile.extractTarPath(finfo.fileID);
                    if(!extracted.containsKey(tarPath)) {
                        HsmFile hsmFile = new HsmFile(tarPath, finfo.basedir);
                        hsmFiles.get(finfo.seriesIUID).add(hsmFile);
                        extracted.put(tarPath, hsmFile);
                    }
                    extracted.get(tarPath).addEntry(finfo);
                }
            }
        }
    }

    public HsmRetrieveOrder(Map<String, List<HsmFile>> hsmFiles, String destination) {
        Assert.hasText(destination, "destination"); // NON-NLS
        Assert.notNull(hsmFiles, "hsmFiles"); // NON-NLS
        this.hsmFiles = new HashMap<String, List<HsmFile>>(hsmFiles.size());
        this.hsmFiles.putAll(hsmFiles);
        this.destination = destination;
    }

    public String getDestination() {
        return destination;
    }

    public Map<String, List<HsmFile>> getHsmFiles() {
        return Collections.unmodifiableMap(hsmFiles);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final HsmRetrieveOrder that = (HsmRetrieveOrder) o;

        return destination.equals(that.destination) && hsmFiles.equals(that.hsmFiles);

    }

    @Override
    public int hashCode() {
        int result;
        result = hsmFiles.hashCode();
        result = 29 * result + destination.hashCode();
        return result;
    }


}
