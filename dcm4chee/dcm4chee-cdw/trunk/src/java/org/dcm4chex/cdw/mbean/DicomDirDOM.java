/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.cdw.mbean;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmObject;
import org.dcm4che.data.DcmValueException;
import org.dcm4che.dict.Tags;
import org.dcm4che.media.DirBuilderFactory;
import org.dcm4che.media.DirReader;
import org.dcm4che.media.DirRecord;
import org.dcm4cheri.util.StringUtils;
import org.dcm4chex.cdw.common.ConfigurationException;
import org.dcm4chex.cdw.common.ExecutionStatusInfo;
import org.dcm4chex.cdw.common.MediaCreationException;
import org.dcm4chex.cdw.common.MediaCreationRequest;
import org.jboss.logging.Logger;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author gunter.zeilinter@tiani.com
 * @version $Revision$ $Date$
 * @since 12.07.2004
 *
 */
class DicomDirDOM {

    private static final DirBuilderFactory dbf = DirBuilderFactory
            .getInstance();

    private static final TransformerFactory tf = TransformerFactory
            .newInstance();

    private static final DOMImplementation dom;

    private static final String INDEX_XSL_URI = "resource:xsl/index.xsl";

    private static final String WEB_XSL_URI = "resource:xsl/web.xsl";

    private static final Templates indexTpl;

    private static final Templates webTpl;

    private final Document doc;

    private final MediaComposerService service;

    private final Logger log;

    private final MediaCreationRequest rq;

    static {
        try {
            dom = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .getDOMImplementation();
            indexTpl = tf.newTemplates(new StreamSource(INDEX_XSL_URI));
            webTpl = tf.newTemplates(new StreamSource(WEB_XSL_URI));
        } catch (TransformerConfigurationException e) {
            throw new ConfigurationException(e);
        } catch (ParserConfigurationException e) {
            throw new ConfigurationException(e);
        } catch (FactoryConfigurationError e) {
            throw new ConfigurationException(e);
        }
    }

    public DicomDirDOM(MediaComposerService service, MediaCreationRequest rq,
            Dataset attrs) throws MediaCreationException {
        this.service = service;
        this.log = service.getLog();
        this.rq = rq;
        this.doc = dom.createDocument(null, "dicomdir", null);
        DirReader reader = null;
        try {
            reader = dbf.newDirReader(new File(rq.getFilesetDir(), "DICOMDIR"));
            Element root = doc.getDocumentElement();
            Dataset fsinfo = reader.getFileSetInfo();
            appendAttrs(root, fsinfo.getFileMetaInfo());
            appendAttrs(root, fsinfo);
            appendRecords(root, reader.getFirstRecord());
            appendAttrs(root, attrs.subSet(new int[]{ Tags.RefSOPSeq }, true));
        } catch (IOException e) {
            throw new MediaCreationException(ExecutionStatusInfo.PROC_FAILURE,
                    e);
        } finally {
            if (reader != null) try {
                reader.close();
            } catch (IOException ignore) {
            }
        }
    }

    private void appendRecords(Element node, DirRecord first)
            throws IOException {
        for (DirRecord rec = first; rec != null; rec = rec.getNextSibling())
            appendRecords(appendRecord(node, rec), rec.getFirstChild());
    }

    private Element appendRecord(Element parent, DirRecord rec)
            throws DcmValueException {
        Element elm = doc.createElement("record");
        elm.setAttribute("type", rec.getType());
        appendAttrs(elm, rec.getDataset());
        parent.appendChild(elm);
        return elm;
    }

    private void appendAttrs(Element parent, DcmObject ds)
            throws DcmValueException {
        Charset cs = ds.getCharset();
        for (Iterator it = ds.iterator(); it.hasNext();)
            appendAttr(parent, (DcmElement) it.next(), cs);
    }

    private void appendAttr(Element parent, DcmElement dcmElm, Charset cs)
            throws DcmValueException {
        if (dcmElm.isEmpty() || dcmElm.tag() == Tags.IconImageSeq) return;
        Element elm = doc.createElement("attr");
        elm.setAttribute("tag", Tags.toString(dcmElm.tag()));
        if (dcmElm.hasItems())
            for (int i = 0, n = dcmElm.vm(); i < n; ++i) {
                Element item = doc.createElement("item");
                appendAttrs(item, dcmElm.getItem(i));
                elm.appendChild(item);
            }
        else {
            String text = StringUtils.toString(dcmElm.getStrings(cs), '/');
            elm.appendChild(doc.createTextNode(text));
        }
        parent.appendChild(elm);
    }

    private void xslt(Templates tpl, File f) throws MediaCreationException {
        try {
            tpl.newTransformer().transform(new DOMSource(doc),
                    new StreamResult(f));
        } catch (TransformerConfigurationException e) {
            throw new MediaCreationException(ExecutionStatusInfo.PROC_FAILURE,
                    e);
        } catch (TransformerException e) {
            throw new MediaCreationException(ExecutionStatusInfo.PROC_FAILURE,
                    e);
        }
    }

    public void toXML(File out) throws MediaCreationException {
        try {
            Transformer tr = tf.newTransformer();
            tr.setOutputProperty(OutputKeys.INDENT, "yes");
            tr.transform(new DOMSource(doc), new StreamResult(out));
        } catch (TransformerConfigurationException e) {
            throw new MediaCreationException(ExecutionStatusInfo.PROC_FAILURE,
                    e);
        } catch (TransformerException e) {
            throw new MediaCreationException(ExecutionStatusInfo.PROC_FAILURE,
                    e);
        }
    }

    public void createIndex() throws MediaCreationException {
        xslt(indexTpl, new File(rq.getFilesetDir(), "INDEX.HTM"));
    }

    public void createWeb() throws MediaCreationException {
        xslt(webTpl, new File(rq.getFilesetDir(), "IHE_PDI"
                + File.separatorChar + "INDEX.HTM"));
    }
}
