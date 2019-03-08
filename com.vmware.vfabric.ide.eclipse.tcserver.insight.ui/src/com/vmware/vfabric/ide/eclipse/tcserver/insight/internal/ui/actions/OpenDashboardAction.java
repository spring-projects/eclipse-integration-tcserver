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
package com.vmware.vfabric.ide.eclipse.tcserver.insight.internal.ui.actions;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerPort;
import org.springsource.ide.eclipse.commons.ui.UiUtil;

import com.vmware.vfabric.ide.eclipse.tcserver.insight.internal.ui.Activator;
import com.vmware.vfabric.ide.eclipse.tcserver.insight.internal.ui.TcServerInsightUtil;
import com.vmware.vfabric.ide.eclipse.tcserver.internal.core.TcServerBehaviour;

/**
 * Action handler that opens the Spring Insight web dashboard.
 * @author Christian Dupuis
 * @author Steffen Pingel
 * @since 2.2.0
 */
@SuppressWarnings("restriction")
public class OpenDashboardAction implements IObjectActionDelegate {

	private IServer server;

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public void run(IAction action) {
		run(server);
	}

	public void run(IServer server) {
		try {
			TcServerBehaviour behaviour = (TcServerBehaviour) server.loadAdapter(TcServerBehaviour.class, null);
			List<ServerPort> ports = behaviour.getTomcatConfiguration().getServerPorts();
			if (server.getServerState() == IServer.STATE_STARTED) {
				for (ServerPort port : ports) {
					if ("HTTP".equals(port.getProtocol())) {
						UiUtil.openUrl("http://" + server.getHost() + ":" + port.getPort() + Activator.CONTEXT);
						break;
					}
					else if ("HTTPS".equals(port.getProtocol())) {
						UiUtil.openUrl("https://" + server.getHost() + ":" + port.getPort() + Activator.CONTEXT);
						break;
					}
				}
			}
		}
		catch (CoreException e) {
			Activator.log(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		server = null;
		if (!selection.isEmpty()) {
			if (selection instanceof IStructuredSelection) {
				Object obj = ((IStructuredSelection) selection).getFirstElement();
				if (obj instanceof IServer) {
					server = (IServer) obj;
				}
			}
		}
		action.setEnabled(isEnabled(server));
	}

	public boolean isEnabled(IServer server) {
		return server != null && server.getServerState() == IServer.STATE_STARTED
				&& TcServerInsightUtil.isInsightRunning(server);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		// nothing to do here
	}

}
