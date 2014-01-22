/*******************************************************************************
 * Copyright (c) 2012 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package com.vmware.vfabric.ide.eclipse.tcserver.insight.internal.ui;

import java.io.File;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;

import com.vmware.vfabric.ide.eclipse.tcserver.insight.internal.ui.InsightPlugin;

import junit.framework.TestCase;


/**
 * @author Steffen Pingel
 */
public class InsightPluginTest extends TestCase {

	private File temp;

	@Override
	protected void setUp() throws Exception {
		temp = StsTestUtil.createTempDirectory();
	}

	public void testNewEnabled() throws Exception {
		File file = new File(temp, "insight-plugin-123.jar");
		file.createNewFile();
		InsightPlugin plugin = new InsightPlugin(file);
		assertEquals(file, plugin.getFile());
		assertEquals("123", plugin.getName());
		assertEquals(null, plugin.getPublisher());
		assertEquals(null, plugin.getVersion());
		assertEquals("", plugin.getDetails());
		assertTrue(plugin.isEnabled());
	}

	public void testNewDisabled() throws Exception {
		File file = new File(temp, "insight-plugin-123.jar.disabled");
		file.createNewFile();
		InsightPlugin plugin = new InsightPlugin(file);
		assertEquals(file, plugin.getFile());
		assertEquals("123", plugin.getName());
		assertEquals(null, plugin.getPublisher());
		assertEquals(null, plugin.getVersion());
		assertEquals("", plugin.getDetails());
		assertFalse(plugin.isEnabled());
	}

	public void testNewManifest() throws Exception {
		File file = new File(temp, "insight-plugin-123.jar");
		FileOutputStream fileOutputStream = new FileOutputStream(file);
		try {
			ZipOutputStream outputStream = new ZipOutputStream(fileOutputStream);
			try {
				ZipEntry zipEntry = new ZipEntry("META-INF/insight-plugin-test.xml");
				outputStream.putNextEntry(zipEntry);
				outputStream.setMethod(ZipOutputStream.DEFLATED);
				String contents = "<beans xmlns:insight=\"http://www.springframework.org/schema/insight-idk\">\n"
						+ "<insight:plugin name=\"spring-web\" version=\"1.0\" publisher=\"SpringSource\" />";
				outputStream.write(contents.getBytes());
				outputStream.flush();
				outputStream.closeEntry();
			}
			finally {
				outputStream.close();
			}
		}
		finally {
			fileOutputStream.close();
		}

		InsightPlugin plugin = new InsightPlugin(file);
		assertEquals(file, plugin.getFile());
		assertEquals("spring-web", plugin.getName());
		assertEquals("SpringSource", plugin.getPublisher());
		assertEquals("1.0", plugin.getVersion());
		assertEquals("1.0 by SpringSource", plugin.getDetails());
		assertTrue(plugin.isEnabled());
	}

	public void testCommit() throws Exception {
		File file = new File(temp, "insight-plugin-123.jar");
		File fileDisabled = new File(temp, "insight-plugin-123.jar.disabled");
		file.createNewFile();
		InsightPlugin plugin = new InsightPlugin(file);
		assertTrue(plugin.isEnabled());

		boolean commit = plugin.commit();
		assertTrue(commit);
		assertTrue(plugin.isEnabled());
		assertTrue(file.exists());
		assertFalse(fileDisabled.exists());

		plugin.setEnabled(false);
		commit = plugin.commit();
		assertTrue(commit);
		assertFalse(file.exists());
		assertTrue(fileDisabled.exists());
		assertEquals(fileDisabled, plugin.getFile());
	}

	public void testCommitToggle() throws Exception {
		File file = new File(temp, "insight-plugin-123.jar");
		File fileDisabled = new File(temp, "insight-plugin-123.jar.disabled");
		file.createNewFile();
		InsightPlugin plugin = new InsightPlugin(file);
		assertTrue(plugin.isEnabled());

		plugin.setEnabled(false);
		plugin.commit();
		assertFalse(plugin.isEnabled());
		assertEquals(fileDisabled, plugin.getFile());

		plugin.setEnabled(true);
		plugin.commit();
		assertTrue(plugin.isEnabled());
		assertEquals(file, plugin.getFile());

		plugin.setEnabled(false);
		plugin.commit();
		assertFalse(plugin.isEnabled());
		assertEquals(fileDisabled, plugin.getFile());
	}

	public void testCommitNonExistant() throws Exception {
		File file = new File(temp, "insight-plugin-123.jar");
		InsightPlugin plugin = new InsightPlugin(file);
		assertTrue(plugin.isEnabled());

		plugin.setEnabled(false);
		boolean commit = plugin.commit();
		assertFalse(commit);
		assertFalse(plugin.isEnabled());
	}

}
