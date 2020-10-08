package de.li2b2.serverdemo;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.xml.bind.JAXB;

import de.sekmi.li2b2.services.PMService;
import de.sekmi.li2b2.services.impl.pm.ProjectImpl;
import de.sekmi.li2b2.services.impl.pm.ProjectManagerImpl;
import de.sekmi.li2b2.services.impl.pm.UserImpl;

public class SimpleProjectManager extends ProjectManagerImpl {

	public static SimpleProjectManager initialize(Path previousStateFile) {
		SimpleProjectManager pm;
		if( Files.exists(previousStateFile) ) {
			pm = SimpleProjectManager.loadFromFile(previousStateFile);
		}else {
			pm = SimpleProjectManager.initializeWithDefaultUserAndProject();
		}
		pm.setFlushDestination(previousStateFile);
		return pm;
	}

	public static SimpleProjectManager loadFromFile(Path path) {
		try( InputStream in = Files.newInputStream(path)) {
			return JAXB.unmarshal(in,SimpleProjectManager.class);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}		
	}

	public static SimpleProjectManager initializeWithDefaultUserAndProject() {
		SimpleProjectManager pm = new SimpleProjectManager();
		pm.setProperty(PMService.SERVER_DOMAIN_ID, "i2b2");
		pm.setProperty(PMService.SERVER_DOMAIN_NAME, "i2b2demo");
		pm.setProperty(PMService.SERVER_ENVIRONMENT, "DEVELOPMENT");

		UserImpl user = pm.addUser("demo");
		user.setPassword("demouser".toCharArray());
		user.setProperty(PMService.USER_FULLNAME, "Demo user");
		ProjectImpl project = pm.addProject("Demo", "li2b2 Demo");
		project.getProjectUser(user).addRoles("USER","EDITOR","DATA_OBFSC");

		// you can use the following lines to display information or announcements during login
//		project.getParameters().add(new ParamImpl("Software","<span style='color:orange;font-weight:bold'>li2b2 server</span>"));
//		project.getProjectUser(user).addParameter("announcement","T","This is a demo of the <span style='color:orange;font-weight:bold'>li2b2 server</span>.");

		// add admin user
		// admin user should not be able to login into the projects
		UserImpl admin = pm.addUser("i2b2");
		admin.setPassword("demouser".toCharArray());
		admin.setAdmin(true);
		return pm;
	}
}
