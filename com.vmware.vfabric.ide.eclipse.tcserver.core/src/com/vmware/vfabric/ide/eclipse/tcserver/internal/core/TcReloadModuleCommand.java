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
import javax.management.RuntimeOperationsException;

/**
 * Sends a reload request for a module through JMX.
 * @author Steffen Pingel
 */
public class TcReloadModuleCommand extends AbstractTcDeployerCommand {

	private boolean forceDeploy;

	public TcReloadModuleCommand(TcServerBehaviour serverBehaviour, String service, String host, String contextPath,
			String warFile) {
		super(serverBehaviour, service, host, contextPath, warFile);
	}

	@Override
	protected Object doOperation(MBeanServerConnection connection) throws IOException, JMException {
		if (isForceDeploy()) {
			try {
				return deployApplication(connection);
			}
			catch (RuntimeOperationsException e) {
				String state = getApplicationState(connection);
				if (STATE_AVAILABLE.equals(state) || STATE_CONFIGURED.equals(state) || STATE_DEPLOYED.equals(state)) {
					try {
						undeployApplication(connection);
					}
					catch (RuntimeOperationsException e2) {
						// ignore
					}
				}
				return deployApplication(connection);
			}
		}
		else {
			try {
				return reloadApplication(connection);
			}
			catch (RuntimeOperationsException e) {
				String state = getApplicationState(connection);
				if (STATE_CONFIGURED.equals(state)) {
					return startApplication(connection);
				}
				else if (STATE_NOT_DEPLOYED.equals(state) && getWarFile() != null) {
					// deploys to the appBase which is does not correspond to
					// wtp
					// configuration
					// return deployApplication(connection, name);
					// ignore, application is not yet deployed
					return null;
				}
				throw e;
			}
		}
	}

	public boolean isForceDeploy() {
		return forceDeploy;
	}

	public void setForceDeploy(boolean forceDeploy) {
		this.forceDeploy = forceDeploy;
	}

}
