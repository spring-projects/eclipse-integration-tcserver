/*******************************************************************************
 * Copyright (c) 2012 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package com.vmware.vfabric.ide.eclipse.tcserver.internal.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jst.server.tomcat.core.internal.ITomcatServer;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.internal.ServerWorkingCopy;
import org.springsource.ide.eclipse.commons.configurator.ServerHandlerCallback;

/**
 * @author Steffen Pingel
 * @author Christian Dupuis
 * @deprecated
 */
@Deprecated
public class TcServer20ServerHandlerCallback extends ServerHandlerCallback {

	public void configureServer(IServerWorkingCopy server) throws CoreException {
		// TODO e3.6 remove casts for setAttribute()
		((ServerWorkingCopy) server).setAttribute(ITomcatServer.PROPERTY_INSTANCE_DIR, (String) null);
		((ServerWorkingCopy) server).setAttribute(ITomcatServer.PROPERTY_TEST_ENVIRONMENT, false);
		((ServerWorkingCopy) server).setAttribute(TcServer.KEY_ASF_LAYOUT, false);
		((ServerWorkingCopy) server).setAttribute(TcServer.KEY_SERVER_NAME, "spring-insight-instance");
		TcServerUtil.importRuntimeConfiguration(server, null);
	}

}
