/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4che2.hp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.ResourceLocator;
import org.dcm4che2.data.Tag;
import org.dcm4che2.hp.spi.HPComparatorSpi;
import org.dcm4che2.hp.spi.HPSelectorSpi;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision$ $Date$
 * @since Jul 30, 2005
 *
 */
public class HangingProtocol {

    public static final String MAINTAIN_LAYOUT = "MAINTAIN_LAYOUT";
    public static final String ADAPT_LAYOUT = "ADAPT_LAYOUT";

    private static final List[] EMPTY_LIST_ARRAY = {};

    private final DicomObject dcmobj;
    private List definitions;
    private List screenDefs;
    private List imageSets;
    private List displaySets;
    private int maxPresGroup = 0;

    public HangingProtocol(DicomObject dcmobj) {
        this.dcmobj = dcmobj;
        init();
    }
    
    public DicomObject getDicomObject() {
        return dcmobj;
    }
    
    public String getHangingProtocolName() {
        return dcmobj.getString(Tag.HangingProtocolName);
    }

    public String getHangingProtocolDescription() {
        return dcmobj.getString(Tag.HangingProtocolDescription);
    }
    
    public String getHangingProtocolLevel() {
        return dcmobj.getString(Tag.HangingProtocolLevel);
    }

    public String getHangingProtocolCreator() {
        return dcmobj.getString(Tag.HangingProtocolCreator);
    }
    
    public Date getHangingProtocolCreationDatetime() {
        return dcmobj.getDate(Tag.HangingProtocolCreationDatetime);
    }
    
    public int getNumberOfPriorsReferenced() {
        return dcmobj.getInt(Tag.NumberofPriorsReferenced);
    }
    
    public int getNumberOfScreens() {
        return dcmobj.getInt(Tag.NumberofScreens);
    }
    
    public Code getHangingProtocolUserIdentificationCode() {
        DicomObject item = dcmobj.getItem(
                Tag.HangingProtocolUserIdentificationCodeSequence);
        return item != null ? new Code(item) : null;
    }

    public ReferencedSOP getSourceHangingProtocolSOPInstanceUID() {
        DicomObject item = dcmobj.getItem(Tag.SourceHangingProtocolSequence);
        return item != null ? new ReferencedSOP(item) : null;
    }
    
    public String getHangingProtocolUserGroupName() {
        return dcmobj.getString(Tag.HangingProtocolUserGroupName);        
    }
    
    public List getHangingProtocolDefinitions() {
        return Collections.unmodifiableList(definitions);
    }

    public List getImageSets() {
        return Collections.unmodifiableList(imageSets);
    }

    public List getNominalScreenDefinitions() {
        return Collections.unmodifiableList(screenDefs);
    }
    
    public int countPresentationGroups() {
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
        String desc = null;
        for (int i = 0, n = displaySets.size(); desc == null && i < n; i++) {
            HPDisplaySet ds = (HPDisplaySet) displaySets.get(i);
            if (ds.getDisplaySetPresentationGroup() == pgNo) {
                desc = ds.getDisplaySetPresentationGroupDescription();
            }
        }
        return null;
    }
    
    public List getDisplaySets() {
        return Collections.unmodifiableList(displaySets);
    }
 
    public String getPartialDataDisplayHandling() {
        return dcmobj.getString(Tag.PartialDataDisplayHandling);
    }
    
    public boolean isPartialDataDisplayHandling(String type) {
        return type.equals(getPartialDataDisplayHandling());
    }

    public List[] getDisplaySetScrollingGroups() {
        DicomElement sq = dcmobj.get(Tag.SynchronizedScrollingSequence);
        if (sq != null && !sq.isEmpty())
            return EMPTY_LIST_ARRAY;
        List[] result = new List[sq.countItems()];
        for (int i = 0, n = sq.countItems(); i < n; i++) {
            int[] group = sq.getItem(i).getInts(Tag.DisplaySetScrollingGroup);
            result[i] = new ArrayList(group.length);
            for (int j = 0; j < group.length; j++) {
                result[i].add(displaySets.get(group[j]));
            }
        }
        return result;
    }   

    public List[] getNavigationDisplaySets() {
        DicomElement sq = dcmobj.get(Tag.NavigationIndicatorSequence);
        if (sq != null && !sq.isEmpty())
            return EMPTY_LIST_ARRAY;
        List[] result = new List[sq.countItems()];
        for (int i = 0, n = sq.countItems(); i < n; i++) {
            DicomObject item = sq.getItem(i);
            int[] group = item.getInts(Tag.ReferenceDisplaySets);
            result[i] = new ArrayList(group.length + 1);
            int nav = item.getInt(Tag.NavigationDisplaySet);
            result[i].add(nav != 0 ? displaySets.get(nav) : null);
            for (int j = 0; j < group.length; j++) {
                result[i].add(displaySets.get(group[j]));
            }
        }
        return result;
    }   
    
    private void init() {
        DicomElement defsq = dcmobj.get(Tag.HangingProtocolDefinitionSequence);
        if (defsq == null)
            throw new IllegalArgumentException(
                    "Missing (0072,000C) Hanging Protocol Definition Sequence");
        if (defsq.isEmpty())
            throw new IllegalArgumentException(
                    "Empty (0072,000C) Hanging Protocol Definition Sequence");
        int numDefinitions = defsq.countItems();
        definitions = new ArrayList(numDefinitions);
        for (int i = 0; i < numDefinitions; i++) {
            definitions.add(new HPDefinition(defsq.getItem(i)));
        }
        DicomElement nsdsq = dcmobj.get(Tag.NominalScreenDefinitionSequence);
        if (nsdsq == null || nsdsq.isEmpty()) {
            screenDefs = Collections.EMPTY_LIST;
        } else {
            int numScreenDef = nsdsq.countItems();
            screenDefs = new ArrayList(numScreenDef);
            for (int i = 0; i < numScreenDef; i++) {
                screenDefs.add(new HPScreenDefinition(nsdsq.getItem(i)));
            }
        }
        DicomElement issq = dcmobj.get(Tag.ImageSetsSequence);
        if (issq == null)
            throw new IllegalArgumentException(
                    "Missing (0072,0020) Image Sets Sequence");
        if (issq.isEmpty())
            throw new IllegalArgumentException(
                    "Empty (0072,0020) Image Sets Sequence");
        imageSets = new ArrayList();
        for (int i = 0, n = issq.countItems(); i < n; i++) {
            DicomObject is = issq.getItem(i);
            DicomElement isssq = is.get(Tag.ImageSetSelectorSequence);
            if (isssq == null)
                throw new IllegalArgumentException(
                        "Missing (0072,0022) Image Set AbstractHPSelector Sequence");
            if (isssq.isEmpty())
                throw new IllegalArgumentException(
                        "Empty (0072,0022) Image Set AbstractHPSelector Sequence");
            int isssqCount = isssq.countItems();
            List selectors = new ArrayList(isssqCount);
            for (int j = 0; j < isssqCount; j++) {
                selectors.add(AbstractHPSelector.createImageSetSelector(isssq.getItem(j)));
            }
            DicomElement tbissq = is.get(Tag.TimeBasedImageSetsSequence);
            if (tbissq == null)
                throw new IllegalArgumentException(
                        "Missing (0072,0030) Time Based Image Sets Sequence");
            if (tbissq.isEmpty())
                throw new IllegalArgumentException(
                        "Empty (0072,0030) Time Based Image Sets Sequence");
            for (int j = 0, m = tbissq.countItems(); j < m; j++) {
                DicomObject timeBasedSelector = tbissq.getItem(j);
                if (timeBasedSelector.getInt(Tag.ImageSetNumber) 
                        != imageSets.size() + 1) {
                    throw new IllegalArgumentException(
                            "Missing or invalid (0072,0032) Image Set Number: " 
                                    + timeBasedSelector.get(Tag.ImageSetNumber));
                }
                imageSets.add(new HPImageSet(selectors, timeBasedSelector));
            }                
        }
        DicomElement dssq = dcmobj.get(Tag.DisplaySetsSequence);
        if (dssq == null)
            throw new IllegalArgumentException(
                    "Missing (0072,0200) Display Sets Sequence");
        if (dssq.isEmpty())
            throw new IllegalArgumentException(
                    "Empty (0072,0200) Display Sets Sequence");
        int numDisplaySets = dssq.countItems();
        displaySets = new ArrayList(numDisplaySets);
        for (int i = 0; i < numDisplaySets; i++) {
            DicomObject ds = dssq.getItem(i);
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
                is = (HPImageSet) imageSets.get(ds
                        .getInt(Tag.ImageSetNumber)-1);
            } catch (IndexOutOfBoundsException e) {
                throw new IllegalArgumentException(
                        "Missing or invalid (0072,0032) Image Set Number: " 
                                + ds.get(Tag.ImageSetNumber));
            }
            displaySets.add(new HPDisplaySet(ds, is));
        }
    }
    
    private static Hashtable selectorSpis; 
    private static Hashtable comparatorSpis;

    static {
        scanForPlugins();
    }
    
    public static void scanForPlugins() {
        scanForSelectorSpis();
        scanForComparatorSpis();
    }

    private static void scanForSelectorSpis() {
        Hashtable newSelectorSpis = new Hashtable();
        List list = ResourceLocator.findResources(HPSelectorSpi.class);
        for (int i = 0, n = list.size(); i < n; ++i) {
            HPSelectorSpi spi = (HPSelectorSpi) ResourceLocator.createInstance(
                    (String) list.get(i));
            String[] categories = spi.getCategories();
            for (int j = 0; j < categories.length; j++) {
                newSelectorSpis.put(categories[j], spi);
            }
        }
        selectorSpis = newSelectorSpis;
    }
    
    private static void scanForComparatorSpis() {
        Hashtable newComparatorSpis = new Hashtable();
        List list = ResourceLocator.findResources(HPComparatorSpi.class);
        for (int i = 0, n = list.size(); i < n; ++i) {
            HPComparatorSpi spi = (HPComparatorSpi) ResourceLocator.createInstance(
                    (String) list.get(i));
            String[] categories = spi.getCategories();
            for (int j = 0; j < categories.length; j++) {
                newComparatorSpis.put(categories[j], spi);
            }
        }
        comparatorSpis = newComparatorSpis;
    }
    
    public static HPSelectorSpi getHPSelectorSpi(String category) {
        return (HPSelectorSpi) selectorSpis.get(category);
    }
        
    public static HPComparatorSpi getHPComparatorSpi(String category) {
        return (HPComparatorSpi) comparatorSpis.get(category);
    }
    
    public static HPSelector createFilterByCategory(DicomObject filterOp) {
        HPSelectorSpi spi = getHPSelectorSpi(
                filterOp.getString(Tag.FilterbyCategory));
        if (spi == null)
            throw new IllegalArgumentException(
                    "Unsupported Filter-by Category: " 
                            + filterOp.get(Tag.FilterbyCategory));
        return spi.createHPSelector(filterOp);
    }

    public static HPComparator createSortByCategory(DicomObject sortingOp) {
        HPComparatorSpi spi = getHPComparatorSpi(
                sortingOp.getString(Tag.SortbyCategory));
        if (spi == null)
            throw new IllegalArgumentException(
                    "Unsupported Sort-by Category: " 
                            + sortingOp.get(Tag.SortbyCategory));
        return spi.createHPComparator(sortingOp);
    }

}
