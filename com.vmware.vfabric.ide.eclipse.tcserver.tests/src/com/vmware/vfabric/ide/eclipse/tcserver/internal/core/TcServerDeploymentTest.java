/*******************************************************************************
 *  Copyright (c) 2012 - 2013 GoPivotal, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package com.vmware.vfabric.ide.eclipse.tcserver.internal.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerUtil;
import org.junit.After;
import org.junit.Test;
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;

import com.vmware.vfabric.ide.eclipse.tcserver.tests.support.TcServerFixture;
import com.vmware.vfabric.ide.eclipse.tcserver.tests.support.TcServerTestPlugin;

/**
 * @author Steffen Pingel
 * @author Tomasz Zarna
 * @author Leo Dos Santos
 */
public class TcServerDeploymentTest {

	private IServer server;

	@After
	public void tearDown() throws Exception {
		StsTestUtil.cleanUpProjects();
		if (server != null) {
			TcServerFixture.deleteServerAndRuntime(server);
		}
	}

	@Test
	public void testDeployServlet30() throws Exception {
		if (!StsTestUtil.ECLIPSE_3_6_OR_LATER) {
			// skip test, the Servlet 3.0 spec is not supported by Eclipse 3.5
			// and earlier
			return;
		}

		server = TcServerFixture.current().createServer(null);
		assertNotNull("Expected server configuration", server.getServerConfiguration());
		server.publish(IServer.PUBLISH_FULL, null);

		File baseDir = server.getRuntime().getLocation().toFile();
		File instanceDir = new File(baseDir, TcServer21ServerHandlerCallback.DEFAULT_INSTANCE);
		File deploymentDir = new File(instanceDir, "wtpwebapps");
		assertTrue(deploymentDir.exists());
		File rootApp = new File(deploymentDir, "ROOT");
		assertEquals(Arrays.asList(rootApp), getDeployedDirectories(deploymentDir));

		StsTestUtil.createPredefinedProject("servlet-3.0-project", TcServerTestPlugin.PLUGIN_ID);
		StsTestUtil.createPredefinedProject("servlet-3.0-fragment", TcServerTestPlugin.PLUGIN_ID);

		IModule[] modules = ServerUtil.getModules("jst.web");
		IServerWorkingCopy wc = server.createWorkingCopy();
		wc.modifyModules(modules, new IModule[0], null);
		wc.save(true, null);

		server.publish(IServer.PUBLISH_FULL, null);
		assertEquals(Arrays.asList(rootApp, new File(deploymentDir, "servlet-3.0-project")),
				getDeployedDirectories(deploymentDir));
		File file = new File(deploymentDir, "servlet-3.0-project/WEB-INF/lib/servlet-3.0-fragment.jar");
		assertTrue("Expected fragement at " + file, file.exists());
	}

	private List<File> getDeployedDirectories(File deploymentDir) {
		List<File> list = Arrays.asList(deploymentDir.listFiles());
		Collections.sort(list, new Comparator<File>() {
			public int compare(File o1, File o2) {
				return o1.getName().compareToIgnoreCase(o2.getName());
			}
		});
		return list;
	}

}
