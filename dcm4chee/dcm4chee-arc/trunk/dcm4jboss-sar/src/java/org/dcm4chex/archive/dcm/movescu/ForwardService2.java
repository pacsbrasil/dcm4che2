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
 * Agfa-Gevaert Group.
 * Portions created by the Initial Developer are Copyright (C) 2003-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See @authors listed below.
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

package org.dcm4chex.archive.dcm.movescu;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Hashtable;

import javax.management.Notification;
import javax.management.NotificationFilterSupport;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamSource;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4chex.archive.common.SeriesStored;
import org.dcm4chex.archive.config.DicomPriority;
import org.dcm4chex.archive.config.ForwardingRules;
import org.dcm4chex.archive.config.RetryIntervalls;
import org.dcm4chex.archive.util.FileUtils;
import org.jboss.system.ServiceMBeanSupport;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Apr 17, 2007
 */
public class ForwardService2 extends ServiceMBeanSupport {

    private static final String FORWARD_XSL = "forward.xsl";

    private static final NotificationFilterSupport seriesStoredFilter = 
            new NotificationFilterSupport();
    static {
        seriesStoredFilter.enableType(SeriesStored.class.getName());
    }
    
    private final NotificationListener seriesStoredListener = new NotificationListener() {
        public void handleNotification(Notification notif, Object handback) {
            ForwardService2.this.onSeriesStored((SeriesStored) notif.getUserData());
        }
    };

    private ObjectName storeScpServiceName;

    private ObjectName moveScuServiceName;
    
    private Hashtable templates = new Hashtable();
    
    private File configDir;
       
    public final String getConfigDir() {
        return configDir.getPath();
    }

    public final void setConfigDir(String path) {
        this.configDir = new File(path.replace('/', File.separatorChar));
    }

    public final ObjectName getMoveScuServiceName() {
        return moveScuServiceName;
    }

    public final void setMoveScuServiceName(ObjectName moveScuServiceName) {
        this.moveScuServiceName = moveScuServiceName;
    }

    public final ObjectName getStoreScpServiceName() {
        return storeScpServiceName;
    }

    public final void setStoreScpServiceName(ObjectName storeScpServiceName) {
        this.storeScpServiceName = storeScpServiceName;
    }

    
    protected void startService() throws Exception {
        server.addNotificationListener(storeScpServiceName,
                seriesStoredListener, seriesStoredFilter, null);
    }

    protected void stopService() throws Exception {
        templates.clear();
        server.removeNotificationListener(storeScpServiceName,
                seriesStoredListener, seriesStoredFilter, null);
    }
    
    private void onSeriesStored(final SeriesStored stored) {
        Templates tpl = getTemplatesFor(stored.getCallingAET(), FORWARD_XSL);
        if (tpl != null) {
            Dataset ds = DcmObjectFactory.getInstance().newDataset();
            ds.putAll(stored.getPatientAttrs());
            ds.putAll(stored.getStudyAttrs());
            ds.putAll(stored.getSeriesAttrs());
            final Calendar cal = Calendar.getInstance();
            try {
                xslt(cal, ds, tpl, new DefaultHandler(){

                    public void startElement(String uri, String localName,
                            String qName, Attributes attrs) {
                        if (qName.equals("destination")) {
                            scheduleMove(stored.getRetrieveAET(),
                                    attrs.getValue("aet"),
                                    toPriority(attrs.getValue("priority")),
                                    null,
                                    stored.getStudyInstanceUID(),
                                    stored.getSeriesInstanceUID(),
                                    toScheduledTime(cal, attrs.getValue("delay")));
                        }
                    }});
            } catch (Exception e) {
                log.error("Applying forwarding rules to " + stored + " fails:", e);                
            }
        }
    }

    private static int toPriority(String s) {
        return s != null ? DicomPriority.toCode(s) : 0;
    }

    private long toScheduledTime(Calendar cal, String s) {
        if (s == null || s.length() == 0) {
            return 0;
        }
        int index = s.indexOf('!');
        if (index == -1) {
            return cal.getTimeInMillis() + RetryIntervalls.parseInterval(s); 
        }
        if (index != 0) {
            cal.setTimeInMillis(cal.getTimeInMillis()
                    + RetryIntervalls.parseInterval(s.substring(0, index)));
        }
        return ForwardingRules.afterBusinessHours(cal, s.substring(index+1));
    }
    
    private static void xslt(Calendar cal, Dataset ds, Templates tpl, ContentHandler ch)
            throws TransformerConfigurationException, IOException {
        SAXTransformerFactory tf = (SAXTransformerFactory)
            TransformerFactory.newInstance();
        TransformerHandler th = tf.newTransformerHandler(tpl);
        Transformer t = th.getTransformer();
        t.setParameter("year", new Integer(cal.get(Calendar.YEAR)));
        t.setParameter("month", new Integer(cal.get(Calendar.MONTH)+1));
        t.setParameter("date", new Integer(cal.get(Calendar.DAY_OF_MONTH)));
        t.setParameter("day", new Integer(cal.get(Calendar.DAY_OF_WEEK)-1));
        t.setParameter("hour", new Integer(cal.get(Calendar.HOUR_OF_DAY)));
        th.setResult(new SAXResult(ch));
        ds.writeDataset2(th, null, null, 64, null);
    }

    private void scheduleMove(String retrieveAET, String destAET, int priority,
            String pid, String studyIUID, String seriesIUID, long scheduledTime) {
        try {
            server.invoke(moveScuServiceName, "scheduleMove", new Object[] {
                    retrieveAET, destAET, new Integer(priority), pid,
                    studyIUID, seriesIUID, null, new Long(scheduledTime) },
                    new String[] { String.class.getName(),
                            String.class.getName(), int.class.getName(),
                            String.class.getName(), String.class.getName(),
                            String.class.getName(), String[].class.getName(),
                            long.class.getName() });
        } catch (Exception e) {
            log.error("Schedule Move failed:", e);
        }
    }

    private Templates getTemplatesFor(String aet, String fname) {
        File f = getXSLFile(aet, fname);
        if (f == null) {
            return null;
        }
        Templates tpl = (Templates) templates.get(f);
        if (tpl == null) {
            try {
                tpl = TransformerFactory.newInstance().newTemplates(
                        new StreamSource(f));
            } catch (Exception e) {
                log.error("Compiling Stylesheet " + f + " failed:", e);
                return null;
            }
            templates.put(f, tpl);
        }
        return tpl;
    }

    private File getXSLFile(String aet, String fname) {
        if (aet != null) {
            File f =  FileUtils.resolve(
                        new File(new File(configDir, aet), fname));
            if (f.exists()) {
                return f;
            }
        }
        File f = FileUtils.resolve(new File(configDir, fname));
        return f.exists() ? f : null;
    }
    
    public void reloadStylesheets() {
        templates.clear();
    }
}
