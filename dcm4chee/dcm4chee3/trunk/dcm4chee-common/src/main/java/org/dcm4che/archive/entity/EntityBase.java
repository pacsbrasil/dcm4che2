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

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

/**
 * Base class for persistent domain objects
 * 
 * @author <a href="mailto:jfalkmu@gmail.com">jfalk</a>
 */
@MappedSuperclass
public abstract class EntityBase implements Serializable {
    private static final long serialVersionUID = 4065240548857418492L;

    @Id
    @Column(name="pk")
    private Long pk;

    /**
     * Return the primary key for the object, which represents its database
     * record.
     * 
     * @return java.lang.Long
     */
    public Long getPk() {
        return pk;
    }

    /**
     * Set the primary key for the object, mapping it to a database record.
     * 
     * @param pk
     *            A java.lang.Long value specifying the new primary key for the
     *            object
     */
    public void setPk(Long pk) {
        this.pk = pk;
    }

    /**
     * Override hashCode.
     * 
     * @return the Objects hashcode.
     */
    public int hashCode() {
        int hashCode = 1;
        hashCode = 31 * hashCode + (pk == null ? 0 : pk.hashCode());
        return hashCode;
    }

    /**
     * Returns <code>true</code> if this <code>EntityBase</code> is the same
     * as the o argument.
     * 
     * @return <code>true</code> if this <code>EntityBase</code> is the same
     *         as the o argument.
     */
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (o.getClass() != getClass()) {
            return false;
        }
        EntityBase castedObj = (EntityBase) o;
        return ((this.pk == null ? castedObj.pk == null : this.pk
                .equals(castedObj.pk)));
    }
}