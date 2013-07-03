package org.dcm4chee.web.war.tc;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.panel.Panel;
import org.dcm4chee.web.common.base.ExternalWebApp;
import org.dcm4chee.web.common.base.ExternalWebAppGroupPanel;
import org.dcm4chee.web.war.config.delegate.WebCfgDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Bernhard Ableitinger <bernhard.ableitinger@agfa.com>
 * @version $Revision$ $Date$
 * @since July 31, 2013
 */
public class TCEnvironment {
	
	private static final Logger log = LoggerFactory.getLogger(TCEnvironment.class);

	@SuppressWarnings("serial")
	public static void init(List<ExternalWebApp> list) {
		try {
			if (list!=null) {
				for (ExternalWebApp app : list) {
					Panel forumAdminPanel = findForumAdminPanel(app);
					if (forumAdminPanel!=null) {
							WebCfgDelegate cfg = WebCfgDelegate.getInstance();
							final TCForumIntegration forum = TCForumIntegration.get(
									cfg.getTCForumIntegrationType());
							
							if (forum!=null) {
								app.getPanel().add(new AbstractBehavior() {
									@Override
									public void beforeRender(Component c) {
										super.beforeRender(c);
										try {
											forum.setAdminUserCookie();
										}
										catch (Exception e) {
											log.error(null, e);
										}
									}
								});
							}
							
							break;
					}
				}
			}
		}
		catch (Exception e) {
			log.error(null, e);
		}
	}
	
	private static Panel findForumAdminPanel(ExternalWebApp app) {
		Panel appPanel = app.getPanel();
		List<Panel> panels = new ArrayList<Panel>(3);
		if (appPanel instanceof ExternalWebAppGroupPanel) {
			List<ITab> tabs = ((ExternalWebAppGroupPanel)appPanel).getTabs();
			if (tabs!=null) {
				for (ITab tab : tabs) {
					Panel tabPanel = tab.getPanel(null);
					if (tabPanel!=null) {
						panels.add(tabPanel);
					}
				}
			}
		}
		else {
			panels.add(appPanel);
		}
		
		for (Panel panel : panels) {
			String url = panel.getDefaultModelObjectAsString();
			if (url.endsWith("jforum/forums/list.page")) {
				return panel;
			}
		}
		
		return null;
	}
}
