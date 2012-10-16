/*******************************************************************************
 * Copyright (c) 2012 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package com.vmware.vfabric.ide.eclipse.tcserver.internal.ui;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jst.server.tomcat.core.internal.TomcatServer;
import org.eclipse.ui.ISources;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.ui.IServerModule;
import org.springsource.ide.eclipse.commons.core.StatusHandler;

public class BrowseDeploymentLocationHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IServer selectedServer = getSelectedServer(event);
		if (selectedServer == null) {
			return null;
		}

		File file = getServerDeployDirectory(selectedServer);
		if (file == null || !file.exists()) {
			return null;
		}
		URI uri = file.toURI();
		if (uri == null) {
			return null;
		}

		try {
			Desktop.getDesktop().browse(uri);
		}
		catch (IOException e) {
			StatusHandler.log(new Status(IStatus.ERROR, TcServerUiPlugin.PLUGIN_ID,
					"Failed to browse deployment location.", e));
		}

		return null;
	}

	@Override
	public void setEnabled(Object evaluationContext) {
		if (!Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
			setBaseEnabled(false);
			return;
		}

		IServer selectedServer = getSelectedServer(evaluationContext);
		if (selectedServer == null) {
			setBaseEnabled(false);
			return;
		}

		File file = getServerDeployDirectory(selectedServer);
		if (file == null || !file.exists()) {
			setBaseEnabled(false);
			return;
		}
		setBaseEnabled(true);
	}

	private File getServerDeployDirectory(IServer selectedServer) {
		TomcatServer tomcatServer = (TomcatServer) selectedServer.loadAdapter(TomcatServer.class, null);
		if (tomcatServer == null) {
			return null;
		}
		IPath deployDirectory = tomcatServer.getServerDeployDirectory();
		return deployDirectory.toFile();
	}

	private IServer getSelectedServer(Object evaluationContext) {
		Object selection = HandlerUtil.getVariable(evaluationContext, ISources.ACTIVE_CURRENT_SELECTION_NAME);
		if (selection instanceof ISelection) {
			return getSelectedServer((ISelection) selection);
		}
		return null;
	}

	private IServer getSelectedServer(ExecutionEvent event) {
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		return getSelectedServer(selection);
	}

	private IServer getSelectedServer(ISelection selection) {
		IServer selectedServer = null;
		if (!selection.isEmpty()) {
			if (selection instanceof IStructuredSelection) {
				Object obj = ((IStructuredSelection) selection).getFirstElement();
				if (obj instanceof IServer) {
					selectedServer = (IServer) obj;
				}
				else if (obj instanceof IServerModule) {
					IServerModule sm = (IServerModule) obj;
					selectedServer = sm.getServer();
				}
			}
		}
		return selectedServer;
	}
}
