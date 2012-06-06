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
package com.vmware.vfabric.ide.eclipse.tcserver.tests.support;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.internal.ServerWorkingCopy;
import org.springsource.ide.eclipse.commons.configurator.ServerHandler;
import org.springsource.ide.eclipse.commons.configurator.ServerHandlerCallback;
import org.springsource.ide.eclipse.commons.core.FileUtil;
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;

import com.vmware.vfabric.ide.eclipse.tcserver.internal.core.TcServer;

/**
 * @author Steffen Pingel
 * @author Kris De Volder
 */
public class TcServerFixture extends TestConfiguration {

	public static String INST_COMBINED = "combined-instance";

	public static String INST_INSIGHT = "spring-insight-instance";

	public static String INST_SEPARATE = "separate-instance";

	public static TcServerFixture V_2_0 = new TcServerFixture("com.springsource.tcserver.60",
			"tc-server-developer-2.0.0.SR01");

	public static TcServerFixture V_2_1 = new TcServerFixture("com.springsource.tcserver.70",
			"tc-server-developer-2.1.0.RELEASE");

	public static TcServerFixture V_2_5 = new TcServerFixture("com.vmware.server.tc.runtime.70",
			"com.vmware.server.tc.70", "vfabric-tc-server-developer-2.5.2.RELEASE",
			"http://download.springsource.com/release/TCS/vfabric-tc-server-developer-2.5.2.RELEASE.zip");

	public static TcServerFixture V_2_6 = new TcServerFixture(TcServerTestPlugin.PLUGIN_ID, "com.vmware.server.tc.70",
			"vfabric-tc-server-developer-2.6.1.RELEASE",
			"http://download.springsource.com/release/TCS/vfabric-tc-server-developer-2.6.1.RELEASE.zip");

	public static TcServerFixture V_2_7 = new TcServerFixture(TcServerTestPlugin.PLUGIN_ID, "com.vmware.server.tc.70",
			"vfabric-tc-server-developer-2.7.0.RC1",
			"http://download.springsource.com/milestone/TCS/vfabric-tc-server-developer-2.7.0.RC1.zip");

	public static TcServerFixture V_6_0 = new TcServerFixture("com.springsource.tcserver.60", "tcServer-6.0");

	private static TcServerFixture current;

	private static final TcServerFixture DEFAULT = V_2_7;

	public static TcServerFixture[] ALL = new TcServerFixture[] { V_6_0, V_2_0, V_2_1, V_2_5, V_2_6, V_2_7 };

	public static TcServerFixture current() {
		if (current == null) {
			DEFAULT.activate();
		}
		return current;
	}

	public static void deleteServerAndRuntime(IServer server) throws CoreException {
		IFolder serverConfiguration = server.getServerConfiguration();
		server.delete();
		serverConfiguration.delete(true, true, new NullProgressMonitor());

		IRuntime runtime = server.getRuntime();
		if (runtime != null) {
			runtime.delete();
		}
	}

	private final String serverType;

	private final String stubPath;

	private final String testPluginId;

	public TcServerFixture(String serverType, String stubPath) {
		this(TcServerTestPlugin.PLUGIN_ID, serverType, stubPath);
	}

	public TcServerFixture(String testPlugin, String serverType, String stubPath) {
		super(stubPath);
		this.testPluginId = testPlugin;
		this.serverType = serverType;
		this.stubPath = stubPath;
	}

	public TcServerFixture(String testPlugin, String serverType, String stubPath, String downloadUrl) {
		super(stubPath);
		this.testPluginId = testPlugin;
		this.serverType = serverType;
		this.stubPath = stubPath;
		setDownloadUrl(downloadUrl);
	}

	@Override
	public TcServerHarness createHarness() {
		return new TcServerHarness(this);
	}

	public IServer createServer(final String instance) throws Exception {
		ServerHandler handler = provisionServer();
		return handler.createServer(new NullProgressMonitor(), ServerHandler.ALWAYS_OVERWRITE,
				new ServerHandlerCallback() {
					@Override
					public void configureServer(IServerWorkingCopy wc) throws CoreException {
						// TODO e3.6 remove casts for setAttribute()
						if (instance != null) {
							((ServerWorkingCopy) wc).setAttribute(TcServer.KEY_ASF_LAYOUT, false);
						}
						else {
							((ServerWorkingCopy) wc).setAttribute(TcServer.KEY_ASF_LAYOUT, true);
						}
						((ServerWorkingCopy) wc).setAttribute(TcServer.KEY_SERVER_NAME, instance);
						((ServerWorkingCopy) wc).setAttribute(TcServer.PROPERTY_TEST_ENVIRONMENT, false);
						((ServerWorkingCopy) wc).importRuntimeConfiguration(wc.getRuntime(), null);
					}
				});
	}

	public ServerHandler getHandler(String path) throws Exception {
		ServerHandler handler = new ServerHandler(serverType);
		handler.setRuntimeName("runtime");
		handler.setServerName("server");
		handler.setServerPath(path);
		return handler;
	}

	public String getServerType() {
		return serverType;
	}

	public File getStubLocation() throws IOException {
		return StsTestUtil.getFilePath(testPluginId, "/testdata/" + stubPath);

	}

	public ServerHandler provisionServer() throws Exception {
		File baseDir = StsTestUtil.createTempDirectory("tcServer", null);
		// copy server skeleton
		FileUtil.copyDirectory(getStubLocation(), baseDir, new NullProgressMonitor());
		return getHandler(baseDir.getAbsolutePath());
	}

	@Override
	protected TestConfiguration getDefault() {
		return DEFAULT;
	}

	@Override
	public void activate() {
		current = this;
	}

}
