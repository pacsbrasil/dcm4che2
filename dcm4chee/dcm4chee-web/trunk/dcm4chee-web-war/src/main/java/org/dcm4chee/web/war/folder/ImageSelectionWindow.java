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

package org.dcm4chee.web.war.folder;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.list.Loop;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.dcm4chee.web.war.common.WadoImage;
import org.dcm4chee.web.war.folder.model.InstanceModel;
import org.dcm4chee.web.war.folder.model.PPSModel;
import org.dcm4chee.web.war.folder.model.SeriesModel;
import org.dcm4chee.web.war.folder.model.StudyModel;

/**
 * @author Franz Willer <franz.willer@gmail.com>
 * @version $Revision$ $Date$
 * @since 05.07.2010
 */
public class ImageSelectionWindow extends ModalWindow {
    private static final long serialVersionUID = 1L;
    private List<InstanceModel> instances = new ArrayList<InstanceModel>();
    private int numCols = 5;
    private int numRows;
    private Model<Integer> imgSizeModel = new Model<Integer>(128);
    private boolean selectionChanged;

    public ImageSelectionWindow(String id, String titleResource) {
        this(id);
        setTitle(new ResourceModel(titleResource));
    }
    
    public ImageSelectionWindow(String id) {
        super(id);
        initWadoBaseUrl();
        setContent(new ImageSelectionPanel());
    }
    
    public void initWadoBaseUrl() {
        WadoImage.setDefaultWadoBaseUrl(WADODelegate.getInstance().getWadoBaseUrl());
    }
    
    private void calcNumRows() {
        numRows = instances.size() / numCols;
        if (instances.size() % numCols != 0) {
            numRows++;
        }
    }
    
    public void show(final AjaxRequestTarget target, StudyModel study) {
        instances.clear();
        if (study.isCollapsed())
            study.expand();
        List<PPSModel> ppss = study.getPPSs();
        List<SeriesModel> seriess;
        SeriesModel series;
        for (int i = 0 ; i < ppss.size() ; i++) {
            seriess = ppss.get(i).getSeries();
            for (int j = 0 ; j < seriess.size() ; j++) {
                series = seriess.get(j);
                if (series.isCollapsed())
                    series.expand();
                addInstances(series);
            }
        }
        show(target);
    }

    private void addInstances(SeriesModel series) {
        if (series.getInstances().isEmpty())
            return;
        if ( WADODelegate.getInstance().getRenderType(series.getInstances().get(0).getSopClassUID()) == WADODelegate.IMAGE) {
            instances.addAll(series.getInstances());
        }
    }
    
    public void show(final AjaxRequestTarget target, SeriesModel series) {
        if (series.isCollapsed())
            series.expand();
        instances.clear();
        addInstances(series);
        show(target);
    }

    @Override
    public void show(final AjaxRequestTarget target) {
        this.calcNumRows();
        selectionChanged = false;
        super.show(target);
    }
    
    private class ImageSelectionPanel extends Panel {

        private static final long serialVersionUID = 1L;

        public ImageSelectionPanel() {
            super("content");
            add(new Loop("rows", new AbstractReadOnlyModel<Integer>() {
                private static final long serialVersionUID = 1L;

                @Override
                public Integer getObject() {
                    return numRows;
                }
            }) {
                private static final long serialVersionUID = 1L;

                @Override
                protected void populateItem(LoopItem rowItem) {
                    int rowIdx = rowItem.getIteration();
                    int startIdx = rowIdx * numCols;
                    int endIdx = Math.min(startIdx+numCols, instances.size());
                    rowItem.add(new ListView<InstanceModel>("columns", instances.subList(startIdx, endIdx)) {
                        private static final long serialVersionUID = 1L;

                        @Override
                        protected void populateItem(ListItem<InstanceModel> colItem) {
                            colItem.add( new WadoImage("wadoimg", colItem.getModelObject(), imgSizeModel));
                            colItem.add( new AjaxCheckBox("selected", new PropertyModel<Boolean>(colItem.getModelObject(), "selected")){

                                private static final long serialVersionUID = 1L;

                                @Override
                                protected void onUpdate(AjaxRequestTarget target) {
                                    selectionChanged = true;
                                    target.addComponent(this);
                                }}.setOutputMarkupId(true));
                        }
                        
                    });
                }
                
            });
        }

    }

    public boolean isSelectionChanged() {
        return selectionChanged;
    }    
    
    
}
