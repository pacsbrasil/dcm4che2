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

package org.dcm4chex.archive.common;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision$ $Date$
 * @since May 4, 2006
 *
 */
public class IANBuilder {
    
    private final Dataset ian;
    
    public IANBuilder() {
        this.ian = DcmObjectFactory.getInstance().newDataset();
        ian.putSQ(Tags.RefPPSSeq);
        ian.putSQ(Tags.RefSeriesSeq);
    }
    
    public IANBuilder(Dataset ian) {
        if (ian == null)
            throw new NullPointerException();
        this.ian = ian;
    }
    
    public final Dataset getIAN() {
        return ian;
    }
    
    public int getNumberOfInstances() {
        int num = 0;
        DcmElement sq = ian.get(Tags.RefSeriesSeq);
        for (int i = 0, n = sq.vm(); i < n; i++) {
            num += sq.getItem(i).get(Tags.RefSOPSeq).vm();
        }
        return num;
    }
    
    public void addRefSOP(Dataset dcmobj, String availability, String retrAET) {
        if (dcmobj == null)
            throw new NullPointerException("dcmobj");
        if (availability == null)
            throw new NullPointerException("availability");
        if (retrAET == null)
            throw new NullPointerException("retrAET");
        String suid = dcmobj.getString(Tags.StudyInstanceUID);
        if (suid == null)
            throw new IllegalArgumentException("missing Study Instance UID");
        String suid0 = ian.getString(Tags.StudyInstanceUID);
        if (suid0 != null && !suid.equals(suid))
            throw new IllegalStateException("Study Instance UID mismatch");
        String seruid = dcmobj.getString(Tags.SeriesInstanceUID);
        if (seruid == null)
            throw new IllegalArgumentException("missing Series Instance UID");
        String cuid = dcmobj.getString(Tags.SOPClassUID);
        if (cuid == null)
            throw new IllegalArgumentException("missing SOP Class UID");
        String iuid = dcmobj.getString(Tags.SOPInstanceUID);
        if (iuid == null)
            throw new IllegalArgumentException("missing SOP Instance UID");
        Dataset pps = dcmobj.getItem(Tags.RefPPSSeq);
        if (pps != null) {
            String ppscuid = pps.getString(Tags.RefSOPClassUID);
            String ppsiuid = pps.getString(Tags.RefSOPInstanceUID);
            Dataset pps0 = ian.getItem(Tags.RefPPSSeq);
            if (ppscuid != null && ppsiuid != null) {
                if (pps0 != null) {
                    if (!ppscuid.equals(pps0.getString(Tags.RefSOPClassUID)))
                        throw new IllegalStateException("Ref PPS Class UID mismatch");
                    if (!ppsiuid.equals(pps0.getString(Tags.RefSOPInstanceUID)))
                        throw new IllegalStateException("Ref PPS Instance UID mismatch");
                } else {
                    pps0 = ian.putSQ(Tags.RefPPSSeq).addNewItem();
                    pps0.putUI(Tags.RefSOPClassUID, ppscuid);
                    pps0.putUI(Tags.RefSOPInstanceUID, ppsiuid);
                    pps0.putSQ(Tags.PerformedWorkitemCodeSeq);
                }
            }
        }
        if (suid0 == null) {
            ian.putUI(Tags.StudyInstanceUID, suid);
        }
        Dataset refsop = getRefSOPSeq(seruid).addNewItem();
        refsop.putUI(Tags.RefSOPClassUID, cuid);
        refsop.putUI(Tags.RefSOPInstanceUID, iuid);
        refsop.putCS(Tags.InstanceAvailability, availability);
        refsop.putAE(Tags.RetrieveAET, retrAET);
     }

    private DcmElement getRefSOPSeq(String seruid) {
        DcmElement sq = ian.get(Tags.RefSeriesSeq);
        for (int i = 0, n = sq.vm(); i < n; i++) {
            Dataset series = sq.getItem(i);
            String seruid0 = series.getString(Tags.SeriesInstanceUID);
            if (seruid.equals(seruid0)) {
                return series.get(Tags.RefSOPSeq);
            }
        }
        Dataset series = sq.addNewItem();
        series.putUI(Tags.SeriesInstanceUID, seruid);
        return series.putSQ(Tags.RefSOPSeq);
    }
    
}
