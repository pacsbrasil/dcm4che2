/* $Id$
 * Copyright (c) 2002,2003 by TIANI MEDGRAPH AG
 *
 * This file is part of dcm4che.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.dcm4chex.archive.ejb.entity;

import javax.ejb.CreateException;
import javax.ejb.EntityBean;
import javax.ejb.RemoveException;

import org.apache.log4j.Logger;
import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;
import org.dcm4chex.archive.ejb.interfaces.StudyLocal;
import org.dcm4chex.archive.ejb.util.DatasetUtil;
import org.dcm4chex.archive.ejb.util.StringUtils;

/**

/**
 * @ejb.bean
 *  name="Series"
 *  type="CMP"
 *  view-type="local"
 *  primkey-field="pk"
 *  local-jndi-name="ejb/Series"
 * 
 * @ejb.transaction 
 *  type="Required"
 * 
 * @ejb.persistence
 *  table-name="series"
 * 
 * @jboss.entity-command
 *  name="postgresql-fetch-seq"
 * 
 * @ejb.finder
 *  signature="Collection findAll()"
 *  query="SELECT OBJECT(a) FROM Series AS a"
 *  transaction-type="Supports"
 *
 * @ejb.finder
 *  signature="org.dcm4chex.archive.ejb.interfaces.SeriesLocal findBySeriesIuid(java.lang.String uid)"
 *  query="SELECT OBJECT(a) FROM Series AS a WHERE a.seriesIuid = ?1"
 *  transaction-type="Supports"
 *
 * @author <a href="mailto:gunter@tiani.com">Gunter Zeilinger</a>
 *
 */
public abstract class SeriesBean implements EntityBean {

    private static final String ATTRS_CFG = "series-attrs.cfg";

    private static final Logger log = Logger.getLogger(SeriesBean.class);
    
    /**
     * Auto-generated Primary Key
     *
     * @ejb.interface-method
     * @ejb.pk-field
     * @ejb.persistence
     *  column-name="pk"
     * @jboss.persistence
     *  auto-increment="true"
     *
     */
    public abstract Integer getPk();

    public abstract void setPk(Integer pk);

    /**
     * Series Instance UID
     *
     * @ejb.interface-method
     * @ejb.persistence
     *  column-name="series_iuid"
     */
    public abstract String getSeriesIuid();

    public abstract void setSeriesIuid(String uid);

    /**
     * Series Number
     *
     * @ejb.interface-method
     * @ejb.persistence
     *  column-name="series_no"
     */
    public abstract String getSeriesNumber();

    public abstract void setSeriesNumber(String no);
    
    /**
     * Modality
     *
     * @ejb.interface-method
     * @ejb.persistence
     *  column-name="modality"
     */
    public abstract String getModality();

    public abstract void setModality(String md);
    
    /**
     * PPS Start Datetime
     *
     * @ejb.interface-method
     * @ejb.persistence
     *  column-name="pps_start"
     */
    public abstract java.util.Date getPpsStartDateTime();

    /**
     * Number Of Series Related Instances
     *
     * @ejb.interface-method
     * @ejb.persistence
     *  column-name="num_instances"
     * 
     */
    public abstract int getNumberOfSeriesRelatedInstances();

    public abstract void setNumberOfSeriesRelatedInstances(int num);

    public abstract void setPpsStartDateTime(java.util.Date datetime);
    
    /**
     * Encoded Series Dataset
     *
     * @ejb.persistence
     *  column-name="series_attrs"
     * 
     */
    public abstract byte[] getEncodedAttributes();

    public abstract void setEncodedAttributes(byte[] attr);

    /**
     * Retrieve AETs
     *
     * @ejb.persistence
     *  column-name="retrieve_aets"
     */
    public abstract String getRetrieveAETsField();

    public abstract void setRetrieveAETsField(String aets);

    /**
     * @ejb.relation
     *  name="study-series"
     *  role-name="series-of-study"
     *  cascade-delete="yes"
     *
     * @jboss:relation
     *  fk-column="study_fk"
     *  related-pk-field="pk"
     * 
     * @param study study of this series
     */
    public abstract void setStudy(StudyLocal study);
    
    /**
     * @ejb.interface-method view-type="local"
     * 
     * @return study of this series
     */
    public abstract StudyLocal getStudy();
    
    /**
     * @ejb.interface-method view-type="local"
     *
     * @param series all instances of this series
     */
    public abstract void setInstances(java.util.Collection series);

    /**
     * @ejb.interface-method view-type="local"
     * @ejb.relation
     *  name="series-instance"
     *  role-name="series-has-instance"
     *    
     * @return all instances of this series
     */
    public abstract java.util.Collection getInstances();

    /**
     * Create series.
     *
     * @ejb.create-method
     */
    public Integer ejbCreate(Dataset ds, StudyLocal study) throws CreateException {
        setAttributes(ds);
        return null;
    }

    public void ejbPostCreate(Dataset ds, StudyLocal study) throws CreateException {
        setStudy(study);
        log.info("Created " + prompt());
    }

    public void ejbRemove() throws RemoveException {
        log.info("Deleting " + prompt());
    }
    
    /**
     * @ejb.interface-method
     */
    public void update()
    {
        setNumberOfSeriesRelatedInstances(getInstances().size());
    }

    /**
     * 
     * @ejb.interface-method
     */
    public void setAttributes(Dataset ds)
    {
        setSeriesIuid(ds.getString(Tags.SeriesInstanceUID));
        setSeriesNumber(ds.getString(Tags.SeriesNumber));
        setModality(ds.getString(Tags.Modality));
        setPpsStartDateTime(ds.getDateTime(Tags.PPSStartDate, Tags.PPSStartTime));
        setEncodedAttributes(DatasetUtil.toByteArray(ds.subSet(DatasetUtil.getFilter(ATTRS_CFG))));        
    }

    /**
     * @ejb.interface-method
     */
    public Dataset getAttributes()
    {
        return DatasetUtil.fromByteArray(getEncodedAttributes());
    }
    
    /**
     * 
     * @ejb.interface-method
     */
    public String[] getRetrieveAETs()
    {
        return StringUtils.split(getRetrieveAETsField(), ',');
    }
    
    /**
     * 
     * @ejb.interface-method
     */
    public void setRetrieveAETs(String[] aets)
    {
        setRetrieveAETsField(StringUtils.toString(aets, ','));
    }
    
    /**
     * 
     * @ejb.interface-method
     */
    public String asString()
    {
        return prompt();
    }

    private String prompt() {
        return "Series[pk="
            + getPk()
            + ", uid="
            + getSeriesIuid()
            + ", study->"
            + getStudy()
            + "]";
    }
}
