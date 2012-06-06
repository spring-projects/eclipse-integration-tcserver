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

import java.util.Iterator;

import org.eclipse.jst.server.tomcat.core.internal.Messages;
import org.eclipse.jst.server.tomcat.core.internal.command.ConfigurationCommand;

import com.vmware.vfabric.ide.eclipse.tcserver.internal.core.TcServerConfiguration;
import com.vmware.vfabric.ide.eclipse.tcserver.internal.core.TcServerPort;

/**
 * Command to change a port in the server configuration.
 * @author Steffen Pingel
 */
public class ModifyTcServerPortCommand extends ConfigurationCommand {

	protected String id;

	protected String port;

	protected String oldPort;

	private final TcServerConfiguration tcServerConfiguration;

	public ModifyTcServerPortCommand(TcServerConfiguration configuration, String id, String port) {
		super(configuration, Messages.configurationEditorActionModifyPort);
		this.tcServerConfiguration = configuration;
		this.id = id;
		this.port = port;
	}

	@Override
	public void execute() {
		// find old port number
		Iterator<TcServerPort> iterator = tcServerConfiguration.getTcServerPorts().iterator();
		while (iterator.hasNext()) {
			TcServerPort temp = iterator.next();
			if (id.equals(temp.getId())) {
				oldPort = temp.getPortString();
			}
		}

		tcServerConfiguration.modifyServerPort(id, port);
	}

	@Override
	public void undo() {
		tcServerConfiguration.modifyServerPort(id, oldPort);
	}
}