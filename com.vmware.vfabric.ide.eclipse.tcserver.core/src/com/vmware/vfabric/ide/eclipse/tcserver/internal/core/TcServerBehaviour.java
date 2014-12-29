/*******************************************************************************
 * Copyright (c) 2012 - 2014 Pivotal Software, Inc.
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
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jst.server.tomcat.core.internal.FileUtil;
import org.eclipse.jst.server.tomcat.core.internal.Messages;
import org.eclipse.jst.server.tomcat.core.internal.PingThread;
import org.eclipse.jst.server.tomcat.core.internal.ProgressUtil;
import org.eclipse.jst.server.tomcat.core.internal.TomcatServerBehaviour;
import org.eclipse.jst.server.tomcat.core.internal.Trace;
import org.eclipse.mylyn.commons.net.WebLocation;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerPort;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.eclipse.wst.server.core.model.IModuleResourceDelta;

import com.vmware.vfabric.ide.eclipse.tcserver.internal.core.TcServer.Layout;
import com.vmware.vfabric.ide.eclipse.tcserver.reloading.TcServerReloadingPlugin;

/**
 * @author Steffen Pingel
 * @author Christian Dupuis
 * @author Kris De Volder
 * @author Leo Dos Santos
 */
public class TcServerBehaviour extends TomcatServerBehaviour {

	private static class SslPingThread extends PingThread {

		private static int DELAY = 10;

		private static int INTERVAL = 250;

		private final String url;

		private boolean stopped;

		private final TcServerBehaviour serverBehaviour;

		private final int maxPings;

		private int interval;

		public SslPingThread(IServer server, String url, int maxPings, TcServerBehaviour behaviour) {
			super(server, url, maxPings, behaviour);
			this.url = url;
			this.maxPings = (maxPings != -1) ? maxPings : 100;
			this.serverBehaviour = behaviour;
		}

		@Override
		protected void ping() {
			interval = DELAY;
			for (int i = 0; i < maxPings && !stopped; i++) {
				try {
					Thread.sleep(interval);

					WebLocation location = new WebLocation(url);
					HttpClient client = new HttpClient();
					org.eclipse.mylyn.commons.net.WebUtil.configureHttpClient(client, ""); //$NON-NLS-1$

					HeadMethod method = new HeadMethod(location.getUrl());
					HostConfiguration hostConfiguration = org.eclipse.mylyn.commons.net.WebUtil
							.createHostConfiguration(client, location, new NullProgressMonitor());
					org.eclipse.mylyn.commons.net.WebUtil.execute(client, hostConfiguration, method,
							new NullProgressMonitor());

					// success
					serverBehaviour.setServerStarted();
					stop();
					break;
				}
				catch (ConnectException e) {
					// ignore
				}
				catch (Exception e) {
					Trace.trace(Trace.SEVERE, "Failed to ping for tc Server startup.", e);
					forceStop();
					break;
				}
				interval = INTERVAL;
			}
		}

		private void forceStop() {
			stop();
			serverBehaviour.stopImpl();
		}

		@Override
		public void stop() {
			super.stop();
			this.stopped = true;
		}

	}

	public static boolean mergeClasspathIfRequired(List<IRuntimeClasspathEntry> cp, IRuntimeClasspathEntry entry) {
		return mergeClasspathIfRequired(cp, entry, false);
	}

	public static boolean mergeClasspathIfRequired(List<IRuntimeClasspathEntry> cp, IRuntimeClasspathEntry entry,
			boolean prepend) {
		boolean first = true;
		Iterator<IRuntimeClasspathEntry> iterator = cp.iterator();
		while (iterator.hasNext()) {
			IRuntimeClasspathEntry entry2 = iterator.next();
			if (entry2.getPath().equals(entry.getPath())) {
				if (prepend && !first) {
					// ensure new element is always first
					iterator.remove();
				}
				else {
					return false;
				}
			}
			first = false;
		}

		if (prepend) {
			cp.add(0, entry);
		}
		else {
			cp.add(entry);
		}
		return true;
	}

	public String getDeployRoot() {
		return getServerDeployDirectory().toOSString() + File.separator;
	}

	// make method visible to package
	@Override
	public IModuleResourceDelta[] getPublishedResourceDelta(IModule[] module) {
		return super.getPublishedResourceDelta(module);
	}

	@Override
	public IModuleResource[] getResources(IModule[] module) {
		return super.getResources(module);
	}

	public IServicabilityInfo getServicabilityInfo() throws CoreException {
		TcServer server = getTomcatServer();
		return getTomcatConfiguration().getServicabilityInfo(server.getRuntimeBaseDirectory());
	}

	@Override
	public TcServerConfiguration getTomcatConfiguration() throws CoreException {
		return (TcServerConfiguration) super.getTomcatConfiguration();
	}

	@Override
	public TcServerRuntime getTomcatRuntime() {
		return (TcServerRuntime) super.getTomcatRuntime();
	}

	@Override
	public TcServer getTomcatServer() {
		return (TcServer) super.getTomcatServer();
	}

	@Override
	public void setupLaunch(ILaunch launch, String launchMode, IProgressMonitor monitor) throws CoreException {
		super.setupLaunch(launch, launchMode, monitor);

		for (IModule[] module : getAllModules()) {
			setModuleState(module, IServer.STATE_STARTING);
		}

		// transfer VM arguments to launch to make them accessible for later use
		launch.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, launch.getLaunchConfiguration()
				.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, (String) null));

		TcServer.getCallback().setupLaunch(getTomcatServer(), launch, launchMode, monitor);

		TcServerConfiguration configuration = getTomcatConfiguration();
		if (configuration.getMainPort() == null && ping == null) {
			ServerPort serverPort = configuration.getMainSslPort();
			if (serverPort != null) {
				try {
					String url = "https://" + getServer().getHost() + ":" + serverPort.getPort();
					ping = new SslPingThread(getServer(), url, -1, this);
				}
				catch (Exception e) {
					Trace.trace(Trace.SEVERE, "Can't ping for tc Server startup.");
				}
			}
		}
	}

	@Override
	public void setupLaunchConfiguration(ILaunchConfigurationWorkingCopy workingCopy, IProgressMonitor monitor)
			throws CoreException {
		super.setupLaunchConfiguration(workingCopy, monitor);

		TcServer.getCallback().setupLaunchConfiguration(getTomcatServer(), workingCopy, monitor);

		if (getTomcatServer().isTestEnvironment()) {
			setupRuntimeClasspathForTestEnvironment(workingCopy, monitor);
		}

		String existingVMArgs = workingCopy.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS,
				(String) null);
		String[] parsedVMArgs = null;
		if (existingVMArgs != null) {
			parsedVMArgs = DebugPlugin.parseArguments(existingVMArgs);
		}

		List<String> argsToAdd = new ArrayList<String>();
		List<String> argsToRemove = new ArrayList<String>();

		if (getTomcatServer().isAgentRedeployEnabled() && TcServerReloadingPlugin.getAgentJarPath() != null) {
			argsToAdd.add("-javaagent:\"" + TcServerReloadingPlugin.getAgentJarPath() + "\"");
			argsToAdd.add("-noverify");
			String agentOptions = getTomcatServer().getAgentOptions();
			if (StringUtils.isNotBlank(agentOptions)) {
				argsToAdd.add("-Dspringloaded=\"" + agentOptions + "\"");
			}
			else {
				argsToRemove.add("-Dspringloaded");
			}
		}
		else {
			argsToRemove.add("-javaagent:\"" + TcServerReloadingPlugin.getAgentJarPath() + "\"");
			argsToRemove.add("-noverify");
			argsToRemove.add("-Dspringloaded");
		}

		boolean grailsInstalled = Platform.getBundle("org.grails.ide.eclipse.runonserver") != null
				|| Platform.getBundle("com.springsource.sts.grails.runonserver") != null;

		boolean addXmx = true;
		boolean addXss = true;
		boolean addGrailsGspEnable = true;
		boolean addMaxPermSize = true;
		boolean addLogManager = true;
		boolean addLogConfigFile = true;

		// check if arguments are already present
		if (parsedVMArgs != null) {
			for (String parsedVMArg : parsedVMArgs) {
				if (parsedVMArg.startsWith("-Xmx")) {
					addXmx = false;
				}
				else if (parsedVMArg.startsWith("-Xss")) {
					addXss = false;
				}
				else if (parsedVMArg.startsWith("-XX:MaxPermSize=")) {
					addMaxPermSize = false;
				}
				else if (parsedVMArg.startsWith("-Dgrails.gsp.enable.reload=")) {
					addGrailsGspEnable = false;
				}
				else if (parsedVMArg.startsWith("-Djava.util.logging.manager")) {
					addLogManager = false;
				}
				else if (parsedVMArg.startsWith("-Djava.util.logging.config.file")) {
					addLogConfigFile = false;
				}
			}
		}
		if (addXmx) {
			argsToAdd.add("-Xmx768m");
		}
		if (addXss) {
			argsToAdd.add("-Xss256k");
		}
		if (addMaxPermSize) {
			argsToAdd.add("-XX:MaxPermSize=256m");
		}

		if (grailsInstalled) {
			if (addGrailsGspEnable) {
				argsToAdd.add("-Dgrails.gsp.enable.reload=true");
			}
		}

		if (addLogManager) {
			argsToAdd
					.add("-Djava.util.logging.manager=com.springsource.tcserver.serviceability.logging.TcServerLogManager");
		}

		if (addLogConfigFile) {
			argsToAdd.add("-Djava.util.logging.config.file="
					+ getTomcatServer().getInstanceBase(getServer().getRuntime()).append("conf")
							.append("logging.properties"));
		}

		argsToAdd.addAll(getTomcatServer().getAddExtraVmArgs());
		argsToRemove.addAll(getTomcatServer().getRemoveExtraVmArgs());

		if (argsToAdd.size() > 0 || argsToRemove.size() > 0) {
			workingCopy.setAttribute(
					IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS,
					mergeArguments(existingVMArgs, argsToAdd.toArray(new String[0]),
							argsToRemove.toArray(new String[0]), false));
		}

	}

	/**
	 * In test mode webtools creates a temporary directory that is missing
	 * required jars for tc Server. This method copies the necessary jars from
	 * the instance base directory.
	 */
	private void setupRuntimeClasspathForTestEnvironment(ILaunchConfigurationWorkingCopy launchConfiguration,
			IProgressMonitor monitor) throws CoreException {
		TcServer tcServer = getTomcatServer();

		IRuntimeClasspathEntry[] originalClasspath = JavaRuntime.computeUnresolvedRuntimeClasspath(launchConfiguration);
		List<IRuntimeClasspathEntry> cp = new ArrayList<IRuntimeClasspathEntry>(Arrays.asList(originalClasspath));

		IPath instanceBaseDirectory = tcServer.getInstanceBase(getServer().getRuntime());
		if (instanceBaseDirectory != null) {
			boolean changed = false;

			IPath path = instanceBaseDirectory.append("bin");
			changed |= addJarToClasspath(launchConfiguration, cp, path, "tomcat-juli.jar", false);

			if (changed) {
				List<String> list = new ArrayList<String>(cp.size());
				for (IRuntimeClasspathEntry entry : cp) {
					try {
						list.add(entry.getMemento());
					}
					catch (Exception e) {
						Trace.trace(Trace.SEVERE, "Could not resolve classpath entry: " + entry, e);
					}
				}
				launchConfiguration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, list);
			}
		}
	}

	private boolean addJarToClasspath(ILaunchConfigurationWorkingCopy launchConfiguration,
			List<IRuntimeClasspathEntry> cp, IPath path, String prefix, boolean prepend) throws CoreException {
		File directory = path.toFile();
		String[] filenames = directory.list();
		if (filenames != null) {
			for (String filename : filenames) {
				if (filename.startsWith(prefix) && filename.endsWith(".jar")) {
					IRuntimeClasspathEntry entry = JavaRuntime.newArchiveRuntimeClasspathEntry(path.append(filename));
					return TcServerBehaviour.mergeClasspathIfRequired(cp, entry, prepend);
				}
			}
		}
		return false;
	}

	@Override
	public void stop(boolean force) {
		if (!force) {
			ServerPort[] ports = getTomcatServer().getServerPorts();
			for (ServerPort serverPort : ports) {
				if ("server".equals(serverPort.getId())) {
					super.stop(force);
					return;
				}
			}

			int state = getServer().getServerState();
			// If stopped or stopping, no need to run stop command again
			if (state == IServer.STATE_STOPPED || state == IServer.STATE_STOPPING) {
				return;
			}
			for (IModule[] module : getAllModules()) {
				int currentState = getServer().getModuleState(module);
				if (currentState < IServer.STATE_STOPPING) {
					setModuleState(module, IServer.STATE_STOPPING);
				}
			}
			setServerState(IServer.STATE_STOPPING);

			// fall-back to JMX command
			ShutdownTcServerCommand command = new ShutdownTcServerCommand(this);
			try {
				command.execute();
				// need to kill server unfortunately since the shutdown command
				// only stops Catalina but not the Tomcat process itself
				terminate();
				stopImpl();
			}
			catch (TimeoutException e) {
				// webtools will invoke this method again with and set force to
				// true in case of a timeout
			}
			catch (CoreException e) {
				// ignore, already logged in command
				super.stop(true);
			}
		}
		else {
			super.stop(force);
		}
	}

	@Override
	protected void stopImpl() {
		/*
		 * Set the stopped state for all modules
		 */
		for (IModule[] module : getAllModules()) {
			setModuleState(module, IServer.STATE_STOPPED);
		}
		super.stopImpl();
	}

	/**
	 * Public for testing only.
	 */
	@Override
	public String[] getRuntimeVMArguments() {
		IPath instancePath = getRuntimeBaseDirectory();
		IPath deployPath;
		// If serving modules without publishing, use workspace path as the
		// deploy path
		if (getTomcatServer().isServeModulesWithoutPublish()) {
			deployPath = ResourcesPlugin.getWorkspace().getRoot().getLocation();
		}
		// Else normal publishing for modules
		else {
			deployPath = getServerDeployDirectory();
			// If deployPath is relative, convert to canonical path and hope for
			// the best
			if (!deployPath.isAbsolute()) {
				try {
					String deployLoc = (new File(deployPath.toOSString())).getCanonicalPath();
					deployPath = new Path(deployLoc);
				}
				catch (IOException e) {
					// Ignore if there is a problem
				}
			}
		}

		IPath installPath;
		if (getTomcatServer().getLayout() == Layout.COMBINED) {
			installPath = instancePath;
		}
		else {
			installPath = getTomcatRuntime().getTomcatLocation();
		}

		// pass true to ensure that configPath is always respected
		return getTomcatVersionHandler().getRuntimeVMArguments(installPath, instancePath, deployPath, true);
	}

	@Override
	protected void publishModule(int kind, int deltaKind, IModule[] moduleTree, IProgressMonitor monitor)
			throws CoreException {
		super.publishModule(kind, deltaKind, moduleTree, monitor);

		// reload application if enhanced redeploy is enabled
		TcPublisher op = new TcPublisher(this, kind, moduleTree, deltaKind);
		op.execute(monitor, null);
	}

	@Override
	protected void publishServer(int kind, IProgressMonitor monitor) throws CoreException {
		if (getServer().getRuntime() == null) {
			return;
		}

		// set install dir to catalina.home
		IPath installDir = getRuntimeBaseDirectory();
		IPath confDir = null;
		if (getTomcatServer().isTestEnvironment()) {
			confDir = getRuntimeBaseDirectory();
			IStatus status = getTomcatVersionHandler().prepareRuntimeDirectory(confDir);
			if (status != null && !status.isOK()) {
				throw new CoreException(status);
			}

			IPath instanceBase = getTomcatServer().getInstanceBase(getServer().getRuntime());

			// copy keystore file in case of ssl instance
			IPath keystorePath = instanceBase.append("conf").append("tcserver.keystore");
			if (keystorePath.toFile().exists()) {
				IPath destPath = confDir.append("conf");
				if (!destPath.toFile().exists()) {
					destPath.toFile().mkdirs();
				}

				File file = keystorePath.toFile();
				destPath = destPath.append(file.getName());
				if (!destPath.toFile().exists()) {
					FileUtil.copyFile(file.getAbsolutePath(), destPath.toFile().getAbsolutePath());
				}
			}

			// copy libraries from instance base
			IPath libPath = instanceBase.append("lib");
			if (libPath.toFile().exists()) {
				File[] files = libPath.toFile().listFiles();
				if (files != null) {
					for (File file : files) {
						if (file.getName().endsWith(".jar")) {
							IPath destPath = confDir.append("lib");
							if (!destPath.toFile().exists()) {
								destPath.toFile().mkdirs();
							}

							destPath = destPath.append(file.getName());
							if (!destPath.toFile().exists()) {
								FileUtil.copyFile(file.getAbsolutePath(), destPath.toFile().getAbsolutePath());
							}
						}
					}
				}
			}
		}
		else {
			confDir = installDir;
		}
		IStatus status = getTomcatVersionHandler().prepareDeployDirectory(getServerDeployDirectory());
		if (status != null && !status.isOK()) {
			throw new CoreException(status);
		}

		TcServer.getCallback().publishServer(getTomcatServer(), kind, monitor);

		monitor = ProgressUtil.getMonitorFor(monitor);
		monitor.beginTask(Messages.publishServerTask, 600);

		status = getTomcatConfiguration().cleanupServer(confDir, installDir,
				!getTomcatServer().isSaveSeparateContextFiles(), ProgressUtil.getSubMonitorFor(monitor, 100));
		if (status != null && !status.isOK()) {
			throw new CoreException(status);
		}

		status = getTomcatConfiguration().backupAndPublish(confDir, !getTomcatServer().isTestEnvironment(),
				ProgressUtil.getSubMonitorFor(monitor, 400));
		if (status != null && !status.isOK()) {
			throw new CoreException(status);
		}

		status = getTomcatConfiguration().localizeConfiguration(confDir, getServerDeployDirectory(), getTomcatServer(),
				ProgressUtil.getSubMonitorFor(monitor, 100));
		if (status != null && !status.isOK()) {
			throw new CoreException(status);
		}

		monitor.done();

		setServerPublishState(IServer.PUBLISH_STATE_NONE);
	}

	@Override
	public boolean canRestartModule(IModule[] module) {
		return true;
	}

	@Override
	public boolean canPublishModule(IModule[] module) {
		return true;
	}

	@Override
	public void startModule(IModule[] module, IProgressMonitor monitor) throws CoreException {
		int currentState = getServer().getModuleState(module);
		if (currentState == IServer.STATE_STOPPED || currentState == IServer.STATE_UNKNOWN) {
			monitor.beginTask("Starting Module", 1);
			try {
				setModuleState(module, IServer.STATE_STARTING);
				new StartModuleCommand(this, module).execute();
				setModuleState(module, IServer.STATE_STARTED);
				monitor.worked(1);
			}
			catch (TimeoutException e) {
				setModuleState(module, IServer.STATE_UNKNOWN);
				throw new CoreException(new Status(IStatus.ERROR, TcServerCorePlugin.PLUGIN_ID, "Cannot start module '"
						+ module[0].getName() + "'", e));
			}
		}
	}

	@Override
	public void stopModule(IModule[] module, IProgressMonitor monitor) throws CoreException {
		int currentState = getServer().getModuleState(module);
		if (currentState < IServer.STATE_STOPPING) {
			monitor.beginTask("Stopping Modules", 1);
			try {
				setModuleState(module, IServer.STATE_STOPPING);
				new StopModuleCommand(this, module).execute();
				setModuleState(module, IServer.STATE_STOPPED);
				monitor.worked(1);
			}
			catch (TimeoutException e) {
				setModuleState(module, IServer.STATE_UNKNOWN);
				throw new CoreException(new Status(IStatus.ERROR, TcServerCorePlugin.PLUGIN_ID, "Cannot start module '"
						+ module[0].getName() + "'", e));
			}
		}
	}

	@Override
	protected void setServerStarted() {
		super.setServerStarted();
		for (IModule[] module : getAllModules()) {
			setModuleState(module, IServer.STATE_STARTED);
		}
	}

}
