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
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jst.server.tomcat.core.internal.Tomcat60Handler;
import org.eclipse.jst.server.tomcat.core.internal.TomcatPlugin;
import org.eclipse.jst.server.tomcat.core.internal.TomcatServer;
import org.eclipse.jst.server.tomcat.core.internal.TomcatVersionHelper;
import org.eclipse.jst.server.tomcat.core.internal.VerifyResourceSpec;
import org.eclipse.wst.server.core.IModule;

/**
 * @author Steffen Pingel
 * @author Christian Dupuis
 * @author Terry Denney
 */
public class TcServerVersionHandler extends Tomcat60Handler {

	private static String VERIFY_SPEC_2_0 = "tijars";

	private static String VERIFY_SPEC_2_5 = "tcruntime-ctl.sh,lib,templates";

	private static String ID_TOMCAT_RUNTIME_60 = "org.eclipse.jst.server.tomcat.runtime.60";

	private static String ID_TOMCAT_RUNTIME_70 = "org.eclipse.jst.server.tomcat.runtime.70";

	private final String runtimeId;

	public TcServerVersionHandler(String runtimeId) {
		this.runtimeId = runtimeId;
	}

	/**
	 * Returns the instance specific directory. This is either the temporary
	 * directory created by webtools or the directory of a specific tc Server
	 * instance.
	 */
	@Override
	public IPath getRuntimeBaseDirectory(TomcatServer server) {
		if (server.isTestEnvironment()) {
			return super.getRuntimeBaseDirectory(server);
		}
		else {
			// for tc Server the runtime specifies the top-level tc Server
			// directory and not
			// catalina home, returns the instance directory here
			return ((TcServer) server).getInstanceBase(server.getServer().getRuntime());
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List getRuntimeClasspath(IPath installPath) {
		List cp = new ArrayList();
		IPath binPath = installPath.append("bin");
		if (binPath.toFile().exists()) {
			IPath path = binPath.append("bootstrap.jar");
			cp.add(JavaRuntime.newArchiveRuntimeClasspathEntry(path));

			// Since tc Server 6.0.20.C we need to manually add the
			// tomcat-juli.jar
			path = binPath.append("tomcat-juli.jar");
			if (path.toFile().exists()) {
				cp.add(JavaRuntime.newArchiveRuntimeClasspathEntry(path));
			}
		}
		return cp;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List getRuntimeClasspath(IPath installPath, IPath configPath) {
		List cp = new ArrayList();
		IPath binPath = installPath.append("bin");
		if (binPath.toFile().exists()) {
			IPath path = binPath.append("bootstrap.jar");
			cp.add(JavaRuntime.newArchiveRuntimeClasspathEntry(path));

			// Since tc Server 6.0.20.C we need to manually add the
			// tomcat-juli.jar
			path = binPath.append("tomcat-juli.jar");
			if (path.toFile().exists()) {
				cp.add(JavaRuntime.newArchiveRuntimeClasspathEntry(path));
			}
			else {
				// tc Server 2.1 puts this into the instance directory
				path = configPath.append("bin/tomcat-juli.jar");
				if (path.toFile().exists()) {
					cp.add(JavaRuntime.newArchiveRuntimeClasspathEntry(path));
				}
			}
		}
		return cp;
	}

	// @Override
	// public String[] getRuntimeVMArguments(IPath installPath, IPath
	// configPath, IPath deployPath, boolean isTestEnv) {
	// List<String> args = new ArrayList<String>();
	// // see setenv.sh
	// args.add("-Xmx512m");
	// args.add("-Xss192k");
	// args.addAll(Arrays.asList(super.getRuntimeVMArguments(installPath,
	// configPath, deployPath, isTestEnv)));
	// return args.toArray(new String[args.size()]);
	// }

	@Override
	public boolean supportsServeModulesWithoutPublish() {
		return true;
	}

	public IStatus prepareForServingDirectly(IPath baseDir, TomcatServer server, String tomcatVersion) {
		// obtain version based on actual catalina.home
		IPath tomcatLocation = ((TcServer) server).getTomcatRuntime().getTomcatLocation();
		String runtimeTypeId = server.getServer().getRuntime().getRuntimeType().getId();
		String serverTypeId = server.getServer().getServerType().getId();
		tomcatVersion = TomcatVersionHelper.getCatalinaVersion(tomcatLocation,
				mapToTomcatServerId(tomcatLocation, serverTypeId));

		IStatus status;
		if (server.isServeModulesWithoutPublish()) {
			status = TomcatVersionHelper.copyLoaderJar(getRuntimeBaseDirectory(server).append("lib"),
					mapToTomcatRuntimeId(tomcatLocation, runtimeTypeId), tomcatVersion);
			if (status.isOK() && server.isTestEnvironment()) {
				status = TomcatVersionHelper.updatePropertiesToServeDirectly(baseDir, "lib", "common");
			}
		}
		else {
			TomcatVersionHelper.removeLoaderJar(getRuntimeBaseDirectory(server).append("lib"),
					mapToTomcatRuntimeId(tomcatLocation, runtimeTypeId), tomcatVersion);
			status = Status.OK_STATUS;
		}
		return status;
	}

	private String mapToTomcatServerId(IPath tomcatLocation, String id) {
		if (id.equals(TcServer.ID_TC_SERVER_2_0)) {
			return TomcatPlugin.TOMCAT_60;
		}
		else {
			if (tomcatLocation.lastSegment().startsWith("tomcat-6")) {
				// catalina.base points to Tomcat 6 runtime
				return TomcatPlugin.TOMCAT_60;
			}
			else {
				return TomcatPlugin.TOMCAT_70;
			}
		}
	}

	private String mapToTomcatRuntimeId(IPath tomcatLocation, String id) {
		if (id.equals(TcServerRuntime.ID_TC_SERVER_2_0)) {
			return ID_TOMCAT_RUNTIME_60;
		}
		else {
			if (tomcatLocation.lastSegment().startsWith("tomcat-6")) {
				// catalina.base points to Tomcat 6 runtime
				return ID_TOMCAT_RUNTIME_60;
			}
			else {
				return ID_TOMCAT_RUNTIME_70;
			}
		}
	}

	@Override
	public IStatus verifyInstallPath(IPath installPath) {
		if (isVersion_2_5()) {
			return checkResource(VERIFY_SPEC_2_5, installPath);
		}
		else {
			IStatus status = TomcatPlugin.verifyTomcatVersionFromPath(installPath, TomcatPlugin.TOMCAT_60);
			if (status.isOK()) {
				status = TomcatPlugin.verifyInstallPath(installPath, TomcatPlugin.TOMCAT_60);
				if (!status.isOK()) {
					return checkResource(VERIFY_SPEC_2_0, installPath);
				}
			}
			return status;
		}
	}

	private IStatus checkResource(String spec, IPath installPath) {
		String dir = installPath.toOSString();
		if (!dir.endsWith(File.separator)) {
			dir += File.separator;
		}
		String[] array = spec.split(",");
		for (String value : array) {
			VerifyResourceSpec resourceSpec = new VerifyResourceSpec(value);
			IStatus result = resourceSpec.checkResource(dir);
			if (!result.isOK()) {
				return result;
			}
		}
		return Status.OK_STATUS;
	}

	public boolean isVersion_2_5() {
		return runtimeId.endsWith("70");
	}

	public boolean supportsServlet30() {
		return isVersion_2_5();
	}

	@Override
	public IStatus canAddModule(IModule module) {
		if (isVersion_2_5()) {
			String version = module.getModuleType().getVersion();
			if ("2.2".equals(version) || "2.3".equals(version) || "2.4".equals(version) || "2.5".equals(version)
					|| "3.0".equals(version)) {
				return Status.OK_STATUS;
			}
		}
		return super.canAddModule(module);
	}

}
