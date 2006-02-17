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
 * Franz Willer <franz.willer@gwi-ag.com>
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

package org.dcm4chex.archive.dcm.qrscp;

import java.io.IOException;
import java.sql.SQLException;

import org.dcm4che.auditlog.AuditLoggerFactory;
import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Status;
import org.dcm4che.dict.Tags;
import org.dcm4che.net.ActiveAssociation;
import org.dcm4che.net.Association;
import org.dcm4che.net.DcmServiceBase;
import org.dcm4che.net.DcmServiceException;
import org.dcm4che.net.Dimse;
import org.dcm4che.net.DimseListener;
import org.dcm4chex.archive.ejb.jdbc.QueryCmd;
import org.jboss.logging.Logger;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision$ $Date$
 * @since 31.08.2003
 */
public class FindScp extends DcmServiceBase {

    private final AuditLoggerFactory alf = AuditLoggerFactory.getInstance();

    protected final QueryRetrieveScpService service;
    
    private final boolean filterResult;

	protected final Logger log;

    public FindScp(QueryRetrieveScpService service, boolean filterResult) {
        this.service = service;
		this.log = service.getLog();
        this.filterResult = filterResult;
    }

    protected MultiDimseRsp doCFind(ActiveAssociation assoc, Dimse rq,
            Command rspCmd) throws IOException, DcmServiceException {
        try {
            Dataset rqData = rq.getDataset();
			log.debug("Identifier:\n");
			log.debug(rqData);
            logDicomQuery(assoc.getAssociation(), rq.getCommand(), rqData);
            return newMultiCFindRsp(rqData);
        } catch (Exception e) {
            log.error("Query DB failed:", e);
            throw new DcmServiceException(Status.ProcessingFailure, e);
        }
    }

	protected MultiDimseRsp newMultiCFindRsp(Dataset rqData) throws SQLException {
        QueryCmd queryCmd = QueryCmd.create(rqData, filterResult);
        queryCmd.execute();
		return new MultiCFindRsp(queryCmd);
	}

	void logDicomQuery(Association assoc, Command cmd, Dataset keys) {
        service.logDicomQuery(keys, alf.newRemoteNode(assoc.getSocket(), assoc
                .getCallingAET()), cmd.getAffectedSOPClassUID());
    }

    protected Dataset getDataset(QueryCmd queryCmd) throws SQLException, 
    		DcmServiceException {
		return queryCmd.getDataset();
	}

	protected class MultiCFindRsp implements MultiDimseRsp {

        private final QueryCmd queryCmd;

        private boolean canceled = false;
        
        public MultiCFindRsp(QueryCmd queryCmd) {
            this.queryCmd = queryCmd;
        }

        public DimseListener getCancelListener() {
            return new DimseListener() {

                public void dimseReceived(Association assoc, Dimse dimse) {
                    canceled = true;
                }
            };
        }

        public Dataset next(ActiveAssociation assoc, Dimse rq, Command rspCmd)
                throws DcmServiceException {
            if (canceled) {
                rspCmd.putUS(Tags.Status, Status.Cancel);
                return null;
            }
            try {
                if (!queryCmd.next()) {
                    rspCmd.putUS(Tags.Status, Status.Success);
                    return null;
                }
                rspCmd.putUS(Tags.Status, Status.Pending);
                Dataset data = getDataset(queryCmd);				
				log.debug("Identifier:\n");
				log.debug(data);
                return data;
            } catch (DcmServiceException e) {
            	throw e;
            } catch (SQLException e) {
                log.error("Retrieve DB record failed:", e);
                throw new DcmServiceException(Status.ProcessingFailure, e);
            } catch (Exception e) {
                log.error("Corrupted DB record:", e);
                throw new DcmServiceException(Status.ProcessingFailure, e);
            }
        }

        public void release() {
            queryCmd.close();
        }
    }
}
