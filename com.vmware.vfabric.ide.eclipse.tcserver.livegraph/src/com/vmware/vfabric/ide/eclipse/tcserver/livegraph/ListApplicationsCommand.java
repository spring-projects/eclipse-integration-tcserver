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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;
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
		Object result = connection.invoke(name, "listApplications", arguments, getSignature(arguments));

		// Filter out applications that do not support Live Beans feature.
		Set applications = new HashSet();
		if (result instanceof Set) {
			Set resultSet = (Set) result;
			Iterator iter = resultSet.iterator();
			while (iter.hasNext()) {
				Object obj = iter.next();
				if (obj instanceof Map) {
					Map attributes = (Map) obj;
					if (attributes.containsKey("baseName") && attributes.get("baseName") instanceof String) {
						String baseName = (String) attributes.get("baseName");
						ObjectInstance candidate = null;
						try {
							// Test the MBean's existence before proceeding.
							// Will throw InstanceNotFoundException
							ObjectName liveBean = ObjectName.getInstance("", "application", "/".concat(baseName));
							candidate = connection.getObjectInstance(liveBean);
						}
						catch (InstanceNotFoundException e) {
							// No MBean. Ignore.
						}
						if (candidate != null) {
							applications.add(obj);
						}
					}
				}
			}
		}
		return applications;
	}

}
