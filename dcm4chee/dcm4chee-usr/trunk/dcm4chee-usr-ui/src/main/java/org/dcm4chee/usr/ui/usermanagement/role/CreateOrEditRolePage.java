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

import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.resources.CompressedResourceReference;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.security.components.SecureWebPage;
import org.dcm4chee.usr.dao.UserAccess;
import org.dcm4chee.usr.model.Role;
import org.dcm4chee.usr.ui.usermanagement.markup.ColorPicker;
import org.dcm4chee.usr.ui.validator.RoleValidator;
import org.dcm4chee.usr.util.JNDIUtils;
import org.dcm4chee.web.common.base.BaseWicketApplication;
import org.dcm4chee.web.common.base.BaseWicketPage;
import org.dcm4chee.web.common.markup.BaseForm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Robert David <robert.david@agfa.com>
 * @version $Revision$ $Date$
 * @since 21.07.2010
 */
public class CreateOrEditRolePage extends SecureWebPage {
    
    private static final long serialVersionUID = 1L;

    private static final ResourceReference BaseCSS = new CompressedResourceReference(BaseWicketPage.class, "base-style.css");
    
    private static Logger log = LoggerFactory.getLogger(CreateOrEditRolePage.class);

    protected ModalWindow window;
   
    public CreateOrEditRolePage(final ModalWindow window, ListModel<Role> allRolenames, Role role) {
        super();
        
        if (CreateOrEditRolePage.BaseCSS != null)
            add(CSSPackageResource.getHeaderContribution(CreateOrEditRolePage.BaseCSS));

        this.window = window;
        add(new CreateOrEditRoleForm("add-role-form", allRolenames, role));
        
        add(new WebMarkupContainer("create-role-title").setVisible(role == null));
        add(new WebMarkupContainer("edit-role-title").setVisible(role != null));
    }

    private final class CreateOrEditRoleForm extends BaseForm {
        
        private static final long serialVersionUID = 1L;

        String serviceObjectName;
        
        private Model<String> rolename = new Model<String>();
        private Model<String> type = new Model<String>();
        private Model<String> description= new Model<String>();
        private Model<String> colorPickerModel = new Model<String>();
        
        private TextField<String> rolenameTextField= new TextField<String>("rolelist.add-role-form.rolename.input", rolename);
        private TextField<String> typeTextField= new TextField<String>("rolelist.add-role-form.type.input", type);
        private TextField<String> descriptionTextField= new TextField<String>("rolelist.add-role-form.description.input", description);
        
        public CreateOrEditRoleForm(String id, final ListModel<Role> allRolenames, final Role role) {
            super(id);

            serviceObjectName = ((BaseWicketApplication) getApplication()).getInitParameter("UserAccessServiceName");
            
            add(rolenameTextField
                    .setRequired(true)
                    .add(new RoleValidator(allRolenames, (role == null ? null : role.getRolename())))
            );
            add(typeTextField);
            add(descriptionTextField);
            final ColorPicker colorPicker;
            add(colorPicker = new ColorPicker("color-picker", colorPickerModel));

            if (role != null) {
                rolenameTextField.setModelObject(role.getRolename());
                typeTextField.setModelObject(role.getType());
                descriptionTextField.setModelObject(role.getDescription());
                colorPicker.setColorValue(role.getColor());
            }
            
            add(new AjaxFallbackButton("add-role-submit", CreateOrEditRoleForm.this) {
                
                private static final long serialVersionUID = 1L;

                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                    try {
                        UserAccess userAccess = (UserAccess) JNDIUtils.lookup(UserAccess.JNDI_NAME);
                        if (role == null) {
                            Role newRole = new Role(rolename.getObject());
                            newRole.setType(type.getObject());
                            newRole.setDescription(description.getObject());
                            newRole.setColor(colorPicker.getColorValue());
                            userAccess.addRole(newRole);
                            
                        } else {
                            role.setRolename(rolename.getObject());
                            role.setType(type.getObject());
                            role.setDescription(description.getObject());
                            role.setColor(colorPicker.getColorValue());
                            userAccess.updateRole(role);
                        }
                        allRolenames.setObject(userAccess.getAllRoles());
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
