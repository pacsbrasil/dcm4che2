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

package org.dcm4chee.usr.wicket.usermanagement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.authentication.AuthenticatedWebApplication;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.validation.EqualPasswordInputValidator;
import org.apache.wicket.markup.html.image.ContextImage;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.pages.InternalErrorPage;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.resources.CompressedResourceReference;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.dcm4chee.usr.dao.UserAccess;
import org.dcm4chee.usr.entity.Role;
import org.dcm4chee.usr.entity.User;
import org.dcm4chee.usr.wicket.util.CSSUtils;
import org.dcm4chee.usr.wicket.util.JNDIUtils;
import org.dcm4chee.usr.wicket.util.SecurityUtils;
import org.dcm4chee.usr.wicket.validator.RoleValidator;
import org.dcm4chee.usr.wicket.validator.UserValidator;
import org.dcm4chee.usr.wicket.validator.ValidatorMessageLabel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Robert David <robert.david@agfa.com>
 * @version $Revision$ $Date$
 * @since 28.09.2009
 */
public class UserListPanel extends Panel {

    private static final long serialVersionUID = 1L;
    
    private static Logger log = LoggerFactory.getLogger(UserListPanel.class);
    
    private static final ResourceReference CSS = new CompressedResourceReference(UserListPanel.class, "wicket-style.css");
    
    private List<User> allUsers;
    private List<String> allRoleNames;

    private ModalWindow window;
    private String userId;

    public UserListPanel(String id, String userId, ModalWindow window) {
        super(id);
        
        try {
            this.userId = userId;

            this.allUsers = this.getAllUsers();
            this.allRoleNames = this.getAllRolenames();

            this.window = window;
            
            if (UserListPanel.CSS != null)
                add(CSSPackageResource.getHeaderContribution(UserListPanel.CSS));
        } catch (Exception e) {
            log.error(this.getClass().toString() + ": " + "init: " + e.getMessage());
            log.debug("Exception: ", e);
            this.getApplication().getSessionStore().setAttribute(getRequest(), "exception", e);
            throw new RuntimeException();
        }
    }

    @Override
    public void onBeforeRender() {
        super.onBeforeRender();
        
        try {
            addOrReplace(new ToggleFormLink("toggle-user-form-link", 
                    new AddUserForm("add-user-form", this.allUsers),
                    this, 
                    "toggle-form-image-user", 
                    Arrays.asList("userlist.add_user.title.tooltip", "userlist.add_user.close.tooltip"))
            );
            
            RepeatingView roleHeaders = new RepeatingView("role-headers");
            for (String rolename : this.allRoleNames) {
                roleHeaders.add(new Label(roleHeaders.newChildId(), rolename));
            }
            addOrReplace(roleHeaders);
    
            RepeatingView roleRows = new RepeatingView("role-rows");
            addOrReplace(roleRows);
            
            for (int i = 0; i < this.allUsers.size(); i++) {
                
                final User user = this.allUsers.get(i);
                
                WebMarkupContainer row_parent = new WebMarkupContainer(roleRows.newChildId());
                Label userIDLabel = new Label("userID", user.getUserID().toString());
                row_parent.add(userIDLabel);
                roleRows.add(row_parent);
    
                row_parent.add(new AttributeModifier("class", true, new Model<String>(CSSUtils.getRowClass(i))));
                
                RemoveUserLink removeUserLink = new RemoveUserLink("remove-user-link", user);
                removeUserLink.add(new AttributeModifier("title", true, new Model<String>(new ResourceModel("userlist.remove_user.tooltip").wrapOnAssignment(this).getObject() + user.getUserID())));
                removeUserLink.add(new AttributeModifier("onclick", true, new Model<String>("return confirm('" + new ResourceModel("userlist.remove_user.confirmation").wrapOnAssignment(this).getObject() + "');")));
                removeUserLink.add(new Image("img-delete", new ResourceReference(UserListPanel.class, "images/action_delete.png")));
                row_parent.add(removeUserLink);

                if (this.userId.equals(user.getUserID()))
                    removeUserLink.setVisible(false);
                
                row_parent.add(
                        new ChangePasswordLink("change-password-link", this.window, this.userId, user)
                        .add(new Image("img-change-password", new ResourceReference(UserListPanel.class, "images/login.png")))
                        .add(new AttributeModifier("title", true, new Model<String>(new ResourceModel("userlist.change_password.tooltip").wrapOnAssignment(this).getObject())))
                );
                
                RepeatingView roleDividers = new RepeatingView("role-dividers");
                row_parent.add(roleDividers);
                roleRows.add(row_parent);
    
                for (final String rolename : this.allRoleNames) {
                    HasRoleCheckBox roleCheckbox = new HasRoleCheckBox("role-checkbox", new HasRoleModel(user, rolename));
                    if (this.userId.equals(user.getUserID())) {
                        AuthenticatedWebApplication awa = (AuthenticatedWebApplication) getApplication(); 
                        if (rolename.equals(awa.getInitParameter("userRoleName")) || rolename.equals(awa.getInitParameter("adminRoleName"))) {
                            for (Role role : user.getRoles()) {
                                if (role.getRole().equals(rolename))
                                    roleCheckbox.setEnabled(false)
                                    .add(new AttributeModifier("title", true, new ResourceModel("userlist.add_role.change_denied.tooltip").wrapOnAssignment(this)));
                            }
                        }                        
                    }
                    roleDividers.add(
                            new WebMarkupContainer(roleRows.newChildId())
                            .add(roleCheckbox)              
                    );
                }
                row_parent.add(new ToggleFormLink("toggle-role-form-link", 
                        new AddRoleForm("add-role-form", user.getUserID(), this.allRoleNames), 
                        row_parent, 
                        "toggle-form-image-role", 
                        Arrays.asList("userlist.add_role.title.tooltip", "userlist.add_role.close.tooltip"))
                );
            }
        } catch (Exception e) {
            log.error(this.getClass().toString() + ": " + "onBeforeRender: " + e.getMessage());
            log.debug("Exception: ", e);
            this.getApplication().getSessionStore().setAttribute(getRequest(), "exception", e);
            throw new RuntimeException();
        }
    }

    private final class AddUserForm extends Form<Object> {
        
        private static final long serialVersionUID = 1L;
        
        private Model<String> newUsername = new Model<String>();
        private Model<String> password = new Model<String>();
        
        public AddUserForm(String id, List<User> currentUserList) {
            super(id);
                      
            newAjaxComponent(this)
                .setVisible(false);

            this.add(newAjaxComponent(
                    new Label("new-username-label", new ResourceModel("userlist.add_user.username.label").wrapOnAssignment(this))));
            TextField<String> usernameTf;
            this.add(newAjaxComponent(
                    (usernameTf = new TextField<String>("userlist.add_user.username.input", newUsername))
                    .setRequired(true)
                    .add(new UserValidator(currentUserList)))
            );
            this.add(newAjaxComponent(
                    new ValidatorMessageLabel("new-username-validator-message-label", usernameTf)));
            
            this.add(newAjaxComponent(
                    new Label("password-label-1", new ResourceModel("userlist.add_user.password_1.label").wrapOnAssignment(this))));
            PasswordTextField passwordTf1 = null;
            this.add(newAjaxComponent(
                    (passwordTf1 = new PasswordTextField("userlist.add_user.password_1.input", password))));
            this.add(newAjaxComponent(
                    new ValidatorMessageLabel("password-validator-message-label-1", passwordTf1)));
         
            this.add(newAjaxComponent(
                    new Label("password-label-2", new ResourceModel("userlist.add_user.password_2.label").wrapOnAssignment(this))));
            PasswordTextField passwordTf2 = null;
            this.add(newAjaxComponent(
                    (passwordTf2 = new PasswordTextField("userlist.add_user.password_2.input", new Model<String>("")))));
            this.add(newAjaxComponent(
                    new ValidatorMessageLabel("password-validator-message-label-2", passwordTf2)));

            this.add(new EqualPasswordInputValidator(passwordTf1, passwordTf2));
        
            add(new AjaxFallbackButton("add-user-submit", AddUserForm.this) {
                
                private static final long serialVersionUID = 1L;
    
                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                    try {
                        User user = new User();
                        user.setUserID(newUsername.getObject());
                        user.setPassword(SecurityUtils.encodePassword(password.getObject()));
                        ((UserAccess) JNDIUtils.lookup(UserAccess.JNDI_NAME)).createUser(user);
                        setResponsePage(this.getPage().getClass());
                    } catch (final Exception e) {
                        log.error(this.getClass().toString() + ": " + "onSubmit: " + e.getMessage());
                        log.debug("Exception: ", e);
                        this.getApplication().getSessionStore().setAttribute(getRequest(), "exception", e);
                        throw new RuntimeException();
                    }
                }
                
                @Override
                protected void onError(AjaxRequestTarget target, Form<?> form) {
                    target.addComponent(form.get("new-username-validator-message-label"));
                    target.addComponent(form.get("password-validator-message-label-1"));
                    target.addComponent(form.get("password-validator-message-label-2"));
                }
            });
        }
    };
    
    private final class AddRoleForm extends Form<Object> {
        
        private static final long serialVersionUID = 1L;
        
        private Model<String> newRolename = new Model<String>();
        
        public AddRoleForm(String id, final String userId, List<String> currentRoleNameList) {
            super(id);
            
            newAjaxComponent(this).setVisible(false);
            
            this.add(newAjaxComponent(
                    new Label("new-rolename-label", new ResourceModel("userlist.add_role.rolename.label").wrapOnAssignment(this))));
            TextField<String> rolenameTf = null;
            this.add(newAjaxComponent(
                    (rolenameTf = new TextField<String>("userlist.add_role.rolename.input", newRolename))
                    .setRequired(true)
                    .add(new RoleValidator(currentRoleNameList)))
            );
            this.add(newAjaxComponent(
                    new ValidatorMessageLabel("new-rolename-validator-message-label", rolenameTf)));
            
            this.add(new AjaxFallbackButton("add-role-submit", AddRoleForm.this) {
                
                private static final long serialVersionUID = 1L;

                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                    try {
                        Role role = new Role();
                        role.setUserID(userId);
                        role.setRole(newRolename.getObject());
                        ((UserAccess) JNDIUtils.lookup(UserAccess.JNDI_NAME)).addRole(role);
                        setResponsePage(this.getPage().getClass());
                    } catch (final Exception e) {
                        log.error(this.getClass().toString() + ": " + "onSubmit: " + e.getMessage());
                        log.debug("Exception: ", e);
                        this.getApplication().getSessionStore().setAttribute(getRequest(), "exception", e);
                        throw new RuntimeException();               
                    }
                }
                
                @Override
                protected void onError(AjaxRequestTarget target, Form<?> form) {
                    target.addComponent(form.get("new-rolename-validator-message-label"));
                }
            });
        }
    };
    
    private final class ToggleFormImage extends Image {
        
        private static final long serialVersionUID = 1L;
        
        private Form<?> forForm;
        
        public ToggleFormImage(String id, Form<?> forForm) {
            super(id);
            this.forForm = forForm;
        }
        
        @Override
        protected void onComponentTag(ComponentTag tag) {
            super.onComponentTag(tag);
            tag.put("src", this.forForm.isVisible() ? 
                    this.getRequestCycle().urlFor(new ResourceReference(UserListPanel.class, "images/action_remove.png"))
                    : this.getRequestCycle().urlFor(new ResourceReference(UserListPanel.class, "images/action_add.png")));
        }
    };
    
    private final class ToggleFormLink extends AjaxFallbackLink<Object> {
        
        private static final long serialVersionUID = 1L;
        
        private Form<?> forForm;
        private ToggleFormImage toggleFormImage;
        private List<String> open_close_tooltip_texts; 
        
        public ToggleFormLink(String id, Form<?> forForm, MarkupContainer container, String toggleFormImageId, List<String> open_close_tooltip_texts) {
            super(id);

            newAjaxComponent(this);

            container.addOrReplace(this.forForm = forForm);
            this.open_close_tooltip_texts = open_close_tooltip_texts;
            this.add(newAjaxComponent(
                    (this.toggleFormImage = new ToggleFormImage(toggleFormImageId, forForm))));
        }

        @Override
        protected void onComponentTag(ComponentTag tag) {
            super.onComponentTag(tag);
            if ((this.open_close_tooltip_texts != null) && (this.open_close_tooltip_texts.size() == 2))
                tag.put("title", new ResourceModel(this.open_close_tooltip_texts.get(forForm.isVisible() ? 1 : 0)).wrapOnAssignment(this).getObject());            
        }
        
        @Override
        public void onClick(AjaxRequestTarget target) {
            this.forForm.setVisible(!this.forForm.isVisible()); 
            target.addComponent(this);
            target.addComponent(this.toggleFormImage);
            target.addComponent(this.forForm);
        }
    };

    private final class HasRoleCheckBox extends AjaxCheckBox {
        
        private static final long serialVersionUID = 1L;
        
        private HasRoleCheckBox(String id, HasRoleModel model) {
            super(id, model);
        }
        
        @Override
        protected void onUpdate(AjaxRequestTarget target) {
            target.addComponent(this);
            final Exception e;
            if ((e = ((HasRoleModel) this.getModel()).getException()) != null) {
                log.error(this.getClass().toString() + ": " + "onUpdate: " + e.getMessage());
                log.debug("Exception: ", e);
            }
        }
        
        @Override
        protected void onComponentTag(ComponentTag tag) {
            super.onComponentTag(tag);
            tag.put("title", new ResourceModel(((HasRoleModel) this.getModel()).getObject().booleanValue() ? "userlist.assign_role.remove.tooltip" : "userlist.assign_role.add.tooltip").wrapOnAssignment(this).getObject());
        }
    }
    
    private final class HasRoleModel implements IModel<Boolean> {
        
        private static final long serialVersionUID = 1L;
        
        private User user;
        private String rolename;
        private Exception exception;
        
        public HasRoleModel(User user, String rolename) {
            this.user = user;
            this.rolename = rolename;
            this.exception = null;
        }
        
        public Exception getException() {
            return exception;
        }

        @Override
        public Boolean getObject() {
            try {
                for (Role role : this.user.getRoles()) {
                    if (role.getRole().equals(this.rolename)) return true;
                }
            } catch (Exception e) {
                this.exception = e;
            }
            return false;
        }
        
        @Override
        public void setObject(Boolean hasRole) {
            try {
                if (hasRole) {
                    Role role = new Role();
                    role.setUserID(this.user.getUserID());
                    role.setRole(this.rolename);
                    ((UserAccess) JNDIUtils.lookup(UserAccess.JNDI_NAME)).addRole(role);
                    this.user.getRoles().add(role);
                } else {
                    for (Role role : this.user.getRoles()) {
                        if (role.getRole().equals(this.rolename)) {
                            ((UserAccess) JNDIUtils.lookup(UserAccess.JNDI_NAME)).removeRole(role);
                            this.user.getRoles().remove(role);
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                this.exception = e;
                log.error(this.getClass().toString() + ": " + "setObject: " + e.getMessage());
                log.debug("Exception: ", e);
            }
        }
        
        @Override
        public void detach() {
        }
    }
    
    private final class RemoveUserLink extends Link<Object> {
        
        private static final long serialVersionUID = 1L;
        
        private User user;
        public RemoveUserLink(String id, User user) {
            super(id);
            this.user = user;
        }
        
        @Override
        public void onClick() {
            try {
                ((UserAccess) JNDIUtils.lookup(UserAccess.JNDI_NAME)).deleteUser(user.getUserID());
                setResponsePage(this.getPage().getClass());
            } catch (Exception e) {
                log.error(this.getClass().toString() + ": " + "onClick: " + e.getMessage());
                log.debug("Exception: ", e);
            }
        }
    };

    private List<User> getAllUsers() {
        
        List<User> allUsers = new ArrayList<User>();
        try {
            allUsers.addAll(((UserAccess) JNDIUtils.lookup(UserAccess.JNDI_NAME)).findAll());
        } catch (Exception e) {
            log.error(this.getClass().toString() + ": " + "getAllUsers: " + e.getMessage());
            log.debug("Exception: ", e);
            this.redirectToInterceptPage(new InternalErrorPage());
        }
        return allUsers;
    }
    
    private ArrayList<String> getAllRolenames() {

        ArrayList<String> allRolenames = new ArrayList<String>(2);
        try {   
            AuthenticatedWebApplication awa = (AuthenticatedWebApplication) getApplication();
            allRolenames.addAll(((UserAccess) JNDIUtils.lookup(UserAccess.JNDI_NAME)).getAllRolenames());
            if (!allRolenames.contains(awa.getInitParameter("userRoleName"))) allRolenames.add(awa.getInitParameter("userRoleName"));
            if (!allRolenames.contains(awa.getInitParameter("adminRoleName"))) allRolenames.add(awa.getInitParameter("adminRoleName"));
        } catch (Exception e) {
            log.error(this.getClass().toString() + ": " + "getAllRolenames: " + e.getMessage());
            log.debug("Exception: ", e);
            this.redirectToInterceptPage(new InternalErrorPage());
        }
        return allRolenames;
    }
    
    private Component newAjaxComponent(Component component) {
        component.setOutputMarkupId(true);
        component.setOutputMarkupPlaceholderTag(true);
        return component;
    }
}