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
package com.vmware.vfabric.ide.eclipse.tcserver.internal.ui;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.vmware.vfabric.ide.eclipse.tcserver.internal.core.TcServer;

/**
 * {@link AbstractOperation} to enable/disable the Agent-based reloading.
 * @author Steffen Pingel
 * @author Christian Dupuis
 * @since 2.5.0
 */
public class ModifyAgentRedeployCommand extends AbstractOperation {

	private final TcServer workingCopy;

	private final boolean oldValue;

	private final boolean newValue;

	private final boolean enhancedOldValue;

	public ModifyAgentRedeployCommand(TcServer workingCopy, boolean newValue) {
		super("Modify redeploy");
		this.workingCopy = workingCopy;
		this.oldValue = workingCopy.isAgentRedeployEnabled();
		this.enhancedOldValue = workingCopy.isEnhancedRedeployEnabled();
		this.newValue = newValue;
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		workingCopy.setAgentRedeployEnabled(newValue);
		if (newValue) {
			workingCopy.setEnhancedRedeployEnabled(false);
		}
		return Status.OK_STATUS;
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		return execute(monitor, info);
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		workingCopy.setEnhancedRedeployEnabled(enhancedOldValue);
		workingCopy.setAgentRedeployEnabled(oldValue);
		return Status.OK_STATUS;
	}
}
