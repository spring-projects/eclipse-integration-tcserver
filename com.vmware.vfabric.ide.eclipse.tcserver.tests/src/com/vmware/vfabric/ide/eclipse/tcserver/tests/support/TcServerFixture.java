/*******************************************************************************
 * Copyright (c) 2012 - 2014 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package com.vmware.vfabric.ide.eclipse.tcserver.tests.support;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.internal.ServerWorkingCopy;
import org.springsource.ide.eclipse.commons.configurator.ServerHandler;
import org.springsource.ide.eclipse.commons.configurator.ServerHandlerCallback;

import com.vmware.vfabric.ide.eclipse.tcserver.internal.core.TcServer;
import com.vmware.vfabric.ide.eclipse.tcserver.internal.core.TcServer21ServerHandlerCallback;

/**
 * @author Steffen Pingel
 * @author Kris De Volder
 * @author Tomasz Zarna
 * @author Leo Dos Santos
 */
public class TcServerFixture extends TestConfiguration {

	public static String INST_COMBINED = "combined-instance";

	public static String INST_INSIGHT = "spring-insight-instance";

	public static String INST_SEPARATE = "separate-instance";

	public static String V_2_8_URL = "http://download.springsource.com/release/TCS/vfabric-tc-server-developer-2.8.0.RELEASE.zip";

	public static String V_2_9_URL = "http://download.springsource.com/release/TCS/vfabric-tc-server-developer-2.9.3.RELEASE.zip";

	public static String V_3_0_URL = "http://download.pivotal.com.s3.amazonaws.com/tcserver/3.0.0/pivotal-tc-server-developer-3.0.0.RELEASE.zip";

	public static TcServerFixture V_2_0 = new TcServerFixture(TcServerTestPlugin.PLUGIN_ID, TcServer.ID_TC_SERVER_2_0,
			"springsource-tc-server-developer",
			"http://download.springsource.com/release/TCS/springsource-tc-server-developer-2.0.0.SR01.zip");

	public static TcServerFixture V_2_1 = new TcServerFixture(TcServerTestPlugin.PLUGIN_ID, TcServer.ID_TC_SERVER_2_1,
			"springsource-tc-server-developer",
			"http://download.springsource.com/release/TCS/springsource-tc-server-developer-2.1.0.RELEASE.zip");

	public static TcServerFixture V_2_5 = new TcServerFixture("com.vmware.server.tc.runtime.70",
			TcServer.ID_TC_SERVER_2_5, "vfabric-tc-server-developer-2.5.2.RELEASE",
			"http://download.springsource.com/release/TCS/vfabric-tc-server-developer-2.5.2.RELEASE.zip");

	public static TcServerFixture V_2_6 = new TcServerFixture(TcServerTestPlugin.PLUGIN_ID, TcServer.ID_TC_SERVER_2_5,
			"vfabric-tc-server-developer-2.6.1.RELEASE",
			"http://download.springsource.com/release/TCS/vfabric-tc-server-developer-2.6.1.RELEASE.zip");

	public static TcServerFixture V_2_7 = new TcServerFixture(TcServerTestPlugin.PLUGIN_ID, TcServer.ID_TC_SERVER_2_5,
			"vfabric-tc-server-developer-2.7.0.RC1",
			"http://download.springsource.com/milestone/TCS/vfabric-tc-server-developer-2.7.0.RC1.zip");

	public static TcServerFixture V_2_8 = new TcServerFixture(TcServerTestPlugin.PLUGIN_ID, TcServer.ID_TC_SERVER_2_5,
			"vfabric-tc-server-developer-2.8.0.RELEASE", V_2_8_URL);

	public static TcServerFixture V_2_9 = new TcServerFixture(TcServerTestPlugin.PLUGIN_ID, TcServer.ID_TC_SERVER_2_5,
			"vfabric-tc-server-developer-2.9.3.RELEASE", V_2_9_URL);

	public static TcServerFixture V_3_0 = new TcServerFixture(TcServerTestPlugin.PLUGIN_ID, TcServer.ID_TC_SERVER_3_0,
			"pivotal-tc-server-developer-3.0.0.RELEASE", V_3_0_URL);

	public static TcServerFixture V_6_0 = new TcServerFixture(TcServer.ID_TC_SERVER_2_0, "tcServer-6.0");

	private static TcServerFixture current;

	private static final TcServerFixture DEFAULT = V_3_0;

	public static TcServerFixture[] ALL = new TcServerFixture[] { V_6_0, V_2_0, V_2_1, V_2_5, V_2_6, V_2_7, V_2_8,
			V_2_9, V_3_0 };

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
		ServerHandlerCallback callback;
		if (TcServer.ID_TC_SERVER_2_5.equals(serverType) || TcServer.ID_TC_SERVER_3_0.equals(serverType)) {
			callback = new TcServer21ServerHandlerCallback();
		}
		else {
			callback = new ServerHandlerCallback() {
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
			};
		}
		return handler.createServer(new NullProgressMonitor(), ServerHandler.ALWAYS_OVERWRITE, callback);
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

	public ServerHandler provisionServer() throws Exception {
		TcServerHarness harness = createHarness();
		return harness.provisionServer();
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
