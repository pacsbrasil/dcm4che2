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

import java.sql.Timestamp;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.PersonName;
import org.dcm4che.dict.Tags;

/**
 * org.dcm4che.archive.entity.VerifyingObserver
 * 
 * @author <a href="mailto:damien.daddy@gmail.com">Damien Evans</a>
 */
@Entity
@Table(name = "verify_observer")
public class VerifyingObserver extends EntityBase {

    private static final long serialVersionUID = -4980872219863544848L;

    @Column(name = "verify_datetime")
    private Timestamp verificationDateTime;

    @Column(name = "observer_name")
    private String verifyingObserverName;

    @Column(name = "observer_i_name")
    private String verifyingObserverIdeographicName;

    @Column(name = "observer_p_name")
    private String verifyingObserverPhoneticName;
    
    @ManyToOne(targetEntity = org.dcm4che.archive.entity.Instance.class)
    @JoinColumn(name = "instance_fk")
    private Instance instance;

    /**
     * 
     */
    public VerifyingObserver() {
    }

    /**
     * 
     */
    public VerifyingObserver(Dataset item) {
        this(item.getDate(Tags.VerificationDateTime), item
                .getPersonName(Tags.VerifyingObserverName));
    }

    /**
     * 
     */
    public VerifyingObserver(Date verDateTime, PersonName verObsName) {
        setVerificationDateTime(verDateTime);
        setVerifyingObserverName(verObsName);
    }

    /**
     * @return the verificationDateTime
     */
    public Timestamp getVerificationDateTime() {
        return verificationDateTime;
    }

    /**
     * @param verificationDateTime
     *            the verificationDateTime to set
     */
    public void setVerificationDateTime(Timestamp verificationDateTime) {
        this.verificationDateTime = verificationDateTime;
    }

    private void setVerificationDateTime(java.util.Date date) {
        setVerificationDateTime(date != null ? new java.sql.Timestamp(date
                .getTime()) : null);
    }

    /**
     * @return the verifyingObserverIdeographicName
     */
    public String getVerifyingObserverIdeographicName() {
        return verifyingObserverIdeographicName;
    }

    /**
     * @param verifyingObserverIdeographicName
     *            the verifyingObserverIdeographicName to set
     */
    public void setVerifyingObserverIdeographicName(
            String verifyingObserverIdeographicName) {
        this.verifyingObserverIdeographicName = verifyingObserverIdeographicName;
    }

    /**
     * @return the verifyingObserverName
     */
    public String getVerifyingObserverName() {
        return verifyingObserverName;
    }

    /**
     * @param verifyingObserverName
     *            the verifyingObserverName to set
     */
    public void setVerifyingObserverName(String verifyingObserverName) {
        this.verifyingObserverName = verifyingObserverName;
    }

    private void setVerifyingObserverName(PersonName pn) {
        if (pn == null) {
            return;
        }
        PersonName ipn = pn.getIdeographic();
        PersonName ppn = pn.getPhonetic();
        String name;
        if ((name = pn.toComponentGroupString(false)) != null) {
            setVerifyingObserverName(name.toUpperCase());
        }
        if (ipn != null && (name = ipn.toComponentGroupString(false)) != null) {
            setVerifyingObserverIdeographicName(name.toUpperCase());
        }
        if (ppn != null && (name = ppn.toComponentGroupString(false)) != null) {
            setVerifyingObserverPhoneticName(name.toUpperCase());
        }
    }

    /**
     * @return the verifyingObserverPhoneticName
     */
    public String getVerifyingObserverPhoneticName() {
        return verifyingObserverPhoneticName;
    }

    /**
     * @param verifyingObserverPhoneticName
     *            the verifyingObserverPhoneticName to set
     */
    public void setVerifyingObserverPhoneticName(
            String verifyingObserverPhoneticName) {
        this.verifyingObserverPhoneticName = verifyingObserverPhoneticName;
    }

    /**
     * @return the instance
     */
    public Instance getInstance() {
        return instance;
    }

    /**
     * @param instance the instance to set
     */
    public void setInstance(Instance instance) {
        this.instance = instance;
    }

}
