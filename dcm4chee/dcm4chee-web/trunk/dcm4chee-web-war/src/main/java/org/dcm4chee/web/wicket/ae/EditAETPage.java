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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.list.PropertyListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.validation.validator.NumberValidator;
import org.apache.wicket.validation.validator.UrlValidator;
import org.dcm4chee.archive.entity.AE;
import org.dcm4chee.web.dao.AEHomeLocal;
import org.dcm4chee.web.wicket.common.LocaleSelectorLink;
import org.dcm4chee.web.wicket.util.JNDIUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Franz Willer <franz.willer@gmail.com>
 * @version $Revision$ $Date$
 * @since June 4, 2009
 */

public class EditAETPage extends WebPage {
    
    private static Logger log = LoggerFactory.getLogger(EditAETPage.class);
    private AEHomeLocal aeHome = (AEHomeLocal) JNDIUtils.lookup(AEHomeLocal.JNDI_NAME);
    private static List<String> availableCiphersuites = new ArrayList<String>();
    static {
        availableCiphersuites.clear();
        availableCiphersuites.add("-");
        availableCiphersuites.add("SSL_RSA_WITH_NULL_SHA");
        availableCiphersuites.add("TLS_RSA_WITH_AES_128_CBC_SHA");
        availableCiphersuites.add("SSL_RSA_WITH_3DES_EDE_CBC_SHA");
    }
    
    public EditAETPage( final AEListPage page, final AE ae) {
        add(new LocaleSelectorLink("lang_en","en"));
        add(new LocaleSelectorLink("lang_de","de"));
        add(new LocaleSelectorLink("lang_fr","fr"));
        add(new FeedbackPanel("feedback"));
        Form form = new Form("form");
        add(form);
        CompoundPropertyModel model = new CompoundPropertyModel(ae);
        setModel(model);
        form.add(new Label("titleDesc", new ResourceModel("aet.title") ) );
        form.add(new Label("hostDesc", new ResourceModel("aet.host") ) );
        form.add(new Label("portDesc", new ResourceModel("aet.port") ) );
        form.add(new Label("descDesc", new ResourceModel("aet.description") ) );
        form.add(new Label("issuerDesc", new ResourceModel("aet.issuer") ) );
        form.add(new Label("fsGroupDesc", new ResourceModel("aet.fsGroup") ) );
        form.add(new Label("wadourlDesc", new ResourceModel("aet.wadourl") ) );
        form.add(new Label("userIdDesc", new ResourceModel("aet.userid") ) );
        form.add(new Label("passwdDesc", new ResourceModel("aet.passwd") ) );
        
        form.add(new TextField("title").setRequired(true)); 
        form.add(new TextField("hostName").setRequired(true)); 
        form.add(new TextField("port").add(NumberValidator.range(0,32000)));
        final CiphersuitesModel cipherModel = new CiphersuitesModel(ae,3,6);
        form.add(new PropertyListView("cipherSuites", cipherModel.getCiphers()) {
            @Override
            protected void populateItem(final ListItem item) {
                item.add(new Label("ciphersDesc", new StringResourceModel("aet.ciphers", EditAETPage.this, null, new Object[]{item.getIndex()+1} ) ) );
                item.add(new DropDownChoice("ciphersuite", cipherModel.getModel(item.getIndex()), availableCiphersuites));
            }
        }); 
        form.add(new TextField("description")); 
        form.add(new TextField("issuerOfPatientID")); 
        form.add(new TextField("fileSystemGroupID"));
        form.add(new TextField("wadoURL").add(new UrlValidator())); 
        form.add(new TextField("userID")); 
        form.add(new PasswordTextField("password").setRequired(false)); 
        form.add(new Button("submit") {

            @Override
            public void onSubmit() {
                submit();
                page.update();
                setResponsePage(page);
            }
        });
        form.add(new Link("cancel") {

            @Override
            public void onClick() {
                setResponsePage(page);
            }});
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
                log.info("create new CipherSuite item!:"+s);
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
