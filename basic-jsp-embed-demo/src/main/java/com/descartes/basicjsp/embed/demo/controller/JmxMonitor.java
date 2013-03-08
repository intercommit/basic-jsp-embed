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
package com.descartes.basicjsp.embed.demo.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.descartes.basicjsp.embed.Controller;

public class JmxMonitor implements Controller {

    private static class SingletonHolder { 
    	public static final JmxMonitor INSTANCE = new JmxMonitor();
    }
    public static JmxMonitor getInstance() {
    	return SingletonHolder.INSTANCE;
    }

	@Override
	public String handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {

		request.setAttribute(PAGE_TITLE, "Monitor");
		return "/WEB-INF/pages/monitor.jsp";
	}

}
