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
 * Java(TM), available at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * TIANI Medgraph AG.
 * Portions created by the Initial Developer are Copyright (C) 2003-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunter.zeilinger@tiani.com>
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

package org.dcm4chex.cdw.mbean;

import javax.management.ObjectName;

import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.DcmParserFactory;
import org.dcm4che.dict.UIDs;
import org.dcm4che.net.AcceptorPolicy;
import org.dcm4che.net.AssociationFactory;
import org.dcm4che.net.DcmService;
import org.dcm4che.net.DcmServiceRegistry;
import org.dcm4che.server.DcmHandler;
import org.dcm4chex.cdw.common.*;
import org.jboss.system.ServiceMBeanSupport;

/**
 * @author gunter.zeilinter@tiani.com
 * @version $Revision$ $Date$
 * @since 22.06.2004
 *
 */
public abstract class AbstractScpService extends ServiceMBeanSupport {
        
    protected static final DcmObjectFactory dof = DcmObjectFactory.getInstance();

    protected static final DcmParserFactory pf = DcmParserFactory.getInstance();
    
    private static final String GET_DCM_HANDLER = "dcmHandler";

    protected static final String[] ONLY_DEFAULT_TS = { UIDs.ImplicitVRLittleEndian,};

    protected static final String[] NATIVE_LE_TS = {
            UIDs.ExplicitVRLittleEndian, UIDs.ImplicitVRLittleEndian,};

    protected static final AssociationFactory asf = AssociationFactory
            .getInstance();

    protected boolean acceptExplicitVRLE = true;

    protected ObjectName dcmServerName;

    protected SpoolDirDelegate spoolDir = new SpoolDirDelegate(this);

    protected DcmHandler dcmHandler;

    public final ObjectName getDcmServerName() {
        return dcmServerName;
    }

    public final void setDcmServerName(ObjectName dcmServerName) {
        this.dcmServerName = dcmServerName;
    }

    public final ObjectName getSpoolDirName() {
        return spoolDir.getSpoolDirName();
    }

    public final void setSpoolDirName(ObjectName spoolDirName) {
        spoolDir.setSpoolDirName(spoolDirName);
    }

    final SpoolDirDelegate getSpoolDir() {
        return spoolDir;
    }

    public final boolean isAcceptExplicitVRLE() {
        return acceptExplicitVRLE;
    }

    public final void setAcceptExplicitVRLE(boolean acceptExplicitVRLE) {
        this.acceptExplicitVRLE = acceptExplicitVRLE;
        updatePresContextsIfRunning();
    }

    protected String[] getTransferSyntaxes() {
        return acceptExplicitVRLE ? NATIVE_LE_TS : ONLY_DEFAULT_TS;
    }

    protected void startService() throws Exception {
        dcmHandler = (DcmHandler) server.invoke(dcmServerName, GET_DCM_HANDLER,
                null, null);
        bindDcmServices();
        updatePresContexts();
    }

    protected void stopService() throws Exception {
        removePresContexts();
        unbindDcmServices();
        dcmHandler = null;
    }

    protected abstract void bindDcmServices();

    protected abstract void unbindDcmServices();

    protected abstract void updatePresContexts();

    protected abstract void removePresContexts();

    protected void bindDcmServices(String[] cuids, DcmService service) {
        DcmServiceRegistry reg = dcmHandler.getDcmServiceRegistry();
        for (int i = 0; i < cuids.length; i++)
            reg.bind(cuids[i], service);
    }

    protected void unbindDcmServices(String[] cuids) {
        DcmServiceRegistry reg = dcmHandler.getDcmServiceRegistry();
        for (int i = 0; i < cuids.length; i++)
            reg.unbind(cuids[i]);
    }

    protected void putPresContexts(String[] asuids, String[] tsuids) {
        AcceptorPolicy policy = dcmHandler.getAcceptorPolicy();
        for (int i = 0; i < asuids.length; i++)
            policy.putPresContext(asuids[i], tsuids);
    }

    protected void updatePresContextsIfRunning() {
        if (getState() == STARTED)
            updatePresContexts();
    }
}
