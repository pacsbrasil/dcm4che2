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
package org.dcm4che2.media;

import java.io.File;
import java.io.IOException;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;
import org.dcm4che2.data.VR;
import org.dcm4che2.util.StringUtils;
import org.dcm4che2.util.UIDUtils;

/**
 * @author Gunter Zeilinger<gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since 10.07.2006
 */

public class FilesetInformation extends FileMetaInformation {

    public FilesetInformation(DicomObject dcmobj) {
        super(dcmobj);
    }

    public FilesetInformation() {
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

    public String[] getFilesetDescriptorFileID() {
        return dcmobj.getStrings(Tag.FilesetDescriptorFileID);
    }

    public void setFilesetDescriptorFileID(String[] cs) {
        dcmobj.putStrings(Tag.FilesetDescriptorFileID, VR.CS, cs);
    }

    public String getSpecificCharacterSetofFilesetDescriptorFile() {
        return dcmobj.getString(Tag.SpecificCharacterSetofFilesetDescriptorFile);
    }

    public void setSpecificCharacterSetofFilesetDescriptorFile(String cs) {
        dcmobj.putString(Tag.FilesetID, VR.CS, cs);
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

    public static File toFile(String[] fileID, File basedir) {
        if (fileID == null || fileID.length == 0) {
            return null;
        }        
        StringBuffer sb = new StringBuffer(basedir.getPath());
        for (int i = 0; i < fileID.length; i++) {
            sb.append(File.separatorChar).append(fileID[i]);
        }
        return new File(sb.toString());
    }

    public static String[] toFileID(File file, File basedir) throws IOException {
        String dirpath = basedir.getCanonicalPath();
        String filepath = file.getCanonicalPath();
        int start = dirpath.length();
        if (!filepath.startsWith(dirpath) || start == filepath.length()) {
            throw new IllegalArgumentException("file " + file 
                    + " not included in file-set " + basedir);
        }
        if (filepath.charAt(start) == File.separatorChar) {
            ++start;
        }
        return StringUtils.split(filepath.substring(start), File.separatorChar);
    }

}
