package de.li2b2.serverdemo;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import de.sekmi.li2b2.api.crc.QueryManager;
import de.sekmi.li2b2.api.ont.Ontology;
import de.sekmi.li2b2.api.pm.ProjectManager;
import de.sekmi.li2b2.services.impl.OntologyImpl;
import de.sekmi.li2b2.services.impl.crc.FileBasedQueryManager;
import de.sekmi.li2b2.services.impl.pm.ProjectManagerImpl;
import de.sekmi.li2b2.services.token.TokenManager;

public class MyBinder extends AbstractBinder{
	private Path dataRoot;

	public MyBinder(Path dataRoot) {
		this.dataRoot = dataRoot;
	}

	@Override
	protected void configure() {
		// project manager
		ProjectManagerImpl pm = SimpleProjectManager.initialize(dataRoot.resolve("pm.xml"));
		bind(pm).to(ProjectManager.class);
				
		// query manager
		FileBasedQueryManager crc = RandomResultQueryManager.initialize(dataRoot.resolve("qm.xml"), dataRoot.resolve("qm"));		
		bind(crc).to(QueryManager.class);

		// ontology
		Ontology ont;
		try {
			if( Files.exists(dataRoot.resolve("ontology.xml")) ){
				ont = OntologyImpl.parse(dataRoot.resolve("ontology.xml").toUri().toURL());
			}else if( System.getProperty("ontology.url") != null ) {
				ont = OntologyImpl.parse(new URL(System.getProperty("ontology.url")));
			}else {
				// load example ontology
				ont = OntologyImpl.parse(MyBinder.class.getResource("/ontology.xml"));
			}
		} catch (MalformedURLException e) {
			throw new RuntimeException("Illegal URL for ontology", e);
		}
		bind(ont).to(Ontology.class);

		// bind token manager
		bind(new TokenManagerImpl()).to(TokenManager.class);
	}

}
