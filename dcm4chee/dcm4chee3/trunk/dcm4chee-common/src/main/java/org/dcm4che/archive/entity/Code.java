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
 * Accurate Software Design, LLC.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunterze@gmail.com>
 * Franz Willer <franz.willer@gwi-ag.com>
 * Justin Falk <jfalkmu@gmail.com>
 * Damien Evans <damien.daddy@gmail.com>
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
package org.dcm4che.archive.entity;

import java.util.Collection;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PersistenceException;
import javax.persistence.Table;

import org.apache.log4j.Logger;
import org.dcm4che.archive.dao.CodeDAO;
import org.dcm4che.archive.dao.ContentCreateException;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.dict.Tags;

/**
 * org.dcm4che.archive.entity.Code
 * 
 * @author <a href="mailto:damien.daddy@gmail.com">Damien Evans</a>
 */
@Entity
@Table(name = "code")
public class Code extends EntityBase {

    private static final long serialVersionUID = -690797565580620744L;

    private static final Logger log = Logger.getLogger(Code.class.getName());

    @Column(name = "code_value", nullable = false)
    private String codeValue;

    @Column(name = "code_designator", nullable = false)
    private String codingSchemeDesignator;

    @Column(name = "code_version")
    private String codingSchemeVersion;

    @Column(name = "code_meaning")
    private String codeMeaning;

    /**
     * @return the codingSchemeDesignator
     */
    public String getCodingSchemeDesignator() {
        return codingSchemeDesignator;
    }

    /**
     * @param codingSchemeDesignator
     *            the codingSchemeDesignator to set
     */
    public void setCodingSchemeDesignator(String codeDesignator) {
        this.codingSchemeDesignator = codeDesignator;
    }

    /**
     * @return the codeMeaning
     */
    public String getCodeMeaning() {
        return codeMeaning;
    }

    /**
     * @param codeMeaning
     *            the codeMeaning to set
     */
    public void setCodeMeaning(String codeMeaning) {
        this.codeMeaning = codeMeaning;
    }

    /**
     * @return the codeValue
     */
    public String getCodeValue() {
        return codeValue;
    }

    /**
     * @param codeValue
     *            the codeValue to set
     */
    public void setCodeValue(String codeValue) {
        this.codeValue = codeValue;
    }

    /**
     * @return the codingSchemeVersion
     */
    public String getCodingSchemeVersion() {
        return codingSchemeVersion;
    }

    /**
     * @param codingSchemeVersion
     *            the codingSchemeVersion to set
     */
    public void setCodingSchemeVersion(String codeVersion) {
        this.codingSchemeVersion = codeVersion;
    }

    public String toString() {
        return "Code[pk=" + getPk() + ", value=" + codeValue + ", designator="
                + codingSchemeDesignator + ", version=" + codingSchemeVersion
                + ", meaning=" + codeMeaning + "]";
    }

    public static Code valueOf(CodeDAO codeDAO, Dataset item)
            throws ContentCreateException, PersistenceException {
        if (item == null)
            return null;

        final String value = item.getString(Tags.CodeValue);
        final String designator = item.getString(Tags.CodingSchemeDesignator);
        final String version = item.getString(Tags.CodingSchemeVersion);
        final String meaning = item.getString(Tags.CodeMeaning);
        List<Code> codes = codeDAO.findByValueAndDesignator(value, designator);
        for (int i = 0; i < codes.size(); i++) {
            final Code code = codes.get(i);
            if (version == null) {
                return code;
            }
            final String version2 = code.getCodingSchemeVersion();
            if (version2 == null || version2.equals(version)) {
                return code;
            }
        }
        return codeDAO.create(value, designator, version, meaning);
    }

    public static void addCodesTo(CodeDAO codeDAO, DcmElement sq,
            Collection<Code> codes) throws ContentCreateException,
            PersistenceException {
        if (sq == null || sq.isEmpty())
            return;
        Dataset item = sq.getItem(0);
        if (item.isEmpty())
            return;
        codes.add(Code.valueOf(codeDAO, item));
        for (int i = 1, n = sq.countItems(); i < n; i++) {
            codes.add(Code.valueOf(codeDAO, sq.getItem(i)));
        }
    }

    public static boolean checkCodes(String prompt, DcmElement sq) {
        if (sq == null || sq.isEmpty())
            return true;
        for (int i = 0, n = sq.countItems(); i < n; i++) {
            Dataset item = sq.getItem(i);
            if (!item.containsValue(Tags.CodeValue)) {
                log.warn("Missing Code Value (0008,0100) in " + prompt
                        + " - ignore all items");
                return false;
            }
            if (!item.containsValue(Tags.CodingSchemeDesignator)) {
                log.warn("Missing Coding Scheme Designator (0008,0102) in "
                        + prompt + " - ignore all items");
                return false;
            }
            if (!item.containsValue(Tags.CodeMeaning)) {
                log.warn("Missing Code Meaning (0008,0104) in " + prompt
                        + " - ignore all items");
                return false;
            }
        }
        return true;
    }
}
