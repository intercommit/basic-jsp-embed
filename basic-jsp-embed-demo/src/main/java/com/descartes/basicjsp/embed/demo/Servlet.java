package com.descartes.basicjsp.embed.demo;

import com.descartes.basicjsp.embed.Controller;
import com.descartes.basicjsp.embed.MainServlet;
import com.descartes.basicjsp.embed.demo.controller.DirFiles;
import com.descartes.basicjsp.embed.demo.controller.DirTree;
import com.descartes.basicjsp.embed.demo.controller.JmxInfo;
import com.descartes.basicjsp.embed.demo.controller.JmxMonitor;
import com.descartes.basicjsp.embed.demo.controller.OpenFile;

@SuppressWarnings("serial")
public class Servlet extends MainServlet {

	@Override
	protected Controller getController(String path) {
		
		Controller c = super.getController(path);
		if (c != null) {
			return c;
		}
		if (path.equals("/dirtree")) {
			return DirTree.getInstance();
		}
		if (path.equals("/dirfiles")) {
			return DirFiles.getInstance();
		}
		if (path.equals("/openfile")) {
			return OpenFile.getInstance();
		}
		if (path.equals("/monitor")) {
			return JmxMonitor.getInstance();
		}
		if (path.equals("/jmxinfo")) {
			return JmxInfo.getInstance();
		}
		return null;
	}
}
