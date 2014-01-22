/*******************************************************************************
 * Copyright (c) 2012 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package com.vmware.vfabric.ide.eclipse.tcserver.internal.core;

import java.util.List;

import org.eclipse.jst.server.tomcat.core.internal.command.ServerCommand;

/**
 * Command to change a property in the server configuration.
 * @author Steffen Pingel
 */
public class ModifyExtraVmArgsCommand extends ServerCommand {

	private final TcServer tcServer;

	private final List<String> addValues;

	private List<String> oldAddValues;

	private final List<String> removeValues;

	private List<String> oldRemoveValues;

	public ModifyExtraVmArgsCommand(TcServer tcServer, List<String> addValues, List<String> removeValues) {
		super(tcServer, "Modify vm arguments");
		this.tcServer = tcServer;
		this.addValues = addValues;
		this.removeValues = removeValues;
	}

	@Override
	public void execute() {
		oldAddValues = tcServer.getAddExtraVmArgs();
		oldRemoveValues = tcServer.getRemoveExtraVmArgs();
		tcServer.setAddExtraVmArgs(addValues);
		tcServer.setRemoveExtraVmArgs(removeValues);
	}

	@Override
	public void undo() {
		tcServer.setAddExtraVmArgs(oldAddValues);
		tcServer.setRemoveExtraVmArgs(oldRemoveValues);
	}

}
