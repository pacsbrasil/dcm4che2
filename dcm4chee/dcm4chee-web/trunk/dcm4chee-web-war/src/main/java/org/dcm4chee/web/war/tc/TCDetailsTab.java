package org.dcm4chee.web.war.tc;

import org.apache.wicket.markup.html.panel.Panel;

@SuppressWarnings("serial")
public abstract class TCDetailsTab extends Panel {
    public TCDetailsTab(final String id) {
        super(id);
    }
    public boolean enabled() {
    	return true;
    }
    public boolean visible() {
    	return true;
    }
}