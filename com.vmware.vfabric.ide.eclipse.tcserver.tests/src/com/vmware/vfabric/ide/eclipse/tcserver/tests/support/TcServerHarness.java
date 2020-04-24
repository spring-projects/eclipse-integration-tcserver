/*******************************************************************************
 * Copyright (c) 2012, 2020 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package com.vmware.vfabric.ide.eclipse.tcserver.tests.support;

import java.io.File;
import java.util.function.Function;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.internal.ServerWorkingCopy;

import com.vmware.vfabric.ide.eclipse.tcserver.configurator.ServerHandler;
import com.vmware.vfabric.ide.eclipse.tcserver.internal.core.TcServer;
import com.vmware.vfabric.ide.eclipse.tcserver.internal.core.TcServer21ServerHandlerCallback;

/**
 * @author Steffen Pingel
 * 
 */
public class TcServerHarness extends TestHarness {

	private IServer server;

	public TcServerHarness(TcServerFixture configuration) {
		super(configuration);
	}

	@Override
	public TcServerFixture getConfiguration() {
		return (TcServerFixture) super.getConfiguration();
	}

	public ServerHandler provisionServer() throws Exception {
		File baseDir = StsTestUtil.createTempDirectory(getConfiguration().getDescription(), null);
		provisionRuntime(baseDir);
		return getHandler(baseDir.getAbsolutePath());
	}

	public ServerHandler getHandler(String path) throws Exception {
		ServerHandler handler = new ServerHandler(getConfiguration().getServerType());
		handler.setRuntimeName("runtime");
		handler.setServerName("server");
		handler.setServerPath(path);
		return handler;
	}
	
	public IServer createServer(final String instance) throws Exception {
		ServerHandler handler = provisionServer();
		Function<IServerWorkingCopy, IStatus> callback;
		if (TcServer21ServerHandlerCallback.DEFAULT_INSTANCE.equals(instance)) {
			callback = new TcServer21ServerHandlerCallback();
		}
		else {
			callback = wc -> {
				try {
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
					return Status.OK_STATUS;
				} catch (CoreException e) {
					return e.getStatus();
				}
			};
		}
		server = handler.createServer(new NullProgressMonitor(), ServerHandler.ALWAYS_OVERWRITE, callback);
		return server;
	}

	public IServer getServer() {
		return server;
	}

	public void dispose() throws CoreException {
		if (server != null) {
			IFolder serverConfiguration = server.getServerConfiguration();
			server.delete();
			serverConfiguration.delete(true, true, new NullProgressMonitor());

			IRuntime runtime = server.getRuntime();
			if (runtime != null) {
				runtime.delete();
			}
		}
	}

}
