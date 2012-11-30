package org.dcm4chee.web.war.tc;

import java.io.Serializable;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AbstractAjaxBehavior;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.Model;

public class TCEditableComboBox extends DropDownChoice<Serializable> implements IHeaderContributor {
	private static final long serialVersionUID = 4036772216132377000L;
	
	private static final String CALLBACK_URL_KEY = "wicket-callback-url";
	private static final String EDITABLE_KEY = "editable";
	private static final String INITIAL_VALUE_KEY = "initial-value";
	private static final String COMBOBOX_CLASS = "ui-combobox";
	
	private AbstractAjaxBehavior selectionChangedBehavior;
	
	public TCEditableComboBox(final String id, List<? extends Serializable> options)
	{
		this(id, options, null, null);
	}
	
	public TCEditableComboBox(final String id, List<? extends Serializable> options, 
			IChoiceRenderer<Serializable> renderer)
	{
		this(id, options, null, renderer);
	}
	
	public TCEditableComboBox(final String id, List<? extends Serializable> options, 
			String selectedValue)
	{
		this(id, options, selectedValue, null);
	}
	
	public TCEditableComboBox(final String id, List<? extends Serializable> options, 
			String selectedValue, IChoiceRenderer<Serializable> renderer)
	{
		super(id, new Model<Serializable>(selectedValue), options, renderer);

		setOutputMarkupId(true);
		add(selectionChangedBehavior=new SelectionChangedBehavior());
		add(new AttributeAppender("class",new Model<String>(COMBOBOX_CLASS)," "));
	}
	
    public void renderHead(IHeaderResponse response) {
        // commented out: already included in the html header of TCPanel.html
    	//response.renderJavascriptReference(new ResourceReference(TCEditableComboBox.class, "js/tc-jquery-ui.js"));
    }
    
    @Override
    protected void onComponentTag(ComponentTag tag)
    {
    	super.onComponentTag(tag);

    	tag.put(CALLBACK_URL_KEY, selectionChangedBehavior.getCallbackUrl());
    	tag.put(EDITABLE_KEY, Boolean.toString(true));
    	tag.put(INITIAL_VALUE_KEY, getDefaultModelObjectAsString());
    }
    
    protected void valueChanged(String value) {}
    
    private class SelectionChangedBehavior extends AbstractDefaultAjaxBehavior {
		private static final long serialVersionUID = -2514246246616053706L;
		@Override
    	public void respond(AjaxRequestTarget target) {
    		String value = RequestCycle.get().getRequest().getParameter("selectedValue");
    		Logger.getLogger(TCEditableComboBox.class).info("VALUE CHANGED: " + value);
    		TCEditableComboBox.this.setDefaultModelObject(value);
    		valueChanged(value);
    	}
    }
}
