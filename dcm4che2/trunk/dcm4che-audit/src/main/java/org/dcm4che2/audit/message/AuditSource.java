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
 
package org.dcm4che2.audit.message;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Identifies the system that detected the auditable event and created 
 * the audit message.
 * 
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Nov 17, 2006
 */
public class AuditSource extends BaseElement {

    public static final String END_USER_DISPLAY_DEVICE = "1";
    public static final String DATA_ACQUISITION_DEVICE = "2";
    public static final String WEB_SERVER_PROCESS = "3";
    public static final String APPLICATION_SERVER_PROCESS = "4";
    public static final String DATABASE_SERVER_PROCESS = "5";
    public static final String SECURITY_SERVER = "6";
    public static final String ISO_LEVEL_1_3_NETWORK_COMPONENT = "7";
    public static final String ISO_LEVEL_4_6_OPERATING_SOFTWARE = "8";
    public static final String OTHER = "9";
    
    private final ArrayList auditSourceTypeCodes = new ArrayList(1);
    
    public AuditSource(String id) {
        super("AuditSourceIdentification");
        addAttribute("AuditSourceID", id);
    }
    
    public final String getAuditSourceID() {
        return (String) getAttribute("AuditSourceID");
    }
    
    public final String getAuditEnterpriseSiteID() {
        return (String) getAttribute("AuditEnterpriseSiteID");
    }
    
    public final AuditSource setAuditEnterpriseSiteID(String id) {
        addAttribute("AuditEnterpriseSiteID", id);
        return this;
    }

    public final List getAuditSourceTypeCodes() {
        return Collections.unmodifiableList(auditSourceTypeCodes);
    }
           
    public AuditSource addAuditSourceTypeCode(
            TypeCode code) {
        if (code == null) {
            throw new NullPointerException();
        }
        auditSourceTypeCodes.add(code);
        return this;
    }

    protected boolean isEmpty() {
        return auditSourceTypeCodes.isEmpty();
    }
        
    protected void outputContent(Writer out) throws IOException {
        outputChilds(out, auditSourceTypeCodes);
    }

    public static class TypeCode extends CodeElement {

        public TypeCode(String code) {
            super("AuditSourceTypeCode", code);
            if (code.length() != 1 || "123456789".indexOf(code.charAt(0)) == -1) {
                throw new IllegalArgumentException(
                        "Illegal Audit Source Type code: " + code);
            }
        }
    }
 }
