package com.descartes.basicjsp.embed.demo.controller;

import java.io.File;
import java.io.FileFilter;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.Charsets;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.descartes.basicjsp.embed.Controller;
import com.descartes.basicjsp.embed.WebUtil;
import com.descartes.basicjsp.embed.demo.OpenDirs;
import com.descartes.basicjsp.embed.demo.Setup;

public class DirFiles implements Controller {

	private static final Logger log = LoggerFactory.getLogger(DirFiles.class);

    private DirFiles() { }
    private static class SingletonHolder { 
    	public static final DirFiles INSTANCE = new DirFiles();
    }
    public static DirFiles getInstance() {
    	return SingletonHolder.INSTANCE;
    }

	@Override
	public String handleRequest(HttpServletRequest request, HttpServletResponse response) {
		
		OpenDirs openDirs = Setup.getInstance().getOpenDirs();
		int dirId = 0;
		try {
			dirId = Integer.valueOf(WebUtil.getParamTrimmed(request, "dir"));
		} catch (Exception ignored) {}
		String path = openDirs.getDir(dirId);
		if (path == null) {
			WebUtil.respondMsg(response, 404, "Directory with ID " + dirId + " no longer available.", null);
			return null;
		}
		File dir = new File(path);
		File[] filesInDir = 
				dir.listFiles(new FileFilter() {
					@Override
					public boolean accept(File f) {
						return f.isFile();
					}
				});
		if (filesInDir == null) {
			log.info("No access to list files in directory " + dir);
			return "/WEB-INF/pages/fileslist.jsp";
		}
		List<File> files = Arrays.asList(dir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File f) {
				return f.isFile();
			}
		}));
		List<String> fileNames = new ArrayList<String>();
		Map<String, File> filesByName = new HashMap<String, File>();
		for (File f : files) {
			String fname = f.getName();
			fileNames.add(fname);
			filesByName.put(fname, f);
		}
		Collections.sort(fileNames, String.CASE_INSENSITIVE_ORDER);
		ArrayList<Map<String, Object>> fileInfo = new ArrayList<Map<String, Object>>();
		NumberFormat nf = Setup.getInstance().getSizeFormatter();
		FastDateFormat df = Setup.getInstance().getFileDateFormat();
		for(String fname : fileNames) {
			Map<String, Object> mfile = new HashMap<String, Object>();
			mfile.put("name", StringEscapeUtils.escapeHtml4(fname));
			mfile.put("nameb64", Base64.encodeBase64URLSafeString(fname.getBytes(Charsets.UTF_8)));
			File f = filesByName.get(fname);
			mfile.put("modified", df.format(f.lastModified()));
			mfile.put("size", nf.format(f.length()));
			fileInfo.add(mfile);
		}
		request.setAttribute("dirId", Integer.toString(dirId));
		request.setAttribute("files", fileInfo);
		request.setAttribute("path", StringEscapeUtils.escapeHtml4(openDirs.getPathName(path)));
		request.setAttribute(PAGE_TITLE, "Files in " + StringEscapeUtils.escapeHtml4(dir.getName()));
		return "/WEB-INF/pages/fileslist.jsp";
	}

}
