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
package com.vmware.vfabric.ide.eclipse.tcserver.reloading;

import java.io.File;
import java.net.URL;
import java.util.Enumeration;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;

/**
 * @author Christian Dupuis
 */
public class TcServerReloadingPlugin extends Plugin {

	private static final String PLUGIN_ID = "com.vmware.vfabric.ide.eclipse.tcserver.reloading";

	private static TcServerReloadingPlugin plugin;

	private static String agentJarPath;

	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
	}

	@SuppressWarnings("unchecked")
	public synchronized static String getAgentJarPath() {
		if (agentJarPath == null) {
			Enumeration<URL> libs = plugin.getBundle().findEntries("/lib", "springloaded-*.jar", false);
			while (libs.hasMoreElements()) {
				try {
					URL nextLib = libs.nextElement();
					agentJarPath = new File(FileLocator.toFileURL(nextLib).getPath()).getCanonicalPath();
				}
				catch (Exception e) {
					plugin.getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, "Error loading tc Server Agent Jar", e));
				}
			}
		}
		return agentJarPath;
	}

}
