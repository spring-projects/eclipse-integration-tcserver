/*******************************************************************************
 * Copyright (c) 2012, 2018 Pivotal Software, Inc.
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
import java.io.FileNotFoundException;
import java.util.Scanner;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jst.server.tomcat.core.internal.TomcatServer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeWorkingCopy;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerUtil;
import org.eclipse.wst.server.core.internal.ServerPlugin;
import org.eclipse.wst.server.core.internal.ServerWorkingCopy;
import org.springsource.ide.eclipse.commons.core.StatusHandler;

/**
 * @author Steffen Pingel
 * @author Tomasz Zarna
 */
public class TcServerUtil {

	@Deprecated
	public static boolean isSpringSource(IRuntimeWorkingCopy wc) {
		return wc != null && wc.getRuntimeType() != null && wc.getRuntimeType().getId().startsWith("com.springsource");
	}

	@Deprecated
	public static boolean isVMWare(IRuntimeWorkingCopy wc) {
		return wc != null && wc.getRuntimeType() != null && wc.getRuntimeType().getId().startsWith("com.vmware");
	}

	public static String getServerVersion(IRuntime runtime) {
		ITcRuntime tcRuntime = getTcRuntime(runtime);
		String directory = tcRuntime.getTomcatLocation().lastSegment();
		return getServerVersion(directory);
	}

	public static String getServerVersion(String tomcatFolerName) {
		return (tomcatFolerName != null && tomcatFolerName.startsWith("tomcat-")) ? tomcatFolerName.substring(7)
				: tomcatFolerName;
	}

	public static void importRuntimeConfiguration(IServerWorkingCopy wc, IProgressMonitor monitor) throws CoreException {
		// invoke tc Server API directly since
		// TcServer.importRuntimeConfiguration() swallows exceptions
		((TcServer) ((ServerWorkingCopy) wc).getWorkingCopyDelegate(monitor)).importRuntimeConfigurationChecked(
				wc.getRuntime(), monitor);
	}

	public static IStatus validateInstance(File instanceDirectory, boolean tcServer21orLater) {
		if (tcServer21orLater) {
			if (!new File(instanceDirectory, ".tc-runtime-instance").exists()) {
				return new Status(IStatus.ERROR, ITcServerConstants.PLUGIN_ID,
						"The specified server is not valid. The .tc-runtime-instance file is missing.");
			}
		}

		File confDir = new File(instanceDirectory, "conf");
		if (!confDir.exists()) {
			return new Status(IStatus.ERROR, ITcServerConstants.PLUGIN_ID,
					"The specified server is not valid. The conf directory is missing.");
		}
		File confFile = new File(confDir, "server.xml");
		if (!confFile.exists()) {
			return new Status(IStatus.ERROR, ITcServerConstants.PLUGIN_ID,
					"The specified server is not valid. The server.xml file in the conf directory is missing.");
		}
		if (!confFile.canRead()) {
			return new Status(IStatus.ERROR, ITcServerConstants.PLUGIN_ID,
					"The specified server is not valid. The server.xml file in the conf directory is not readable.");
		}
		return Status.OK_STATUS;
	}

	public static void executeInstanceCreation(IRuntime runtime, String instanceName, String[] arguments)
			throws CoreException {
		ITcRuntime tcRuntime = getTcRuntime(runtime);
		ServerInstanceCommand command = new ServerInstanceCommand(tcRuntime);

		// execute
		int returnCode;
		try {
			returnCode = command.execute(arguments);
		}
		catch (Exception e) {
			throw handleResult(tcRuntime.runtimeLocation(), command, arguments, new Status(IStatus.ERROR,
					ITcServerConstants.PLUGIN_ID, "The instance creation command resulted in an exception", e));
		}

		if (returnCode != 0) {
			throw handleResult(tcRuntime.runtimeLocation(), command, arguments, new Status(IStatus.ERROR,
					ITcServerConstants.PLUGIN_ID, "The instance creation command failed and returned code "
							+ returnCode));
		}

		// verify result
		IPath instanceDirectory = getInstanceDirectory(arguments, instanceName);
		if (instanceDirectory == null) {
			instanceDirectory = tcRuntime.instanceDirectory(instanceName);
		}
		IStatus status = validateInstance(instanceDirectory.toFile(), true);
		if (!status.isOK()) {
			throw handleResult(tcRuntime.runtimeLocation(), command, arguments, status);
		}
	}
	
	private static IPath getInstanceDirectory(String[] arguments, String instanceName) {
		for (int i = 0; i < arguments.length; i++) {
			if (arguments[i].equals("-i") && arguments[i + 1] != null) {
				return new Path(new File(arguments[i + 1], instanceName).toString());
			}
		}
		return null;
	}

	private static CoreException handleResult(IPath installLocation, ServerInstanceCommand command, String[] arguments,
			IStatus result) {
		StringBuilder cmdStr = new StringBuilder(command.toString());
		for (String arg : arguments) {
			cmdStr.append(' ');
			cmdStr.append(arg);
		}
		MultiStatus status = new MultiStatus(
				ITcServerConstants.PLUGIN_ID,
				0,
				NLS.bind(
						"Error creating server instance with command:\n \"{0}\". Check access permission for the directory {1} and its files and subdirectories.",
						new Object[] { cmdStr, installLocation }), null);
		if (result != null) {
			status.add(result);
		}
		IStatus output = new Status(IStatus.ERROR, ITcServerConstants.PLUGIN_ID,
				"Output of the instance creation command:\n" + command.getOutput());
		status.add(output);

		StatusHandler.log(status);

		return new CoreException(status);
	}

	public static String getInstanceTomcatVersion(File instanceFolder) {
		File tomcatVersionFile = new File(new File(instanceFolder, "conf"), "tomcat.version");
		Scanner scanner = null;
		try {
			if (tomcatVersionFile.exists()) {
				scanner = new Scanner(tomcatVersionFile);
				return scanner.useDelimiter("\\Z").next().trim();
			}
			else {
				return null;
			}
		}
		catch (FileNotFoundException e) {
			return null;
		}
		finally {
			if (scanner != null) {
				scanner.close();
			}
		}
	}

	public static File getInstanceDirectory(ServerWorkingCopy wc) {
		if (wc != null) {
			String instanceDir = wc.getAttribute(TomcatServer.PROPERTY_INSTANCE_DIR, (String) null);
			if (instanceDir != null) {
				File file = new File(instanceDir);
				if (file.exists()) {
					return file;
				}
			}
			String serverName = wc.getAttribute(TcServer.KEY_SERVER_NAME, (String) null);
			if (serverName != null) {
				ITcRuntime tcRuntime = getTcRuntime(wc.getRuntime());
				IPath path = tcRuntime == null ? wc.getRuntime().getLocation() : tcRuntime.instanceDirectory(serverName);
				File directory = path.toFile();
				if (directory.exists()) {
					return directory;
				}
			}
		}
		return null;
	}

	public static void setTcServerDefaultName(IServerWorkingCopy wc) {
		ServerUtil.setServerDefaultName(wc);
		String defaultName = wc.getName();
		String prefix = wc.getAttribute(TcServer.KEY_SERVER_NAME, (String) null);
		if (prefix != null && !prefix.isEmpty()) {
			String name = prefix + " - " + defaultName;
			int idx = name.lastIndexOf('(');
			if (idx != -1) {
				name = name.substring(0, idx).trim();
			}
			int i = 2;
			defaultName = name;
			while (ServerPlugin.isNameInUse(wc.getOriginal(), defaultName)) {
				defaultName = name + " (" + i + ")";
				i++;
			}
		}
		wc.setName(defaultName);
	}

	public static boolean isTcServerDefaultName(IServerWorkingCopy wc) {
		ServerWorkingCopy defaultServer = new ServerWorkingCopy(null, null, wc.getRuntime(), wc.getServerType());
		defaultServer.setAttribute(TcServer.KEY_SERVER_NAME, wc.getAttribute(TcServer.KEY_SERVER_NAME, (String) null));
		defaultServer.setDefaults(new NullProgressMonitor());
		return wc.getName().equals(defaultServer.getName());
	}

	static boolean isWindows() {
		return File.separatorChar == '\\';
	}
	
	public static ITcRuntime getTcRuntime(IRuntime runtime) {
		return (ITcRuntime) runtime.loadAdapter(ITcRuntime.class, new NullProgressMonitor());
	}

}
