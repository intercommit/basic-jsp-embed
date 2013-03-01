/*  Copyright 2013 Descartes Systems Group
*
*  This file is part of the "BasicJspEmbedDemo" project hosted on https://github.com/intercommit/basic-jsp-embed
*
*  BasicJspEmbedDemo is free software: you can redistribute it and/or modify
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
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.descartes.appboot.AppBoot;

/**
 * Retrieves files and directories from {@link AppBoot#getHomeDir()}.
 * The directories are somewhat obfuscated: the original root-directory
 * is never shown, instead the alias is shown.
 * @author fwiers
 *
 */
public class OpenDirs {

	private static final Logger log = LoggerFactory.getLogger(OpenDirs.class);

	private Map<Integer, String> dirForId = new HashMap<Integer, String>();
	private List<String> sortedDirs = new ArrayList<String>();
	private Set<String> dirsWithAlias = new HashSet<String>();
	private Map<String, String> aliasForDir = new HashMap<String, String>();
	private Map<Integer, Integer> dirLevelById = new HashMap<Integer, Integer>();
	
	public OpenDirs initialize() {
		
		/*
		 * This method is a bit over-engineered.
		 * Originally it was able to handle multiple root-directories with aliasses.
		 * This implementation limits to 1 directory with a fixed alias.
		 */
		List<String> propDirs = new ArrayList<String>();
		propDirs.add(AppBoot.getHomeDir());
		Map<String, String> propDirAlias = new HashMap<String, String>();
		propDirAlias.put("1", "BasicJSP Demo Home");
		Map<String, String> propSubdirs = new HashMap<String, String>();
		propSubdirs.put("1", "-1");
		Set<String> opendDirs = new HashSet<String>();
		if (propDirs.isEmpty()) propDirs.add(FileUtil.TmpDir);
		for (int i = 0; i < propDirs.size(); i++) {
			String dir = propDirs.get(i);
			File f = new File(dir);
			if (!f.isDirectory()) {
				log.warn("Directory does not exist: " + f);
				continue;
			}
			String dirFull = FileUtil.getFullPath(f);
			if (opendDirs.contains(dirFull)) {
				log.debug("Directory already added: " + dirFull);
				continue;
			}
			String dirAlias = null;
			try {
				dirAlias = propDirAlias.get(Integer.toString(i+1));
			} catch (Exception ignored) {}
			if (!(dirAlias == null || dirAlias.isEmpty())) {
				aliasForDir.put(dirFull, dirAlias);
				dirsWithAlias.add(dirFull);
			}
			int maxSubDirLevel = 0;
			try {
				maxSubDirLevel = Integer.valueOf(propSubdirs.get(Integer.toString(i+1)));
			} catch (Exception ignored) {}
			List<String> allDirs = getSubDirs(dirFull, maxSubDirLevel);
			if (log.isDebugEnabled() && allDirs.size() > 1) {
				log.debug("Found " + (allDirs.size() -1) + " sub-directories in " + dirFull);
			}
			opendDirs.addAll(allDirs);
		}
		sortedDirs.addAll(opendDirs);
		Collections.sort(sortedDirs, String.CASE_INSENSITIVE_ORDER);
		for (String dir : sortedDirs) {
			dirForId.put(getDirId(dir), dir);
		}
		log.info("Allowing access to files in " + sortedDirs.size() + " directories.");
		return this;
	}
	
	/** Returns an un-modifiable list of the open directories sorted by name. */ 
	public List<String> list() { return Collections.unmodifiableList(sortedDirs); }
	public int getDirId(String dir) { return dir.hashCode(); }
	public String getDir(int id) { return dirForId.get(id); }
	public int getDirLevel(int dirId) { return dirLevelById.get(dirId); }
	public int getDirLevel(String dir) { return dirLevelById.get(getDirId(dir)); }
	
	public String getDirName(String dir) {
		
		if (dirsWithAlias.contains(dir)) { 
			return aliasForDir.get(dir);
		}
		int nameIndex = dir.lastIndexOf(File.separatorChar);
		if (nameIndex < 1) return dir;
		return (nameIndex < 1 ? dir : dir.substring(nameIndex + 1));
		
	}
	
	public String getPathName(String dir) {
		
		String aliasDir = null;
		for (String d : dirsWithAlias) {
			if (dir.startsWith(d)) {
				aliasDir = d;
				break;
			}
		}
		if (aliasDir == null) return dir;
		return aliasForDir.get(aliasDir) + dir.substring(aliasDir.length());
	}
	
	/** maxLevel -1 indicates no maxLevel. */
	private List<String> getSubDirs(String dir, int maxLevel) {
		
		List<String> dirs = new LinkedList<String>();
		getSubDirsRecursive(dir, dirs, 1, maxLevel);
		return dirs;
	}

	private void getSubDirsRecursive(String dir, List<String> dirs, int level, int maxLevel) {
		
		dirs.add(dir);
		dirLevelById.put(getDirId(dir), level);
		if (maxLevel != -1 && level > maxLevel) return;
		File d = new File(dir);
		if (d.isDirectory()) {
			File[] subdirs = d.listFiles(new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					return pathname.isDirectory();
				}
			});
			if (subdirs == null) {
				log.info("No list-access for directory " + d);
			} else {
				for(File sd : subdirs) {
					// Do not use FileUtil.getFullPath because that resolves links on Linux to different root-paths. 
					String subDir = dir + File.separator + sd.getName();
					getSubDirsRecursive(subDir, dirs, level+1, maxLevel);
				}
			}
		}
	}
	
}
