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
import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.dcm4chee.web.dao.tc.TCQueryFilterKey;
import org.dcm4chee.web.war.tc.keywords.TCKeywordCatalogueProvider;

/**
 * @author Bernhard Ableitinger <bernhard.ableitinger@agfa.com>
 * @version $Revision$ $Date$
 * @since May 27, 2011
 */
public class TCDetailsInfoTab extends Panel {

    private static final long serialVersionUID = 1L;

    private static final TCKeywordCatalogueProvider catProv = TCKeywordCatalogueProvider
            .getInstance();

    public TCDetailsInfoTab(final String id) {
        super(id);

        WebMarkupContainer titleWmc = new WebMarkupContainer(
                "details-title-row");
        titleWmc.add(new Label("details-title", new Model<String>() {

            private static final long serialVersionUID = 1L;

            @Override
            public String getObject() {
                return getStringValue(TCQueryFilterKey.Title);
            }
        }).add(new AbstractBehavior() {

            private static final long serialVersionUID = 1L;

            @Override
            public void onComponentTag(Component c, ComponentTag tag) {
                tag.put("title", getStringValue(TCQueryFilterKey.Title));
            }
        }));

        WebMarkupContainer abstractWmc = new WebMarkupContainer(
                "details-abstract-row");
        abstractWmc.add(new MultiLineLabel("details-abstract",
                new Model<String>() {

                    private static final long serialVersionUID = 1L;

                    @Override
                    public String getObject() {
                        return getStringValue(TCQueryFilterKey.Abstract);
                    }
                }));

        WebMarkupContainer keywordWmc = new WebMarkupContainer(
                "details-keyword-row");
        keywordWmc.add(new Label("details-keyword", new Model<String>() {

            private static final long serialVersionUID = 1L;

            @Override
            public String getObject() {
                return getShortStringValue(TCQueryFilterKey.Keyword);
            }
        }).add(new AbstractBehavior() {

            private static final long serialVersionUID = 1L;

            @Override
            public void onComponentTag(Component c, ComponentTag tag) {
            	String s = getTooltipString(TCQueryFilterKey.Keyword);
            	if (s!=null && !s.isEmpty())
            	{
            		tag.put("title", s); //$NON-NLS-1$
            	}
            }
        }));

        WebMarkupContainer anatomyWmc = new WebMarkupContainer(
                "details-anatomy-row");
        anatomyWmc.add(new Label("details-anatomy", new Model<String>() {

            private static final long serialVersionUID = 1L;

            @Override
            public String getObject() {
                return getShortStringValue(TCQueryFilterKey.Anatomy);
            }
        }).add(new AbstractBehavior() {

            private static final long serialVersionUID = 1L;

            @Override
            public void onComponentTag(Component c, ComponentTag tag) {
            	String s = getTooltipString(TCQueryFilterKey.Anatomy);
            	if (s!=null && !s.isEmpty())
            	{
            		tag.put("title", s); //$NON-NLS-1$
            	}
            }
        }));

        WebMarkupContainer pathologyWmc = new WebMarkupContainer(
                "details-pathology-row");
        pathologyWmc.add(new Label("details-pathology", new Model<String>() {

            private static final long serialVersionUID = 1L;

            @Override
            public String getObject() {
                return getShortStringValue(TCQueryFilterKey.Pathology);
            }
        }).add(new AbstractBehavior() {

            private static final long serialVersionUID = 1L;

            @Override
            public void onComponentTag(Component c, ComponentTag tag) {
            	String s = getTooltipString(TCQueryFilterKey.Pathology);
            	if (s!=null && !s.isEmpty())
            	{
            		tag.put("title", s); //$NON-NLS-1$
            	}
            }
        }));

        WebMarkupContainer findingWmc = new WebMarkupContainer(
                "details-finding-row");
        findingWmc.add(new MultiLineLabel("details-finding",
                new Model<String>() {

                    private static final long serialVersionUID = 1L;

                    @Override
                    public String getObject() {
                        return getShortStringValue(TCQueryFilterKey.Finding);
                    }
                }).add(new AbstractBehavior() {

            private static final long serialVersionUID = 1L;

            @Override
            public void onComponentTag(Component c, ComponentTag tag) {
            	String s = getTooltipString(TCQueryFilterKey.Finding);
            	if (s!=null && !s.isEmpty())
            	{
            		tag.put("title", s); //$NON-NLS-1$
            	}
            }
        }));

        WebMarkupContainer diagnosisWmc = new WebMarkupContainer(
                "details-diagnosis-row");
        diagnosisWmc.add(new Label("details-diagnosis", new Model<String>() {

            private static final long serialVersionUID = 1L;

            @Override
            public String getObject() {
                return getShortStringValue(TCQueryFilterKey.Diagnosis);
            }
        }).add(new AbstractBehavior() {

            private static final long serialVersionUID = 1L;

            @Override
            public void onComponentTag(Component c, ComponentTag tag) {
            	String s = getTooltipString(TCQueryFilterKey.Diagnosis);
            	if (s!=null && !s.isEmpty())
            	{
            		tag.put("title", s); //$NON-NLS-1$
            	}
            }
        }));

        WebMarkupContainer diffdiagnosisWmc = new WebMarkupContainer(
                "details-diffdiagnosis-row");
        diffdiagnosisWmc.add(new Label("details-diffdiagnosis",
                new Model<String>() {

                    private static final long serialVersionUID = 1L;

                    @Override
                    public String getObject() {
                        return getShortStringValue(TCQueryFilterKey.DifferentialDiagnosis);
                    }
                }).add(new AbstractBehavior() {

            private static final long serialVersionUID = 1L;

            @Override
            public void onComponentTag(Component c, ComponentTag tag) {
            	String s = getTooltipString(TCQueryFilterKey.DifferentialDiagnosis);
            	if (s!=null && !s.isEmpty())
            	{
            		tag.put("title", s); //$NON-NLS-1$
            	}
            }
        }));

        WebMarkupContainer diagnosisConfirmedWmc = new WebMarkupContainer(
                "details-diagnosis-confirmed-row");
        diagnosisConfirmedWmc.add(new Label("details-diagnosis-confirmed",
                new Model<String>() {

                    private static final long serialVersionUID = 1L;

                    @Override
                    public String getObject() {
                        return getStringValue(TCQueryFilterKey.DiagnosisConfirmed);
                    }
                }));

        WebMarkupContainer categoryWmc = new WebMarkupContainer(
                "details-category-row");
        categoryWmc.add(new Label("details-category", new Model<String>() {

            private static final long serialVersionUID = 1L;

            @Override
            public String getObject() {
                return getStringValue(TCQueryFilterKey.Category);
            }
        }).add(new AbstractBehavior() {

            private static final long serialVersionUID = 1L;

            @Override
            public void onComponentTag(Component c, ComponentTag tag) {
            	String s = getTooltipString(TCQueryFilterKey.Category);
            	if (s!=null && !s.isEmpty())
            	{
            		tag.put("title", s); //$NON-NLS-1$
            	}
            }
        }));

        WebMarkupContainer levelWmc = new WebMarkupContainer(
                "details-level-row");
        levelWmc.add(new Label("details-level", new Model<String>() {

            private static final long serialVersionUID = 1L;

            @Override
            public String getObject() {
                return getStringValue(TCQueryFilterKey.Level);
            }
        }).add(new AbstractBehavior() {

            private static final long serialVersionUID = 1L;

            @Override
            public void onComponentTag(Component c, ComponentTag tag) {
            	String s = getTooltipString(TCQueryFilterKey.Level);
            	if (s!=null && !s.isEmpty())
            	{
            		tag.put("title", s); //$NON-NLS-1$
            	}
            }
        }));

        WebMarkupContainer modalitiesWmc = new WebMarkupContainer(
                "details-modalities-row");
        modalitiesWmc.add(new Label("details-modalities", new Model<String>() {

            private static final long serialVersionUID = 1L;

            @Override
            public String getObject() {
                return getStringValue(TCQueryFilterKey.AcquisitionModality);
            }
        }).add(new AbstractBehavior() {

            private static final long serialVersionUID = 1L;

            @Override
            public void onComponentTag(Component c, ComponentTag tag) {
            	String s = getTooltipString(TCQueryFilterKey.AcquisitionModality);
            	if (s!=null && !s.isEmpty())
            	{
            		tag.put("title", s); //$NON-NLS-1$
            	}
            }
        }));

        WebMarkupContainer patientSexWmc = new WebMarkupContainer(
                "details-patient-sex-row");
        patientSexWmc.add(new Label("details-patient-sex", new Model<String>() {

            private static final long serialVersionUID = 1L;

            @Override
            public String getObject() {
                return getStringValue(TCQueryFilterKey.PatientSex);
            }
        }));
        
        WebMarkupContainer patientAgeWmc = new WebMarkupContainer(
                "details-patient-age-row");
        patientAgeWmc.add(new Label("details-patient-age", new Model<String>() {

            private static final long serialVersionUID = 1L;

            @Override
            public String getObject() {
                return TCPatientAgeUtilities.format(
                		getTCObject().getPatientAge());
            }
        }));

        WebMarkupContainer patientSpeciesWmc = new WebMarkupContainer(
                "details-patient-species-row");
        patientSpeciesWmc.add(new Label("details-patient-species",
                new Model<String>() {

                    private static final long serialVersionUID = 1L;

                    @Override
                    public String getObject() {
                        return getStringValue(TCQueryFilterKey.PatientSpecies);
                    }
                }).add(new AbstractBehavior() {

            private static final long serialVersionUID = 1L;

            @Override
            public void onComponentTag(Component c, ComponentTag tag) {
            	String s = getTooltipString(TCQueryFilterKey.PatientSpecies);
            	if (s!=null && !s.isEmpty())
            	{
            		tag.put("title", s); //$NON-NLS-1$
            	}
            }
        }));

        if (catProv != null) {
            diffdiagnosisWmc.setVisible(catProv
                    .hasCatalogue(TCQueryFilterKey.DifferentialDiagnosis));
            diagnosisWmc.setVisible(catProv
                    .hasCatalogue(TCQueryFilterKey.Diagnosis));
            diagnosisConfirmedWmc.setVisible(catProv
                    .hasCatalogue(TCQueryFilterKey.Diagnosis));
        }

        add(titleWmc);
        add(abstractWmc);
        add(keywordWmc);
        add(anatomyWmc);
        add(pathologyWmc);
        add(diagnosisWmc);
        add(diagnosisConfirmedWmc);
        add(diffdiagnosisWmc);
        add(findingWmc);
        add(categoryWmc);
        add(levelWmc);
        add(modalitiesWmc);
        add(patientSexWmc);
        add(patientAgeWmc);
        add(patientSpeciesWmc);
    }

    private TCObject getTCObject() {
        return (TCObject) getDefaultModelObject();
    }

    private String getShortStringValue(TCQueryFilterKey key) {
        TCObject tc = getTCObject();

        String s = tc != null ? tc.getValueAsLocalizedString(key, this, true)
                : null;

        return s != null ? s : "-";
    }
    
    private String getStringValue(TCQueryFilterKey key) {
        TCObject tc = getTCObject();

        String s = tc != null ? tc.getValueAsLocalizedString(key, this, false)
                : null;

        return s != null ? s : "-";
    }
    
    private String getTooltipString(TCQueryFilterKey key) {
        TCObject tc = getTCObject();
        return tc != null ? tc.getValueAsLocalizedString(key, this, false) : null;
    }
}
