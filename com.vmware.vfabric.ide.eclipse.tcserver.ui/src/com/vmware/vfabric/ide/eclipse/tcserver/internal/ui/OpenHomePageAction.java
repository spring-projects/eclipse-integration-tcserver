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
package com.vmware.vfabric.ide.eclipse.tcserver.internal.ui;

import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.ui.IServerModule;
import org.springsource.ide.eclipse.commons.ui.UiUtil;

import com.vmware.vfabric.ide.eclipse.tcserver.internal.core.TcServer;

/**
 * @author Steffen Pingel
 */
public class OpenHomePageAction implements IObjectActionDelegate {

	// private IWorkbenchPart targetPart;

	private IServer selectedServer;

	private IModule selectedModule;

	public OpenHomePageAction() {
	}

	public void run(IAction action) {
		if (selectedServer == null || selectedModule == null) {
			StatusManager.getManager().handle(
					new Status(IStatus.ERROR, TcServerUiPlugin.PLUGIN_ID, "No module selected."), StatusManager.SHOW);
		}

		TcServer server = (TcServer) selectedServer.loadAdapter(TcServer.class, null);
		if (server == null) {
			StatusManager.getManager().handle(
					new Status(IStatus.ERROR, TcServerUiPlugin.PLUGIN_ID, "Unable to load server delegate."),
					StatusManager.SHOW);
			return;
		}

		URL url = server.getModuleRootURL(selectedModule);
		if (url == null) {
			StatusManager.getManager().handle(
					new Status(IStatus.ERROR, TcServerUiPlugin.PLUGIN_ID, NLS.bind(
							"No valid URL specified for module ''{0}''.", selectedModule.getName())),
					StatusManager.LOG | StatusManager.SHOW);
			return;
		}

		try {
			UiUtil.openUrl(url.toURI().toString());
		}
		catch (URISyntaxException e) {
			StatusManager.getManager().handle(
					new Status(IStatus.ERROR, TcServerUiPlugin.PLUGIN_ID, NLS.bind(
							"Unexpected error while determining URL for module ''{0}''.", selectedModule.getName())),
					StatusManager.LOG | StatusManager.SHOW);
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
		selectedServer = null;
		selectedModule = null;
		if (!selection.isEmpty()) {
			if (selection instanceof IStructuredSelection) {
				Object obj = ((IStructuredSelection) selection).getFirstElement();
				if (obj instanceof IServer) {
					selectedServer = (IServer) obj;
				}
				else if (obj instanceof IServerModule) {
					IServerModule sm = (IServerModule) obj;
					IModule[] module = sm.getModule();
					selectedModule = module[module.length - 1];
					if (selectedModule != null) {
						selectedServer = sm.getServer();
					}
				}
			}
		}

		if (selectedServer != null) {
			if (selectedServer.getServerState() == IServer.STATE_STARTED) {
				action.setEnabled(true);
				return;
			}
		}
		action.setEnabled(false);
	}

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		// this.targetPart = targetPart;
	}

}
