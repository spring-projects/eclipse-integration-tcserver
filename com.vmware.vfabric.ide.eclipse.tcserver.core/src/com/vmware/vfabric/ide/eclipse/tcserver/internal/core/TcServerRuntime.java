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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;
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
public class TcServerRuntime extends TomcatRuntime implements ITcRuntime {
	
	private static final String INSTANCE_CREATION_SCRIPT = "tcruntime-instance";
	
	protected static final String UNIX_SUFFIX = ".sh";

	protected static final String WINDOWS_SUFFIX = ".bat";

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

	public static String ID_TC_SERVER_2_5 = "com.vmware.server.tc.runtime.70";

	public static String ID_TC_SERVER_3_0 = "com.pivotal.server.tc.runtime.80";

	protected static final String TEMPLATES_FOLDER = "templates";

	protected static final String TEMPLATE_VARIATION_STR = "-tomcat-";

	protected static final Pattern TEMPLATE_PATTERN = Pattern.compile("(.*)-tomcat-(.*)");

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
		IPath installPath = getTomcatServersContainer();
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
			String serverVersion = ((Runtime) getRuntime()).getAttribute(KEY_SERVER_VERSION, (String) null);
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

	public static IVMInstall getVM(IRuntime runtime) {
		String vmInstallTypeId = ((Runtime) runtime).getAttribute(PROP_VM_INSTALL_TYPE_ID, (String) null);
		if (vmInstallTypeId == null) {
			return JavaRuntime.getDefaultVMInstall();
		}
		try {
			IVMInstallType vmInstallType = JavaRuntime.getVMInstallType(vmInstallTypeId);
			IVMInstall[] vmInstalls = vmInstallType.getVMInstalls();
			String id = ((Runtime) runtime).getAttribute(PROP_VM_INSTALL_ID, (String) null);
			int size = vmInstalls.length;
			for (int i = 0; i < size; i++) {
				if (id.equals(vmInstalls[i].getId())) {
					return vmInstalls[i];
				}
			}
		}
		catch (Exception e) {
			// ignore
		}
		return null;
	}

	public boolean supportsServlet30() {
		return TcServer.isVersion25(getRuntime()) || TcServer.isVersion30(getRuntime());
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

			File f = new File(runtimeLocation().toFile(), "conf");
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
			return new Status(status.getSeverity(), ITcServerConstants.PLUGIN_ID, status.getMessage().replace(
					"Tomcat version 7.0", "tc Server v2.5 or later"));
		}

		if (ID_TC_SERVER_3_0.equals(getRuntime().getRuntimeType().getId())) {
			if (!status.isOK() && status.getMessage().contains("8.0") && status.getMessage().contains("Java SE 7")) {
				String tomcatStr = getAttribute(TcServerRuntime.KEY_SERVER_VERSION, (String) null);
				if (tomcatStr != null) {
					String version = TcServerUtil.getServerVersion(tomcatStr);
					if (version.startsWith("7")) {
						return Status.OK_STATUS;
					}
				}
			}
		}
		return status;
	}

	/**
	 * Returns a set of templates for the current runtime
	 *
	 * @return set of template strings
	 */
	public Set<String> getTemplates() {
		Set<String> templates = new HashSet<String>();
		IPath runtimePath = runtimeLocation();
		File templatePath = new File(runtimePath.toFile(), TEMPLATES_FOLDER);
		if (templatePath.exists()) {
			File[] children = templatePath.listFiles();
			if (children != null) {
				for (File child : children) {
					if (child.isDirectory()) {
						Matcher matcher = TEMPLATE_PATTERN.matcher(child.getName());
						if (matcher.matches()) {
							String template = matcher.group(1);
							String version = matcher.group(2);
							if (TcServerUtil.getServerVersion(getTomcatLocation().lastSegment()).startsWith(version)) {
								templates.add(template);
							}
						}
						else {
							templates.add(child.getName());
						}
					}
				}
			}
		}
		return templates;
	}

	/**
	 * Returns the folder for the template
	 *
	 * @param templateName
	 * @return template's folder
	 */
	public File getTemplateFolder(String templateName) {
		StringBuilder templatePath = new StringBuilder();
		templatePath.append(runtimeLocation());
		templatePath.append(File.separator);
		templatePath.append(TEMPLATES_FOLDER);
		templatePath.append(File.separator);
		templatePath.append(templateName);
		File templateFolder = new File(templatePath.toString());
		if (!templateFolder.exists() || !templateFolder.isDirectory()) {
			templateFolder = null;
			String serverVersion = TcServerUtil.getServerVersion(getTomcatLocation().lastSegment());
			if (serverVersion != null && !serverVersion.isEmpty()) {
				templateFolder = null;
				int idx = serverVersion.indexOf('.');
				String majorVersion = idx > -1 ? serverVersion.substring(0, idx) : serverVersion;
				templatePath.append(TEMPLATE_VARIATION_STR);
				templatePath.append(majorVersion);
				templateFolder = new File(templatePath.toString());
				if (!templateFolder.exists() && !templateFolder.isDirectory()) {
					templateFolder = null;
				}
			}
		}
		return templateFolder;
	}

	@Override
	public IPath runtimeLocation() {
		return getRuntime().getLocation();
	}

	@Override
	public IPath instanceCreationScript() {
		return runtimeLocation().append(INSTANCE_CREATION_SCRIPT + (TcServerUtil.isWindows() ? WINDOWS_SUFFIX : UNIX_SUFFIX));
	}
	
	@Override
	public IPath instanceDirectory(String instanceName) {
		return defaultInstancesDirectory().append(instanceName);
	}

	@Override
	public IPath getTomcatServersContainer() {
		return getRuntime().getLocation();
	}

	@Override
	public IPath defaultInstancesDirectory() {
		return runtimeLocation();
	}

	
}
