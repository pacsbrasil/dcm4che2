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

package org.dcm4che2.config;

import java.io.IOException;
import java.io.OutputStream;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision$ $Date$
 * @since Oct 31, 2005
 * 
 */
public class XMLEncoder
{
    private static String encoding = "UTF-8";

    private final OutputStream out;
    private int indentation = 0;
    private IdentityHashMap nc2id = new IdentityHashMap();

    public XMLEncoder(OutputStream out)
    {
        this.out = out;
    }
    
    public void writeDevice(Device dev)
    throws IOException
    {
        writeln("<Device>");
        indentation++;
        if (dev.getDeviceName() != null)
            writeln("<DeviceName>" + quoteCharacters(dev.getDeviceName())
                    + "</DeviceName>");
        if (dev.getDescription() != null)
            writeln("<Description><![CDATA[" + dev.getDescription()
                    + "]]></Description>");
        List ncs = dev.getNetworkConnections();
        for (Iterator iter = ncs.iterator(); iter.hasNext();)
        {
            NetworkConnection nc = (NetworkConnection) iter.next();
            writeNetworkConnection(nc, true);
        }
        List aes = dev.getNetworkAEs();
        for (Iterator iter = aes.iterator(); iter.hasNext();)
        {
            NetworkAE ae = (NetworkAE) iter.next();
            writeNetworkAE(ae);
        }
        indentation--;
        writeln("</Device");
    }
    
    public void writeNetworkAE(NetworkAE ae)
    throws IOException
    {
        writeln("<NetworkAE>");
        indentation++;
        if (ae.getAEtitle() != null)
            writeln("<AEtitle>" + quoteCharacters(ae.getAEtitle())
                    + "</AEtitle>");
        if (ae.getDescription() != null)
            writeln("<Description><![CDATA[" + ae.getDescription()
                    + "]]></Description>");
        List ncs = ae.getNetworkConnections();
        for (Iterator iter = ncs.iterator(); iter.hasNext();)
        {
            NetworkConnection nc = (NetworkConnection) iter.next();
            writeNetworkConnection(nc, false);
        }
        List tcs = ae.getTransferCapabilities();
        for (Iterator iter = tcs.iterator(); iter.hasNext();)
        {
            TransferCapability tc = (TransferCapability) iter.next();
            writeTransferCapability(tc);
        }
        indentation--;
        writeln("</NetworkAE");
    }
    
    private void writeNetworkConnection(NetworkConnection nc, boolean id)
    throws IOException
    {
        String idstr = (String) nc2id.get(nc);
        if (idstr != null)
        {
            writeln("<NetworkConnection idref=" + '\"' + idstr + "\"/>"); 
            return;
        }
        if (id)
        {
            idstr = "nc" + (nc2id.size() + 1);
            nc2id.put(nc, idstr);
            writeln("<NetworkConnection id=" + '\"' + idstr + "\">"); 
        }
        else
        {
            writeln("<NetworkConnection>");
        }
        indentation++;
        if (nc.getCommonName() != null)
            writeln("<CommonName>" + quoteCharacters(nc.getCommonName())
                    + "</CommonName>");
        if (nc.getInstalled() != null)
            writeln("<Installed>" + nc.getInstalled() + "</Installed>");
        writeln("<Hostname>" + quoteCharacters(nc.getHostname())
                + "</Hostname>");
        writeln("<Port>" + nc.getPort() + "</Port>");
        List cipherSuites = nc.getTlsCipherSuites();
        for (Iterator iter = cipherSuites.iterator(); iter.hasNext();)
            writeln("<TLSCipherSuite>" + quoteCharacters((String) iter.next()) 
                    + "</TLSCipherSuite>");
        indentation--;
        writeln("</NetworkConnection>");
    }

    private void writeTransferCapability(TransferCapability tc)
    throws IOException
    {
        writeln("<TransferCapability>");
        indentation++;
        if (tc.getCommonName() != null)
            writeln("<CommonName>" + quoteCharacters(tc.getCommonName())
                    + "</CommonName>");
        writeln("<SOPClass>" + quoteCharacters(tc.getSopClass())
                + "</SOPClass>");
        writeln("<Role>" + tc.getRole() + "</Role>");
        List tsuids = tc.getTransferSyntaxes();
        for (Iterator iter = tsuids.iterator(); iter.hasNext();)
            writeln("<TransferSyntax>" + quoteCharacters((String) iter.next()) 
                    + "</TransferSyntax>");
        indentation--;
        writeln("</TransferCapability>");
    }

    private static String quoteCharacters(String s)
    {
        StringBuffer result = null;
        for (int i = 0, max = s.length(), delta = 0; i < max; i++)
        {
            char c = s.charAt(i);
            String replacement = null;

            if (c == '&')
                replacement = "&amp;";
            else if (c == '<')
                replacement = "&lt;";
            else if (c == '\r')
                replacement = "&#13;";
            else if (c == '>')
                replacement = "&gt;";
            else if (c == '"')
                replacement = "&quot;";
            else if (c == '\'')
                replacement = "&apos;";

            if (replacement != null)
            {
                if (result == null)
                    result = new StringBuffer(s);
                result.replace(i + delta, i + delta + 1, replacement);
                delta += (replacement.length() - 1);
            }
        }
        if (result == null)
            return s;
        return result.toString();
    }

    private void writeln(String exp) throws IOException
    {
        for (int i = 0; i < indentation; i++)
            out.write(' ');
        
        out.write(exp.getBytes(encoding));
        out.write(" \n".getBytes());
    }
}
