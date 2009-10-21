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

package org.dcm4chee.web.wicket.ae;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow.WindowClosedCallback;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.list.PropertyListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.ResourceModel;
import org.dcm4chee.archive.entity.AE;
import org.dcm4chee.web.wicket.common.ComponentUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Jan 5, 2009
 */
public class AEListPanel extends Panel {

    private AEMgtPage page;
    
    private static Logger log = LoggerFactory.getLogger(AEListPanel.class);
    List<AE> aeList = AEMgtDelegate.getInstance().getAEList();
    
    DicomEchoPanel echoPanel;
    @SuppressWarnings("serial")
    public AEListPanel(String id, AEMgtPage p) {
        super(id);
        setOutputMarkupId(true);
        final ModalWindow mw = new ModalWindow("echoPanel");
        mw.setCssClassName(ModalWindow.CSS_CLASS_GRAY);
        mw.setTitle(new ResourceModel("aet.echoPanelTitle"));
        mw.setWindowClosedCallback(new WindowClosedCallback(){
            public void onClose(AjaxRequestTarget target) {
                AEMgtDelegate.getInstance().updateAEList();
                target.addComponent(AEListPanel.this);
            }});
        echoPanel = new DicomEchoPanel(mw, true);
        mw.setContent( echoPanel );
        add(mw);
        ComponentUtil util = new ComponentUtil(AEMgtPage.getModuleName());
        util.addLabel(this, "titleHdr");
        util.addLabel(this, "hostHdr");
        util.addLabel(this, "portHdr");
        util.addLabel(this, "cipherHdr");
        util.addLabel(this, "descriptionHdr");
        util.addLabel(this, "issuerHdr");
        util.addLabel(this, "fsgrpHdr");
        util.addLabel(this, "wadoHdr");
        util.addLabel(this, "userHdr");
        util.addLabel(this, "stationHdr");
        util.addLabel(this, "institutionHdr");
        util.addLabel(this, "departmentHdr");
        util.addLabel(this, "installedHdr");
        page = p;
        add(new PropertyListView("list", aeList ) {

            @Override
            protected void populateItem(final ListItem item) {
                item.add(new Label("title"));
                item.add(new Label("hostName"));
                item.add(new Label("port"));
                item.add(new ListView("cipherSuites", ((AE) item.getModelObject()).getCipherSuites()) {
                    @Override
                    protected void populateItem(final ListItem item1) {
                        item1.add(new Label("ciphersuite", item1.getModel()));
                    }
                });
                item.add(new Label("description"));
                item.add(new Label("issuerOfPatientID"));
                item.add(new Label("fileSystemGroupID"));
                item.add(new Label("wadoURL"));
                item.add(new Label("userID"));
                item.add(new Label("stationName"));
                item.add(new Label("institution"));
                item.add(new Label("department"));
                item.add(new Label("installed"));
                item.add(new Link("editAET") {
                    
                    @Override
                    public void onClick() {
                        page.setEditPage( (AE)item.getModelObject());
                    }
                });
                item.add(new Link("removeAET") {
                    
                    @Override
                    public void onClick() {
                        AEMgtDelegate.getInstance().removeAET((AE)item.getModelObject());
                    }
                });
                item.add(new AjaxLink("echo") {
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        echoPanel.setAE((AE) item.getModelObject());
                        mw.show(target);
                    }});
            }
            
        });
        add(new Link("newAET") {
            
            @Override
            public void onClick() {
                page.setEditPage(new AE());
            }
        });

    }
}
