/*******************************************************************************
 * Copyright (c) 2012, 2014 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package com.vmware.vfabric.ide.eclipse.tcserver.internal.core;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jst.server.tomcat.core.internal.ITomcatServer;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.internal.ServerWorkingCopy;
import org.springsource.ide.eclipse.commons.configurator.ServerHandlerCallback;

/**
 * @author Steffen Pingel
 * @author Christian Dupuis
 */
public class TcServer21ServerHandlerCallback extends ServerHandlerCallback {

	public static final String DEFAULT_INSTANCE = "base-instance";

	public void configureServer(IServerWorkingCopy server) throws CoreException {
		// Create a default instance in case that one is missing
		IPath installLocation = server.getRuntime().getLocation();
		if (!installLocation.append(DEFAULT_INSTANCE).toFile().exists()) {
			List<String> arguments = new ArrayList<String>(Arrays.asList(new String[] { "create", DEFAULT_INSTANCE,
					"-t", "base", "--force" }));
			List<File> tomcatFolders = TcServerRuntime.getTomcatVersions(installLocation.toFile());
			if (tomcatFolders != null && !tomcatFolders.isEmpty()) {
				String tomcatVersion = TcServerUtil.getServerVersion(tomcatFolders.get(0).getName());
				arguments.add("-v");
				arguments.add(tomcatVersion);
				((ServerWorkingCopy) server).setAttribute(TcServerRuntime.KEY_SERVER_VERSION, tomcatVersion);
			}
			TcServerUtil.executeInstanceCreation(installLocation, DEFAULT_INSTANCE,
					arguments.toArray(new String[arguments.size()]));
		}

		// TODO e3.6 remove casts for setAttribute()
		((ServerWorkingCopy) server).setAttribute(ITomcatServer.PROPERTY_INSTANCE_DIR, (String) null);
		((ServerWorkingCopy) server).setAttribute(ITomcatServer.PROPERTY_TEST_ENVIRONMENT, false);
		((ServerWorkingCopy) server).setAttribute(TcServer.KEY_ASF_LAYOUT, false);
		((ServerWorkingCopy) server).setAttribute(TcServer.KEY_SERVER_NAME, DEFAULT_INSTANCE);
		TcServerUtil.importRuntimeConfiguration(server, null);
	}
}
