package org.dcm4chee.xds.infoset.v30.util;


import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.util.JAXBResult;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;

import org.dcm4chee.xds.infoset.v30.ExternalIdentifierType;
import org.dcm4chee.xds.infoset.v30.ExtrinsicObjectType;
import org.dcm4chee.xds.infoset.v30.ProvideAndRegisterDocumentSetRequestType;
import org.dcm4chee.xds.infoset.v30.RegistryObjectType;
import org.dcm4chee.xds.infoset.v30.RegistryPackageType;
import org.dcm4chee.xds.infoset.v30.SlotType1;
import org.dcm4chee.xds.infoset.v30.SubmitObjectsRequest;
import org.dcm4chee.xds.infoset.v30.ProvideAndRegisterDocumentSetRequestType.Document;
import org.w3c.dom.Node;

public class InfoSetUtil {

    private static JAXBContext jaxbContext;

    public static Map getSlotsFromRegistryObject(RegistryObjectType ro) throws JAXBException {
        List slots = ro.getSlot();
        Map slotByName = new HashMap(slots.size());
        if (slots != null) {
            for (Iterator iter = slots.iterator() ; iter.hasNext() ;) {
                SlotType1 slot = (SlotType1)iter.next();
                String slotName = slot.getName();
                slotByName.put(slotName, slot);
            }
        }
        return slotByName;
    }

    public static String getExternalIdentifierValue(String urn, RegistryObjectType ro) {
        List<ExternalIdentifierType> l = ro.getExternalIdentifier();
        ExternalIdentifierType ei;
        for ( Iterator iter = l.iterator() ; iter.hasNext() ; ) {
            ei = (ExternalIdentifierType) iter.next();
            if ( ei.getIdentificationScheme().equals(urn)) {
                return ei.getValue();
            }
        }
        return null;
    }
    public static String setExternalIdentifierValue(String urn, String value, RegistryObjectType ro) {
        List<ExternalIdentifierType> l = ro.getExternalIdentifier();
        ExternalIdentifierType ei;
        for ( Iterator iter = l.iterator() ; iter.hasNext() ; ) {
            ei = (ExternalIdentifierType) iter.next();
            if ( ei.getIdentificationScheme().equals(urn)) {
                String oldValue = ei.getValue();
                ei.setValue(value);
                return oldValue;
            }
        }
        ei = new ExternalIdentifierType();
        ei.setIdentificationScheme(urn);
        ei.setValue(value);
        l.add(ei);
        return null;
    }

    public static RegistryPackageType getRegistryPackage(SubmitObjectsRequest so) {
        List list = so.getRegistryObjectList().getIdentifiable();
        Object o;
        for ( Iterator iter = list.iterator(); iter.hasNext() ; ) {
            o = ((JAXBElement) iter.next()).getValue();
            if ( o instanceof RegistryPackageType) {
                return (RegistryPackageType) o;
            }
        }
        return null;
    }

    public static Map getExtrinsicObjects(SubmitObjectsRequest so) {
        Map map = new HashMap();
        List list = so.getRegistryObjectList().getIdentifiable();
        Object o;
        ExtrinsicObjectType extrObj;
        for ( Iterator iter = list.iterator(); iter.hasNext() ; ) {
            o = ((JAXBElement) iter.next()).getValue();
            if ( o instanceof ExtrinsicObjectType) {
                extrObj = (ExtrinsicObjectType) o;
                map.put(extrObj.getId(), extrObj);

            }
        }
        return map;
    }

    public static Map getDocuments(ProvideAndRegisterDocumentSetRequestType req) {
        List docs = req.getDocument();
        Map map = new HashMap(docs.size());
        Document doc;
        for ( Iterator iter = docs.iterator() ; iter.hasNext() ;) {
            doc = (Document) iter.next();
            map.put(doc.getId(), doc);
        }
        return map;
    }

    public static Source getSourceForObject(Object o) throws JAXBException {
        return new StreamSource(new ByteArrayInputStream(marshallObject(o, false).getBytes()));
    }

    public static String marshallObject(Object o, boolean indent) throws JAXBException {
        StringWriter sw = new StringWriter();
        Marshaller m = getJAXBContext().createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.valueOf(indent));
        m.marshal(o, sw);
        String s = sw.toString();
        return s;
    }
    
    public static Node getNodeForObject(Object o) throws JAXBException {
        Marshaller m = getJAXBContext().createMarshaller();
        DOMResult res = new DOMResult(); 
        m.marshal(o, res);
        return res.getNode();
    }

    public static void writeObject(Object o, OutputStream os, boolean indent) throws JAXBException {
        Marshaller m = getJAXBContext().createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.valueOf(indent));
        m.marshal(o, os);
    }

    public static JAXBContext getJAXBContext() throws JAXBException {
        if (jaxbContext == null) {
            jaxbContext = JAXBContext.newInstance("org.dcm4chee.xds.infoset.v30");
        }
        return jaxbContext;
    }

}
