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
