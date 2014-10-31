/*******************************************************************************
 * Copyright (c) 2014 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package com.vmware.vfabric.ide.eclipse.tcserver.internal.core;

import java.io.IOException;

import javax.management.JMException;
import javax.management.MBeanServerConnection;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.wst.server.core.IModule;

/**
 * Command for stopping a (started or running) server module/application
 *
 * @author Alex Boyko
 *
 */
public class StopModuleCommand extends AbstractModuleCommand {

	public StopModuleCommand(TcServerBehaviour serverBehaviour, IModule[] module) {
		super(serverBehaviour, module);
	}

	@Override
	protected Object doOperation(MBeanServerConnection beanServerConnection) throws IOException, JMException,
			CoreException {
		DeployInfo info = new DeployInfo(serverBehaviour, module);
		Object[] operationArguments = new Object[] { info.getService(), info.getHost(), info.getContextPath() };
		return beanServerConnection.invoke(getName(), "stopApplication", operationArguments,
				getSignature(operationArguments));
	}

}
