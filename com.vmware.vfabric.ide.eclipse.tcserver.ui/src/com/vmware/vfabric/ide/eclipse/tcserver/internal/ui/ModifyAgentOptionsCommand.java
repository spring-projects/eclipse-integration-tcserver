/*******************************************************************************
 * Copyright (c) 2012 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     GoPivotal, Inc. - initial API and implementation
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
 * {@link AbstractOperation} to modify the Agent-based reloading options.
 * @author Steffen Pingel
 * @author Christian Dupuis
 * @since 2.5.0
 */
public class ModifyAgentOptionsCommand extends AbstractOperation {

	private final TcServer workingCopy;

	private final String oldValue;

	private final String newValue;

	public ModifyAgentOptionsCommand(TcServer workingCopy, String newValue) {
		super("Modify agent options");
		this.workingCopy = workingCopy;
		this.oldValue = workingCopy.getAgentOptions();
		this.newValue = newValue;
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		workingCopy.setAgentOptions(newValue);
		return Status.OK_STATUS;
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		return execute(monitor, info);
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		workingCopy.setAgentOptions(oldValue);
		return Status.OK_STATUS;
	}
}
