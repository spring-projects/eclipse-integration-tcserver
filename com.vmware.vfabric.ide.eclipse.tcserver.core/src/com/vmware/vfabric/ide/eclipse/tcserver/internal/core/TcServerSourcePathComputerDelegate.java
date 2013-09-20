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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jst.server.tomcat.core.internal.TomcatSourcePathComputerDelegate;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerUtil;

/**
 * Specialized version of TomcatSourcePathComputerDelegate, to ensure that
 * grails.app projects will get their source code added to the source code
 * lookup.
 * @author Kris De Volder
 * @author Christian Dupuis
 * @since 2.5.2
 */
public class TcServerSourcePathComputerDelegate extends TomcatSourcePathComputerDelegate {

	@Override
	public ISourceContainer[] computeSourceContainers(ILaunchConfiguration configuration, IProgressMonitor monitor)
			throws CoreException {
		ISourceContainer[] superContainers = super.computeSourceContainers(configuration, monitor);

		// Super doesn't handle "grails.app" modules. So we must handle them.
		IServer server = ServerUtil.getServer(configuration);
		// TODO: KDV: instead of putting code like below in here to support
		// grails.app modules. It would be nicer if we made
		// it possible to contribute a handler for a module type / version... so
		// that this kind of code can be placed in the
		// plugin that defines the module type.

		IModule[] modules = server.getModules();
		List<IProject> grailsProjects = new ArrayList<IProject>();
		for (IModule m : modules) {
			if (ITcServerConstants.GRAILS_APP_MODULE_TYPE.equals(m.getModuleType().getId())) {
				grailsProjects.add(m.getProject());
			}
		}
		ISourceContainer[] allSourceContainers;
		if (grailsProjects.size() == 0) {
			allSourceContainers = superContainers;
		}
		else {
			ArrayList<ISourceContainer> containers = new ArrayList<ISourceContainer>(Arrays.asList(superContainers));
			IRuntimeClasspathEntry[] projectEntries = new IRuntimeClasspathEntry[grailsProjects.size()];
			int i = 0;
			for (IProject p : grailsProjects) {
				IJavaProject javaProject = JavaCore.create(p);
				projectEntries[i++] = JavaRuntime.newDefaultProjectClasspathEntry(javaProject);
			}
			IRuntimeClasspathEntry[] sourceEntries = JavaRuntime.resolveSourceLookupPath(projectEntries, configuration);
			containers.addAll(Arrays.asList(JavaRuntime.getSourceContainers(sourceEntries)));
			allSourceContainers = containers.toArray(new ISourceContainer[containers.size()]);
		}
		return allSourceContainers;
	}

}
