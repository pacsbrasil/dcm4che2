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

import org.apache.wicket.ResourceReference;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.validation.EqualPasswordInputValidator;
import org.apache.wicket.markup.html.resources.CompressedResourceReference;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.util.ListModel;
import org.dcm4chee.usr.dao.UserAccess;
import org.dcm4chee.usr.entity.User;
import org.dcm4chee.usr.ui.util.SecurityUtils;
import org.dcm4chee.usr.ui.validator.UserValidator;
import org.dcm4chee.usr.util.JNDIUtils;
import org.dcm4chee.web.common.base.BaseWicketPage;
import org.dcm4chee.web.common.markup.BaseForm;

/**
 * @author Robert David <robert.david@agfa.com>
 * @version $Revision$ $Date$
 * @since 28.09.2009
 */
public class AddUserPage extends WebPage {
    
    private static final long serialVersionUID = 1L;
    
    private static final ResourceReference BaseCSS = new CompressedResourceReference(BaseWicketPage.class, "base-style.css");

    protected ModalWindow window;
    
    public AddUserPage(final ModalWindow window) {
        super();
        
        if (AddUserPage.BaseCSS != null)
            add(CSSPackageResource.getHeaderContribution(AddUserPage.BaseCSS));

        this.window = window;
        
        add(new Label("page-title", new ResourceModel("userlist.add-user-form.title")));
        add(new AddUserForm("add-user-form"));        
    }

//    private final class AddGroupForm extends BaseForm {
//        
//        private static final long serialVersionUID = 1L;
//        
//        private Model<String> newGroupname = new Model<String>();
//
//        public AddGroupForm(String id) {
//            super(id);
//                      
//            add(new Label("new-groupname-label", new ResourceModel("dashboard.report.add-group-form.label").wrapOnAssignment(this)));
//            add((new TextField<String>("dashboard.report.add-group-form.groupname.input", newGroupname)
//            .add(new PatternValidator("^[A-Za-z0-9]+$"))
//            .setRequired(true)));
//            add(new AjaxFallbackButton("add-group-submit", AddGroupForm.this) {
//        
//                private static final long serialVersionUID = 1L;
//
//                @Override
//                protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
//                    try {
//                        DashboardDelegator.getInstance((((AuthenticatedWebApplication) getApplication()).getInitParameter("DashboardServiceName"))).createReport(new ReportModel(null, newGroupname.getObject(), null, null, null, false, null, null), true);
//                        window.close(target);
//                    } catch (final Exception e) {
//                        log.error(this.getClass().toString() + ": " + "onSubmit: " + e.getMessage());
//                        log.debug("Exception: ", e);
//                    }
//                }
//                
//                @Override
//                protected void onError(AjaxRequestTarget target, Form<?> form) {
//                    target.addComponent(form);
//                }
//            });
//        }
//    }
    
    private final class AddUserForm extends BaseForm {
        
        private static final long serialVersionUID = 1L;
        
        private Model<String> newUsername = new Model<String>();
        private Model<String> password = new Model<String>();

        public AddUserForm(String id) {
            super(id);

            ListModel<User> allUsers = new ListModel<User>();
            List<User> l = new ArrayList<User>();
l.addAll(((UserAccess) JNDIUtils.lookup(UserAccess.JNDI_NAME)).findAll());
            allUsers.setObject(l);

            add(new Label("new-username-label", new ResourceModel("userlist.add-user-form.username.label")));
            add((new TextField<String>("userlist.add-user-form.username.input", newUsername))
                    .setRequired(true)
                    .add(new UserValidator(allUsers))
            );
            
            add(new Label("password-label-1", new ResourceModel("userlist.add-user-form.password_1.label")));
            add(new Label("password-label-2", new ResourceModel("userlist.add-user-form.password_2.label")));
            
            PasswordTextField passwordTf1 = null;
            PasswordTextField passwordTf2 = null;
            add(passwordTf1 = new PasswordTextField("userlist.add-user-form.password_1.input", password));
            add(passwordTf2 = new PasswordTextField("userlist.add-user-form.password_2.input", new Model<String>("")));
            add(new EqualPasswordInputValidator(passwordTf1, passwordTf2));
        
            add(new Button("add-user-submit") {
                
                private static final long serialVersionUID = 1L;

                @Override
                public void onSubmit() {
                    User user = new User();
                    user.setUserID(newUsername.getObject());
                    user.setPassword(SecurityUtils.encodePassword(password.getObject()));
                    ((UserAccess) JNDIUtils.lookup(UserAccess.JNDI_NAME)).createUser(user);

                    newUsername.setObject("");
                    clearInput();
                    getParent().setVisible(false);
                }
            });
        }
    };
}
