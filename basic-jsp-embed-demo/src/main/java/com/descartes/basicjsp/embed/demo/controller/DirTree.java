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
package com.descartes.basicjsp.embed.demo.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.descartes.basicjsp.embed.Controller;
import com.descartes.basicjsp.embed.WebUtil;
import com.descartes.basicjsp.embed.demo.OpenDirs;
import com.descartes.basicjsp.embed.demo.Setup;

public class DirTree implements Controller {

	private static final Logger log = LoggerFactory.getLogger(DirTree.class);
	
    private DirTree() { }
    private static class SingletonHolder { 
    	public static final DirTree INSTANCE = new DirTree();
    }
    public static DirTree getInstance() {
    	return SingletonHolder.INSTANCE;
    }

	@Override
	public String handleRequest(HttpServletRequest request,	HttpServletResponse response) throws Exception {
		
		request.setAttribute(PAGE_TITLE, "DirTree");
		final String submitType = request.getParameter("submit");
		if ("refresh".equals(submitType)) {
			try {
				Setup.getInstance().refreshOpenDirs();
			} catch (Exception e) {
				log.error("Refreshing open directories failed.", e);
			}
		}
		if (log.isDebugEnabled()) {
			log.debug("Showing open directories for " + WebUtil.getRemoteLocation(request));
		}
		ArrayList<Map<String, Object>> dirs = new ArrayList<Map<String, Object>>();
		OpenDirs openDirs = Setup.getInstance().getOpenDirs();
		int previousLevel = 1;
		Map<String, Object> mdir = null;
		for (String dir : openDirs.list()) {
			if (mdir != null) {
				String levelEnd = getLevelEnd(openDirs, dir, previousLevel);
				//log.debug("End: " + levelEnd + ", dlevel: " + openDirs.getDirLevel(dir) + ", plevel " + previousLevel);
				mdir.put("levelEnd", levelEnd);
			}
			mdir = new HashMap<String, Object>();
			mdir.put("name", openDirs.getDirName(dir));
			mdir.put("id", openDirs.getDirId(dir));
			dirs.add(mdir);
			previousLevel = openDirs.getDirLevel(dir);
		}
		request.setAttribute("closeDirLevel", getCloseLevel(openDirs, previousLevel));
		request.setAttribute("dirs", dirs);
		return "/WEB-INF/pages/dirtree.jsp";
	}

	private String getLevelEnd(OpenDirs openDirs, String dir, int previousLevel) {
		
		int level = openDirs.getDirLevel(dir);
		if (level == previousLevel) return "</li>";
		String s = "";
		if (level < previousLevel) {
			for (int i = level; i < previousLevel; i ++) {
				s += "</li></ul>";
			}
		} else {
			for (int i = previousLevel; i < level; i ++) {
				s += "<ul>";
			}
		}
		return s;
	}

	private String getCloseLevel(OpenDirs openDirs, int lastLevel) {
		
		String s = "";
		for (int i = 1; i < lastLevel; i ++) {
			s += "</li></ul>";
		}
		return s;
	}
	
}
