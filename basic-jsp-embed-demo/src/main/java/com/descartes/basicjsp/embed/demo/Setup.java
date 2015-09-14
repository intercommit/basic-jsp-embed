package com.descartes.basicjsp.embed.demo;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.concurrent.atomic.AtomicReference;

import javax.servlet.ServletContextEvent;

import org.apache.commons.lang3.time.FastDateFormat;

import com.descartes.basicjsp.embed.AppClassLoader;
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
				setVersion(AppClassLoader.getVersion(Setup.class)); 
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
