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
package com.vmware.vfabric.ide.eclipse.tcserver.internal.core;

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
			String[] arguments = new String[] { "create", DEFAULT_INSTANCE, "-t", "base", "--force" };
			TcServerUtil.executeInstanceCreation(installLocation, DEFAULT_INSTANCE, arguments);
		}

		// TODO e3.6 remove casts for setAttribute()
		((ServerWorkingCopy) server).setAttribute(ITomcatServer.PROPERTY_INSTANCE_DIR, (String) null);
		((ServerWorkingCopy) server).setAttribute(ITomcatServer.PROPERTY_TEST_ENVIRONMENT, false);
		((ServerWorkingCopy) server).setAttribute(TcServer.KEY_ASF_LAYOUT, false);
		((ServerWorkingCopy) server).setAttribute(TcServer.KEY_SERVER_NAME, DEFAULT_INSTANCE);
		TcServerUtil.importRuntimeConfiguration(server, null);
	}
}
