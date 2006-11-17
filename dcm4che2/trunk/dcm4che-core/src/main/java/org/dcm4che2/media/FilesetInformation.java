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

    public static final int NO_KNOWN_INCONSISTENCIES = 0;
    public static final int KNOWN_INCONSISTENCIES = 0xffff;    
    
    public FilesetInformation(DicomObject dcmobj) {
        super(dcmobj);
    }

    public FilesetInformation() {
        super();
    }
    
    public void init() {
        super.init();
        dcmobj.putNull(Tag.FILE_SET_ID, VR.CS);
        setOffsetFirstRootRecord(0);
        setOffsetLastRootRecord(0);
        setFilesetConsistencyFlag(0);
    }

    public String getFilesetID() {
        return dcmobj.getString(Tag.FILE_SET_ID);
    }

    public void setFilesetID(String id) {
        dcmobj.putString(Tag.FILE_SET_ID, VR.CS, id);
    }

    public String[] getFilesetDescriptorFileID() {
        return dcmobj.getStrings(Tag.FILE_SET_DESCRIPTOR_FILE_ID);
    }

    public void setFilesetDescriptorFileID(String[] cs) {
        dcmobj.putStrings(Tag.FILE_SET_DESCRIPTOR_FILE_ID, VR.CS, cs);
    }

    public File getFilesetDescriptorFile(File basedir) {
	return toFile(getFilesetDescriptorFileID(), basedir);
    }
    
    public void setFilesetDescriptorFile(File file, File basedir) {
	setFilesetDescriptorFileID(toFileID(file, basedir));
    }
    
    public String getSpecificCharacterSetofFilesetDescriptorFile() {
        return dcmobj.getString(Tag.SPECIFIC_CHARACTER_SET_OF_FILE_SET_DESCRIPTOR_FILE);
    }

    public void setSpecificCharacterSetofFilesetDescriptorFile(String cs) {
        dcmobj.putString(Tag.FILE_SET_ID, VR.CS, cs);
    }
    
    public int getOffsetFirstRootRecord() {
        return dcmobj.getInt(
                Tag.OFFSET_OF_THE_FIRST_DIRECTORY_RECORD_OF_THE_ROOT_DIRECTORY_ENTITY);
    }

    public void setOffsetFirstRootRecord(int offset) {
        dcmobj.putInt(Tag.OFFSET_OF_THE_FIRST_DIRECTORY_RECORD_OF_THE_ROOT_DIRECTORY_ENTITY,
                VR.UL, offset);
    }

    public int getOffsetLastRootRecord() {
        return dcmobj.getInt(
                Tag.OFFSET_OF_THE_LAST_DIRECTORY_RECORD_OF_THE_ROOT_DIRECTORY_ENTITY);
    }

    public void setOffsetLastRootRecord(int offset) {
        dcmobj.putInt(Tag.OFFSET_OF_THE_LAST_DIRECTORY_RECORD_OF_THE_ROOT_DIRECTORY_ENTITY,
                VR.UL, offset);
    }
    
    public boolean isEmpty() {
        return getOffsetFirstRootRecord() == 0;
    }

    public int getFilesetConsistencyFlag() {
        return dcmobj.getInt(Tag.FILE_SET_CONSISTENCY_FLAG);
    }
    
    public void setFilesetConsistencyFlag(int flag) {
        dcmobj.putInt(Tag.FILE_SET_CONSISTENCY_FLAG, VR.US, flag);
    }
    
    public boolean isNoKnownInconsistencies() {
	return getFilesetConsistencyFlag() == NO_KNOWN_INCONSISTENCIES;
    }
    
    protected String getSOPClassUID() {
        return UID.MEDIA_STORAGE_DIRECTORY_STORAGE;
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

    public static String[] toFileID(File file, File basedir) {
        String dirpath = basedir.getAbsolutePath();
        String filepath = file.getAbsolutePath();
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
