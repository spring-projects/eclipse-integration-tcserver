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
package com.vmware.vfabric.ide.eclipse.tcserver.internal.core;

import java.io.IOException;

import javax.management.JMException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

/**
 * Sends a shutdown request through JMX.
 * @author Steffen Pingel
 * @author Christian Dupuis
 */
public class ShutdownTcServerCommand extends AbstractJmxServerCommand<Object> {

	public ShutdownTcServerCommand(TcServerBehaviour serverBehaviour) {
		super(serverBehaviour);
	}

	@Override
	protected Object doOperation(MBeanServerConnection connection) throws IOException, JMException {
		ObjectName name = ObjectName.getInstance("Catalina:type=Service,serviceName=Catalina");
		return connection.invoke(name, "stop", null, null);
	}

}
