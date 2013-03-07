package com.descartes.basicjsp.embed.demo;

import java.io.File;

public class FileUtil {

	/** Directory for temporary files. Guaranteed to end with a file-separator
	 * (java.io.tmpdir does not always end with a file-separator, for example on Linux)
	 */
	public static final String TmpDir = endWithSep(System.getProperty("java.io.tmpdir"));

	/**
	 * @param s A path
	 * @return The path ending with a file separator (/ or \).
	 */
	public static String endWithSep(final String s) {
		if (s.endsWith("\\") || s.endsWith("/")) return s;
		return s + File.separator;
	}

	/** Returns the canonical or absolute path to the file. */
	public static String getFullPath(final File f) {
		
		String fullPath = null;
		try {
			fullPath = f.getCanonicalPath();
		} catch (Exception e) {
			fullPath = f.getAbsolutePath();
		}
		return fullPath;
	}
}
