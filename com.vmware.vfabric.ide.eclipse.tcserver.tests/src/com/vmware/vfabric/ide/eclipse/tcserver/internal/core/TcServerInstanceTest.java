/*******************************************************************************
 * Copyright (c) 2012 - 2013 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package com.vmware.vfabric.ide.eclipse.tcserver.internal.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestName;
import org.springsource.ide.eclipse.commons.configurator.ServerHandler;

import com.vmware.vfabric.ide.eclipse.tcserver.tests.support.TcServerFixture;

/**
 * @author Tomasz Zarna
 */
public class TcServerInstanceTest {
	@Rule
	public TestName name = new TestName();

	@Rule
	public TemporaryFolder instanceDir = new TemporaryFolder();

	private ServerHandler handler;

	@After
	public void tearDown() throws Exception {
		if (handler != null) {
			handler.deleteServerAndRuntime(new NullProgressMonitor());
		}
	}

	@Test
	public void testInstanceDir() throws Exception {
		handler = TcServerFixture.V_2_9.provisionServer();
		IPath runtimeLocation = new Path(handler.getServerPath());
		String instanceName = name.getMethodName();
		String[] arguments = createArgumentsArray();

		TcServerUtil.executeInstanceCreation(runtimeLocation, instanceName, arguments);
	}

	private String[] createArgumentsArray(/* IRuntime runtime */) {
		List<String> arguments = new ArrayList<String>();
		arguments.add("create");
		arguments.add(name.getMethodName());
		arguments.add("-t");
		arguments.add("base");
		arguments.add("-i");
		arguments.add(instanceDir.getRoot().toString());
		return arguments.toArray(new String[arguments.size()]);
	}
}
