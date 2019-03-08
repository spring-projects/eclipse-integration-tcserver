/*******************************************************************************
 * Copyright (c) 2018 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package com.vmware.vfabric.ide.eclipse.tcserver.internal.core;

import java.io.File;
import java.io.FilenameFilter;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * tc server runtime for tc server 4.0
 * 
 * @author Alex Boyko
 *
 */
public class TcServerRuntime40 extends TcServerRuntime {
	
	private static final String INSTANCE_CREATION_SCRIPT = "tcserver";

	@Override
	public boolean supportsServlet30() {
		return true;
	}

	public static IPath getTcServerRuntimePath(IPath installPath) {
		File[] files = installPath.toFile().listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return  (
							Pattern.matches("developer-\\d\\.\\d\\.\\d\\.(RELEASE|SNAPSHOT-BUILD)", name)
								|| Pattern.matches("standard-\\d\\.\\d\\.\\d\\.(RELEASE|SNAPSHOT-BUILD)", name)
						) 
						&& new File(dir, name).isDirectory();
			}
		});
		return files != null && files.length > 0 ? Path.fromOSString(files[0].toString()) : installPath;
	}

	@Override
	public IPath runtimeLocation() {
		return getTcServerRuntimePath(getRuntime().getLocation());
	}

	@Override
	public IPath instanceCreationScript() {
		return runtimeLocation().append(INSTANCE_CREATION_SCRIPT + (TcServerUtil.isWindows() ? WINDOWS_SUFFIX : ""));
	}

	@Override
	public IPath getTomcatServersContainer() {
		return getRuntime().getLocation().append("runtimes");
	}
	
	@Override
	public IPath defaultInstancesDirectory() {
		return getRuntime().getLocation().append("instances");
	}

}
