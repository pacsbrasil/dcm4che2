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
 * Accurate Software Design, LLC.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunterze@gmail.com>
 * Damien Evans <damien.daddy@gmail.com>
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

package org.dcm4che.archive.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * org.dcm4che.archive.entity.StudyPermission
 * 
 * @author <a href="mailto:damien.daddy@gmail.com">Damien Evans</a>
 */
@Entity
@Table(name = "study_permission")
public class StudyPermission extends EntityBase {

    private static final long serialVersionUID = 8164450587441995636L;

    @Column(name = "study_iuid", nullable = false)
    private String studyIuid;

    @Column(name = "action", nullable = false)
    private String action;

    @Column(name = "roles", nullable = false)
    private String role;

    /**
     * 
     */
    public StudyPermission() {
    }

    public StudyPermission(String suid, String action, String role) {
        setStudyIuid(suid);
        setAction(action);
        setRole(role);
    }

    /**
     * @return the action
     */
    public String getAction() {
        return action;
    }

    /**
     * @param action
     *            the action to set
     */
    public void setAction(String action) {
        this.action = action;
    }

    /**
     * @return the role
     */
    public String getRole() {
        return role;
    }

    /**
     * @param role
     *            the role to set
     */
    public void setRole(String role) {
        this.role = role;
    }

    /**
     * @return the studyIuid
     */
    public String getStudyIuid() {
        return studyIuid;
    }

    /**
     * @param studyIuid
     *            the studyIuid to set
     */
    public void setStudyIuid(String studyIuid) {
        this.studyIuid = studyIuid;
    }

    public String toString() {
        return new StringBuilder("StudyPermission[pk=").append(getPk()).append(
                ", suid=").append(getStudyIuid()).append(", action=").append(
                getAction()).append(", role=").append(getRole()).append("]")
                .toString();
    }

    public StudyPermissionDTO toDTO() {
        StudyPermissionDTO dto = new StudyPermissionDTO();
        dto.setPk(getPk().longValue());
        dto.setStudyIuid(getStudyIuid());
        dto.setAction(getAction());
        dto.setRole(getRole());
        return dto;
    }
}
