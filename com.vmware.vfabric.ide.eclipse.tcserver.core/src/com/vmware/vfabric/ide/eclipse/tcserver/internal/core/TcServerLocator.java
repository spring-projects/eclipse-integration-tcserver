/*******************************************************************************
 * Copyright (c) 2012 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package com.vmware.vfabric.ide.eclipse.tcserver.internal.core;

import java.io.File;
import java.io.FileFilter;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jst.server.tomcat.core.internal.ITomcatRuntimeWorkingCopy;
import org.eclipse.jst.server.tomcat.core.internal.TomcatRuntimeLocator;
import org.eclipse.jst.server.tomcat.core.internal.Trace;
import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.IRuntimeWorkingCopy;
import org.eclipse.wst.server.core.IServerType;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.internal.provisional.ServerLocatorDelegate;
import org.eclipse.wst.server.core.model.RuntimeLocatorDelegate.IRuntimeSearchListener;

/**
 * @author Steffen Pingel
 * @author Christian Dupuis
 */
public class TcServerLocator extends ServerLocatorDelegate {

	protected static IRuntimeWorkingCopy getRuntimeFromDir(String runtimeId, File dir, IProgressMonitor monitor) {
		try {
			IRuntimeType runtimeType = ServerCore.findRuntimeType(runtimeId);
			String absolutePath = dir.getAbsolutePath();
			IRuntimeWorkingCopy runtime = runtimeType.createRuntime(runtimeId, monitor);
			runtime.setName(dir.getName());
			runtime.setLocation(new Path(absolutePath));
			ITomcatRuntimeWorkingCopy wc = (ITomcatRuntimeWorkingCopy) runtime.loadAdapter(
					ITomcatRuntimeWorkingCopy.class, null);
			wc.setVMInstall(JavaRuntime.getDefaultVMInstall());
			IStatus status = runtime.validate(monitor);
			if (status == null || status.getSeverity() != IStatus.ERROR) {
				return runtime;
			}
		}
		catch (CoreException e) {
		}
		return null;
	}

	protected static void searchDir(IRuntimeSearchListener listener, File dir, int depth, IProgressMonitor monitor) {
		if ("conf".equals(dir.getName())) {
			IRuntimeWorkingCopy runtime = getRuntimeFromDir(TcServerRuntime.ID_TC_SERVER_2_5, dir.getParentFile(),
					monitor);
			if (runtime != null) {
				listener.runtimeFound(runtime);
				return;
			}
		}

		if (depth == 0) {
			return;
		}

		File[] files = dir.listFiles(new FileFilter() {
			public boolean accept(File file) {
				return file.isDirectory();
			}
		});
		if (files != null) {
			int size = files.length;
			for (int i = 0; i < size; i++) {
				if (monitor.isCanceled()) {
					return;
				}
				searchDir(listener, files[i], depth - 1, monitor);
			}
		}
	}

	protected static void searchForRuntimes2(IPath path, IRuntimeSearchListener listener, IProgressMonitor monitor) {
		File[] files = null;
		if (path != null) {
			File f = path.toFile();
			if (f.exists()) {
				files = f.listFiles();
			}
			else {
				return;
			}
		}
		else {
			files = File.listRoots();
		}

		if (files != null) {
			int size = files.length;
			int work = 100 / size;
			int workLeft = 100 - (work * size);
			for (int i = 0; i < size; i++) {
				if (monitor.isCanceled()) {
					return;
				}
				if (files[i] != null && files[i].isDirectory()) {
					searchDir(listener, files[i], 4, monitor);
				}
				monitor.worked(work);
			}
			monitor.worked(workLeft);
		}
		else {
			monitor.worked(100);
		}
	}

	@Override
	public void searchForServers(String host, final IServerSearchListener listener, final IProgressMonitor monitor) {
		TomcatRuntimeLocator.IRuntimeSearchListener listener2 = new TomcatRuntimeLocator.IRuntimeSearchListener() {
			public void runtimeFound(IRuntimeWorkingCopy runtime) {
				String runtimeTypeId = runtime.getRuntimeType().getId();
				String serverTypeId = runtimeTypeId.substring(0, runtimeTypeId.length() - 8);
				IServerType serverType = ServerCore.findServerType(serverTypeId);
				try {
					IServerWorkingCopy server = serverType.createServer(serverTypeId, null, runtime, monitor);
					listener.serverFound(server);
				}
				catch (Exception e) {
					Trace.trace(Trace.WARNING, "Could not create Tomcat server", e);
				}
			}
		};
		searchForRuntimes2(null, listener2, monitor);
	}

}
