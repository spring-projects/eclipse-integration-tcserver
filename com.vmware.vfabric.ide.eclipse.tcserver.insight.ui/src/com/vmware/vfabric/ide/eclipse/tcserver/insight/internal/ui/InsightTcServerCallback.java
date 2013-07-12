/*******************************************************************************
 *  Copyright (c) 2012 - 2013 GoPivotal, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package com.vmware.vfabric.ide.eclipse.tcserver.insight.internal.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jst.server.tomcat.core.internal.FileUtil;
import org.eclipse.jst.server.tomcat.core.internal.TomcatPlugin;
import org.eclipse.jst.server.tomcat.core.internal.Trace;
import org.eclipse.swt.widgets.Display;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.springsource.ide.eclipse.commons.ui.UiUtil;

import com.vmware.vfabric.ide.eclipse.tcserver.internal.core.TcServer;
import com.vmware.vfabric.ide.eclipse.tcserver.internal.core.TcServerBehaviour;
import com.vmware.vfabric.ide.eclipse.tcserver.internal.core.TcServerCallback;

/**
 * Prompts to enable insight the first time tc Server is launched.
 * @author Steffen Pingel
 * @author Christian Dupuis
 * @author Kris De Volder
 * @author Leo Dos Santos
 */
public class InsightTcServerCallback extends TcServerCallback {

	public static final String WANT_INSIGHT_DIALOG_TITLE = "Spring Insight";

	public final static String ATTRIBUTE_INSIGHT_CONFIGURED = Activator.PLUGIN_ID + ".insightConfigured";

	public final static String PREFERENCE_ENABLE_INSIGHT_PONT = "pont.insight.enable";

	public final static String WANT_INSIGHT_DIALOG_MESSAGE = "Spring Insight is available for this tc Server instance. Do you want to enable gathering of metrics for this tc Server instance (takes effect on server restart)?\n\nThe setting can be changed in the server editor.";

	public InsightTcServerCallback() {
		Activator.getDefault().getPreferenceStore()
				.setDefault(PREFERENCE_ENABLE_INSIGHT_PONT, MessageDialogWithToggle.PROMPT);
	}

	private void addInsightToClasspath(TcServer tcServer, ILaunchConfigurationWorkingCopy launchConfiguration)
			throws CoreException {
		IRuntimeClasspathEntry[] originalClasspath = JavaRuntime.computeUnresolvedRuntimeClasspath(launchConfiguration);
		List<IRuntimeClasspathEntry> cp = new ArrayList<IRuntimeClasspathEntry>(Arrays.asList(originalClasspath));

		IPath runtimeBaseDirectory = TcServerInsightUtil.getInsightBase(tcServer);
		if (runtimeBaseDirectory != null) {
			boolean changed = false;

			// looking for insight-bootstrap-tomcat-extlibs on tc v2.9,
			// looking for insight-bootstrap-tcserver on earlier versions
			IPath path = runtimeBaseDirectory.append("bin");
			changed |= addJarToClasspath(launchConfiguration, cp, path, "insight-bootstrap", true);

			// only add weaver if insight was found
			if (changed) {
				// tc Server v2.9
				if (!addJarToClasspath(launchConfiguration, cp, path, "insight-weaver", false)) {
					// fall back for older versions of tc Server
					path = runtimeBaseDirectory.append("lib");
					addJarToClasspath(launchConfiguration, cp, path, "aspectjweaver", false);
				}

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

	private void promptIfInsightNotEnabled(final TcServer tcServer, ILaunchConfigurationWorkingCopy launchConfiguration) {
		if (!TcServerInsightUtil.isInsightEnabled(tcServer)) {
			String value = Activator.getDefault().getPreferenceStore().getString(PREFERENCE_ENABLE_INSIGHT_PONT);
			if (MessageDialogWithToggle.PROMPT.equals(value)) {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						MessageDialogWithToggle dialog = MessageDialogWithToggle.openYesNoCancelQuestion(
								UiUtil.getShell(), WANT_INSIGHT_DIALOG_TITLE, WANT_INSIGHT_DIALOG_MESSAGE,
								"Do not ask again", false, Activator.getDefault().getPreferenceStore(),
								PREFERENCE_ENABLE_INSIGHT_PONT);
						if (dialog.getReturnCode() == IDialogConstants.YES_ID) {
							IServerWorkingCopy serverWC = tcServer.getServer().createWorkingCopy();
							TcServer serverInstance = (TcServer) serverWC.loadAdapter(TcServer.class, null);
							new ModifyInsightVmArgsCommand(serverInstance, true).execute();
							try {
								IServer server = serverWC.save(true, null);
								// force publishing to update insight
								// directories
								// if (server instanceof Server) {
								// ((Server)server).setServerPublishState(IServer.PUBLISH_STATE_FULL);
								// }
							}
							catch (CoreException e) {
								TomcatPlugin.log(e.getStatus());
							}
						}
					}
				});
			}
		}
	}

	@Override
	public void setDefaults(TcServer server, IProgressMonitor monitor) {
		// disable insight by default
		new ModifyInsightVmArgsCommand(server, false).execute();
	}

	@Override
	public void setupLaunchConfiguration(final TcServer tcServer, ILaunchConfigurationWorkingCopy launchConfiguration,
			IProgressMonitor monitor) throws CoreException {
		if (!TcServerInsightUtil.hasInsight(tcServer.getServer())) {
			// ignore
			return;
		}

		// prompt user once if insight should be enabled
		if (!launchConfiguration.getAttribute(ATTRIBUTE_INSIGHT_CONFIGURED, false)
				&& TcServerInsightUtil.isInsightCompatible(tcServer.getServer())) {
			launchConfiguration.setAttribute(ATTRIBUTE_INSIGHT_CONFIGURED, true);
			promptIfInsightNotEnabled(tcServer, launchConfiguration);
		}

		// add insight jars to classpath
		addInsightToClasspath(tcServer, launchConfiguration);

		if (TcServerInsightUtil.isInsightEnabled(tcServer)) {
			String existingVMArgs = launchConfiguration.getAttribute(
					IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, (String) null);
			existingVMArgs = appendArg(existingVMArgs, "-Dinsight.devedition", "-Dinsight.devedition=true");
			existingVMArgs = appendArg(existingVMArgs, "-Daspectj.overweaving", "-Daspectj.overweaving=true");
			existingVMArgs = appendArg(existingVMArgs, "-Djava.awt.headless", "-Djava.awt.headless=true");
			existingVMArgs = appendArg(existingVMArgs, "-Dgemfire.disableShutdownHook",
					"-Dgemfire.disableShutdownHook=true");
			existingVMArgs = addVMArgs(tcServer, existingVMArgs);
			launchConfiguration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, existingVMArgs);
		}
		else {
			String existingVMArgs = launchConfiguration.getAttribute(
					IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, (String) null);
			existingVMArgs = existingVMArgs.replace("-Dinsight.devedition=true", "");
			if (existingVMArgs != null && existingVMArgs.contains("-Daspectj.overweaving")) {
				existingVMArgs = existingVMArgs.replace("-Daspectj.overweaving=true", "");
				existingVMArgs = existingVMArgs.replace("-Daspectj.overweaving=false", "");
				existingVMArgs = existingVMArgs.replace("-Dgemfire.disableShutdownHook=true", "");
			}

			// insight seems to need this even when disabled
			existingVMArgs = addInsightBase(tcServer, existingVMArgs);
			launchConfiguration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, existingVMArgs);
		}
	}

	private String appendArg(String existingVMArgs, String arg, String value) {
		if (existingVMArgs == null) {
			existingVMArgs = value;
		}
		else {
			if (!existingVMArgs.contains(arg)) {
				existingVMArgs += " " + value;
			}
		}
		return existingVMArgs;
	}

	private String addVMArgs(TcServer tcServer, String existingVMArgs) {
		existingVMArgs = addInsightBase(tcServer, existingVMArgs);
		existingVMArgs = addMemoryArgs(tcServer, existingVMArgs);
		return existingVMArgs;
	}

	private String addMemoryArgs(TcServer tcServer, String existingVMArgs) {
		// heap
		if (!existingVMArgs.contains("-Xmx")) {
			existingVMArgs += " -Xmx1024m";
		}
		else {
			Pattern p = Pattern.compile("-Xmx([0-9]+)m");
			Matcher m = p.matcher(existingVMArgs);
			if (m.find()) {
				try {
					int i = Integer.parseInt(m.group(1));
					if (i < 1024) {
						existingVMArgs = m.replaceFirst("-Xmx1024m");
					}
				}
				catch (NumberFormatException e) {
					existingVMArgs = m.replaceFirst("-Xmx1024m");
				}
			}
		}

		// stack
		if (!existingVMArgs.contains("-XX:MaxPermSize")) {
			existingVMArgs += " -XX:MaxPermSize=256m";
		}
		else {
			Pattern p = Pattern.compile("-XX:MaxPermSize=([0-9]+)m");
			Matcher m = p.matcher(existingVMArgs);
			if (m.find()) {
				try {
					int i = Integer.parseInt(m.group(1));
					if (i < 256) {
						existingVMArgs = m.replaceFirst("-XX:MaxPermSize=256m");
					}
				}
				catch (NumberFormatException e) {
					existingVMArgs = m.replaceFirst("-XX:MaxPermSize=256m");
				}
			}
		}

		return existingVMArgs;
	}

	private String addInsightBase(TcServer tcServer, String existingVMArgs) {
		IPath path = TcServerInsightUtil.getInsightPath(tcServer.getServer());
		if (path != null) {
			existingVMArgs = addInsightBase(existingVMArgs, path);
		}
		return existingVMArgs;
	}

	/**
	 * Public for testing.
	 */
	public static String addInsightBase(String existingVMArgs, IPath path) {
		String arg = "-Dinsight.base=\"" + path.toOSString() + "\"";
		if (!existingVMArgs.contains("-Dinsight.base")) {
			existingVMArgs += " " + arg;
		}
		else {
			String regexp = "-Dinsight.base=\".*?\"";
			existingVMArgs = existingVMArgs.replaceAll(regexp, Matcher.quoteReplacement(arg));
		}
		return existingVMArgs;
	}

	/**
	 * Mostly for debugging purposes: disable the dialog by presetting the
	 * "don't ask again" value.
	 */
	public static void disableDialog(boolean insightEnabled) {
		Activator
				.getDefault()
				.getPreferenceStore()
				.setValue(InsightTcServerCallback.PREFERENCE_ENABLE_INSIGHT_PONT,
						insightEnabled ? MessageDialogWithToggle.ALWAYS : MessageDialogWithToggle.NEVER);
	}

	@Override
	public void publishServer(TcServer tcServer, int kind, IProgressMonitor monitor) throws CoreException {
		if (tcServer.isTestEnvironment()) {
			IPath runtimeBase = tcServer.getRuntimeBaseDirectory();
			if (runtimeBase == null) {
				return;
			}
			IPath insightBase = tcServer.getInstanceBase(tcServer.getServer().getRuntime());
			if (insightBase == null) {
				return;
			}

			IPath destPath = runtimeBase.append("insight");
			if (!destPath.toFile().exists()) {
				IPath srcPath = insightBase.append("insight");
				if (srcPath.toFile().exists()) {
					FileUtil.copyDirectory(srcPath.toOSString(), destPath.toOSString(), monitor);
				}
			}

			destPath = runtimeBase.append("webapps").append("insight.war");
			if (!destPath.toFile().exists()) {
				IPath srcPath = insightBase.append("webapps").append("insight.war");
				FileUtil.copyFile(srcPath.toOSString(), destPath.toOSString());
			}
		}
	}

}
