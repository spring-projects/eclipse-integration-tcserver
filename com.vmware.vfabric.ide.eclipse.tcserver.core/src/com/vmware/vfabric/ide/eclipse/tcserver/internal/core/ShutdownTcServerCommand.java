/*******************************************************************************
 * Copyright (c) 2012 - 2013 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package com.vmware.vfabric.ide.eclipse.tcserver.internal.core;

import java.io.IOException;

import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

/**
 * Sends a shutdown request through JMX.
 * @author Steffen Pingel
 * @author Christian Dupuis
 * @author Leo Dos Santos
 */
public class ShutdownTcServerCommand extends AbstractJmxServerCommand<Object> {

	public ShutdownTcServerCommand(TcServerBehaviour serverBehaviour) {
		super(serverBehaviour);
	}

	@Override
	protected Object doOperation(MBeanServerConnection connection) throws IOException, JMException {
		// First attempt invoking Tomcat 7 Service JMX bean.
		ObjectName name = ObjectName.getInstance("Catalina:type=Service");
		Object obj;
		try {
			obj = connection.invoke(name, "stop", null, null);
		}
		catch (InstanceNotFoundException ex) {
			// We could be running a Tomcat 6 based tc Server, so attempt
			// invoking Tomcat 6 Service JMX bean.
			name = ObjectName.getInstance("Catalina:type=Service,serviceName=Catalina");
			obj = connection.invoke(name, "stop", null, null);
		}
		return obj;
	}

}
