package de.li2b2.serverdemo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.sekmi.li2b2.api.crc.QueryStatus;
import de.sekmi.li2b2.services.impl.crc.ExecutionImpl;
import de.sekmi.li2b2.services.impl.crc.FileBasedQueryManager;
import de.sekmi.li2b2.services.impl.crc.QueryImpl;

/**
 * Implementation of query manager which forwards
 * the query to the FLARE executor RESTful backend
 *
 * 
 * @author R.W.Majeed
 *
 */
public class FlareHttpQueryManager extends FileBasedQueryManager{
	private static final Logger log = Logger.getLogger(FlareHttpQueryManager.class.getName());
	@XmlElement
	private URI flareBaseURI;

	public FlareHttpQueryManager() {
		super();
		addResultType("PATIENT_COUNT_XML", "CATNUM", "Number of patients");//"Patient count (simple)");
//		addResultType("MULT_SITE_COUNT", "CATNUM", "Number of patients per site");//"Patient count (simple)");
//		addResultType("PATIENT_GENDER_COUNT_XML", "CATNUM", "Gender patient breakdown");
//		addResultType("PATIENT_VITALSTATUS_COUNT_XML", "CATNUM", "Vital Status patient breakdown");
//		addResultType("PATIENT_RACE_COUNT_XML", "CATNUM", "Race patient breakdown");
//		addResultType("PATIENT_AGE_COUNT_XML", "CATNUM", "Age patient breakdown");
		// TODO more result types for i2b2
	}
	@Override
	protected void executeQuery(QueryImpl query){
		ExecutionImpl exec = query.addExecution(QueryStatus.INCOMPLETE);
		
		// print query
		System.out.println("Query "+query.getId());
		try {
			System.out.println(Util.elementToXmlString(query.getDefinition(), true));
			System.out.println("Wrapped");
			
		} catch (TransformerException e1) {
			e1.printStackTrace();
		}
		
		// perform execution, customized execution can be done here 
		exec.setStartTimestamp(Instant.now());

		HttpURLConnection c;
		Document response = null;
		try {
			String requestBody = Util.elementToXmlString(Util.wrapRequestQueryDefinition(query.getDefinition(), query.getRequestTypes()), true);
			c = (HttpURLConnection)flareBaseURI.toURL().openConnection();
			c.setDoOutput(true);
			c.setDoInput(true);
			c.setRequestMethod("POST");
			c.setRequestProperty("Content-type", "application/xml; charset=utf-8");
			try( OutputStreamWriter w = new OutputStreamWriter(c.getOutputStream(), StandardCharsets.UTF_8) ){
				w.write(requestBody);
			}
			if( c.getResponseCode() == 200 ) {
				try( InputStream in = c.getInputStream() ){
					DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
					response = builder.parse(in);
				}
			}else {
				log.warning("Upstream FHIR server returned status code "+c.getResponseCode());
			}
			c.disconnect();
		} catch (MalformedURLException e) {
			throw new IllegalStateException("Illegal FLARE base URI: "+flareBaseURI.toString());
		} catch (IOException e) {
			log.log(Level.WARNING, "IO error during request processing", e);
			exec.setStatus(QueryStatus.ERROR);
		} catch (TransformerException | ParserConfigurationException | SAXException e) {
			log.log(Level.WARNING, "XML error during request or response processing", e);
			exec.setStatus(QueryStatus.ERROR);
		}
		if( response != null ) {
			String count = getElementValueAttribute(response, "patient_count");
			double start = Double.parseDouble(getElementValueAttribute(response, "start_time"));
			double end = Double.parseDouble(getElementValueAttribute(response, "end_time"));
			
			exec.setStartTimestamp(Instant.ofEpochMilli((long)(start*1000)));
			exec.setEndTimestamp(Instant.ofEpochMilli((long)(end*1000)));
			exec.getResult("PATIENT_COUNT_XML").fillWithPatientCount(Integer.parseInt(count));
			
			exec.setStatus(QueryStatus.FINISHED);
		}else {
			exec.setStatus(QueryStatus.ERROR);
		}
	}
	private static String getElementValueAttribute(Document dom, String elementName) {
		NodeList nl = dom.getElementsByTagName(elementName);
		if( nl.getLength() == 0 ) {
			return null;
		}
		return ((Element)nl.item(0)).getAttribute("value");
	}

	public static FlareHttpQueryManager initialize(Path previousStateFile, Path queryDir) {
		FlareHttpQueryManager crc;
		if( Files.exists(previousStateFile) ) {
			try( InputStream in = Files.newInputStream(previousStateFile)) {
				crc = JAXB.unmarshal(in,FlareHttpQueryManager.class);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}else {
			crc = new FlareHttpQueryManager();
			crc.flareBaseURI = URI.create("http://localhost:5000/i2b2");
		}
		try {
			crc.setFlushDestination(previousStateFile, queryDir);
			crc.loadAllQueries();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		return crc;
	}

}
