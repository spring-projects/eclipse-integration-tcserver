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
package com.vmware.vfabric.ide.eclipse.tcserver.tests;

import junit.framework.TestCase;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.wst.server.core.IServer;

import com.vmware.vfabric.ide.eclipse.tcserver.tests.support.TcServerFixture;
import com.vmware.vfabric.ide.eclipse.tcserver.tests.support.TcServerHarness;

/**
 * @author Steffen Pingel
 */
public class TcServerProvisioningTest extends TestCase {

	private TcServerHarness harness;

	@Override
	protected void setUp() throws Exception {
		harness = TcServerFixture.current().createHarness();
	}

	@Override
	protected void tearDown() throws Exception {
		harness.dispose();
	}

	public void testCreateServer() throws Exception {
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
