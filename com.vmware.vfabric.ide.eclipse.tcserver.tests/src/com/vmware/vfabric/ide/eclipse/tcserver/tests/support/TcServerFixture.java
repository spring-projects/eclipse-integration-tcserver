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
package com.vmware.vfabric.ide.eclipse.tcserver.tests.support;

import java.io.File;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.internal.RuntimeWorkingCopy;
import org.eclipse.wst.server.core.internal.ServerWorkingCopy;
import org.springsource.ide.eclipse.commons.configurator.ServerHandler;
import org.springsource.ide.eclipse.commons.configurator.ServerHandlerCallback;

import com.vmware.vfabric.ide.eclipse.tcserver.internal.core.ITcRuntime;
import com.vmware.vfabric.ide.eclipse.tcserver.internal.core.TcServer;
import com.vmware.vfabric.ide.eclipse.tcserver.internal.core.TcServer21ServerHandlerCallback;
import com.vmware.vfabric.ide.eclipse.tcserver.internal.core.TcServerRuntime;
import com.vmware.vfabric.ide.eclipse.tcserver.internal.core.TcServerUtil;

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

	public static String V_3_0_URL = "http://download.pivotal.com.s3.amazonaws.com/tcserver/3.0.1/pivotal-tc-server-developer-3.0.1.RELEASE.zip";

	public static String V_3_1_URL = "http://dist.springsource.com.s3.amazonaws.com/release/TCS/pivotal-tc-server-developer-3.1.0.RELEASE.zip";

	public static String V_4_0_URL = "http://dist.springsource.com.s3.amazonaws.com/release/TCS/pivotal-tc-server-developer-4.0.0.RELEASE.zip";
	
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
			"pivotal-tc-server-developer-3.0.1.RELEASE", V_3_0_URL);

	public static TcServerFixture V_3_1 = new TcServerFixture(TcServerTestPlugin.PLUGIN_ID, TcServer.ID_TC_SERVER_3_0,
			"pivotal-tc-server-developer-3.1.0.RELEASE", V_3_1_URL);

	public static TcServerFixture V_4_0 = new TcServerFixture(TcServerTestPlugin.PLUGIN_ID, TcServer.ID_TC_SERVER_4_0,
			"pivotal-tc-server", V_4_0_URL);
	
	public static TcServerFixture V_6_0 = new TcServerFixture("com.vmware.server.tc.runtime.70",
			TcServer.ID_TC_SERVER_2_5, "vfabric-tc-server-developer-2.5.2.RELEASE",
			"http://download.springsource.com/release/TCS/vfabric-tc-server-developer-2.5.2.RELEASE.zip", "6", true);

	private static TcServerFixture current;

	private static final TcServerFixture DEFAULT = V_4_0;

	public static TcServerFixture[] ALL = new TcServerFixture[] { V_6_0, V_2_5, V_2_6, V_2_7, V_2_8, V_2_9, V_3_0,
			V_3_1, V_4_0 };

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

	private String tomcatVersion = null;

	private boolean legacyTests = false;

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

	public TcServerFixture(String testPlugin, String serverType, String stubPath, String downloadUrl,
			String tomcatVersion, boolean createDefault) {
		super(stubPath);
		this.testPluginId = testPlugin;
		this.serverType = serverType;
		this.stubPath = stubPath;
		this.tomcatVersion = tomcatVersion;
		this.legacyTests = createDefault;
		setDownloadUrl(downloadUrl);
	}

	@Override
	public TcServerHarness createHarness() {
		return new TcServerHarness(this);
	}

	public IServer createServer(final String instance) throws Exception {
		ServerHandler handler = provisionServer();
		ServerHandlerCallback callback;
		callback = new TcServer21ServerHandlerCallback() {

			@Override
			public void configureServer(IServerWorkingCopy server) throws CoreException {
				super.configureServer(server);
				if (tomcatVersion != null) {
					ITcRuntime tcRuntime = TcServerUtil.getTcRuntime(server.getRuntime());
					List<File> tomcatFolders = TcServerRuntime.getTomcatVersions(tcRuntime.getTomcatServersContainer().toFile());
					for (File tomcatFolder : tomcatFolders) {
						String version = TcServerUtil.getServerVersion(tomcatFolder.getName());
						if (version.startsWith(tomcatVersion)) {
							RuntimeWorkingCopy wc = (RuntimeWorkingCopy) server.getRuntime().createWorkingCopy();
							wc.setAttribute(TcServerRuntime.KEY_SERVER_VERSION, tomcatFolder.getName());
							wc.save(true, new NullProgressMonitor());
							break;
						}
					}
				}
				if (legacyTests) {
					if (instance != null) {
						((ServerWorkingCopy) server).setAttribute(TcServer.KEY_ASF_LAYOUT, false);
					}
					else {
						((ServerWorkingCopy) server).setAttribute(TcServer.KEY_ASF_LAYOUT, true);
					}
					((ServerWorkingCopy) server).setAttribute(TcServer.KEY_SERVER_NAME, instance);
					((ServerWorkingCopy) server).setAttribute(TcServer.PROPERTY_TEST_ENVIRONMENT, false);
				}
				((ServerWorkingCopy) server).importRuntimeConfiguration(server.getRuntime(), null);
			}

		};
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

	/**
	 * Checks whether current fixture is after the argument fixture in terms of
	 * order in ALL array. To be used as version check before/after.
	 * @param fixture
	 * @return <code>true</code> if server version is newer than the argument's
	 * server version.
	 */
	public boolean after(TcServerFixture fixture) {
		boolean foundMyself = false;
		for (TcServerFixture s : ALL) {
			if (s == this) {
				foundMyself = true;
			}
			if (s == fixture) {
				return !foundMyself;
			}
		}
		return false;
	}

}
