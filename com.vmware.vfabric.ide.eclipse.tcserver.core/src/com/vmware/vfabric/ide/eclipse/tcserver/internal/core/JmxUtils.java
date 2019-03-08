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
package com.vmware.vfabric.ide.eclipse.tcserver.internal.core;

import java.io.IOException;
import java.util.Hashtable;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.eclipse.core.runtime.CoreException;

/**
 * @author Leo Dos Santos
 */
public class JmxUtils {

	public static final String JMX_CONNECTOR_URL = "service:jmx:rmi:///jndi/rmi://%s:%d/jmxrmi"; //$NON-NLS-1$

	public static JMXConnector getJmxConnector(TcServerBehaviour behaviour) throws IOException {
		Hashtable<String, Object> h = new Hashtable<String, Object>();
		JmxCredentials credentials = getJmxCredentials(behaviour);
		if (credentials != null) {
			h.put("jmx.remote.credentials", new String[] { credentials.getUsername(), credentials.getPassword() });
		}
		String connectorUrl = getJmxUrl(behaviour);
		return JMXConnectorFactory.connect(new JMXServiceURL(connectorUrl), h);
	}

	public static String getJmxUrl(TcServerBehaviour behaviour) throws IOException {
		IServicabilityInfo info = getServicabilityInfo(behaviour);
		return String.format(JMX_CONNECTOR_URL, info.getHost(), Integer.parseInt(info.getPort()));
	}

	public static JmxCredentials getJmxCredentials(TcServerBehaviour behaviour) throws IOException {
		IServicabilityInfo info = getServicabilityInfo(behaviour);
		TcServer server = behaviour.getTomcatServer();
		return info.getCredentials(server);
	}

	private static IServicabilityInfo getServicabilityInfo(TcServerBehaviour behaviour) throws IOException {
		IServicabilityInfo info;
		try {
			info = behaviour.getServicabilityInfo();
			if (info == null || !info.isValid()) {
				throw new IOException("JMX access is not configured for server");
			}
		}
		catch (CoreException e) {
			// TODO log exception
			throw new IOException("Configuration of JMX connection failed");
		}
		return info;
	}

}
