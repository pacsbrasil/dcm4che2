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
 * Agfa-Gevaert AG.
 * Portions created by the Initial Developer are Copyright (C) 2008
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

package org.dcm4chee.dashboard.web;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;

import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.wicket.PageParameters;
import org.apache.wicket.extensions.ajax.markup.html.tabs.AjaxTabbedPanel;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.StringResourceModel;
import org.dcm4chee.dashboard.web.common.InternalErrorPage;
import org.jboss.mx.util.MBeanServerLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Robert David <robert.david@agfa.com>
 * @version $Revision$ $Date$
 * @since 18.11.2009
 */
public class DashboardMainPage extends WebPage {
    
    private static final long serialVersionUID = 1L;
    
    private static Logger log = LoggerFactory.getLogger(DashboardMainPage.class);
    
    public DashboardMainPage(final PageParameters parameters) {
        try {
            this.add(new AjaxTabbedPanel("tabs", new ArrayList<ITab>(Arrays.asList(
                new AbstractTab(new StringResourceModel("dashboard.tabs.tab1.name", this, null)) {
                
                    private static final long serialVersionUID = 1L;
                
                    public Panel getPanel(String panelId) {
                        return new FileSystemPanel(panelId);
                    }
                },  
                new AbstractTab(new StringResourceModel("dashboard.tabs.tab2.name", this, null)) {
                    
                    private static final long serialVersionUID = 1L;
                
                    public Panel getPanel(String panelId) {
                        return new ReportPanel(panelId);
                    }
                },
                new AbstractTab(new StringResourceModel("dashboard.tabs.tab3.name", this, null)) {
                    
                    private static final long serialVersionUID = 1L;
                
                    public Panel getPanel(String panelId) {
                        return new SystemInfoPanel(panelId);
                    }
                })))
            );
            ((AjaxTabbedPanel) this.get(0)).setSelectedTab(((parameters != null) && parameters.containsKey("tab")) ? parameters.getInt("tab") : 0);
        } catch (Exception e) {
            log.error(this.getClass().toString() + ": " + "init: " + e.getMessage());
            log.debug("Exception: ", e);
            this.redirectToInterceptPage(new InternalErrorPage());
        }
    }
    
    protected static ResultSet queryDatabase(String query) {

        Context jndiCtx = null;
        try {
            jndiCtx = new InitialContext();
            return ((DataSource) 
                    jndiCtx.lookup((String) MBeanServerLocator.locate().getAttribute(
                            new ObjectName("org.dcm4chee.dashboard.mbean:service=DashboardService"),
                            "dataSourceName")
                    )).getConnection()
                    .createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY)
                    .executeQuery(query);
        } catch (Exception e) {
        } finally {
            try {
                jndiCtx.close();
            } catch (NamingException ignore) {
            }
        }
        return null;
    }
}
