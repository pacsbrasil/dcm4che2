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
package org.dcm4chee.xero.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4chee.xero.metadata.MetaData;
import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.metadata.filter.FilterUtil;
import org.dcm4chee.xero.wado.WadoParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** This class handles the c-find queries for non-hierarchical c-find systems.
 * It should only be included at the series or instance level
 */
public class HierarchicalSearchFilter implements Filter<ResultFromDicom> {
    private static final Logger log = LoggerFactory.getLogger(HierarchicalSearchFilter.class);
    
    boolean isObjectLevel = false;
    
    Filter<ResultFromDicom> studySearch;
    @MetaData(out = "${ref:studySource.studySearch}")
    public void setStudySearch(Filter<ResultFromDicom> studySearch) {
        this.studySearch = studySearch;
    }

    Filter<ResultFromDicom> seriesSearch;
    @MetaData(out="${class:org.dcm4chee.xero.search.study.SeriesOnlySearch}")
    public void setSeriesSearch(Filter<ResultFromDicom> seriesSearch) {
        this.seriesSearch = seriesSearch;
    }

    Filter<ResultFromDicom> imageSearch;
    @MetaData(out="${class:org.dcm4chee.xero.search.study.ImageOnlySearch}")
    public void setImageSearch(Filter<ResultFromDicom> imageSearch) {
        this.imageSearch = imageSearch;
    }

    Filter<SearchCriteria> studyCriteria;   
    @MetaData(out="${class:org.dcm4chee.xero.search.study.StudySearchConditionParser}")
    public void setStudyCriteria(Filter<SearchCriteria> studyCriteria) {
        this.studyCriteria = studyCriteria;
    }

    Filter<SearchCriteria> seriesCriteria;
    @MetaData(out="${class:org.dcm4chee.xero.search.study.ImageSearchConditionParser}")
    public void setSeriesCriteria(Filter<SearchCriteria> seriesCriteria) {
        this.seriesCriteria = seriesCriteria;
    }

    /** Call the filter up a level to get study/patient (and series for image query) level data and then
     * call a custom series or image level query that excludes the search request for
     * the up-level data.
     */
    public ResultFromDicom filter(FilterItem<ResultFromDicom> filterItem, Map<String, Object> params) {
        Map<String,Object> ae = AEProperties.getAE(params);
        if( !FilterUtil.getBoolean(ae,"hierarchicalOnly") ) {
            log.debug("Not performing hierarchical search on ae {}", ae.get(AEProperties.AE_PROPERTY_NAME));
            return filterItem.callNextFilter(params);
        }
        log.info("Doing a hierarchical only search at study.");
        
        ResultFromDicom resultFromDicom = DicomCFindFilter.getResultFromDicom(params); 
        
        CollectDicomObject cdo = new CollectDicomObject();
        params.put(DicomCFindFilter.EXTEND_RESULTS_KEY,cdo);

        Map<String,Object> serParams = createSeriesParams(studyCriteria, params);
        if( serParams==null ) return resultFromDicom;
        studySearch.filter(null, params);
        
        DicomObject[] dobjs = cdo.getDicomObjects();
        if( dobjs.length==0 ) return resultFromDicom;
        
        for(DicomObject ds : dobjs) {
            addSeriesResults(ds, serParams, resultFromDicom, true);
        }
        
        return resultFromDicom;
    }

    /** Creates a map of parameters excluding the parameters up a level */
    protected Map<String, Object> createSeriesParams(Filter<SearchCriteria> criteriaFilter, Map<String, Object> params) {
        Map<String,Object> ret = new HashMap<String,Object>(params);
        SearchCriteria sc = criteriaFilter.filter(null,params);
        Map<String, TableColumn> atts = sc.getAttributeByName();
        if( atts==null || atts.isEmpty() ) return null;
        for(String key : atts.keySet()) {
            if( key.equals(WadoParams.OBJECT_UID) ) continue;
            ret.remove(key);
        }
        return ret;
    }

    /** Adds the series results to the top level object results */
    protected void addSeriesResults(DicomObject ds, Map<String, Object> params, ResultFromDicom resultFromDicom, boolean isSeries) {
        boolean extendLevel = (isSeries && isObjectLevel);
        CollectDicomObject cdo = new CollectDicomObject();
        params.put(DicomCFindFilter.EXTEND_RESULTS_KEY, cdo);

        params.put(WadoParams.STUDY_UID, ds.getString(Tag.StudyInstanceUID));
        if( isSeries ) {
            log.info("Doing a series level hierarchical query on study UID {}", params.get(WadoParams.STUDY_UID ));
            seriesSearch.filter(null,params);
        } else {
            params.put(WadoParams.SERIES_UID, ds.getString(Tag.SeriesInstanceUID));
            log.info("Doing an image level hierarchical search on series {}",params.get(WadoParams.SERIES_UID));
            imageSearch.filter(null,params);
        }
        DicomObject[] dobjs = cdo.getDicomObjects();
        if( dobjs==null || dobjs.length==0 ) return;
        Map<String,Object> imgParams = null;
        if( extendLevel ) {
            imgParams = createSeriesParams(seriesCriteria,params);
            imgParams.put(WadoParams.STUDY_UID,params.get(WadoParams.STUDY_UID));
        }
        for(DicomObject dsSer : dobjs) {
            ds.copyTo(dsSer);
            if( extendLevel ) {
                addSeriesResults(dsSer, imgParams, resultFromDicom, false);
            } else {
                resultFromDicom.addResult(dsSer);
            }
        }
    }

    @MetaData(out="SERIES")
    public void setLevel(String level) {
        this.isObjectLevel = !level.equals("SERIES");
    }

    /** Collect study or series results from the query into an array */
    static class CollectDicomObject implements ResultFromDicom {
        List<DicomObject> results = new ArrayList<DicomObject>();
        static DicomObject[] EMPTY = new DicomObject[0];

        /** Add the item to the array of results */
        public void addResult(DicomObject data) {
            results.add(data);
        }

        /** Get the final results */
        public DicomObject[] getDicomObjects() {
            return results.toArray(EMPTY);
        }
        
    }

}
