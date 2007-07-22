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

package org.dcm4che.archive.dao.jpa;

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;

import org.dcm4che.archive.dao.CodeDAO;
import org.dcm4che.archive.dao.ContentCreateException;
import org.dcm4che.archive.dao.ContentDeleteException;
import org.dcm4che.archive.dao.GPSPSPerformerDAO;
import org.dcm4che.archive.entity.Code;
import org.dcm4che.archive.entity.GPSPS;
import org.dcm4che.archive.entity.GPSPSPerformer;
import org.dcm4che.archive.util.StringUtils;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.PersonName;
import org.dcm4che.dict.Tags;

/**
 * org.dcm4che.archive.dao.jpa.GPSPSPerformerDAOImpl
 * 
 * @author <a href="mailto:damien.daddy@gmail.com">Damien Evans</a>
 */
@Stateless
@TransactionManagement(value = TransactionManagementType.CONTAINER)
public class GPSPSPerformerDAOImpl extends BaseDAOImpl<GPSPSPerformer>
        implements GPSPSPerformerDAO {
    
    
    private CodeDAO codeDAO;

    /**
     * 
     */
    public GPSPSPerformerDAOImpl() {
    }

    /**
     * @see org.dcm4che.archive.dao.jpa.BaseDAOImpl#getPersistentClass()
     */
    @Override
    public Class getPersistentClass() {
        return GPSPSPerformer.class;
    }

    /**
     * @see org.dcm4che.archive.dao.GPSPSPerformerDAO#create(org.dcm4che.data.Dataset,
     *      org.dcm4che.archive.entity.GPSPS)
     */
    public GPSPSPerformer create(Dataset item, GPSPS gpsps)
            throws ContentCreateException {
        
        GPSPSPerformer performer = new GPSPSPerformer();

        PersonName pn = item.getPersonName(Tags.HumanPerformerName);
        if (pn != null) {
            performer.setHumanPerformerName(StringUtils.toUpperCase(pn.toComponentGroupString(false)));
            PersonName ipn = pn.getIdeographic();
            if (ipn != null) {
                performer.setHumanPerformerIdeographicName(ipn.toComponentGroupString(false));
            }
            PersonName ppn = pn.getPhonetic();
            if (ppn != null) {
                performer.setHumanPerformerPhoneticName(ppn.toComponentGroupString(false));
            }
        }  
        performer.setCode(Code.valueOf(codeDAO, item
                .getItem(Tags.HumanPerformerCodeSeq)));

        performer.setGpsps(gpsps);
        save(performer);
        logger.info("Created " + performer);
        return performer;

    }

    /**
     * @see org.dcm4che.archive.dao.DAO#remove(org.dcm4che.archive.entity.EntityBase)
     */
    public void remove(GPSPSPerformer obj) throws ContentDeleteException {
    }

    /**
     * @see org.dcm4che.archive.dao.DAO#save(org.dcm4che.archive.entity.EntityBase)
     */
    public void save(GPSPSPerformer obj) throws ContentCreateException {
    }
    
    /**
     * @return the codeDAO
     */
    public CodeDAO getCodeDAO() {
        return codeDAO;
    }

    /**
     * @param codeDAO
     *            the codeDAO to set
     */
    public void setCodeDAO(CodeDAO codeDAO) {
        this.codeDAO = codeDAO;
    }

}
