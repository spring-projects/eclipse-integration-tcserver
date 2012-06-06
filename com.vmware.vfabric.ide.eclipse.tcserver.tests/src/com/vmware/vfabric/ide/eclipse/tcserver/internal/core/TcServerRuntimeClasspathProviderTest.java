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

import java.util.Arrays;

import junit.framework.TestCase;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.wst.server.core.IServer;

import com.vmware.vfabric.ide.eclipse.tcserver.internal.core.TcServerRuntimeClasspathProvider;
import com.vmware.vfabric.ide.eclipse.tcserver.tests.support.TcServerFixture;

/**
 * @author Steffen Pingel
 */
public class TcServerRuntimeClasspathProviderTest extends TestCase {

	private IServer server;

	private TcServerRuntimeClasspathProvider classPathProvider;

	@Override
	protected void setUp() throws Exception {
		classPathProvider = new TcServerRuntimeClasspathProvider();
	}

	@Override
	protected void tearDown() throws Exception {
		if (server != null) {
			TcServerFixture.deleteServerAndRuntime(server);
		}
	}

	public void testResolveClasspathAsf60() throws Exception {
		server = TcServerFixture.V_6_0.createServer(null);
		IClasspathEntry[] cp = classPathProvider.resolveClasspathContainer(null, server.getRuntime());
		assertHasServletApi(server.getRuntime().getLocation(), cp);
	}

	private void assertHasServletApi(IPath path, IClasspathEntry[] cp) {
		boolean hasServlet = false;
		for (IClasspathEntry entry : cp) {
			assertTrue("Expected entry to start with '" + path + "', got '" + entry.getPath() + "'",
					path.isPrefixOf(entry.getPath()));
			if (entry.getPath().toString().endsWith("servlet-api.jar")) {
				hasServlet = true;
			}
		}
		if (!hasServlet) {
			fail("Expected servlet-api.jar in '" + Arrays.asList(cp) + "'");
		}

	}

	public void testResolveClasspathSeparate20() throws Exception {
		server = TcServerFixture.V_2_0.createServer(TcServerFixture.INST_INSIGHT);
		IClasspathEntry[] cp = classPathProvider.resolveClasspathContainer(null, server.getRuntime());
		assertHasServletApi(server.getRuntime().getLocation(), cp);
	}

	public void testResolveClasspathSeparate21() throws Exception {
		server = TcServerFixture.V_2_1.createServer(TcServerFixture.INST_SEPARATE);
		IClasspathEntry[] cp = classPathProvider.resolveClasspathContainer(null, server.getRuntime());
		assertHasServletApi(server.getRuntime().getLocation(), cp);
	}

}
