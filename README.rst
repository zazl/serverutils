==========================
Dojo Zazl Server Utilities
==========================

The Zazl Server Utilities contains server side modules that provide support for other Zazl components such as the Optimizer and Zazl Server-Side Templating. 

These utilities include:

1) Resource Loader (http://www.zazl.org/?page_id=168)
2) Rhino ClassLoader (http://www.zazl.org/?page_id=152)
3) V8 Java Bridge (http://www.zazl.org/?page_id=150)
4) A commonjs compliant loader that runs in Rhino and V8 via java.

See http://www.zazl.org for more details

Setting up a Development Environment
====================================

NOTE: This step is also required for setting up a development environment for the optimizer and zazl components too.

1) Make a directory called "zazldev" and cd into it.
2) In the "zazldev" directory create a new directory called "workspace"
2) Download the Zazl Eclipse Target Platform from http://www.zazl.org/downloads/0.3.0/zazltargetplatform.zip and unzip into the "zazldev" directory. 
3) Clone the Server Utils git repo (git clone https://github.com/zazl/serverutils.git) from within the "zazldev/workspace" directory.

To develop and build with Eclipse:
1) Download a copy of Eclipse 3.7 (http://www.eclipse.org/downloads/)
2) Start Eclipse and point it to the "zazldev/workspace" directory. 
3) Import the projects found in the "zazldev/workspace/serverutils" directory via "File->Import->General->Existing Projects into Workspace".
4) Initially you will have some compile errors
5) Open the "/org.dojotoolkit.server.util.feature/zazltarget.target" file and click the "Set as Target Platform" link in the top right-hand corner. This should make the compile errors go away.

Zazl Eclipse Target Platform
============================

The Zazl Target Platform zip file (http://www.zazl.org/downloads/0.4.6/zazltargetplatform.zip) contains the prereqs that Zazl requires to run and build.
