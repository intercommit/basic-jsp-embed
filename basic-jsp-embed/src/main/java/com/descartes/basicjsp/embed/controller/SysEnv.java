/*  Copyright 2013 Descartes Systems Group
*
*  This file is part of the "BasicJspEmbed" project hosted on https://github.com/intercommit/basic-jsp-embed
*
*  BasicJspEmbed is free software: you can redistribute it and/or modify
*  it under the terms of the GNU Lesser General Public License as published by
*  the Free Software Foundation, either version 3 of the License, or
*  any later version.
*
*  BasicJspEmbed is distributed in the hope that it will be useful,
*  but WITHOUT ANY WARRANTY; without even the implied warranty of
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*  GNU Lesser General Public License for more details.
*
*  You should have received a copy of the GNU Lesser General Public License
*  along with BasicJspEmbed.  If not, see <http://www.gnu.org/licenses/>.
*
*/
package com.descartes.basicjsp.embed.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.descartes.basicjsp.embed.Controller;
import com.descartes.basicjsp.embed.SysPropsUtil;


/**
 * Show the system environment using methods from {@link SysPropsUtil}.
 * @author frederikw
 *
 */
public class SysEnv implements Controller {

    private SysEnv() { }
    private static class SingletonHolder { 
    	public static final SysEnv INSTANCE = new SysEnv();
    }
    public static SysEnv getInstance() {
    	return SingletonHolder.INSTANCE;
    }

	@Override
	public String handleRequest(final HttpServletRequest request, final HttpServletResponse response) {
		
		request.setAttribute(PAGE_TITLE, "System");
		request.setAttribute("memoryUsage", SysPropsUtil.getMemoryUsage());
		request.setAttribute("systemEnv", SysPropsUtil.getSystemEnv());
		request.setAttribute("systemProps", SysPropsUtil.getSystemProps());
		return "/WEB-INF/pages/sysenv.jsp";
	}
}
