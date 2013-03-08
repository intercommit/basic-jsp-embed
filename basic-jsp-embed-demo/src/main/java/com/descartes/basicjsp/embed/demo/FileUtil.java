/*  Copyright 2013 Descartes Systems Group
*
*  This file is part of the "BasicJspEmbedDemo" project hosted on https://github.com/intercommit/basic-jsp-embed
*
*  BasicJspEmbed is free software: you can redistribute it and/or modify
*  it under the terms of the GNU Lesser General Public License as published by
*  the Free Software Foundation, either version 3 of the License, or
*  any later version.
*
*  BasicJspEmbedDemo is distributed in the hope that it will be useful,
*  but WITHOUT ANY WARRANTY; without even the implied warranty of
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*  GNU Lesser General Public License for more details.
*
*  You should have received a copy of the GNU Lesser General Public License
*  along with BasicJspEmbedDemo.  If not, see <http://www.gnu.org/licenses/>.
*
*/
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
