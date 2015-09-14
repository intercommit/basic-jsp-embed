package com.descartes.appboot;

import static com.descartes.appboot.BootKeys.*;
import static com.descartes.appboot.BootUtil.*;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * Main class of AppBoot that configures the boot class loader using system properties and/or arguments.
 * See {@link BootKeys} for a description of the properties.
 * AppBoot assumes a default directory structure of <code>home-dir/lib</code> containing dependency jars 
 * and <code>home-dir/conf</code> containing resources for the application.
 * If <code>appboot.jar</code> is not in <code>home-dir/lib</code>, AppBoot assumes all dependencies are in the home-dir itself.
 * The <code>lib</code> value can be customized using the {@link BootKeys#APP_LIB_DIRNAME} property.
 * <p>
 * AppBoot also supports a maven test environment so that source code changes can be tested 
 * by running the application from the <code>target/test-classes</code> directory. See also the {@link BootKeys#APP_MAVEN_TEST} property.
 * <p>
 * Apache Commons Daemon can be used to start/stop an application via AppBoot, see the {@link #stop(String[])} method for more information.
 * <p>
 * The <code>appboot-test</code> project contains examples on how AppBoot can be used and also contains Maven POM and assembly
 * configurations to allow AppBoot to function in different environments.
 * <br>To build a jar with a main class:<pre>
&lt;plugin&gt;
  &lt;artifactId&gt;maven-jar-plugin&lt;/artifactId&gt;
  &lt;configuration&gt;
    &lt;archive&gt;
      &lt;manifest&gt;
        &lt;mainClass&gt;${manifest.main}&lt;/mainClass&gt;
      &lt;/manifest&gt;
    &lt;/archive&gt;
  &lt;/configuration&gt;
&lt;/plugin&gt;
 * </pre> To copy dependencies to the maven target/dependency directory: <pre>
&lt;plugin&gt;
  &lt;artifactId&gt;maven-dependency-plugin&lt;/artifactId&gt;
  &lt;version&gt;2.6&lt;/version&gt;
  &lt;executions&gt;
    &lt;execution&gt;
      &lt;id&gt;copy-dependencies&lt;/id&gt;
      &lt;phase&gt;process-resources&lt;/phase&gt;
      &lt;goals&gt;
        &lt;goal&gt;copy-dependencies&lt;/goal&gt;
      &lt;/goals&gt;
      &lt;configuration&gt;
        &lt;overWriteSnapshots&gt;true&lt;/overWriteSnapshots&gt;
        &lt;excludeArtifactIds&gt;appboot&lt;/excludeArtifactIds&gt;
      &lt;/configuration&gt;
    &lt;/execution&gt;
    &lt;execution&gt;
      &lt;id&gt;copy-dependency-appboot&lt;/id&gt;
      &lt;phase&gt;process-resources&lt;/phase&gt;
      &lt;goals&gt;
        &lt;goal&gt;copy-dependencies&lt;/goal&gt;
      &lt;/goals&gt;
      &lt;configuration&gt;
        &lt;overWriteSnapshots&gt;true&lt;/overWriteSnapshots&gt;
        &lt;stripVersion&gt;true&lt;/stripVersion&gt;
        &lt;includeArtifactIds&gt;appboot&lt;/includeArtifactIds&gt;
      &lt;/configuration&gt;
    &lt;/execution&gt;
  &lt;/executions&gt;
&lt;/plugin&gt;
 *</pre>To copy dependencies for an assembly build (configuration in assembler.xml): <pre>
 &lt;dependencySets&gt;
  &lt;dependencySet&gt;
    &lt;outputDirectory&gt;/lib&lt;/outputDirectory&gt;
    &lt;useProjectArtifact&gt;true&lt;/useProjectArtifact&gt;
    &lt;scope&gt;runtime&lt;/scope&gt;
    &lt;excludes&gt;
      &lt;exclude&gt;com.descartes:appboot&lt;/exclude&gt;
    &lt;/excludes&gt;
  &lt;/dependencySet&gt;
  &lt;dependencySet&gt;
    &lt;outputDirectory&gt;/lib&lt;/outputDirectory&gt;
    &lt;useProjectArtifact&gt;true&lt;/useProjectArtifact&gt;
    &lt;outputFileNameMapping&gt;${artifact.artifactId}.${artifact.extension}&lt;/outputFileNameMapping&gt;
    &lt;scope&gt;runtime&lt;/scope&gt;
    &lt;includes&gt;
      &lt;include&gt;com.descartes:appboot&lt;/include&gt;
    &lt;/includes&gt;
  &lt;/dependencySet&gt;
&lt;/dependencySets&gt;
 * </pre> 
 * @author fwiers
 *
 */
public class AppBoot {

	/**
	 * Found in main-jar or set to value of {@link BootKeys#APP_MAIN}.
	 */
	public static String mainClassName;
	/**
	 * Set to value of {@link BootKeys#APP_NAME}.
	 */
	public static String appName;
	/**
	 * The boot classloader created by AppBoot and used to start the main class of the applicaiton.
	 */
	public static URLClassLoader bootClassLoader;
	/**
	 * Set to value of {@link BootKeys#APP_BOOT_DEBUG}.
	 */
	public static boolean debug;
	/**
	 * Set to value of {@link BootKeys#APP_MAVEN_TEST}.
	 */
	public static boolean mavenTest;
	/**
	 * Set to value of {@link BootKeys#APP_MAVEN_TEST_SKIP_CLASSES}.
	 */
	public static boolean mavenTestSkipClasses;
	
	public static String getInfo() {
		
		StringBuilder sb = new StringBuilder("AppBoot version ");
		sb.append(getPomVersion(AppBoot.class));
		sb.append(CR);
		sb.append(CR).append("Creates a boot class loader and runs the main class of the application.");
		sb.append(CR).append("Resources (e.g. the appboot.jar) for the application must be located ");
		sb.append(CR).append("in the home-directory of the application and/or the home-dir/lib directory.");
		sb.append(CR).append("Parameters can be specified via a command-line argument or a Java system property (using the -D switch).");
		sb.append(CR).append("Values for the command-line arguments may be surrounded by double quotes.");
		sb.append(CR).append("Parameters for AppBoot:");
		sb.append(CR).append(APP_MAIN + "\t: the main class of the application to run, or use");
		sb.append(CR).append(APP_NAME + "\t: the name of the application to run.");
		sb.append(CR).append("\tAppBoot will look for a jar-file starting with the given name.");
		sb.append(CR).append("\tThe main-class specified in the manifest of the jar-file will be used to start the application.");
		sb.append(CR).append(APP_HOME_DIR +"\t: the home directory of the application (optional).");
		sb.append(CR).append(APP_CONF_DIR +"\t: the configuration directory of the application (optional, default \"conf\").");
		sb.append(CR).append("\tThe configuration directory is added to the boot-class loader");
		sb.append(CR).append("\tso that configuration files can be opened as a resource in the application.");
		sb.append(CR).append(APP_LIB_DIRNAME +"\t: the name of the lib-directory containing application dependencies (optional, default \"lib\").");
		sb.append(CR).append(APP_BOOT_DEBUG +"\t: a switch (no need to specifiy a value) to let AppBoot show debug-output.");
		sb.append(CR).append(APP_MAVEN_TEST +"\t: a switch to run AppBoot from a Maven target/test-classes directory.");
		sb.append(CR).append("\tIf this switch is used, " + APP_MAIN + " must also be used.");
		//sb.append(CR).append(APP_MAVEN_TEST_SKIP_CLASSES +"\t: a switch to ignore the classes-directory in Maven target directory.");
		sb.append(CR);
		sb.append(CR).append("All AppBoot parameters are removed from the command line arguments before the application main class is started.");
		return sb.toString();
	}

	public static void showInfo() {
		
		showln(getInfo());
	}
	
	public static void main(String... args) {
		
		debug = getPropBool(APP_BOOT_DEBUG, args);
		mavenTest = getPropBool(APP_MAVEN_TEST, args);
		mavenTestSkipClasses = getPropBool(APP_MAVEN_TEST_SKIP_CLASSES, args);
		mainClassName = getProp(APP_MAIN, args);
		appName = getProp(APP_NAME, args);
		if (mainClassName == null) {
			if (AppBoot.mavenTest) {
				throw new RuntimeException("Use of " + APP_MAIN + " is mandatory when " + APP_MAVEN_TEST + " is set.");
			}
			if (appName == null) {
				showInfo();
				return;
			}
		}
		List<File> appFiles = AppResource.getResources(args);
		if (mainClassName == null) {
			mainClassName = AppResource.findMainClass(appFiles, appName);
		}
		// Remove files in system classloader from app-classloader.
		List<File> sysFiles = getSystemFiles();
		for (File f : sysFiles) {
			if (appFiles.contains(f)) {
				appFiles.remove(f);
				if (debug) {
					System.out.println("Removed system class-path: " + f);
				}
			}
		}
		URL[] appUrls = new URL[appFiles.size()];
		for (int i = 0; i < appFiles.size(); i++) {
			appUrls[i] = getUrl(appFiles.get(i));
		}
		bootClassLoader = new URLClassLoader(appUrls, Thread.currentThread().getContextClassLoader());
		System.setProperty(APP_BOOT_CL_HASHCODE, Integer.toString(bootClassLoader.hashCode()));
		runMain(bootClassLoader, mainClassName, filterAppBootArgs(args));
	}
	
	/**
	 * Runs the main-method of the given class.
	 */
	public static void runMain(final URLClassLoader classLoader, final String className, final String... args) {
		runMain(classLoader, className, "main", args);
	}

	/**
	 * Runs the static mainMethod of the given class with the given args.
	 * When this method returns, the original class-loader is put back into the currentThread.
	 */
	public static void runMain(final URLClassLoader classLoader, final String className, final String mainMethod, final String... args) {

		final ClassLoader orgCL = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(classLoader);
		try {
			// Should use Class.forName instead of bootStrapClassLoader.loadClass(className)
			// see also http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6434149.
			Class<?> c = Class.forName(className, false, classLoader);
			// args can be null.
			Method method = c.getDeclaredMethod(mainMethod, new Class[] {String[].class});
			method.invoke(null, new Object[] {args});
		} catch (InvocationTargetException e) {
			BootUtil.rethrowRuntimeCause(e);
		} catch (Exception e) {
			throw new RuntimeException("Failed to invoke main method on " + className, e);
		} finally {
			Thread.currentThread().setContextClassLoader(orgCL);
		}
	}
	
	/**
	 * The full path to the home directory set by AppBoot, always ends with a file separator.
	 * Retrieved via system-property {@link BootKeys#APP_HOME_DIR}.
	 * @return not null.
	 */
	public static String getHomeDir() {
		return System.getProperty(APP_HOME_DIR);
	}
	
	/**
	 * The full path to the configuration directory set by AppBoot, always ends with a file separator.
	 * Retrieved via system-property {@link BootKeys#APP_CONF_DIR}.
	 * @return can return null.
	 */
	public static String getConfDir() {
		return System.getProperty(APP_CONF_DIR);
	}
	
	/**
	 * Uses the Thread's context class-loader hierarchy to find the class loader used by appboot
	 * (lookup is done using the {@link BootKeys#APP_BOOT_CL_HASHCODE} key stored as system property).
	 * @return The class-loader used by AppBoot or null if not found.
	 */
	public static URLClassLoader getAppBootClassLoader() {
		
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		int hc = 0;
		try {
			hc = Integer.valueOf(System.getProperty(APP_BOOT_CL_HASHCODE));
		} catch (Exception ignored) {
			return null;
		}
		while (cl != null && cl.hashCode() != hc) {
			cl = cl.getParent();
		}
		return (cl == null ? null : cl.hashCode() == hc ? (URLClassLoader) cl : null);
	}

	/**
	 * Return files on the class-path from the system classloader.
	 */
	protected static List<File> getSystemFiles() {
		
		List<File> sysFiles = new ArrayList<File>();
		URL[] sysUrls = null;
		
		try {
			// AppBoot is on class-path which is entered into system-classloader.
			final URLClassLoader sysCL = (URLClassLoader) Thread.currentThread().getContextClassLoader();
			sysUrls = sysCL.getURLs();
		} catch (Exception e) {
			// sysCL was probably not a URLClassLoader - nothing we can do
			if (debug) {
				System.out.println("Failed to retrieve URLs from system classloader: " + e);
			}
		}
		if (sysUrls != null) {
			for (URL u : sysUrls) {
				try {
					File f = new File(u.toURI());
					if (f.exists()) {
						sysFiles.add(f);
					}
				} catch (Exception ignored) {
					// URI was not a file
					if (debug) {
						System.out.println("Ignoring URL from system classloader: " + u);
					}
				}
			}
		}
		return sysFiles;
	}
	
	/**
	 * Signals a "stop running as service", for use with Apache Commons Daemon (see http://commons.apache.org/proper/commons-daemon/).
	 * <br>Re-uses the earlier found main class and created boot classloader.
	 * If main class is uknown, an error message appears on stderr.
	 * Example service contents for a myappservice.bat using Commons Daemon:
<pre>sc delete myapps
myapps.exe //IS//myapps ^
--Install="%CD%\myapps.exe" ^
--StartPath="%CD%" ^
--Description="MyApp" ^
--Startup=auto ^
--Jvm=auto ^
++JvmOptions=-Dapp.name=myapp ^
--Classpath=%CD%\lib\appboot.jar ^
--StartMode=jvm ^
--StartClass=com.descartes.appboot.AppBoot ^
++StartParams=-runAsService ^
--StopMode=jvm ^
--StopClass=com.descartes.appboot.AppBoot ^
--StopMethod=stop ^
++StopParams=-stopService ^
--StopTimeout=60 ^
--LogPath="%CD%\logs" ^
--StdOutput=auto ^
--StdError=auto

pause</pre>
	 * In the exampe above, the main class of the application needs to understand the
	 * arguments <tt>-runAsService</tt> and <tt>-stopService</tt>.
	 * Also <tt>myapps.exe</tt> is a renamed version of Common Daemons <tt>prunsrv.exe</tt>
	 * and <tt>myappsw.exe</tt> is a renamed version of Common Daemons <tt>prunmgr.exe</tt>
	 * (placed in the same directory).
	 * <br>A call to <tt>System.exit(0)</tt> is usually enough to stop running as a service.
	 */
	public static void stop(String[] args) {
		
		if (mainClassName == null) {
			System.err.println("Could not stop application service, application main class is unknown.");
		} else {
			runMain(bootClassLoader, mainClassName, "main", args);
		}
	}
	
}
