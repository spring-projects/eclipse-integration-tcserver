/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package com.vmware.vfabric.ide.eclipse.tcserver.internal.core;

import junit.framework.TestCase;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IServer;
import org.springsource.ide.eclipse.commons.configurator.ServerHandler;

import com.vmware.vfabric.ide.eclipse.tcserver.internal.core.TcServer;
import com.vmware.vfabric.ide.eclipse.tcserver.internal.core.TcServerRuntime;
import com.vmware.vfabric.ide.eclipse.tcserver.internal.core.TcServer.Layout;
import com.vmware.vfabric.ide.eclipse.tcserver.tests.support.TcServerFixture;

/**
 * @author Steffen Pingel
 */
public class TcServerRuntimeTest extends TestCase {

	private ServerHandler handler;

	private IServer server;

	@Override
	protected void tearDown() throws Exception {
		if (handler != null) {
			handler.deleteServerAndRuntime(new NullProgressMonitor());
		}
		if (server != null) {
			TcServerFixture.deleteServerAndRuntime(server);
		}
	}

	public void testTomcatLocationAsfLayout60() throws Exception {
		server = TcServerFixture.V_6_0.createServer(null);
		TcServerRuntime runtime = (TcServerRuntime) server.getRuntime().loadAdapter(TcServerRuntime.class, null);
		TcServer tcServer = (TcServer) server.loadAdapter(TcServer.class, null);
		assertEquals(Layout.ASF, tcServer.getLayout());
		System.err.println(runtime.getRuntimeClasspath());
	}

	public void testTomcatLocationSeparateLayout20() throws Exception {
		server = TcServerFixture.V_2_0.createServer(TcServerFixture.INST_INSIGHT);
		TcServerRuntime runtime = (TcServerRuntime) server.getRuntime().loadAdapter(TcServerRuntime.class, null);
		TcServer tcServer = (TcServer) server.loadAdapter(TcServer.class, null);
		assertEquals(Layout.SEPARATE, tcServer.getLayout());
		System.err.println(runtime.getRuntimeClasspath());
	}

	public void testTomcatLocationSeparateLayout21() throws Exception {
		server = TcServerFixture.V_2_1.createServer(TcServerFixture.INST_SEPARATE);
		TcServerRuntime runtime = (TcServerRuntime) server.getRuntime().loadAdapter(TcServerRuntime.class, null);
		TcServer tcServer = (TcServer) server.loadAdapter(TcServer.class, null);
		assertEquals(Layout.SEPARATE, tcServer.getLayout());
		System.err.println(runtime.getRuntimeClasspath());
	}

	public void testTomcatLocationCombinedLayout21() throws Exception {
		server = TcServerFixture.V_2_1.createServer(TcServerFixture.INST_COMBINED);
		TcServerRuntime runtime = (TcServerRuntime) server.getRuntime().loadAdapter(TcServerRuntime.class, null);
		TcServer tcServer = (TcServer) server.loadAdapter(TcServer.class, null);
		assertEquals(Layout.COMBINED, tcServer.getLayout());
		System.err.println(runtime.getRuntimeClasspath());
	}

	public void testValidate20() throws Exception {
		handler = TcServerFixture.V_2_0.getHandler(TcServerFixture.V_2_0.getStubLocation().getAbsolutePath());
		IServer server = handler.createServer(new NullProgressMonitor(), ServerHandler.ALWAYS_OVERWRITE);
		assertEquals(Status.OK_STATUS, server.getRuntime().validate(null));
	}

	public void testValidate20With21() throws Exception {
		handler = TcServerFixture.V_2_0.getHandler(TcServerFixture.V_2_1.getStubLocation().getAbsolutePath());
		try {
			handler.createServer(new NullProgressMonitor(), ServerHandler.ALWAYS_OVERWRITE);
			fail("Expected CoreException");
		}
		catch (CoreException e) {
			assertEquals("Expected error, got: " + e.getStatus(), IStatus.ERROR, e.getStatus().getSeverity());
		}
	}

	public void testValidate21() throws Exception {
		handler = TcServerFixture.V_2_1.getHandler(TcServerFixture.V_2_1.getStubLocation().getAbsolutePath());
		IServer server = handler.createServer(new NullProgressMonitor(), ServerHandler.ALWAYS_OVERWRITE);
		assertEquals(Status.OK_STATUS, server.getRuntime().validate(null));
	}

	public void testValidate21With20() throws Exception {
		handler = TcServerFixture.V_2_1.getHandler(TcServerFixture.V_2_0.getStubLocation().getAbsolutePath());
		try {
			handler.createServer(new NullProgressMonitor(), ServerHandler.ALWAYS_OVERWRITE);
			fail("Expected CoreException");
		}
		catch (CoreException e) {
			assertEquals("Expected error, got: " + e.getStatus(), IStatus.ERROR, e.getStatus().getSeverity());
		}
	}

}
