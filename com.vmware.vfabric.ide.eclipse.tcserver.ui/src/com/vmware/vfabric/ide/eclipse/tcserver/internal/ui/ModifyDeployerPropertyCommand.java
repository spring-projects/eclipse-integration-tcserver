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

import org.eclipse.jst.server.tomcat.core.internal.command.ServerCommand;

import com.vmware.vfabric.ide.eclipse.tcserver.internal.core.TcServer;

/**
 * Command to change a property in the server configuration.
 * @author Steffen Pingel
 */
public class ModifyDeployerPropertyCommand extends ServerCommand {

	protected String key;

	protected String value;

	protected String oldValue;

	private final TcServer tcServer;

	public ModifyDeployerPropertyCommand(TcServer tcServer, String key, String value) {
		super(tcServer, "Modify deployer property");
		this.tcServer = tcServer;
		this.key = key;
		this.value = value;
	}

	@Override
	public void execute() {
		oldValue = tcServer.getDeployerProperty(key);
		tcServer.setDeployerProperty(key, value);
	}

	@Override
	public void undo() {
		tcServer.setDeployerProperty(key, oldValue);
	}

}
