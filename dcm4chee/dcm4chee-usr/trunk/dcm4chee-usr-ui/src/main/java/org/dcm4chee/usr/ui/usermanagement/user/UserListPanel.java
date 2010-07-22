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

package org.dcm4chee.usr.ui.usermanagement.user;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.authentication.AuthenticatedWebApplication;
import org.apache.wicket.authorization.strategies.role.Roles;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.resources.CompressedResourceReference;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.util.ListModel;
import org.dcm4chee.icons.ImageManager;
import org.dcm4chee.icons.behaviours.ImageSizeBehaviour;
import org.dcm4chee.usr.dao.Role;
import org.dcm4chee.usr.dao.UserAccess;
import org.dcm4chee.usr.entity.User;
import org.dcm4chee.usr.entity.UserRoleAssignment;
import org.dcm4chee.usr.ui.usermanagement.ChangePasswordLink;
import org.dcm4chee.usr.ui.usermanagement.UserManagementPanel;
import org.dcm4chee.usr.ui.util.CSSUtils;
import org.dcm4chee.usr.util.JNDIUtils;
import org.dcm4chee.web.common.base.BaseWicketPage;
import org.dcm4chee.web.common.base.JaasWicketSession;
import org.dcm4chee.web.common.behaviours.TooltipBehaviour;
import org.dcm4chee.web.common.markup.ModalWindowLink;
import org.dcm4chee.web.common.markup.modal.ConfirmationWindow;

/**
 * @author Robert David <robert.david@agfa.com>
 * @version $Revision$ $Date$
 * @since 28.09.2009
 */
@AuthorizeInstantiation({Roles.ADMIN, "WebAdmin"})
public class UserListPanel extends Panel {

    private static final long serialVersionUID = 1L;
    
    private static final ResourceReference CSS = new CompressedResourceReference(UserManagementPanel.class, "usr-style.css");
    
    private ListModel<User> allUsers;
    private ListModel<Role> allRolenames;

    private String userId;

    private ModalWindow changePasswordWindow;
    private ConfirmationWindow<User> confirmationWindow;
    
    private ModalWindow modalWindow;

    public UserListPanel(String id) {
        super(id);
        
        add(CSSPackageResource.getHeaderContribution(BaseWicketPage.class, "base-style.css"));
        if (UserListPanel.CSS != null)
            add(CSSPackageResource.getHeaderContribution(UserListPanel.CSS));

        setOutputMarkupId(true);
        
        this.userId = ((JaasWicketSession) getSession()).getUsername();
        add(this.changePasswordWindow = new ModalWindow("change-password-window"));
               
        this.allUsers = new ListModel<User>(getAllUsers());
        this.allRolenames = new ListModel<Role>(getAllRolenames());

        add(this.confirmationWindow = new ConfirmationWindow<User>("confirmation-window") {

            private static final long serialVersionUID = 1L;

            @Override
            public void onConfirmation(AjaxRequestTarget target, User userObject) {
                ((UserAccess) JNDIUtils.lookup(UserAccess.JNDI_NAME)).deleteUser(userObject.getUserID());
                target.addComponent(UserListPanel.this);
                allUsers.setObject(getAllUsers());
            }
        });
        
        add(modalWindow = new ModalWindow("modal-window")
            .setPageCreator(new ModalWindow.PageCreator() {
                
                private static final long serialVersionUID = 1L;
                  
                @Override
                public Page createPage() {
                    return new AddUserPage(modalWindow, allUsers);
                }
            })
        );
        
        add(new ModalWindowLink("toggle-user-form-link", modalWindow, 
                new Integer(new ResourceModel("userlist.add-user.window.width").wrapOnAssignment(this).getObject().toString()).intValue(), 
                new Integer(new ResourceModel("userlist.add-user.window.height").wrapOnAssignment(this).getObject().toString()).intValue()
        )
        .add(new Image("toggle-user-form-image", ImageManager.IMAGE_USER_ADD)
        .add(new ImageSizeBehaviour("vertical-align: middle;")))
        .add(new Label("userlist.add-user-form.title", new ResourceModel("userlist.add-user-form.title")))
        .add(new TooltipBehaviour("userlist."))
        );
    }

    @Override
    protected void onBeforeRender() {
        super.onBeforeRender();
        
        RepeatingView roleHeaders = new RepeatingView("role-headers");
        for (Role role : this.allRolenames.getObject())
            roleHeaders.add(new Label(roleHeaders.newChildId(), role.getRolename()));
        addOrReplace(roleHeaders);

        RepeatingView roleRows = new RepeatingView("role-rows");
        addOrReplace(roleRows);
        
        for (int i = 0; i < this.allUsers.getObject().size(); i++) {
            
            final User user = this.allUsers.getObject().get(i);
            
            WebMarkupContainer rowParent;
            roleRows.add((rowParent = new WebMarkupContainer(roleRows.newChildId())).add(new Label("userID", user.getUserID())));

            rowParent.add((new AjaxFallbackLink<Object>("remove-user-link") {

                                private static final long serialVersionUID = 1L;

                                @Override
                                public void onClick(AjaxRequestTarget target) {
                                    confirmationWindow.confirm(target, new Model<String>(new ResourceModel("userlist.remove-user-link.confirmation").wrapOnAssignment(this.getParent()).getObject()), user);
                                }
                            }
                    .add(new Image("img-delete", ImageManager.IMAGE_COMMON_REMOVE)
                    .add(new TooltipBehaviour("userlist.", "remove-user-link", new PropertyModel<String>(user,"userID")))
                    .add(new ImageSizeBehaviour())))
                    .setVisible(!this.userId.equals(user.getUserID())))
                .add(new ChangePasswordLink("change-password-link", this.changePasswordWindow, this.userId, user)
                    .add(new Image("img-change-password", ImageManager.IMAGE_USER_CHANGE_PASSWORD)
                    .add(new ImageSizeBehaviour()))
                    .add(new AttributeModifier("title", true, new Model<String>(new ResourceModel("userlist.change_password.tooltip").wrapOnAssignment(this).getObject())))
                )
                .add(new AttributeModifier("class", true, new Model<String>(CSSUtils.getRowClass(i))));
            
            RepeatingView roleDividers = new RepeatingView("role-dividers");
            rowParent.add(roleDividers);

            for (final Role role : this.allRolenames.getObject()) {
                AjaxCheckBox roleCheckbox = new AjaxCheckBox("role-checkbox", new HasRoleModel(user, role.getRolename())) {

                    private static final long serialVersionUID = 1L;

                    @Override
                    protected void onUpdate(AjaxRequestTarget target) {
                        target.addComponent(this);
                    }
                      
                    @Override
                    protected void onComponentTag(ComponentTag tag) {
                        super.onComponentTag(tag);
                        tag.put("title", new ResourceModel(((HasRoleModel) this.getModel()).getObject().booleanValue() ? "userlist.has-role-checkbox.remove.tooltip" : "userlist.has-role-checkbox.add.tooltip").wrapOnAssignment(this).getObject());
                    }
                };
                
                if (this.userId.equals(user.getUserID())) {
                    AuthenticatedWebApplication awa = (AuthenticatedWebApplication) getApplication(); 
                    if (role.getRolename().equals(awa.getInitParameter("userRoleName")) || role.getRolename().equals(awa.getInitParameter("adminRoleName"))) {
                        for (UserRoleAssignment ura : user.getRoles()) {
                            if (ura.getRole().equals(role.getRolename()))
                                roleCheckbox.setEnabled(false)
                                .add(new AttributeModifier("title", true, new ResourceModel("userlist.add-role-form.change_denied.tooltip").wrapOnAssignment(this)));
                        }
                    }                        
                }
                roleDividers.add(
                        new WebMarkupContainer(roleRows.newChildId())
                        .add(roleCheckbox)              
                );
            }
        }
    }

    private final class HasRoleModel implements IModel<Boolean> {
        
        private static final long serialVersionUID = 1L;
        
        private User user;
        private String rolename;
        
        public HasRoleModel(User user, String rolename) {
            this.user = user;
            this.rolename = rolename;
        }
        
        @Override
        public Boolean getObject() {
            for (UserRoleAssignment role : this.user.getRoles())
                if (role.getRole().equals(this.rolename)) return true;
            return false;
        }
        
        @Override
        public void setObject(Boolean hasRole) {
            if (hasRole) {
                UserRoleAssignment role = new UserRoleAssignment();
                role.setUserID(this.user.getUserID());
                role.setRole(this.rolename);
                ((UserAccess) JNDIUtils.lookup(UserAccess.JNDI_NAME)).assignRole(role);
                this.user.getRoles().add(role);
            } else {
                for (UserRoleAssignment role : this.user.getRoles()) {
                    if (role.getRole().equals(this.rolename)) {
                        ((UserAccess) JNDIUtils.lookup(UserAccess.JNDI_NAME)).unassignRole(role);
                        this.user.getRoles().remove(role);
                        break;
                    }
                }
            }
        }
        
        @Override
        public void detach() {
        }
    }
    
    private List<User> getAllUsers() {
        List<User> allUsers = new ArrayList<User>();
        allUsers.addAll(((UserAccess) JNDIUtils.lookup(UserAccess.JNDI_NAME)).findAll());
        return allUsers;
    }
    
    private List<Role> getAllRolenames() {
        List<Role> allRolenames = new ArrayList<Role>(2);
        allRolenames.addAll(JNDIUtils.lookupAndInit(UserAccess.JNDI_NAME, 
                ((AuthenticatedWebApplication) getApplication()).getInitParameter("UserAccessServiceName"))
                .getAllRolenames());
        return allRolenames;
    }    
}
