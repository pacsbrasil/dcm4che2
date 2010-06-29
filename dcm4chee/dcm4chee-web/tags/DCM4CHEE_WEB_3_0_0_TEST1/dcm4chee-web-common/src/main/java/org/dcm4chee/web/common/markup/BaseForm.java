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

package org.dcm4chee.web.common.markup;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.IBehavior;
import org.apache.wicket.datetime.StyleDateConverter;
import org.apache.wicket.datetime.markup.html.form.DateTextField;
import org.apache.wicket.extensions.yui.calendar.DateField;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.AbstractTextComponent.ITextFormatProvider;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.validator.StringValidator;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.VR;
import org.dcm4chee.web.common.behaviours.MarkInvalidBehaviour;
import org.dcm4chee.web.common.behaviours.TooltipBehaviour;
import org.dcm4chee.web.common.model.DicomElementModel;
import org.dcm4chee.web.common.util.DateUtils;
import org.dcm4chee.web.common.validators.UIDValidator;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;


/**
 * @author Franz Willer <franz.willer@gmail.com>
 * @version $Revision$ $Date$
 * @since Oct 31, 2009
 */

public class BaseForm extends Form<Object> {

    private static final long serialVersionUID = 0L;
    public static final String LABEL_ID_EXTENSION = ".label";

    private String resourceIdPrefix;
    private WebMarkupContainer parent;
    private boolean rendered;
    
    MarkInvalidBehaviour markInvalidBehaviour = new MarkInvalidBehaviour();
        
    public BaseForm(String id) {
        super(id);
    }
    
    public BaseForm(String id, IModel<Object> model) {
        super(id, model);
    }

    public void setResourceIdPrefix(String resourceIdPrefix) {
        this.resourceIdPrefix = resourceIdPrefix;
    }

    @Override
    protected void onBeforeRender() {
        super.onBeforeRender();
        if (!rendered) {
            visitChildren(new FormVisitor());
            rendered = true;
        }
    }

    public MarkupContainer setParent(WebMarkupContainer parent) {
        this.parent = parent;
        if (parent != null) 
            super.add(parent);
        return this;
    }
    
    public MarkupContainer addComponent(Component child) {
        if (parent == null)
            super.add(child);
        else
            parent.add(child);
        return this;
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
    public TextField<String> addLabeledTextField(String id) {
        TextField<String> tf = new TextField<String>(id);
        addInternalLabel(id);
        this.addComponent(tf);
        return tf;
    }

    public TextField<Integer> addLabeledNumberTextField(String id) {
        TextField<Integer> tf = new TextField<Integer>(id);
        addInternalLabel(id);
        this.addComponent(tf);
        return tf;
    }

    public TextField<String> addTextField(String id, final IModel<Boolean> enabledModel, boolean addLabel) {
        TextField<String> tf = new TextField<String>(id) {

            private static final long serialVersionUID = 1L;

            @Override
            public boolean isEnabled() {
                return enabledModel == null ? true : enabledModel.getObject();
            }
        };
        if (addLabel) 
            addInternalLabel(id);
        addComponent(tf);
        return tf;
    }

    public SimpleDateTimeField addDateTimeField(String id, IModel<Date> model, final IModel<Boolean> enabledModel, final boolean max, boolean addLabel) {
        SimpleDateTimeField dtf = getSimpleDateTimeField(id, model, enabledModel, max);
        if (addLabel) 
            addInternalLabel(id);
        return dtf;
    }

    public SimpleDateTimeField getSimpleDateTimeField(String id, IModel<Date> model,
            final IModel<Boolean> enabledModel, final boolean max) {
        final SimpleDateTimeField dtf = new SimpleDateTimeField(id, model) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isEnabled() {
                return enabledModel == null ? true : enabledModel.getObject();
            }
        };
        addComponent(dtf);
        dtf.add(new TooltipBehaviour(generateResourcePrefix(dtf), id, 
                new AbstractReadOnlyModel<String>(){
                    private static final long serialVersionUID = 1L;

                    @Override
                    public String getObject() {
                        return DateUtils.getDatePattern(dtf);
                    }
                }
        ));
        return dtf;
    }
    
    public DateTextField getDateTextField(String id, IModel<Date> model, String tooltipPrefix, final IModel<Boolean> enabledModel) {
        DateTextField dt = new DateTextField(id, model,  
                new StyleDateConverter("S-", false) {

                    private static final long serialVersionUID = 1L;

                    @Override
                    protected DateTimeFormatter getFormat() {
                        String pattern = DateUtils.getDatePattern(getComponent());
                        return DateTimeFormat.forPattern(pattern).withLocale(getLocale())
                                        .withPivotYear(2000);
                    }                    
            }) {
                private static final long serialVersionUID = 1L;

            @Override
            public boolean isEnabled() {
                return enabledModel == null ? true : enabledModel.getObject();
            }
        };
        if (tooltipPrefix != null) 
            dt.add(new TooltipBehaviour((resourceIdPrefix != null ? resourceIdPrefix : "") + tooltipPrefix, id, new PropertyModel<Object>(dt,"textFormat")));
        dt.add(markInvalidBehaviour);
        return dt;
    }
    
    public DateField getDateField(String id, IModel<Date> model, final String tooltipPrefix) {
        DateField dt = new DateField(id, model){
            private static final long serialVersionUID = 1L;

            @Override
            public void onComponentTag(ComponentTag tag) {
                super.onComponentTag(tag);
                if ( tag.getAttribute("class") == null )
                    tag.put("class", "dateField");
            }
            
            @SuppressWarnings("unchecked")
            @Override
            protected DateTextField newDateTextField(String id, PropertyModel dateFieldModel) {
                return getDateTextField(id, dateFieldModel, tooltipPrefix, new Model<Boolean>(true));
            }            
        };
        if (tooltipPrefix != null)
            dt.add(new TooltipBehaviour((resourceIdPrefix != null ? resourceIdPrefix : "") + tooltipPrefix, id, new PropertyModel<Object>(dt,"textFormat")));
        return dt;
    }

    public DropDownChoice<?> addLabeledDropDownChoice(String id, IModel<Object> model, List<String> values) {
        DropDownChoice<?> ch = model == null ? new DropDownChoice<Object>(id, values) :
                                            new DropDownChoice<Object>(id, model, values);
        addInternalLabel(id);
        addComponent(ch);
        return ch;
    }

    public DropDownChoice<?> addDropDownChoice(String id, IModel<Object> model, List<String> values, final IModel<Boolean> enabledModel, boolean addLabel) {
        DropDownChoice<?> ch = model == null ? new DropDownChoice<Object>(id, values)  {

                                                private static final long serialVersionUID = 1L;
                                    
                                                @Override
                                                public boolean isEnabled() {
                                                    return enabledModel == null ? true : enabledModel.getObject();
                                                }
                                            } : 
                                            new DropDownChoice<Object>(id, model, values) {

                                                private static final long serialVersionUID = 1L;

                                                @Override
                                                public boolean isEnabled() {
                                                    return enabledModel == null ? true : enabledModel.getObject();
                                                }
                                            };
        if (addLabel) 
            addInternalLabel(id);
        addComponent(ch);
        return ch;
    }

    public CheckBox addLabeledCheckBox(String id, IModel<Boolean> model) {
        CheckBox chk = model == null ? new CheckBox(id) :
                                            new CheckBox(id, model);
        addInternalLabel(id);
        addComponent(chk);
        return chk;
    }
    
    public Label addInternalLabel(String id) {
        String labelId = id + LABEL_ID_EXTENSION;
        return addLabel(labelId);
    }
    
    public Label addLabel(String id) {
        Label l = new Label(id, new ResourceModel(toResourceKey(id)));
        addComponent(l);
        return l;
    }

    private String toResourceKey(String id) {
        StringBuffer resourceKey = new StringBuffer(id);
        if (parent != null) {
            resourceKey.insert(0, ".");
            resourceKey.insert(0, parent.getId());
        }
        if (resourceIdPrefix != null) resourceKey.insert(0, resourceIdPrefix);
        return resourceKey.toString();
    }
    
    public static void addInvalidComponentsToAjaxRequestTarget(
            final AjaxRequestTarget target, final Form<?> form) {
        IVisitor<Component> visitor = new IVisitor<Component>() {

            public Object component(Component c) {
                if ( c instanceof FormComponent<?> ) {
                    FormComponent<?> fc = (FormComponent<?>) c;
                    if ( !fc.isValid() ) {
                        target.addComponent(fc);
                    }
                }
                return IVisitor.CONTINUE_TRAVERSAL;
            }
        };
        form.visitChildren(visitor);
    }
    
    public static void addFormComponentsToAjaxRequestTarget(
            final AjaxRequestTarget target, final Form<?> form) {
        IVisitor<Component> visitor = new IVisitor<Component>() {

            public Object component(Component c) {
                if ( c.getOutputMarkupId() ) {
                    target.addComponent(c);
                }
                return IVisitor.CONTINUE_TRAVERSAL;
            }
        };
        form.visitChildren(visitor);
    }

    class FormVisitor implements IVisitor<Component>, Serializable {
        
        private static final long serialVersionUID = 0L;

        public Object component(Component c) {
            
            if (componentHasNoTooltip(c)) 
                c.add(new TooltipBehaviour(generateResourcePrefix(c), c.getId()));
            if (c instanceof FormComponent<?>) {
                c.add(markInvalidBehaviour);
                c.setOutputMarkupId(true);
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

    private String generateResourcePrefix(Component c) {

        StringBuffer prefix = new StringBuffer("");
        Component parent = c.getParent();
        while ((parent != null) && !parent.equals("") && !(parent instanceof BaseForm)) {
            prefix.insert(0, ".");
            prefix.insert(0, parent.getId());
            parent = parent.getParent();
        }
        if ((resourceIdPrefix != null) && (!resourceIdPrefix.equals(""))) 
            prefix.insert(0, resourceIdPrefix);
        return prefix.toString();
    }

    public FormComponent<?> getDicomObjectField(String id, DicomObject dcmObj, int[] tagPath) {
        VR vr = DicomElementModel.getVRof(dcmObj, tagPath);
        FormComponent<?> fc;
        if (vr==VR.DA) {
            fc = this.getDateField(id, DicomElementModel.newDateModel(dcmObj, tagPath), null);
        } else if (vr==VR.DT) {
            fc = this.getSimpleDateTimeField(id, DicomElementModel.newDateModel(dcmObj, tagPath), null, false);
        } else if (vr==VR.TM) {
            fc = new TimeField(id, DicomElementModel.newDateModel(dcmObj, tagPath));
        } else {
            final String tfClass = ( vr == VR.IS || vr == VR.AE || vr == VR.SH || vr == VR.CS) ?
                   "textFieldSH" : "textFieldLO";
            FormComponent<String> fct = new TextField<String>(id, DicomElementModel.newStringModel(dcmObj, tagPath)){
                 private static final long serialVersionUID = 1L;

                @Override
                public void onComponentTag(ComponentTag tag) {
                    super.onComponentTag(tag);
                    tag.put("class", tfClass);
                }
            };
            fct.add(getDicomTextValidator(vr));
            fc = fct;
        }
        if (fc instanceof ITextFormatProvider) {
            fc.add(new TooltipBehaviour("dcmfield.", "format", new PropertyModel<Object>(fc,"textFormat")));
        }
        return fc;
    }

    private IValidator<String> getDicomTextValidator(VR vr) {
        if (vr == VR.AE || vr == VR.SH || vr == VR.CS) {
            return new StringValidator.MaximumLengthValidator(16);
        } else if (vr == VR.UI) {
            return new UIDValidator();
        } else if (vr == VR.LO || vr == VR.PN) {
            return new StringValidator.MaximumLengthValidator(64);
        } else if (vr == VR.ST) {
            return new StringValidator.MaximumLengthValidator(1024);
        } else if (vr == VR.LT) {
            return new StringValidator.MaximumLengthValidator(10240);
        } else if (vr == VR.IS) {
            return new StringValidator.MaximumLengthValidator(12);
        } else if (vr == VR.UT) {
            return new StringValidator.MaximumLengthValidator(Integer.MAX_VALUE);
        }
        return new StringValidator.MaximumLengthValidator(64);
    }
}

