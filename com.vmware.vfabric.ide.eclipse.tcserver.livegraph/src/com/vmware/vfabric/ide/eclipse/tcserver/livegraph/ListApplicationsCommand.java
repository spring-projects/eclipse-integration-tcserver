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
package com.vmware.vfabric.ide.eclipse.tcserver.livegraph;

import java.io.IOException;
import java.util.Set;

import javax.management.JMException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import com.vmware.vfabric.ide.eclipse.tcserver.internal.core.AbstractJmxServerCommand;
import com.vmware.vfabric.ide.eclipse.tcserver.internal.core.TcServerBehaviour;

/**
 * @author Leo Dos Santos
 */
public class ListApplicationsCommand extends AbstractJmxServerCommand<Set> {

	public ListApplicationsCommand(TcServerBehaviour serverBehaviour, boolean logStatusErrors) {
		super(serverBehaviour, logStatusErrors);
	}

	public ListApplicationsCommand(TcServerBehaviour serverBehaviour) {
		super(serverBehaviour);
	}

	@Override
	protected Set doOperation(MBeanServerConnection connection) throws IOException, JMException {
		String service = serverBehaviour.getTomcatServer().getDeployerService();
		String host = serverBehaviour.getTomcatServer().getDeployerHost();
		Object[] arguments = new Object[] { service, host };
		ObjectName name = ObjectName.getInstance("tcServer:type=Serviceability,name=Deployer");
		return (Set) connection.invoke(name, "listApplications", arguments, getSignature(arguments));
	}

}
