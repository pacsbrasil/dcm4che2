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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.resources.CompressedResourceReference;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.security.components.SecureWebPage;
import org.dcm4chee.usr.dao.UserAccess;
import org.dcm4chee.usr.model.Group;
import org.dcm4chee.usr.model.Role;
import org.dcm4chee.usr.ui.config.delegate.UsrCfgDelegate;
import org.dcm4chee.usr.ui.util.CSSUtils;
import org.dcm4chee.usr.util.JNDIUtils;
import org.dcm4chee.web.common.base.BaseWicketPage;
import org.dcm4chee.web.common.markup.modal.ConfirmationWindow;
import org.dcm4chee.web.common.secure.SecureSession;
import org.dcm4chee.web.common.secure.SecurityBehavior;
/**
 * @author Robert David <robert.david@agfa.com>
 * @version $Revision$ $Date$
 * @since Jan. 31, 2011
 */
public class RoleTypePage extends SecureWebPage {

    private static final long serialVersionUID = 1L;
    
    private static final ResourceReference BaseCSS = new CompressedResourceReference(BaseWicketPage.class, "base-style.css");
    
    UserAccess userAccess;
    private Role role;
    
    public RoleTypePage(final ModalWindow modalWindow, Role role) {
        super();

        if (RoleTypePage.BaseCSS != null)
            add(CSSPackageResource.getHeaderContribution(RoleTypePage.BaseCSS));

        userAccess = (UserAccess) JNDIUtils.lookup(UserAccess.JNDI_NAME);        
        this.role = role;
        setOutputMarkupId(true);
    }

    @Override
    protected void onBeforeRender() {
        super.onBeforeRender();
        
        addOrReplace(new Label("rolename", role.getRolename())
            .add(new AttributeModifier("title", true, new Model<String>(role.getDescription()))
            )
        );

        RepeatingView typeRows = new RepeatingView("type-rows");
        addOrReplace(typeRows);
        
        int i = 0;
        List<String> roleTypes = UsrCfgDelegate.getInstance().getRoleTypes();
        List<String> roleRoleTypes = role.getRoleTypes();
        int knownRoleTypes = roleTypes.size();
        for (int j = 0, len =roleRoleTypes.size(); j < len ; j++) {
            if (!roleTypes.contains(roleRoleTypes.get(j))) {
                roleTypes.add(roleRoleTypes.get(j));
            }
        }
        for (String type : roleTypes) {
            WebMarkupContainer rowParent;            
            typeRows.add((rowParent = new WebMarkupContainer(typeRows.newChildId()))
                    .add(new Label("typename", i < knownRoleTypes ? type : type+" (retired)")
                    )
            );
            rowParent.add(new AttributeModifier("class", true, new Model<String>(CSSUtils.getRowClass(i++))));

            AjaxCheckBox typeCheckbox = new AjaxCheckBox("type-checkbox", new HasTypeModel(role, type)) {

                private static final long serialVersionUID = 1L;

                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                    target.addComponent(this);
                }
                  
                @Override
                protected void onComponentTag(ComponentTag tag) {
                    super.onComponentTag(tag);
                    tag.put("title", new ResourceModel(((HasTypeModel) this.getModel()).getObject().booleanValue() ? "roleTypes.has-type-checkbox.remove.tooltip" : "roleTypes.has-type-checkbox.add.tooltip").wrapOnAssignment(this).getObject());
                }
            };
            typeCheckbox.add(new SecurityBehavior(getModuleName() + ":changeTypeAssignmentCheckbox"));
            typeRows.add(rowParent.add(typeCheckbox));
        }
    }
    
    private final class HasTypeModel implements IModel<Boolean> {
        
        private static final long serialVersionUID = 1L;
        
        private Role role;
        private String typename;
        
        public HasTypeModel(Role role, String type) {
            this.role = role;
            this.typename = type;
            if (role.getRoleTypes() == null)
                role.setRoleTypes(new ArrayList<String>());
        }
        
        @Override
        public Boolean getObject() {
            return role.getRoleTypes().contains(typename);
        }
        
        @Override
        public void setObject(Boolean hasType) {
            List<String> currentRoleTypes = role.getRoleTypes();           
            if (hasType)
                currentRoleTypes.add(typename);
            else 
                currentRoleTypes.remove(typename);
            role.setRoleTypes(currentRoleTypes);
            userAccess.updateRole(role);
        }

        @Override
        public void detach() {
        }
    }
    
    public static String getModuleName() {
        return "roletypes";
    }
}
