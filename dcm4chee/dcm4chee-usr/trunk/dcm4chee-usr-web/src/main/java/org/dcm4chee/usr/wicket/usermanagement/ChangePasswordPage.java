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

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.validation.EqualPasswordInputValidator;
import org.apache.wicket.markup.html.pages.InternalErrorPage;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.dcm4chee.usr.dao.UserAccess;
import org.dcm4chee.usr.entity.User;
import org.dcm4chee.usr.wicket.util.JNDIUtils;
import org.dcm4chee.usr.wicket.util.SecurityUtils;
import org.dcm4chee.usr.wicket.validator.PasswordValidator;
import org.dcm4chee.usr.wicket.validator.ValidatorMessageLabel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Robert David <robert.david@agfa.com>
 * @version $Revision$ $Date$
 * @since 28.09.2009
 */
@AuthorizeInstantiation({"USER", "ADMIN"})
public class ChangePasswordPage extends WebPage {
    
    private static final long serialVersionUID = 1L;
    
    private static Logger log = LoggerFactory.getLogger(ChangePasswordPage.class);

    public ChangePasswordPage(final ModalWindow window, String userId, final User forUser) {
        try {
            Label resultMessage = new Label("result-message");
            final ChangePasswordForm changePasswordForm = new ChangePasswordForm("change-password-form", userId, forUser, new Model<String>(), new Model<String>(), resultMessage);
            add(changePasswordForm);
            add(resultMessage);

            add(new AjaxLink<Object>("close") {

                private static final long serialVersionUID = 1L;

                @Override
                public void onClick(AjaxRequestTarget target) {
                    window.close(target);
                }
            });
        } catch (Exception e) {
            log.error(this.getClass().toString() + ": " + "init: " + e.getMessage());
            log.debug("Exception: ", e);
            this.redirectToInterceptPage(new InternalErrorPage());
        }
    }
        
    private final class ChangePasswordForm extends Form<Object> {

        private static final long serialVersionUID = 1L;

        private User forUser;
        private Model<String> newPassword;
        private Label resultMessage;

        public ChangePasswordForm(String id, String userId, User forUser, Model<String> oldPassword, Model<String> newPassword, Label resultMessage) {
            super(id);
            
            this.forUser = forUser;
            this.newPassword = newPassword;

            this.resultMessage = resultMessage;
            this.resultMessage.setVisible(false);

            final Label oldPasswordLabel = new Label("old-password-label", new StringResourceModel("change_password.old_password.label", this, null));
            this.add(oldPasswordLabel);

            final PasswordTextField oldPasswordTf = new PasswordTextField("change_password.old_password.input", oldPassword);
            this.add(oldPasswordTf);

            final ValidatorMessageLabel oldPasswordVml = new ValidatorMessageLabel("old-password-validator-message-label", oldPasswordTf);
            this.add(oldPasswordVml);

            Label forUserLabel = new Label("for-user-label", this.forUser.getUserID());
//            if (this.forUser.getUserID().equals(((JaasWicketSession) getSession()).getUsername())) {
            if (this.forUser.getUserID().equals(userId)) {
                forUserLabel.setVisible(false);
                this.add(new PasswordValidator(this.forUser, oldPasswordTf));
            } else {
                oldPasswordLabel.setVisible(false);
                oldPasswordTf.setVisible(false);
            }
            this.add(forUserLabel);
            
            final PasswordTextField newPasswordTf1 = new PasswordTextField("change_password.new_password_1.input", newPassword);
            this.add(newPasswordTf1);
            
            final ValidatorMessageLabel newPasswordVml1 = new ValidatorMessageLabel("new-password-validator-message-label-1", newPasswordTf1);
            this.add(newPasswordVml1);

            final PasswordTextField newPasswordTf2 = new PasswordTextField("change_password.new_password_2.input", new Model<String>(""));
            this.add(newPasswordTf2);

            final ValidatorMessageLabel newPasswordVml2 = new ValidatorMessageLabel("new-password-validator-message-label-2", newPasswordTf2);
            this.add(newPasswordVml2);

            EqualPasswordInputValidator eiv = new EqualPasswordInputValidator(newPasswordTf1, newPasswordTf2);        
            this.add(eiv);

            final Button submitButton = new Button("change-password-submit");
            this.add(submitButton);
        }
        
        @Override
        protected void onValidate() {
            resultMessage.setVisible(false);
        }
        
        @Override
        protected void onSubmit() {
            try {
                UserAccess dao = (UserAccess) JNDIUtils.lookup(UserAccess.JNDI_NAME);
                String encodedPassword = SecurityUtils.encodePassword(this.newPassword.getObject());
                dao.updateUser(forUser.getUserID(), encodedPassword);
                this.forUser.setPassword(encodedPassword);
                
                resultMessage.setDefaultModel(new StringResourceModel("change_password.success-message", this, null));
                resultMessage.add(new AttributeModifier("class", true, new Model<String>("message-system")));               
            } catch (final Exception e) {
                log.error(this.getClass().toString() + ": " + "init: " + e.getMessage());
                log.debug("Exception: ", e);
                resultMessage.setDefaultModel(new StringResourceModel("change_password.failure-message", this, null));
                resultMessage.add(new AttributeModifier("class", true, new Model<String>("message-error")));
            }
            resultMessage.setVisible(true);
        }                
    }
}
