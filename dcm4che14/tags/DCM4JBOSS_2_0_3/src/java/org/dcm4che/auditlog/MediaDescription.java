/*                                                                           *
 *  Copyright (c) 2002 by TIANI MEDGRAPH AG                                  *
 *                                                                           *
 *  This file is part of dcm4che.                                            *
 *                                                                           *
 *  This library is free software; you can redistribute it and/or modify it  *
 *  under the terms of the GNU Lesser General Public License as published    *
 *  by the Free Software Foundation; either version 2 of the License, or     *
 *  (at your option) any later version.                                      *
 *                                                                           *
 *  This library is distributed in the hope that it will be useful, but      *
 *  WITHOUT ANY WARRANTY; without even the implied warranty of               *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU        *
 *  Lesser General Public License for more details.                          *
 *                                                                           *
 *  You should have received a copy of the GNU Lesser General Public         *
 *  License along with this library; if not, write to the Free Software      *
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA  *
 */
package org.dcm4che.auditlog;

/**
 * @author     <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @since      March 16, 2003
 * @version    $Revision$ $Date$
 */
public interface MediaDescription
{

    // Constants -----------------------------------------------------

    // Methods -------------------------------------------------------
    /**
     *  Sets the mediaID attribute of the InstancesAction object
     *
     * @param  mediaID  The new mediaID value
     */
    public void setMediaID(String mediaID);


    /**
     *  Sets the mediaType attribute of the InstancesAction object
     *
     * @param  mediaType  The new mediaType value
     */
    public void setMediaType(String mediaType);


    /**
     *  Sets the destination attribute of the InstancesAction object
     *
     * @param  dest  The new destination value
     */
    public void setDestination(Destination dest);


    /**
     *  Adds a feature to the Patient attribute of the InstancesAction object
     *
     * @param  patient  The feature to be added to the Patient attribute
     */
    public void addPatient(Patient patient);


    /**
     *  Adds a feature to the StudyInstanceUID attribute of the InstancesAction object
     *
     * @param  suid  The feature to be added to the StudyInstanceUID attribute
     */
    public void addStudyInstanceUID(String suid);


    /**
     *  Description of the Method
     *
     * @param  sb  Description of the Parameter
     */
    public void writeTo(StringBuffer sb);
}

