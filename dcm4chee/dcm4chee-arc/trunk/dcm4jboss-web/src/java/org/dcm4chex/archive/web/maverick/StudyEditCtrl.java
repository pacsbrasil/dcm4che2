/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.web.maverick;

import org.dcm4chex.archive.ejb.interfaces.StudyDTO;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision$ $Date$
 * @since 5.10.2004
 *
 */
public class StudyEditCtrl extends Dcm4JbossController {
    private int patPk;

    private int studyPk;

    public final int getPatPk() {
        return patPk;
    }

    public final void setPatPk(int pk) {
        this.patPk = pk;
    }

    public final int getStudyPk() {
        return studyPk;
    }

    public final void setStudyPk(int pk) {
        this.studyPk = pk;
    }

    public StudyDTO getStudy() {
        return FolderForm.getFolderForm(getCtx().getRequest()).getStudyByPk(
                patPk, studyPk);
    }

}