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
package com.descartes.basicjsp.embed;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains methods to clear thread local variables.
 * Tomcat will complain about thread locals in error-messages (<tt>ERROR o.a.c.loader.WebappClassLoader - The web application [] created a ThreadLocal</tt>).
 * Prevent these error messages by calling the {@link #clearThreadLocal(ThreadLocal)} method when the web application is stopped.
 * 
 * @author fwiers
 *
 */
public class ThreadLocalCleaner {

	public static Logger log = LoggerFactory.getLogger(LaunchWebApp.class);

	/**
	 * Calls {@link #clearThreadLocals(Collection)} with the one threadLocal.
	 */
	public static void clearThreadLocal(ThreadLocal<?> threadLocal) {
		clearThreadLocals(Arrays.asList(new ThreadLocal<?>[] { threadLocal }));
	}
	
	/**
	 * Clears (nullifies) all thread-local instances from all threads.
	 * This prevents a severe log-statement from Tomcat about memory leakage through used thread-locals.
	 * Note that Tomcat 7.0.6 can clear these leaks itself, see
	 * https://issues.apache.org/bugzilla/show_bug.cgi?id=49159
	 * <br>Copied from<br>
	 * http://svn.apache.org/repos/asf/tomcat/tc7.0.x/tags/TOMCAT_7_0_8/java/org/apache/catalina/loader/WebappClassLoader.java
	 * @param threadLocals The (static) ThreadLocal variables that are used in this web-app.
	 */
	public static void clearThreadLocals(Collection<ThreadLocal<?>> threadLocals) {
		
		Thread[] threads = getThreads();
		try {
			// Make the fields in the Thread class that store ThreadLocals accessible
			Field threadLocalsField = Thread.class.getDeclaredField("threadLocals");
			threadLocalsField.setAccessible(true);
			Field inheritableThreadLocalsField = Thread.class.getDeclaredField("inheritableThreadLocals");
			inheritableThreadLocalsField.setAccessible(true);
			// Make the "remove from threadLocalMap"-method available
			Class<?> tlmClass = Class.forName("java.lang.ThreadLocal$ThreadLocalMap");
			Method removeKeyEntry = tlmClass.getDeclaredMethod("remove", ThreadLocal.class);
			removeKeyEntry.setAccessible(true);
			for (int i = 0; i < threads.length; i++) {
				if (threads[i] == null) continue;
				Object threadLocalMap;
				// Clear the first map
				threadLocalMap = threadLocalsField.get(threads[i]);
				clearThreadLocals(threadLocals, removeKeyEntry, threadLocalMap);
				// Clear the second map
				threadLocalMap = inheritableThreadLocalsField.get(threads[i]);
				clearThreadLocals(threadLocals, removeKeyEntry, threadLocalMap);
			}
		} catch (Exception e) {
			log.warn("Failed to clear thread-local variables.", e);
		}       
	}
	
	/**
	 * Get the set of current threads as an array.
	 * Used by {@link #clearThreadLocals(Collection)}
	 */
	public static Thread[] getThreads() {

		// Get the current thread group 
		ThreadGroup tg = Thread.currentThread( ).getThreadGroup( );
		// Find the root thread group
		while (tg.getParent() != null) {
			tg = tg.getParent();
		}
		int threadCountGuess = tg.activeCount() + 50;
		Thread[] threads = new Thread[threadCountGuess];
		int threadCountActual = tg.enumerate(threads);
		// Make sure we don't miss any threads
		while (threadCountActual == threadCountGuess) {
			threadCountGuess *= 2;
			threads = new Thread[threadCountGuess];
			// Note tg.enumerate(Thread[]) silently ignores any threads that
			// can't fit into the array 
			threadCountActual = tg.enumerate(threads);
		}
		return threads;
	}

	private static void clearThreadLocals(Collection<ThreadLocal<?>> threadLocals, Method removeKeyEntry, Object threadLocalMap) 
			throws Exception {

		if (threadLocalMap == null) return;
		for (ThreadLocal<?> tl : threadLocals) {
			removeKeyEntry.invoke(threadLocalMap, tl);
		}
	}


}
