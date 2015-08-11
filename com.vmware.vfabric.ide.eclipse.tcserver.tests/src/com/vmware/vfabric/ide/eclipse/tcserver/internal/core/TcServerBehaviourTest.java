/*******************************************************************************
 * Copyright (c) 2012 - 2013 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package com.vmware.vfabric.ide.eclipse.tcserver.internal.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.internal.Server;
import org.eclipse.wst.server.core.internal.ServerType;
import org.junit.After;
import org.junit.Test;

import com.vmware.vfabric.ide.eclipse.tcserver.tests.support.TcServerFixture;

/**
 * @author Steffen Pingel
 * @author Tomasz Zarna
 * @author Leo Dos Santos
 */
public class TcServerBehaviourTest {

	private IServer server;

	@Test
	// @Ignore("Ignoring tcServer-6.0 tests.")
	public void testRuntimeVMArgumentsAsf60() throws Exception {
		runtimeVMArguments(TcServerFixture.V_6_0, null, "tomcat-6.0.33.A.RELEASE", "tomcat-6.0.33.A.RELEASE");
	}

	@Test
	// @Ignore("Layout.COMBINED javadoc says it's supported by v2.5 and later
	// only.")
	public void testRuntimeVMArgumentsCombined21() throws Exception {
		runtimeVMArguments(TcServerFixture.V_6_0, TcServerFixture.INST_COMBINED, "tomcat-6.0.33.A.RELEASE",
				TcServerFixture.INST_COMBINED);
	}

	@Test
	public void testRuntimeVMArgumentsSeparate60() throws Exception {
		runtimeVMArguments(TcServerFixture.V_6_0, TcServerFixture.INST_INSIGHT, "tomcat-6.0.33.A.RELEASE",
				TcServerFixture.INST_INSIGHT);
	}

	@Test
	public void testSetupLaunchConfigurationDefaultArgs() throws Exception {
		server = TcServerFixture.current().createServer(null);
		server.publish(Server.PUBLISH_FULL, null);

		ILaunchConfigurationWorkingCopy wc = createLaunchConfiguration();
		((Server) server).setupLaunchConfiguration(wc, null);
		String args = wc.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, (String) null);
		assertTrue("Expected -Xmx768m -Xss256k in '" + args + "'", args.contains("-Xmx768m -Xss256k"));

		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, args.replace("-Xmx768m", "-Xmx123m"));
		((Server) server).setupLaunchConfiguration(wc, null);
		args = wc.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, (String) null);
		assertTrue("Expected -Xmx123m -Xss256k in '" + args + "'", args.contains("-Xmx123m -Xss256k"));

		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, args.replace("-Xss256k", ""));
		((Server) server).setupLaunchConfiguration(wc, null);
		args = wc.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, (String) null);
		assertTrue("Expected -Xmx123m in '" + args + "'", args.contains("-Xmx123m"));
		assertTrue("Expected -Xss256k in '" + args + "'", args.contains("-Xss256k"));
		assertTrue(
				"Expected -Djava.util.logging.manager=com.springsource.tcserver.serviceability.logging.TcServerLogManager in '"
						+ args + "'",
				args.contains(
						"-Djava.util.logging.manager=com.springsource.tcserver.serviceability.logging.TcServerLogManager"));
		TcServer tcServer = server.getAdapter(TcServer.class);
		String logConfFileArg = "-Djava.util.logging.config.file=\""
				+ tcServer.getInstanceBase(server.getRuntime()).append("conf").append("logging.properties") + "\"";
		assertTrue("Expected " + logConfFileArg + " in '" + args + "'", args.contains(logConfFileArg));
	}

	@Test
	// @Ignore("Ignoring tcServer-6.0 tests.")
	public void testTomcatLocationAsfLayout60() throws Exception {
		server = TcServerFixture.V_6_0.createServer(null);
		server.publish(Server.PUBLISH_FULL, null);

		ILaunchConfigurationWorkingCopy wc = createLaunchConfiguration();
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, "-Xmx123m");
		((Server) server).setupLaunchConfiguration(wc, null);
		String args = wc.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, (String) null);
		assertTrue("Expected -Xmx123m in '" + args + "'", args.contains("-Xmx123m"));
		assertTrue("Expected -Xss256k in '" + args + "'", args.contains("-Xss256k"));
	}

	private ILaunchConfigurationWorkingCopy createLaunchConfiguration() throws CoreException {
		ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfigurationType launchConfigType = ((ServerType) server.getServerType()).getLaunchConfigurationType();

		String launchName = launchManager.generateLaunchConfigurationName("Tc Server Launch");
		ILaunchConfigurationWorkingCopy wc = launchConfigType.newInstance(null, launchName);
		return wc;
	}

	private List<String> expectedArgs(String runtime, String instance) {
		instance = instance == null ? TcServer21ServerHandlerCallback.DEFAULT_INSTANCE : instance;
		List<String> args = new ArrayList<String>();
		IPath location = server.getRuntime().getLocation();
		args.add("-Dcatalina.base=\"" + location.append(instance).toOSString() + "\"");
		args.add("-Dcatalina.home=\"" + location.append(runtime).toOSString() + "\"");
		args.add("-Dwtp.deploy=\"" + location.append(instance).append("wtpwebapps").toOSString() + "\"");
		args.add("-Djava.endorsed.dirs=\"" + location.append(runtime).append("endorsed").toOSString() + "\"");
		return args;
	}

	private void runtimeVMArguments(TcServerFixture fixture, String instanceToCreate, String runtime, String instance)
			throws Exception {
		server = fixture.createServer(instanceToCreate);
		TcServerBehaviour behaviour = (TcServerBehaviour) server.loadAdapter(TcServerBehaviour.class, null);
		assertEquals(expectedArgs(runtime, instance), Arrays.asList(behaviour.getRuntimeVMArguments()));
	}

	@After
	public void tearDown() throws Exception {
		if (server != null) {
			TcServerFixture.deleteServerAndRuntime(server);
		}
	}

}
