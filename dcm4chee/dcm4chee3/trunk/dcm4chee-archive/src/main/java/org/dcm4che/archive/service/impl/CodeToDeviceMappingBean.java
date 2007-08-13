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
 * Java(TM), available at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * TIANI Medgraph AG.
 * Portions created by the Initial Developer are Copyright (C) 2003-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Damien Evans <damien.daddy@gmail.com>
 * Gunter Zeilinger <gunter.zeilinger@tiani.com>
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

package org.dcm4che.archive.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;

import org.apache.log4j.Logger;
import org.dcm4che.archive.dao.CodeDAO;
import org.dcm4che.archive.dao.ContentCreateException;
import org.dcm4che.archive.dao.ContentDeleteException;
import org.dcm4che.archive.dao.DeviceDAO;
import org.dcm4che.archive.entity.Code;
import org.dcm4che.archive.entity.Device;
import org.dcm4che.archive.service.CodeToDeviceMapping;
import org.dcm4che.archive.service.CodeToDeviceMappingLocal;
import org.dcm4che.archive.service.CodeToDeviceMappingRemote;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.dict.Tags;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author franz.willer@tiani.com
 * @version $Revision: 1.1 $ $Date: 2007/06/23 18:59:01 $
 * @since 17.02.2005
 */
//EJB3
@Stateless
@TransactionManagement(TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
// Spring
@Transactional(propagation = Propagation.REQUIRED)
public class CodeToDeviceMappingBean implements CodeToDeviceMappingLocal, CodeToDeviceMappingRemote {

    @EJB private CodeDAO codeDAO;

    @EJB private DeviceDAO devDAO;

    private static final Logger log = Logger
            .getLogger(CodeToDeviceMappingBean.class);

    /** 
     * @see org.dcm4che.archive.service.CodeToDeviceMapping#createMapping(org.dcm4che.data.Dataset)
     */
    public void createMapping(Dataset ds) throws ContentCreateException,
            PersistenceException {
        DcmElement spsSeq = ds.get(Tags.SPSSeq);
        DcmElement codeSeq;
        int len = spsSeq.countItems();
        int lenCS;
        Dataset ds1, dsCode;
        for (int i = 0; i < len; i++) {
            ds1 = spsSeq.getItem(i);
            Device device = getDevice(ds1);
            List codes = new ArrayList();
            codeSeq = ds1.get(Tags.ScheduledProtocolCodeSeq);
            lenCS = codeSeq.countItems();
            for (int j = 0; j < lenCS; j++) {
                dsCode = codeSeq.getItem(j);
                codes.add(Code.valueOf(codeDAO, dsCode));
            }
            device.setProtocolCodes(codes);
            log.info("Device " + device.getStationName()
                    + " mapped to protocols:" + codes);
        }
    }

    /**
     * @param ds1
     * @return
     * @throws PersistenceException
     * @throws ContentCreateException
     */
    private Device getDevice(Dataset ds1) throws PersistenceException,
            ContentCreateException {
        String name = ds1.getString(Tags.ScheduledStationName);
        String aet = ds1.getString(Tags.ScheduledStationAET);
        String modality = ds1.getString(Tags.Modality);
        Device device = null;
        try {
            device = devDAO.findByStationName(name);
        }
        catch (Exception x) { // IGNORE
        }
        if (device == null) {
            device = devDAO.create(name, aet, modality);
        }
        else {
            // delete mapping with this device ?
            // update aet and/or modality ?
        }
        return device;
    }

    /** 
     * @see org.dcm4che.archive.service.CodeToDeviceMapping#addScheduledStationInfo(org.dcm4che.data.Dataset)
     */
    public Dataset addScheduledStationInfo(Dataset ds)
            throws PersistenceException {
        DcmElement spsSeq = ds.get(Tags.SPSSeq);
        DcmElement codeSeq;
        int len = spsSeq.countItems();
        Dataset sps;
        for (int i = 0; i < len; i++) {
            sps = spsSeq.getItem(i);
            Device device = lookupDevice(sps);
            if (device != null) {
                sps.putAE(Tags.ScheduledStationAET, device.getStationAET());
                sps.putSH(Tags.ScheduledStationName, device.getStationName());
                String reqModality = sps.getString(Tags.Modality);
                if (reqModality == null) {
                    sps.putCS(Tags.Modality, device.getModality());
                }
                else if (!reqModality.equals(device.getModality())) {
                    log.warn("Different Modality (" + reqModality
                            + ") in request and device ("
                            + device.getModality() + ") !!!");
                }
            }
        }
        return ds;
    }

    private Device lookupDevice(Dataset sps) throws PersistenceException {
        String name = sps.getString(Tags.ScheduledStationName);
        if (name != null) {
            try {
                return devDAO.findByStationName(name);
            }
            catch (NoResultException ignore) {
                log.warn("Failed to find device with name: " + name);
            }
        }
        Dataset protocol = sps.getItem(Tags.ScheduledProtocolCodeSeq);
        if (protocol == null) {
            return null;
        }
        String codeValue = protocol.getString(Tags.CodeValue);
        String codingScheme = protocol.getString(Tags.CodingSchemeDesignator);
        Collection col = codeDAO.findByValueAndDesignator(codeValue,
                codingScheme);
        if (col.isEmpty()) {
            log.warn("Failed to find device for unkown Protocol Code: "
                    + codeValue + '^' + codingScheme);
            return null;
        }
        Collection<Device> col1 = devDAO.findByProtocolCode((Code) col
                .iterator().next());
        if (col1.isEmpty()) {
            log.warn("Failed to find device for Protocol Code: " + codeValue
                    + '^' + codingScheme);
            return null;
        }
        return col1.iterator().next();
    }

    /** 
     * @see org.dcm4che.archive.service.CodeToDeviceMapping#deleteDevice(java.lang.String)
     */
    public boolean deleteDevice(String stationName)
            throws PersistenceException, ContentDeleteException {
        Device device = devDAO.findByStationName(stationName);
        if (device == null)
            return false;
        deleteDevice(device);
        return true;
    }

    private void deleteDevice(Device device) throws ContentDeleteException {
        Collection col = new ArrayList();
        col.addAll(device.getProtocolCodes()); // need independent collection!
        String name = device.getStationName();
        log.info("Delete device:" + name);
        devDAO.remove(device);
        Iterator iter = col.iterator();
        while (iter.hasNext()) {
            Code code = (Code) iter.next();
            log.info("Delete protocol code for device (" + name + "):"
                    + code.getCodeValue() + "("
                    + code.getCodingSchemeDesignator() + ")");
            try {
                codeDAO.remove(code);
            }
            catch (Exception x) {
                log.error("Cant delete code ('" + code.getCodeValue() + "')");
            }
        }

    }

    /** 
     * @see org.dcm4che.archive.service.CodeToDeviceMapping#deleteMapping()
     */
    public boolean deleteMapping() throws PersistenceException,
            ContentDeleteException {
        Collection<Device> col = devDAO.findAll();
        Iterator iter = col.iterator();
        while (iter.hasNext()) {
            deleteDevice((Device) iter.next());
        }
        return true;
    }

    /** 
     * @see org.dcm4che.archive.service.CodeToDeviceMapping#getCodeDAO()
     */
    public CodeDAO getCodeDAO() {
        return codeDAO;
    }

    /** 
     * @see org.dcm4che.archive.service.CodeToDeviceMapping#setCodeDAO(org.dcm4che.archive.dao.CodeDAO)
     */
    public void setCodeDAO(CodeDAO codeDAO) {
        this.codeDAO = codeDAO;
    }

    /** 
     * @see org.dcm4che.archive.service.CodeToDeviceMapping#getDevDAO()
     */
    public DeviceDAO getDevDAO() {
        return devDAO;
    }

    /** 
     * @see org.dcm4che.archive.service.CodeToDeviceMapping#setDevDAO(org.dcm4che.archive.dao.DeviceDAO)
     */
    public void setDevDAO(DeviceDAO devDAO) {
        this.devDAO = devDAO;
    }

}