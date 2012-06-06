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
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

/**
 * Sends a reload request for a module through JMX.
 * @author Steffen Pingel
 */
public abstract class AbstractTcDeployerCommand extends AbstractJmxServerCommand<Object> {

	private final String service;

	private final String host;

	private final String contextPath;

	private final String warFile;

	private ObjectName name;

	public static final String STATE_CONFIGURED = "CONFIGURED";

	public static final String STATE_AVAILABLE = "AVAILABLE";

	public static final String STATE_NOT_DEPLOYED = "NOT_DEPLOYED";

	public static final String STATE_DEPLOYED = "DEPLOYED";

	public AbstractTcDeployerCommand(TcServerBehaviour serverBehaviour, String service, String host,
			String contextPath, String warFile) {
		super(serverBehaviour);
		this.service = service;
		this.host = host;
		this.contextPath = contextPath;
		this.warFile = warFile;
	}

	protected Object deployApplication(MBeanServerConnection connection) throws IOException, JMException {
		Object[] operationArguments = new Object[] { service, host, contextPath, warFile };
		return connection.invoke(getName(), "deployApplication", operationArguments, getSignature(operationArguments));
	}

	protected String getApplicationState(MBeanServerConnection connection) throws IOException, JMException {
		Object[] operationArguments = new Object[] { service, host, contextPath };
		return (String) connection.invoke(name, "getApplicationState", operationArguments,
				getSignature(operationArguments));
	}

	protected ObjectName getName() throws MalformedObjectNameException {
		if (name == null) {
			name = ObjectName.getInstance("tcServer:type=Serviceability,name=Deployer");
		}
		return name;
	}

	public String getWarFile() {
		return warFile;
	}

	protected Object reloadApplication(MBeanServerConnection connection) throws IOException, JMException {
		Object[] operationArguments = new Object[] { service, host, contextPath };
		return connection.invoke(getName(), "reloadApplication", operationArguments, getSignature(operationArguments));
	}

	protected Object startApplication(MBeanServerConnection connection) throws IOException, JMException {
		Object[] operationArguments = new Object[] { service, host, contextPath };
		return connection.invoke(getName(), "startApplication", operationArguments, getSignature(operationArguments));
	}

	protected Object undeployApplication(MBeanServerConnection connection) throws IOException, JMException {
		Object[] operationArguments = new Object[] { service, host, contextPath };
		return connection
				.invoke(getName(), "undeployApplication", operationArguments, getSignature(operationArguments));
	}

}
