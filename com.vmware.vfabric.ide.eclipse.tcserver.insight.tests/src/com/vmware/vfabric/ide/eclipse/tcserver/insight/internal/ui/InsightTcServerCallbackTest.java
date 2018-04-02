/*******************************************************************************
 * Copyright (c) 2012, 2018 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package com.vmware.vfabric.ide.eclipse.tcserver.insight.internal.ui;

import java.io.File;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.internal.Server;
import org.eclipse.wst.server.core.internal.ServerType;

import com.vmware.vfabric.ide.eclipse.tcserver.internal.core.TcServer;
import com.vmware.vfabric.ide.eclipse.tcserver.tests.support.TcServerFixture;

/**
 * @author Steffen Pingel
 * @author Leo Dos Santos
 */
public class InsightTcServerCallbackTest extends TestCase {

	private IServer server;

	public void testAddInsightBase() {
		IPath path = new Path("/");
		String result;

		result = InsightTcServerCallback.addInsightBase("", path);
		assertEquals(" -Dinsight.base=\"" + File.separatorChar + "\"", result);

		result = InsightTcServerCallback.addInsightBase("arg", path);
		assertEquals("arg -Dinsight.base=\"" + File.separatorChar + "\"", result);
	}

	public void testAddInsightBaseReplace() {
		IPath path = new Path("/");
		String result;

		result = InsightTcServerCallback.addInsightBase("-Dinsight.base=\"old\"", path);
		assertEquals("-Dinsight.base=\"" + File.separatorChar + "\"", result);

		path = new Path("com  plex/path");
		result = InsightTcServerCallback.addInsightBase("ab  -Dinsight.base=\"old\" cd", path);
		assertEquals("ab  -Dinsight.base=\"com  plex" + File.separatorChar + "path\" cd", result);

		path = new Path("/");
		result = InsightTcServerCallback.addInsightBase("-Dinsight.base=\"/space in path/file\"", path);
		assertEquals("-Dinsight.base=\"" + File.separatorChar + "\"", result);
	}

	public void testAddInsightBaseMultipleArguments() {
		IPath path = new Path("/");
		String result;

		result = InsightTcServerCallback.addInsightBase("-DargOne=\"one\"", path);
		assertEquals("-DargOne=\"one\" -Dinsight.base=\"" + File.separatorChar + "\"", result);

		result = InsightTcServerCallback.addInsightBase("-DargOne=\"one\" -Dinsight.base=\"old\" -DargOne=\"two\"",
				path);
		assertEquals("-DargOne=\"one\" -Dinsight.base=\"" + File.separatorChar + "\" -DargOne=\"two\"", result);
	}

	public void testLaunchConfigurationArgs28() throws Exception {
		server = InsightTestFixture.V_2_8.createServer(null);
		server.publish(Server.PUBLISH_FULL, null);

		ILaunchConfigurationWorkingCopy wc = createLaunchConfiguration();
		((Server) server).setupLaunchConfiguration(wc, null);
		String args = wc.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, (String) null);
		assertTrue("Expected -Xmx1024m in '" + args + "'", args.contains("-Xmx1024m"));

		TcServer tcServer = (TcServer) server.loadAdapter(TcServer.class, null);
		String agentPath = TcServerInsightUtil.getAgentJarPath(tcServer);
		assertNull("There should be no path to agent under tc Server 2.8 or earlier", agentPath);
	}

	public void testLaunchConfigurationArgs29() throws Exception {
		launchConfigurationArgsTest(InsightTestFixture.V_2_9);
	}

	public void testLaunchConfigurationArgs30() throws Exception {
		launchConfigurationArgsTest(InsightTestFixture.V_3_0);
	}

	public void testLaunchConfigurationArgs31() throws Exception {
		launchConfigurationArgsTest(InsightTestFixture.V_3_1);
	}
	
	private void launchConfigurationArgsTest(InsightTestFixture testFixture) throws Exception {
		server = testFixture.createServer(null);
		server.publish(Server.PUBLISH_FULL, null);

		ILaunchConfigurationWorkingCopy wc = createLaunchConfiguration();
		((Server) server).setupLaunchConfiguration(wc, null);
		String args = wc.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, (String) null);
		assertTrue("Expected -Xmx1024m in '" + args + "'", args.contains("-Xmx1024m"));

		TcServer tcServer = (TcServer) server.loadAdapter(TcServer.class, null);
		String agentPath = TcServerInsightUtil.getAgentJarPath(tcServer);
		assertNotNull("Expected to find a path to insight-weaver agent", agentPath);

		String agentArgument = "-javaagent:\"" + agentPath + "\"";
		assertTrue("Expected " + agentArgument + " in '" + args + "'", args.contains(agentArgument));
	}
	
	public void testInsightClasspath28() throws Exception {
		server = InsightTestFixture.V_2_8.createServer(null);
		server.publish(Server.PUBLISH_FULL, null);

		ILaunchConfigurationWorkingCopy wc = createLaunchConfiguration();
		((Server) server).setupLaunchConfiguration(wc, null);
		List args = wc.getAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, (List) null);

		boolean hasBootstrap = false;
		boolean hasWeaver = false;
		for (Object obj : args) {
			String entry = (String) obj;
			if (entry.contains("insight-bootstrap")) {
				hasBootstrap = true;
			}
			if (entry.contains("aspectjweaver")) {
				hasWeaver = true;
			}
		}

		assertTrue("Expected to find insight-bootstrap jar on the classpath, but none was found.", hasBootstrap);
		assertTrue("Expected to find aspectjweaver jar on the classpath, but none was found.", hasWeaver);
	}

	public void testInsightClasspath29() throws Exception {
		insightClasspathTest(InsightTestFixture.V_2_9);
	}

	public void testInsightClasspath30() throws Exception {
		insightClasspathTest(InsightTestFixture.V_3_0);
	}
	
	public void testInsightClasspath31() throws Exception {
		insightClasspathTest(InsightTestFixture.V_3_1);
	}
	
	private void insightClasspathTest(InsightTestFixture testFixture) throws Exception {
		server = testFixture.createServer(null);
		server.publish(Server.PUBLISH_FULL, null);

		ILaunchConfigurationWorkingCopy wc = createLaunchConfiguration();
		((Server) server).setupLaunchConfiguration(wc, null);
		List args = wc.getAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, (List) null);

		boolean hasBootstrap = false;
		boolean hasWeaver = false;
		for (Object obj : args) {
			String entry = (String) obj;
			if (entry.contains("insight-bootstrap")) {
				hasBootstrap = true;
			}
			if (entry.contains("insight-weaver")) {
				hasWeaver = true;
			}
		}

		assertTrue("Expected to find insight-bootstrap jar on the classpath, but none was found.", hasBootstrap);
		assertTrue("Expected to find insight-weaver jar on the classpath, but none was found.", hasWeaver);
	}

	@Override
	protected void tearDown() throws Exception {
		if (server != null) {
			TcServerFixture.deleteServerAndRuntime(server);
			server = null;
		}
	}

	private ILaunchConfigurationWorkingCopy createLaunchConfiguration() throws CoreException {
		ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfigurationType launchConfigType = ((ServerType) server.getServerType()).getLaunchConfigurationType();

		String launchName = launchManager.generateUniqueLaunchConfigurationNameFrom("Tc Server Launch");
		ILaunchConfigurationWorkingCopy wc = launchConfigType.newInstance(null, launchName);
		return wc;
	}

}
