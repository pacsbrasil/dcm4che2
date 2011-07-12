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
package org.dcm4chee.web.war.tc;

import org.apache.wicket.Component;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.resources.CompressedResourceReference;
import org.dcm4chee.web.common.ajax.MaskingAjaxCallBehavior;
import org.dcm4chee.web.dao.tc.TCQueryFilter;
import org.dcm4chee.web.war.tc.TCResultPanel.TCListModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Bernhard Ableitinger <bernhard.ableitinger@agfa.com>
 * @version $Revision$ $Date$
 * @since April 28, 2011
 */
public class TCPanel extends Panel {
    private static final Logger log = LoggerFactory.getLogger(TCPanel.class);

    private static final ResourceReference CSS = new CompressedResourceReference(
            TCPanel.class, "tc-style.css");

    public static final String ModuleName = "tc";

    private static final MaskingAjaxCallBehavior macb = new MaskingAjaxCallBehavior();

    public TCPanel(final String id) {
        super(id);

        if (TCPanel.CSS != null) {
            add(CSSPackageResource.getHeaderContribution(TCPanel.CSS));
        }

        final TCDetailsPanel detailsPanel = new TCDetailsPanel("details-panel");

        final TCListModel listModel = new TCListModel();
        final TCResultPanel listPanel = new TCResultPanel("result-panel",
                listModel) {
            @Override
            public Component selectionChanged(TCModel tc) {
                try {
                    if (tc == null) {
                        detailsPanel.clearTCObject(false);
                    } else {
                        detailsPanel.setTCObject(TCDetails.create(tc));
                    }
                } catch (Exception e) {
                    log.error("Parsing TC object failed!", e);

                    detailsPanel.clearTCObject(true);
                }

                return detailsPanel;
            }
        };

        add(macb);
        add(new TCSearchPanel("search-panel") {
            @Override
            public Component doSearch(TCQueryFilter filter) {
                listModel.update(filter);

                return listPanel;
            }
        });

        add(listPanel);
        add(detailsPanel);
    }

    public static String getModuleName() {
        return ModuleName;
    }

    public static MaskingAjaxCallBehavior getMaskingBehaviour() {
        return macb;
    }

}
