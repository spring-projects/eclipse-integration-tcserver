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
 * Command to modify the list of static resources that control deployment.
 * @author Steffen Pingel
 * @author Christian Dupuis
 */
public class ModifyStaticResourcesCommand extends AbstractOperation {

	private final TcServer workingCopy;

	private final String oldValue;

	private final String newValue;

	public ModifyStaticResourcesCommand(TcServer workingCopy, String newValue) {
		super("Modify static resources");
		this.workingCopy = workingCopy;
		this.oldValue = workingCopy.getStaticFilenamePatterns();
		this.newValue = newValue;
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		workingCopy.setStaticFilenamePatterns(newValue);
		return Status.OK_STATUS;
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		workingCopy.setStaticFilenamePatterns(newValue);
		return Status.OK_STATUS;
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		workingCopy.setStaticFilenamePatterns(oldValue);
		return Status.OK_STATUS;
	}

}
