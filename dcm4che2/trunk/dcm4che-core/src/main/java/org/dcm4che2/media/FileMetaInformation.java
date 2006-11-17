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

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Implementation;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;
import org.dcm4che2.data.VR;

/**
 * @author Gunter Zeilinger<gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since 10.07.2006
 */

public class FileMetaInformation {
    private static final int VERS_01 = 1;
    protected final DicomObject dcmobj;
    public FileMetaInformation(DicomObject dcmobj) {
        if (dcmobj == null) {
            throw new NullPointerException("dcmobj");
        }
        this.dcmobj = dcmobj;
    }

    public FileMetaInformation() {
        this(new BasicDicomObject());
    }
    
    public final DicomObject getDicomObject() {
        return dcmobj;
    }

    protected String getSOPClassUID() {
        return dcmobj.getString(Tag.SOP_CLASS_UID);
    }

    protected String getSOPInstanceUID() {
        return dcmobj.getString(Tag.SOP_INSTANCE_UID);
    }

    public void init() {
        setFileMetaInformationVersion(VERS_01);
        setMediaStorageSOPClassUID(getSOPClassUID());
        setMediaStorageSOPInstanceUID(getSOPInstanceUID());
        setTransferSyntaxUID(UID.EXPLICIT_VR_LITTLE_ENDIAN);
        setImplementationClassUID(Implementation.classUID());
        setImplementationVersionName( Implementation.versionName());
    }

    public int getFileMetaInformationVersion() {
        byte[] b = dcmobj.getBytes(Tag.FILE_META_INFORMATION_VERSION);
        return (b[0] & 0xff) << 8 | (b[1] & 0xff);
    }

    public void setFileMetaInformationVersion(int version) {
        byte[] b = { (byte) (version >> 8),  (byte) version };
        dcmobj.putBytes(Tag.FILE_META_INFORMATION_VERSION, VR.OB, b);
    }

    public String getMediaStorageSOPInstanceUID() {
        return dcmobj.getString(Tag.MEDIA_STORAGE_SOP_INSTANCE_UID);
    }

    public void setMediaStorageSOPInstanceUID(String uid) {
        dcmobj.putString(Tag.MEDIA_STORAGE_SOP_INSTANCE_UID, VR.UI, uid);
    }

    public String getMediaStorageSOPClassUID() {
        return dcmobj.getString(Tag.MEDIA_STORAGE_SOP_CLASS_UID);
    }

    public void setMediaStorageSOPClassUID(String uid) {
        dcmobj.putString(Tag.MEDIA_STORAGE_SOP_CLASS_UID, VR.UI, uid);
    }

    public String getImplementationClassUID() {
        return dcmobj.getString(Tag.IMPLEMENTATION_CLASS_UID);
    }

    public void setImplementationClassUID(String uid) {
        dcmobj.putString(Tag.IMPLEMENTATION_CLASS_UID, VR.UI, uid);
    }
    
    public String getImplementationVersionName() {
        return dcmobj.getString(Tag.IMPLEMENTATION_VERSION_NAME);
    }

    public void setImplementationVersionName(String name) {
        dcmobj.putString(Tag.IMPLEMENTATION_VERSION_NAME, VR.SH, name);
    }

    public String getTransferSyntaxUID() {
        return dcmobj.getString(Tag.TRANSFER_SYNTAX_UID);
    }

    public void setTransferSyntaxUID(String uid) {
        dcmobj.putString(Tag.TRANSFER_SYNTAX_UID, VR.UI, uid);
    }
    
    public String getSourceApplicationEntityTitle() {
        return dcmobj.getString(Tag.SOURCE_APPLICATION_ENTITY_TITLE);
    }

    public void setSourceApplicationEntityTitle(String ae) {
        dcmobj.putString(Tag.SOURCE_APPLICATION_ENTITY_TITLE, VR.AE, ae);
    }

    public String getPrivateInformationCreatorUID() {
        return dcmobj.getString(Tag.PRIVATE_INFORMATION_CREATOR_UID);
    }

    public void setPrivateInformationCreatorUID(String uid) {
        dcmobj.putString(Tag.PRIVATE_INFORMATION_CREATOR_UID, VR.UI, uid);
    }
    
    public byte[] getPrivateInformation() {
        return dcmobj.getBytes(Tag.PRIVATE_INFORMATION);
    }

    public void setPrivateInformation(byte[] ob) {
        dcmobj.putBytes(Tag.FILE_META_INFORMATION_VERSION, VR.OB, ob);
    }

}
