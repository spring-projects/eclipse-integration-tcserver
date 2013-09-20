/*******************************************************************************
 * Copyright (c) 2012 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package com.vmware.vfabric.ide.eclipse.tcserver.insight.internal.ui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPartService;
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

		Display.getDefault().asyncExec(new Runnable() {

			public void run() {
				IPartService service = (IPartService) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
						.getService(IPartService.class);
				service.addPartListener(listener);
			}
		});
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);

		Display.getDefault().asyncExec(new Runnable() {

			public void run() {
				IPartService service = (IPartService) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
						.getService(IPartService.class);
				service.removePartListener(listener);
			}
		});
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
