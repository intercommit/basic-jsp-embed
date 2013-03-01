/*  Copyright 2013 Descartes Systems Group
*
*  This file is part of the "BasicJspEmbedDemo" project hosted on https://github.com/intercommit/basic-jsp-embed
*
*  BasicJspEmbedDemo is free software: you can redistribute it and/or modify
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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.concurrent.atomic.AtomicReference;

import javax.servlet.ServletContextEvent;

import org.apache.commons.lang3.time.FastDateFormat;

import com.descartes.appboot.BootUtil;
import com.descartes.basicjsp.embed.ThreadLocalCleaner;
import com.descartes.basicjsp.embed.WebConfig;
import com.descartes.basicjsp.embed.WebSetup;

public class Setup extends WebSetup {
	
	public static final String DefaultDateFormat = "dd/MM/yyyy HH:mm:ss";

	/** http://stackoverflow.com/questions/1285279/java-synchronization-issue-with-numberformat */
	private static ThreadLocal<NumberFormat> SizeNumberFormat;

	private AtomicReference<OpenDirs> openDirsRef;
	private FastDateFormat fileDateFormat;

	public static Setup getInstance() {
		return (Setup)instance;
	}

	public OpenDirs getOpenDirs() { 
		return openDirsRef.get(); 
	}

	public void refreshOpenDirs() { 
		openDirsRef.lazySet(new OpenDirs().initialize()); 
	}

	@Override
	public WebConfig getConfig() {
		return new WebConfig() {{ 
				setAppName("JspDemo"); 
				setVersion(BootUtil.getPomVersion(Setup.class)); 
			}};
	}
	
	public NumberFormat getSizeFormatter() {
		return SizeNumberFormat.get(); 
	}

	public FastDateFormat getFileDateFormat() {
		return fileDateFormat;
	}

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		
		super.contextInitialized(sce);
		instance = this;
		openDirsRef = new AtomicReference<OpenDirs>(new OpenDirs().initialize());
		String datePattern = DefaultDateFormat;
		try {
			fileDateFormat = FastDateFormat.getInstance(datePattern);
		} catch (IllegalArgumentException e) {
			log.warn("Date pattern [" + datePattern + "] is invalid, defaulting to " + DefaultDateFormat);
			fileDateFormat = FastDateFormat.getInstance(DefaultDateFormat);
		}
		SizeNumberFormat = new ThreadLocal<NumberFormat>() {
			@Override protected NumberFormat initialValue() {
				DecimalFormat df = new DecimalFormat();
				df.setGroupingUsed(true); df.setGroupingSize(3);
				DecimalFormatSymbols dfs = new DecimalFormatSymbols();
				dfs.setGroupingSeparator(' ');
				df.setDecimalFormatSymbols(dfs);
				df.setPositivePrefix("");
				df.setPositiveSuffix("");
				return df;
			}
		};
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		
		ThreadLocalCleaner.clearThreadLocal(SizeNumberFormat);
		super.contextDestroyed(sce);
	}

}
