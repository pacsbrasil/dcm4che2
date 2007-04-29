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
 * Fuad Ibrahimov, Diagnoseklinik Muenchen.de GmbH,
 * Portions created by the Initial Developer are Copyright (C) 2007
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Fuad Ibrahimov <fuad@ibrahimov.de>
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
package org.dcm4chex.archive.hsm.spi;

import org.dcm4chex.archive.dcm.qrscp.MoveScp;
import org.dcm4chex.archive.dcm.qrscp.QueryRetrieveScpService;
import org.dcm4chex.archive.hsm.spi.utils.Assert;
import org.dcm4chex.archive.ejb.jdbc.FileInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.management.ObjectName;
import java.io.Serializable;
import java.text.MessageFormat;

/**
 * This class extends <code>QueryRetrieveScpService</code> to create an instance
 * of <code>HsmAwareMoveScp</code> class instead of <code>MoveScp</code> to filter out archived
 * files and retrieve them from the HSM archive.
 *
 * HSM retrieve is done asynchronously, posting an <code>HsmRetrieveOrder</code> to the
 * <code>HsmRetrieve</code> JMS queue.
 *
 * @see HsmAwareMoveScp
 * @see QueryRetrieveScpService
 * @see #createMoveScp
 * @see  #retriveAndSend
 * @author Fuad Ibrahimov
 * @since Nov 29, 2006
 */
public class HsmAwareQueryRetrieveScpService extends QueryRetrieveScpService {

    private int jmsPriority;
    private ObjectName jmsServiceName;
    private TimeProvider timeProvider; // Used for tests to stub out the call to System.currentTimeMillis()

    private static final String ERROR_FAILED_TO_QUEUE_HSM_RETRIEVE_ORDER = "Failed to queue HSM retrieve order [{0}]"; // NON-NLS
    private String hsmRetrieveQueueName = HSM_RETRIEVE;
    static final String QUEUE = "queue";  // NON-NLS
    static final String HSM_RETRIEVE = "HsmRetrieve"; // NON-NLS

    private static final Log logger = LogFactory.getLog(HsmAwareQueryRetrieveScpService.class);

    /**
     * Creates a new instance of <code>HsmAwareMoveScp</code>.
     * @return a new instance of <code>HsmAwareMoveScp</code>
     */
    protected MoveScp createMoveScp() {
        return new HsmAwareMoveScp(this);
    }

    /**
     * Invokes an asynchronous HSM retrieval of archived files. Invoked <code>HsmRetrieveOrder</code>
     * contains the destination AE title for a deferred <code>C-MOVE</code> task invocation.
     * <p>
     * <b>Note:</b> In case if files were packed into a TAR archive, the
     * granularity of the deferred <code>C-MOVE</code> tasks will be a single series.
     *
     * @param archivedFiles an array of files to be retrieved
     * @param destination destination AE title for the <code>C-MOVE</code> task
     * @see org.dcm4chex.archive.hsm.spi.HsmRetrieveOrder
     * @see org.dcm4chex.archive.hsm.spi.HsmRetrieveService
     */
    public void retriveAndSend(FileInfo[][] archivedFiles, String destination) {
        HsmRetrieveOrder order = new HsmRetrieveOrder(archivedFiles, destination);
        try {
            server.invoke(jmsServiceName,
                    QUEUE,
                    new Object[]{this.hsmRetrieveQueueName, order, this.jmsPriority, now()},
                    new String[]{String.class.getName(), Serializable.class.getName(), int.class.getName(), long.class.getName()});
        } catch (Exception e) {
            logger.error(MessageFormat.format(ERROR_FAILED_TO_QUEUE_HSM_RETRIEVE_ORDER, order), e);
        }
    }

    // Hook for tests to substitute the call to System.currentTimeMillis()
    private long now() {
        return timeProvider == null ? System.currentTimeMillis() : timeProvider.now();
    }

    protected void startService() throws Exception {
        if (jmsPriority < 0) throw new IllegalArgumentException("Illegal JMS priority: " + jmsPriority);
        Assert.hasText(hsmRetrieveQueueName, QUEUE);

        super.startService();
    }

    public int getJmsPriority() {
        return jmsPriority;
    }

    public void setJmsPriority(int jmsPriority) {
        this.jmsPriority = jmsPriority;
    }

    public String getHsmRetrieveQueueName() {
        return hsmRetrieveQueueName;
    }

    public void setHsmRetrieveQueueName(String hsmRetrieveQueueName) {
        this.hsmRetrieveQueueName = hsmRetrieveQueueName;
    }

    public ObjectName getJmsServiceName() {
        return jmsServiceName;
    }

    public void setJmsServiceName(ObjectName jmsServiceName) {
        this.jmsServiceName = jmsServiceName;
    }


    void setTimeProvider(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    static interface TimeProvider {
        public long now();
    }
}
