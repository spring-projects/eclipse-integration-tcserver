/*******************************************************************************
 * Copyright (c) 2014 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package com.vmware.vfabric.ide.eclipse.tcserver.internal.core;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.eclipse.wst.server.core.IModule;

/**
 * Abstract command for server operation on a server module (i.e. server
 * application)
 *
 * @author Alex Boyko
 *
 */
public abstract class AbstractModuleCommand extends AbstractJmxServerCommand<Object> {

	protected final IModule[] module;

	private ObjectName name;

	public AbstractModuleCommand(TcServerBehaviour behaviour, IModule[] module) {
		super(behaviour);
		this.module = module;
	}

	protected ObjectName getName() throws MalformedObjectNameException {
		if (name == null) {
			name = ObjectName.getInstance("tcServer:type=Serviceability,name=Deployer");
		}
		return name;
	}

}
