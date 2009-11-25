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

package org.dcm4chee.web.wicket.common;

import java.lang.reflect.Field;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.internal.HtmlHeaderContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.ResourceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Franz Willer <franz.willer@gmail.com>
 * @version $Revision$ $Date$
 * @since Nov 02, 2009
 */
public class MessageWindow extends ModalWindow {
    public static final String TITLE_INFO="msgwindow.title.info";
    public static final String TITLE_WARNING="msgwindow.title.warn";
    public static final String TITLE_ERROR="msgwindow.title.error";
    
    private static final long serialVersionUID = 0L;
    private boolean renderScript;
    private transient Field showField;
    private String msg;

    private static Logger log = LoggerFactory.getLogger(MessageWindow.class);

    public MessageWindow(String id) {
        super(id);
        setInitialWidth(300);
        setInitialHeight(200);
        setTitle(new ResourceModel(TITLE_INFO,"MessageWindow"));
        setContent(new MessageWindowPanel("content"));
        add(new AutoOpenBehaviour());
    }
    
    public void setMessage(String msg) {
        this.msg = msg;
    }
    public void setInfoMessage(String msg) {
        this.msg = msg;
        setTitle(new ResourceModel(TITLE_INFO,"MessageWindow"));
    }
    public void setWarningMessage(String msg) {
        this.msg = msg;
        setTitle(new ResourceModel(TITLE_WARNING,"MessageWindow"));
    }
    public void setErrorMessage(String msg) {
        this.msg = msg;
        setTitle(new ResourceModel(TITLE_ERROR,"MessageWindow"));
    }
    
    public void renderHead( HtmlHeaderContainer container ) {
        super.renderHead(container);
        if (msg != null && !isShown()) {
            Component c = this.get("content:close");
            container.getHeaderResponse().renderOnLoadJavascript("self.focus();var elem=document.getElementById('"+
                    c.getMarkupId() + "');elem.focus()");
        }
    }

    @Override
    protected void onBeforeRender() {
        if (msg != null && !isShown()) {
            renderScript = true;
            show();
            super.onBeforeRender();
            show();// super.shown is set to false for none Ajax requests!
        } else {
            super.onBeforeRender();
        }
    }
    
    @Override
    public void show(final AjaxRequestTarget target) {
        super.show(target);
        target.focusComponent(this.get("content:close"));
    }

    private void show() {
        try {
            if (showField == null) {
                try {
                    showField = ModalWindow.class.getDeclaredField("shown");
                    showField.setAccessible(true);
                } catch (Exception e) {
                    log.error("Failed to initialize shown Field from ModalWindow!");
                }
            }
            showField.set(this, true);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
    
    
    public void show(AjaxRequestTarget target, String msg){
        this.msg = msg;
        show(target);
    }

    public class MessageWindowPanel extends Panel {
        private static final long serialVersionUID = 0L;

        public MessageWindowPanel(String id) {
            super(id);
            add(new Label("msg", new AbstractReadOnlyModel<String>(){
                @Override
                public String getObject() {
                    return msg;
                }
            }));
            add(new AjaxFallbackLink("close", new ResourceModel("closeBtn")){
                @Override
                public void onClick(AjaxRequestTarget target) {
                    close(target);
                }
            }.add(new Label("closeLabel", new ResourceModel("closeBtn"))) );
        }

        /**
         * Return always true because ModalWindow.beforeRender set visibility of content to false!
         */
        @Override
        public boolean isVisible() {
            return true;
        }
        @Override
        protected void onAfterRender() {
            msg = null;
            super.onAfterRender();
        }
        
    }   
    
    private class AutoOpenBehaviour extends AbstractBehavior implements IHeaderContributor {
        @Override
        public void renderHead(IHeaderResponse response) {
            if (renderScript) {
                try {
                    String script = getWindowOpenJavascript();
                    response.renderOnDomReadyJavascript(script);
                } catch (Exception e) {
                    log.error("Error render Header with 'WindowOpenJavascript'");
                }
                renderScript = false;
            }
         }
        
    }

}
