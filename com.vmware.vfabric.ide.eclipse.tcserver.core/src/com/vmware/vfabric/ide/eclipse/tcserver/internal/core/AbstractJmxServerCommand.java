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
import java.util.Hashtable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.management.JMException;
import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jst.server.tomcat.core.internal.TomcatPlugin;

/**
 * @author Steffen Pingel
 * @author Christian Dupuis
 */
public abstract class AbstractJmxServerCommand<T> {

	private static final String JMX_CONNECTOR_URL = "service:jmx:rmi:///jndi/rmi://%s:%d/jmxrmi"; //$NON-NLS-1$

	public static String[] getSignature(Object[] operationArguments) {
		String[] classNames = new String[operationArguments.length];
		for (int i = 0; i < operationArguments.length; i++) {
			if (operationArguments[i] instanceof Boolean) {
				classNames[i] = boolean.class.getName();
			}
			else {
				classNames[i] = operationArguments[i].getClass().getName();
			}
		}
		return classNames;
	}

	protected final TcServerBehaviour serverBehaviour;

	public AbstractJmxServerCommand(TcServerBehaviour serverBehaviour) {
		this.serverBehaviour = serverBehaviour;
	}

	protected abstract T doOperation(MBeanServerConnection beanServerConnection) throws IOException, JMException;

	@SuppressWarnings("unchecked")
	public final T execute() throws TimeoutException, CoreException {
		final CountDownLatch resultLatch = new CountDownLatch(1);
		final Object[] result = new Object[1];
		final IStatus[] status = new IStatus[1];
		Job deployOperation = new Job("Executing Server Command") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				JMXConnector connector = null;
				try {
					connector = getJmxConnector();
					result[0] = doOperation(connector.getMBeanServerConnection());
				}
				catch (Exception e) {
					status[0] = new Status(IStatus.ERROR, ITcServerConstants.PLUGIN_ID, "Server command failed", e);
				}
				finally {
					resultLatch.countDown();
					if (connector != null) {
						try {
							connector.close();
						}
						catch (IOException e) {
							// ignore, the server may have already shutdown
							// TomcatPlugin.log(new Status(IStatus.ERROR,
							// ITcServerConstants.PLUGIN_ID,
							// "Failed to close server connection", e));
						}
					}
				}
				return Status.OK_STATUS;
			}
		};
		deployOperation.schedule();

		try {
			if (resultLatch.await(30, TimeUnit.SECONDS)) {
				if (status[0] != null) {
					TomcatPlugin.log(status[0]);
					throw new CoreException(status[0]);
				}
				return (T) result[0];
			}
		}
		catch (InterruptedException e) {
			// swallow exception here
		}
		return null;
	}

	private JMXConnector getJmxConnector() throws IOException {
		TcServer server = serverBehaviour.getTomcatServer();
		IServicabilityInfo info;
		try {
			info = serverBehaviour.getServicabilityInfo();
			if (info == null || !info.isValid()) {
				throw new IOException("JMX access is not configured for server");
			}
		}
		catch (CoreException e) {
			// TODO log exception
			throw new IOException("Configuration of JMX connection failed");
		}

		Hashtable<String, Object> h = new Hashtable<String, Object>();
		JmxCredentials credentials = info.getCredentials(server);
		if (credentials != null) {
			h.put("jmx.remote.credentials", new String[] { credentials.getUsername(), credentials.getPassword() });
		}

		String connectorUrl = String.format(JMX_CONNECTOR_URL, info.getHost(), Integer.parseInt(info.getPort()));
		return JMXConnectorFactory.connect(new JMXServiceURL(connectorUrl), h);
	}

}
