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

import com.vmware.vfabric.ide.eclipse.tcserver.internal.core.ServerProperty;
import com.vmware.vfabric.ide.eclipse.tcserver.internal.core.TcServerConfiguration;

/**
 * Command to change a property in the server configuration.
 * @author Steffen Pingel
 */
public class ModifyServerPropertyCommand extends ConfigurationCommand {

	protected String key;

	protected String value;

	protected String oldValue;

	private final TcServerConfiguration tcServerConfiguration;

	public ModifyServerPropertyCommand(TcServerConfiguration configuration, String key, String value) {
		super(configuration, Messages.configurationEditorActionModifyPort);
		this.tcServerConfiguration = configuration;
		this.key = key;
		this.value = value;
	}

	@Override
	public void execute() {
		// find old property
		Iterator<ServerProperty> iterator = tcServerConfiguration.getProperties().iterator();
		while (iterator.hasNext()) {
			ServerProperty temp = iterator.next();
			if (key.equals(temp.getKey())) {
				oldValue = temp.getValue();
			}
		}
		if (value != null && !value.equals(oldValue)) {
			tcServerConfiguration.modifyProperty(key, value);
		}
	}

	@Override
	public void undo() {
		tcServerConfiguration.modifyProperty(key, oldValue);
	}
}