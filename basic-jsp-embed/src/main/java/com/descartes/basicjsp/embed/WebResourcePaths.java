package com.descartes.basicjsp.embed;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.JarResourceSet;
import org.apache.catalina.webresources.StandardRoot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.descartes.appboot.AppBoot;

/**
 * Registers web-app resources suitable in both Maven runtime environment and in assembly format.
 * <br>Note: for security the {@link PagesFilter} must be used, else resources under "WEB-INF"
 * are always directly accessible.
 * <p>
 * See also {@link TomcatStandardRoot}.
 * @author fwiers
 *
 */
public class WebResourcePaths {

	private static final Logger log = LoggerFactory.getLogger(WebResourcePaths.class);

	/**
	 * @return the absolute path of the jar-file containing the given class.
	 * @throws RuntimeException if the jar-file cannot be found.
	 */
	public static String getJarFilePath(Class<?> classInJar) {
		
		File jarFile = null;
		try {
			jarFile = new File(classInJar.getProtectionDomain().getCodeSource().getLocation().getFile());
		} catch (Exception e) {
			throw new RuntimeException("Unable to find jar-file for class " + classInJar, e);
		}
		return jarFile.getAbsolutePath();
	}
	
	public static final String[] DEFAULT_WEB_JAR_MAPPING = new String[] {"/WEB-INF", "/WEB-INF", "/", "/"};
	public static final String[] DEFAULT_WEB_DIR_MAPPING = new String[] {"/", "/"};
	// The mapping "/WEB-INF" --> "/WEB-INF" is already done when the Maven classes dir
	// is added as web-app to Tomcat in LaunchWebApp.
	public static final String[] MAVEN_CLASSES_DIR_MAPPING = new String[] {"/", "/WEB-INF/classes", "/", "/"};
	
	private final List<String> resourcePaths  = new LinkedList<String>(); 
	protected final Map<String, WebResourceConf> resourceMappings = new HashMap<>();
	private String[] defaultWebJarMapping = DEFAULT_WEB_JAR_MAPPING;
	private String[] defaultWebDirMapping = DEFAULT_WEB_DIR_MAPPING;

	public WebResourcePaths () {
		super();
	}

	/**
	 * @param mavenClassesDir the Maven classes directory containing the classes and resources for the reloadable web-app.
	 */
	public WebResourcePaths(String mavenClassesDir) {
		super();
		addWebDirPath(mavenClassesDir, true, MAVEN_CLASSES_DIR_MAPPING);
	}

	/**
	 * @param mainWebJarClass the class in the main web-jar file.
	 */
	public WebResourcePaths(Class<?> mainWebJarClass) {
		super();
		addWebJarPath(mainWebJarClass);
	}

	/** The list of registered web resource jar-files and directories. */
	public List<String> getResourcePaths() {
		return resourcePaths;
	}
	
	/**
	 * Called by {@link TomcatStandardRoot} to regiser resource-jars.
	 */
	public void registerResources(WebResourceRoot webResources) {
		
		for (String path : getResourcePaths()) {
			WebResourceConf wrconf = resourceMappings.get(path);
			registerResource(webResources, wrconf.path, wrconf.directory, wrconf.pre, wrconf.mappings);
		}
	}
	
	/**
	 * Registers the resource, see also {@link #registerResources(WebResourceRoot)}.
	 */
	protected void registerResource(WebResourceRoot webResources, String resourcePath, boolean isDirType, 
			boolean isPreResource, String... mappings) {

		if (isDirType) {
			log.debug("Registering web resource directory {}", resourcePath);
		} else {
			log.debug("Registering web resource jar-file {}", resourcePath);
		}
		for (int i = 0; i < mappings.length; i += 2) {
			if (isDirType) {
				DirResourceSet dr = new DirResourceSet(webResources, mappings[i + 1], resourcePath, mappings[i]);
				if (isPreResource) {
					webResources.addPreResources(dr);
				} else {
					webResources.addPostResources(dr);
				}
			} else {
				JarResourceSet jr = new JarResourceSet(webResources, mappings[i + 1], resourcePath, mappings[i]);
				webResources.addJarResources(jr);
			}
		}
	}

	/* *** web JARS *** */

	/**
	 * Adds the jar-file containing the given class, see {@link #addWebJarPath(String)}.
	 * @return true
	 */
	public boolean addWebJarPath(Class<?> webJarClass) {
		return addWebJarPath(getJarFilePath(webJarClass));
	}

	/**
	 * Adds the path to a web-jar file to the list of web-resources.
	 * The jar-file is assumned to have resources in the "WEB-INF" folder of the -jar-file
	 * and might contain re-usable JSP-pages in the "WEB-INF/pages" folder 
	 * (served via the {@link PagesFilter}) of the jar-file. 
	 * @return true
	 */
	public boolean addWebJarPath(String webJarPath) {
		return addWebJarPath(webJarPath, getDefaultJarMapping());
	}

	public boolean addWebJarPath(String webJarPath, String... jarMappings) {
		
		if (jarMappings.length % 2 != 0) {
			throw new IllegalArgumentException("Web-jar mapping must be in pairs of internal jar-file path to web-app path.");
		}
		resourceMappings.put(webJarPath, new WebResourceConf(webJarPath, false, jarMappings));
		return resourcePaths.add(webJarPath);
	}

	/* *** web DIRS *** */

	/**
	 * Adds the path to the list of web-resources.
	 * The web-application serves the files in the directory relative to context root ("/"). 
	 * Directories are added last in the list of web-resources: any files or directories 
	 * in other web-resources take precedence. 
	 * @return true
	 */
	public boolean addWebDirPath(String webDirPath) {
		return addWebDirPath(webDirPath, false, getDefaultDirMapping());
	}

	public boolean addWebDirPath(String webDirPath, boolean asPreResource, String... dirMappings) {

		if (dirMappings.length % 2 != 0) {
			throw new IllegalArgumentException("Web-dir mapping must be in pairs of internal dir-path to web-app path.");
		}
		WebResourceConf wrconf = new WebResourceConf(webDirPath, true, dirMappings);
		wrconf.pre = asPreResource;
		resourceMappings.put(webDirPath, wrconf);
		return resourcePaths.add(webDirPath);
	}

	/* *** resource LIB *** */
	
	/**
	 * Currently UNUSED (only here for reference).
	 * <br>Adds the libDir as a {@link DirResourceSet} (internal path is set to "/WEB-INF/lib"),
	 * This is only needed when web-app should reload when a jar changes
	 * ({@link AppBoot} will already serve classes within the jars to Tomcat). 
	 * <br>WARNING: can have strange side-effects and reloading appears to be broken (webapp is stopped but not started).
	 */
	protected void addWebLibDir(StandardRoot webResources, String libDir) {
		
		log.debug("Adding resource lib directory " + libDir);
		DirResourceSet dr = new DirResourceSet(webResources, "/WEB-INF/lib", libDir, "/");
		webResources.addPostResources(dr);
	}

	/* *** properties *** */
	
	public String[] getDefaultJarMapping() {
		return defaultWebJarMapping;
	}

	public void setDefaultJarMapping(String[] defaultWebJarMapping) {
		this.defaultWebJarMapping = defaultWebJarMapping;
	}

	public String[] getDefaultDirMapping() {
		return defaultWebDirMapping;
	}

	public void setDefaultDirMapping(String[] defaultWebDirMapping) {
		this.defaultWebDirMapping = defaultWebDirMapping;
	}

	protected class WebResourceConf {
		
		public String path;
		public String[] mappings;
		public boolean directory;
		public boolean pre;

		public WebResourceConf(String path, boolean directory) {
			this(path, directory, (directory ? getDefaultDirMapping() : getDefaultJarMapping()));
		}

		public WebResourceConf(String path, boolean directory, String... mappings) {
			this.path = path;
			this.directory = directory;
			this.mappings = mappings;
		}
	}
}
