package com.descartes.basicjsp.embed.demo;

import com.descartes.basicjsp.embed.ControllerFactorySingleton;
import com.descartes.basicjsp.embed.MainServlet;
import com.descartes.basicjsp.embed.demo.controller.DirFiles;
import com.descartes.basicjsp.embed.demo.controller.DirTree;
import com.descartes.basicjsp.embed.demo.controller.JmxInfo;
import com.descartes.basicjsp.embed.demo.controller.JmxMonitor;
import com.descartes.basicjsp.embed.demo.controller.OpenFile;
import com.descartes.basicjsp.embed.demo.controller.WsEcho;

@SuppressWarnings("serial")
public class Servlet extends MainServlet {

	@Override
	protected void buildControllerFactories() {
		
		super.buildControllerFactories();
		controllerFactories.put("/dirtree", new ControllerFactorySingleton(new DirTree()));
		controllerFactories.put("/dirfiles", new ControllerFactorySingleton(new DirFiles()));
		controllerFactories.put("/openfile", new ControllerFactorySingleton(new OpenFile()));
		controllerFactories.put("/monitor", new ControllerFactorySingleton(new JmxMonitor()));
		controllerFactories.put("/jmxinfo", new ControllerFactorySingleton(new JmxInfo()));
		controllerFactories.put("/wsecho", new ControllerFactorySingleton(new WsEcho()));
	}

}
