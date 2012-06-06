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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jst.server.tomcat.core.internal.Messages;
import org.eclipse.jst.server.tomcat.core.internal.TomcatPlugin;
import org.eclipse.jst.server.tomcat.core.internal.TomcatRuntime;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.internal.Runtime;

/**
 * @author Steffen Pingel
 * @author Christian Dupuis
 * @author Terry Denney
 */
public class TcServerRuntime extends TomcatRuntime {

	/**
	 * Sorts server directories. Higher versions have lower indices.
	 */
	private static class TcVersionComparator implements Comparator<File> {

		public int compare(File o1, File o2) {
			return new TcServerVersion(extractVersion(o2)).compareTo(new TcServerVersion(extractVersion(o1)));
		}

		private String extractVersion(File o) {
			String name = o.getName();
			return name.startsWith("tomcat-") ? name.substring(7) : "";
		}

	}

	public static String ID_TC_SERVER_2_0 = "com.springsource.tcserver.runtime.60";

	public static String ID_TC_SERVER_2_1 = "com.springsource.tcserver.runtime.70";

	public static String ID_TC_SERVER_2_5 = "com.vmware.server.tc.runtime.70";

	public static final String KEY_SERVER_VERSION = "com.springsource.tcserver.version";

	public static List<File> getTomcatVersions(File location) {
		List<File> results = new ArrayList<File>();
		File[] serverDirectories = location.listFiles();
		if (serverDirectories != null) {
			for (File directory : serverDirectories) {
				if (directory.isDirectory() && directory.getName().startsWith("tomcat-")
						&& new File(directory, "lib").exists()) {
					results.add(directory);
				}
			}
			Collections.sort(results, new TcVersionComparator());
		}
		return results;
	}

	// Eclipse 3.5 or earlier
	@SuppressWarnings("rawtypes")
	public List getRuntimeClasspath() {
		IPath installPath = getTomcatLocation();
		return (getVersionHandler()).getRuntimeClasspath(installPath);
	}

	// Eclipse 3.6 or later
	@SuppressWarnings("rawtypes")
	public List getRuntimeClasspath(IPath configPath) {
		IPath installPath = getTomcatLocation();
		return (getVersionHandler()).getRuntimeClasspath(installPath, configPath);
	}

	public IPath getTomcatLocation() {
		return getTomcatLocation(getRuntime());
	}

	/**
	 * Returns the <code>catalina.home</code> directory for <code>runtime</code>
	 * .
	 * 
	 * @return the path or null
	 */
	public static IPath getTomcatLocation(IRuntime runtime) {
		IPath installPath = runtime.getLocation();
		// If installPath is relative, convert to canonical path and hope for
		// the best
		if (!installPath.isAbsolute()) {
			try {
				String installLoc = (new File(installPath.toOSString())).getCanonicalPath();
				installPath = new Path(installLoc);
			}
			catch (IOException e) {
				// Ignore if there is a problem
			}
		}

		if (!installPath.append("lib").append("catalina.jar").toFile().exists()) {
			// search for Tomcat instance
			String serverVersion = ((Runtime) runtime).getAttribute(KEY_SERVER_VERSION, (String) null);
			if (serverVersion != null) {
				installPath = installPath.append(serverVersion);
			}
			else {
				// fall-back to latest server version
				List<File> serverDirectories = getTomcatVersions(installPath.toFile());
				if (serverDirectories.size() > 0) {
					installPath = installPath.append(serverDirectories.get(0).getName());
				}
			}
		}

		return installPath;
	}

	public boolean supportsServlet30() {
		return TcServer.isVersion25(getRuntime());
	}

	@Override
	public TcServerVersionHandler getVersionHandler() {
		return new TcServerVersionHandler(getRuntime().getRuntimeType().getId());
	}

	@Override
	public IStatus validate() {
		IStatus status = super.validate();
		if (status.getMessage().contains("Java SDK")) {
			// ignore, tc Server does not require a JRE to compile JSPs

			File f = getRuntime().getLocation().append("conf").toFile();
			File[] conf = f.listFiles();
			if (conf != null) {
				int size = conf.length;
				for (int i = 0; i < size; i++) {
					if (!f.canRead()) {
						return new Status(IStatus.WARNING, TomcatPlugin.PLUGIN_ID, 0, Messages.warningCantReadConfig,
								null);
					}
				}
			}
			return Status.OK_STATUS;
		}
		if (status.getMessage().contains("7.0") && status.getMessage().contains("Java SE 6")) {
			if (ID_TC_SERVER_2_1.equals(getRuntime().getRuntimeType().getId())) {
				// ignore warning about Java 6 requirement since tc Server 2.1
				// does not include Tomcat 7 and hence does not require Java 1.6
				return Status.OK_STATUS;
			}
			else {
				return new Status(status.getSeverity(), ITcServerConstants.PLUGIN_ID, status.getMessage().replace(
						"Tomcat version 7.0", "tc Server v2.5 or later"));
			}
		}
		return status;
	}

}
