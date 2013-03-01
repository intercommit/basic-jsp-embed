Basic JSP embedded
==================

An example of a stand-alone web application that uses Tomcat Embedded and JSP.
Various tips and tricks as well as best practices are included in the example project.

To get the example project running you will need Maven and:
- copy all 5 directories to your local drive (tip: use the ZIP download button shown above)
- open a command prompt in the "basic-jsp-embed-parent" directory
- run "mvn install"
- go to the "basic-jsp-embed-demo" directory
- go to the "target/test-classes" sub-directory
- make sure port 8080 is not used and run "runtest.bat"

The following components are used to run the demo:

### AppBoot ###

An application boot loader. It looks for jar-files in a lib-directory and adds them to a class-loader.
The conf-directory (if any) is also added to the class-loader. The main class of the application is 
started using the new class-loader. 
AppBoot also supports starting an application from a Maven test-classes directory.
This makes it possible to test code-changes without the need to re-build a complete project.

The demo uses AppBoot to start the main class and not worry about required jar-files to run.
To learn more about AppBoot, assemble the appboot-test project and run it. 
The appboot-test project only contains 1 main class that demonstrates how AppBoot can be used
to start your own application.

### Basic JSP Embed ###

The demo extends classes from this "core" package and also re-uses (web) resources in this package.
The project itself can also be assembled and started as a stand-alone web-application,
but the demo project shows how you can re-use classes and resources from this package.
Use this project to create your own "core" web application package with classes and resources you know 
you will need in other projects. The source code contains comments on where to find (copied) tips and tricks.
