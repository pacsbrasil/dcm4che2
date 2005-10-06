/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.mbean;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmDecodeParam;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4che.util.UIDGenerator;
import org.dcm4chex.archive.common.DatasetUtils;
import org.dcm4chex.archive.ejb.jdbc.FileInfo;
import org.dcm4chex.archive.ejb.jdbc.RetrieveCmd;
import org.dcm4chex.archive.ejb.jdbc.RetrieveStudyDatesCmd;
import org.dcm4chex.archive.ejb.jdbc.WadoQueryCmd;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.jboss.system.ServiceMBeanSupport;

/**
 * @author franz.willer@tiani.com
 * @version $Revision$ $Date$
 * @since 17.02.2005
 */
public class StudyInfoService extends ServiceMBeanSupport {

	private static Logger log = Logger.getLogger(StudyInfoService.class
			.getName());

	DcmObjectFactory dof = DcmObjectFactory.getInstance();

	public StudyInfoService() {
	}

	protected void startService() throws Exception {
	}

	protected void stopService() throws Exception {
	}

	public String getEjbProviderURL() {
		return EJBHomeFactory.getEjbProviderURL();
	}

	public void setEjbProviderURL(String ejbProviderURL) {
		EJBHomeFactory.setEjbProviderURL(ejbProviderURL);
	}

	private Dataset getQueryDS(String level, String uid) {
		Dataset dsQ = dof.newDataset();
		if ("STUDY".equals(level))
			dsQ.putUI(Tags.StudyInstanceUID, uid);
		else if ("SERIES".equals(level))
			dsQ.putUI(Tags.SeriesInstanceUID, uid);
		else if ("IMAGE".equals(level))
			dsQ.putUI(Tags.SOPInstanceUID, uid);
		else
			throw new IllegalArgumentException(
					"Argument level must be either STUDY,SERIES or IMAGE! level:"
							+ level);
		dsQ.putCS(Tags.QueryRetrieveLevel, level);
		return dsQ;
	}

	public boolean checkOutdated(Date date, String level, String uid) {
		Dataset dsQ = getQueryDS(level, uid);
		RetrieveStudyDatesCmd cmd = null;
		try {
			cmd = RetrieveStudyDatesCmd.create(dsQ);
			Date mrDate = cmd.getMostRecentUpdatedTime();
			return date.before(mrDate);
		} catch (SQLException x) {
			log.error("Error while RetrieveStudyDatesCmd!", x);
		} finally {
			if (cmd != null)
				cmd.close();
		}
		return true;
	}

	public Dataset retrieveStudyInfo(String level, Dataset dsQ)
			throws SQLException, IOException {

		WadoQueryCmd queryCmd = null;
		Dataset dsAll = dof.newDataset();

		// general attributes
		dsAll.putUI(Tags.SOPClassUID, UIDs.TianiStudyInfo);
		dsAll
				.putUI(Tags.SOPInstanceUID, UIDGenerator.getInstance()
						.createUID());

		try {
			queryCmd = WadoQueryCmd.create(dsQ, false);
			queryCmd.execute();
			HashMap data = new HashMap();
			while (queryCmd.next()) {
				//
				// Need to convert flat result set to hierachical hierarchical
				//				
				queryCmd.fillDatasetHierarchical(data);
			}

			// Convert to dataset

			// referenced patient sequence
			DcmElement psq = dsAll.putSQ(Tags.RefPatientSeq);
			for (Iterator pit = data.values().iterator(); pit.hasNext();) {
				WadoQueryCmd.KeyData pkd = (WadoQueryCmd.KeyData) pit.next();
				psq.addItem(pkd.ds);

				if (pkd.seq.size() == 0)
					continue;

				// referenced study sequence
				DcmElement ssq = pkd.ds.putSQ(Tags.RefStudySeq);
				for (Iterator sit = pkd.seq.values().iterator(); sit.hasNext();) {
					WadoQueryCmd.KeyData skd = (WadoQueryCmd.KeyData) sit
							.next();
					ssq.addItem(skd.ds);

					if (skd.seq.size() == 0)
						continue;

					// referenced series sequence
					DcmElement rsq = pkd.ds.putSQ(Tags.RefSeriesSeq);
					for (Iterator rit = skd.seq.values().iterator(); rit
							.hasNext();) {
						WadoQueryCmd.KeyData rkd = (WadoQueryCmd.KeyData) rit
								.next();
						rsq.addItem(rkd.ds);

						if (rkd.seq.size() == 0)
							continue;

						// referenced image instance sequence
						DcmElement isq = rkd.ds.putSQ(Tags.RefInstanceSeq);
						for (Iterator iit = rkd.seq.values().iterator(); iit
								.hasNext();) {
							WadoQueryCmd.KeyData ikd = (WadoQueryCmd.KeyData) iit
									.next();
							isq.addItem(ikd.ds);

							if (ikd.seq.size() == 0)
								continue;

							// referenced image location sequence
							DcmElement lsq = ikd.ds.putSQ(Tags.RefImageSeq);
							for (Iterator lit = ikd.seq.values().iterator(); lit
									.hasNext();) {
								WadoQueryCmd.KeyData lkd = (WadoQueryCmd.KeyData) lit
										.next();
								lsq.addItem(lkd.ds);
							}
						}
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			queryCmd.close();
		}

		return dsAll;
	}
}
