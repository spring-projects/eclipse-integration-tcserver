/*******************************************************************************
 * Copyright (c) 2012, 2018 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package com.vmware.vfabric.ide.eclipse.tcserver.internal.core;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jst.server.tomcat.core.internal.ITomcatServer;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.internal.ServerWorkingCopy;

/**
 * @author Steffen Pingel
 * @author Christian Dupuis
 */
public class TcServer21ServerHandlerCallback implements Function<IServerWorkingCopy, IStatus> {

	public static final String DEFAULT_INSTANCE = "base-instance";

	public IStatus apply(IServerWorkingCopy server) {
		try {
			// Create a default instance in case that one is missing
			IPath installLocation = server.getRuntime().getLocation();
			if (!installLocation.append(DEFAULT_INSTANCE).toFile().exists()) {
				ITcRuntime tcRuntime = TcServerUtil.getTcRuntime(server.getRuntime());
				List<String> arguments = new ArrayList<String>(Arrays.asList(new String[] { "create", DEFAULT_INSTANCE,
						"-t", "base", "--force" }));
				List<File> tomcatFolders = TcServerRuntime.getTomcatVersions(tcRuntime.getTomcatServersContainer().toFile());
				if (tomcatFolders != null && !tomcatFolders.isEmpty()) {
					String tomcatVersion = ((ServerWorkingCopy) server).getAttribute(TcServerRuntime.KEY_SERVER_VERSION,
							TcServerUtil.getServerVersion(tcRuntime.getTomcatLocation().lastSegment()));
					arguments.add("-v");
					arguments.add(tomcatVersion);
					((ServerWorkingCopy) server).setAttribute(TcServerRuntime.KEY_SERVER_VERSION, tomcatVersion);
				}
				TcServerUtil.executeInstanceCreation(server.getRuntime(), DEFAULT_INSTANCE,
						arguments.toArray(new String[arguments.size()]));
			}

			// TODO e3.6 remove casts for setAttribute()
			((ServerWorkingCopy) server).setAttribute(ITomcatServer.PROPERTY_INSTANCE_DIR, (String) null);
			((ServerWorkingCopy) server).setAttribute(ITomcatServer.PROPERTY_TEST_ENVIRONMENT, false);
			((ServerWorkingCopy) server).setAttribute(TcServer.KEY_ASF_LAYOUT, false);
			((ServerWorkingCopy) server).setAttribute(TcServer.KEY_SERVER_NAME, DEFAULT_INSTANCE);
			TcServerUtil.importRuntimeConfiguration(server, null);
			return Status.OK_STATUS;
		} catch (CoreException e) {
			return e.getStatus();
		}
	}
}
