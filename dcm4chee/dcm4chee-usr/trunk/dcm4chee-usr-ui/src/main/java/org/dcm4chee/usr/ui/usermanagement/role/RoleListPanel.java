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
import java.util.Arrays;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.authentication.AuthenticatedWebApplication;
import org.apache.wicket.authorization.strategies.role.Roles;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.resources.CompressedResourceReference;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.util.ListModel;
import org.dcm4chee.icons.ImageManager;
import org.dcm4chee.icons.behaviours.ImageSizeBehaviour;
import org.dcm4chee.usr.dao.Role;
import org.dcm4chee.usr.dao.UserAccess;
import org.dcm4chee.usr.ui.util.CSSUtils;
import org.dcm4chee.usr.ui.util.JNDIUtils;
import org.dcm4chee.usr.ui.validator.RoleValidator;
import org.dcm4chee.web.common.base.BaseWicketPage;
import org.dcm4chee.web.common.base.JaasWicketSession;
import org.dcm4chee.web.common.behaviours.TooltipBehaviour;
import org.dcm4chee.web.common.markup.BaseForm;
import org.dcm4chee.web.common.markup.modal.ConfirmationWindow;

/**
 * @author Robert David <robert.david@agfa.com>
 * @version $Revision$ $Date$
 * @since 01.07.2010
 */
@AuthorizeInstantiation({Roles.ADMIN, "WebAdmin"})
public class RoleListPanel extends Panel {

    private static final long serialVersionUID = 1L;
    
    private static final ResourceReference CSS = new CompressedResourceReference(RoleListPanel.class, "usr-style.css");
    
    private ListModel<Role> allRolenames;

    private ConfirmationWindow<Role> confirmationWindow;

    public RoleListPanel(String id) {
        super(id);
        add(CSSPackageResource.getHeaderContribution(BaseWicketPage.class, "base-style.css"));
        if (RoleListPanel.CSS != null)
            add(CSSPackageResource.getHeaderContribution(RoleListPanel.CSS));

        setOutputMarkupId(true);

        AuthenticatedWebApplication awa = ((AuthenticatedWebApplication) getApplication());
        ((UserAccess) JNDIUtils.lookup(UserAccess.JNDI_NAME)).checkSystemRoles(
                awa.getInitParameter("userRoleName"), 
                awa.getInitParameter("adminRoleName"));
        
        ((JaasWicketSession) getSession()).getUsername();
        this.allRolenames = new ListModel<Role>(getAllRolenames());

        add(this.confirmationWindow = new ConfirmationWindow<Role>("confirmation-window") {

            private static final long serialVersionUID = 1L;

            @Override
            public void onConfirmation(AjaxRequestTarget target, Role role) {
                ((UserAccess) JNDIUtils.lookup(UserAccess.JNDI_NAME)).removeRole(role);
                target.addComponent(RoleListPanel.this);
                allRolenames.setObject(getAllRolenames());
            }
        });
        
        add(new ToggleFormLink("toggle-role-form-link", 
                new AddRoleForm("add-role-form"), 
                this, 
                new Image("toggle-role-form-image", ImageManager.IMAGE_USER_ROLE_ADD),  
                Arrays.asList("rolelist.toggle-role-form-link.title.tooltip", "rolelist.toggle-role-form-link.close.tooltip"))
        );
    }

    @Override
    protected void onBeforeRender() {
        super.onBeforeRender();
        
        RepeatingView roleRows = new RepeatingView("role-rows");
        addOrReplace(roleRows);
        
        for (int i = 0; i < this.allRolenames.getObject().size(); i++) {
            final Role role = this.allRolenames.getObject().get(i);
            
            AuthenticatedWebApplication awa = ((AuthenticatedWebApplication) getApplication());
            
            WebMarkupContainer rowParent;
            roleRows.add((rowParent = new WebMarkupContainer(roleRows.newChildId())).add(new Label("rolename", role.getRolename())));
            rowParent.add((new AjaxFallbackLink<Object>("remove-role-link") {

                                private static final long serialVersionUID = 1L;

                                @Override
                                public void onClick(AjaxRequestTarget target) {
                                    confirmationWindow.confirm(target, new Model<String>(new ResourceModel("rolelist.remove-role-link.confirmation").wrapOnAssignment(this.getParent()).getObject()), role);
                                }
                            }
                    .add(new Image("img-delete", ImageManager.IMAGE_COMMON_REMOVE)
                    .add(new TooltipBehaviour("rolelist.", "remove-role-link", new Model<String>(role.getRolename()))))
                    .add(new ImageSizeBehaviour()))
                    .setVisible(!awa.getInitParameter("userRoleName").equals(role.getRolename())
                            && !awa.getInitParameter("adminRoleName").equals(role.getRolename()))
                )
                .add(new AttributeModifier("class", true, new Model<String>(CSSUtils.getRowClass(i))));
        }
    }

    private final class AddRoleForm extends BaseForm {
        
        private static final long serialVersionUID = 1L;
        
        private Model<String> newRolename = new Model<String>();
        private TextField<String> rolenameTextField= new TextField<String>("rolelist.add-role-form.rolename.input", newRolename);
        public AddRoleForm(String id) {
            super(id);
            
            newAjaxComponent(this)
                .setVisible(false);
            
            add(new Label("new-rolename-label", new ResourceModel("rolelist.add-role-form.rolename.label")));
            add(rolenameTextField
                    .setRequired(true)
                    .add(new RoleValidator(allRolenames))
            );
            
            add(new Button("add-role-submit") {
                
                private static final long serialVersionUID = 1L;

                @Override
                public void onSubmit() {
                    ((UserAccess) JNDIUtils.lookup(UserAccess.JNDI_NAME)).addRole(new Role(newRolename.getObject()));
                    rolenameTextField.setModelObject("");
                    getParent().setVisible(false);
                    allRolenames.setObject(getAllRolenames());
                    
                }
            });
        }
    };
    
    private final class ToggleFormLink extends AjaxFallbackLink<Object> {
        
        private static final long serialVersionUID = 1L;
        
        private Form<?> forForm;
        private List<String> open_close_tooltip_texts; 
        
        public ToggleFormLink(String id, Form<?> forForm, MarkupContainer container, Image toggleFormImage, List<String> open_close_tooltip_texts) {
            super(id);

            newAjaxComponent(this);

            container.addOrReplace(this.forForm = forForm);
            this.open_close_tooltip_texts = open_close_tooltip_texts;
            this.add(newAjaxComponent(toggleFormImage)
                    .add(new ImageSizeBehaviour("vertical-align: middle;")));
            this.add(newAjaxComponent(new Label("rolelist.add-role-form.title", new ResourceModel("rolelist.add-role-form.title"))
            .add(new AttributeModifier("style", true, new Model<String>("vertical-align: middle")))));
        }

        @Override
        public void onComponentTag(ComponentTag tag) {
            super.onComponentTag(tag);
            if ((this.open_close_tooltip_texts != null) && (this.open_close_tooltip_texts.size() == 2))
                tag.put("title", new ResourceModel(this.open_close_tooltip_texts.get(forForm.isVisible() ? 1 : 0)).wrapOnAssignment(this).getObject());            
        }
        
        @Override
        public void onClick(AjaxRequestTarget target) {
            this.forForm.setVisible(!this.forForm.isVisible()); 
            target.addComponent(this);
            target.addComponent(this.forForm);
        }
    };

    private ArrayList<Role> getAllRolenames() {

        ArrayList<Role> allRolenames = new ArrayList<Role>(2);
        allRolenames.addAll(((UserAccess) JNDIUtils.lookup(UserAccess.JNDI_NAME)).getAllRolenames());
        return allRolenames;
    }
    
    private Component newAjaxComponent(Component component) {
        component.setOutputMarkupId(true);
        component.setOutputMarkupPlaceholderTag(true);
        return component;
    }    
}
