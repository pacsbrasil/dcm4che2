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
import org.apache.wicket.Page;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.authentication.AuthenticatedWebApplication;
import org.apache.wicket.authorization.strategies.role.Roles;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
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
import org.dcm4chee.usr.ui.usermanagement.UserManagementPanel;
import org.dcm4chee.usr.ui.usermanagement.user.AddUserPage;
import org.dcm4chee.usr.ui.util.CSSUtils;
import org.dcm4chee.usr.ui.validator.RoleValidator;
import org.dcm4chee.usr.util.JNDIUtils;
import org.dcm4chee.web.common.base.BaseWicketPage;
import org.dcm4chee.web.common.base.JaasWicketSession;
import org.dcm4chee.web.common.behaviours.TooltipBehaviour;
import org.dcm4chee.web.common.markup.BaseForm;
import org.dcm4chee.web.common.markup.ModalWindowLink;
import org.dcm4chee.web.common.markup.modal.ConfirmationWindow;

/**
 * @author Robert David <robert.david@agfa.com>
 * @version $Revision$ $Date$
 * @since 01.07.2010
 */
@AuthorizeInstantiation({Roles.ADMIN, "WebAdmin"})
public class RoleListPanel extends Panel {

    private static final long serialVersionUID = 1L;
    
    private static final ResourceReference CSS = new CompressedResourceReference(UserManagementPanel.class, "usr-style.css");
    
    String serviceObjectName;
    
    private ListModel<Role> allRolenames;

    private ConfirmationWindow<Role> confirmationWindow;

    private ModalWindow modalWindow;
    
    public RoleListPanel(String id) {
        super(id);
        
        add(CSSPackageResource.getHeaderContribution(BaseWicketPage.class, "base-style.css"));
        if (RoleListPanel.CSS != null)
            add(CSSPackageResource.getHeaderContribution(RoleListPanel.CSS));

        serviceObjectName = ((AuthenticatedWebApplication) getApplication()).getInitParameter("UserAccessServiceName");
            
        setOutputMarkupId(true);

        ((JaasWicketSession) getSession()).getUsername();
        this.allRolenames = new ListModel<Role>(getAllRolenames());

        add(this.confirmationWindow = new ConfirmationWindow<Role>("confirmation-window") {

            private static final long serialVersionUID = 1L;

            @Override
            public void onConfirmation(AjaxRequestTarget target, Role role) {
                JNDIUtils.lookupAndInit(UserAccess.JNDI_NAME, serviceObjectName).removeRole(role);
                target.addComponent(RoleListPanel.this);
                allRolenames.setObject(getAllRolenames());
            }
        });

        add(modalWindow = new ModalWindow("modal-window")
            .setPageCreator(new ModalWindow.PageCreator() {
                
                private static final long serialVersionUID = 1L;
                  
                @Override
                public Page createPage() {
                    return new AddRolePage(modalWindow, allRolenames);
                }
            })
        );
    
        add(new ModalWindowLink("toggle-role-form-link", modalWindow, 
                new Integer(new ResourceModel("rolelist.add-role.window.width").wrapOnAssignment(this).getObject().toString()).intValue(), 
                new Integer(new ResourceModel("rolelist.add-role.window.height").wrapOnAssignment(this).getObject().toString()).intValue()
        )
        .add(new Image("toggle-role-form-image", ImageManager.IMAGE_USER_ROLE_ADD)
        .add(new ImageSizeBehaviour("vertical-align: middle;")))
        .add(new Label("rolelist.add-role-form.title", new ResourceModel("rolelist.add-role-form.title")))
        .add(new TooltipBehaviour("rolelist."))
        );
    }

    @Override
    protected void onBeforeRender() {
        super.onBeforeRender();
        
        RepeatingView roleRows = new RepeatingView("role-rows");
        addOrReplace(roleRows);
        
        for (int i = 0; i < this.allRolenames.getObject().size(); i++) {
            final Role role = this.allRolenames.getObject().get(i);
            
            
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
                    .setVisible(!JNDIUtils.lookupAndInit(UserAccess.JNDI_NAME, serviceObjectName).getUserRoleName().equals(role.getRolename())
                            && !JNDIUtils.lookupAndInit(UserAccess.JNDI_NAME, serviceObjectName).getAdminRoleName().equals(role.getRolename()))
                .add(new AttributeModifier("class", true, new Model<String>(CSSUtils.getRowClass(i)))));
        }
    }

    private ArrayList<Role> getAllRolenames() {
        ArrayList<Role> allRolenames = new ArrayList<Role>(2);
        allRolenames.addAll(JNDIUtils.lookupAndInit(UserAccess.JNDI_NAME, serviceObjectName).getAllRolenames());
        return allRolenames;
    }    
}
