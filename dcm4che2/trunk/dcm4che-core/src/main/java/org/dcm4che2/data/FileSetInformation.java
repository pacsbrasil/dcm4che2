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
 * Gunter Zeilinger, Huetteldorferstr. 24/10, 1150 Vienna/Austria/Europe.
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
package org.dcm4che2.data;

import org.dcm4che2.util.UIDUtils;

/**
 * @author Gunter Zeilinger<gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since 10.07.2006
 */

public class FileSetInformation extends FileMetaInformation {

    public FileSetInformation(DicomObject dcmobj) {
        super(dcmobj);
    }

    public FileSetInformation() {
        super();
    }
    
    public void init() {
        super.init();
        dcmobj.putNull(Tag.FilesetID, VR.CS);
        setOffsetFirstRootRecord(0);
        setOffsetLastRootRecord(0);
        setFilesetConsistencyFlag(0);
    }

    public String getFilesetID() {
        return dcmobj.getString(Tag.FilesetID);
    }

    public void setFilesetID(String id) {
        dcmobj.putString(Tag.FilesetID, VR.CS, id);
    }

    public int getOffsetFirstRootRecord() {
        return dcmobj.getInt(
                Tag.OffsetoftheFirstDirectoryRecordoftheRootDirectoryEntity);
    }

    public void setOffsetFirstRootRecord(int offset) {
        dcmobj.putInt(Tag.OffsetoftheFirstDirectoryRecordoftheRootDirectoryEntity,
                VR.UL, offset);
    }

    public int getOffsetLastRootRecord() {
        return dcmobj.getInt(
                Tag.OffsetoftheLastDirectoryRecordoftheRootDirectoryEntity);
    }

    public void setOffsetLastRootRecord(int offset) {
        dcmobj.putInt(Tag.OffsetoftheLastDirectoryRecordoftheRootDirectoryEntity,
                VR.UL, offset);
    }
    
    public boolean isEmpty() {
        return getOffsetFirstRootRecord() == 0;
    }

    public int getFilesetConsistencyFlag() {
        return dcmobj.getInt(Tag.FilesetConsistencyFlag);
    }
    
    public void setFilesetConsistencyFlag(int flag) {
        dcmobj.putInt(Tag.FilesetConsistencyFlag, VR.US, flag);
    }
    
    protected String getSOPClassUID() {
        return UID.MediaStorageDirectoryStorage;
    }

    protected String getSOPInstanceUID() {
        return UIDUtils.createUID();
    }

}
