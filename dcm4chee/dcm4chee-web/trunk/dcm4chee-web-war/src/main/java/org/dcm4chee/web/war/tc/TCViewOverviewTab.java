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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.util.ListModel;
import org.dcm4chee.icons.ImageManager;
import org.dcm4chee.icons.behaviours.ImageSizeBehaviour;
import org.dcm4chee.web.common.behaviours.TooltipBehaviour;
import org.dcm4chee.web.dao.tc.ITextOrCode;
import org.dcm4chee.web.dao.tc.TCQueryFilterKey;
import org.dcm4chee.web.dao.tc.TCQueryFilterValue;
import org.dcm4chee.web.war.tc.TCInput.ValueChangeListener;
import org.dcm4chee.web.war.tc.TCObject.TextOrCode;
import org.dcm4chee.web.war.tc.TCUtilities.NullDropDownItem;
import org.dcm4chee.web.war.tc.TCUtilities.SelfUpdatingTextArea;
import org.dcm4chee.web.war.tc.TCUtilities.SelfUpdatingTextField;
import org.dcm4chee.web.war.tc.TCViewPanel.AbstractEditableTCViewTab;
import org.dcm4chee.web.war.tc.keywords.TCKeywordCatalogueProvider;

/**
 * @author Bernhard Ableitinger <bernhard.ableitinger@agfa.com>
 * @version $Revision$ $Date$
 * @since Nov 25, 2011
 */
public class TCViewOverviewTab extends AbstractEditableTCViewTab 
{
    private static final long serialVersionUID = 1L;

    public TCViewOverviewTab(final String id, IModel<TCEditableObject> model) 
    {
        this(id, model, false);
    }
        
    public TCViewOverviewTab(final String id, IModel<TCEditableObject> model, boolean editing) {
        super(id, model, editing);
        
        add(new Label("tc-view-overview-title-label", 
                new InternalStringResourceModel("tc.title.text")));
        add(new Label("tc-view-overview-abstract-label", 
                new InternalStringResourceModel("tc.abstract.text")));
        add(new Label("tc-view-overview-keyword-label", 
                new InternalStringResourceModel("tc.keyword.text")));
        add(new Label("tc-view-overview-anatomy-label", 
                new InternalStringResourceModel("tc.anatomy.text")));
        add(new Label("tc-view-overview-pathology-label", 
                new InternalStringResourceModel("tc.pathology.text")));
        add(new Label("tc-view-overview-category-label", 
                new InternalStringResourceModel("tc.category.text")));
        add(new Label("tc-view-overview-level-label", 
                new InternalStringResourceModel("tc.level.text")));
        add(new Label("tc-view-overview-patientsex-label", 
                new InternalStringResourceModel("tc.patient.sex.text")));
        add(new Label("tc-view-overview-patientrace-label", 
                new InternalStringResourceModel("tc.patient.species.text")));
        add(new Label("tc-view-overview-modalities-label", 
                new InternalStringResourceModel("tc.modalities.text")));
        add(new Label("tc-view-overview-imagecount-label", 
                new InternalStringResourceModel("tc.view.images.count.text")));
        add(new Label("tc-view-overview-authorname-label", 
                new InternalStringResourceModel("tc.author.name.text")));
        add(new Label("tc-view-overview-authoraffiliation-label", 
                new InternalStringResourceModel("tc.author.affiliation.text")));
        add(new Label("tc-view-overview-authorcontact-label", 
                new InternalStringResourceModel("tc.author.contact.text")));

        final TextField<String> titleText = new SelfUpdatingTextField("tc-view-overview-title-text", getStringValue(TCQueryFilterKey.Title)) {
            @Override
            protected void textUpdated(String text)
            {
                if (isEditing())
                {
                    getTC().setTitle(text);
                }
            }
        };
        final TextField<String> modalitiesText = new SelfUpdatingTextField("tc-view-overview-modalities-text", getStringValue(TCQueryFilterKey.AcquisitionModality)) {
            @Override
            protected void textUpdated(String text)
            {
                if (isEditing())
                {
                    String[] modalities = text!=null?text.trim().split(";"):null;
                    getTC().setValue(TCQueryFilterKey.AcquisitionModality, modalities!=null?
                            Arrays.asList(modalities):null);
                }
            }
        };

        final TextArea<String> keywordArea = new SelfUpdatingTextArea("tc-view-overview-keyword-area", getShortStringValue(TCQueryFilterKey.Keyword)) {
            @Override
            protected void textUpdated(String text)
            {
                if (isEditing())
                {
                    String[] strings = text!=null?text.trim().split(";"):null;
                    List<ITextOrCode> keywords = null;
                    
                    if (strings!=null && strings.length>0) {
                        keywords = new ArrayList<ITextOrCode>(strings.length);
                        for (String s : strings) {
                            keywords.add(TextOrCode.text(s));
                        }
                    }
                    
                    getTC().setKeywords(keywords);
                }
            }
            
            @Override
            protected void onComponentTag(ComponentTag tag)
            {
            	tag.put("title", getStringValue(TCQueryFilterKey.Keyword)); //$NON-NLS-1$
            }
        };
        keywordArea.setOutputMarkupId(true);
        keywordArea.setMarkupId("tc-view-overview-keyword-area");
        
        final TextArea<String> abstractArea = new SelfUpdatingTextArea("tc-view-overview-abstract-area", getStringValue(TCQueryFilterKey.Abstract)) {
            @Override
            protected void textUpdated(String text)
            {
                if (isEditing())
                {
                    getTC().setAbstract(text);
                }
            }
        };
        abstractArea.setOutputMarkupId(true);
        abstractArea.setMarkupId("tc-view-overview-abstract-area");
        
        final TextField<String> authornameText = new SelfUpdatingTextField("tc-view-overview-authorname-text", getStringValue(TCQueryFilterKey.AuthorName)) {
            @Override
            protected void textUpdated(String text)
            {
                if (isEditing())
                {
                    getTC().setAuthorName(text);
                }
            }
        };
        final TextField<String> authoraffiliationText = new SelfUpdatingTextField("tc-view-overview-authoraffiliation-text", getStringValue(TCQueryFilterKey.AuthorAffiliation)) {
            @Override
            protected void textUpdated(String text)
            {
                if (isEditing())
                {
                    getTC().setAuthorAffiliation(text);
                }
            }
        };
        final TextArea<String> authorcontactArea = new SelfUpdatingTextArea("tc-view-overview-authorcontact-area", getStringValue(TCQueryFilterKey.AuthorContact)) {
            @Override
            protected void textUpdated(String text)
            {
                if (isEditing())
                {
                    getTC().setAuthorContact(text);
                }
            }
        };
        
        final CheckBox diagConfirmedChkBox = new CheckBox("tc-view-overview-diagconfirmed-input");
        
        final TCInput anatomyInput = TCUtilities.createInput("tc-view-overview-anatomy-input", 
                TCQueryFilterKey.Anatomy, getTC().getValue(TCQueryFilterKey.Anatomy),true);
        anatomyInput.addChangeListener(
                new ValueChangeListener() {
                    @Override
                    public void valueChanged(ITextOrCode[] values)
                    {
                        getTC().setAnatomy(values!=null&&values.length>0?values[0]:null);
                    }
                }
        );

        final TCInput pathologyInput = TCUtilities.createInput("tc-view-overview-pathology-input", 
                TCQueryFilterKey.Pathology, getTC().getValue(TCQueryFilterKey.Pathology),true);
        pathologyInput.addChangeListener(
                new ValueChangeListener() {
                    @Override
                    public void valueChanged(ITextOrCode[] values)
                    {
                        getTC().setPathology(values!=null&&values.length>0?values[0]:null);
                    }
                }
        );
        
        final TCInput findingInput = TCUtilities.createInput("tc-view-overview-finding-input", 
                TCQueryFilterKey.Finding, getTC().getValue(TCQueryFilterKey.Finding),true);
        findingInput.addChangeListener(
                new ValueChangeListener() {
                    @Override
                    public void valueChanged(ITextOrCode[] values)
                    {
                        getTC().setFinding(values!=null&&values.length>0?values[0]:null);
                    }
                }
        );
        
        final TCInput diffDiagInput = TCUtilities.createInput("tc-view-overview-diffdiag-input", 
                TCQueryFilterKey.DifferentialDiagnosis, getTC().getValue(TCQueryFilterKey.DifferentialDiagnosis),true);
        diffDiagInput.addChangeListener(
                new ValueChangeListener() {
                    @Override
                    public void valueChanged(ITextOrCode[] values)
                    {
                        getTC().setDiffDiagnosis(values!=null&&values.length>0?values[0]:null);
                    }
                }
        );
        
        final TCInput diagInput = TCUtilities.createInput("tc-view-overview-diag-input", 
                TCQueryFilterKey.Diagnosis, getTC().getValue(TCQueryFilterKey.Diagnosis),true);
        diagInput.addChangeListener(
                new ValueChangeListener() {
                    @Override
                    public void valueChanged(ITextOrCode[] values)
                    {
                        getTC().setDiagnosis(values!=null&&values.length>0?values[0]:null);
                    }
                }
        );
        
        
        final TCComboBox<TCQueryFilterValue.Category> categoryCBox = TCUtilities.createEnumComboBox(
                "tc-view-overview-category-select", getTC().getCategory(),
                Arrays.asList(TCQueryFilterValue.Category.values()), true,
                "tc.category", NullDropDownItem.Undefined, new DropDownChangeListener<TCQueryFilterValue.Category>(TCQueryFilterKey.Category));
        final TCComboBox<TCQueryFilterValue.Level> levelCBox = TCUtilities.createEnumComboBox(
                "tc-view-overview-level-select", getTC().getLevel(),
                Arrays.asList(TCQueryFilterValue.Level.values()), true,
                "tc.level", NullDropDownItem.Undefined, new DropDownChangeListener<TCQueryFilterValue.Level>(TCQueryFilterKey.Level));
        final TCComboBox<TCQueryFilterValue.PatientSex> patientSexCBox = TCUtilities.createEnumComboBox(
                "tc-view-overview-patientsex-select", getTC().getPatientSex(),
                Arrays.asList(TCQueryFilterValue.PatientSex.values()), true,
                "tc.patientsex", NullDropDownItem.Undefined, new DropDownChangeListener<TCQueryFilterValue.PatientSex>(TCQueryFilterKey.PatientSex));
        final TCEditableComboBox patientSpeciesCBox = TCUtilities.createEnumEditableComboBox(
                "tc-view-overview-patientrace-select", getTC().getValueAsString(TCQueryFilterKey.PatientSpecies),
                Arrays.asList(TCQueryFilterValue.PatientSpecies.values()), true,
                "tc.patient.species", NullDropDownItem.Undefined, new DropDownChangeListener<String>(TCQueryFilterKey.PatientSpecies));

        
        final TextField<String> anatomyText = new TextField<String>("tc-view-overview-anatomy-value-label", new Model<String>(
                getShortStringValue(TCQueryFilterKey.Anatomy)
        )) {
			private static final long serialVersionUID = 3465370488528419531L;
			@Override
            protected void onComponentTag(ComponentTag tag)
            {
            	tag.put("title", getStringValue(TCQueryFilterKey.Anatomy)); //$NON-NLS-1$
            }
        };
        final TextField<String> pathologyText = new TextField<String>("tc-view-overview-pathology-value-label", new Model<String>(
                getShortStringValue(TCQueryFilterKey.Pathology)
        )) {
			private static final long serialVersionUID = 3465370488528419531L;
			@Override
            protected void onComponentTag(ComponentTag tag)
            {
            	tag.put("title", getStringValue(TCQueryFilterKey.Pathology)); //$NON-NLS-1$
            }
        };
        final TextField<String> findingText = new TextField<String>("tc-view-overview-finding-value-label", new Model<String>(
                getShortStringValue(TCQueryFilterKey.Finding)
        )) {
			private static final long serialVersionUID = 3465370488528419531L;
			@Override
            protected void onComponentTag(ComponentTag tag)
            {
            	tag.put("title", getStringValue(TCQueryFilterKey.Finding)); //$NON-NLS-1$
            }
        };
        final TextField<String> diffDiagText = new TextField<String>("tc-view-overview-diffdiag-value-label", new Model<String>(
                getShortStringValue(TCQueryFilterKey.DifferentialDiagnosis)
        )) {
			private static final long serialVersionUID = 3465370488528419531L;
			@Override
            protected void onComponentTag(ComponentTag tag)
            {
            	tag.put("title", getStringValue(TCQueryFilterKey.DifferentialDiagnosis)); //$NON-NLS-1$
            }
        };
        final TextField<String> diagText = new TextField<String>("tc-view-overview-diag-value-label", new Model<String>(
                getShortStringValue(TCQueryFilterKey.Diagnosis)
        )) {
			private static final long serialVersionUID = 3465370488528419531L;
			@Override
            protected void onComponentTag(ComponentTag tag)
            {
            	tag.put("title", getStringValue(TCQueryFilterKey.Diagnosis)); //$NON-NLS-1$
            }
        };
        final TextField<String> categoryText = new TextField<String>("tc-view-overview-category-value-label", new Model<String>(
                getStringValue(TCQueryFilterKey.Category)
        ));
        final TextField<String> levelText = new TextField<String>("tc-view-overview-level-value-label", new Model<String>(
                getStringValue(TCQueryFilterKey.Level)
        ));
        final TextField<String> patientSexText = new TextField<String>("tc-view-overview-patientsex-value-label", new Model<String>(
                getStringValue(TCQueryFilterKey.PatientSex)
        ));
        final TextField<String> patientSpeciesText = new TextField<String>("tc-view-overview-patientrace-value-label", new Model<String>(
                getStringValue(TCQueryFilterKey.PatientSpecies)
        ));
        final TextField<String> imageCountText = new TextField<String>("tc-view-overview-imagecount-value-label", new Model<String>(
                getTC().getReferencedImages()!=null ? Integer.toString(getTC().getReferencedImages().size()) : "0"
        ));
        
        final KeywordsListModel keywordsModel = new KeywordsListModel();
        final WebMarkupContainer keywordCodesContainer = new WebMarkupContainer("tc-view-overview-keyword-input-container");
        final ListView<ITextOrCode> keywordCodesView = new ListView<ITextOrCode>(
                "tc-view-overview-keyword-input-view", keywordsModel) {            
            @Override
            protected void populateItem(final ListItem<ITextOrCode> item) {
                final int index = item.getIndex();

                AjaxLink<String> addBtn = new AjaxLink<String>("tc-view-overview-keyword-input-add") {
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        keywordsModel.addKeyword();
                        target.addComponent(keywordCodesContainer);
                    }
                };
                addBtn.add(new Image("tc-view-overview-keyword-input-add-img",
                        ImageManager.IMAGE_COMMON_ADD).add(
                                new ImageSizeBehaviour("vertical-align: middle;")));
                addBtn.add(new TooltipBehaviour("tc.view.overview.keyword.","add"));
                addBtn.setOutputMarkupId(true);
                addBtn.setVisible(index==0);
                
                AjaxLink<String> removeBtn = new AjaxLink<String>("tc-view-overview-keyword-input-remove") {
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        keywordsModel.removeKeyword(item.getModelObject());
                        target.addComponent(keywordCodesContainer);
                    }
                };
                removeBtn.add(new Image("tc-view-overview-keyword-input-remove-img",
                        ImageManager.IMAGE_TC_CANCEL).add(
                                new ImageSizeBehaviour("vertical-align: middle;")));
                removeBtn.add(new TooltipBehaviour("tc.view.overview.keyword.","remove"));
                removeBtn.setOutputMarkupId(true);
                removeBtn.setVisible(index>0);
                
                TCInput keywordInput = TCUtilities.createInput("tc-view-overview-keyword-input", 
                        TCQueryFilterKey.Keyword, item.getModelObject(), true);
                keywordInput.addChangeListener(
                        new ValueChangeListener() {
                            @Override
                            public void valueChanged(ITextOrCode[] values)
                            {
                                keywordsModel.setKeywordAt(index, values!=null&&values.length>0?values[0]:null);
                            }
                        }
                );
                
                item.setOutputMarkupId(true);
                item.add(keywordInput.getComponent());
                item.add(addBtn);
                item.add(removeBtn);
                
                if (index>0) {
                    item.add(new AttributeModifier("style",true,new Model<String>("border-top: 4px solid transparent")) {
                        @Override
                        protected String newValue(String currentValue, String newValue) {
                            if (currentValue==null) {
                                return newValue;
                            }
                            else if (newValue==null) {
                                return currentValue;
                            }
                            else {
                                return currentValue + ";" + newValue;
                            }
                        }
                    });
                }
            }
        };
        keywordCodesView.setOutputMarkupId(true);    
        keywordCodesContainer.setOutputMarkupId(true);
        
        final WebMarkupContainer findingRow = new WebMarkupContainer("tc-view-overview-finding-row");
        findingRow.add(findingText);
        findingRow.add(findingInput.getComponent());
        findingRow.add(new Label("tc-view-overview-finding-label", 
                new InternalStringResourceModel("tc.finding.text")));
        findingRow.setVisible(TCKeywordCatalogueProvider.getInstance().hasCatalogue(TCQueryFilterKey.Finding));
        
        final WebMarkupContainer diffDiagRow = new WebMarkupContainer("tc-view-overview-diffdiag-row");
        diffDiagRow.add(diffDiagText);
        diffDiagRow.add(diffDiagInput.getComponent());
        diffDiagRow.add(new Label("tc-view-overview-diffdiag-label", 
                new InternalStringResourceModel("tc.diffdiagnosis.text")));
        diffDiagRow.setVisible(TCKeywordCatalogueProvider.getInstance().hasCatalogue(TCQueryFilterKey.DifferentialDiagnosis));
        
        final WebMarkupContainer diagRow = new WebMarkupContainer("tc-view-overview-diag-row");
        diagRow.add(diagText);
        diagRow.add(diagInput.getComponent());
        diagRow.add(new Label("tc-view-overview-diag-label", 
                new InternalStringResourceModel("tc.diagnosis.text")));
        diagRow.setVisible(TCKeywordCatalogueProvider.getInstance().hasCatalogue(TCQueryFilterKey.Diagnosis));
        
        final WebMarkupContainer diagConfirmedRow = new WebMarkupContainer("tc-view-overview-diagconfirmed-row");
        diagConfirmedRow.add(diagConfirmedChkBox);
        diagConfirmedRow.add(new Label("tc-view-overview-diagconfirmed-label", 
                new InternalStringResourceModel("tc.diagnosis.confirmed.text")));
        diagConfirmedRow.setVisible(TCKeywordCatalogueProvider.getInstance().hasCatalogue(TCQueryFilterKey.Diagnosis));
        
        keywordCodesContainer.add(keywordCodesView);

        AttributeModifier readonlyModifier = new AttributeAppender("readonly",true,new Model<String>("readonly"), " ");

        imageCountText.add(readonlyModifier);
        
        if (!editing) {
            diagConfirmedChkBox.setEnabled(false);
            
            titleText.add(readonlyModifier);
            keywordArea.add(readonlyModifier);
            abstractArea.add(readonlyModifier);
            modalitiesText.add(readonlyModifier);
            patientSpeciesCBox.add(readonlyModifier);
            authornameText.add(readonlyModifier);
            authoraffiliationText.add(readonlyModifier);
            authorcontactArea.add(readonlyModifier);
            anatomyText.add(readonlyModifier);
            pathologyText.add(readonlyModifier);
            categoryText.add(readonlyModifier);
            levelText.add(readonlyModifier);
            patientSexText.add(readonlyModifier);
            patientSpeciesText.add(readonlyModifier);
        }
        
        boolean keywordCodeInput = editing && TCKeywordCatalogueProvider.
            getInstance().hasCatalogue(TCQueryFilterKey.Keyword);
        
        keywordCodesContainer.setVisible(keywordCodeInput);
        anatomyInput.getComponent().setVisible(editing);
        pathologyInput.getComponent().setVisible(editing);
        findingInput.getComponent().setVisible(editing);
        diffDiagInput.getComponent().setVisible(editing);
        diagInput.getComponent().setVisible(editing);
        categoryCBox.setVisible(editing);
        levelCBox.setVisible(editing);
        patientSexCBox.setVisible(editing);
        patientSpeciesCBox.setVisible(editing);
        
        keywordArea.setVisible(!keywordCodeInput);
        anatomyText.setVisible(!editing);
        pathologyText.setVisible(!editing);
        findingText.setVisible(!editing);
        diffDiagText.setVisible(!editing);
        diagText.setVisible(!editing);
        categoryText.setVisible(!editing);
        levelText.setVisible(!editing);
        patientSexText.setVisible(!editing);
        patientSpeciesText.setVisible(!editing);
        
        add(titleText);
        add(abstractArea);
        add(authornameText);
        add(authoraffiliationText);
        add(authorcontactArea);
        add(keywordArea);
        add(keywordCodesContainer);
        add(anatomyText);
        add(anatomyInput.getComponent());
        add(pathologyText);
        add(pathologyInput.getComponent());
        add(findingRow);
        add(diffDiagRow);
        add(diagRow);
        add(diagConfirmedRow);
        add(categoryCBox);
        add(categoryText);
        add(levelCBox);
        add(levelText);
        add(patientSexCBox);
        add(patientSexText);
        add(patientSpeciesText);
        add(patientSpeciesCBox);
        add(modalitiesText);
        add(imageCountText);
    }
    
    @Override
    public String getTabTitle()
    {
        return getString("tc.view.overview.tab.title");
    }
    
    @Override
    public boolean hasContent()
    {
        return getTC()!=null;
    }
    
    @Override
    protected void saveImpl()
    {
    }
    
    private class KeywordsListModel extends ListModel<ITextOrCode>
    {
        public KeywordsListModel() {
            if (getSize()==0) {
                getTC().addKeyword(createTemplateKeyword());
            }
        }
        @Override
        public List<ITextOrCode> getObject()
        {
            return getTC().getKeywords();
        }
        
        @Override
        public void setObject(List<ITextOrCode> keywords) {
            getTC().setKeywords(keywords);
        }
        
        public int getSize() {
            return getTC().getKeywordCount();
        }
        
        public void addKeyword() {
            getTC().addKeywordImpl(createTemplateKeyword());
        }
        
        public void removeKeyword(ITextOrCode keyword) {
            getTC().removeKeywordImpl(keyword);
        }
        
        public void setKeywordAt(int index, ITextOrCode keyword) {
            getTC().setKeywordAt(index, keyword);
        }
        
        private ITextOrCode createTemplateKeyword() {
            return TextOrCode.text(null);
        }
    }
    
    private class InternalStringResourceModel extends AbstractReadOnlyModel<String>
    {
        private String key;
        private String value;
        
        public InternalStringResourceModel(String key)
        {
            this.key = key;
        }
        
        @Override
        public String getObject()
        {
            if (value==null)
            {
                value = getString(key);
                if (value!=null && !value.endsWith(":"))
                {
                    value = value + ":";
                }
            }
            return value;
        }
    }
    
    private class DropDownChangeListener<T> implements TCUtilities.TCChangeListener<T>
    {
        private TCQueryFilterKey key;
        
        public DropDownChangeListener(TCQueryFilterKey key)
        {
            this.key = key;
        }
        
        @Override
        public void valueChanged(T value)
        {
            getTC().setValue(key, value);
        }
    }
}
