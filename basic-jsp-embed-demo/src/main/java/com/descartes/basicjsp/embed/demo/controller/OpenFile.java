package com.descartes.basicjsp.embed.demo.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.Charsets;

import static com.descartes.basicjsp.embed.WebUtil.*;
import com.descartes.basicjsp.embed.Controller;
import com.descartes.basicjsp.embed.demo.FileUtil;
import com.descartes.basicjsp.embed.demo.OpenDirs;
import com.descartes.basicjsp.embed.demo.Setup;

public class OpenFile implements Controller {

    private OpenFile() { }
    private static class SingletonHolder { 
    	public static final OpenFile INSTANCE = new OpenFile();
    }
    public static OpenFile getInstance() {
    	return SingletonHolder.INSTANCE;
    }

	@Override
	public String handleRequest(HttpServletRequest request, HttpServletResponse response) {
		
		String rloc = getRemoteLocation(request);
		String action = getParamTrimmed(request, "action");
		if (action == null) action = "view";
		String fnameb64 = getParamTrimmed(request, "name");
		if (fnameb64 == null) {
			respondMsg(response, 400, "name parameter required", rloc);
			return null;
		}
		String dirId = getParamTrimmed(request, "dir");
		if (dirId == null) {
			respondMsg(response, 400, "dir parameter required", rloc);
			return null;
		}
		OpenDirs openDirs = Setup.getInstance().getOpenDirs();
		String fname = new String(Base64.decodeBase64(fnameb64), Charsets.UTF_8);
		String dir = openDirs.getDir(Integer.valueOf(dirId));
		File f = new File(FileUtil.endWithSep(dir) + fname);
		FileInputStream in = null;
		try {
			in = new FileInputStream(f);
		} catch (Exception e) {
			respondMsg(response, 404, "File " + fname + " in " + openDirs.getPathName(dir) + " could not be opened: " + e.getClass(), rloc);
			return null;
		}
		if ("view".equals(action)) {
			writeResponse(response, (fname.toLowerCase().endsWith(".xml") ? "text/xml" : "text/plain"), 
					new InputStreamReader(in, Charsets.UTF_8), true);
			return null;
		} 
		// Causes download
		writeResponse(response, fname, in, true);
		return null;
	}

}
