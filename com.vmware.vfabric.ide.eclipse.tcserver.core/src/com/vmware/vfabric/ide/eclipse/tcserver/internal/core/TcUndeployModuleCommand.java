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

import java.io.IOException;

import javax.management.JMException;
import javax.management.MBeanServerConnection;

/**
 * Undeploys a module through JMX.
 * @author Steffen Pingel
 */
public class TcUndeployModuleCommand extends AbstractTcDeployerCommand {

	public TcUndeployModuleCommand(TcServerBehaviour serverBehaviour, String service, String host, String contextPath) {
		super(serverBehaviour, service, host, contextPath, null);
	}

	@Override
	protected Object doOperation(MBeanServerConnection connection) throws IOException, JMException {
		return undeployApplication(connection);
	}

}
