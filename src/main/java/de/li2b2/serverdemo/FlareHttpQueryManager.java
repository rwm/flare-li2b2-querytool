package de.li2b2.serverdemo;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Random;

import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import de.sekmi.li2b2.api.crc.QueryResult;
import de.sekmi.li2b2.api.crc.QueryStatus;
import de.sekmi.li2b2.services.impl.crc.ExecutionImpl;
import de.sekmi.li2b2.services.impl.crc.FileBasedQueryManager;
import de.sekmi.li2b2.services.impl.crc.QueryImpl;
import de.sekmi.li2b2.services.impl.crc.ResultImpl;

/**
 * Implementation of query manager which forwards
 * the query to the FLARE executor RESTful backend
 *
 * 
 * @author R.W.Majeed
 *
 */
public class FlareHttpQueryManager extends FileBasedQueryManager{
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
		ExecutionImpl e = query.addExecution(QueryStatus.INCOMPLETE);
		// TODO perform execution
		e.setStartTimestamp(Instant.now());

		HttpURLConnection connection;
		try {
			connection = (HttpURLConnection)flareBaseURI.toURL().openConnection();
			connection.disconnect();
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		for( QueryResult result : e.getResults() ) {
			ResultImpl ri = (ResultImpl)result;
			switch( result.getResultType() ) {
			case "PATIENT_COUNT_XML":
				ri.fillWithPatientCount(2342);
			}
		}

		e.setEndTimestamp(Instant.now());
		
		e.setStatus(QueryStatus.FINISHED);
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
