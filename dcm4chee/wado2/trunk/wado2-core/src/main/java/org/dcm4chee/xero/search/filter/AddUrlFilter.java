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
 * Agfa HealthCare Inc., 
 * Portions created by the Initial Developer are Copyright (C) 2009
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Bill Wallace <bill.wallace@agfa.com>
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
package org.dcm4chee.xero.search.filter;

import java.util.Map;

import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.metadata.filter.FilterUtil;
import org.dcm4chee.xero.search.macro.UrlMacro;
import org.dcm4chee.xero.search.study.DicomObjectInterface;
import org.dcm4chee.xero.search.study.DicomObjectType;
import org.dcm4chee.xero.search.study.GspsBean;
import org.dcm4chee.xero.search.study.ImageBean;
import org.dcm4chee.xero.search.study.ImageBeanFrame;
import org.dcm4chee.xero.search.study.KeyObjectBean;
import org.dcm4chee.xero.search.study.MacroItems;
import org.dcm4chee.xero.search.study.PatientBean;
import org.dcm4chee.xero.search.study.PatientType;
import org.dcm4chee.xero.search.study.ResultsBean;
import org.dcm4chee.xero.search.study.SeriesBean;
import org.dcm4chee.xero.search.study.SeriesType;
import org.dcm4chee.xero.search.study.StudyBean;
import org.dcm4chee.xero.search.study.StudyType;

/** Adds URLs to the patient, study, series and image levels */
public class AddUrlFilter  implements Filter<ResultsBean>{

    /** Adds URL's to the various levels */
    public ResultsBean filter(FilterItem<ResultsBean> filterItem, Map<String, Object> params) {
        ResultsBean ret = filterItem.callNextFilter(params);
        if( ret==null || ! FilterUtil.getBoolean(params,"url") ) return ret;
        for(PatientType patient : ret.getPatient()) {
            for(StudyType study : patient.getStudy()) {
                String studyUID = study.getStudyUID();
                addUrl(study);
                for(SeriesType series : study.getSeries()) {
                    addUrl(studyUID, series);
                    String seriesUID = series.getSeriesUID();
                    for(DicomObjectType dot : series.getDicomObject()) {
                        addUrl(studyUID, seriesUID, dot);
                    }
                }
            }
        }
        return ret;
    }
    
    /** Return the substring after the colon */
    private static final String after(String s) {
        if( s.indexOf(':')<0 ) return s;
        return s.substring(s.lastIndexOf(':')+1);
    }

    private void addUrl(String studyUID, String seriesUID, DicomObjectType dot) {
        DicomObjectInterface  doi = (DicomObjectInterface) dot;
        MacroItems m = doi.getMacroItems();
        if( m.findMacro(UrlMacro.class)!=null ) return;
        String url = "?requestType=WADO&studyUID="+after(studyUID)+"&seriesUID="+after(seriesUID) + "&objectUID="+after(dot.getObjectUID());
        if( doi instanceof ImageBean ) {
            ImageBean ib = (ImageBean) doi; 
            if( ib.getFrame()!=null ) {
                url = url + "&frameNumber="+ib.getFrame();
            }
            if( ib.getGspsUID()!=null ) {
                url = url + "&presentationUID="+ib.getGspsUID();
            }
        }
        else if( doi instanceof GspsBean ) {
            GspsBean gsps = (GspsBean) doi;
            url = "?requestType=IMAGE&url=true&regroup=true&Position=0&Count=4&studyUID="+after(studyUID)
                + "&gsps="+ gsps.getContentLabel();
        } else if( doi instanceof KeyObjectBean ) {
            KeyObjectBean kob = (KeyObjectBean) doi;
            url = "?requestType=IMAGE&url=true&regroup=true&Position=0&Count=4&studyUID="+after(studyUID)
            + "&koUID="+ kob.getObjectUID();
        }
        UrlMacro urlm = new UrlMacro(url);
        doi.addMacro(urlm);
    }

    private void addUrl(String studyUID, SeriesType series) {
        SeriesBean sb = (SeriesBean) series;
        if( sb.getMacroItems().findMacro(UrlMacro.class)!=null ) return;
        String url = "?requestType=IMAGE&url=true&regroup=true&Position=0&Count=64&studyUID="+studyUID+"&seriesUID="+series.getSeriesUID();
        UrlMacro urlm = new UrlMacro(url);
        sb.addMacro(urlm);
    }

    private void addUrl(StudyType study) {
        StudyBean sb = (StudyBean) study;
        if( sb.getMacroItems().findMacro(UrlMacro.class)!=null ) return;
        String url = "?requestType=IMAGE&url=true&regroup=true&Position=0&Count=4&studyUID="+study.getStudyUID();
        UrlMacro urlm = new UrlMacro(url);
        sb.addMacro(urlm);
    }

}
