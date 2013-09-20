/*******************************************************************************
 * Copyright (c) 2012 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package com.vmware.vfabric.ide.eclipse.tcserver.insight.internal.ui;

import java.io.File;
import java.util.Collections;

import junit.framework.TestCase;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;

import com.vmware.vfabric.ide.eclipse.tcserver.insight.internal.ui.InsightPlugin;
import com.vmware.vfabric.ide.eclipse.tcserver.insight.internal.ui.InsightPluginModel;


/**
 * @author Steffen Pingel
 */
public class InsightPluginModelTest extends TestCase {

	InsightPluginModel model = new InsightPluginModel();

	private File temp;

	private IPath path;

	@Override
	protected void setUp() throws Exception {
		temp = StsTestUtil.createTempDirectory();
		path = Path.fromOSString(temp.getAbsolutePath());

		assertEquals(0, model.getPlugins().size());
	}

	public void testLoadEnabled() throws Exception {
		File file = new File(temp, "insight-plugin-123.jar");
		file.createNewFile();
		model.load(path);
		assertEquals(1, model.getPlugins().size());
		InsightPlugin plugin = model.getPlugins().get(0);
		assertEquals(file, plugin.getFile());
		assertEquals("123", plugin.getName());
		assertTrue(plugin.isEnabled());
	}

	public void testLoadDisabled() throws Exception {
		File file = new File(temp, "insight-plugin-123.jar.disabled");
		file.createNewFile();
		model.load(path);
		assertEquals(1, model.getPlugins().size());
		InsightPlugin plugin = model.getPlugins().get(0);
		assertEquals(file, plugin.getFile());
		assertEquals("123", plugin.getName());
		assertFalse(plugin.isEnabled());
	}

	public void testLoadMultiple() throws Exception {
		File fileA = new File(temp, "insight-plugin-a.jar.disabled");
		fileA.createNewFile();
		File fileB = new File(temp, "insight-plugin-b.jar");
		fileB.createNewFile();
		File fileC = new File(temp, "insight-plugin-c.jar");
		fileC.createNewFile();
		model.load(path);
		// ensure predictable order
		Collections.sort(model.getPlugins());
		assertEquals(3, model.getPlugins().size());
		InsightPlugin plugin = model.getPlugins().get(0);
		assertEquals(fileA, plugin.getFile());
		assertFalse(plugin.isEnabled());
		plugin = model.getPlugins().get(1);
		assertEquals(fileB, plugin.getFile());
		assertTrue(plugin.isEnabled());
		plugin = model.getPlugins().get(2);
		assertEquals(fileC, plugin.getFile());
		assertTrue(plugin.isEnabled());
	}

	public void testCommit() throws Exception {
		File fileA = new File(temp, "insight-plugin-a.jar.disabled");
		fileA.createNewFile();
		File fileB = new File(temp, "insight-plugin-b.jar");
		fileB.createNewFile();
		model.load(path);
		// ensure predictable order
		Collections.sort(model.getPlugins());
		model.getPlugins().get(0).setEnabled(true);
		model.getPlugins().get(1).setEnabled(false);
		assertTrue(fileA.exists());
		assertTrue(fileB.exists());

		model.commit();
		assertFalse(fileA.exists());
		assertFalse(fileB.exists());

		File fileAEnabled = new File(temp, "insight-plugin-a.jar");
		assertTrue(fileAEnabled.exists());
		File fileBDisabled = new File(temp, "insight-plugin-b.jar.disabled");
		assertTrue(fileBDisabled.exists());
	}

	public void testCommitToggle() throws Exception {
		File fileA = new File(temp, "insight-plugin-a.jar.disabled");
		fileA.createNewFile();
		File fileAEnabled = new File(temp, "insight-plugin-a.jar");

		model.load(path);

		model.getPlugins().get(0).setEnabled(true);
		model.commit();
		assertFalse(fileA.exists());
		assertTrue(fileAEnabled.exists());

		model.getPlugins().get(0).setEnabled(false);
		model.commit();
		assertTrue(fileA.exists());
		assertFalse(fileAEnabled.exists());
	}

}
