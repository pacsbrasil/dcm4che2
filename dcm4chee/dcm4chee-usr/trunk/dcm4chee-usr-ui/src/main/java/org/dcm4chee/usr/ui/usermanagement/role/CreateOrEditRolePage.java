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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.resources.CompressedResourceReference;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.security.components.SecureWebPage;
import org.dcm4chee.usr.dao.UserAccess;
import org.dcm4chee.usr.model.Group;
import org.dcm4chee.usr.model.Role;
import org.dcm4chee.usr.ui.config.delegate.UsrCfgDelegate;
import org.dcm4chee.usr.ui.util.CSSUtils;
import org.dcm4chee.usr.ui.validator.RoleValidator;
import org.dcm4chee.usr.util.JNDIUtils;
import org.dcm4chee.web.common.base.BaseWicketApplication;
import org.dcm4chee.web.common.base.BaseWicketPage;
import org.dcm4chee.web.common.markup.BaseForm;
import org.dcm4chee.web.common.secure.SecurityBehavior;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Robert David <robert.david@agfa.com>
 * @version $Revision$ $Date$
 * @since Jul. 21, 2010
 */
public class CreateOrEditRolePage extends SecureWebPage {
    
    private static final long serialVersionUID = 1L;

    private static final ResourceReference BaseCSS = new CompressedResourceReference(BaseWicketPage.class, "base-style.css");
    
    private static Logger log = LoggerFactory.getLogger(CreateOrEditRolePage.class);

    protected ModalWindow window;
   
    public CreateOrEditRolePage(final ModalWindow window, ListModel<Role> allRolenames, Role role, Map<String,Group> types) {
        super();
        
        if (CreateOrEditRolePage.BaseCSS != null)
            add(CSSPackageResource.getHeaderContribution(CreateOrEditRolePage.BaseCSS));

        this.window = window;
        add(new CreateOrEditRoleForm("add-role-form", allRolenames, role, types));
        
        add(new WebMarkupContainer("create-role-title").setVisible(role == null));
        add(new WebMarkupContainer("edit-role-title").setVisible(role != null));
    }

    private final class CreateOrEditRoleForm extends BaseForm {
        
        private static final long serialVersionUID = 1L;

        private Model<String> rolename = new Model<String>();
        private Model<String> description= new Model<String>();
        private Model<String> type= new Model<String>();
        
        private TextField<String> rolenameTextField= new TextField<String>("rolelist.add-role-form.rolename.input", rolename);
        private TextField<String> descriptionTextField= new TextField<String>("rolelist.add-role-form.description.input", description);
        private DropDownChoice<String> groupDropDown;
        
        private CheckBox webroleCheckbox;
        private CheckBox dicomroleCheckbox;
        private List<CheckBox> typeCheckboxList;
        
        public CreateOrEditRoleForm(String id, final ListModel<Role> allRolenames, final Role role, final Map<String,Group> types) {
            super(id);

            ((BaseWicketApplication) getApplication()).getInitParameter("UserAccessServiceName");
            
            add(rolenameTextField
                    .setRequired(true)
                    .add(new RoleValidator(allRolenames, (role == null ? null : role.getRolename())))
            );
            add(descriptionTextField);
            
            add((groupDropDown = new DropDownChoice<String>("rolelist.add-role-form.group.input", 
                    type, 
                    new ArrayList<String>(types.keySet()), 
                    new IChoiceRenderer<String>() {

                        private static final long serialVersionUID = 1L;

                        public Object getDisplayValue(String object) {
                            return types.get(object);
                        }

                        public String getIdValue(String object, int index) {
                            return String.valueOf(index);
                        }
                    }
            )).setNullValid(true));
            
            if (role != null) {
                rolenameTextField.setModelObject(role.getRolename());
                descriptionTextField.setModelObject(role.getDescription());
                if (role.getGroupUuid() != null)
                    groupDropDown.setModelObject(role.getGroupUuid());
            }
            
            add((webroleCheckbox = new CheckBox("webrole-checkbox", new Model<Boolean>(role == null ? false : role.isWebRole()))));
            add((dicomroleCheckbox = new CheckBox("dicomrole-checkbox", new Model<Boolean>(role == null ? false : role.isDicomRole()))));

            RepeatingView typeRows = new RepeatingView("type-rows");
            add(typeRows);
            
            int i = 0;
            List<String> roleTypes = UsrCfgDelegate.getInstance().getRoleTypes();
            if (roleTypes != null) {
                int knownRoleTypes = roleTypes.size();
                if (role != null) {
                    List<String> roleRoleTypes = role.getRoleTypes();
                    for (int j = 0, len = roleRoleTypes.size(); j < len ; j++) {
                        if (!roleTypes.contains(roleRoleTypes.get(j))) {
                            roleTypes.add(roleRoleTypes.get(j));
                        }
                    }
                }
                
                typeCheckboxList = new ArrayList<CheckBox>(roleTypes.size());
                for (final String type : roleTypes) {
                    WebMarkupContainer rowParent;            
                    typeRows.add((rowParent = new WebMarkupContainer(typeRows.newChildId()))
                            .add(new Label("typename", i < knownRoleTypes ? type : type + " (retired)"))
                    );
                    rowParent.add(new AttributeModifier("class", true, new Model<String>(CSSUtils.getRowClass(i++))));

                    CheckBox typeCheckbox;
                    rowParent.add((typeCheckbox = new CheckBox("type-checkbox", 
                            new Model<Boolean>(role != null ? role.getRoleTypes().contains(type) : false)))
                    .setLabel(new Model<String>(type))
                    .add(new SecurityBehavior(getModuleName() + ":changeTypeAssignmentCheckbox")));
                    typeCheckboxList.add(typeCheckbox);
                    typeRows.add(rowParent);
                }
            }

            add(new AjaxFallbackButton("add-role-submit", CreateOrEditRoleForm.this) {
                
                private static final long serialVersionUID = 1L;

                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                    try {
                        UserAccess userAccess = (UserAccess) JNDIUtils.lookup(UserAccess.JNDI_NAME);

                        List<String> typeList = new ArrayList<String>();
                        for (CheckBox checkbox : typeCheckboxList)
                            if (checkbox.getModelObject())
                                typeList.add(checkbox.getLabel().getObject());

                        if (role == null) {
                            Role newRole = new Role(rolename.getObject());
                            newRole.setDescription(description.getObject());
                            newRole.setGroupUuid(type.getObject());
                            newRole.setWebRole(webroleCheckbox.getModelObject());
                            newRole.setDicomRole(dicomroleCheckbox.getModelObject());
                            newRole.setRoleTypes(typeList);
                            userAccess.addRole(newRole);
                        } else {
                            role.setRolename(rolename.getObject());
                            role.setGroupUuid(type.getObject());
                            role.setDescription(description.getObject());
                            role.setWebRole(webroleCheckbox.getModelObject());
                            role.setDicomRole(dicomroleCheckbox.getModelObject());
                            role.setRoleTypes(typeList);
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

    public static String getModuleName() {
        return "roletypes";
    }
}
