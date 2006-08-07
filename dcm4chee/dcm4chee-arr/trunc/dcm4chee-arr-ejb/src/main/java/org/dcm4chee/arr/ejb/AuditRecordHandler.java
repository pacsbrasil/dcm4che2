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
 * Gunter Zeilinger <gunterze@gmail.com>
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision$ $Date$
 * @since Jun 17, 2006
 * 
 */
class AuditRecordHandler extends DefaultHandler {

    private Logger log = LoggerFactory.getLogger(AuditRecordHandler.class);

    private EntityManager em;
    private AuditRecord rec;
    private ActiveParticipant ap;
    private AuditSource as;
    private int asTypeCount;
    private ParticipantObject po;
    private StringBuffer sb = new StringBuffer(64);
    private boolean append;

    public AuditRecordHandler(EntityManager em) {
        this.em = em;
    }

    public void setAuditRecord(AuditRecord rec) {
        this.rec = rec;
    }

    public void reset() {
        rec = null;
        ap = null;
        as = null;
        asTypeCount = 0;
        po = null;
        sb.setLength(0);
        append = false;
    }

    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        if (append) {
            sb.append(ch, start, length);
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName,
            Attributes attrs) throws SAXException {
        if ("EventIdentification".equals(qName)) {
            eventIdentification(attrs);
        } else if ("EventID".equals(qName)) {
            eventID(attrs);
        } else if ("EventTypeCode".equals(qName)) {
            eventTypeCode(attrs);
        } else if ("ActiveParticipant".equals(qName)) {
            activeParticipant(attrs);
        } else if ("RoleIDCode".equals(qName)) {
            roleIDCode(attrs);
        } else if ("AuditSourceIdentification".equals(qName)) {
            auditSourceIdentification(attrs);
        } else if ("AuditSourceTypeCode".equals(qName)) {
            auditSourceTypeCode(attrs);
        } else if ("ParticipantObjectIdentification".equals(qName)) {
            participantObjectIdentification(attrs);
        } else if ("ParticipantObjectIDTypeCode".equals(qName)) {
            participantObjectIDTypeCode(attrs);
        } else if ("ParticipantObjectName".equals(qName)) {
            participantObjectName(attrs);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        if ("ActiveParticipant".equals(qName)) {
            activeParticipant();
        } else if ("AuditSourceIdentification".equals(qName)) {
            auditSourceIdentification();
        } else if ("ParticipantObjectIdentification".equals(qName)) {
            participantObjectIdentification();
        } else if ("ParticipantObjectName".equals(qName)) {
            participantObjectName();
        }
    }

    private void eventIdentification(Attributes attrs) {
        rec.setEventActionCode(attrs.getValue("EventActionCode"));
        rec.setEventOutcomeIndicator(parseInt(attrs
                .getValue("EventOutcomeIndicator")));
        rec.setEventDateTime(parseISO8601DateTime(attrs
                .getValue("EventDateTime")));
    }

    private void eventID(Attributes attrs) {
        rec.setEventID(toCode(attrs));
    }

    private void eventTypeCode(Attributes attrs) {
        Collection<Code> c = rec.getEventTypeCode();
        if (c == null) {
            c = new ArrayList<Code>(3);
            rec.setEventTypeCode(c);
        }
        c.add(toCode(attrs));
    }

    private void activeParticipant(Attributes attrs) {
        ap = new ActiveParticipant();
        ap.setAuditRecord(rec);
        ap.setUserID(toUpper(attrs.getValue("UserID")));
        ap.setAlternativeUserID(toUpper(attrs.getValue("AlternativeUserID")));
        ap.setUserName(toUpper(attrs.getValue("UserName")));
        ap.setRequestor(!isFalse(attrs.getValue("UserIsRequestor")));
        ap.setNetworkAccessPointID(toUpper(attrs.getValue("NetworkAccessPointID")));
        ap.setNetworkAccessPointType(parseInt(attrs
                .getValue("NetworkAccessPointTypeCode")));
        Collection<ActiveParticipant> c = rec.getActiveParticipant();
        if (c == null) {
            c = new ArrayList<ActiveParticipant>(3);
            rec.setActiveParticipant(c);
        }
        c.add(ap);
    }

    private void roleIDCode(Attributes attrs) {
        Collection<Code> c = ap.getRoleIDCode();
        if (c == null) {
            c = new ArrayList<Code>(3);
            ap.setRoleIDCode(c);
        }
        c.add(toCode(attrs));
    }

    private void activeParticipant() {
        ap = null;
    }

    private void auditSourceIdentification(Attributes attrs) {
        as = new AuditSource();
        as.setAuditRecord(rec);
        as.setEnterpriseSiteID(toUpper(attrs.getValue("AuditEnterpriseSiteID")));
        as.setSourceID(toUpper(attrs.getValue("AuditSourceID")));
        Collection<AuditSource> c = rec.getAuditSource();
        if (c == null) {
            c = new ArrayList<AuditSource>(3);
            rec.setAuditSource(c);
        }
        c.add(as);
    }

    private void auditSourceTypeCode(Attributes attrs) {
	int code = parseInt(attrs.getValue("code"));
	switch (++asTypeCount) {
	case 1:
	    as.setSourceTypeCode(code);
	    break;
	case 2:
	    as.setSourceTypeCode2(code);
	    break;
	case 3:
	    as.setSourceTypeCode3(code);
	    break;
	}
    }

    private void auditSourceIdentification() {
        as = null;
        asTypeCount = 0;
    }

    private void participantObjectIdentification(Attributes attrs) {
        po = new ParticipantObject();
        po.setAuditRecord(rec);
        po.setParticipantObjectID(toUpper(attrs.getValue("ParticipantObjectID")));
        po.setParticipantObjectTypeCode(parseInt(attrs
                .getValue("ParticipantObjectTypeCode")));
        po.setParticipantObjectTypeCodeRole(parseInt(attrs
                .getValue("ParticipantObjectTypeCodeRole")));
        po.setParticipantObjectDataLifeCycle(parseInt(attrs
                .getValue("ParticipantObjectDataLifeCycle")));
        po.setParticipantObjectSensitivity(toUpper(attrs
                .getValue("ParticipantObjectSensitivity")));
        po.setParticipantObjectName(toUpper(attrs.getValue("ParticipantObjectName")));
        Collection<ParticipantObject> c = rec.getParticipantObject();
        if (c == null) {
            c = new ArrayList<ParticipantObject>(3);
            rec.setParticipantObject(c);
        }
        c.add(po);
    }

    private void participantObjectIDTypeCode(Attributes attrs) {
        Code code = toCode(attrs);
        if (code != null) {
            po.setParticipantObjectIDTypeCode(code);
        } else {
            po.setParticipantObjectIDTypeCodeRFC(parseInt(attrs
                    .getValue("code")));
        }
    }

    private void participantObjectName(Attributes attrs) {
        append = true;

    }

    private void participantObjectName() {
        po.setParticipantObjectName(toUpper(sb.toString()));
        sb.setLength(0);
        append = false;
    }

    private void participantObjectIdentification() {
        po = null;
    }

    private Code toCode(Attributes attrs) {
        String value = attrs.getValue("code");
        String designator = attrs.getValue("codeSystemName");
        if (value == null || designator == null) {
            return null;
        }
        String meaning = attrs.getValue("displayName");
        List queryResult = em.createQuery(
                "FROM Code c WHERE c.value = :value AND c.designator = :designator")
                .setParameter("value", value)
                .setParameter("designator", designator)
                .setHint("org.hibernate.readOnly", Boolean.TRUE)
                .getResultList();
        if (!queryResult.isEmpty()) {
            return (Code) queryResult.get(0);
        }
        Code code = new Code();
        code.setValue(value);
        code.setDesignator(designator);
        code.setMeaning(meaning);
        em.persist(code);
        return code;
    }

    private String toUpper(String s) {
	return s != null ? s.toUpperCase() : null;
    }

    private int parseInt(String s) {
        return s != null ? Integer.parseInt(s) : 0;
    }

    private boolean isFalse(String s) {
        return "false".equalsIgnoreCase(s);
    }

    private Date parseISO8601DateTime(String s) {
        int tzindex = indexOfTimeZone(s);
        Calendar cal;
        if (tzindex == -1) {
            cal = Calendar.getInstance();
        } else {
            cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            cal.set(Calendar.ZONE_OFFSET, timeZoneOffset(s, tzindex));
            s = s.substring(0, tzindex);
        }
        int pos = 0;
        cal.set(Calendar.YEAR, Integer.parseInt(s.substring(pos, 4)));
        if (!isDigit(s.charAt(pos += 4))) {
            ++pos;
        }
        cal.set(Calendar.MONTH, 
                Integer.parseInt(s.substring(pos, pos + 2)) - 1);
        if (!isDigit(s.charAt(pos += 2))) {
            ++pos;
        }
        cal.set(Calendar.DAY_OF_MONTH, 
                Integer.parseInt(s.substring(pos, pos + 2)));
        if (!isDigit(s.charAt(pos += 2))) {
            ++pos;
        }
        cal.set(Calendar.HOUR_OF_DAY,
                Integer.parseInt(s.substring(pos, pos + 2)));
        if (!isDigit(s.charAt(pos += 2))) {
            ++pos;
        }
        cal.set(Calendar.MINUTE, Integer.parseInt(s.substring(pos, pos + 2)));
        int sec = 0;
        int ms = 0;
        if ((pos += 2) < s.length()) {
            if (!isDigit(s.charAt(pos))) {
                ++pos;
            }
            float f = Float.parseFloat(s.substring(pos));
            sec = (int) f;
            ms = (int) ((f - sec) * 1000);
        }
        cal.set(Calendar.SECOND, sec);
        cal.set(Calendar.MILLISECOND, ms);
        return cal.getTime();
    }

    private int indexOfTimeZone(String s) {
        int len = s.length();
        int index = len - 1;
        char c = s.charAt(index);
        if (c == 'Z') {
            return index;
        }
        index = len - 6;
        c = s.charAt(index);
        if (c == '-' || c == '+') {
            return index;
        }
        index = len - 3;
        c = s.charAt(index);
        if (c == '-' || c == '+') {
            return index;
        }
        return -1;
    }

    private int timeZoneOffset(String s, int tzindex) {
        char c = s.charAt(tzindex);
        if (c == 'Z') {
            return 0;
        }
        int off = Integer.parseInt(s.substring(tzindex + 1, tzindex + 3)) * 3600000;
        if (tzindex + 6 == s.length()) {
            off += Integer.parseInt(s.substring(tzindex + 4)) * 60000;
        }
        return c == '-' ? -off : off;
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }
}
