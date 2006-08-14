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
 * Gunter Zeilinger, Huetteldorferstr. 24/10, 1150 Vienna/Austria/Europe.
 * Portions created by the Initial Developer are Copyright (C) 2002-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See listed authors below.
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
package org.dcm4chee.arr.ejb;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.persistence.TemporalType;

import org.dcm4chee.arr.util.XSLTUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.RequestParameter;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.datamodel.DataModel;
import org.jboss.seam.annotations.datamodel.DataModelSelectionIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$
 * @since 29.07.2006
 */
@Stateful
@Name("auditRecordList")
@Scope(ScopeType.SESSION)
public class AuditRecordListAction implements AuditRecordList {
    
    private static final int FROM_POS = 16;
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final int YEAR = 4;
    private static final int MONTH = 7;
    private static final int DAY_OF_MONTH = 10;
    private static final int HOUR_OF_DAY = 13;
    private static final int MINUTE = 16;
    private static final int SECOND = 19;
    
    private static Logger log = LoggerFactory.getLogger(AuditRecordList.class);

    @PersistenceContext(type=PersistenceContextType.EXTENDED) 
    private EntityManager em;

    @DataModel
    private List<AuditRecord> records;
    
    @DataModelSelectionIndex
    private int selectedIndex = -1;
    
    @RequestParameter
    private Integer page;
    
    private int curPage = 1;
    private int pageSize = 20;
    private int count = 0;
    private boolean showXml = false;   
    private boolean orderByEventDateTime = false;
    
    private String dateTimeRange = today();
    private String[] eventIDs = { "" };
    private String[] eventTypes = { "" };
    private String[] eventActions = { "" };
    private String[] eventOutcomes = { "" };
    
    private String userID1 = "";
    private String altUserID1 = "";
    private String userName1 = "";
    private boolean userIsRequestor1 = false;
    private String[] roleIDs1 = { "" };
    private String[] napTypes1 = { "" };
    private String napID1 = "";

    private String userID2 = "";
    private String altUserID2 = "";
    private String userName2 = "";
    private boolean userIsRequestor2 = false;
    private String[] roleIDs2 = { "" };
    private String[] napTypes2 = { "" };
    private String napID2 = "";
    
    private String siteID = "";
    private String sourceID = "";
    private String[] sourceTypes = { "" };
    
    private String[] objectTypes = { "" };
    private String[] objectRoles = { "" };
    private String[] lifeCycles = { "" };
    private String[] objectIDTypes = { "" };
    private String objectID = "";
    private String objectName = "";

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getCount() {
        return count;
    }

    public int getCurPage() {
        return curPage;
    }
    
    public int getMinPage() {
        return Math.max(1, Math.min(curPage, getLastPage() - 5) - 4);
    }
    
    public int getMaxPage() {
        return Math.min(getLastPage(), Math.max(5, curPage) + 4);
    }
    
    public int getLastPage() {
        return (count-1) / pageSize + 1;
    }
    
    public int getFirstResult() {
        return (curPage-1) * pageSize;
    }

    private static String today() {
	return new SimpleDateFormat(DATE_FORMAT).format(new Date());
    }
    
    public String getDateTimeRange() {
        return dateTimeRange;
    }

    public void setDateTimeRange(String dt) {
        this.dateTimeRange = dt;
    }

    public boolean isOrderByEventDateTime() {
        return orderByEventDateTime;
    }

    public void setOrderByEventDateTime(boolean enable) {
        this.orderByEventDateTime = enable;
    }

    public String[] getEventIDs() {
        return eventIDs;
    }

    public void setEventIDs(String[] ids) {
        this.eventIDs = ids;
    }
        
    public String[] getEventTypes() {
        return eventTypes;
    }

    public void setEventTypes(String[] types) {
        this.eventTypes = types;
    }

    public String[] getEventActions() {
        return eventActions;
    }

    public void setEventActions(String[] actions) {
        this.eventActions = actions;
    }

    public String[] getEventOutcomes() {
        return eventOutcomes;
    }

    public void setEventOutcomes(String[] outcomes) {
        this.eventOutcomes = outcomes;
    }
    
    public String getUserID1() {
        return userID1;
    }

    public void setUserID1(String id) {
        this.userID1 = id;
    }

    public String getAltUserID1() {
        return altUserID1;
    }

    public void setAltUserID1(String id) {
        this.altUserID1 = id;
    }

    public String getUserName1() {
        return userName1;
    }

    public void setUserName1(String name) {
        this.userName1 = name;
    }
    
    public boolean isUserIsRequestor1() {
        return userIsRequestor1;
    }

    public void setUserIsRequestor1(boolean requestor) {
        this.userIsRequestor1 = requestor;
    }

    public String[] getRoleIDs1() {
	return roleIDs1;
    }
    
    public void setRoleIDs1(String[] ids) {
	this.roleIDs1 = ids;
    }

    public String[] getNapTypes1() {
	return napTypes1;
    }
    
    public void setNapTypes1(String[] types) {
	this.napTypes1 = types;
    }
    
    public String getNapID1() {
        return napID1;
    }

    public void setNapID1(String id) {
        this.napID1 = id;
    }
    
    public String getUserID2() {
        return userID2;
    }

    public void setUserID2(String id) {
        this.userID2 = id;
    }

    public String getAltUserID2() {
        return altUserID2;
    }

    public void setAltUserID2(String id) {
        this.altUserID2 = id;
    }

    public String getUserName2() {
        return userName2;
    }

    public void setUserName2(String name) {
        this.userName2 = name;
    }
    
    public boolean isUserIsRequestor2() {
        return userIsRequestor2;
    }

    public void setUserIsRequestor2(boolean requestor) {
        this.userIsRequestor2 = requestor;
    }

    public String[] getRoleIDs2() {
	return roleIDs2;
    }
    
    public void setRoleIDs2(String[] ids) {
	this.roleIDs2 = ids;
    }

    public String[] getNapTypes2() {
	return napTypes2;
    }
    
    public void setNapTypes2(String[] types) {
	this.napTypes2 = types;
    }
    
    public String getNapID2() {
        return napID2;
    }

    public void setNapID2(String id) {
        this.napID2 = id;
    }
    
    public String getSiteID() {
        return siteID;
    }

    public void setSiteID(String id) {
        this.siteID = id;
    }

    public String getSourceID() {
        return sourceID;
    }

    public void setSourceID(String id) {
        this.sourceID = id;
    }

    public String[] getSourceTypes() {
        return sourceTypes;
    }

    public void setSourceTypes(String[] types) {
        this.sourceTypes = types;
    }

    public String[] getObjectTypes() {
        return objectTypes;
    }

    public void setObjectTypes(String[] types) {
        this.objectTypes = types;
    }

    public String[] getObjectRoles() {
        return objectRoles;
    }

    public void setObjectRoles(String[] roles) {
        this.objectRoles = roles;
    }

    public String[] getLifeCycles() {
        return lifeCycles;
    }

    public void setLifeCycles(String[] lifeCycles) {
        this.lifeCycles = lifeCycles;
    }

    public String[] getObjectIDTypes() {
        return objectIDTypes;
    }

    public void setObjectIDTypes(String[] types) {
        this.objectIDTypes = types;
    }

    public String getObjectID() {
        return objectID;
    }

    public void setObjectID(String id) {
        this.objectID = id;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String name) {
        this.objectName = name;
    }

    @Factory("records")
    public String find() {
	curPage = 1;
	updateResults();
	return ("browse");
    }
    
    public void selectPage() {
	curPage = page;
	updateResults();
    }
    
    @SuppressWarnings("unchecked")
    private void updateResults() {
	Date[] dtRange;
	try {
	    dtRange = parseDateTimeRange();
	} catch (Exception e) {
	    dateTimeRange = today();
	    try {
		dtRange = parseDateTimeRange();
	    } catch (Exception e1) {
		throw new RuntimeException(e1);
	    }
	}
	StringBuffer query = new StringBuffer("SELECT COUNT(*) FROM AuditRecord r WHERE ");
	query.append(orderByEventDateTime ? "r.eventDateTime" : "r.receiveDateTime");
	query.append(" BETWEEN :from AND :to");
	count = ((Long) em.createQuery(query.toString())
		.setParameter("from", dtRange[0], TemporalType.TIMESTAMP)
		.setParameter("to", dtRange[1], TemporalType.TIMESTAMP)
		.getSingleResult()).intValue();
        query.append(" ORDER BY ");
        if (orderByEventDateTime) {
            query.append("r.eventDateTime DESC, ");
        }
        query.append("r.pk DESC");
        records = em.createQuery(query.substring(FROM_POS))
        	.setParameter("from", dtRange[0], TemporalType.TIMESTAMP)
        	.setParameter("to", dtRange[1], TemporalType.TIMESTAMP)
        	.setFirstResult(getFirstResult())
        	.setMaxResults(pageSize)
        	.setHint("org.hibernate.readOnly", Boolean.TRUE)
        	.getResultList();
	log.info("Found {} records",  count);
	selectedIndex = -1;
    }
    
    private Date[] parseDateTimeRange() throws ParseException {
	int dtlen = dateTimeRange.length();
	if (dtlen < YEAR) {
	    throw new ParseException("Missing year", dtlen);
	}
	String dtformat = DATE_TIME_FORMAT.substring(0, dtlen);
	Date[] dtRange = new Date[2];
	dtRange[0] = new SimpleDateFormat(dtformat).parse(dateTimeRange);
	Calendar cal = Calendar.getInstance();
	cal.setTime(dtRange[0]);
	cal.set(Calendar.MILLISECOND, 999);
	if (dtlen < SECOND) {
	    cal.set(Calendar.SECOND, 59);
	    if (dtlen < MINUTE) {
		cal.set(Calendar.MINUTE, 59);
		if (dtlen < HOUR_OF_DAY) {
		    cal.set(Calendar.HOUR_OF_DAY, 23);
		    if (dtlen < DAY_OF_MONTH) {
		        if (dtlen < MONTH) {
		            cal.set(Calendar.MONTH, 11);
		        }
		        cal.add(Calendar.MONTH, 1);
		        cal.add(Calendar.DAY_OF_MONTH, -1);
		    }
		}
	    }
	}
	dtRange[1] = cal.getTime();
	return dtRange;
    }
    
    public boolean isShowXml() {
        return showXml;
    }

    public void showXml() {
        showXml = true;
    }

    public void showDetails() {
        showXml = false;
    }
    
    public String getXml() {
        if (selectedIndex == -1) {
            return "No Record selected";
        }
        return XSLTUtils.toXML(records.get(selectedIndex).getXmldata());
    }
    
    public String getDetails() {
        if (selectedIndex == -1) {
            return "No Record selected";
        }
        return XSLTUtils.toDetails(records.get(selectedIndex).getXmldata());
    }
    
    public String getRowClasses() {
	int n = records.size();
	if (n == 0) {
	    return "";
	}
	StringBuffer sb = new StringBuffer((n + 3)/2 * 9);
	for (int i = 0; i < n; i++) {
	    sb.append((i & 1) == 0 ? "odd" : "even");
	    if (i == selectedIndex) {
		sb.append(" selected");
	    }
	    sb.append(',');
	}
	return sb.substring(0, sb.length()-1);
    }

    @Destroy @Remove
    public void destroy() {}
    
}
