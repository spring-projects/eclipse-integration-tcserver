/*******************************************************************************
 * Copyright (c) 2012 - 2013 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package com.vmware.vfabric.ide.eclipse.tcserver.insight.internal.ui;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.osgi.service.resolver.VersionRange;
import org.eclipse.wst.server.core.IServer;
import org.osgi.framework.Version;
import org.springsource.ide.eclipse.commons.core.StatusHandler;

import com.vmware.vfabric.ide.eclipse.tcserver.internal.core.TcServer;

/**
 * @author Steffen Pingel
 * @author Leo Dos Santos
 */
public class TcServerInsightUtil {

	public static String DISABLED_INSIGHT = "-Dinsight.enabled=false";

	/**
	 * Returns true, if the instance webapps directory for <code>server</code>
	 * contains insight.
	 */
	public static boolean hasInsight(IServer server) {
		TcServer tcServer = (TcServer) server.loadAdapter(TcServer.class, null);
		IPath runtimeBaseDirectory = getInsightBase(tcServer);
		if (runtimeBaseDirectory != null) {
			IPath path = runtimeBaseDirectory.append("webapps").append("insight.war");
			return path.toFile().exists();
		}
		return false;
	}

	/**
	 * See https://issuetracker.springsource.com/browse/STS-1541.
	 */
	public static boolean isInsightCompatible(IServer server) {
		TcServer tcServer = (TcServer) server.loadAdapter(TcServer.class, null);
		IPath runtimeBaseDirectory = getInsightBase(tcServer);
		if (!runtimeBaseDirectory.toPortableString().contains(" ")) {
			// nothing to do if the directory does not contain spaces
			return true;
		}
		if (runtimeBaseDirectory != null) {
			IPath path = runtimeBaseDirectory.append("lib");
			File directory = path.toFile();
			if (directory.exists()) {
				String[] filenames = directory.list();
				if (filenames != null) {
					for (String filename : filenames) {
						if (filename.startsWith("aspectjweaver-") && filename.endsWith(".jar")) {
							String versionString = filename.substring("aspectjweaver-".length(), filename.length()
									- ".jar".length());
							try {
								// 1.6.9 and smaller are affected
								Version version = new Version(versionString);
								if (new VersionRange("[0.0.0, 1.6.10]").isIncluded(version)) {
									return false;
								}
							}
							catch (IllegalArgumentException e) {
								// let it slip
							}
						}
					}
				}
			}
		}
		// default to false
		return true;
	}

	// insight is now copied to workspace specific instances, see
	// InsightTcServerCallback.publishServer()
	// /**
	// * Returns the base path of the instance for <code>tcServer</code>.
	// Insight
	// * is not copied when server configuration is loaded from the workspace
	// * therefore it's always referenced from the instance directory.
	// */
	// public static IPath getInsightBase(TcServer tcServer) {
	// return tcServer.getInstanceBase(tcServer.getServer().getRuntime());
	// }

	public static IPath getInsightBase(TcServer tcServer) {
		return tcServer.getRuntimeBaseDirectory();
	}

	public static IPath getInsightPath(IServer server) {
		TcServer tcServer = (TcServer) server.loadAdapter(TcServer.class, null);
		IPath runtimeBaseDirectory = getInsightBase(tcServer);
		if (runtimeBaseDirectory != null) {
			IPath path = runtimeBaseDirectory.append("insight");
			return (path.toFile().exists()) ? path : null;
		}
		return null;
	}

	/**
	 * Returns true, if insight is enabled in the launch configuration for the
	 * server.
	 */
	public static boolean isInsightEnabled(TcServer server) {
		return !server.getAddExtraVmArgs().contains(TcServerInsightUtil.DISABLED_INSIGHT);
	}

	/**
	 * Returns true, if <code>server</code> is running and insight was enabled
	 * in the last launch.
	 */
	public static boolean isInsightRunning(IServer server) {
		ILaunch launch = server.getLaunch();
		if (launch != null) {
			String vmArgs = launch.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS);
			if (vmArgs != null) {
				return !vmArgs.contains(TcServerInsightUtil.DISABLED_INSIGHT);
			}
		}
		return false;
	}

	public static String getAgentJarPath(TcServer tcServer) {
		IPath baseDir = getInsightBase(tcServer);
		if (baseDir != null) {
			IPath binPath = baseDir.append("bin");
			File binDir = binPath.toFile();
			if (binDir.exists()) {
				String[] fileNames = binDir.list();
				if (fileNames != null) {
					for (String fileName : fileNames) {
						if (fileName.startsWith("insight-weaver") && fileName.endsWith(".jar")) {
							File agent = new File(binDir, fileName);
							if (agent.exists()) {
								try {
									return agent.getCanonicalPath();
								}
								catch (IOException e) {
									StatusHandler.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
											"Could not get path to insight-weaver agent.", e));
								}
							}
						}
					}
				}
			}
		}
		return null;
	}

}
