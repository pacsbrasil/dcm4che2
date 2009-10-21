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

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow.WindowClosedCallback;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.validator.NumberValidator;
import org.apache.wicket.validation.validator.StringValidator;
import org.dcm4chee.archive.entity.AE;
import org.dcm4chee.web.wicket.common.ComponentUtil;
import org.dcm4chee.web.wicket.common.UrlValidator1;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Franz Willer <franz.willer@gmail.com>
 * @version $Revision$ $Date$
 * @since June 4, 2009
 */

public class EditAETPanel extends Panel {
    transient AEMgtDelegate delegate = AEMgtDelegate.getInstance();
    private static Logger log = LoggerFactory.getLogger(EditAETPanel.class);
    
    public EditAETPanel( String id, final AEMgtPage page, final AE ae) {
        super(id);
        setOutputMarkupId(true);
        Form form = new Form("form");
        add(form);
        CompoundPropertyModel model = new CompoundPropertyModel(ae);
        setDefaultModel(model);
        ComponentUtil util = new ComponentUtil(AEMgtPage.getModuleName());
        
        util.addLabeledTextField(form, "title").add(new AETitleValidator()).setRequired(true); 
        util.addLabeledTextField(form, "hostName","host")
            .add(StringValidator.minimumLength(1)).setRequired(true); 
        util.addLabeledTextField(form, "port").add(NumberValidator.range(1,65535));
        form.add(new Label("ciphersLabel1", new StringResourceModel("aet.ciphers", EditAETPanel.this, null, new Object[]{1} ) ) );
        form.add(new DropDownChoice("ciphersuite1", new CipherModel(ae, 0), AEMgtDelegate.AVAILABLE_CIPHERSUITES));
        form.add(new Label("ciphersLabel2", new StringResourceModel("aet.ciphers", EditAETPanel.this, null, new Object[]{2} ) ) );
        form.add(new DropDownChoice("ciphersuite2", new CipherModel(ae, 1), AEMgtDelegate.AVAILABLE_CIPHERSUITES));
        form.add(new Label("ciphersLabel3", new StringResourceModel("aet.ciphers", EditAETPanel.this, null, new Object[]{3} ) ) );
        form.add(new DropDownChoice("ciphersuite3", new CipherModel(ae, 2), AEMgtDelegate.AVAILABLE_CIPHERSUITES));
        util.addLabeledTextField(form, "description"); 
        util.addLabeledTextField(form, "issuerOfPatientID", "issuer"); 
        util.addLabeledDropDownChoice(form, "fileSystemGroupID", null, 
                delegate.getFSGroupIDs()).setNullValid(true);
        util.addLabeledTextField(form, "wadoURL").add(new UrlValidator1()); //Wicket UrlValidator doesn't accept http://hostname:8080/web!
        util.addLabeledTextField(form, "userID"); 
        form.add(new Label("passwdLabel", new ResourceModel("aet.passwd") ) );
        form.add(new PasswordTextField("password").setRequired(false)); 
        util.addLabeledTextField(form, "stationName"); 
        util.addLabeledTextField(form, "institution"); 
        util.addLabeledTextField(form, "department"); 
        form.add(new Label("installedLabel", new ResourceModel("aet.installed") ) );
        form.add(new AjaxCheckBox("installed"){

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
            }
            
        }); 
        form.add(new Button("submit") {

            @Override
            public void onSubmit() {
                if (submit() ) {
                    AEMgtDelegate.getInstance().updateAEList();
                    page.setListPage();
                }
            }
        });
        form.add(new Link("cancel") {

            @Override
            public void onClick() {
                page.setListPage();
            }});
        final ModalWindow mw = new ModalWindow("echoPanel");
        mw.setTitle(new ResourceModel("aet.echoPanelTitle"));
        mw.setCssClassName(ModalWindow.CSS_CLASS_GRAY);
        mw.setContent(new DicomEchoPanel(ae,mw,true));
        mw.setWindowClosedCallback(new WindowClosedCallback(){
            public void onClose(AjaxRequestTarget target) {
                log.info("#### EchoPanel closed!");
                AEMgtDelegate.getInstance().updateAEList();
                target.addComponent(EditAETPanel.this);
                log.info("#### ae:"+ae);
            }});
        
        add(mw);
        form.add(new AjaxButton("echo") {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form form) {
                mw.show(target);
            }});

    }
    
    private TextField newTextField(String id, IValidator v) {
        TextField tf = new TextField(id);
        tf.add( new AttributeModifier("title", true, 
                new ResourceModel("ae."+id+".descr")));
        if ( v != null ) tf.add(v);
        return tf;
    }
    
    private boolean submit() {
        try {
            AE ae = (AE) getDefaultModelObject();
            delegate.update(ae);
            return true;
        } catch ( Exception x ) {
            error((String)new ResourceModel("aet.titleAlreadyExist").wrapOnAssignment(this).getObject());
            return false;
        }
    }
      
 }
