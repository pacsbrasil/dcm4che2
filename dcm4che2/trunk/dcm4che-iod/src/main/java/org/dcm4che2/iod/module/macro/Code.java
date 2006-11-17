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
 * Gunter Zeilinger <gunterze@gmail.com>
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

package org.dcm4che2.iod.module.macro;

import java.util.Date;

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4che2.iod.module.Module;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision$ $Date$
 * @since Jun 9, 2006
 * 
 */
public class Code extends Module {
    
    public Code(DicomObject dcmobj) {
        super(dcmobj);
    }

    public Code() {
        this(new BasicDicomObject());
    }

    public static Code[] toCodes(DicomElement sq) {
        if (sq == null || !sq.hasItems()) {
            return null;
        }
        Code[] a = new Code[sq.countItems()];
        for (int i = 0; i < a.length; i++) {
            a[i] = new Code(sq.getDicomObject(i));
        }
        return a;
    }
    
    public String getCodeValue() {
        return dcmobj.getString(Tag.CODE_VALUE);
    }

    public void setCodeValue(String s) {
        dcmobj.putString(Tag.CODE_VALUE, VR.SH, s);
    }

    public String getCodingSchemeDesignator() {
        return dcmobj.getString(Tag.CODING_SCHEME_DESIGNATOR);
    }

    public void setCodingSchemeDesignator(String s) {
        dcmobj.putString(Tag.CODING_SCHEME_DESIGNATOR, VR.SH, s);
    }

    public String getCodingSchemeVersion() {
        return dcmobj.getString(Tag.CODING_SCHEME_VERSION);
    }

    public void setCodingSchemeVersion(String s) {
        dcmobj.putString(Tag.CODING_SCHEME_VERSION, VR.SH, s);
    }

    public String getCodeMeaning() {
        return dcmobj.getString(Tag.CODE_MEANING);
    }

    public void setCodeMeaning(String s) {
        dcmobj.putString(Tag.CODE_MEANING, VR.LO, s);
    }

    public String getContextIdentifier() {
        return dcmobj.getString(Tag.CONTEXT_IDENTIFIER);
    }

    public void setContextIdentifier(String s) {
        dcmobj.putString(Tag.CONTEXT_IDENTIFIER, VR.CS, s);
    }

    public String getMappingResource() {
        return dcmobj.getString(Tag.MAPPING_RESOURCE);
    }

    public void setMappingResource(String s) {
        dcmobj.putString(Tag.MAPPING_RESOURCE, VR.CS, s);
    }

    public Date getContextGroupVersion() {
        return dcmobj.getDate(Tag.CONTEXT_GROUP_VERSION);
    }

    public void setContextGroupVersion(Date d) {
        dcmobj.putDate(Tag.MAPPING_RESOURCE, VR.DT, d);
    }

    public String getContextGroupExtensionFlag() {
        return dcmobj.getString(Tag.CONTEXT_GROUP_EXTENSION_FLAG);
    }

    public void setContextGroupExtensionFlag(String s) {
        dcmobj.putString(Tag.CONTEXT_GROUP_LOCAL_VERSION, VR.CS, s);
    }

    public Date getContextGroupLocalVersion() {
        return dcmobj.getDate(Tag.CONTEXT_GROUP_LOCAL_VERSION);
    }

    public void setContextGroupLocalVersion(Date d) {
        dcmobj.putDate(Tag.CONTEXT_GROUP_LOCAL_VERSION, VR.DT, d);
    }

    public String getContextGroupExtensionCreatorUID() {
        return dcmobj.getString(Tag.CONTEXT_GROUP_EXTENSION_CREATOR_UID);
    }

    public void setContextGroupExtensionCreatorUID(String s) {
        dcmobj.putString(Tag.CONTEXT_GROUP_EXTENSION_CREATOR_UID, VR.UI, s);
    }

}
