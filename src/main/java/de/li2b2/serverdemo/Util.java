package de.li2b2.serverdemo;

import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Util {

	public static Element wrapRequestQueryDefinition(Element queryDefinition, String[] result_types) {
		Document d = queryDefinition.getOwnerDocument();
		try {
				
			Element el = d.createElementNS("http://www.i2b2.org/xsd/cell/crc/psm/1.1/", "request");
			el.setPrefix("ns4");
			el.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "xsi:type", "ns4:query_definition_requestType");
			el.appendChild(queryDefinition.cloneNode(true));
			Element rol = d.createElement("result_output_list");
			el.appendChild(rol);
	
			for( int i=0; i<result_types.length; i++ ) {
				Element ro = d.createElement("result_output");
				ro.setAttribute("priority_index", Integer.toString(i));
				ro.setTextContent(result_types[i]);
				rol.appendChild(ro);
			}
			return el;
		}catch( Throwable t ) {
			t.printStackTrace();
			return null;
		}
		
	}

	public static String elementToXmlString(Element element, boolean prettyPrint) throws TransformerException {
		DOMSource domSource = new DOMSource(element);
		StringWriter writer = new StringWriter();
		StreamResult result = new StreamResult(writer);
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = tf.newTransformer();

		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		if( prettyPrint ) {
			// pretty print
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		}
		transformer.transform(domSource, result);
		return writer.toString();		
	}
	
}
