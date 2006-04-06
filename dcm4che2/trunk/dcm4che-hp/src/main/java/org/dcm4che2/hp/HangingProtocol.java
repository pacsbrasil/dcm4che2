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
 * Gunter Zeilinger, Huetteldorferstr. 24/10, 1150 Vienna/Austria/Europe.
 * Portions created by the Initial Developer are Copyright (C) 2002-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunterze@gmail.com>
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

package org.dcm4che2.hp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4che2.hp.spi.HPCategorySpi;
import org.dcm4che2.hp.spi.HPComparatorSpi;
import org.dcm4che2.hp.spi.HPRegistry;
import org.dcm4che2.hp.spi.HPSelectorSpi;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision$ $Date$
 * @since Jul 30, 2005
 */
public class HangingProtocol {

    private final DicomObject dcmobj;

    private List definitions;

    private List screenDefs;

    private List imageSets;

    private List displaySets;

    private List scrollingGroups;

    private List navigationGroups;

    private int maxPresGroup = 0;

    public HangingProtocol(DicomObject dcmobj) {
        this.dcmobj = dcmobj;
        init();
    }

    public HangingProtocol() {
        definitions = new ArrayList();
        screenDefs = new ArrayList();
        imageSets = new ArrayList();
        displaySets = new ArrayList();
        scrollingGroups = new ArrayList();
        navigationGroups = new ArrayList();
        this.dcmobj = new BasicDicomObject();
        dcmobj.putSequence(Tag.HangingProtocolDefinitionSequence);
        dcmobj.putSequence(Tag.NominalScreenDefinitionSequence);
        dcmobj.putSequence(Tag.ImageSetsSequence);
        dcmobj.putSequence(Tag.DisplaySetsSequence);
    }

    public DicomObject getDicomObject() {
        return dcmobj;
    }

    public String getHangingProtocolName() {
        return dcmobj.getString(Tag.HangingProtocolName);
    }

    public void setHangingProtocolName(String name) {
        dcmobj.putString(Tag.HangingProtocolName, VR.SH, name);
    }

    public String getHangingProtocolDescription() {
        return dcmobj.getString(Tag.HangingProtocolDescription);
    }

    public void setHangingProtocolDescription(String description) {
        dcmobj.putString(Tag.HangingProtocolDescription, VR.LO, description);
    }

    public String getHangingProtocolLevel() {
        return dcmobj.getString(Tag.HangingProtocolLevel);
    }

    public void setHangingProtocolLevel(String level) {
        dcmobj.putString(Tag.HangingProtocolLevel, VR.CS, level);
    }

    public String getHangingProtocolCreator() {
        return dcmobj.getString(Tag.HangingProtocolCreator);
    }

    public void setHangingProtocolCreator(String creator) {
        dcmobj.putString(Tag.HangingProtocolCreator, VR.LO, creator);
    }

    public Date getHangingProtocolCreationDatetime() {
        return dcmobj.getDate(Tag.HangingProtocolCreationDatetime);
    }

    public void setHangingProtocolCreationDatetime(Date datetime) {
        dcmobj.putDate(Tag.HangingProtocolCreationDatetime, VR.DT, datetime);
    }

    public int getNumberOfPriorsReferenced() {
        return dcmobj.getInt(Tag.NumberofPriorsReferenced);
    }

    public void setNumberofPriorsReferenced(int priors) {
        dcmobj.putInt(Tag.NumberofPriorsReferenced, VR.US, priors);
    }

    public int getNumberOfScreens() {
        return dcmobj.getInt(Tag.NumberofScreens);
    }

    public void setNumberofScreens(int screens) {
        dcmobj.putInt(Tag.NumberofScreens, VR.US, screens);
    }

    public Code getHangingProtocolUserIdentificationCode() {
        DicomObject item = dcmobj
                .getNestedDicomObject(Tag.HangingProtocolUserIdentificationCodeSequence);
        return item != null ? new Code(item) : null;
    }

    public void setHangingProtocolUserIdentificationCodeSequence(Code user) {
        dcmobj.putNestedDicomObject(
                Tag.HangingProtocolUserIdentificationCodeSequence, user
                        .getDicomObject());
    }

    public ReferencedSOP getSourceHangingProtocol() {
        DicomObject item = dcmobj
                .getNestedDicomObject(Tag.SourceHangingProtocolSequence);
        return item != null ? new ReferencedSOP(item) : null;
    }

    public void setSourceHangingProtocol(ReferencedSOP sop) {
        dcmobj.putNestedDicomObject(Tag.SourceHangingProtocolSequence, sop
                .getDicomObject());
    }

    public String getHangingProtocolUserGroupName() {
        return dcmobj.getString(Tag.HangingProtocolUserGroupName);
    }

    public void setHangingProtocolUserGroupName(String name) {
        dcmobj.putString(Tag.HangingProtocolUserGroupName, VR.LO, name);
    }

    public String getPartialDataDisplayHandling() {
        return dcmobj.getString(Tag.PartialDataDisplayHandling);
    }

    public void setPartialDataDisplayHandling(String type) {
        dcmobj.putString(Tag.PartialDataDisplayHandling, VR.CS, type);
    }

    public List getHangingProtocolDefinitions() {
        return Collections.unmodifiableList(definitions);
    }

    public void addHangingProtocolDefinition(HPDefinition def) {
        if (def == null)
            throw new NullPointerException();

        getHangingProtocolDefinationSequence().addDicomObject(
                def.getDicomObject());
        definitions.add(def);
    }

    public List getImageSets() {
        return Collections.unmodifiableList(imageSets);
    }

    public void addImageSet(HPImageSet imageSet) {
        if (imageSet == null)
            throw new NullPointerException();

        imageSet.setImageSetNumber(imageSets.size() + 1);
        getImageSetsSequence().addDicomObject(imageSet.getDicomObject());
        imageSets.add(imageSet);
    }

    public List getNominalScreenDefinitions() {
        return Collections.unmodifiableList(screenDefs);
    }

    public void addNominalScreenDefinition(HPScreenDefinition def) {
        if (def == null)
            throw new NullPointerException();

        getNominalScreenDefinitionSequence().addDicomObject(
                def.getDicomObject());
        screenDefs.add(def);
    }

    public int getNumberOfPresentationGroups() {
        return maxPresGroup;
    }

    public List getDisplaySetsOfPresentationGroup(int pgNo) {
        ArrayList result = new ArrayList(displaySets.size());
        for (int i = 0, n = displaySets.size(); i < n; i++) {
            HPDisplaySet ds = (HPDisplaySet) displaySets.get(i);
            if (ds.getDisplaySetPresentationGroup() == pgNo)
                result.add(ds);
        }
        return result;
    }

    public String getDisplaySetPresentationGroupDescription(int pgNo) {
        for (int i = 0, n = displaySets.size(); i < n; i++) {
            HPDisplaySet ds = (HPDisplaySet) displaySets.get(i);
            if (ds.getDisplaySetPresentationGroup() == pgNo) {
                String desc = ds.getDisplaySetPresentationGroupDescription();
                if (desc != null)
                    return desc;
            }
        }
        return null;
    }

    public List getDisplaySets() {
        return Collections.unmodifiableList(displaySets);
    }

    public void addDisplaySet(HPDisplaySet displaySet) {
        if (displaySet == null)
            throw new NullPointerException();

        displaySet.setDisplaySetNumber(displaySets.size() + 1);
        int group = displaySet.getDisplaySetPresentationGroup();
        if (group == 0) {
            group = Math.max(maxPresGroup, 1);
            displaySet.setDisplaySetPresentationGroup(group);
        }
        maxPresGroup = Math.max(maxPresGroup, group);
        getDisplaySetsSequence().addDicomObject(displaySet.getDicomObject());
        displaySets.add(displaySet);
    }

    public List getScrollingGroups() {
        return maskNull(scrollingGroups);
    }

    public void addScrollingGroup(HPScrollingGroup scrollingGroup) {
        DicomElement sq = dcmobj.get(Tag.SynchronizedScrollingSequence);
        if (sq == null)
            sq = dcmobj.putSequence(Tag.SynchronizedScrollingSequence);
        sq.addDicomObject(scrollingGroup.getDicomObject());
        scrollingGroups.add(scrollingGroup);
    }

    public List getNavigationGroups() {
        return maskNull(navigationGroups);
    }

    private List maskNull(List list) {
        return list == null ? Collections.EMPTY_LIST : Collections
                .unmodifiableList(list);
    }

    public void addNavigationGroup(HPNavigationGroup navigationGroup) {
        DicomElement sq = dcmobj.get(Tag.NavigationIndicatorSequence);
        if (sq == null)
            sq = dcmobj.putSequence(Tag.NavigationIndicatorSequence);
        sq.addDicomObject(navigationGroup.getDicomObject());
        navigationGroups.add(navigationGroup);
    }

    private void init() {
        initHangingProtocolDefinition();
        initNominalScreenDefinition();
        initImageSets();
        initDisplaySets();
        initScrollingGroups();
        initNavigationGroups();
    }

    private void initNavigationGroups() {
        DicomElement nis = dcmobj.get(Tag.NavigationIndicatorSequence);
        if (nis == null || nis.isEmpty())
            return;

        int numNavGroups = nis.countItems();
        navigationGroups = new ArrayList(numNavGroups);
        for (int i = 0; i < numNavGroups; i++) {
            DicomObject ni = nis.getDicomObject(i);
            int[] group = ni.getInts(Tag.ReferenceDisplaySets);
            if (group == null)
                throw new IllegalArgumentException(
                        "Missing (0072,0218) Reference Display Sets");
            HPNavigationGroup ng = new HPNavigationGroup(group.length);
            int nds = ni.getInt(Tag.NavigationDisplaySet);
            if (nds != 0) {
                try {
                    ng.setNavigationDisplaySet((HPDisplaySet) displaySets
                            .get(nds - 1));
                } catch (IndexOutOfBoundsException e) {
                    throw new IllegalArgumentException(
                            "Referenced Display Set does not exists: "
                                    + ni.get(Tag.NavigationDisplaySet));
                }
            }
            for (int j = 0; j < group.length; j++) {
                try {
                    ng.addReferenceDisplaySet((HPDisplaySet) displaySets
                            .get(group[j] - 1));
                } catch (IndexOutOfBoundsException e) {
                    throw new IllegalArgumentException(
                            "Referenced Display Set does not exists: "
                                    + ni.get(Tag.ReferenceDisplaySets));
                }
            }
            navigationGroups.add(ng);
        }
    }

    private void initScrollingGroups() {
        DicomElement ssq = dcmobj.get(Tag.SynchronizedScrollingSequence);
        if (ssq == null || ssq.isEmpty())
            return;

        int numScrollingGroups = ssq.countItems();
        scrollingGroups = new ArrayList(numScrollingGroups);
        for (int i = 0; i < numScrollingGroups; i++) {
            DicomObject dssg = ssq.getDicomObject(i);
            int[] group = dssg.getInts(Tag.DisplaySetScrollingGroup);
            if (group == null)
                throw new IllegalArgumentException(
                        "Missing (0072,0212) Display Set Scrolling Group");
            if (group.length < 2)
                throw new IllegalArgumentException(""
                        + dssg.get(Tag.DisplaySetScrollingGroup));
            HPScrollingGroup sg = new HPScrollingGroup(group.length);
            for (int j = 0; j < group.length; j++) {
                try {
                    sg.addDisplaySet((HPDisplaySet) displaySets
                            .get(group[j] - 1));
                } catch (IndexOutOfBoundsException e) {
                    throw new IllegalArgumentException(
                            "Referenced Display Set does not exists: "
                                    + dssg.get(Tag.DisplaySetScrollingGroup));
                }
            }
            scrollingGroups.add(sg);
        }

    }

    private void initDisplaySets() {
        DicomElement dssq = getDisplaySetsSequence();
        if (dssq == null)
            throw new IllegalArgumentException(
                    "Missing (0072,0200) Display Sets Sequence");
        if (dssq.isEmpty())
            throw new IllegalArgumentException(
                    "Empty (0072,0200) Display Sets Sequence");
        int numDisplaySets = dssq.countItems();
        displaySets = new ArrayList(numDisplaySets);
        for (int i = 0; i < numDisplaySets; i++) {
            DicomObject ds = dssq.getDicomObject(i);
            if (ds.getInt(Tag.DisplaySetNumber) != displaySets.size() + 1) {
                throw new IllegalArgumentException(
                        "Missing or invalid (0072,0202) Display Set Number: "
                                + ds.get(Tag.DisplaySetNumber));
            }
            final int dspg = ds.getInt(Tag.DisplaySetPresentationGroup);
            if (dspg == 0)
                throw new IllegalArgumentException(
                        "Missing or invalid (0072,0204) Display Set Presentation Group: "
                                + ds.get(Tag.DisplaySetPresentationGroup));
            maxPresGroup = Math.max(maxPresGroup, dspg);
            HPImageSet is;
            try {
                is = (HPImageSet) imageSets
                        .get(ds.getInt(Tag.ImageSetNumber) - 1);
            } catch (IndexOutOfBoundsException e) {
                throw new IllegalArgumentException(
                        "Missing or invalid (0072,0032) Image Set Number: "
                                + ds.get(Tag.ImageSetNumber));
            }
            displaySets.add(new HPDisplaySet(ds, is));
        }
    }

    private void initImageSets() {
        DicomElement issq = getImageSetsSequence();
        if (issq == null)
            throw new IllegalArgumentException(
                    "Missing (0072,0020) Image Sets Sequence");
        if (issq.isEmpty())
            throw new IllegalArgumentException(
                    "Empty (0072,0020) Image Sets Sequence");
        imageSets = new ArrayList();
        for (int i = 0, n = issq.countItems(); i < n; i++) {
            DicomObject is = issq.getDicomObject(i);
            DicomElement isssq = is.get(Tag.ImageSetSelectorSequence);
            if (isssq == null)
                throw new IllegalArgumentException(
                        "Missing (0072,0022) Image Set Selector Sequence");
            if (isssq.isEmpty())
                throw new IllegalArgumentException(
                        "Empty (0072,0022) Image Set Selector Sequence");
            int isssqCount = isssq.countItems();
            List selectors = new ArrayList(isssqCount);
            for (int j = 0; j < isssqCount; j++) {
                selectors.add(HPSelectorFactory.createImageSetSelector(isssq
                        .getDicomObject(j)));
            }
            DicomElement tbissq = is.get(Tag.TimeBasedImageSetsSequence);
            if (tbissq == null)
                throw new IllegalArgumentException(
                        "Missing (0072,0030) Time Based Image Sets Sequence");
            if (tbissq.isEmpty())
                throw new IllegalArgumentException(
                        "Empty (0072,0030) Time Based Image Sets Sequence");
            for (int j = 0, m = tbissq.countItems(); j < m; j++) {
                DicomObject timeBasedSelector = tbissq.getDicomObject(j);
                if (timeBasedSelector.getInt(Tag.ImageSetNumber) != imageSets
                        .size() + 1) {
                    throw new IllegalArgumentException(
                            "Missing or invalid (0072,0032) Image Set Number: "
                                    + timeBasedSelector.get(Tag.ImageSetNumber));
                }
                imageSets.add(new HPImageSet(selectors, timeBasedSelector));
            }
        }
    }

    private void initNominalScreenDefinition() {
        DicomElement nsdsq = getNominalScreenDefinitionSequence();
        if (nsdsq == null || nsdsq.isEmpty()) {
            screenDefs = Collections.EMPTY_LIST;
        } else {
            int numScreenDef = nsdsq.countItems();
            screenDefs = new ArrayList(numScreenDef);
            for (int i = 0; i < numScreenDef; i++) {
                screenDefs.add(new HPScreenDefinition(nsdsq.getDicomObject(i)));
            }
        }
    }

    private void initHangingProtocolDefinition() {
        DicomElement defsq = getHangingProtocolDefinationSequence();
        if (defsq == null)
            throw new IllegalArgumentException(
                    "Missing (0072,000C) Hanging Protocol Definition Sequence");
        if (defsq.isEmpty())
            throw new IllegalArgumentException(
                    "Empty (0072,000C) Hanging Protocol Definition Sequence");
        int numDefinitions = defsq.countItems();
        definitions = new ArrayList(numDefinitions);
        for (int i = 0; i < numDefinitions; i++) {
            definitions.add(new HPDefinition(defsq.getDicomObject(i)));
        }
    }

    public DicomElement getDisplaySetsSequence() {
        return dcmobj.get(Tag.DisplaySetsSequence);
    }

    public DicomElement getImageSetsSequence() {
        return dcmobj.get(Tag.ImageSetsSequence);
    }

    public DicomElement getNominalScreenDefinitionSequence() {
        return dcmobj.get(Tag.NominalScreenDefinitionSequence);
    }

    public DicomElement getHangingProtocolDefinationSequence() {
        return dcmobj.get(Tag.HangingProtocolDefinitionSequence);
    }

    public static void scanForPlugins(ClassLoader cl) {
        HPRegistry.getHPRegistry().registerApplicationClasspathSpis(cl);
    }

    public static HPSelectorSpi getHPSelectorSpi(String category) {
        return (HPSelectorSpi) getHPCategorySpi(HPSelectorSpi.class, category);
    }

    public static HPComparatorSpi getHPComparatorSpi(String category) {
        return (HPComparatorSpi) getHPCategorySpi(HPComparatorSpi.class,
                category);
    }

    private static HPCategorySpi getHPCategorySpi(Class serviceClass,
            final String category) {
        Iterator iter = HPRegistry.getHPRegistry().getServiceProviders(
                serviceClass, new HPRegistry.Filter() {
                    public boolean filter(Object provider) {
                        return ((HPCategorySpi) provider)
                                .containsCategory(category);
                    }
                }, true);
        return (HPCategorySpi) (iter.hasNext() ? iter.next() : null);
    }

    public static String[] getSupportedHPSelectorCategories() {
        return (String[]) getSupportedHPCategories(HPSelectorSpi.class);
    }

    public static String[] getSupportedHPComparatorCategories() {
        return (String[]) getSupportedHPCategories(HPComparatorSpi.class);
    }

    private static String[] getSupportedHPCategories(Class serviceClass) {
        Iterator iter = HPRegistry.getHPRegistry().getServiceProviders(
                serviceClass, true);
        HashSet set = new HashSet();
        while (iter.hasNext()) {
            HPCategorySpi spi = (HPCategorySpi) iter.next();
            String[] ss = spi.getCategories();
            for (int i = 0; i < ss.length; i++) {
                set.add(ss[i]);
            }
        }
        return (String[]) set.toArray(new String[set.size()]);
    }

}
