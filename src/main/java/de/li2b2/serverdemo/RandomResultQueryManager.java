package de.li2b2.serverdemo;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Random;

import javax.xml.bind.JAXB;
import javax.xml.transform.TransformerException;

import de.sekmi.li2b2.api.crc.QueryResult;
import de.sekmi.li2b2.api.crc.QueryStatus;
import de.sekmi.li2b2.services.impl.crc.ExecutionImpl;
import de.sekmi.li2b2.services.impl.crc.FileBasedQueryManager;
import de.sekmi.li2b2.services.impl.crc.QueryImpl;
import de.sekmi.li2b2.services.impl.crc.ResultImpl;

/**
 * Implementation of query manager which generates
 * random patient counts for demonstration purposes.
 *
 * You can use this template to build useful implementation.
 * 
 * @author R.W.Majeed
 *
 */
public class RandomResultQueryManager extends FileBasedQueryManager{
	private Random rand;

	public RandomResultQueryManager() {
		super();
		this.rand = new Random();
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
		
		// print query
		System.out.println("Query "+query.getId());
		try {
			System.out.println(Util.elementToXmlString(query.getDefinition(), true));
			System.out.println("Wrapped");
			System.out.println(Util.elementToXmlString(Util.wrapRequestQueryDefinition(query.getDefinition(), query.getRequestTypes()), true));
		} catch (TransformerException e1) {
			e1.printStackTrace();
		}

		
		// perform execution, customized execution can be done here 
		e.setStartTimestamp(Instant.now());

		for( QueryResult result : e.getResults() ) {
			ResultImpl ri = (ResultImpl)result;
			switch( result.getResultType() ) {
			case "PATIENT_COUNT_XML":
				ri.fillWithPatientCount(rand.nextInt(Integer.MAX_VALUE));
			}
		}

		e.setEndTimestamp(Instant.now());
		
		e.setStatus(QueryStatus.FINISHED);
	}

	public static RandomResultQueryManager initialize(Path previousStateFile, Path queryDir) {
		RandomResultQueryManager crc;
		if( Files.exists(previousStateFile) ) {
			try( InputStream in = Files.newInputStream(previousStateFile)) {
				crc = JAXB.unmarshal(in,RandomResultQueryManager.class);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}else {
			crc = new RandomResultQueryManager();
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
