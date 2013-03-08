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

import org.jolokia.util.LogHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JmxAgentLogger implements LogHandler {

	private static final Logger log = LoggerFactory.getLogger(JmxAgentLogger.class);

	@Override
	public void debug(String message) {
		log.debug(message);
	}

	@Override
	public void info(String message) {
		log.info(message);
	}

	@Override
	public void error(String message, Throwable t) {
		log.error(message, t);
	}

}
