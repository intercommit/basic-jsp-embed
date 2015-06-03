package com.descartes.basicjsp.embed;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Static methods dealing with propery values from the system (environment).
 * @author fwiers
 *
 */
public class SysPropsUtil {

	private SysPropsUtil() {}

	public static final String lf = System.getProperty("line.separator");

	/** Set to true to disable logging of system (environment) properties by {@link #logSysProps(org.slf4j.Logger, boolean, boolean)}. */
	public static boolean logNothing;

	/**
	 * Shows system properties (from {@link System#getProperties()}) and system environment properties (from {@link System#getenv()}) in log.
	 * @param log The log to log the properties to in one log-statement.
	 * @param includeEnv Include system environment properties or not.
	 * @param debug log as debug statement or, if false, log as info-statement.
	 */
	public static void logSysProps(final org.slf4j.Logger log, final boolean includeEnv, final boolean debug) {

		if (logNothing || (debug && !log.isDebugEnabled())) return;
		if (includeEnv) {
			if (debug) {
				log.debug("System environment properties: {}", getSystemEnv());
			} else {
				log.info("System environment properties: {}", getSystemEnv());
			}
		}
		if (debug) {
			log.debug("System properties: {}", getSystemProps());
		} else {
			log.info("System properties: {}", getSystemProps());
		}
	}

	public static String getMemoryUsage() {

		// Retrieve memory managed bean from management factory.
		MemoryMXBean memBean = ManagementFactory.getMemoryMXBean() ;
		MemoryUsage heap = memBean.getHeapMemoryUsage();
		MemoryUsage nonHeap = memBean.getNonHeapMemoryUsage();

		StringBuilder sb = new StringBuilder();

		sb.append('\n').append("Committed: ").append(humanReadableByteCount(heap.getCommitted() + nonHeap.getCommitted(), false));
		sb.append('\n').append("Used     : ").append(humanReadableByteCount(heap.getUsed() + nonHeap.getUsed(), false));
		sb.append('\n').append("Max      : ").append(humanReadableByteCount(heap.getMax() +  nonHeap.getMax(), false));
		return sb.toString();
	}

	/**
	 * copied from http://stackoverflow.com/questions/3758606/how-to-convert-byte-size-into-human-readable-format-in-java
	 * @param si if true uses 1000 as unit (binary), else uses 1024.
	 */
	public static String humanReadableByteCount(long bytes, boolean si) {

		int unit = si ? 1000 : 1024;
		if (bytes < unit) return bytes + " B";
		int exp = (int) (Math.log(bytes) / Math.log(unit));
		String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
		return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}

	public static String getSystemEnv() {

		ArrayList<String> sysKeys = new ArrayList<String>(); 
		for (final Object k : System.getenv().keySet()) {
			sysKeys.add(k.toString());
		}
		Collections.sort(sysKeys);
		StringBuilder sb = new StringBuilder();
		for (String k : sysKeys) {
			sb.append(lf).append(k).append(": ").append(System.getenv(k));
		}
		return sb.toString();
	}

	public static String getSystemProps() {

		ArrayList<String> sysKeys = new ArrayList<String>(); 
		for (Object k : System.getProperties().keySet()) {
			sysKeys.add(k.toString());
		}
		Collections.sort(sysKeys);
		StringBuilder sb = new StringBuilder();
		for (String k : sysKeys) {
			sb.append(lf).append(getPropKey(k))
			.append("=")
			.append(getPropValue(System.getProperty(k)));
		}
		return sb.toString();
	}

	/** Converts a key-name to a property key-name */ 
	public static String getPropKey(String key) {
		return convertToProp(key, true, true);
	}
	/** Converts a value to a property value */ 
	public static String getPropValue(String key) {
		return convertToProp(key, false, true);
	}
	/**
	 * Converts key/prop-value strings to Properties-format.
	 * <br>Copied from java.util.Properties.java. 
	 * Converts unicodes to encoded &#92;uxxxx and escapes
	 * special characters with a preceding slash
	 * @param theString A key or prop-value to convert.
	 * @param escapeSpace Should be true for a key, false for a property value.
	 * @param escapeUnicode Should be true by default.
	 */
	public static String convertToProp(String theString, boolean escapeSpace, boolean escapeUnicode) {

		int len = theString.length();
		int bufLen = len * 2;
		if (bufLen < 0) {
			bufLen = Integer.MAX_VALUE;
		}
		StringBuilder outBuffer = new StringBuilder(bufLen);

		for(int x=0; x<len; x++) {
			char aChar = theString.charAt(x);
			// Handle common case first, selecting largest block that
			// avoids the specials below
			if ((aChar > 61) && (aChar < 127)) {
				if (aChar == '\\') {
					outBuffer.append('\\'); outBuffer.append('\\');
					continue;
				}
				outBuffer.append(aChar);
				continue;
			}
			switch(aChar) {
			case ' ':
				if (x == 0 || escapeSpace)
					outBuffer.append('\\');
				outBuffer.append(' ');
				break;
			case '\t':outBuffer.append('\\'); outBuffer.append('t');
			break;
			case '\n':outBuffer.append('\\'); outBuffer.append('n');
			break;
			case '\r':outBuffer.append('\\'); outBuffer.append('r');
			break;
			case '\f':outBuffer.append('\\'); outBuffer.append('f');
			break;
			case '=': // Fall through
			case ':': // Fall through
			case '#': // Fall through
			case '!':
				outBuffer.append('\\'); outBuffer.append(aChar);
				break;
			default:
				if (escapeUnicode && ((aChar < 0x0020) || (aChar > 0x007e))) {
					outBuffer.append('\\');
					outBuffer.append('u');
					outBuffer.append(toHex((aChar >> 12) & 0xF));
					outBuffer.append(toHex((aChar >>  8) & 0xF));
					outBuffer.append(toHex((aChar >>  4) & 0xF));
					outBuffer.append(toHex( aChar        & 0xF));
				} else {
					outBuffer.append(aChar);
				}
			}
		}
		return outBuffer.toString();
	}

	/**
	 * Convert a nibble to a hex character
	 * <br>Copied from java.util.Properties.java. 
	 * @param   nibble  the nibble to convert.
	 */
	public static char toHex(int nibble) {
		return hexDigit[(nibble & 0xF)];
	}

	/** A table of hex digits 
	 * <br>Copied from java.util.Properties.java. 
	 * */
	public static final char[] hexDigit = {
		'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'
	};

}
