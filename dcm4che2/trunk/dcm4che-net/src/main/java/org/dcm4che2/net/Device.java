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
 * Damien Evans <damien@theevansranch.com>
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

package org.dcm4che2.net;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

/**
 * DICOM Part 15, Annex H compliant description of a DICOM enabled system or
 * device. This is used to describe a DICOM-enabled network endpoint in terms of
 * its physical attributes (serial number, manufacturer, etc.), its context
 * (issuer of patient ids used by the device, etc.), as well as its capabilities
 * (TLS-enabled, AE titles used, etc.).
 * 
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision$ $Date$
 * @since Nov 25, 2005
 * 
 */
public class Device
{
    private String deviceName = "";
    private String description;
    private String manufacturer;
    private String manufacturerModelName;
    private String stationName;
    private String deviceSerialNumber;
    private String issuerOfPatientID;
    private String[] softwareVersion = {};
    private String[] primaryDeviceType = {};
    private String[] institutionName = {};
    private String[] institutionAddress = {};
    private String[] institutionalDepartmentName = {};
    private X509Certificate[][] authorizedNodeCertificate = {};
    private X509Certificate[][] thisNodeCertificate = {};
    private Object[] relatedDeviceConfiguration = {};
    private Object[] vendorDeviceData = {};
    private int associationReaperPeriod = 10000;
    private boolean installed = true;

    private NetworkConnection[] networkConnection = {};
    private NetworkApplicationEntity[] networkAE = {};

    private SSLContext sslContext;
    private SecureRandom random;
    private AssociationReaper reaper;

    /**
     * Default constructor.
     */
    public Device()
    {
    }

    /**
     * Constructor which sets the name of this device.
     * 
     * @param deviceName String
     */
    public Device(String deviceName)
    {
        this.deviceName = deviceName;
    }

    /**
     * Get the <code>AssociationReaper</code> which monitors the open
     * associations made by this device. If there is no reaper, one will be
     * created.
     * <p>
     * Synchronized for concurrent access.
     * 
     * @return AssociationReaper
     */
    synchronized final AssociationReaper getAssociationReaper()
    {
        if (reaper == null)
            reaper = new AssociationReaper(getAssociationReaperPeriod());
        return reaper;
    }

    /**
     * Get the name of this device.
     * 
     * @return A String containing the device name.
     */
    public final String getDeviceName()
    {
        return deviceName;
    }

    /**
     * Set the name of this device.
     * <p>
     * This should be a unique name for this device. It is restricted to legal
     * LDAP names, and not constrained by DICOM AE Title limitations.
     * 
     * @param deviceName A String containing the device name.
     */
    public final void setDeviceName(String deviceName)
    {
        this.deviceName = deviceName;
    }

    /**
     * Get the description of this device.
     * 
     * @return A String containing the device description.
     */
    public final String getDescription()
    {
        return description;
    }

    /**
     * Set the description of this device.
     * 
     * @param description A String containing the device description.
     */
    public final void setDescription(String description)
    {
        this.description = description;
    }

    /**
     * Get the manufacturer of this device.
     * 
     * @return A String containing the device manufacturer.
     */
    public final String getManufacturer()
    {
        return manufacturer;
    }

    /**
     * Set the manufacturer of this device.
     * <p>
     * This should be the same as the value of Manufacturer (0008,0070) in SOP
     * instances created by this device.
     * 
     * @param manufacturer A String containing the device manufacturer.
     */
    public final void setManufacturer(String manufacturer)
    {
        this.manufacturer = manufacturer;
    }

    /**
     * Get the manufacturer model name of this device.
     * 
     * @return A String containing the device manufacturer model name.
     */
    public final String getManufacturerModelName()
    {
        return manufacturerModelName;
    }

    /**
     * Set the manufacturer model name of this device.
     * <p>
     * This should be the same as the value of Manufacturer Model Name
     * (0008,1090) in SOP instances created by this device.
     * 
     * @param manufacturerModelName A String containing the device manufacturer
     *            model name.
     */
    public final void setManufacturerModelName(String manufacturerModelName)
    {
        this.manufacturerModelName = manufacturerModelName;
    }

    /**
     * Get the software versions running on (or implemented by) this device.
     * 
     * @return A String array containing the software versions.
     */
    public final String[] getSoftwareVersion()
    {
        return softwareVersion;
    }

    /**
     * Set the software versions running on (or implemented by) this device.
     * <p>
     * This should be the same as the values of Software Versions (0018,1020) in
     * SOP instances created by this device.
     * 
     * @param softwareVersion A String array containing the software versions.
     */
    public final void setSoftwareVersion(String[] softwareVersion)
    {
        this.softwareVersion = softwareVersion;
    }

    /**
     * Get the station name belonging to this device.
     * 
     * @return A String containing the station name.
     */
    public final String getStationName()
    {
        return stationName;
    }

    /**
     * Set the station name belonging to this device.
     * <p>
     * This should be the same as the value of Station Name (0008,1010) in SOP
     * instances created by this device.
     * 
     * @param stationName A String containing the station name.
     */
    public final void setStationName(String stationName)
    {
        this.stationName = stationName;
    }

    /**
     * Get the serial number belonging to this device.
     * 
     * @return A String containing the serial number.
     */
    public final String getDeviceSerialNumber()
    {
        return deviceSerialNumber;
    }

    /**
     * Set the serial number of this device.
     * <p>
     * This should be the same as the value of Device Serial Number (0018,1000)
     * in SOP instances created by this device.
     * 
     * @param deviceSerialNumber A String containing the serial number.
     */
    public final void setDeviceSerialNumber(String deviceSerialNumber)
    {
        this.deviceSerialNumber = deviceSerialNumber;
    }

    /**
     * Get the type codes associated with this device.
     * 
     * @return A String array containing the type codes of this device.
     */
    public final String[] getPrimaryDeviceType()
    {
        return primaryDeviceType;
    }

    /**
     * Set the type codes associated with this device.
     * <p>
     * Represents the kind of device and is most applicable for acquisition
     * modalities. Types should be selected from the list of code values
     * (0008,0100) for Context ID 30 in PS3.16 when applicable.
     * 
     * @param primaryDeviceType
     */
    public final void setPrimaryDeviceType(String[] primaryDeviceType)
    {
        this.primaryDeviceType = primaryDeviceType;
    }

    /**
     * Get the institution name associated with this device; may be the site
     * where it resides or is operating on behalf of.
     * 
     * @return A String array containing the institution name values.
     */
    public final String[] getInstitutionName()
    {
        return institutionName;
    }

    /**
     * Set the institution name associated with this device; may be the site
     * where it resides or is operating on behalf of.
     * <p>
     * Should be the same as the value of Institution Name (0008,0080) in SOP
     * Instances created by this device.
     * 
     * @param names A String array containing the institution name values.
     */
    public final void setInstitutionName(String[] name)
    {
        this.institutionName = name;
    }

    /**
     * Set the address of the institution which operates this device.
     * 
     * @return A String array containing the institution address values.
     */
    public final String[] getInstitutionAddress()
    {
        return institutionAddress;
    }

    /**
     * Get the address of the institution which operates this device.
     * <p>
     * Should be the same as the value of Institution Address (0008,0081)
     * attribute in SOP Instances created by this device.
     * 
     * @param addr A String array containing the institution address values.
     */
    public final void setInstitutionAddress(String[] addr)
    {
        this.institutionAddress = addr;
    }

    /**
     * Get the department name associated with this device.
     * 
     * @return A String array containing the dept. name values.
     */
    public final String[] getInstitutionalDepartmentName()
    {
        return institutionalDepartmentName;
    }

    /**
     * Set the department name associated with this device.
     * <p>
     * Should be the same as the value of Institutional Department Name
     * (0008,1040) in SOP Instances created by this device.
     * 
     * @param name A String array containing the dept. name values.
     */
    public final void setInstitutionalDepartmentName(String[] name)
    {
        this.institutionalDepartmentName = name;
    }

    /**
     * Get the issuer of patient IDs for this device.
     * 
     * @return A String containing the PID issuer value.
     */
    public final String getIssuerOfPatientID()
    {
        return issuerOfPatientID;
    }

    /**
     * Set the issuer of patient IDs for this device.
     * <p>
     * Default value for the Issuer of Patient ID (0010,0021) for SOP Instances
     * created by this device. May be overridden by the values received in a
     * worklist or other source.
     * 
     * @param issuerOfPatientID A String containing the PID issuer value.
     */
    public final void setIssuerOfPatientID(String issuerOfPatientID)
    {
        this.issuerOfPatientID = issuerOfPatientID;
    }

    /**
     * Get references to any related configuration and descriptive information.
     * 
     * @return An Object array of related references.
     */
    public final Object[] getRelatedDeviceConfiguration()
    {
        return relatedDeviceConfiguration;
    }

    /**
     * Set references to any related configuration and descriptive information.
     * <p>
     * The DNs of related device descriptions outside the DICOM Configuration
     * hierarchy. Can be used to link the DICOM Device object to additional LDAP
     * objects instantiated from other schema and used for separate
     * administrative purposes.
     * 
     * @param relatedDevice An Object array of related references.
     */
    public final void setRelatedDeviceConfiguration(Object[] relatedDevice)
    {
        this.relatedDeviceConfiguration = relatedDevice;
    }

    /**
     * Get the certificates of nodes that are authorized to connect to this
     * device.
     * 
     * @return An X509Certificate two dimensional array containing the
     *         certificates.
     */
    public final X509Certificate[][] getAuthorizedNodeCertificate()
    {
        return authorizedNodeCertificate;
    }

    /**
     * Set the certificates of nodes that are authorized to connect to this
     * device.
     * 
     * @param cert An X509Certificate two dimensional array containing the
     *            certificates.
     */
    public final void setAuthorizedNodeCertificate(X509Certificate[][] cert)
    {
        this.authorizedNodeCertificate = cert;
    }

    /**
     * Get the public certificate for this device.
     * 
     * @return An X509Certificate two dimensional array containing the
     *         certificates.
     */
    public final X509Certificate[][] getThisNodeCertificate()
    {
        return thisNodeCertificate;
    }

    /**
     * Set the public certificates for this device.
     * 
     * @param cert An X509Certificate two dimensional array containing the
     *            certificates.
     */
    public final void setThisNodeCertificate(X509Certificate[][] cert)
    {
        this.thisNodeCertificate = cert;
    }

    /**
     * Get device specific vendor configuration information
     * 
     * @return An Object array of the device data.
     */
    public final Object[] getVendorDeviceData()
    {
        return vendorDeviceData;
    }

    /**
     * Set device specific vendor configuration information
     * 
     * @param vendorDeviceData An Object array of the device data.
     */
    public final void setVendorDeviceData(Object[] vendorDeviceData)
    {
        this.vendorDeviceData = vendorDeviceData;
    }

    /**
     * Get a boolean to indicate whether this device is presently installed on
     * the network. (This is useful for pre-configuration, mobile vans, and
     * similar situations.)
     * 
     * @return A boolean which will be true if this device is installed.
     */
    public final boolean isInstalled()
    {
        return installed;
    }

    /**
     * Get a boolean to indicate whether this device is presently installed on
     * the network. (This is useful for pre-configuration, mobile vans, and
     * similar situations.)
     * 
     * @param installed A boolean which will be true if this device is
     *            installed.
     */
    public final void setInstalled(boolean installed)
    {
        this.installed = installed;
    }

    /**
     * Get all of the <code>NetworkApplicationEntity</code> objects that this
     * device is known by. The entities define how the device's DICOM services
     * are identified on the network, as well as the properties and capabilities
     * of those services.
     * 
     * @return An array of <code>NetworkApplicationEntity</code> objects.
     */
    public final NetworkApplicationEntity[] getNetworkApplicationEntity()
    {
        return networkAE;
    }

    /**
     * Set the <code>NetworkApplicationEntity</code> object that this device
     * is known by. The entity defines how the device's DICOM services are
     * identified on the network, as well as the properties and capabilities of
     * those services.
     * 
     * @param networkAE A <code>NetworkApplicationEntity</code> object.
     */
    public final void setNetworkApplicationEntity(
            NetworkApplicationEntity networkAE)
    {
        setNetworkApplicationEntity(new NetworkApplicationEntity[] { networkAE });
    }

    /**
     * Set all of the <code>NetworkApplicationEntity</code> objects that this
     * device is known by. The entities define how the device's DICOM services
     * are identified on the network, as well as the properties and capabilities
     * of those services.
     * 
     * @param networkAE An array of <code>NetworkApplicationEntity</code>
     *            objects.
     */
    public final void setNetworkApplicationEntity(
            NetworkApplicationEntity[] networkAE)
    {
        for (int i = 0; i < networkAE.length; i++)
            networkAE[i].setDevice(this);

        this.networkAE = networkAE;
    }

    /**
     * Get the <code>NetworkConnection</code> objects associated with this
     * device. These will coincide with the TCP ports used by the device.
     * 
     * @return An array of <code>NetworkConnection</code> objects.
     */
    public final NetworkConnection[] getNetworkConnection()
    {
        return networkConnection;
    }

    /**
     * Get the <code>NetworkConnection</code> object associated with this
     * device. This will coincide with the TCP port used by the device.
     * 
     * @param networkConnection A<code>NetworkConnection</code> object.
     */
    public final void setNetworkConnection(NetworkConnection networkConnection)
    {
        setNetworkConnection(new NetworkConnection[] { networkConnection });
    }

    /**
     * Get the <code>NetworkConnection</code> objects associated with this
     * device. These will coincide with the TCP ports used by the device.
     * 
     * @param An array of <code>NetworkConnection</code> objects.
     */
    public final void setNetworkConnection(NetworkConnection[] networkConnection)
    {
        for (int i = 0; i < networkConnection.length; i++)
            networkConnection[i].setDevice(this);

        this.networkConnection = networkConnection;
    }

    /**
     * Get the period of time (in milliseconds) which associations made by this
     * device may remain idle.
     * 
     * @return An int signifying association idle period in milliseconds.
     */
    public final int getAssociationReaperPeriod()
    {
        return associationReaperPeriod;
    }

    /**
     * Set the period of time (in milliseconds) which associations made by this
     * device may remain idle.
     * 
     * @param associationReaperPeriod An int signifying association idle period
     *            in milliseconds.
     */
    public final void setAssociationReaperPeriod(int associationReaperPeriod)
    {
        this.associationReaperPeriod = associationReaperPeriod;
    }

    /**
     * Initialize transport layer security (TLS) for network interactions using
     * the device's certificate (as returned by
     * <code>getThisNodeCertificate()</code>).
     * 
     * @param key The <code>KeyStore</code> containing the keys needed for
     *            secure network interaction with another device.
     * @param password A char array containing the password used to access the
     *            key.
     * @throws GeneralSecurityException
     */
    public void initTLS(KeyStore key, char[] password)
            throws GeneralSecurityException
    {
        KeyStore trust = KeyStore.getInstance(KeyStore.getDefaultType());
        addCertificate(trust, getThisNodeCertificate());
        addCertificate(trust, getAuthorizedNodeCertificate());
        initTLS(key, password, trust);
    }

    private void addCertificate(KeyStore trust, final X509Certificate[][] certs)
            throws KeyStoreException
    {
        for (int i = 0; i < certs.length; i++)
            for (int j = 0; j < certs[i].length; j++)
                trust.setCertificateEntry(certs[i][j].getSubjectDN().getName(),
                        certs[i][j]);
    }

    /**
     * Initialize transport layer security (TLS) for network interactions using
     * the trusted material (certificates, etc.) contained in the "trust"
     * parameter..
     * 
     * @param key The <code>KeyStore</code> containing the keys needed for
     *            secure network interaction with another device.
     * @param password A char array containing the password used to access the
     *            key.
     * @param trust The <code>KeyStore</code> object containing the source of
     *            certificates and trusted material.
     * @throws GeneralSecurityException
     */
    public void initTLS(KeyStore key, char[] password, KeyStore trust)
            throws GeneralSecurityException
    {
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory
                .getDefaultAlgorithm());
        kmf.init(key, password);
        TrustManagerFactory tmf = TrustManagerFactory
                .getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trust);
        if (random == null)
            random = SecureRandom.getInstance("SHA1PRNG");
        if (sslContext == null)
            sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), random);
    }

    /**
     * Bind to a socket and start listening for DICOM associations.
     * 
     * @param executor The <code>Executor</code> threading implementation to
     *            use when binding to a socket.
     * @throws IOException
     */
    public void startListening(Executor executor) throws IOException
    {
        for (int i = 0; i < networkConnection.length; i++)
        {
            NetworkConnection c = networkConnection[i];
            if (c.isInstalled() && c.isListening())
                c.bind(executor);
        }
    }

    /**
     * Unbind from all active socket connections.
     */
    public void stopListening()
    {
        for (int i = 0; i < networkConnection.length; i++)
        {
            NetworkConnection c = networkConnection[i];
            c.unbind();
        }
    }

    /**
     * Get the secure socket layer context which was created by calling
     * <code>initTLS(...)</code>.
     * 
     * @return The initialized <code>SSLContext</code>.
     * @throws IllegalStateException if the TLS context has not been
     *             initialized.
     */
    final SSLContext getSSLContext()
    {
        if (sslContext == null)
            throw new IllegalStateException("TLS Context not initialized!");
        return sslContext;
    }

    /**
     * Get a specific <code>NetworkApplicationEntity</code> object by it's AE
     * title.
     * 
     * @param aet A String containing the AE title.
     * @return The <code>NetworkApplicationEntity</code> corresponding to the
     *         aet parameter.
     */
    public NetworkApplicationEntity getNetworkApplicationEntity(String aet)
    {
        for (int i = 0; i < networkAE.length; i++)
        {
            NetworkApplicationEntity ae = networkAE[i];
            String aeti = ae.getAETitle();
            if (aeti == null || aeti.equals(aet))
                return ae;
        }
        return null;
    }
}
