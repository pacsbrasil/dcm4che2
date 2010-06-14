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
 * Portions created by the Initial Developer are Copyright (C) 2008
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

package org.dcm4chee.web.common.markup.modal;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.dcm4chee.icons.ImageManager;

/**
 * @author Franz Willer <franz.willer@gmail.com>
 * @version $Revision$ $Date$
 * @since Dec 11, 2009
 */
public abstract class ConfirmationWindow<T> extends AutoOpenModalWindow {
    
    private static final long serialVersionUID = 1L;
    
    public static final String FOCUS_ON_CONFIRM = "content:confirm";
    public static final String FOCUS_ON_DECLINE = "content:decline";
    public static final String FOCUS_ON_CANCEL = "content:cancel";

    private T userObject;
    private String focusElementId;
    private IModel<?> msg, confirm, decline, cancel;
    
    protected boolean hasStatus;
    private boolean showCancel = false;
    
    protected boolean ajaxRunning = false;
    protected Label msgLabel;
    protected Image hourglassImage;
    protected AjaxFallbackLink<Object> okBtn;

    public ConfirmationWindow(String id) {
        this(id, new ResourceModel("yesBtn"), new ResourceModel("noBtn"), new ResourceModel("cancelBtn"));
        
        setCloseButtonCallback(new CloseButtonCallback() {

            private static final long serialVersionUID = 1L;

            public boolean onCloseButtonClicked(AjaxRequestTarget target) {
                return !ajaxRunning;
            }
        });
    }
    
    public ConfirmationWindow(String id, IModel<?> confirm, IModel<?> decline, IModel<?> cancel) {
        super(id);
        this.confirm = confirm;
        this.decline = decline;
        this.cancel = cancel;
        initContent();
    }

    protected void initContent() {
        setInitialWidth(400);
        setInitialHeight(300);
        setTitle("ConfirmationWindow");
        setContent(new MessageWindowPanel("content"));
    }
    
    public abstract void onConfirmation(AjaxRequestTarget target, T userObject);
    public void onDecline(AjaxRequestTarget target, T userObject){}
    public void onCancel(AjaxRequestTarget target, T userObject){}
    public void onOk(AjaxRequestTarget target) {}
    
    @Override
    public void show(final AjaxRequestTarget target) {
        hasStatus = false;
        super.show(target);
        if (focusElementId != null)
            target.focusComponent(this.get(focusElementId));
    }
    
    public void confirm(AjaxRequestTarget target, IModel<?> msg, T userObject) {
        confirm(target, msg, userObject, FOCUS_ON_DECLINE);
    }
    public void confirm(AjaxRequestTarget target, IModel<?> msg, T userObject, String focusElementId) {
        confirm(target, msg, userObject, focusElementId, false);
    }
    public void confirm(AjaxRequestTarget target, IModel<?> msg, T userObject, String focusElementId, boolean showCancel){
        this.msg = msg;
        this.userObject = userObject;
        this.focusElementId = focusElementId;
        this.showCancel = showCancel;
        show(target);
    }

    public void confirmWithCancel(AjaxRequestTarget target, IModel<?> msg, T userObject) {
        confirm(target, msg, userObject, FOCUS_ON_CANCEL, true);
    }
    
    public void setStatus(IModel<?> statusMsg) {
        msg = statusMsg;
        hasStatus = true;
    }

    @Override
    protected boolean needAutoOpen() {
        return msg != null;
    }

    public class MessageWindowPanel extends Panel {
        private static final long serialVersionUID = 1L;

        public MessageWindowPanel(String id) {
            super(id);
            
            add((hourglassImage = new Image("hourglass-image", ImageManager.IMAGE_COMMON_AJAXLOAD) {

                private static final long serialVersionUID = 1L;

                @Override
                public boolean isVisible() {
                    return ajaxRunning;
                }
            }).setOutputMarkupId(true));

            add((msgLabel = new Label("msg", new AbstractReadOnlyModel<Object>() {

                private static final long serialVersionUID = 1L;

                @Override
                public Object getObject() {
                    return msg == null ? null : msg.getObject();
                }
            })).setOutputMarkupId(true));
            
            AjaxFallbackLink<Object> confirmBtn = new AjaxFallbackLink<Object>("confirm"){

                private static final long serialVersionUID = 1L;

                @Override
                public void onClick(AjaxRequestTarget target) {
                    onConfirmation(target, userObject);
                    if (hasStatus) {
                        target.addComponent(MessageWindowPanel.this);
                    } else {
                        msg = null;
                        close(target);
                    }
                }
                @Override
                public boolean isVisible() {
                    return !hasStatus;
                }
            };
            confirmBtn.add(new Label("confirmLabel", confirm));
            confirmBtn.setOutputMarkupId(true);
            add(confirmBtn);
            
            add(new AjaxFallbackLink<Object>("decline"){

                private static final long serialVersionUID = 1L;

                @Override
                public void onClick(AjaxRequestTarget target) {
                    onDecline(target, userObject);
                    if (hasStatus) {
                        target.addComponent(MessageWindowPanel.this);
                    } else {
                        msg = null;
                        close(target);
                    }
                }
                @Override
                public boolean isVisible() {
                    return !hasStatus;
                }
            }.add(new Label("declineLabel", decline)) );
            
            add(new AjaxFallbackLink<Object>("cancel"){
                private static final long serialVersionUID = 1L;

                @Override
                public void onClick(AjaxRequestTarget target) {
                    onCancel(target, userObject);
                    msg = null;
                    close(target);
                }
                @Override
                public boolean isVisible() {
                    return !hasStatus && showCancel;
                }
            }.add(new Label("cancelLabel", cancel)) );
            
            add(okBtn = new AjaxFallbackLink<Object>("ok") {

                private static final long serialVersionUID = 1L;

                @Override
                public void onClick(AjaxRequestTarget target) {
                    onOk(target);
                    msg = null;
                    close(target);
                }
                
                @Override
                public boolean isVisible() {
                    return ajaxRunning ? false : hasStatus;
                }
            });
            okBtn.add(new Label("okLabel", new ResourceModel("okBtn")));
            okBtn.setOutputMarkupId(true);     
            okBtn.setOutputMarkupPlaceholderTag(true);
            this.setOutputMarkupId(true);
        }
        
        /**
         * Return always true because ModalWindow.beforeRender set visibility of content to false!
         */
        @Override
        public boolean isVisible() {
            return true;
        }
    }
}
