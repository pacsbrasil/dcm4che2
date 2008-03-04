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
 * Agfa-Gevaert AG.
 * Portions created by the Initial Developer are Copyright (C) 2008
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
package org.dcm4chee.archive.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Feb 28, 2008
 */
@Entity
@EntityListeners( { EntityLogger.class })
@Table(name = "code")
public class Code implements Serializable {

    private static final long serialVersionUID = 3626021926959276349L;

    @Id
    @GeneratedValue
    @Column(name = "pk")
    private long pk;

    @Column(name = "code_value", nullable = false)
    private String codeValue;

    @Column(name = "code_designator", nullable = false)
    private String codingSchemeDesignator;

    @Column(name = "code_version")
    private String codingSchemeVersion;

    @Column(name = "code_meaning")
    private String codeMeaning;

    public long getPk() {
        return pk;
    }

    public String getCodeValue() {
        return codeValue;
    }

    public String getCodingSchemeDesignator() {
        return codingSchemeDesignator;
    }

    public String getCodingSchemeVersion() {
        return codingSchemeVersion;
    }

    public String getCodeMeaning() {
        return codeMeaning;
    }

    @Override
    public String toString() {
        return '(' + codeValue
                + ", " + codingSchemeDesignator
                + (codingSchemeVersion != null
                        ? ';' + codingSchemeVersion
                        : "")
                + ", \"codeMeaning\""
                + ')';
    }

    public void setAttributes(DicomObject attrs) {
        this.codeValue = attrs.getString(Tag.CodeValue);
        this.codingSchemeDesignator = attrs
                .getString(Tag.CodingSchemeDesignator);
        this.codingSchemeVersion = attrs.getString(Tag.CodingSchemeVersion);
        this.codeMeaning = attrs.getString(Tag.CodeMeaning);
    }

}
