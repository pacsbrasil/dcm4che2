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
 * Portions created by the Initial Developer are Copyright (C) 2002-2008
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
package org.dcm4che2.cda;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author Gunter Zeilinger<gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Mar 10, 2008
 */
public class ClinicalDocument extends BaseElement {

    private static final String XML_VERSION_1_0_ENCODING_UTF_8 = 
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

    private static boolean incXMLDecl = false;

    private static final BaseElement TYPE_ID =
            new BaseElement("typeId",
                    new String[] { "extension", "root" },
                    new Object[] { "POCD_HD000040", "2.16.840.1.113883.1.3" }
            );

    private ID id;
    private Code code;
    private Title title;
    private EffectiveTime effectiveTime;
    private ConfidentialityCode confidentialityCode;
    private LanguageCode languageCode;
    private List<RecordTarget> recordTargets;
    private List<Author> authors;
    private DataEnterer dataEnterer;
    private Custodian custodian;
    private LegalAuthenticator legalAuthenticator;
    private List<DocumentationOf> documentationOfs;
    private Component component;

    public ClinicalDocument() {
        super("ClinicalDocument",
                new String[] { 
                    "xmlns", 
                    "xmlns:xsi",
                    "classCode",
                    "moodCode",
                    "xsi:schemaLocation"
                },
                new Object[] {
                    "urn:hl7-org:v3",
                    "http://www.w3.org/2001/XMLSchema-instance",
                    "DOCCLIN",
                    "EVN",
                    "urn:hl7-org:v3 CDA.xsd"
                });
    }

    public static final boolean isIncludeXMLDeclaration() {
        return incXMLDecl;
    }

    public static final void setIncludeXMLDeclaration(boolean incXMLDecl) {
        ClinicalDocument.incXMLDecl = incXMLDecl;
    }

    public ID getId() {
        return id;
    }

    public void setId(ID id) {
        this.id = id;
    }

    public Code getCode() {
        return code;
    }

    public void setCode(Code code) {
        this.code = code;
    }

    public Title getTitle() {
        return title;
    }

    public void setTitle(Title title) {
        this.title = title;
    }

    public EffectiveTime getEffectiveTime() {
        return effectiveTime;
    }

    public void setEffectiveTime(EffectiveTime effectiveTime) {
        this.effectiveTime = effectiveTime;
    }

    public CodeElement getConfidentialityCode() {
        return confidentialityCode;
    }

    public void setConfidentialityCode(ConfidentialityCode confidentialityCode) {
        this.confidentialityCode = confidentialityCode;
    }

    public LanguageCode getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(LanguageCode code) {
        this.languageCode = code;
    }

    public List<RecordTarget> getRecordTargets() {
        return recordTargets;
    }

    public void setRecordTargets(List<RecordTarget> recordTargets) {
        this.recordTargets = recordTargets;
    }

    public RecordTarget getRecordTarget() {
        return recordTargets != null && !recordTargets.isEmpty()
                ? recordTargets.get(0) : null;
    }

    public void setRecordTarget(RecordTarget recordTarget) {
        this.recordTargets = Collections.singletonList(recordTarget);
    }

    public List<Author> getAuthors() {
        return authors;
    }

    public void setAuthors(List<Author> authors) {
        this.authors = authors;
    }

    public Author getAuthor() {
        return authors != null && !authors.isEmpty() ? authors.get(0) : null;
    }

    public void setAuthor(Author author) {
        this.authors = Collections.singletonList(author);
    }

    public DataEnterer getDataEnterer() {
        return dataEnterer;
    }

    public void setDataEnterer(DataEnterer dataEnterer) {
        this.dataEnterer = dataEnterer;
    }

    public Custodian getCustodian() {
        return custodian;
    }

    public void setCustodian(Custodian custodian) {
        this.custodian = custodian;
    }

    public LegalAuthenticator getLegalAuthenticator() {
        return legalAuthenticator;
    }

    public void setLegalAuthenticator(LegalAuthenticator legalAuthenticator) {
        this.legalAuthenticator = legalAuthenticator;
    }

    public List<DocumentationOf> getDocumentationOfs() {
        return documentationOfs;
    }

    public void setDocumentationOfs(List<DocumentationOf> documentationOfs) {
        this.documentationOfs = documentationOfs;
    }

    public DocumentationOf getDocumentationOf() {
        return documentationOfs != null && !documentationOfs.isEmpty()
                ? documentationOfs.get(0) : null;
    }

    public void setDocumentationOf(DocumentationOf documentationOf) {
        this.documentationOfs = Collections.singletonList(documentationOf);
    }

    public Component getComponent() {
        return component;
    }

    public void setComponent(Component component) {
        this.component = component;
    }

    @Override
    protected boolean isEmpty() {
        return false;
    }

    @Override
    public String toString() {
        return toString(1024);
    }    

    @Override
    public void writeTo(Writer out) throws IOException {
        if (incXMLDecl) {
            out.write(XML_VERSION_1_0_ENCODING_UTF_8);
        }
        super.writeTo(out);
    }

    @Override
    protected void writeContentTo(Writer out) throws IOException {
        writeTo(TYPE_ID, out);
        writeTo(id, out);
        writeTo(code, out);
        writeTo(title, out);
        writeTo(effectiveTime, out);
        writeTo(confidentialityCode, out);
        writeTo(languageCode, out);
        writeTo(recordTargets, out);
        writeTo(authors, out);
        writeTo(dataEnterer, out);
        writeTo(custodian, out);
        writeTo(legalAuthenticator, out);
        writeTo(documentationOfs, out);
        writeTo(component, out);
    }


    public static class Title extends TextElement {

        private Title(String text) {
            super("title", text);
        }

    }


    public static class EffectiveTime extends TimeElement {

        public EffectiveTime(Date time) {
            super("effectiveTime", time);
        }

    }

}
