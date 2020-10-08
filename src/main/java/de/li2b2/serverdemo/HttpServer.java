package de.li2b2.serverdemo;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

import de.sekmi.li2b2.services.OntologyService;
import de.sekmi.li2b2.services.PMService;
import de.sekmi.li2b2.services.QueryToolService;
import de.sekmi.li2b2.services.Webadmin;
import de.sekmi.li2b2.services.Webclient;
import de.sekmi.li2b2.services.WorkplaceService;
import de.sekmi.li2b2.util.CORSFilter;

/**
 * li2b2 server for unit tests
 * or demonstrations.
 *
 * Initial project configuration and users are provided
 * via {@link MyBinder}.
 *
 * @author R.W.Majeed
 *
 */
public class HttpServer {

	private ResourceConfig rc;
	private Server jetty;

	public HttpServer(Path dataRoot){
		rc = new ResourceConfig();
		rc.register(PMService.class);
		rc.register(QueryToolService.class);
		rc.register(WorkplaceService.class);
		rc.register(OntologyService.class);
		rc.register(new MyBinder(dataRoot));
		
		// add CORS filter
		Map<String, String> props = new HashMap<String, String>();
		props.put("jersey.config.server.provider.classnames", CORSFilter.class.getName());
		rc.setProperties(props);

	}
	public void register(Class<?> componentClass){
		rc.register(componentClass);
	}
	
	protected void start_local(int port) throws Exception{
		start(new InetSocketAddress(InetAddress.getLoopbackAddress(), port));
	}
	public URI getPMServiceURI(){
		return jetty.getURI().resolve(PMService.SERVICE_URL);
	}
	public void start(InetSocketAddress addr) throws Exception{
		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath("/");

		jetty = new Server(addr);
		jetty.setHandler(context);

		ServletHolder jersey = new ServletHolder(new ServletContainer(rc));
//		jersey.setInitOrder(0);
		context.addServlet(jersey, "/*");

		jetty.start();
	}
	public void join() throws InterruptedException{
		jetty.join();
	}
	public void destroy() throws Exception{
		if( jetty.isStopped() ) {
			jetty.destroy();
		}
	}
	public void stop() throws Exception{
		jetty.stop();
	}


	public static boolean checkWebclient(){
		if( null != HttpServer.class.getResource(Webclient.WEBCLIENT_SOURCES_RESOURCE_PATH+"default.htm") ){
			// webclient source found, everything all right
			return true;
		}
		System.err.println();
		System.err.println("WARNING: webclient html sources not detected!!!");
		System.err.println();
		System.err.println("You can still use the server, but you need to");
		System.err.println("run and configure your client/webclient yourself.");
		System.err.println("For instructions to include the webclient, see README.md");
		System.err.println();
		return false;
	}
	/**
	 * Run the test server with with the official i2b2
	 * webclient.
	 * @param args command line arguments: port can be specified optionally
	 * @throws Exception any error
	 */
	public static void main(String[] args) throws Exception{
		// use port if specified
		int port;
		if( args.length == 0 ){
			port = 8085;
		}else if( args.length == 1 ){
			port = Integer.parseInt(args[0]);
		}else{
			System.err.println("Too many command line arguments!");
			System.err.println("Usage: "+HttpServer.class.getCanonicalName()+" [port]");
			System.exit(-1);
			return;
		}

		Path dataDir = Paths.get(System.getProperty("user.dir"));
		// start server
		HttpServer server = new HttpServer(dataDir);
		server.register(Webclient.class);
		server.register(Webadmin.class);
		try{
			server.start(new InetSocketAddress(port));
			if( checkWebclient() ){
				System.err.println("Webclient at: "+server.jetty.getURI().resolve("/webclient/default.htm"));
				System.err.println("Webadmin at: "+server.jetty.getURI().resolve("/admin/default.htm"));
			}
			System.err.println("PM service at: "+server.getPMServiceURI());
			server.join();
		}finally{
			server.destroy();
		}
	}
}
