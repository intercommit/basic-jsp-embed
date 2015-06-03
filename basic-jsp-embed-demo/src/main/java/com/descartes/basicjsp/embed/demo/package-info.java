/**
 * Basic JSP demo web application.
 * <p>
 * This demo project re-uses as much as possible from the Basic JSP embed project and extends the required classes.
 * The extended class {@link com.descartes.basicjsp.embed.demo.Launch} makes the re-use possible.
 * <p>
 * The demo can show the directory-tree of the project's home directory.
 * This directory-tree can be browsed for files which can be be opened or downloaded.
 * <p>
 * As such, the demo is not trivial: some interesting JSP syntax is used, a form is used and 
 * URL parameters are read and encoded/decoded using Base64.
 * Also, three Apache Commons packages are used (lang3, io and codec) for common functions often encountered 
 * during web application development.
 * <p>
 * To explore this demo, first get it running. Built it using:
 * <br><tt>mvn clean package assembly:single</tt>
 * <br>Go to the assembly directory <tt>target/basic-jsp-embed-demo-[version]</tt> and execute the run-script.
 * Open the main page at http://localhost:8080/
 * and click around in the "directory tree".
 * <p>
 * Credits for the directory tree go to: http://odyniec.net/articles/turning-lists-into-trees/ 
 * 
 */
package com.descartes.basicjsp.embed.demo;
