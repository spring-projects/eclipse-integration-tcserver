/*******************************************************************************
 * Copyright (c) 2012, 2018 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
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

		File instanceDir = TcServerUtil.getTcRuntime(server.getRuntime()).instanceDirectory(TcServer21ServerHandlerCallback.DEFAULT_INSTANCE).toFile();
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

		// /*
		// * Start server
		// */
		// OperationListener listener = new OperationListener();
		// server.start(ILaunchManager.RUN_MODE, listener);
		// waitForOperation(listener);
		// assertEquals(IServer.STATE_STARTED, server.getModuleState(modules));
		//
		// /*
		// * Stop module
		// */
		// listener = new OperationListener();
		// server.stopModule(modules, listener);
		// waitForOperation(listener);
		// assertEquals(IServer.STATE_STOPPED, server.getModuleState(modules));
		//
		// /*
		// * Start module
		// */
		// listener = new OperationListener();
		// server.startModule(modules, listener);
		// waitForOperation(listener);
		// assertEquals(IServer.STATE_STARTED, server.getModuleState(modules));
		//
		// /*
		// * Stop module for restarting stopped module
		// */
		// listener = new OperationListener();
		// server.stopModule(modules, listener);
		// waitForOperation(listener);
		// assertEquals(IServer.STATE_STOPPED, server.getModuleState(modules));
		//
		// /*
		// * Restart the stopped module
		// */
		// listener = new OperationListener();
		// server.restartModule(modules, listener);
		// waitForOperation(listener);
		// assertEquals(IServer.STATE_STARTED, server.getModuleState(modules));
		//
		// /*
		// * Restart the started module
		// */
		// listener = new OperationListener();
		// server.restartModule(modules, listener);
		// waitForOperation(listener);
		// assertEquals(IServer.STATE_STARTED, server.getModuleState(modules));
		//
		// /*
		// * Stop the server
		// */
		// listener = new OperationListener();
		// server.stop(true, listener);
		// waitForOperation(listener);
		// assertEquals(IServer.STATE_STOPPED, server.getModuleState(modules));
	}

	// private void waitForOperation(OperationListener listener) throws
	// Exception {
	// while (!listener.isDone()) {
	// while (PlatformUI.getWorkbench().getDisplay().readAndDispatch()) {
	// // nothing
	// }
	// Thread.sleep(100);
	// }
	// /*
	// * Sleep half a second for the operation to finish and assign states to
	// * modules accordingly. Flaky, but didn't figure anything better yet.
	// */
	// Thread.sleep(500);
	// }

	private List<File> getDeployedDirectories(File deploymentDir) {
		List<File> list = Arrays.asList(deploymentDir.listFiles());
		Collections.sort(list, new Comparator<File>() {
			public int compare(File o1, File o2) {
				return o1.getName().compareToIgnoreCase(o2.getName());
			}
		});
		return list;
	}

	// private class OperationListener implements IOperationListener {
	//
	// private final AtomicBoolean isDone = new AtomicBoolean(false);
	//
	// public void done(IStatus result) {
	// isDone.compareAndSet(false, true);
	// }
	//
	// public boolean isDone() {
	// return isDone.get();
	// }
	//
	// }

}
