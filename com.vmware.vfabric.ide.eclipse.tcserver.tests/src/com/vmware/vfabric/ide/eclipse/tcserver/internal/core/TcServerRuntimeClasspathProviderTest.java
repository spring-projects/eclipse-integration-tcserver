/*******************************************************************************
 * Copyright (c) 2012, 2014 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package com.vmware.vfabric.ide.eclipse.tcserver.internal.core;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.wst.server.core.IServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.vmware.vfabric.ide.eclipse.tcserver.tests.support.TcServerFixture;

/**
 * @author Steffen Pingel
 * @author Tomasz Zarna
 */
public class TcServerRuntimeClasspathProviderTest {

	private IServer server;

	private TcServerRuntimeClasspathProvider classPathProvider;

	@Before
	public void setUp() throws Exception {
		classPathProvider = new TcServerRuntimeClasspathProvider();
	}

	@After
	public void tearDown() throws Exception {
		if (server != null) {
			TcServerFixture.deleteServerAndRuntime(server);
		}
	}

	@Test
	// @Ignore("Ignoring tcServer-6.0 tests")
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

	@Test
	public void testResolveClasspathSeparate29() throws Exception {
		server = TcServerFixture.V_2_9.createServer(TcServerFixture.INST_INSIGHT);
		IClasspathEntry[] cp = classPathProvider.resolveClasspathContainer(null, server.getRuntime());
		assertHasServletApi(server.getRuntime().getLocation(), cp);
	}

	@Test
	public void testResolveClasspathSeparate30() throws Exception {
		server = TcServerFixture.V_3_0.createServer(TcServerFixture.INST_SEPARATE);
		IClasspathEntry[] cp = classPathProvider.resolveClasspathContainer(null, server.getRuntime());
		assertHasServletApi(server.getRuntime().getLocation(), cp);
	}

}
