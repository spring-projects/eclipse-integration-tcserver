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
package com.vmware.vfabric.ide.eclipse.tcserver.internal.ui;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.vmware.vfabric.ide.eclipse.tcserver.internal.core.TcServer;

/**
 * {@link AbstractOperation} to modify the jmx deployer port.
 * @author Steffen Pingel
 * @author Christian Dupuis
 * @since 1.1.3
 */
public class ModifyEnhancedRedeployCommand extends AbstractOperation {

	private final TcServer workingCopy;

	private final boolean oldValue;

	private final boolean newValue;

	private final boolean agentOldValue;

	public ModifyEnhancedRedeployCommand(TcServer workingCopy, boolean newValue) {
		super("Modify redeploy");
		this.workingCopy = workingCopy;
		this.oldValue = workingCopy.isEnhancedRedeployEnabled();
		this.agentOldValue = workingCopy.isAgentRedeployEnabled();
		this.newValue = newValue;
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		workingCopy.setEnhancedRedeployEnabled(newValue);
		if (newValue) {
			workingCopy.setAgentRedeployEnabled(false);
		}
		return Status.OK_STATUS;
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		return execute(monitor, info);
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		workingCopy.setEnhancedRedeployEnabled(oldValue);
		workingCopy.setAgentRedeployEnabled(agentOldValue);
		return Status.OK_STATUS;
	}
}
