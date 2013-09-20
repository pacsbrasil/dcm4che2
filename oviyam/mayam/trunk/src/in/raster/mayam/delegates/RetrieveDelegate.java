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
 *
 * The Initial Developer of the Original Code is
 * Raster Images
 * Portions created by the Initial Developer are Copyright (C) 2009-2010
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Babu Hussain A
 * Devishree V
 * Meer Asgar Hussain B
 * Prakash J
 * Suresh V
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
package in.raster.mayam.delegates;

import in.raster.mayam.context.ApplicationContext;
import in.raster.mayam.models.Series;
import in.raster.mayam.models.ServerModel;
import java.util.ArrayList;
import org.dcm4che.util.DcmURL;
import org.dcm4che2.data.UID;

/**
 *
 * @author devishree
 * @version 2.0
 */
public class RetrieveDelegate {

    public void wado(String patientId, String studyUID, String serUid, String iuid, ServerModel serverDetails) {
        WadoInstanceRetrieveDelegate instanceRetrieveDelegate = new WadoInstanceRetrieveDelegate(studyUID, serUid, iuid, serverDetails);
    }

    //Retrieves the whole study
    public void cGet(String patientId, String studyUID, String modalitiesInStudy, ArrayList<Series> seriesList, int totalInstances, String[] patientInfo, DcmURL url, ArrayList<String> sopUidList) {
        ArrayList<String> cgetParam = new ArrayList<String>();
        cgetParam.add(url.getCalledAET() + "@" + url.getHost() + ":" + url.getPort());
        cgetParam.add("-L " + ApplicationContext.listenerDetails[0]);
        cgetParam.add("-cget");
        cgetParam.add("-I");
        cgetParam.add("-qStudyInstanceUID=" + studyUID);
        for (int i = 0; i < sopUidList.size(); i++) {
            cgetParam.add("-cstore");
            cgetParam.add(sopUidList.get(i) + ":" + UID.ExplicitVRLittleEndian);
            if (sopUidList.contains(UID.VideoEndoscopicImageStorage) || sopUidList.contains(UID.VideoMicroscopicImageStorage) || sopUidList.contains(UID.VideoPhotographicImageStorage)) {
                cgetParam.add(",");
                cgetParam.add(sopUidList.get(i) + ":" + UID.MPEG2);
                cgetParam.add(",");
                cgetParam.add(sopUidList.get(i) + ":" + UID.MPEG2MainProfileHighLevel);
                cgetParam.add(",");
                cgetParam.add(sopUidList.get(i) + ":" + UID.MPEG4AVCH264HighProfileLevel41);
                cgetParam.add(",");
                cgetParam.add(sopUidList.get(i) + ":" + UID.MPEG4AVCH264BDCompatibleHighProfileLevel41);
            }
        }
        cgetParam.add("-cstoredest");
        cgetParam.add(ApplicationContext.listenerDetails[2]);
        String[] cgetParam1 = cgetParam.toArray(new String[cgetParam.size()]);
        CGetDelegate cGetDelegate = new CGetDelegate(cgetParam1, patientId, studyUID, modalitiesInStudy, seriesList, totalInstances, patientInfo);
    }

    public void cMove(String patientId, String studyUID, String modalitiesInStudy, ArrayList<Series> seriesList, int totalInstances, String[] patientInfo, DcmURL url) {
        String cmoveParam[] = new String[]{url.getProtocol() + "://" + url.getCalledAET() + "@" + url.getHost() + ":" + url.getPort(), "--dest", ApplicationContext.listenerDetails[0], "--pid", patientId, "--suid", studyUID};
        MoveDelegate moveDelegate = new MoveDelegate(cmoveParam, patientId, studyUID, modalitiesInStudy, seriesList, totalInstances, patientInfo);
    }

    public void wado(String patientId, String studyUID, String modalitiesInStudy, ArrayList<Series> seriesList, int totalInstances, String[] patientInfo, String serverName, DcmURL url) {
        WadoRetrieveDelegate retrieveDelegate = new WadoRetrieveDelegate();
        retrieveDelegate.retrieveStudy(serverName, patientId, studyUID, totalInstances, patientInfo);
    }
}