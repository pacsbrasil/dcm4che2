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
import org.dcm4che2.net.service.BasicDicomServiceRegistry;
import org.dcm4che2.net.service.DicomService;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision$ $Date$
 * @since Nov 25, 2005
 *
 */
public class Device
{
    private final DeviceConfiguration conf;
    private final Connector[] connector;
    private final ApplicationEntity[] ae;
    private Executor executor;
    private SSLContext sslContext;
    private SecureRandom random;
    private BasicDicomServiceRegistry serviceRegistry = 
            new BasicDicomServiceRegistry();

    public final BasicDicomServiceRegistry getServiceRegistry()
    {
        return serviceRegistry;
    }

    public Device(DeviceConfiguration conf)
    {
        if (conf == null)
            throw new NullPointerException("conf");
        
        this.conf = conf;
        this.executor = new NewThreadExecutor(conf.getDeviceName());
        final NetworkConnection[] connections = conf.getNetworkConnection();
        this.connector = new Connector[connections.length];
        for (int i = 0; i < connections.length; i++)
            connector[i] = new Connector(this, connections[i]);
        final NetworkApplicationEntity[] nae = conf.getNetworkApplicationEntity();
        this.ae = new ApplicationEntity[nae.length];
        for (int i = 0; i < ae.length; i++)
            ae[i] = new ApplicationEntity(this, nae[i]);
    }
    
    public final DeviceConfiguration getConfiguration()
    {
        return conf;
    }
    
    public final Executor getExecutor()
    {
        return executor;
    }
    
    public void initTLS(KeyStore key, char[] password)
    throws GeneralSecurityException
    {
        KeyStore trust = KeyStore.getInstance(KeyStore.getDefaultType());
        addCertificate(trust, conf.getThisNodeCertificate());
        addCertificate(trust, conf.getAuthorizedNodeCertificate());
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
    
    
    public Connector getConnector(NetworkConnection nc)
    {
        for (int i = 0; i < connector.length; i++)
            if (connector[i].getConfiguration() == nc)
                return connector[i];
        return null;
    }
    
    public ApplicationEntity getApplicationEntity(NetworkApplicationEntity nae)
    {
        for (int i = 0; i < ae.length; i++)
            if (ae[i].getConfiguration() == nae)
                return ae[i];
        return null;
    }
    
    public void startListening() throws IOException
    {
        for (int i = 0; i < connector.length; i++)
            connector[i].bind();
    }

    public void stopListening()
    {
        for (int i = 0; i < connector.length; i++)
            connector[i].unbind();
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
        for (int i = 0; i < ae.length; i++)
        {
            String aeti = ae[i].getAETitle();
            if (aeti == null || aeti.equals(aet))
            {
                a.setApplicationEntity(ae[i]);
                return ae[i].negotiate(a, rq);
            }
        }
        throw new AAssociateRJException(
                AAssociateRJException.RESULT_REJECTED_PERMANENT,
                AAssociateRJException.SOURCE_SERVICE_USER,
                AAssociateRJException.REASON_CALLED_AET_NOT_RECOGNIZED);
    }

    public void register(DicomService service)
    {
        serviceRegistry.register(service);        
    }

}
