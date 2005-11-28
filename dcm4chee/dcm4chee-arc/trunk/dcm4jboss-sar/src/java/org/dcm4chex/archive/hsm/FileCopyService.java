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
 * Portions created by the Initial Developer are Copyright (C) 2005
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

package org.dcm4chex.archive.hsm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.management.JMException;
import javax.management.Notification;
import javax.management.NotificationFilterSupport;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.dcm4chex.archive.common.Availability;
import org.dcm4chex.archive.common.FileStatus;
import org.dcm4chex.archive.config.ForwardingRules;
import org.dcm4chex.archive.config.RetryIntervalls;
import org.dcm4chex.archive.ejb.interfaces.Storage;
import org.dcm4chex.archive.ejb.interfaces.StorageHome;
import org.dcm4chex.archive.notif.FileInfo;
import org.dcm4chex.archive.notif.SeriesStored;
import org.dcm4chex.archive.util.BufferedOutputStream;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.dcm4chex.archive.util.FileUtils;
import org.dcm4chex.archive.util.HomeFactoryException;
import org.dcm4chex.archive.util.JMSDelegate;
import org.jboss.system.ServiceMBeanSupport;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision$ $Date$
 * @since Nov 9, 2005
 */
public class FileCopyService extends ServiceMBeanSupport implements
		MessageListener, NotificationListener {

	private static final NotificationFilterSupport seriesStoredFilter 
			= new NotificationFilterSupport();
	static {
		seriesStoredFilter.enableType(SeriesStored.class.getName());
	}

	private ObjectName storeScpServiceName;

	private String queueName;

	private String retrieveAET;

	private String userInfo;
	
	private int availability = 1;
	
	private int concurrency = 1;
	
	private int fileStatus = FileStatus.TO_ARCHIVE;
	
	private boolean verifyCopy;

	private ForwardingRules copyingRules = new ForwardingRules("");

	private RetryIntervalls retryIntervalls = new RetryIntervalls();

	private ObjectName fileSystemMgtName;

    public final ObjectName getFileSystemMgtName() {
        return fileSystemMgtName;
    }

    public final void setFileSystemMgtName(ObjectName fileSystemMgtName) {
        this.fileSystemMgtName = fileSystemMgtName;
    }

	public String getEjbProviderURL() {
		return EJBHomeFactory.getEjbProviderURL();
	}

	public void setEjbProviderURL(String ejbProviderURL) {
		EJBHomeFactory.setEjbProviderURL(ejbProviderURL);
	}

	public final ObjectName getStoreScpServiceName() {
		return storeScpServiceName;
	}

	public final void setStoreScpServiceName(ObjectName storeScpServiceName) {
		this.storeScpServiceName = storeScpServiceName;
	}

	public final String getRetrieveAET() {
		return retrieveAET;
	}

	public final void setRetrieveAET(String aet) {
		this.retrieveAET = aet;
	}

	public final String getUserInfo() {
		return userInfo;
	}

	public final void setUserInfo(String userInfo) {
		this.userInfo = userInfo;
	}

	public final String getAvailability() {
		return Availability.toString(availability);
	}

	public final void setAvailability(String availability) {
		this.availability = Availability.toInt(availability);
	}

	public final int getConcurrency() {
		return concurrency;
	}

	public final String getCopyingRules() {
		return copyingRules.toString();
	}

	public final void setCopyingRules(String copyingRules) {
		this.copyingRules = new ForwardingRules(copyingRules.replace('\\', '/'));
	}

	public final String getFileStatus() {
		return FileStatus.toString(fileStatus);
	}

	public final void setFileStatus(String fileStatus) {
		this.fileStatus = FileStatus.toInt(fileStatus);
	}

	public final boolean isVerifyCopy() {
		return verifyCopy;
	}

	public final void setVerifyCopy(boolean verifyCopy) {
		this.verifyCopy = verifyCopy;
	}

	public final String getRetryIntervalls() {
		return retryIntervalls.toString();
	}

	public final void setRetryIntervalls(String s) {
		this.retryIntervalls = new RetryIntervalls(s);
	}

	public final void setConcurrency(int concurrency) throws Exception {
		if (concurrency <= 0)
			throw new IllegalArgumentException("Concurrency: " + concurrency);
		if (this.concurrency != concurrency) {
			final boolean restart = getState() == STARTED;
			if (restart)
				stop();
			this.concurrency = concurrency;
			if (restart)
				start();
		}
	}

	public final String getQueueName() {
		return queueName;
	}

	public final void setQueueName(String queueName) {
		this.queueName = queueName;
	}

	protected void startService() throws Exception {
		JMSDelegate.startListening(queueName, this, concurrency);
		server.addNotificationListener(storeScpServiceName, this,
				seriesStoredFilter, null);
	}

	protected void stopService() throws Exception {
		server.removeNotificationListener(storeScpServiceName, this,
				seriesStoredFilter, null);
		JMSDelegate.stopListening(queueName);
	}

	public void handleNotification(Notification notif, Object handback) {
		SeriesStored seriesStored = (SeriesStored) notif.getUserData();
		Map param = new HashMap();
		param.put("calling", new String[] { seriesStored.getCallingAET() });
		param.put("called", new String[] { seriesStored.getCalledAET() });
		String[] dests = copyingRules.getForwardDestinationsFor(param);
		for (int i = 0; i < dests.length; i++) {
			final String dest = ForwardingRules.toAET(dests[i]);
			final long scheduledTime = ForwardingRules
					.toScheduledTime(dests[i]);
			scheduleCopy(seriesStored.getFileInfos(), dest, scheduledTime);
		}
	}

	public void scheduleCopy(List fileInfos, String destFsPath,
			long scheduledTime) {
		schedule(new FileCopyOrder(fileInfos, destFsPath), scheduledTime);
	}

	private void schedule(FileCopyOrder order, long scheduledTime) {
		try {
			log.info("Scheduling " + order);
			JMSDelegate.queue(queueName, order, Message.DEFAULT_PRIORITY,
					scheduledTime);
		} catch (JMSException e) {
			log.error("Failed to schedule " + order, e);
		}
	}

	public void onMessage(Message message) {
		ObjectMessage om = (ObjectMessage) message;
		try {
			FileCopyOrder order = (FileCopyOrder) om.getObject();
			log.info("Start processing " + order);
			try {
				process(order);
				log.info("Finished processing " + order);
			} catch (Exception e) {
				final int failureCount = order.getFailureCount() + 1;
				order.setFailureCount(failureCount);
				final long delay = retryIntervalls.getIntervall(failureCount);
				if (delay == -1L) {
					log.error("Give up to process " + order, e);
				} else {
					log.warn("Failed to process " + order
							+ ". Scheduling retry.", e);
					schedule(order, System.currentTimeMillis() + delay);
				}
			}
		} catch (Throwable e) {
			log.error("unexpected error during processing message: " + message,
					e);
		}
	}

	private void process(FileCopyOrder order) throws Exception {
		String destPath = order.getDestinationFileSystemPath();
		List fileInfos = order.getFileInfos();
		byte[] buffer = allocateBuffer();
		Storage storage = getStorageHome().create();
		for (Iterator iter = fileInfos.iterator(); iter.hasNext();) {
			FileInfo finfo = (FileInfo) iter.next();
			File src = FileUtils.toFile(finfo.getFileSystemPath() + '/'
					+ finfo.getFilePath());
			File dst = FileUtils.toFile(destPath + '/' + finfo.getFilePath());
			copy(src, dst, buffer );
			storage.storeFile(finfo.getSOPInstanceUID(),
					finfo.getTransferSyntaxUID(), retrieveAET, availability,
					userInfo, destPath, finfo.getFilePath(),
					(int) finfo.getFileSize(), finfo.getMd5sum(), fileStatus);
		}
	}

	byte[] allocateBuffer() {
        try {
			return (byte[]) server.invoke(fileSystemMgtName, "allocateBuffer", null, null);
		} catch (JMException e) {
            throw new RuntimeException(
                    "Failed to invoke allocateBuffer", e);
		}
 	}    
	
	private void copy(File src, File dst, byte[] buffer) throws IOException {
		FileInputStream fis = new FileInputStream(src);
		try {
			mkdirs(dst.getParentFile());
			BufferedOutputStream bos = new BufferedOutputStream(
					new FileOutputStream(dst), buffer);
			try {
				bos.write(fis, (int) src.length());
			} finally {
				bos.close();
			}
		} catch (IOException e) {
			dst.delete();
			throw e;
		} finally {
			fis.close();
		}
	}

	private void mkdirs(File dir) {
		if (dir.mkdirs()) {
			log.info("M-WRITE dir:" + dir);
		}
	}

	private StorageHome getStorageHome() throws HomeFactoryException {
		return (StorageHome) EJBHomeFactory.getFactory().lookup(
				StorageHome.class, StorageHome.JNDI_NAME);
	}

}
