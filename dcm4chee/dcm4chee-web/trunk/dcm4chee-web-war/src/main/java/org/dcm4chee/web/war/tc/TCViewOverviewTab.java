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

import java.util.Arrays;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.dcm4chee.web.dao.tc.TCQueryFilterKey;
import org.dcm4chee.web.dao.tc.TCQueryFilterValue;
import org.dcm4chee.web.war.tc.TCInput.ValueChangeListener;
import org.dcm4chee.web.war.tc.TCUtilities.NullDropDownItem;
import org.dcm4chee.web.war.tc.TCUtilities.SelfUpdatingTextArea;
import org.dcm4chee.web.war.tc.TCUtilities.SelfUpdatingTextField;
import org.dcm4chee.web.war.tc.TCViewPanel.AbstractEditableTCViewTab;
import org.dcm4chee.web.war.tc.keywords.TCKeywordCatalogueProvider;
import org.slf4j.LoggerFactory;

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
                    String[] modalities = text!=null?text.trim().split(","):null;
                    LoggerFactory.getLogger("test").info("Changing modalities to '" + text + "'");
                    getTC().setValue(TCQueryFilterKey.AcquisitionModality, modalities!=null?
                            Arrays.asList(modalities):null);
                    LoggerFactory.getLogger("test").info("Changed modalities to '" + getTC().getStringValue(TCQueryFilterKey.AcquisitionModality) + "'");
                }
            }
        };
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
        
        final TCInput keywordInput = TCUtilities.createInput("tc-view-overview-keyword-input", 
                TCQueryFilterKey.Keyword, getTC().getValue(TCQueryFilterKey.Keyword));
        keywordInput.addChangeListener(
                new ValueChangeListener() {
                    @Override
                    public void valueChanged(Object value)
                    {
                        getTC().setValue(TCQueryFilterKey.Keyword, keywordInput.getInputValue());
                    }
                }
        );

        final TCInput anatomyInput = TCUtilities.createInput("tc-view-overview-anatomy-input", 
                TCQueryFilterKey.Anatomy, getTC().getValue(TCQueryFilterKey.Anatomy));
        anatomyInput.addChangeListener(
                new ValueChangeListener() {
                    @Override
                    public void valueChanged(Object value)
                    {
                        getTC().setValue(TCQueryFilterKey.Anatomy, anatomyInput.getInputValue());
                    }
                }
        );

        final TCInput pathologyInput = TCUtilities.createInput("tc-view-overview-pathology-input", 
                TCQueryFilterKey.Pathology, getTC().getValue(TCQueryFilterKey.Pathology));
        pathologyInput.addChangeListener(
                new ValueChangeListener() {
                    @Override
                    public void valueChanged(Object value)
                    {
                        getTC().setValue(TCQueryFilterKey.Pathology, pathologyInput.getInputValue());
                    }
                }
        );
        
        final TCInput findingInput = TCUtilities.createInput("tc-view-overview-finding-input", 
                TCQueryFilterKey.Finding, getTC().getValue(TCQueryFilterKey.Finding));
        findingInput.addChangeListener(
                new ValueChangeListener() {
                    @Override
                    public void valueChanged(Object value)
                    {
                        getTC().setValue(TCQueryFilterKey.Finding, findingInput.getInputValue());
                    }
                }
        );
        
        final TCInput diffDiagInput = TCUtilities.createInput("tc-view-overview-diffdiag-input", 
                TCQueryFilterKey.DifferentialDiagnosis, getTC().getValue(TCQueryFilterKey.DifferentialDiagnosis));
        diffDiagInput.addChangeListener(
                new ValueChangeListener() {
                    @Override
                    public void valueChanged(Object value)
                    {
                        getTC().setValue(TCQueryFilterKey.DifferentialDiagnosis, diffDiagInput.getInputValue());
                    }
                }
        );
        
        final TCInput diagInput = TCUtilities.createInput("tc-view-overview-diag-input", 
                TCQueryFilterKey.Diagnosis, getTC().getValue(TCQueryFilterKey.Diagnosis));
        diagInput.addChangeListener(
                new ValueChangeListener() {
                    @Override
                    public void valueChanged(Object value)
                    {
                        getTC().setValue(TCQueryFilterKey.Diagnosis, diagInput.getInputValue());
                    }
                }
        );
        
        final TextField<String> patientSpeciesText = new SelfUpdatingTextField("tc-view-overview-patientrace-text", getStringValue(TCQueryFilterKey.PatientSpecies)) {
            @Override
            protected void textUpdated(String text)
            {
                if (isEditing())
                {
                    getTC().setPatientSpecies(text);
                }
            }
        };

        final DropDownChoice<TCQueryFilterValue.Category> categoryChoice = TCUtilities.createEnumDropDownChoice(
                "tc-view-overview-category-select", new Model<TCQueryFilterValue.Category>(getTC().getCategory()),
                Arrays.asList(TCQueryFilterValue.Category.values()), true,
                "tc.category", NullDropDownItem.Undefined, new DropDownChangeListener<TCQueryFilterValue.Category>(TCQueryFilterKey.Category));
        final DropDownChoice<TCQueryFilterValue.Level> levelChoice = TCUtilities.createEnumDropDownChoice(
                "tc-view-overview-level-select", new Model<TCQueryFilterValue.Level>(getTC().getLevel()),
                Arrays.asList(TCQueryFilterValue.Level.values()), true,
                "tc.level", NullDropDownItem.Undefined, new DropDownChangeListener<TCQueryFilterValue.Level>(TCQueryFilterKey.Level));
        final DropDownChoice<TCQueryFilterValue.PatientSex> patientSexChoice = TCUtilities.createEnumDropDownChoice(
                "tc-view-overview-patientsex-select", new Model<TCQueryFilterValue.PatientSex>(getTC().getPatientSex()),
                Arrays.asList(TCQueryFilterValue.PatientSex.values()), true,
                "tc.patientsex", NullDropDownItem.Undefined, new DropDownChangeListener<TCQueryFilterValue.PatientSex>(TCQueryFilterKey.PatientSex));

        final Label keywordLabel = new Label("tc-view-overview-keyword-value-label", new Model<String>(
                getStringValue(TCQueryFilterKey.Keyword)
        ));
        final Label anatomyLabel = new Label("tc-view-overview-anatomy-value-label", new Model<String>(
                getStringValue(TCQueryFilterKey.Anatomy)
        ));
        final Label pathologyLabel = new Label("tc-view-overview-pathology-value-label", new Model<String>(
                getStringValue(TCQueryFilterKey.Pathology)
        ));
        final Label findingLabel = new Label("tc-view-overview-finding-value-label", new Model<String>(
                getStringValue(TCQueryFilterKey.Finding)
        ));
        final Label diffDiagLabel = new Label("tc-view-overview-diffdiag-value-label", new Model<String>(
                getStringValue(TCQueryFilterKey.DifferentialDiagnosis)
        ));
        final Label diagLabel = new Label("tc-view-overview-diag-value-label", new Model<String>(
                getStringValue(TCQueryFilterKey.Diagnosis)
        ));
        final Label categoryLabel = new Label("tc-view-overview-category-value-label", new Model<String>(
                getStringValue(TCQueryFilterKey.Category)
        ));
        final Label levelLabel = new Label("tc-view-overview-level-value-label", new Model<String>(
                getStringValue(TCQueryFilterKey.Level)
        ));
        final Label patientSexLabel = new Label("tc-view-overview-patientsex-value-label", new Model<String>(
                getStringValue(TCQueryFilterKey.PatientSex)
        ));
        final Label imageCountLabel = new Label("tc-view-overview-imagecount-value-label", new Model<String>(
                getTC().getReferencedImages()!=null ? Integer.toString(getTC().getReferencedImages().size()) : "0"
        ));
            
        final WebMarkupContainer findingRow = new WebMarkupContainer("tc-view-overview-finding-row");
        findingRow.add(findingLabel);
        findingRow.add(findingInput.getInputComponent());
        findingRow.add(new Label("tc-view-overview-finding-label", 
                new InternalStringResourceModel("tc.finding.text")));
        findingRow.setVisible(TCKeywordCatalogueProvider.getInstance().hasCatalogue(TCQueryFilterKey.Finding));
        
        final WebMarkupContainer diffDiagRow = new WebMarkupContainer("tc-view-overview-diffdiag-row");
        diffDiagRow.add(diffDiagLabel);
        diffDiagRow.add(diffDiagInput.getInputComponent());
        diffDiagRow.add(new Label("tc-view-overview-diffdiag-label", 
                new InternalStringResourceModel("tc.diffdiagnosis.text")));
        diffDiagRow.setVisible(TCKeywordCatalogueProvider.getInstance().hasCatalogue(TCQueryFilterKey.DifferentialDiagnosis));
        
        final WebMarkupContainer diagRow = new WebMarkupContainer("tc-view-overview-diag-row");
        diagRow.add(diagLabel);
        diagRow.add(diagInput.getInputComponent());
        diagRow.add(new Label("tc-view-overview-diag-label", 
                new InternalStringResourceModel("tc.diagnosis.text")));
        diagRow.setVisible(TCKeywordCatalogueProvider.getInstance().hasCatalogue(TCQueryFilterKey.Diagnosis));
        
        final WebMarkupContainer diagConfirmedRow = new WebMarkupContainer("tc-view-overview-diagconfirmed-row");
        diagConfirmedRow.add(diagConfirmedChkBox);
        diagConfirmedRow.add(new Label("tc-view-overview-diagconfirmed-label", 
                new InternalStringResourceModel("tc.diagnosis.confirmed.text")));
        diagConfirmedRow.setVisible(TCKeywordCatalogueProvider.getInstance().hasCatalogue(TCQueryFilterKey.Diagnosis));
        
        titleText.add(createTextInputCssClassModifier());
        abstractArea.add(createTextInputCssClassModifier());
        modalitiesText.add(createTextInputCssClassModifier());
        patientSpeciesText.add(createTextInputCssClassModifier());
        authornameText.add(createTextInputCssClassModifier());
        authoraffiliationText.add(createTextInputCssClassModifier());
        authorcontactArea.add(createTextInputCssClassModifier());
        
        diagConfirmedChkBox.setEnabled(editing);
        
        keywordInput.getInputComponent().setVisible(editing);
        anatomyInput.getInputComponent().setVisible(editing);
        pathologyInput.getInputComponent().setVisible(editing);
        findingInput.getInputComponent().setVisible(editing);
        diffDiagInput.getInputComponent().setVisible(editing);
        diagInput.getInputComponent().setVisible(editing);
        categoryChoice.setVisible(editing);
        levelChoice.setVisible(editing);
        patientSexChoice.setVisible(editing);
        
        keywordLabel.setVisible(!editing);
        anatomyLabel.setVisible(!editing);
        pathologyLabel.setVisible(!editing);
        findingLabel.setVisible(!editing);
        diffDiagLabel.setVisible(!editing);
        diagLabel.setVisible(!editing);
        categoryLabel.setVisible(!editing);
        levelLabel.setVisible(!editing);
        patientSexLabel.setVisible(!editing);
        
        add(titleText);
        add(abstractArea);
        add(authornameText);
        add(authoraffiliationText);
        add(authorcontactArea);
        add(keywordLabel);
        add(keywordInput.getInputComponent());
        add(anatomyLabel);
        add(anatomyInput.getInputComponent());
        add(pathologyLabel);
        add(pathologyInput.getInputComponent());
        add(findingRow);
        add(diffDiagRow);
        add(diagRow);
        add(diagConfirmedRow);
        add(categoryChoice);
        add(categoryLabel);
        add(levelChoice);
        add(levelLabel);
        add(patientSexChoice);
        add(patientSexLabel);
        add(patientSpeciesText);
        add(modalitiesText);
        add(imageCountLabel);
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
