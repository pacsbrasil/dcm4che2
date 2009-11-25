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
 ***** END LICENSE BLOCK ***** */

package org.dcm4chee.web.wicket.common;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.IBehavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Franz Willer <franz.willer@gmail.com>
 * @version $Revision$ $Date$
 * @since Oct 31, 2009
 */

@SuppressWarnings("unchecked")
public class BaseForm extends Form {

    private static final long serialVersionUID = 0L;
    private static final String LABEL_ID_EXTENSION = "Label";

    private static Logger log = LoggerFactory.getLogger(BaseForm.class);
    
    private String resourceIdPrefix;
    private TooltipBehaviour tooltipBehaviour;

    @SuppressWarnings("unchecked")
    private IVisitor visitor = new FormVisitor();
    
    MarkInvalidBehaviour markInvalidBehaviour = new MarkInvalidBehaviour();
    
    public BaseForm(String id) {
        super(id);
    }
    @SuppressWarnings("unchecked")
    public BaseForm(String id, IModel model) {
        super(id, model);
    }

    public void setResourceIdPrefix(String resourceIdPrefix) {
        this.resourceIdPrefix = resourceIdPrefix;
    }
    public void setTooltipBehaviour(TooltipBehaviour tooltip) {
        tooltipBehaviour = tooltip;
    }
    
    @SuppressWarnings("unchecked")
    public void onBeforeRender() {
        super.onBeforeRender();
        visitChildren(visitor);
    }
    
    /**
     * Add a Label and a TextField with text from ResourceModel.
     * <p>
     * The text for the label is defined in the ResourceModel with key &lt;module&gt;.&lt;id&gt;Label<br/>
     * The TextField use a CompoundPropertyModel with given <code>id</code><br/>
     * 
     * @param id        Id of TextField.
     * 
     * @return The TextField (to allow adding Validators,.. )
     */
    public TextField addLabeledTextField(String id) {
        TextField tf = new TextField(id);
        addLabel(id);
        add(tf);
        return tf;
    }

    public DropDownChoice addLabeledDropDownChoice(String id, IModel model, List<String> values) {
        DropDownChoice ch = model == null ? new DropDownChoice(id, values) :
                                            new DropDownChoice(id, model, values);
        addLabel(id);
        add(ch);
        return ch;
    }
    
    public CheckBox addLabeledCheckBox(String id, IModel model) {
        CheckBox chk = model == null ? new CheckBox(id) :
                                            new CheckBox(id, model);
        addLabel(id);
        add(chk);
        return chk;
    }
    
    public Label addLabel(String id) {
        String labelId = id+LABEL_ID_EXTENSION;
        Label l = new Label(labelId, new ResourceModel(toResourcekey(id)));
        add(l);
        return l;
    }
    private String toResourcekey(String id) {
        return resourceIdPrefix == null ? id : resourceIdPrefix+id;
    }

    
    
    public static void addInvalidComponentsToAjaxRequestTarget(
            AjaxRequestTarget target, Form form) {
        Component c;
        for ( Iterator<Component> it = (Iterator<Component>) form.iterator() ; it.hasNext() ;) {
            c = it.next();
            if ( c instanceof FormComponent ) {
                FormComponent fc = (FormComponent) c;
                if ( !fc.isValid() )
                    target.addComponent(fc);
            }
        }
    }

    @SuppressWarnings("unchecked")
    class FormVisitor implements IVisitor, Serializable {
        private static final long serialVersionUID = 0L;

        Set<Component> visited = new HashSet<Component>();

        public Object component(Component c) {
            if (!visited.contains(c)) {
                visited.add(c);
                if ( tooltipBehaviour != null && componentHasNoTooltip(c))
                    c.add(tooltipBehaviour);
                if (c instanceof FormComponent)
                    c.add(markInvalidBehaviour);
            }
            return IVisitor.CONTINUE_TRAVERSAL;
        }
    }

    public boolean componentHasNoTooltip(Component c) {
        for ( IBehavior b : c.getBehaviors() ) {
            if ( b instanceof TooltipBehaviour )
                return false;
        }
        return true;
    }

}

