/*  Copyright 2013 Descartes Systems Group
*
*  This file is part of the "AppBootTest" project hosted on https://github.com/intercommit/basic-jsp-embed
*
*  AppBootTest is free software: you can redistribute it and/or modify
*  it under the terms of the GNU Lesser General Public License as published by
*  the Free Software Foundation, either version 3 of the License, or
*  any later version.
*
*  AppBootTest is distributed in the hope that it will be useful,
*  but WITHOUT ANY WARRANTY; without even the implied warranty of
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*  GNU Lesser General Public License for more details.
*
*  You should have received a copy of the GNU Lesser General Public License
*  along with AppBootTest.  If not, see <http://www.gnu.org/licenses/>.
*
*/
package com.descartes.appboot.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.descartes.appboot.BootKeys;
import com.descartes.appboot.BootUtil;

/**
 * This project provides an assembly that can be used to test appboot functions.
 * <br>After running <code>maven clean package</code>, target/test-classes/runtest.bat
 * can be used to check the appboot functions in a Maven test environment.
 * <br>After running <code>assemble.bat</code>, target/appboot-test-version-bin/run.bat
 * can be used to check the appboot functions in a runtime environment.
 *
 * @author fwiers
 *
 */
public class AppBootTest {

	private static final Logger log = LoggerFactory.getLogger(AppBootTest.class);
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		log.info("AppBoot Test Jar " + BootUtil.getPomVersion(AppBootTest.class) + " - main method in " + AppBootTest.class);
		log.info("The log-statements should show a time-stamp as configured in simplelogger.properties");
		StringBuilder sb = new StringBuilder("Main Arguments: ");
		if (BootUtil.isEmpty(args)) {
			sb.append("none");
		} else {
			for (String s : args) {
				sb.append('\n').append(s);
			}
		}
		log.info(sb.toString());
		if (System.getProperties().containsKey(BootKeys.APP_MAVEN_TEST)) {
			log.info("Started in Maven test environment.");
		} else {
			log.info("Started in assembly environment.");
		}
	}

}
