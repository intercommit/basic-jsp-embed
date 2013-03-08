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
