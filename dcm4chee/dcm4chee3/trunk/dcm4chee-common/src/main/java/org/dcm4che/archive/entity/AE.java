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

import java.util.StringTokenizer;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * org.dcm4che.archive.entity.AE
 * 
 * @author <a href="mailto:damien.daddy@gmail.com">Damien Evans</a>
 */
@Entity
@Table(name = "ae")
@SequenceGenerator(name = "SEQ_STORE", sequenceName = "ae_s")
public class AE extends EntityBase {

    static final long serialVersionUID = 9128665077590256461L;

    static final String[] EMPTY_STRING_ARRAY = {};

    @Column(name = "aet", nullable = false)
    private String title;

    @Column(name = "hostname", nullable = false)
    private String hostname;

    @Column(name = "port", nullable = false)
    private int port;

    @Column(name = "cipher_suites")
    private String cipherSuites;

    @Column(name = "pat_id_issuer")
    private String issuerOfPatientID;

    @Column(name = "ae_desc")
    private String description;

    /**
     * Default constructor.
     */
    public AE() {
        super();
    }

    /**
     * Create an AE and initialize all of its fields.
     * 
     * @param pk
     *            The primary key of the AE record in the database. May be null
     *            if the AE is new.
     * @param aet
     *            The AE title.
     * @param aeHost
     *            The host name.
     * @param portNum
     *            The port number of the AE title.
     * @param cipher
     *            Security protocols in use by the AE.
     * @param issuer
     *            The issuer of the patient id where this AE is located/used.
     * @param desc
     *            A description of the AE.
     */
    public AE(Long pk, String aet, String aeHost, int portNum, String cipher,
            String issuer, String desc) {
        setPk(pk);
        this.title = aet;
        this.hostname = aeHost;
        this.port = portNum;
        this.issuerOfPatientID = issuer;
        this.cipherSuites = cipher;
        this.description = desc;
    }

    /**
     * @return the cipherSuites
     */
    public String getCipherSuites() {
        return cipherSuites;
    }

    /**
     * @param cipherSuites
     *            the cipherSuites to set
     */
    public void setCipherSuites(String cipherSuites) {
        this.cipherSuites = cipherSuites;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description
     *            the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the hostname
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * @param hostname
     *            the hostname to set
     */
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    /**
     * @return the issuerOfPatientID
     */
    public String getIssuerOfPatientID() {
        return issuerOfPatientID;
    }

    /**
     * @param issuerOfPatientID
     *            the issuerOfPatientID to set
     */
    public void setIssuerOfPatientID(String issuerOfPatientID) {
        this.issuerOfPatientID = issuerOfPatientID;
    }

    /**
     * @return the port
     */
    public int getPort() {
        return port;
    }

    /**
     * @param port
     *            the port to set
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title
     *            the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    private String getProtocol() {
        if (cipherSuites == null || cipherSuites.length() == 0) {
            return "dicom";
        }
        if ("SSL_RSA_WITH_NULL_SHA".equals(cipherSuites)) {
            return "dicom-tls.nodes";
        }
        if ("SSL_RSA_WITH_3DES_EDE_CBC_SHA".equals(cipherSuites)) {
            return "dicom-tls.3des";
        }
        if ("TLS_RSA_WITH_AES_128_CBC_SHA,SSL_RSA_WITH_3DES_EDE_CBC_SHA"
                .equals(cipherSuites)) {
            return "dicom-tls.aes";
        }
        return "dicom-tls";
    }

    public String[] getCipherSuiteArray() {
        if (cipherSuites == null || cipherSuites.length() == 0) {
            return EMPTY_STRING_ARRAY;
        }
        StringTokenizer stk = new StringTokenizer(cipherSuites, " ,");
        String[] retval = new String[stk.countTokens()];
        for (int i = 0; i < retval.length; ++i) {
            retval[i] = stk.nextToken();
        }
        return retval;
    }

    public boolean isTLS() {
        return cipherSuites != null && cipherSuites.length() != 0;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(64);
        sb.append(getProtocol()).append("://").append(title).append('@')
                .append(hostname).append(':').append(port);
        return sb.toString();
    }
}
