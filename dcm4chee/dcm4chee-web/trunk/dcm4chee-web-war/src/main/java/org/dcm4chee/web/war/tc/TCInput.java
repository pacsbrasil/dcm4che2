package org.dcm4chee.web.war.tc;

import java.io.Serializable;

import org.apache.wicket.Component;

public interface TCInput extends Serializable {
    public Object getInputValue();

    public void resetInputValue();

    public Component getInputComponent();

    public void addChangeListener(ValueChangeListener l);
    
    public void removeChangeListener(ValueChangeListener l);
    
    public static interface ValueChangeListener extends Serializable
    {
        public void valueChanged(Object value);
    }
}