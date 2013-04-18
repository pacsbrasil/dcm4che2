package org.dcm4chee.web.war.tc.widgets;

import java.io.Serializable;
import java.util.List;

import org.apache.wicket.RequestCycle;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AbstractAjaxBehavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public abstract class TCAjaxComboBox<T extends Serializable> extends TCComboBox<T> {

	private static final Logger log = LoggerFactory.getLogger(TCAjaxComboBox.class);
	
	private static final String CALLBACK_URL_KEY = "wicket-callback-url";
	private static final String INITIAL_VALUE_KEY = "initial-value";
	
	private AbstractAjaxBehavior selectionChangedBehavior;
	
	public TCAjaxComboBox(final String id, List<? extends T> options)
	{
		this(id, options, (T) null, null);
	}
	
	public TCAjaxComboBox(final String id, List<? extends T> options, IChoiceRenderer<T> renderer)
	{
		this(id, options, (T) null, renderer);
	}
	
	public TCAjaxComboBox(final String id, List<? extends T> options, T selectedValue)
	{
		this(id, options, selectedValue, null);
	}
	
	public TCAjaxComboBox(final String id, List<? extends T> options, 
			T selectedValue, IChoiceRenderer<T> renderer)
	{
		this(id, options, new Model<T>(selectedValue), renderer);
	}
	
	public TCAjaxComboBox(final String id, List<? extends T> options, 
			IModel<T> selectedValue, IChoiceRenderer<T> renderer)
	{
		super(id, options, selectedValue, renderer);
		add(selectionChangedBehavior=new SelectionChangedBehavior());
	}
	
    @Override
    protected void onComponentTag(ComponentTag tag)
    {
    	super.onComponentTag(tag);

    	tag.put(CALLBACK_URL_KEY, selectionChangedBehavior.getCallbackUrl());
    	tag.put(INITIAL_VALUE_KEY, getDefaultModelObjectAsString());
    }
	
	protected abstract T convertValue(String value) throws Exception;
	
    private class SelectionChangedBehavior extends AbstractDefaultAjaxBehavior {
		@Override
    	public void respond(AjaxRequestTarget target) {
			try {
	    		String valueString = RequestCycle.get().getRequest().getParameter("selectedValue");
	    		T value = convertValue(valueString);
	    		setDefaultModelObject(value);
	    		valueChanged(value);
			}
			catch (Exception e) {
				log.error("Unable to update model: Value conversion failed!", e);
			}
    	}
    }
}
