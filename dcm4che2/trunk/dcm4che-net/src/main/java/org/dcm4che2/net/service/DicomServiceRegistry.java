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

package org.dcm4che2.net.service;

import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.net.Association;
import org.dcm4che2.net.CommandFactory;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Reversion$ $Date$
 * @since Oct 3, 2005
 *
 */
public class DicomServiceRegistry
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

    private Object lookupService(HashMap registry, Association as, String cuid)
    {
        Object scp = registry.get(cuid);
        if (scp == null)
        {
            //TODO
        }
        return scp;
    }

    public CStoreSCP getCStoreSCP(Association as, DicomObject cmd)
    {
        Object scp = lookupService(cstoreSCP, as, 
                cmd.getString(Tag.AffectedSOPClassUID));
        if (scp == null)
        {
            //TODO
        }
        return (CStoreSCP) scp;
    }

    public CGetSCP getCGetSCP(Association as, DicomObject cmd)
    {
        Object scp = lookupService(cgetSCP, as,
                cmd.getString(Tag.AffectedSOPClassUID));
        if (scp == null)
        {
            //TODO
        }
        return (CGetSCP) scp;
    }

    public CFindSCP getCFindSCP(Association as, DicomObject cmd)
    {
        Object scp = lookupService(cfindSCP, as,
                cmd.getString(Tag.AffectedSOPClassUID));
        if (scp == null)
        {
            //TODO
        }
        return (CFindSCP) scp;
    }

    public CMoveSCP getCMoveSCP(Association as, DicomObject cmd)
    {
        Object scp = lookupService(cmoveSCP, as,
                cmd.getString(Tag.AffectedSOPClassUID));
        if (scp == null)
        {
            //TODO
        }
        return (CMoveSCP) scp;
    }

    public CEchoSCP getCEchoSCP(Association as, DicomObject cmd)
    {
        Object scp = lookupService(cechoSCP, as,
                cmd.getString(Tag.AffectedSOPClassUID));
        if (scp == null)
        {
            //TODO
        }
        return (CEchoSCP) scp;
    }

    public NEventReportSCU getNEventReportSCP(Association as, DicomObject cmd)
    {
        Object scu = lookupService(neventReportSCU, as,
                cmd.getString(Tag.AffectedSOPClassUID));
        if (scu == null)
        {
            //TODO
        }
        return (NEventReportSCU) scu;
    }

    public NGetSCP getNGetSCP(Association as, DicomObject cmd)
    {
        Object scp = lookupService(ngetSCP, as,
                cmd.getString(Tag.RequestedSOPClassUID));
        if (scp == null)
        {
            //TODO
        }
        return (NGetSCP) scp;
    }

    public NSetSCP getNSetSCP(Association as, DicomObject cmd)
    {
        Object scp = lookupService(nsetSCP, as, 
                cmd.getString(Tag.RequestedSOPClassUID));
        if (scp == null)
        {
            //TODO
        }
        return (NSetSCP) scp;
    }

    public NActionSCP getNActionSCP(Association as, DicomObject cmd)
    {
        Object scp = lookupService(nactionSCP, as, 
                cmd.getString(Tag.RequestedSOPClassUID));
        if (scp == null)
        {
            //TODO
        }
        return (NActionSCP) scp;
    }

    public NCreateSCP getNCreateSCP(Association as, DicomObject cmd)
    {
        Object scp = lookupService(ncreateSCP, as, 
                cmd.getString(Tag.AffectedSOPClassUID));
        if (scp == null)
        {
            //TODO
        }
        return (NCreateSCP) scp;
    }

    public NDeleteSCP getNDeleteSCP(Association as, DicomObject cmd)
    {
        Object scp = lookupService(ndeleteSCP, as, 
                cmd.getString(Tag.RequestedSOPClassUID));
        if (scp == null)
        {
            //TODO
        }
        return (NDeleteSCP) scp;
    }

    public void process(Association as, int pcid, DicomObject cmd, InputStream dataStream)
    {
        switch (cmd.getInt(Tag.CommandField))
        {
        case CommandFactory.C_STORE_RQ:
            getCStoreSCP(as, cmd).cstore(as, pcid, cmd, dataStream);
            break;
        case CommandFactory.C_GET_RQ:
            getCGetSCP(as, cmd).cget(as, pcid, cmd, dataStream);
            break;
        case CommandFactory.C_FIND_RQ:
            getCFindSCP(as, cmd).cfind(as, pcid, cmd, dataStream);
            break;
        case CommandFactory.C_MOVE_RQ:
            getCMoveSCP(as, cmd).cmove(as, pcid, cmd, dataStream);
            break;
        case CommandFactory.C_ECHO_RQ:
            getCEchoSCP(as, cmd).cecho(as, pcid, cmd, dataStream);
            break;
        case CommandFactory.N_EVENT_REPORT_RQ:
            getNEventReportSCP(as, cmd).neventReport(as, pcid, cmd, dataStream);
            break;
        case CommandFactory.N_GET_RQ:
            getNGetSCP(as, cmd).nget(as, pcid, cmd, dataStream);
            break;
        case CommandFactory.N_SET_RQ:
            getNSetSCP(as, cmd).nset(as, pcid, cmd, dataStream);
            break;
        case CommandFactory.N_ACTION_RQ:
            getNActionSCP(as, cmd).naction(as, pcid, cmd, dataStream);
            break;
        case CommandFactory.N_CREATE_RQ:
            getNCreateSCP(as, cmd).ncreate(as, pcid, cmd, dataStream);
            break;
        case CommandFactory.N_DELETE_RQ:
            getNDeleteSCP(as, cmd).ndelete(as, pcid, cmd, dataStream);
            break;
        }
    }
}
