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
 * Accurate Software Design, LLC.
 * Portions created by the Initial Developer are Copyright (C) 2006-2008
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

import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;

/**
 * @author Franz Willer <franz.willer@gmail.com>
 * @version $Revision$ $Date$
 * @since June 18, 2009
 */
public class ComponentUtil {

    private String module;

    private static final String LABEL_ID_EXTENSION = "Label";
    private static final String DESCRIPTION_PROP_EXTENSION = ".descr";
    
    /**
     * Creates a new Utility Instance for a <code>module</code>.
     * <p>
     * The <code>module</code> String will be used to format wicket- and resource IDs.
     *  
     * @param module
     */
    public ComponentUtil( String module ) {
        this.module = module;
    }
    
    public Label addLabel(MarkupContainer c, String id) {
        Label l = new Label(id+LABEL_ID_EXTENSION, new ResourceModel(toResourcekey(id)));
        c.add(l);
        return l;
    }
    
    public TextField addLabeledTextField(MarkupContainer c, String id) {
        return addLabeledTextField(c, id, id);
    }
    
    /**
     * Add a Label and a TextField to the given markupContainer with text from ResourceModel.
     * <p>
     * The text for the label is defined in the ResourceModel with key &lt;module&gt;.&lt;labelId&gt;.<br/>
     * The TextField use a CompoundPropertyModel with given <code>id</code><br/>
     * A title (description) is set to the TextField when a property with key &lt;module&gt;.&lt;labelId&gt;.descr is set.
     * 
     * 
     * @param c         MarkupContainer where Label and TextField should be added.
     * @param id        Id of TextField.
     * @param labelId   Id of Label (format: '&lt;labelId&gt;Label')
     * 
     * @return The TextField (to allow adding Validators,.. )
     */
    public TextField addLabeledTextField(MarkupContainer c, String id, String labelId) {
        addLabel(c, labelId);
        TextField tf = new TextField(id);
        c.add(tf);
        addDescription(tf, labelId);
        return tf;
    }

    public DropDownChoice addLabeledDropDownChoice(MarkupContainer c, String id, IModel model, List<String> values) {
        addLabel(c,id);
        DropDownChoice ch = model == null ? new DropDownChoice(id, values) :
                                            new DropDownChoice(id, model, values);
        c.add(ch);
        addDescription(ch, id);
        return ch;
    }
    
    public CheckBox addLabeledCheckBox(MarkupContainer c, String id, IModel model) {
        addLabel(c,id);
        CheckBox chk = model == null ? new CheckBox(id) :
                                            new CheckBox(id, model);
        c.add(chk);
        addDescription(chk, id);
        return chk;
    }

    public Component addDescription(Component c) {
        return addDescription(c, c.getId());
    }
    public Component addDescription(Component c, String key) {
        c.add( new AttributeModifier("title", true, 
                new ResourceModel(toResourcekey(key)+DESCRIPTION_PROP_EXTENSION, "")));
        return c;
    }
    
    public String toResourcekey(String id) {
        return module == null ? id : module+"."+id;
    }
}
