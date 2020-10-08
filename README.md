li2b2 custom server demo
========================

This project is an example to show how to create a custom server application 
using li2b2-facade.

When built with maven, a fat-jar is produced, which can be run from the command line:
```
java -jar li2b2-custom-server-demo-jar-with-dependencies.jar 8080
```
This command will start a simulated i2b2 server listening on the local network 
on port 8080. Without command line arguments, the port 8085 is used.

To access the server, you need the i2b2 webclient. This can be downloades separately 
from i2b2.org or https://github.com/i2b2/i2b2-webclient/releases . If you wish to include 
the webclient in the fat jar, see notes below.

The simulated i2b2 server is fully functional when used from the i2b2 web client. 
E.g. users can be added, passwords changed, etc. The state is saved in XML files 
in the working directory (from which the application is run).

E.g. you can supply custom metadata via a file `ontology.xml`.
Changes in project and user configuration is stored in `pm.xml`.
Query data is stored in `qm.xml` and the subfolder `qm`.


Building from source
--------------------

You can build the project from source code by using Apache maven.

After building with `mvn clean install`, the target/ directory will contain a fat-jar 
which includes all runtime dependencies.

If you wish to include the webclient in the produced fat jar, you can run the build 
with `mvn -P webclient clean install`. For this command to work, you need to download 
the webclient source code first and register it with maven. 
See https://github.com/li2b2/li2b2-facade/blob/master/server/README.md  


