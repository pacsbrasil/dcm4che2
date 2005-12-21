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


package org.dcm4chex.archive.tce;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.management.Notification;
import javax.management.NotificationFilterSupport;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.DcmValueException;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.VRs;
import org.dcm4chex.archive.common.DatasetUtils;
import org.dcm4chex.archive.ejb.jdbc.FileInfo;
import org.dcm4chex.archive.ejb.jdbc.QueryCmd;
import org.dcm4chex.archive.ejb.jdbc.RetrieveCmd;
import org.dcm4chex.archive.notif.SeriesStored;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.dcm4chex.archive.util.FileUtils;
import org.dcm4chex.archive.util.JMSDelegate;
import org.jboss.system.ServiceMBeanSupport;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision$ $Date$
 * @since Dec 19, 2005
 */
public class ExportManagerService extends ServiceMBeanSupport implements
		NotificationListener, MessageListener {

	private static final String[] NONE = {};
	
	private static final NotificationFilterSupport seriesStoredFilter 
			= new NotificationFilterSupport();

	static {
		seriesStoredFilter.enableType(SeriesStored.class.getName());
	}
	
	private ObjectName storeScpServiceName;

	private String queueName;

	private int concurrency = 1;
	
	private String[] exportSelectorTitles = NONE;

	private File dispConfigDir;
	
	private String defaultDisposition;
	
	public final String getExportSelectorTitles() {
		if (exportSelectorTitles.length == 0)
			return "NONE";
		
		StringBuffer sb = new StringBuffer(exportSelectorTitles[0]);		
		for (int i = 1; i < exportSelectorTitles.length; ++i)
			sb.append((i & 1) != 0 ? '^' : ',').append(exportSelectorTitles[i]);
		
		return sb.toString();
	}

	public final void setExportSelectorTitles(String s) {
		if (s.equalsIgnoreCase("NONE"))
			this.exportSelectorTitles = NONE;
		else
		{
			StringTokenizer stk = new StringTokenizer(s, "^,; \r\n\t");
			this.exportSelectorTitles = new String[stk.countTokens() & ~1];
			for (int i = 0; i < exportSelectorTitles.length; i++)
				exportSelectorTitles[i] = stk.nextToken();
		}
	}

	public String getEjbProviderURL() {
		return EJBHomeFactory.getEjbProviderURL();
	}

	public void setEjbProviderURL(String ejbProviderURL) {
		EJBHomeFactory.setEjbProviderURL(ejbProviderURL);
	}

	public final String getDispositionConfigDir() {
		return dispConfigDir.getPath();
	}

	public final void setDispositionConfigDir(String path) {
		this.dispConfigDir = new File(path.replace('/', File.separatorChar));
	}


	public final String getDefaultDisposition() {
		return defaultDisposition;
	}

	public final void setDefaultDisposition(String defaultDisposition) {
		this.defaultDisposition = defaultDisposition;
	}

	public final ObjectName getStoreScpServiceName() {
		return storeScpServiceName;
	}

	public final void setStoreScpServiceName(ObjectName storeScpServiceName) {
		this.storeScpServiceName = storeScpServiceName;
	}

	public final String getQueueName() {
		return queueName;
	}

	public final void setQueueName(String queueName) {
		this.queueName = queueName;
	}

	public final int getConcurrency() {
		return concurrency;
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
		String suid = seriesStored.getStudyInstanceUID();
		for (int i = 1; i < exportSelectorTitles.length; i++, i++)
			onSeriesStored(suid, exportSelectorTitles[i-1], exportSelectorTitles[i]);
	}	
	
	private void onSeriesStored(String suid, String code, String designator) {
		List list = queryExportSelectors(suid, code, designator);
		for (Iterator iter = list.iterator(); iter.hasNext();) {
			Dataset sel = (Dataset) iter.next();
			if (isScheduled(sel))
				continue;
			if (isAllReceived(sel))
			{				
				if (schedule(new ExportTFOrder(sel), 0L))
					setScheduled(sel);
			}
		}		
	}
	
	private String toDisposition(Dataset sel) {
		DcmElement sq = sel.get(Tags.ContentSeq);
		for (int i = 0, n = sq.vm(); i < n; i++) {
			Dataset item = sq.getItem(i);
			if (!"TEXT".equals(item.getString(Tags.ValueType)))
				continue;
			Dataset cn = item.getItem(Tags.ConceptNameCodeSeq);
			if (cn != null && "113012".equals(cn.getString(Tags.CodeValue))
					&& "DCM".equals(cn.getString(Tags.CodingSchemeDesignator)))
				return item.getString(Tags.TextValue, defaultDisposition);
		}
		return defaultDisposition;
	}

	private List queryExportSelectors(String suid, String code, String designator) {
		ArrayList list = new ArrayList();
		Dataset keys = DcmObjectFactory.getInstance().newDataset();
		keys.putUI(Tags.StudyInstanceUID, suid);
		Dataset item = keys.putSQ(Tags.ConceptNameCodeSeq).addNewItem();
		item.putSH(Tags.CodeValue, code);
		item.putSH(Tags.CodingSchemeDesignator, designator);
		QueryCmd query = null;
		try {
			query = QueryCmd.createInstanceQuery(keys, false);
			query.execute();
			while (query.next())
				list.add(query.getDataset());			
		} catch (SQLException e) {
			log.error("Query DB for Export Selectors " + code + '^'
					+ designator + " of study " + suid + " failed!", e);
		} finally {
			if (query != null)
				query.close();
		}
		return list;
	}

	private boolean isAllReceived(Dataset sel) {
		Dataset keys = DcmObjectFactory.getInstance().newDataset();
		ArrayList list = new ArrayList();
		copyIUIDs(sel.get(Tags.CurrentRequestedProcedureEvidenceSeq), list);
		final String[] iuids = (String[]) list.toArray(new String[list.size()]);
		keys.putUI(Tags.SOPInstanceUID, iuids);
		QueryCmd query = null;
		try {
			query = QueryCmd.createInstanceQuery(keys, true);
			query.execute();
			for (int i = 0; i < iuids.length; i++) {
				if (!query.next())
					return false;
			}
			return true;
		} catch (SQLException e) {
			log.error("Query DB for Referenced Instances failed!", e);
		} finally {
			if (query != null)
				query.close();
		}
		return false;
	}
	
	private boolean isScheduled(Dataset selector) {
		// TODO Auto-generated method stub
		return false;
	}

	private void setScheduled(Dataset selector) {
		// TODO Auto-generated method stub
		
	}
	

	public void onMessage(Message message) {
		ObjectMessage om = (ObjectMessage) message;
		try {
			ExportTFOrder order = (ExportTFOrder) om.getObject();
			log.info("Start processing " + order);
			try {
				process(order);
				log.info("Finished processing " + order);
			} catch (Exception e) {
				log.warn("Failed to process " + order, e);
			}
		} catch (Throwable e) {
			log.error("unexpected error during processing message: " + message,
					e);
		}
	}

	private boolean schedule(ExportTFOrder order, long scheduledTime) {
		try {
			log.info("Scheduling " + order);
			JMSDelegate.queue(queueName, order, Message.DEFAULT_PRIORITY,
					scheduledTime);
			return true;
		} catch (JMSException e) {
			log.error("Failed to schedule " + order, e);
			return false;
		}
	}
	
	private void process(ExportTFOrder order) throws Exception {
		Dataset sel = order.getSelector();
		Properties config = loadConfig(sel);
		HashMap studies = new HashMap();
		HashMap series = new HashMap();
		HashMap instances = new HashMap();
		HashMap fileInfos = new HashMap();
		Dataset patAttrs = queryAttrs(sel, studies, series, instances, fileInfos);
		int numpasses = Integer.parseInt(config.getProperty("numpasses", "1"));
		for (int i = 1; i < numpasses; i++)	
			coerceAttributes(config, Integer.toString(i), patAttrs, studies, series, instances);
		replaceUIDs(sel, studies, series, instances);
		updateDB(patAttrs, studies, series, instances, fileInfos);
		scheduleMoveOrders(sel, config.getProperty("destination"));
	}

	private Properties loadConfig(Dataset sel) throws IOException {
		String disposition = toDisposition(sel);
		File dispConfigFile = FileUtils.resolve(
				new File(dispConfigDir, disposition + ".disp"));
		if (!dispConfigFile.exists()) {
			log.warn("No configuration for disposition " + disposition + " - use default.");
			dispConfigFile = FileUtils.resolve(
					new File(dispConfigDir, defaultDisposition + ".disp"));
		}
			
		FileInputStream fin = new FileInputStream(dispConfigFile);
		BufferedInputStream bis = new BufferedInputStream(fin);
		try {
			Properties config = new Properties();
			config.load(bis);
			return config;
		} finally {
			bis.close();
		}
	}
	
	private Dataset queryAttrs(Dataset sel, Map studies, Map series, Map instances, Map fileInfos)
	throws Exception {
		ArrayList list = new ArrayList();
		list.add(sel.getString(Tags.SOPInstanceUID));
		copyIUIDs(sel.get(Tags.IdenticalDocumentsSeq), list);
		copyIUIDs(sel.get(Tags.CurrentRequestedProcedureEvidenceSeq), list);
		Dataset keys = DcmObjectFactory.getInstance().newDataset();
		keys.putUI(Tags.SOPInstanceUID,
				(String[]) list.toArray(new String[list.size()]));
		RetrieveCmd cmd = RetrieveCmd.createInstanceRetrieve(keys);
		String patID = sel.getString(Tags.PatientID);
		FileInfo[][] a = cmd.getFileInfos();
		for (int i = 0; i < a.length; i++) {
			FileInfo fi = a[i][0];
			if (!equals(patID, fi.patID))
				throw new Exception("Export Selector references studies for different patients");
			Dataset studyAttr = (Dataset) studies.get(fi.studyIUID);
			if (studyAttr  == null) {
				studyAttr = DatasetUtils.fromByteArray(fi.studyAttrs);
				studies.put(fi.studyIUID, studyAttr);
			}
			Dataset seriesAttr = (Dataset) series.get(fi.seriesIUID);
			if (seriesAttr == null) {
				seriesAttr = DatasetUtils.fromByteArray(fi.seriesAttrs);
				series.put(fi.seriesIUID, seriesAttr);
			}
			fileInfos.put(fi.sopIUID, DatasetUtils.fromByteArray(fi.instAttrs));
			fileInfos.put(fi.sopIUID, fi);
		}
		return DatasetUtils.fromByteArray(a[0][0].patAttrs);
	}

	private void copyIUIDs(DcmElement sq1, List list) {
		for (int i1 = 0, n1 = sq1.vm(); i1 < n1; ++i1) {
			Dataset item1 = sq1.getItem(i1);
			DcmElement sq2 = item1.get(Tags.RefSeriesSeq);
			for (int i2 = 0, n2 = sq2.vm(); i2 < n2; ++i2) {
				Dataset item2 = sq1.getItem(i2);
				DcmElement sq3 = item2.get(Tags.RefSOPSeq);
				for (int i3 = 0, n3 = sq3.vm(); i3 < n3; ++i3) {
					Dataset item3 = sq1.getItem(i3);
					String iuid = item3.getString(Tags.RefSOPInstanceUID);
					list.add(iuid);
				}
			}
		}
	}

	private static boolean equals(Object o1, Object o2) {
		return o1 == null ? o2 == null : o1.equals(o2);
	}
	
	private void coerceAttributes(Properties config, String pass,
			Dataset patAttrs, Map studies, Map series, Map instances) {
		String patientPrefix = pass + ".patient.";
		String studyPrefix = pass + ".study.";
		String seriesPrefix = pass + ".series.";
		String instPrefix = pass + ".instance.";
		for (Iterator iter = config.entrySet().iterator(); iter.hasNext();) {
			Map.Entry e = (Map.Entry) iter.next();
			String key = (String) e.getKey();
			String value = (String) e.getValue();
			if (key.startsWith(patientPrefix))
				coerceAttribute(patAttrs, 
						key.substring(patientPrefix.length()), value);
			else if (key.startsWith(studyPrefix))
				coerceAttribute(studies, 
						key.substring(studyPrefix.length()), value);
			else if (key.startsWith(seriesPrefix))
				coerceAttribute(series, 
						key.substring(seriesPrefix.length()), value);
			else if (key.startsWith(instPrefix))
				coerceAttribute(instances, 
						key.substring(instPrefix.length()), value);
		}
	}
	
	private void coerceAttribute(Map map, String key, String value) {
		for (Iterator iter = map.values().iterator(); iter.hasNext();) {
			Dataset ds = (Dataset) iter.next();
			coerceAttribute(ds, key, value);
		}
	}

	private void coerceAttribute(Dataset attrs, String key, String value) {
		int tag = Tags.valueOf(key);
		if (value.length() == 0)
			deleteValue(attrs, tag);
		else if (value.equals("firstDayOfMonth()"))
			setFirstDayOfMonth(attrs, tag);
		else
			changeValue(attrs, tag, value);
	}

	private void deleteValue(Dataset attrs, int tag) {
		attrs.putXX(tag);
	}

	private void setFirstDayOfMonth(Dataset attrs, int tag) {
		DcmElement el = attrs.get(tag);
		if (el == null)
			return;
		try {
			Date date = el.getDate();
			date.setDate(1);
			if (el.vr() == VRs.DA)
				attrs.putDA(tag, date);
			else if (el.vr() == VRs.DT)
				attrs.putDT(tag, date);
			else {
				log.warn("Unexpected VR, delete value - " + el);
				attrs.putXX(tag);
			}
		} catch (DcmValueException e) {
			log.warn("Delete illegal Date value: " + el, e);
			attrs.putXX(tag);
		}
	}

	private void changeValue(Dataset attrs, int tag, String value) {
		StringBuffer sb = new StringBuffer();
		StringTokenizer stk = new StringTokenizer(value, "#${}", true);
		while (stk.hasMoreTokens()) {
			String tk = stk.nextToken();
			int ch = tk.charAt(0);
			if (ch != '#' && ch != '$' || !stk.hasMoreTokens())
			{
				sb.append(tk);
				continue;
			}
			tk = stk.nextToken();
			if (!tk.equals("{")  || !stk.hasMoreTokens())
			{
				sb.append(ch).append(tk);
				continue;
			}
			tk = stk.nextToken();
			int srctag = Tags.valueOf(tk);
			String s = attrs.getString(srctag);
			if (s != null)
			{
				if (ch == '#')
					s = Integer.toString(s.hashCode());
				sb.append(s);
			}
			if (stk.hasMoreTokens())
				stk.nextToken(); // skip "}"
		}
		attrs.putXX(tag, sb.toString());
	}

	private void replaceUIDs(Dataset sel, Map studies, Map series, Map instances) {
		// TODO Auto-generated method stub
		
	}

	private void updateDB(Dataset patAttrs, Map studies, Map series, Map instances, Map fileInfos) {
		// TODO Auto-generated method stub		
	}

	private void scheduleMoveOrders(Dataset sel, String destination) {
		// TODO Auto-generated method stub		
	}
}
