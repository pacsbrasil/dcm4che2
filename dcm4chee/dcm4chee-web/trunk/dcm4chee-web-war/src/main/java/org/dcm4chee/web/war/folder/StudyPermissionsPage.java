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

package org.dcm4chee.web.war.folder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Page;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.resources.CompressedResourceReference;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.security.components.SecureWebPage;
import org.dcm4che2.data.DicomObject;
import org.dcm4chee.archive.entity.StudyPermission;
import org.dcm4chee.icons.ImageManager;
import org.dcm4chee.icons.behaviours.ImageSizeBehaviour;
import org.dcm4chee.usr.dao.UserAccess;
import org.dcm4chee.usr.entity.Role;
import org.dcm4chee.usr.ui.util.CSSUtils;
import org.dcm4chee.usr.util.JNDIUtils;
import org.dcm4chee.web.common.base.BaseWicketApplication;
import org.dcm4chee.web.common.base.BaseWicketPage;
import org.dcm4chee.web.common.behaviours.TooltipBehaviour;
import org.dcm4chee.web.common.markup.ModalWindowLink;
import org.dcm4chee.web.common.secure.SecureSession;
import org.dcm4chee.web.common.secure.SecurityBehavior;
import org.dcm4chee.web.dao.folder.StudyPermissionsLocal;
import org.dcm4chee.web.war.common.model.AbstractEditableDicomModel;
import org.dcm4chee.web.war.folder.model.PatientModel;
import org.dcm4chee.web.war.folder.model.StudyModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Robert David <robert.david@agfa.com>
 * @version $Revision$ $Date$
 * @since 21.07.2010
 */
public class StudyPermissionsPage extends SecureWebPage {
    
    private static final long serialVersionUID = 1L;

    private static final ResourceReference BaseCSS = new CompressedResourceReference(BaseWicketPage.class, "base-style.css");
    
    private static Logger log = LoggerFactory.getLogger(StudyPermissionsPage.class);

    private AbstractEditableDicomModel model;
    
    private boolean forPatient = false;
    private long studyCountForPatient = -1;
   
List<StudyPermission> currentStudyPermissions;
    
    public StudyPermissionsPage(final ModalWindow modalWindow, AbstractEditableDicomModel model) {
        super();

        this.model = model;

        if (StudyPermissionsPage.BaseCSS != null)
            add(CSSPackageResource.getHeaderContribution(StudyPermissionsPage.BaseCSS));

        if (model instanceof org.dcm4chee.web.war.folder.model.PatientModel) forPatient = true;
        else if (!(model instanceof org.dcm4chee.web.war.folder.model.PatientModel)) 
            log.error(this.getClass() + ": No valid model for StudyPermission assignment");
        
        add(new WebMarkupContainer("studyPermissions-patient").setVisible(forPatient));
        add(new WebMarkupContainer("studyPermissions-study").setVisible(!forPatient));
        
        add(new Label("for-description", (forPatient ? 
                new StringResourceModel("folder.studyPermissions.description.patient", this, null,new Object[]{((PatientModel) model).getName()}) : 
                    new StringResourceModel("folder.studyPermissions.description.study", this, null,new Object[]{((StudyModel) model).getStudyInstanceUID()})
                )
            )
        );

        StudyPermissionsLocal dao = (StudyPermissionsLocal) JNDIUtils.lookup(StudyPermissionsLocal.JNDI_NAME);
        if (forPatient) {
            long pk = ((PatientModel) model).getPk();
            currentStudyPermissions = dao.getStudyPermissionsForPatient(pk);
            studyCountForPatient = dao.countStudiesOfPatient(pk);
        } else 
            currentStudyPermissions = dao.getStudyPermissions(((StudyModel) model).getStudyInstanceUID());            

        Set<String> studyPermissionActions = new LinkedHashSet<String>();
        studyPermissionActions.add(StudyPermission.APPEND_ACTION);
        studyPermissionActions.add(StudyPermission.DELETE_ACTION);
        studyPermissionActions.add(StudyPermission.EXPORT_ACTION);
        studyPermissionActions.add(StudyPermission.QUERY_ACTION);
        studyPermissionActions.add(StudyPermission.READ_ACTION);
        studyPermissionActions.add(StudyPermission.UPDATE_ACTION);
        
        RepeatingView actionHeaders = new RepeatingView("action-headers");
        Iterator<String> iterator = studyPermissionActions.iterator();
        while (iterator.hasNext()) {
            String studyPermission = iterator.next();
            actionHeaders.add(new Label(actionHeaders.newChildId(), new ResourceModel("studypermission.action." + studyPermission))
            .add(new AttributeModifier("title", true, new ResourceModel("studypermission.action." + studyPermission))));
        }
        add(actionHeaders);
        
        RepeatingView roleRows = new RepeatingView("role-rows");
        add(roleRows);

        for (final Role role : getAllRolenames()) {
            WebMarkupContainer rowParent;
            roleRows.add((rowParent = new WebMarkupContainer(roleRows.newChildId()))
                    .add(new Label("rolename", role.getRolename())
                    .add(new AttributeModifier("title", true, new Model<String>(role.getDescription()))))
            );
            rowParent.add(new Label("type", role.getType()));
            rowParent.add(new AttributeModifier("style", true, new Model<String>("background-color: " + role.getColor())));

            RepeatingView actionDividers = new RepeatingView("action-dividers");
            rowParent.add(actionDividers);

            iterator = studyPermissionActions.iterator();
            while (iterator.hasNext()) {
                final String action = iterator.next();
                final Label countLabel = new Label("number-of-studies-label", new Model<String>(forPatient ? (countStudies(role, action) + "/" + studyCountForPatient) : ""));
                AjaxCheckBox roleCheckbox = new AjaxCheckBox("study-permission-checkbox", new HasStudyPermissionModel(role, action)) {

                    private static final long serialVersionUID = 1L;

                    @Override
                    protected void onUpdate(AjaxRequestTarget target) {
                        target.addComponent(this);
                        target.addComponent(countLabel.setDefaultModel(new Model<String>(forPatient ? (countStudies(role, action) + "/" + studyCountForPatient) : "")));
                    }
                      
                    @Override
                    protected void onComponentTag(ComponentTag tag) {
                        super.onComponentTag(tag);
                        tag.put("title", new ResourceModel(((HasStudyPermissionModel) this.getModel()).getObject().booleanValue() ? "studypermission.has-study-permission-checkbox.add.tooltip" : "studypermission.has-study-permission-checkbox.add.tooltip").wrapOnAssignment(this).getObject());
                    }
                };
                actionDividers.add(
                        new WebMarkupContainer(roleRows.newChildId())
                        .add(roleCheckbox)
                        .add(countLabel
                            .setOutputMarkupId(true)
                            .setVisible(forPatient)
                        )
                );
                roleCheckbox.add(new SecurityBehavior(getModuleName() + ":changeStudyPermissionAssignmentCheckbox"));
            }
        }
    }

    private int countStudies(Role role, String action) {
        int count = 0;
        for (StudyPermission sp : currentStudyPermissions) 
            if (sp.getRole().equals(role.getRolename()) && sp.getAction().equals(action)) 
                count++;
        return count;
    }

    private final class HasStudyPermissionModel implements IModel<Boolean> {
        
        private static final long serialVersionUID = 1L;

        private Role role;
        private String action;
        
        public HasStudyPermissionModel(Role role, String action) {
            this.role = role;
            this.action = action;
        }
        
        @Override
        public Boolean getObject() {
            return countStudies(role, action) > 0;
        }
        
        @Override
        public void setObject(Boolean hasStudyPermission) {
            if (forPatient) {
                StudyPermissionsLocal dao = (StudyPermissionsLocal) JNDIUtils.lookup(StudyPermissionsLocal.JNDI_NAME);
                long pk = ((PatientModel) model).getPk();
                if (hasStudyPermission) 
                    dao.grantForPatient(pk, action, role.getRolename());
                else
                    dao.revokeForPatient(pk, action, role.getRolename());
                currentStudyPermissions = dao.getStudyPermissionsForPatient(pk);
            } else {
                if (hasStudyPermission) {
                    StudyPermission sp = new StudyPermission();
                    sp.setStudyInstanceUID(((StudyModel) model).getStudyInstanceUID());
                    sp.setRole(role.getRolename());
                    sp.setAction(action);
                    ((StudyPermissionsLocal) JNDIUtils.lookup(StudyPermissionsLocal.JNDI_NAME)).grant(sp);
                    currentStudyPermissions.add(sp);
                } else {
                    for (StudyPermission sp : currentStudyPermissions) 
                        if (sp.getRole().equals(role.getRolename()) && sp.getAction().equals(action)) {
                            ((StudyPermissionsLocal) JNDIUtils.lookup(StudyPermissionsLocal.JNDI_NAME)).revoke(sp.getPk());
                            currentStudyPermissions.remove(sp);
                            return;
                        }
                }
            }
        }
        
        @Override
        public void detach() {
        }
    }

    private ArrayList<Role> getAllRolenames() {
        ArrayList<Role> allRolenames = new ArrayList<Role>(2);
        allRolenames.addAll(JNDIUtils.lookupAndInit(UserAccess.JNDI_NAME, ((BaseWicketApplication) getApplication()).getInitParameter("UserAccessServiceName")).getAllRolenames());
        return allRolenames;
    }
    
    public static String getModuleName() {
        return "studypermissions";
    }
}
