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

package org.dcm4chee.usr.ui.usermanagement.role;

import java.util.List;

import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.authentication.AuthenticatedWebApplication;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.resources.CompressedResourceReference;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.util.ListModel;
import org.dcm4chee.usr.dao.Role;
import org.dcm4chee.usr.dao.UserAccess;
import org.dcm4chee.usr.ui.validator.RoleValidator;
import org.dcm4chee.usr.util.JNDIUtils;
import org.dcm4chee.web.common.base.BaseWicketPage;
import org.dcm4chee.web.common.markup.BaseForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Robert David <robert.david@agfa.com>
 * @version $Revision$ $Date$
 * @since 21.07.2010
 */
public class AddRolePage extends WebPage {
    
    private static final long serialVersionUID = 1L;
    
    private static Logger log = LoggerFactory.getLogger(AddRolePage.class);
    private static final ResourceReference BaseCSS = new CompressedResourceReference(BaseWicketPage.class, "base-style.css");

    protected ModalWindow window;
   
    public AddRolePage(final ModalWindow window, ListModel<Role> allRolenames) {
        super();
        
        if (AddRolePage.BaseCSS != null)
            add(CSSPackageResource.getHeaderContribution(AddRolePage.BaseCSS));

        this.window = window;
        
        add(new Label("page-title", new ResourceModel("rolelist.add-role-form.title")));
        add(new AddRoleForm("add-role-form", allRolenames));        
    }

    private final class AddRoleForm extends BaseForm {
        
        private static final long serialVersionUID = 1L;

        String serviceObjectName;
        
        private Model<String> newRolename = new Model<String>();
        private TextField<String> rolenameTextField= new TextField<String>("rolelist.add-role-form.rolename.input", newRolename);

        public AddRoleForm(String id, final ListModel<Role> allRolenames) {
            super(id);

            serviceObjectName = ((AuthenticatedWebApplication) getApplication()).getInitParameter("UserAccessServiceName");

            add(new Label("new-rolename-label", new ResourceModel("rolelist.add-role-form.rolename.label")));
            add(rolenameTextField
                    .setRequired(true)
                    .add(new RoleValidator(allRolenames))
            );
            
            add(new AjaxFallbackButton("add-role-submit", AddRoleForm.this) {
                
                private static final long serialVersionUID = 1L;

                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                    try {
                        Role role = new Role(newRolename.getObject());
                        JNDIUtils.lookupAndInit(UserAccess.JNDI_NAME, serviceObjectName)
                            .addRole(role);
                        List<Role> currentRolenames = allRolenames.getObject();
                        currentRolenames.add(role);
                        allRolenames.setObject(currentRolenames);
                        window.close(target);
                    } catch (final Exception e) {
                        log.error(this.getClass().toString() + ": " + "onSubmit: " + e.getMessage());
                        log.debug("Exception: ", e);
                    }
                }
                
                @Override
                protected void onError(AjaxRequestTarget target, Form<?> form) {
                    target.addComponent(form);
                }
            });
        }
    };
}
