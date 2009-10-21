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

import java.util.Iterator;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.HeaderContributor;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.validation.validator.RangeValidator;
import org.apache.wicket.validation.validator.StringValidator;
import org.dcm4chee.archive.entity.AE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Franz Willer <franz.willer@gmail.com>
 * @version $Revision$ $Date$
 * @since Aug 18, 2009
 */
public class DicomEchoPanel extends Panel {
    
    private ModalWindow mw;
    private boolean echoOnShow;
    private AE aeOri;
    private final AE aeEcho = new AE();
    private Integer nrOfTests = 1;
    private String result;
    
    private static Logger log = LoggerFactory.getLogger(DicomEchoPanel.class);

    private IModel nrOfTestsModel = new IModel() {
        public Object getObject() {
            return nrOfTests;
        }
        public void setObject(Object object) {
            nrOfTests = (Integer)object;
        }
        public void detach() {}
    };
    
    private Label resultLabel = new Label("result", new AbstractReadOnlyModel() {
        @Override
        public Object getObject() {
            return result;
        }}) {
        @Override
        public void onComponentTag(ComponentTag tag) {
            tag.getAttributes().put("class", result.indexOf("success") != -1 ? 
                    "ae_echo_succeed" : "ae_echo_failed");
            super.onComponentTag(tag);
        }
    };
        
    private AjaxButton saveBtn = new SaveButton("save");
    
    public DicomEchoPanel(AE ae, final ModalWindow mw, boolean echoOnShow) {
        this(mw,echoOnShow);
        setAE(ae);
    }
    public DicomEchoPanel(final ModalWindow mw, boolean echoOnShow) {
        super("content");
        this.mw = mw;
        this.echoOnShow = echoOnShow;
        Form form = new Form("form");
        add(HeaderContributor.forCss(DicomEchoPanel.class, "style.css"));
        add(form);
        CompoundPropertyModel model = new CompoundPropertyModel(aeEcho);
        setDefaultModel(model);
        form.add(new Label("aetLabel", new ResourceModel("aet.echoAETitle")));
        form.add(new Label("ciphersLabel", new ResourceModel("aet.echoCiphers")));
        form.add(new Label("nrOfTestsLabel", new ResourceModel("aet.echoNrOfTests")));
        form.add(new Label("echoResultLabel", new ResourceModel("aet.echoResult")));
        form.add(new TextField("title").add(new AETitleValidator()).setRequired(true)); 
        form.add(new TextField("hostName").add(StringValidator.minimumLength(1)).setRequired(true)); 
        form.add( new TextField("port").add(new RangeValidator(1,65535)));
        form.add(new DropDownChoice("ciphersuite1", new CipherModel(aeEcho, 0), AEMgtDelegate.AVAILABLE_CIPHERSUITES));
        form.add(new DropDownChoice("ciphersuite2", new CipherModel(aeEcho, 1), AEMgtDelegate.AVAILABLE_CIPHERSUITES));
        form.add(new DropDownChoice("ciphersuite3", new CipherModel(aeEcho, 2), AEMgtDelegate.AVAILABLE_CIPHERSUITES));
        form.add( new TextField("nrOfTests", nrOfTestsModel, Integer.class).add(new RangeValidator(1,2000)));
        resultLabel.setOutputMarkupId(true).setEnabled(false);
        form.add(resultLabel);
        form.add(new AjaxButton("cancel", new ResourceModel("aet.cancelButton"))
        {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form form) {
                mw.close(target);
            }
        });
        saveBtn.setEnabled(false);
        form.add(saveBtn);
        form.add(new EchoButton("echo"));
        add(new FeedbackPanel("feedback").setOutputMarkupId(true));
    }

    public void setAE(final AE ae) {
        aeOri = ae;
        copyNetCfg(aeOri, aeEcho);
    }
    
    private AE copyNetCfg(final AE aeSrc, final AE aeDest) {
        aeDest.setTitle(aeSrc.getTitle());
        aeDest.setHostName(aeSrc.getHostName());
        aeDest.setPort(aeSrc.getPort());
        aeDest.setCipherSuites(aeSrc.getCipherSuites());
        return aeDest;
    }
    private boolean isSameNetCfg(final AE aeSrc, final AE aeDest) {
        if ( aeDest.getTitle().equals(aeSrc.getTitle()) &&
                aeDest.getHostName().equals(aeSrc.getHostName()) &&
                aeDest.getPort()== aeSrc.getPort() ) {
            List<String> c1 = aeSrc.getCipherSuites();
            List<String> c2 = aeDest.getCipherSuites();
            if ( c1 == null ) {
                return c2 == null;
            } else if ( c2 != null ) {
                if ( c1.size() == c2.size() ) {
                    Iterator<String> it1 = c1.iterator();
                    Iterator<String> it2 = c2.iterator();
                    while (it1.hasNext()) {
                        if ( !it1.next().equals(it2.next()) ) {
                            return false;
                        }
                    }
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void onBeforeRender() {
        if (echoOnShow) {
            doEcho(aeOri);
        }
        super.onBeforeRender();
    }
    
    public void doEcho(AE ae) {
        result = new EchoDelegate().echo(ae, nrOfTests);
    }

    class EchoButton extends AjaxButton {
        private EchoButton(String id) {
            super(id);
        }
        @Override
        protected void onSubmit(AjaxRequestTarget target, Form form) {
            doEcho(aeEcho);
            boolean chgd = !isSameNetCfg( aeOri, aeEcho);
            if ( chgd != saveBtn.isEnabled()) {
                saveBtn.setEnabled(chgd);
                target.addComponent(saveBtn);
            }
            target.addComponent(resultLabel);
            target.addComponent(DicomEchoPanel.this.get("feedback"));
        }
        @Override
        protected void onError(AjaxRequestTarget target, Form form) {
            target.addComponent(DicomEchoPanel.this.get("feedback"));
        }
    }

    class SaveButton extends AjaxButton {
        private SaveButton(String id) {
            super(id, new ResourceModel("aet.saveButton"));
        }
        @Override
        protected void onSubmit(AjaxRequestTarget target, Form form) {
            try {
                AEMgtDelegate.getInstance().update(copyNetCfg(aeEcho, aeOri));
                saveBtn.setEnabled(false);
                mw.close(target);
            } catch (Exception x) {
                error((String)new ResourceModel("aet.titleAlreadyExist").wrapOnAssignment(this).getObject());
                target.addComponent(saveBtn);
                target.addComponent(DicomEchoPanel.this.get("feedback"));
            }
        }
    }
}
