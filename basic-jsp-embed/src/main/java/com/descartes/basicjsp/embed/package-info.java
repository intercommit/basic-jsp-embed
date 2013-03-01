/**
 * Basic JSP web application running in embedded Tomcat.
 * <p>
 * The jar resulting from the build of this project can be re-used to build a simple stand-alone web-application with JSP pages.
 * See the <tt>basic-jsp-embed-demo</tt> project for an example.
 * <p>
 * The assembly from this project can also be used to start a web-application and shows some of the features of this basic JSP web application.
 * <p>
 * The web application can be tested during development: it works similar to having Tomcat running from your IDE.
 * In this case, start the web application from the Maven test-classes directory (<tt>project_home/target/test-classes</tt>) with the <tt>runtest</tt> script.
 * Any changes in code are detected by Tomcat after which the web context is restarted. 
 * Changes to resources and jsp-files (except <tt>web.xml</tt>) are picked up immediatly.
 * <p>
 * There is no need to create a WAR file for the web-application.
 * Since this implementation runs standalone, all required libraries are loaded at startup 
 * (there is no separate <tt>WEB-INF/lib</tt> folder needed).
 * Tomcat has a function to use "resource JARs" which is used by this project.
 * Resource JARs contain all resources in the <tt>META-INF/resources</tt> directory.
 * In Maven land this is <tt>src/main/resources/META-INF/resources</tt>.
 * This resources folder should look like:
 * <pre>
META-INF/resources
  - favicon.ico
  - static
     - images/
     - css/
     - etc.
  - WEB-INF
    - web.xml
    - pages/
      - home.jsp
      - etc.
    - any other content not directly available
 * </pre> 
 * <p>
 * To build your own web application project:
 * <br> - copy the dependencies of this project's pom.xml to the new project.
 * <br> - add a dependency to this project (<tt>com.descartes:basic-jsp-embed</tt>)
 * <br> - copy the assembler.xml 
 * <br> - copy the contents from /src/test/resources, /conf and /scripts 
 * <br> - update the run(test)-scripts to use your project's name/main-class.
 * <br> - overload the classes {@link com.descartes.basicjsp.embed.LaunchWebApp},
 * {@link com.descartes.basicjsp.embed.MainServlet} and {@link com.descartes.basicjsp.embed.WebSetup}.
 * <br> - update the web.xml with the overloaded classes
 * 
 */
package com.descartes.basicjsp.embed;
