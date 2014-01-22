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
package com.vmware.vfabric.ide.eclipse.tcserver.tests;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.wst.server.core.IServer;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.vmware.vfabric.ide.eclipse.tcserver.tests.support.TcServerFixture;
import com.vmware.vfabric.ide.eclipse.tcserver.tests.support.TcServerHarness;

/**
 * @author Steffen Pingel
 * @author Tomasz Zarna
 */
@RunWith(Parameterized.class)
public class TcServerProvisioningTest {

	private final TcServerFixture fixture;

	public TcServerProvisioningTest(TcServerFixture fixture) {
		this.fixture = fixture;
	}

	@Parameters
	public static Collection<Object[]> data() {
		Object[][] data = new Object[TcServerFixture.ALL.length][1];
		for (int i = 0; i < TcServerFixture.ALL.length; i++) {
			data[i][0] = TcServerFixture.ALL[i];
		}
		return Arrays.asList(data);
	}

	private TcServerHarness harness;

	@Before
	public void setUp() throws Exception {
		harness = fixture.createHarness();
	}

	@After
	public void tearDown() throws Exception {
		harness.dispose();
	}

	@Test
	public void testCreateServer() throws Exception {
		Assume.assumeNotNull(fixture.getDownloadUrl());
		try {
			IServer server = harness.createServer(null);
			assertNotNull(server);
		}
		catch (CoreException e) {
			// on Java 1.5 provisioning is expected to fail with tc Server v2.5
			// or later
			if (e.getMessage()
					.equals("tc Server v2.5 or later requires Java SE 6 or later.  Change the JRE to one that meets this requirement.")) {
				assertTrue("Expected Java version < 1.6, got: " + System.getProperty("java.version"), System
						.getProperty("java.version").compareTo("1.6") < 0);
			}
			else {
				throw e;
			}
		}
	}

}
