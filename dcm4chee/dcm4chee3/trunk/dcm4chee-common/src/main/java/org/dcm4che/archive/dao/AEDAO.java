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
 * Damien Evans <damien.daddy@gmail.com>
 * Justin Falk <jfalkmu@gmail.com>
 * Jeremy Vosters <jlvosters@gmail.com>
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
package org.dcm4che.archive.dao;

import javax.ejb.Local;
import javax.persistence.NoResultException;

import org.dcm4che.archive.entity.AE;

/**
 * org.dcm4che.archive.dao.AEDAO
 * 
 * @author <a href="mailto:damien.daddy@gmail.com">Damien Evans</a>
 */
@Local
public interface AEDAO extends DAO<AE> {
    public static final String JNDI_NAME = "dcm4cheeArchive/AEDAOImpl/local";

    /**
     * Create an AE and initialize all of its fields.
     * 
     * @param pk
     *            The primary key of the AE record in the database. May be null
     *            if the AE is new.
     * @param aet
     *            The AE title.
     * @param aeHost
     *            The host name.
     * @param portNum
     *            The port number of the AE title.
     * @param cipher
     *            Security protocols in use by the AE.
     * @param issuer
     *            The issuer of the patient id where this AE is located/used.
     * @param user
     *            A string containing the user id for user identity negotiation.
     * @param passwd
     *            A string containing the password for user identity
     *            negotiation.
     * @param desc
     *            A description of the AE.
     */
    public AE create(String title, String hostname, int port,
            String cipherSuites, String issuer, String user, String passwd,
            String desc) throws ContentCreateException;

    public AE findByAET(String aet) throws NoResultException;
}
