/*******************************************************************************
 * Copyright (c) 2012 - 2013 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package com.vmware.vfabric.ide.eclipse.tcserver.internal.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IServer;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import org.springsource.ide.eclipse.commons.configurator.ServerHandler;

import com.vmware.vfabric.ide.eclipse.tcserver.internal.core.TcServer.Layout;
import com.vmware.vfabric.ide.eclipse.tcserver.tests.support.TcServerFixture;

/**
 * @author Steffen Pingel
 * @author Tomasz Zarna
 */
public class TcServerRuntimeTest {

	private ServerHandler handler;

	private IServer server;

	@After
	public void tearDown() throws Exception {
		if (handler != null) {
			handler.deleteServerAndRuntime(new NullProgressMonitor());
		}
		if (server != null) {
			TcServerFixture.deleteServerAndRuntime(server);
		}
	}

	@Test
	@Ignore("Ignoring tcServer-6.0 tests")
	public void testTomcatLocationAsfLayout60() throws Exception {
		server = TcServerFixture.V_6_0.createServer(null);
		TcServerRuntime runtime = (TcServerRuntime) server.getRuntime().loadAdapter(TcServerRuntime.class, null);
		TcServer tcServer = (TcServer) server.loadAdapter(TcServer.class, null);
		assertEquals(Layout.ASF, tcServer.getLayout());
		System.err.println(runtime.getRuntimeClasspath());
	}

	@Test
	public void testTomcatLocationSeparateLayout20() throws Exception {
		server = TcServerFixture.V_2_0.createServer(TcServerFixture.INST_INSIGHT);
		TcServerRuntime runtime = (TcServerRuntime) server.getRuntime().loadAdapter(TcServerRuntime.class, null);
		TcServer tcServer = (TcServer) server.loadAdapter(TcServer.class, null);
		assertEquals(Layout.SEPARATE, tcServer.getLayout());
		System.err.println(runtime.getRuntimeClasspath());
	}

	@Test
	public void testTomcatLocationSeparateLayout21() throws Exception {
		server = TcServerFixture.V_2_1.createServer(TcServerFixture.INST_SEPARATE);
		TcServerRuntime runtime = (TcServerRuntime) server.getRuntime().loadAdapter(TcServerRuntime.class, null);
		TcServer tcServer = (TcServer) server.loadAdapter(TcServer.class, null);
		assertEquals(Layout.SEPARATE, tcServer.getLayout());
		System.err.println(runtime.getRuntimeClasspath());
	}

	@Test
	@Ignore("Layout.COMBINED javadoc says it's supported by v2.5 and later only.")
	public void testTomcatLocationCombinedLayout21() throws Exception {
		server = TcServerFixture.V_2_1.createServer(TcServerFixture.INST_COMBINED);
		TcServerRuntime runtime = (TcServerRuntime) server.getRuntime().loadAdapter(TcServerRuntime.class, null);
		TcServer tcServer = (TcServer) server.loadAdapter(TcServer.class, null);
		assertEquals(Layout.COMBINED, tcServer.getLayout());
		System.err.println(runtime.getRuntimeClasspath());
	}

	@Test
	public void testValidate20() throws Exception {
		handler = TcServerFixture.V_2_0.provisionServer();
		IServer server = handler.createServer(new NullProgressMonitor(), ServerHandler.ALWAYS_OVERWRITE);
		assertEquals(Status.OK_STATUS, server.getRuntime().validate(null));
	}

	@Test
	public void testValidate20With21() throws Exception {
		handler = TcServerFixture.V_2_0.getHandler(TcServerFixture.V_2_1.createHarness().provisionServer()
				.getServerPath());
		try {
			handler.createServer(new NullProgressMonitor(), ServerHandler.ALWAYS_OVERWRITE);
			fail("Expected CoreException");
		}
		catch (CoreException e) {
			assertEquals("Expected error, got: " + e.getStatus(), IStatus.ERROR, e.getStatus().getSeverity());
		}
	}

	@Test
	public void testValidate21() throws Exception {
		handler = TcServerFixture.V_2_1.provisionServer();
		IServer server = handler.createServer(new NullProgressMonitor(), ServerHandler.ALWAYS_OVERWRITE);
		assertEquals(Status.OK_STATUS, server.getRuntime().validate(null));
	}

	@Test
	public void testValidate21With20() throws Exception {
		handler = TcServerFixture.V_2_1.getHandler(TcServerFixture.V_2_0.createHarness().provisionServer()
				.getServerPath());
		try {
			handler.createServer(new NullProgressMonitor(), ServerHandler.ALWAYS_OVERWRITE);
			fail("Expected CoreException");
		}
		catch (CoreException e) {
			assertEquals("Expected error, got: " + e.getStatus(), IStatus.ERROR, e.getStatus().getSeverity());
		}
	}

	@Test
	public void testValidateCurrent() throws Exception {
		handler = TcServerFixture.current().provisionServer();
		IServer server = handler.createServer(new NullProgressMonitor(), ServerHandler.ALWAYS_OVERWRITE);
		assertEquals(Status.OK_STATUS, server.getRuntime().validate(null));
	}

}
