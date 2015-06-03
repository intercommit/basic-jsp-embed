package com.descartes.appboot;

import static org.junit.Assert.*;
import static com.descartes.appboot.BootKeys.*;

import org.junit.Test;

import com.descartes.appboot.BootKeys;
import com.descartes.appboot.BootUtil;

public class TestUtil {

	@Test
	public void testVersion() {
		
		String v = BootUtil.getPomVersion(org.fest.assertions.Assert.class);
		BootUtil.showln(v);
		assertFalse(v.equals(BootUtil.UNKNOWN_VERSION));
	}
	
	@Test
	public void testProps() {
		
		System.setProperty(APP_BOOT_DEBUG, "");
		boolean b = BootUtil.getPropBool(APP_BOOT_DEBUG, (String[])null);
		System.getProperties().remove(APP_BOOT_DEBUG);
		assertTrue(b);
		String[] args = new String[1];
		args[0] = APP_BOOT_DEBUG + "=false";
		b = BootUtil.getPropBool(APP_BOOT_DEBUG, args);
		assertFalse(b);
		
		System.setProperty(APP_NAME, "appboot");
		String s = BootUtil.getProp(APP_NAME, (String[])null);
		System.getProperties().remove(APP_NAME);
		assertEquals("appboot", s);
		args[0] = APP_NAME + "=appboot";
		s = BootUtil.getProp(APP_NAME, args);
		assertEquals("appboot", s);
		args[0] = APP_NAME + "=\"appboot\"";
		s = BootUtil.getProp(APP_NAME, args);
		assertEquals("appboot", s);
		
		assertEquals(0, BootKeys.filterAppBootArgs(args).length);
	}
}
