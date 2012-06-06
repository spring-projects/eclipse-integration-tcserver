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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jst.server.core.RuntimeClasspathProviderDelegate;
import org.eclipse.wst.server.core.IRuntime;

/**
 * @author Steffen Pingel
 */
public class TcServerRuntimeClasspathProvider extends RuntimeClasspathProviderDelegate {

	public IClasspathEntry[] resolveClasspathContainer(IProject project, IRuntime runtime) {
		IPath installPath = TcServerRuntime.getTomcatLocation(runtime);
		if (installPath == null) {
			return new IClasspathEntry[0];
		}

		List<IClasspathEntry> list = new ArrayList<IClasspathEntry>();
		IPath path = installPath.append("lib");
		addLibraryEntries(list, path.toFile(), true);
		return list.toArray(new IClasspathEntry[list.size()]);
	}

}
