/*******************************************************************************
 * Copyright (c) 2012 -2014 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package com.vmware.vfabric.ide.eclipse.tcserver.insight.internal.ui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.vmware.vfabric.ide.eclipse.tcserver.insight.internal.ui.link.StsProtocolPartListener;

/**
 * The activator class controls the plug-in life cycle
 * @author Christian Dupuis
 * @author Steffen Pingel
 */
public class Activator extends AbstractUIPlugin {

	public static final String CONTEXT = "/insight";

	public static final String COOKIE = "com.springsource.sts.run.embedded=true";

	public static final String PLUGIN_ID = "com.vmware.vfabric.ide.eclipse.tcserver.insight.ui";

	// The shared instance
	private static Activator plugin;

	private final IPartListener listener = new StsProtocolPartListener();

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;

		/*
		 * Check first if there is a valid UI thread available on the workbench
		 * (could check if workbench is running as alternative) not to add this
		 * listener in case a headless mode without the workbench UI running
		 */
		Display display = PlatformUI.getWorkbench().getDisplay();
		if (display != null && !display.isDisposed()) {
			display.asyncExec(new Runnable() {
				public void run() {
					IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					if (workbenchWindow != null) {
						IPartService service = (IPartService) workbenchWindow.getService(IPartService.class);
						service.addPartListener(listener);
					}
				}
			});
		}
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		/*
		 * Display might be disposed (Workbench has been shut down already) at
		 * this point since Eclipse Luna
		 */
		Display display = PlatformUI.getWorkbench().getDisplay();
		if (display != null && !display.isDisposed()) {
			display.asyncExec(new Runnable() {
				public void run() {
					IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					if (workbenchWindow != null) {
						IPartService service = (IPartService) workbenchWindow.getService(IPartService.class);
						service.removePartListener(listener);
					}
				}
			});
		}

		plugin = null;
		super.stop(context);

	}

	public static Activator getDefault() {
		return plugin;
	}

	public static void log(Throwable exception) {
		getDefault().getLog().log(createErrorStatus("Internal Error", exception));
	}

	/**
	 * Returns a new {@link IStatus} with status "ERROR" for this plug-in.
	 */
	public static IStatus createErrorStatus(String message, Throwable exception) {
		if (message == null) {
			message = "";
		}
		return new Status(IStatus.ERROR, PLUGIN_ID, 0, message, exception);
	}

}
