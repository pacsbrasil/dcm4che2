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
 * Justin Falk <jfalkmu@gmail.com>
 * Damien Evans <damien.daddy@gmail.com>
 * Gunter Zeilinger <gunterze@gmail.com>
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
package org.dcm4che.archive.entity;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * org.dcm4che.archive.entity.StudyOnFileSystem
 * 
 * @author <a href="mailto:damien.daddy@gmail.com">Damien Evans</a>
 */
@Entity
@Table(name = "study_on_fs")
@SequenceGenerator(name = "SEQ_STORE", sequenceName = "study_on_fs_s")
public class StudyOnFileSystem extends EntityBase {

    private static final long serialVersionUID = 1716183638389803719L;

    @Column(name = "access_time", nullable = false)
    private Timestamp accessTime;

    @ManyToOne(targetEntity = org.dcm4che.archive.entity.Study.class)
    @JoinColumn(name = "study_fk")
    private Study study;

    @ManyToOne(targetEntity = org.dcm4che.archive.entity.FileSystem.class)
    @JoinColumn(name = "filesystem_fk")
    private FileSystem fileSystem;

    /**
     * @return the accessTime
     */
    public Timestamp getAccessTime() {
        return accessTime;
    }

    /**
     * @param accessTime
     *            the accessTime to set
     */
    public void setAccessTime(Timestamp accessTime) {
        this.accessTime = accessTime;
    }

    /**
     * @return the fileSystem
     */
    public FileSystem getFileSystem() {
        return fileSystem;
    }

    /**
     * @param fileSystem
     *            the fileSystem to set
     */
    public void setFileSystem(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }

    /**
     * @return the study
     */
    public Study getStudy() {
        return study;
    }

    /**
     * @param study
     *            the study to set
     */
    public void setStudy(Study study) {
        this.study = study;
    }

    public String toString() {
        return "StudyOnFileSystem[" + (study == null ? "null" : study.toString()) + "@"
                + (fileSystem == null ? "null" : fileSystem.toString()) + "]";
    }

    /**
     * Update the access time of this object.
     */
    public void touch() {
        setAccessTime(new Timestamp(System.currentTimeMillis()));
    }
}
