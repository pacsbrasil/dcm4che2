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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.net.service.CEchoSCP;
import org.dcm4che2.net.service.CFindSCP;
import org.dcm4che2.net.service.CGetSCP;
import org.dcm4che2.net.service.CMoveSCP;
import org.dcm4che2.net.service.CStoreSCP;
import org.dcm4che2.net.service.DicomService;
import org.dcm4che2.net.service.NActionSCP;
import org.dcm4che2.net.service.NCreateSCP;
import org.dcm4che2.net.service.NDeleteSCP;
import org.dcm4che2.net.service.NEventReportSCU;
import org.dcm4che2.net.service.NGetSCP;
import org.dcm4che2.net.service.NSetSCP;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Reversion$ $Date$
 * @since Oct 3, 2005
 *
 */
class DicomServiceRegistry 
implements CStoreSCP, CGetSCP, CFindSCP, CMoveSCP, CEchoSCP,
    NEventReportSCU, NGetSCP, NCreateSCP, NSetSCP, NDeleteSCP, NActionSCP
{
    private final HashSet sopCUIDs = new HashSet();
    private final HashMap cstoreSCP = new HashMap();
    private final HashMap cgetSCP = new HashMap();
    private final HashMap cmoveSCP = new HashMap();
    private final HashMap cfindSCP = new HashMap();
    private final HashMap cechoSCP = new HashMap(1);
    private final HashMap neventReportSCU = new HashMap();
    private final HashMap ngetSCP = new HashMap();
    private final HashMap nsetSCP = new HashMap();
    private final HashMap nactionSCP = new HashMap();
    private final HashMap ncreateSCP = new HashMap();
    private final HashMap ndeleteSCP = new HashMap();

    private void registerInto(HashMap registry, DicomService service)
    {
        final String[] sopClasses = service.getSopClasses();
        for (int i = 0; i < sopClasses.length; i++)
        {
            registry.put(sopClasses[i], service);
            sopCUIDs.add(sopClasses[i]);
        }
        final String serviceClass = service.getServiceClass();
        if (serviceClass != null)
        {
            registry.put(serviceClass, service);
            sopCUIDs.add(serviceClass);
        }
    }

    private void unregisterFrom(HashMap registry, DicomService service)
    {
        for (Iterator iter = registry.entrySet().iterator(); iter.hasNext();)
        {
            Map.Entry element = (Map.Entry) iter.next();
            if (element.getValue() == service)
                iter.remove();
        }
    }
    
    public void register(DicomService service) {
        if (service instanceof CStoreSCP)
            registerInto(cstoreSCP, service);
        if (service instanceof CGetSCP)
            registerInto(cgetSCP, service);
        if (service instanceof CMoveSCP)
            registerInto(cmoveSCP, service);
        if (service instanceof CFindSCP)
            registerInto(cfindSCP, service);
        if (service instanceof CEchoSCP)
            registerInto(cechoSCP, service);
        if (service instanceof NEventReportSCU)
            registerInto(neventReportSCU, service);
        if (service instanceof NGetSCP)
            registerInto(ngetSCP, service);
        if (service instanceof NSetSCP)
            registerInto(nsetSCP, service);
        if (service instanceof NActionSCP)
            registerInto(nactionSCP, service);
        if (service instanceof NCreateSCP)
            registerInto(ncreateSCP, service);
        if (service instanceof NDeleteSCP)
            registerInto(ndeleteSCP, service);
    }

    public void unregister(DicomService service) {
        if (service instanceof CStoreSCP)
            unregisterFrom(cstoreSCP, service);
        if (service instanceof CGetSCP)
            unregisterFrom(cgetSCP, service);
        if (service instanceof CMoveSCP)
            unregisterFrom(cmoveSCP, service);
        if (service instanceof CFindSCP)
            unregisterFrom(cfindSCP, service);
        if (service instanceof CEchoSCP)
            unregisterFrom(cechoSCP, service);
        if (service instanceof NEventReportSCU)
            unregisterFrom(neventReportSCU, service);
        if (service instanceof NGetSCP)
            unregisterFrom(ngetSCP, service);
        if (service instanceof NSetSCP)
            unregisterFrom(nsetSCP, service);
        if (service instanceof NActionSCP)
            unregisterFrom(nactionSCP, service);
        if (service instanceof NCreateSCP)
            unregisterFrom(ncreateSCP, service);
        if (service instanceof NDeleteSCP)
            unregisterFrom(ndeleteSCP, service);
    }
    
    private CStoreSCP getCStoreSCP(Association as, DicomObject cmd)
    {
        CStoreSCP scp = (CStoreSCP) cstoreSCP.get(cmd.getString(Tag.AffectedSOPClassUID));
        return scp != null ? scp : this;
    }

    private CGetSCP getCGetSCP(Association as, DicomObject cmd)
    {
        CGetSCP scp = (CGetSCP) cgetSCP.get(cmd.getString(Tag.AffectedSOPClassUID));
        return scp != null ? scp : this;
    }

    private CFindSCP getCFindSCP(Association as, DicomObject cmd)
    {
        CFindSCP scp = (CFindSCP) cfindSCP.get(cmd.getString(Tag.AffectedSOPClassUID));
        return scp != null ? scp : this;
    }

    private CMoveSCP getCMoveSCP(Association as, DicomObject cmd)
    {
        CMoveSCP scp = (CMoveSCP) cmoveSCP.get(cmd.getString(Tag.AffectedSOPClassUID));
        return scp != null ? scp : this;
    }

    private CEchoSCP getCEchoSCP(Association as, DicomObject cmd)
    {
        CEchoSCP scp = (CEchoSCP) cechoSCP.get(cmd.getString(Tag.AffectedSOPClassUID));
        return scp != null ? scp : this;
    }

    private NEventReportSCU getNEventReportSCU(Association as, DicomObject cmd)
    {
        NEventReportSCU scu = (NEventReportSCU) neventReportSCU.get(cmd.getString(Tag.AffectedSOPClassUID));
        return scu != null ? scu : this;
    }

    private NGetSCP getNGetSCP(Association as, DicomObject cmd)
    {
        NGetSCP scp = (NGetSCP) ngetSCP.get(cmd.getString(Tag.RequestedSOPClassUID));
        return scp != null ? scp : this;
    }

    private NSetSCP getNSetSCP(Association as, DicomObject cmd)
    {
        NSetSCP scp = (NSetSCP) nsetSCP.get(cmd.getString(Tag.RequestedSOPClassUID));
        return scp != null ? scp : this;
    }

    private NActionSCP getNActionSCP(Association as, DicomObject cmd)
    {
        NActionSCP scp = (NActionSCP) nactionSCP.get(cmd.getString(Tag.RequestedSOPClassUID));
        return scp != null ? scp : this;
    }

    private NCreateSCP getNCreateSCP(Association as, DicomObject cmd)
    {
        NCreateSCP scp = (NCreateSCP) ncreateSCP.get(cmd.getString(Tag.AffectedSOPClassUID));
        return scp != null ? scp : this;
    }

    private NDeleteSCP getNDeleteSCP(Association as, DicomObject cmd)
    {
        NDeleteSCP scp = (NDeleteSCP) ndeleteSCP.get(cmd.getString(Tag.RequestedSOPClassUID));
        return scp != null ? scp : this;
    }

    public void process(Association as, int pcid, DicomObject cmd,
            PDVInputStream dataStream, String tsuid)
    {
        final int cmdfield = cmd.getInt(Tag.CommandField);
        if (cmdfield == CommandUtils.C_STORE_RQ)
        {
            getCStoreSCP(as, cmd).cstore(as, pcid, cmd, dataStream, tsuid);
        }
        else
        {
            final DicomObject dataset;
            try
            {
                dataset = dataStream != null ? dataStream.readDataset() : null;
            }
            catch (IOException e)
            {
                as.abort();
                return;
            }
            switch (cmdfield)
            {
                case CommandUtils.C_GET_RQ:
                    getCGetSCP(as, cmd).cget(as, pcid, cmd, dataset);
                    break;
                case CommandUtils.C_FIND_RQ:
                    getCFindSCP(as, cmd).cfind(as, pcid, cmd, dataset);
                    break;
                case CommandUtils.C_MOVE_RQ:
                    getCMoveSCP(as, cmd).cmove(as, pcid, cmd, dataset);
                    break;
                case CommandUtils.C_ECHO_RQ:
                    getCEchoSCP(as, cmd).cecho(as, pcid, cmd);
                    break;
                case CommandUtils.N_EVENT_REPORT_RQ:
                    getNEventReportSCU(as, cmd).neventReport(as, pcid, cmd, dataset);
                    break;
                case CommandUtils.N_GET_RQ:
                    getNGetSCP(as, cmd).nget(as, pcid, cmd, dataset);
                    break;
                case CommandUtils.N_SET_RQ:
                    getNSetSCP(as, cmd).nset(as, pcid, cmd, dataset);
                    break;
                case CommandUtils.N_ACTION_RQ:
                    getNActionSCP(as, cmd).naction(as, pcid, cmd, dataset);
                    break;
                case CommandUtils.N_CREATE_RQ:
                    getNCreateSCP(as, cmd).ncreate(as, pcid, cmd,
                            dataStream != null ? dataset : null);
                    break;
                case CommandUtils.N_DELETE_RQ:
                    getNDeleteSCP(as, cmd).ndelete(as, pcid, cmd, dataset);
                    break;
                default:
                    // TODO Auto-generated method stub        

            }
        }
    }

    public void cstore(Association as, int pcid, DicomObject cmd, PDVInputStream dataStream, String tsuid)
    {
        // TODO Auto-generated method stub
        
    }

    public void cget(Association as, int pcid, DicomObject cmd, DicomObject data)
    {
        // TODO Auto-generated method stub
        
    }

    public void cfind(Association as, int pcid, DicomObject cmd, DicomObject data)
    {
        // TODO Auto-generated method stub
        
    }

    public void cmove(Association as, int pcid, DicomObject cmd, DicomObject data)
    {
        // TODO Auto-generated method stub
        
    }

    public void cecho(Association as, int pcid, DicomObject cmd)
    {
        // TODO Auto-generated method stub
        
    }

    public void neventReport(Association as, int pcid, DicomObject cmd, DicomObject data)
    {
        // TODO Auto-generated method stub
        
    }

    public void nget(Association as, int pcid, DicomObject cmd, DicomObject data)
    {
        // TODO Auto-generated method stub
        
    }

    public void ncreate(Association as, int pcid, DicomObject cmd, DicomObject data)
    {
        // TODO Auto-generated method stub
        
    }

    public void nset(Association as, int pcid, DicomObject cmd, DicomObject data)
    {
        // TODO Auto-generated method stub
        
    }

    public void ndelete(Association as, int pcid, DicomObject cmd, DicomObject data)
    {
        // TODO Auto-generated method stub
        
    }

    public void naction(Association as, int pcid, DicomObject cmd, DicomObject data)
    {
        // TODO Auto-generated method stub
        
    }
}
