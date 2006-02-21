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
package org.dcm4chex.archive.web.maverick;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.dict.Tags;
import org.dcm4chex.archive.common.PrivateTags;
import org.dcm4chex.archive.ejb.interfaces.ContentManager;
import org.dcm4chex.archive.ejb.interfaces.ContentManagerHome;
import org.dcm4chex.archive.util.EJBHomeFactory;

/**
 * @author franz.willer@gwi-ag.com
 * @version $Revision$ $Date$
 */
public class FolderUtil {

	
    private static Logger log = Logger.getLogger( FolderUtil.class.getName() );

    /**
	 * @param stickyPatients
	 * @param stickyStudies
	 * @param stickySeries
	 * @param stickyInstances
	 * @return
	 */
	public static Set getSelectedInstances(Set stickyPatients, Set stickyStudies, Set stickySeries, Set stickyInstances) {
		if ( stickyPatients.size() > 1 ) {
			throw new IllegalArgumentException("Instances from different patients are selected! Please check your selection.");
		}
		Set instances = new HashSet();
		if ( stickyPatients.size() == 1 ) {
			addPatientInstances(instances, Integer.valueOf(stickyPatients.iterator().next().toString()));
		}
		if ( stickyStudies.size() > 0 ) {
			for ( Iterator iter = stickyStudies.iterator() ; iter.hasNext() ; ) {
				addStudyInstances( instances, Integer.valueOf(iter.next().toString()) );
			}
		}
		if ( stickySeries.size() > 0 ) {
			for ( Iterator iter = stickySeries.iterator() ; iter.hasNext() ; ) {
				addSeriesInstances( instances, Integer.valueOf(iter.next().toString()) );
			}
		}
		if ( stickyInstances.size() > 0 ) {
			Set set = new HashSet();
			for ( Iterator iter = stickyInstances.iterator() ; iter.hasNext() ; ) {
				set.add( Integer.valueOf(iter.next().toString()));
			}
			try {
				Collection col = lookupContentManager().getSOPInstanceRefMacros(set);
				for ( Iterator iter = col.iterator() ; iter.hasNext() ; ) {
					addSOPInstanceRefs( instances, (Dataset) iter.next() );
				}
			} catch (Exception x) {
				log.error("Cant add selected instances ! :"+set,x);
				throw new IllegalArgumentException("Cant add selected instances!");
			}
		}

		return instances;
	}
	/**
	 * @param instances
	 * @param integer
	 */
	private static void addSeriesInstances(Set instances, Integer seriesPk) {
		List l;
		try {
			l = lookupContentManager().listInstancesOfSeries(seriesPk.intValue());
			for ( Iterator iter = l.iterator() ; iter.hasNext() ; ) {
				instances.add( ((Dataset) iter.next()).getString(Tags.SOPInstanceUID));
			}
		} catch (Exception x) {
			log.error("Cant add instances of series (pk="+seriesPk+")!",x);
			throw new IllegalArgumentException("Cant add instances of series!");
		}
	}
	
	/**
	 * @param integer
	 */
	private static void addStudyInstances(Set instances, Integer studyPk) {
		try {
			Dataset ds = lookupContentManager().getSOPInstanceRefMacro(studyPk.intValue(), false);
			addSOPInstanceRefs( instances, ds );
		} catch (Exception x) {
			log.error("Cant add instances of study (pk="+studyPk+")!",x);
			throw new IllegalArgumentException("Cant add instances of study!");
		}
		
	}
	/**
	 * @param integer
	 */
	private static void addPatientInstances(Set instances, Integer patPk) {
		try {
			List l = lookupContentManager().listStudiesOfPatient(patPk.intValue());
			for ( Iterator iter = l.iterator() ; iter.hasNext() ; ) {
				addStudyInstances( instances, ((Dataset) iter.next()).getInteger(PrivateTags.StudyPk));
			}
		} catch (Exception x) {
			log.error("Cant add instances of patient (pk="+patPk+")!",x);
			throw new IllegalArgumentException("Cant add instances of patient!");
		}
	}

	private static void addSOPInstanceRefs( Set instances, Dataset ds) {
		DcmElement refSerSq = ds.get(Tags.RefSeriesSeq);
		DcmElement refSopSq;
		Dataset seriesItem;
		for ( int i = 0 ; i < refSerSq.vm() ; i++) {
			seriesItem = refSerSq.getItem(i);
			refSopSq = seriesItem.get(Tags.RefSOPSeq);
			for ( int j = 0 ; j < refSopSq.vm() ; j++ ) {
				instances.add(refSopSq.getItem(j).getString(Tags.RefSOPInstanceUID));
			}
		}
	}

	private static ContentManager lookupContentManager() throws Exception {
        ContentManagerHome home = (ContentManagerHome) EJBHomeFactory
                .getFactory().lookup(ContentManagerHome.class,
                        ContentManagerHome.JNDI_NAME);
        return home.create();
    }
	
}
