/*  Copyright 2013 Descartes Systems Group
*
*  This file is part of the "AppBoot" project hosted on https://github.com/intercommit/basic-jsp-embed
*
*  AppBoot is free software: you can redistribute it and/or modify
*  it under the terms of the GNU Lesser General Public License as published by
*  the Free Software Foundation, either version 3 of the License, or
*  any later version.
*
*  AppBoot is distributed in the hope that it will be useful,
*  but WITHOUT ANY WARRANTY; without even the implied warranty of
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*  GNU Lesser General Public License for more details.
*
*  You should have received a copy of the GNU Lesser General Public License
*  along with AppBoot.  If not, see <http://www.gnu.org/licenses/>.
*
*/
package com.descartes.appboot;

import static com.descartes.appboot.AppBoot.*;
import static com.descartes.appboot.BootKeys.*;
import static com.descartes.appboot.BootUtil.*;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * Functions related to looking up resources for the boot class loader.
 *
 */
public class AppResource {

	/**
	 * The main function looking up the files and directories for the boot class loader.
	 */
	public static List<File> getResources(String... args) {
		
		File appHomeDir = null;
		File appLibDir = null;
		String libDirName = getProp(APP_LIB_DIRNAME, args);
		if (libDirName == null) {
			libDirName = "lib";
		}
		String homeProp = getProp(APP_HOME_DIR, args);
		if (homeProp != null) {
			appHomeDir = new File(homeProp);
			if (!appHomeDir.isDirectory()) {
				throw new RuntimeException("Application home directory does not exist: " + homeProp);
			}
		} else {
			File bootFile = getJarFile(AppResource.class);
			if (bootFile == null) {
				throw new RuntimeException("Could not find the jar-file containing the AppBoot classes.");
			}
			appHomeDir = bootFile.getParentFile();
			if (mavenTest) {
				// target/dependency
				appLibDir = appHomeDir;
				// target/test-classes
				appHomeDir = new File(endWithSep(appLibDir.getParent()) + "test-classes");
			} else if (appHomeDir.getName().equalsIgnoreCase(libDirName)) {
				appLibDir = appHomeDir;
				appHomeDir = appHomeDir.getParentFile();
			}
		}
		System.setProperty(APP_HOME_DIR, endWithSep(getFullPath(appHomeDir)));
		if (appLibDir == null) {
			appLibDir = new File(endWithSep(appHomeDir.getPath()) + libDirName);
			if (!appLibDir.isDirectory()) {
				appLibDir = null;
			}
		}
		String confProp = getProp(APP_CONF_DIR, args);
		File appConfDir = null;
		if (confProp != null) {
			appConfDir = new File(System.getProperty(APP_HOME_DIR) + confProp);
			if (!appConfDir.isDirectory()) {
				appConfDir = new File(confProp);
			}
			if (!appConfDir.isDirectory()) {
				throw new RuntimeException("Application configuration directory does not exist: " + confProp);
			}
		} else {
			 appConfDir = new File(endWithSep(appHomeDir.getPath()) + "conf");
			 if (!appConfDir.isDirectory()) {
				 appConfDir = null;
			 }
		}
		if (appConfDir != null) {
			System.setProperty(APP_CONF_DIR, endWithSep(getFullPath(appHomeDir)));
		}
		if (debug) {
			showln("Application home: " + appHomeDir);
			showln("Application lib : " + appLibDir);
			showln("Application conf: " + appConfDir);
		}
		@SuppressWarnings("serial")
		List<File> classPaths = new ArrayList<File>() {
			public boolean add(File f) {
				if (debug) {
					showln("Added to class-path: " + f);
				}
				return super.add(f);
			}
		};
		if (mavenTest || appLibDir == null || appConfDir == null) {
			classPaths.add(appHomeDir);
			addJars(classPaths, appHomeDir);
		}
		if (mavenTest && !mavenTestSkipClasses) {
			// add target/classes
			classPaths.add(new File(endWithSep(appHomeDir.getParent()) + "classes"));
		}
		if (appConfDir != null) {
			classPaths.add(appConfDir);
		}
		if (appLibDir != null) {
			classPaths.add(appLibDir);
			addJars(classPaths, appLibDir);
		}
		return classPaths;
	}
	
	/**
	 * Adds any file with a name ending with ".jar" (case-insensitive) in dir to classPaths.
	 */
	public static void addJars(List<File> classPaths, File dir) {
		
		String[] fileNames = dir.list();
		String dirName = endWithSep(dir.getPath());
		for (String fname : fileNames) {
			if (fname.toLowerCase().endsWith(".jar")) {
				File f = new File(dirName + fname);
				if (f.isFile()) {
					classPaths.add(new File(dirName + fname));
				}
			}
		}
	}

	/**
	 * Searches for a jar-files that matches appName (case-insensitive).
	 * A jar-file named "appName.jar" takes precedence over a jar-file named "appName-*.jar"
	 */
	public static String findMainClass(List<File> appFiles, String appName) {
		
		File mainJarFile = null;
		String appNameLC = appName.toLowerCase();
		for (File f : appFiles) {
			if (f.isDirectory()) continue;
			String fname = f.getName().toLowerCase();
			if (fname.startsWith(appNameLC)) {
				if (mainJarFile == null) {
					mainJarFile = f;
				} else {
					if (fname.equals(appNameLC + ".jar")) {
						mainJarFile = f;
					} else if (fname.startsWith(appNameLC + "-")) {
						String current = mainJarFile.getName().toLowerCase();
						if (!fname.equals(current)) {
							mainJarFile = f;
						}
					}
				}
			}
		}
		if (mainJarFile == null) {
			throw new RuntimeException("Could not find a jar-file for application name " + appName);
		} 
		if (debug) {
			showln("Main class found in jar-file " + mainJarFile.getName());
		}
		String mainClass = getMainClass(mainJarFile);
		if (mainClass == null) {
			throw new RuntimeException("Could not find a main-class in manifest of jar-file: " + mainJarFile);
		} 
		if (debug) {
			showln("Main class: " + mainClass);
		}
		return mainClass;
	}
	
	/**
	 * Reads the "Main-class" from "META-INF/MANIFEST.MF".
	 */
	public static String getMainClass(File jarFile) {
		
		String mainClass = null;
		InputStream in = null;
		try {
			in = ZipUtil.getInputStream(jarFile, "META-INF/MANIFEST.MF", null);
			Manifest manifest = new Manifest(in);
			Attributes attr = manifest.getMainAttributes();
			mainClass = attr.getValue("Main-Class");
		} catch (Exception ignored) {
			;
		}
		return mainClass;
	}

}
