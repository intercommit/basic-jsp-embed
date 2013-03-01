package com.descartes.appboot;

import static org.junit.Assert.*;

import org.junit.Test;

import com.descartes.appboot.AppBoot;

public class TestAppBoot {

	@Test
	public void testMain() {
		
		try {
			AppBoot.main("");
			// should just show some info
		} catch (Exception e) {
			fail();
		}
	}
}
