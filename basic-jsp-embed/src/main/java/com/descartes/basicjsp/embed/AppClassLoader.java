package com.descartes.basicjsp.embed;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.descartes.appboot.AppBoot;
import com.descartes.appboot.BootUtil;
import com.descartes.appboot.ZipUtil;

/**
 * Access to static (instances) of classes or singletons from the web-app is not possible:
 * the Tomcat class-loader reloads these classes.
 * To work around this, delegate can be set to "true" in {@link LaunchWebApp#webLoader}
 * but this breaks the web-app contract and also disables reloading for all classes.
 * <p>
 * Methods in this class can be called from the web-app and use the class-loader
 * from {@link AppBoot#bootClassLoader} to get access to classes loaded during startup. 
 * @author FWiers
 *
 */
public class AppClassLoader {

	private static final Logger log = LoggerFactory.getLogger(AppClassLoader.class);

	private AppClassLoader() {}

	public static ClassLoader getAppClassLoader() {

		ClassLoader cl = AppBoot.getAppBootClassLoader();
		if (cl == null) {
			cl = ClassLoader.getSystemClassLoader();
			log.info("No context class loader available, using system classloader.");
		}
		return cl;
	}

	public static Class<?> getAppClass(String className) throws Exception {
		return getAppClassLoader().loadClass(className);
	}

	public static Method getMethod(Class<?> clazz, String methodName) throws Exception {
		return getMethod(clazz, methodName, (Class<?>[]) null);
	}

	public static Method getMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) throws Exception {
		return clazz.getMethod(methodName, parameterTypes);
	}

	public static Object getStaticObject(Method staticMethod) throws Exception {
		return getStaticObject(staticMethod, (Object[]) null);
	}

	public static Object getStaticObject(Method staticMethod, Object... args) throws Exception {
		return staticMethod.invoke((Object[]) null, args);
	}

	public static Object getInstance(String className) throws Exception {
		return getStaticObject(getMethod(getAppClass(className), "getInstance"));
	}
	
	public static Object invokeOnInstance(Object instance, String methodName) throws Exception {
		
		Method m = instance.getClass().getMethod(methodName, (Class<?>[]) null);
		return m.invoke(instance, (Object[]) null);
	}

	/**
	 * The normal {@link BootUtil#getPomVersion(Class)} does not work due to the Tomcat classloader.
	 * This method does the same but uses the boot classloader which has a better chance of finding the version number.
	 */
	public static String getVersion(Class<?> clazz) {
		
		String version = BootUtil.UNKNOWN_VERSION;
		ClassLoader cl = getAppClassLoader();
        try {
        	Class<?> vc = cl.loadClass(clazz.getName());
        	File jarFile = BootUtil.getJarFile(vc);
        	if (jarFile == null) {
        		log.debug("Unable to find jarFile containing pom-version information.");
        	} else {
        		InputStream in = null;
        		try {
        			in = ZipUtil.getInputStream(jarFile, "META-INF", "pom.properties");
        			if (in != null) {
        				version = BootUtil.getProps(in).getProperty("version", BootUtil.UNKNOWN_VERSION);
        			}
        		} finally {
        			if (in != null) {
        				in.close();
        			}
        		}
        	}
        } catch (Exception e) {
        	log.debug("Could not find pom-version for class " + clazz.getSimpleName() + " - " + e);
        }
		return version;
	}

}
