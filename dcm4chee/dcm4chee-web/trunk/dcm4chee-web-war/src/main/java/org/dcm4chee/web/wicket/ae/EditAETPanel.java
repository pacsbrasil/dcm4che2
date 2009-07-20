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

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PropertyListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.validator.NumberValidator;
import org.apache.wicket.validation.validator.StringValidator;
import org.dcm4chee.archive.entity.AE;
import org.dcm4chee.web.dao.AEHomeLocal;
import org.dcm4chee.web.dao.FileSystemHomeLocal;
import org.dcm4chee.web.wicket.common.ComponentUtil;
import org.dcm4chee.web.wicket.common.UrlValidator1;
import org.dcm4chee.web.wicket.util.JNDIUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Franz Willer <franz.willer@gmail.com>
 * @version $Revision$ $Date$
 * @since June 4, 2009
 */

public class EditAETPanel extends Panel {
    URL url;
    private static Logger log = LoggerFactory.getLogger(EditAETPanel.class);
    private AEHomeLocal aeHome = (AEHomeLocal) JNDIUtils.lookup(AEHomeLocal.JNDI_NAME);
    private FileSystemHomeLocal fsHome = (FileSystemHomeLocal) JNDIUtils.lookup(FileSystemHomeLocal.JNDI_NAME);
    private List<String> fsGroups = new ArrayList<String>();
    private static List<String> availableCiphersuites = new ArrayList<String>();
    static {
        availableCiphersuites.clear();
        availableCiphersuites.add("-");
        availableCiphersuites.add("SSL_RSA_WITH_NULL_SHA");
        availableCiphersuites.add("TLS_RSA_WITH_AES_128_CBC_SHA");
        availableCiphersuites.add("SSL_RSA_WITH_3DES_EDE_CBC_SHA");
    }
    
    public EditAETPanel( String id, final AEMgtPage page, final AE ae) {
        super(id);
        initFSGroups();
        Form form = new Form("form");
        add(form);
        CompoundPropertyModel model = new CompoundPropertyModel(ae);
        setModel(model);
        ComponentUtil util = new ComponentUtil(AEMgtPage.getModuleName());
        
        util.addLabeledTextField(form, "title").add(new AETitleValidator()).setRequired(true); 
        util.addLabeledTextField(form, "hostName","host")
            .add(StringValidator.minimumLength(1)).setRequired(true); 
        util.addLabeledTextField(form, "port").add(NumberValidator.range(1,65535));
        final CiphersuitesModel cipherModel = new CiphersuitesModel(ae);
        form.add(new PropertyListView("cipherSuites", cipherModel.getCiphers()) {
            @Override
            protected void populateItem(final ListItem item) {
                item.add(new Label("ciphersLabel", new StringResourceModel("aet.ciphers", EditAETPanel.this, null, new Object[]{item.getIndex()+1} ) ) );
                item.add(new DropDownChoice("ciphersuite", cipherModel.getModel(item.getIndex()), availableCiphersuites));
            }
        }); 
        util.addLabeledTextField(form, "description"); 
        util.addLabeledTextField(form, "issuerOfPatientID", "issuer"); 
        util.addLabeledDropDownChoice(form, "fileSystemGroupID", null, fsGroups).setNullValid(true);
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
                submit();
                page.update();
                page.setListPage();
            }
        });
        form.add(new Link("cancel") {

            @Override
            public void onClick() {
                page.setListPage();
            }});
    }
    
    private void initFSGroups() {
        fsGroups.clear();
        fsGroups.addAll(fsHome.listGroupIDs());
    }

    private TextField newTextField(String id, IValidator v) {
        TextField tf = new TextField(id);
        tf.add( new AttributeModifier("title", true, 
                new ResourceModel("ae."+id+".descr")));
        if ( v != null ) tf.add(v);
        return tf;
    }
    
    private void submit() {
        AE ae = (AE) getModelObject();
        if ( ae.getPk() == -1) {
            aeHome.createAET(ae);
        } else {
            aeHome.updateAET(ae);
        }
    }
    
    
    private class CiphersuitesModel implements Serializable {
        private AE ae;
        private List ciphers;
        private int minSuites, maxSuites;
        
        public CiphersuitesModel(AE ae) {
            this(ae, 3, 3);
        }
        public CiphersuitesModel(AE ae, int min, int max) {
            this.ae = ae;
            minSuites = min;
            this.maxSuites = max;
            init();
        }
        
        private void init() {
            ciphers = ae.getCipherSuites();
            if ( ciphers == null )
                ciphers = new ArrayList<String>();
            if ( ciphers.size() < minSuites ) {
                while ( ciphers.size() < minSuites ) {
                    ciphers.add(null);
                }
            } else if ( ciphers.size() < maxSuites ) {
                ciphers.add(null);
            }
        }
        
        public CipherModel getModel(int idx) {
            return new CipherModel(this, idx);
        }

        public List getCiphers() {
            return ciphers;
        }

        public void store() {
            ae.setCipherSuites(ciphers);
        }
    }
    private class CipherModel implements IModel {
        private CiphersuitesModel ciphers;
        private int idx;
        
        public CipherModel( CiphersuitesModel m, int idx ) {
            this.ciphers = m;
            this.idx = idx;
        }
        
        public Object getObject() {
            List<String> l = ciphers.getCiphers();
            if ( l != null & l.size() > idx ) {
                return l.get(idx);
            }
            return null;
        }

        public void setObject(Object o) {
            String s = (String)o;
            if ( "-".equals(s) || "".equals(s)) s = null;
            List<String> l = ciphers.getCiphers();
            if ( l != null && l.size() > idx ) {
                l.set(idx, s);
            } else if ( s != null && l.size() == idx ) {
                log.debug("create new CipherSuite item!:"+s);
                l.add(s);
            } else {
                log.warn("CipherModel has illegal index ("+idx+", l:"+l.size()+")! set will be ignored!");
            }
            ciphers.store();
        }

        public void detach() {
        }
        
    }
}
