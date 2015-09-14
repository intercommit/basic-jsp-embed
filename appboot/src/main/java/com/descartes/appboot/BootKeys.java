package com.descartes.appboot;

import static com.descartes.appboot.BootUtil.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BootKeys {

	/**
	 * The main class of the application to run with the boot class loader.
	 * Alternatively, {@link #APP_NAME} can be used.
	 */
	public static final String APP_MAIN = "app.main.class";

	/**
	 * The artifactId of the application to run.
	 * AppBoot uses this value to search for a jar that starts with the specified name.
	 * This jar should have a manifest with a main-class. AppBoot will run the main-class with the boot class loader.
	 * Example command line:
	 * <br><code>java -Dapp.name=myapp -jar lib/appboot.jar</code>
	 */
	public static final String APP_NAME = "app.name";
	
	/**
	 * A switch to indicate that AppBoot should show the files added to the boot class loader.
	 */
	public static final String APP_BOOT_DEBUG = "app.boot.debug";
	
	/**
	 * A switch to indicate that the programm is started from a maven/target/test-classes directory.
	 * If set, the use of {@link #APP_MAIN} is mandatory.
	 * AppBoot will add all jars in the directory of appboot.jar itself (usually target/dependency),
	 * target/classes and target/test-classes to the boot class loader. 
	 * Example command line (from target/test-classes):
	 * <br><code>java -Dapp.maven.test -jar ..\dependency\appboot.jar app.main.class=com.mycompany.myapp.AppMain</code>
	 */
	public static final String APP_MAVEN_TEST = "app.maven.test";

	/**
	 * Can be used in combination with {@link #APP_MAVEN_TEST} to skip adding the "classes" directory
	 * to the classloader. Not sure when this is useful.
	 */
	public static final String APP_MAVEN_TEST_SKIP_CLASSES = "app.maven.test.skip.classes";

	/**
	 * The path to the home-directory of the application (optional).
	 * AppBoot determines the home-directory of the application using the location of the appboot.jar itself.
	 * This behavior can be disabled by setting the value for this property.
	 * <br>The determined home directory is available as system-property, see also {@link AppBoot#getHomeDir()}. 
	 */
	public static final String APP_HOME_DIR = "app.home.dir";
	
	/**
	 * The name of the directory containing application dependencies (optional).
	 * By default AppBoot assumes "lib".
	 */
	public static final String APP_LIB_DIRNAME = "app.lib.dirname";
	
	/**
	 * The path (can be relative to the home-directory) to the configuration directory of the application (optional).
	 * By default AppBoot assumes home-dir/conf.
	 * <br>The determined configuration directory is available as system-property, see also {@link AppBoot#getConfDir()}. 
	 */
	public static final String APP_CONF_DIR = "app.conf.dir";
	
	/**
	 * The hash-code of the boot-classloader, set as system property when boot class loader is created.
	 * See also {@link AppBoot#getAppBootClassLoader()}.
	 */
	public static final String APP_BOOT_CL_HASHCODE = "app.boot.classloader.hashcode";
	
	/**
	 * All boot key switches.
	 */
	public static final String[] BOOT_KEY_SWITCHES = new String[] { APP_BOOT_DEBUG, APP_MAVEN_TEST, APP_MAVEN_TEST_SKIP_CLASSES };
	/**
	 * All boot keys that take a value.
	 */
	public static final String[] BOOT_KEY_VALUES = new String[] { APP_CONF_DIR, APP_HOME_DIR, APP_LIB_DIRNAME, APP_MAIN, APP_NAME };
	
	/**
	 * Called by AppBoot to remove all properties in this class from the command line arguments.
	 * Removes all args from the list that start with a value in the set returned by {@link #getAppBootArgVariations()}.
	 * Uses {@link BootUtil#valueStartsWith(Set, String)} to compare the values to the set. 
	 */
	public static String[] filterAppBootArgs(String... orgArgs) {
		
		if (isEmpty(orgArgs)) return orgArgs;
		List<String> args = new ArrayList<String>();
		final Set<String> appBootVars = getAppBootArgVariations();
		for (String arg : orgArgs) {
			if (isEmpty(arg)) {
				if (arg != null) {
					// could be an empty value for some reason
					args.add(arg);
				}
			} else if (!valueStartsWith(appBootVars, arg)) {
				args.add(arg);
			}
		}
		return args.toArray(new String[args.size()]);
	}
	
	/**
	 * Returns all boot-keys including all variations.
	 * For switches: switch and -switch
	 * For key-values: key= and -key=
	 */
	public static Set<String> getAppBootArgVariations() {
		
		Set<String> appBootArgs = new HashSet<String>();
		// switches do not need a pre- or postfix to be an argument.
		appBootArgs.addAll(Arrays.asList(BOOT_KEY_SWITCHES));
		appBootArgs.addAll(prefixValues(Arrays.asList(BOOT_KEY_SWITCHES), "-"));
		appBootArgs.addAll(postfixValues(Arrays.asList(BOOT_KEY_VALUES), "="));
		appBootArgs.addAll(postfixValues(
				prefixValues(Arrays.asList(BOOT_KEY_VALUES), "-"), "="));
		return appBootArgs;
	}
	
}
