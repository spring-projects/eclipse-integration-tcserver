/*******************************************************************************
 * Copyright (c) 2012 GoPivotal, Inc.
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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.management.JMException;
import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jst.server.tomcat.core.internal.TomcatPlugin;

/**
 * @author Steffen Pingel
 * @author Christian Dupuis
 * @author Leo Dos Santos
 */
public abstract class AbstractJmxServerCommand<T> {

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

	protected final boolean logStatus;

	public AbstractJmxServerCommand(TcServerBehaviour serverBehaviour) {
		this(serverBehaviour, true);
	}

	public AbstractJmxServerCommand(TcServerBehaviour serverBehaviour, boolean logStatusErrors) {
		this.serverBehaviour = serverBehaviour;
		this.logStatus = logStatusErrors;
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
					connector = JmxUtils.getJmxConnector(serverBehaviour);
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
					if (logStatus) {
						TomcatPlugin.log(status[0]);
					}
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

}
