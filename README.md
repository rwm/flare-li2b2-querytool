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

The simulated i2b2 server is fully functional when used from the i2b2 web client. 
E.g. users can be added, passwords changed, etc. The state is saved in XML files 
in the working directory (from which the application is run).

E.g. you can supply custom metadata via a file `ontology.xml`.
Changes in project and user configuration is stored in `pm.xml`.
Query data is stored in `qm.xml` and the subfolder `qm`.