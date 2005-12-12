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

package org.dcm4che2.net;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Iterator;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.dcm4che2.config.DeviceConfiguration;
import org.dcm4che2.config.NetworkApplicationEntity;
import org.dcm4che2.config.NetworkConnection;
import org.dcm4che2.data.UID;
import org.dcm4che2.net.pdu.AAssociateAC;
import org.dcm4che2.net.pdu.AAssociateRJException;
import org.dcm4che2.net.pdu.AAssociateRQ;
import org.dcm4che2.net.service.DicomService;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision$ $Date$
 * @since Nov 25, 2005
 *
 */
public class Device
{
    private final DeviceConfiguration config;
    private final ArrayList connectors;
    private final ArrayList aes;
    private Executor executor;
    private SSLContext sslContext;
    private SecureRandom random;
    private DicomServiceRegistry serviceRegistry = new DicomServiceRegistry();
    private AssociationReaper reaper;

    public Device(String deviceName)
    {
        this(new DeviceConfiguration(deviceName));        
    }
    
    public Device(DeviceConfiguration config)
    {
        if (config == null)
            throw new NullPointerException("config");
        
        this.config = config;
        
        final NetworkConnection[] connections = config.getNetworkConnection();
        this.connectors = new ArrayList(connections.length);
        for (int i = 0; i < connections.length; i++)
            addConnector(new Connector(connections[i]));
        
        final NetworkApplicationEntity[] nae = config.getNetworkApplicationEntity();
        this.aes = new ArrayList(nae.length);
        for (int i = 0; i < nae.length; i++)
            addApplicationEntity(new ApplicationEntity(nae[i]));
    }

    public final void setExecutor(Executor executor)
    {
        this.executor = executor;
    }

    public DeviceConfiguration getConfiguration()
    {
        config.setNetworkApplicationEntity(getNetworkApplicationEntity());
        config.setNetworkConnection(getNetworkConnection());
        return config;
    }

    public final NetworkApplicationEntity[] getNetworkApplicationEntity()
    {
        NetworkApplicationEntity[] a = new NetworkApplicationEntity[aes.size()];
        for (int i = 0; i < a.length; i++)
            a[i] = ((ApplicationEntity) aes.get(i)).getConfiguration();
        return a;
    }
    
    public NetworkConnection[] getNetworkConnection()
    {
        NetworkConnection[] a = new NetworkConnection[connectors.size()];
        for (int i = 0; i < a.length; i++)
            a[i] = ((Connector) connectors.get(i)).getConfiguration();
        return a;
    }
        
    public final String getDeviceName()
    {
        return config.getDeviceName();
    }
    
    public final void setDeviceName(String deviceName)
    {
        config.setDeviceName(deviceName);
    }
    
    public final String getDescription()
    {
        return config.getDeviceName();
    }
    
    public final void setDescription(String description)
    {
        config.setDescription(description);
    }
    
    public final String getManufactorer()
    {
        return config.getManufactorer();
    }
    
    public final void setManufactorer(String manufactorer)
    {
        config.setManufactorer(manufactorer);
    }
    
    public final String getManufactorerModelName()
    {
        return config.getManufactorerModelName();
    }
    
    public final void setManufactorerModelName(String manufactorerModelName)
    {
        config.setManufactorerModelName(manufactorerModelName);
    }
    
    public final String[] getSoftwareVersion()
    {
        return config.getSoftwareVersion();
    }
    
    public final void setSoftwareVersion(String[] softwareVersion)
    {
        config.setSoftwareVersion(softwareVersion);
    }
    
    public final String getStationName()
    {
        return config.getStationName();
    }
    
    public final void setStationName(String stationName)
    {
        config.setStationName(stationName);
    }
    
    public final String getDeviceSerialNumber()
    {
        return config.getDeviceSerialNumber();
    }
    
    public final void setDeviceSerialNumber(String deviceSerialNumber)
    {
        config.setDeviceSerialNumber(deviceSerialNumber);
    }
    
    public final String[] getPrimaryDeviceType()
    {
        return config.getPrimaryDeviceType();
    }
    
    public final void setPrimaryDeviceType(String[] primaryDeviceType)
    {
        config.setPrimaryDeviceType(primaryDeviceType);
    }
    
    public final String[] getInstitutionName()
    {
        return config.getInstitutionName();
    }
    
    public final void setInstitutionName(String[] name)
    {
        config.setInstitutionName(name);
    }
    
    public final String[] getInstitutionAddress()
    {
        return config.getInstitutionAddress();
    }
    
    public final void setInstitutionAddresses(String[] addr)
    {
        config.setInstitutionAddresses(addr);
    }
    
    public final String[] getInstitutionalDepartmentName()
    {
        return config.getInstitutionalDepartmentName();
    }
    
    public final void setInstitutionalDepartmentName(String[] name)
    {
        config.setInstitutionalDepartmentName(name);
    }
    
    public final String getIssuerOfPatientID()
    {
        return config.getDeviceName();
    }
    
    public final void setIssuerOfPatientID(String issuerOfPatientID)
    {
        config.setIssuerOfPatientID(issuerOfPatientID);
    }
    
    public final Object[] getRelatedDevice()
    {
        return config.getRelatedDevice();
    }
    
    public final void setRelatedDeviceReference(Object[] relatedDevice)
    {
        config.setRelatedDeviceReference(relatedDevice);
    }
    
    public final X509Certificate[] getAuthorizedNodeCertificate()
    {
        return config.getAuthorizedNodeCertificate();
    }
    
    public final void setAuthorizedNodeCertificate(X509Certificate[] cert)
    {
        config.setAuthorizedNodeCertificate(cert);
    }
    
    public final X509Certificate[] getThisNodeCertificate()
    {
        return config.getThisNodeCertificate();
    }
    
    public final void setThisNodeCertificate(X509Certificate[] cert)
    {
        config.setThisNodeCertificate(cert);
    }
    
    public final Object[] getVendorDeviceData()
    {
        return config.getVendorDeviceData();
    }
    
    public final void setVendorDeviceData(Object[] vendorDeviceData)
    {
        config.setVendorDeviceData(vendorDeviceData);
    }
    
    public final boolean isInstalled()
    {
        return config.isInstalled();
    }
    
    public final void setInstalled(boolean installed)
    {
        config.setInstalled(installed);
    }
    
    public final int getAssociationReaperPeriod()
    {
        return config.getAssociationReaperPeriod();
    }

    public final void setAssociationReaperPeriod(int period)
    {
        config.setAssociationReaperPeriod(period);
    }
    
    synchronized final Executor getExecutor()
    {
        if (executor == null)
            executor = new NewThreadExecutor(getDeviceName());
        return executor;
    }
 

    synchronized final AssociationReaper getAssociationReaper()
    {
        if (reaper == null)
            reaper = new AssociationReaper(getAssociationReaperPeriod());
        return reaper;
    }
    
    public void addConnector(Connector c)
    {
        if (c.getDevice() == null)
        {
            connectors.add(c);
            c.setDevice(this);
        }
        else if (c.getDevice() != this)
        {
            throw new IllegalArgumentException(
                    "Connector already associated to other device");
        }
    }

    
    public void addApplicationEntity(ApplicationEntity ae)
    {
        if (ae.getDevice() == null)
        {
            aes.add(ae);
            ae.setDefaultServiceRegistry(serviceRegistry);
            
            for (Iterator iter = ae.getConnectors().iterator(); iter.hasNext();)
                addConnector((Connector) iter.next());
            
            ae.setDevice(this);
            ae.initConnectors();
        }
        else if (ae.getDevice() != this)
        {
            throw new IllegalArgumentException(
                    "Application Entity already associated to other device");
        }
    }
        
    public void initTLS(KeyStore key, char[] password)
    throws GeneralSecurityException
    {
        KeyStore trust = KeyStore.getInstance(KeyStore.getDefaultType());
        addCertificate(trust, getThisNodeCertificate());
        addCertificate(trust, getAuthorizedNodeCertificate());
        initTLS(key, password, trust );
    }

    private void addCertificate(KeyStore trust, final X509Certificate[] certs)
    throws KeyStoreException
    {
        for (int i = 0; i < certs.length; i++)
            trust.setCertificateEntry(certs[i].getSubjectDN().getName(), certs[i]);            
    }
    
    public void initTLS(KeyStore key, char[] password, KeyStore trust)
    throws GeneralSecurityException
    {
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(
                KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(key, password);
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trust);
        if (random == null)
            random = SecureRandom.getInstance("SHA1PRNG");
        if (sslContext == null)
            sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), random);
    }
    
    
    Connector getConnector(NetworkConnection nc)
    {
        for (Iterator iter = connectors.iterator(); iter.hasNext();)
        {
            Connector c = (Connector) iter.next();
            if (c.getConfiguration() == nc)
                return c;
        }
        return null;
    }
    
    ApplicationEntity getApplicationEntity(NetworkApplicationEntity nae)
    {
        for (Iterator iter = aes.iterator(); iter.hasNext();)
        {
            ApplicationEntity ae = (ApplicationEntity) iter.next();
            if (ae.getConfiguration() == nae)
                return ae;
        }
        return null;
    }
    
    public void register(DicomService service)
    {
        serviceRegistry.register(service);        
    }
    
    public void startListening() throws IOException
    {
        for (Iterator iter = connectors.iterator(); iter.hasNext();)
        {
            Connector c = (Connector) iter.next();
            if (c.isInstalled() && c.isListening())
                c.bind();
        }
   }

    public void stopListening()
    {
        for (Iterator iter = connectors.iterator(); iter.hasNext();)
        {
            Connector c = (Connector) iter.next();
            c.unbind();
        }
    }

    final SSLContext getSSLContext()
    {
        if (sslContext == null)
            throw new IllegalStateException("TLS Context not initialized!");
        return sslContext;
    }

    AAssociateAC negotiate(Association a, AAssociateRQ rq)
    throws AAssociateRJException
    {
        if ((rq.getProtocolVersion() & 1) == 0)
            throw new AAssociateRJException(
                    AAssociateRJException.RESULT_REJECTED_PERMANENT,
                    AAssociateRJException.SOURCE_SERVICE_PROVIDER_ACSE,
                    AAssociateRJException.REASON_PROTOCOL_VERSION_NOT_SUPPORTED);
        if (!rq.getApplicationContext().equals(UID.DICOMApplicationContextName))
            throw new AAssociateRJException(
                    AAssociateRJException.RESULT_REJECTED_PERMANENT,
                    AAssociateRJException.SOURCE_SERVICE_USER,
                    AAssociateRJException.REASON_APP_CTX_NAME_NOT_SUPPORTED);
        String aet = rq.getCalledAET();
        for (Iterator iter = aes.iterator(); iter.hasNext();)
        {
            ApplicationEntity ae = (ApplicationEntity) iter.next();
            String aeti = ae.getAETitle();
            if (aeti == null || aeti.equals(aet))
            {
                a.setApplicationEntity(ae);
                return ae.negotiate(a, rq);
            }
        }
        throw new AAssociateRJException(
                AAssociateRJException.RESULT_REJECTED_PERMANENT,
                AAssociateRJException.SOURCE_SERVICE_USER,
                AAssociateRJException.REASON_CALLED_AET_NOT_RECOGNIZED);
    }

}
