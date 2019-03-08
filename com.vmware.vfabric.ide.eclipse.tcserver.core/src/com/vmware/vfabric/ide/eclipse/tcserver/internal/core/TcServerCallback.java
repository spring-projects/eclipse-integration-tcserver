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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;

/**
 * Callback interface to support clients to hook into tc Server processes.
 * @author Steffen Pingel
 */
public abstract class TcServerCallback {

	/**
	 * @see TcServer#setDefaults(IProgressMonitor)
	 */
	public void setDefaults(TcServer server, IProgressMonitor monitor) {
	}

	/**
	 * @see TcServerBehaviour#setupLaunch(ILaunch, String, IProgressMonitor)
	 */
	public void setupLaunch(TcServer server, ILaunch launch, String launchMode, IProgressMonitor monitor)
			throws CoreException {
	}

	/**
	 * @see TcServerBehaviour#setupLaunchConfiguration(ILaunchConfigurationWorkingCopy,
	 * IProgressMonitor)
	 */
	public void setupLaunchConfiguration(TcServer server, ILaunchConfigurationWorkingCopy workingCopy,
			IProgressMonitor monitor) throws CoreException {
	}

	/**
	 * @see TcServerBehaviour#publishServer(int, IProgressMonitor)
	 */
	public void publishServer(TcServer tcServer, int kind, IProgressMonitor monitor) throws CoreException {
	}

}
