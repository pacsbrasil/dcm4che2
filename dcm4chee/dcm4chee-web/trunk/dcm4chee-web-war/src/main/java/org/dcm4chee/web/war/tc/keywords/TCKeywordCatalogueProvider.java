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
package org.dcm4chee.web.war.tc.keywords;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.wicket.resource.loader.PackageStringResourceLoader;
import org.dcm4chee.web.common.util.FileUtils;
import org.dcm4chee.web.dao.tc.TCQueryFilterKey;
import org.dcm4chee.web.war.config.delegate.WebCfgDelegate;
import org.dcm4chee.web.war.config.delegate.WebCfgDelegate.KeywordCatalogue;
import org.dcm4chee.web.war.tc.TCDetails.DicomCode;
import org.dcm4chee.web.war.tc.TCPanel;
import org.dcm4chee.web.war.tc.keywords.acr.ACRCatalogue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Bernhard Ableitinger <bernhard.ableitinger@agfa.com>
 * @version $Revision$ $Date$
 * @since May 30, 2011
 */
public class TCKeywordCatalogueProvider {

    private final static Logger log = LoggerFactory
            .getLogger(TCKeywordCatalogueProvider.class);

    private final static String ID_DELIMITER = "$";

    private static TCKeywordCatalogueProvider instance;

    private final WebCfgDelegate configDelegate;

    private Map<String, TCKeywordCatalogue> catalogues;

    private Map<TCQueryFilterKey, TCKeywordCatalogue> catalogueMap;

    private TCKeywordCatalogueProvider() {
        configDelegate = WebCfgDelegate.getInstance();
        catalogues = new HashMap<String, TCKeywordCatalogue>();
        catalogueMap = new HashMap<TCQueryFilterKey, TCKeywordCatalogue>();

        Map<String, KeywordCatalogue> configuredCatalogues = configDelegate
                .getTCKeywordCatalogues();

        if (configuredCatalogues != null && !configuredCatalogues.isEmpty()) {
            // read in ACR keyword catalogue
            ACRCatalogue acr = ACRCatalogue.getInstance();
            catalogues.put(acr.getDesignatorId() + ID_DELIMITER + acr.getId(),
                    acr);

            log.info("Added teaching-file keyword catalogue: " + acr);

            // read in custom keyword catalogues
            List<TCKeywordCatalogue> customCatalogues = findCustomInstances(configDelegate
                    .getTCKeywordCataloguesPath());
            for (TCKeywordCatalogue c : customCatalogues) {
                catalogues.put(c.getDesignatorId() + ID_DELIMITER + c.getId(),
                        c);
            }

            for (Map.Entry<String, KeywordCatalogue> me : configuredCatalogues
                    .entrySet()) {
                try {
                    TCQueryFilterKey key = TCQueryFilterKey
                            .valueOf(me.getKey());

                    if (key != null) {
                        TCKeywordCatalogue catalogue = catalogues.get(me
                                .getValue().getDesignator()
                                + ID_DELIMITER
                                + me.getValue().getId());
                        if (catalogue != null) {
                            catalogueMap.put(key, catalogue);
                        } else {
                            log.warn("Configured keyword catalogue not supported: "
                                    + me.getValue().toString());
                        }
                    } else {
                        log.warn("Configured keyword catalogue attribute not supported: "
                                + me.getKey());
                    }
                } catch (Exception e) {
                    log.error("Initializing TC keyword provider failed!", e);
                }
            }
        }
    }

    public static synchronized TCKeywordCatalogueProvider getInstance() {
        if (instance == null) {
            instance = new TCKeywordCatalogueProvider();
        }

        return instance;
    }

    public boolean hasCatalogue(TCQueryFilterKey key) {
        return catalogueMap.containsKey(key);
    }

    public TCKeywordCatalogue getCatalogue(TCQueryFilterKey key) {
        return catalogueMap.get(key);
    }

    private List<TCKeywordCatalogue> findCustomInstances(String path) {
        if (path != null) {
            File file = FileUtils.resolve(new File(path));

            if (file.exists()) {
                List<TCKeywordCatalogue> catalogues = new ArrayList<TCKeywordCatalogue>();

                File[] candidates = file.isDirectory() ? file.listFiles()
                        : new File[] { file };
                if (candidates != null) {
                    for (File f : candidates) {
                        if (f.getAbsolutePath().endsWith(".xml")) {
                            FileInputStream fis = null;

                            try {
                                fis = new FileInputStream(f);

                                DocumentBuilderFactory dbf = DocumentBuilderFactory
                                        .newInstance();

                                dbf.setValidating(false);
                                dbf.setIgnoringComments(true);
                                dbf.setIgnoringElementContentWhitespace(true);
                                dbf.setNamespaceAware(true);

                                Document doc = dbf.newDocumentBuilder().parse(
                                        fis);

                                if (doc.getElementsByTagName("simple-list").getLength() > 0) //$NON-NLS-1$
                                {
                                    TCKeywordCatalogue cat = TCKeywordCatalogueXMLList
                                            .createInstance(doc);

                                    catalogues.add(cat);

                                    log.info("Added teaching-file keyword catalogue: "
                                            + cat);
                                } else if (doc.getElementsByTagName(
                                        "simple-tree").getLength() > 0) //$NON-NLS-1$
                                {
                                    TCKeywordCatalogue cat = TCKeywordCatalogueXMLTree
                                            .createInstance(doc);

                                    catalogues.add(cat);

                                    log.info("Added teaching-file keyword catalogue: "
                                            + cat);
                                } else {
                                    throw new UnsupportedOperationException(
                                            "Unsupported XML format!"); //$NON-NLS-1$
                                }
                            } catch (Exception e) {
                                log.error(
                                        "Parsing teaching-file keyword catalogue failed! Invalid syntax in file "
                                                + file.getAbsolutePath(), e);
                            } finally {
                                if (fis != null) {
                                    try {
                                        fis.close();
                                    } catch (Exception e) {
                                        log.error(null, e);
                                    }
                                }
                            }
                        }
                    }
                }

                return catalogues;
            }
        }

        return Collections.emptyList();
    }

    private static class TCKeywordCatalogueXMLList extends TCKeywordCatalogue {
        private List<TCKeyword> keywords;

        private TCKeywordCatalogueXMLList(String id, String designatorId,
                String designatorName, List<TCKeyword> keywords) {
            super(id, designatorId, designatorName);

            this.keywords = keywords;
        }

        public static TCKeywordCatalogueXMLList createInstance(Document doc) {
            Node rootNode = doc.getElementsByTagName("coding-system").item(0); //$NON-NLS-1$
            NamedNodeMap rootAttrs = rootNode.getAttributes();

            String systemId = rootAttrs.getNamedItem("id").getTextContent(); //$NON-NLS-1$
            String designatorId = rootAttrs
                    .getNamedItem("designator-id").getTextContent(); //$NON-NLS-1$
            String designatorName = rootAttrs
                    .getNamedItem("designator-name").getTextContent(); //$NON-NLS-1$
            String id = designatorId + ID_DELIMITER + systemId; //$NON-NLS-1$

            List<TCKeyword> values = new ArrayList<TCKeyword>();

            NodeList nodes = doc.getElementsByTagName("simple-list"); //$NON-NLS-1$
            if (nodes != null && nodes.getLength() > 0) {
                NodeList valueNodes = nodes.item(0).getChildNodes();

                for (int i = 0; i < valueNodes.getLength(); i++) {
                    Node node = valueNodes.item(i);

                    if ("code".equals(node.getNodeName())) //$NON-NLS-1$
                    {
                        NamedNodeMap attrs = valueNodes.item(i).getAttributes();

                        String value = attrs.getNamedItem("value")
                                .getTextContent();
                        String meaning = attrs.getNamedItem("meaning")
                                .getTextContent();

                        values.add(new TCKeyword(new DicomCode(id, value,
                                meaning)));
                    }
                }
            }

            return new TCKeywordCatalogueXMLList(systemId, designatorId,
                    designatorName, values);
        }

        public List<TCKeyword> getKeywords() {
            return Collections.unmodifiableList(keywords);
        }

        @Override
        public TCKeywordInput createInput(final String id,
                TCKeyword selectedKeyword) {
            return new TCKeywordListInput(id, selectedKeyword, getKeywords());
        }

        @Override
        public TCKeyword findKeyword(String value) {
            if (value != null) {
                for (TCKeyword keyword : keywords) {
                    DicomCode code = keyword.getCode();
                    if (value.equals(code.getValue())) {
                        return keyword;
                    }
                }
            }

            return null;
        }
    }

    private static class TCKeywordCatalogueXMLTree extends TCKeywordCatalogue {
        private TCKeywordNode root;

        private TCKeywordCatalogueXMLTree(String id, String designatorId,
                String designatorName, TCKeywordNode root) {
            super(id, designatorId, designatorName);
            this.root = root;
        }

        public static TCKeywordCatalogueXMLTree createInstance(Document doc) {
            Node rootNode = doc.getElementsByTagName("coding-system").item(0); //$NON-NLS-1$
            NamedNodeMap rootAttrs = rootNode.getAttributes();

            String systemId = rootAttrs.getNamedItem("id").getTextContent(); //$NON-NLS-1$
            String designatorId = rootAttrs
                    .getNamedItem("designator-id").getTextContent(); //$NON-NLS-1$
            String designatorName = rootAttrs
                    .getNamedItem("designator-name").getTextContent(); //$NON-NLS-1$
            String id = designatorId + ID_DELIMITER + systemId; //$NON-NLS-1$

            TCKeywordNode root = new TCKeywordNode();

            root.addChildren(new TCKeywordNode(
                    TCKeyword
                            .createAllKeywordsPlaceholder(new PackageStringResourceLoader()
                                    .loadStringResource(TCPanel.class,
                                            "tc.search.null.text", null, null))));

            NodeList nodes = doc.getElementsByTagName("simple-tree"); //$NON-NLS-1$
            if (nodes != null && nodes.getLength() > 0) {
                NodeList codeNodes = nodes.item(0).getChildNodes();

                for (int i = 0; i < codeNodes.getLength(); i++) {
                    TCKeywordNode node = createTree(id, codeNodes.item(i));
                    if (node != null) {
                        root.addChildren(node);
                    }
                }
            }

            return new TCKeywordCatalogueXMLTree(systemId, designatorId,
                    designatorName, root);
        }

        public TCKeywordNode getRoot() {
            return root;
        }

        @Override
        public TCKeywordInput createInput(final String id,
                TCKeyword selectedKeyword) {
            return new TCKeywordTreeInput(id, selectedKeyword, getRoot());
        }

        @Override
        public TCKeyword findKeyword(String value) {
            return findKeyword(value, root);
        }

        private TCKeyword findKeyword(String value, TCKeywordNode node) {
            if (value != null && node != null) {
                DicomCode code = node.getKeyword().getCode();
                if (value.equals(code.getValue())) {
                    return node.getKeyword();
                }

                if (node.getChildCount() > 0) {
                    for (TCKeywordNode child : node.getChildren()) {
                        TCKeyword keyword = findKeyword(value, child);
                        if (keyword != null) {
                            return keyword;
                        }
                    }
                }
            }

            return null;
        }

        private static TCKeywordNode createTree(String id, Node node) {
            TCKeywordNode parent = createNode(id, node);

            if (parent != null) {
                NodeList nodes = node.getChildNodes();
                if (nodes != null) {
                    for (int i = 0; i < nodes.getLength(); i++) {
                        TCKeywordNode child = createTree(id, nodes.item(i));
                        if (child != null) {
                            parent.addChildren(child);
                        }
                    }
                }

                return parent;
            }

            return null;
        }

        private static TCKeywordNode createNode(String id, Node node) {
            if ("code".equals(node.getNodeName())) //$NON-NLS-1$
            {
                NamedNodeMap attrs = node.getAttributes();

                String value = attrs.getNamedItem("value").getTextContent();
                String meaning = attrs.getNamedItem("meaning").getTextContent();

                return new TCKeywordNode(new TCKeyword(new DicomCode(id, value,
                        meaning)));
            }

            return null;
        }
    }

}
