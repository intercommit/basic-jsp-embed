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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Various utility functions for zip-files used by classes in this package.
 * @author fwiers
 *
 */
public class ZipUtil {
	
	private ZipUtil() {}

	/**
	 * Searches for a zip-entry and returns it as an input-stream (backed by a byte-array). 
	 * @param entryNameStart null/empty or the first part of the name of the zip-entry that should be opened. 
	 * @param entryNameEnd null/empty or the last part of the name of the zip-entry that should be opened.
	 * @return An input-stream (that does not have to be closed, it is a {@link ByteArrayInputStream}.
	 */
	public static InputStream getInputStream(File zippedFile, String entryNameStart, String entryNameEnd) throws IOException {

		ZipFile zipFile = null;
		InputStream in = null;
		try {
			zipFile = new ZipFile(zippedFile);
			Enumeration<? extends ZipEntry> e = zipFile.entries();
			while (e.hasMoreElements()) {
				ZipEntry entry = (ZipEntry) e.nextElement();
				String fname = entry.getName();
				boolean foundEntry = BootUtil.isEmpty(entryNameStart) ? true : fname.startsWith(entryNameStart);
				if (foundEntry) {
					foundEntry = BootUtil.isEmpty(entryNameEnd) ? true : fname.endsWith(entryNameEnd);
				}
				if (foundEntry) {
					in = getInputStream(zipFile, entry);
					break;
				}
			}		
		} catch (Exception ignored) {
			//ignored.printStackTrace();
			;
		} finally {
			// JRE 6: zip file is NOT a closeable
			// BootUtil.close(zipFile);
			try {
				zipFile.close();
			} catch (Exception ignored) {
				;
			}
		}
		return in;
	}
	
	/**
	 * Returns the bytes form the zip-entry as input-stream.
	 */
	public static InputStream getInputStream(ZipFile zipFile, ZipEntry entry) throws IOException {

		return new ByteArrayInputStream(getBytes(zipFile, entry));
	}
	
	/**
	 * Returns the bytes form the zip-entry.
	 */
	public static byte[] getBytes(ZipFile zipFile, ZipEntry entry) throws IOException {
		
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		InputStream in = zipFile.getInputStream(entry);
		int b;
		while ((b = in.read()) != -1) {
			bout.write(b);
		}
		return bout.toByteArray();
	}

}
