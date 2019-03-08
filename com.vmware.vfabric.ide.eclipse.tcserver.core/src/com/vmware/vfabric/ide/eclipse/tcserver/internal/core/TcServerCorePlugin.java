/*******************************************************************************
 * Copyright (c) 2012 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package com.vmware.vfabric.ide.eclipse.tcserver.internal.core;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

public class TcServerCorePlugin extends Plugin {

	public static final String PLUGIN_ID = "com.vmware.vfabric.ide.eclipse.tcserver.core";

	private static TcServerCorePlugin plugin;

	private static TcServerConfigurationResourceListener configurationListener;

	public static TcServerCorePlugin getDefault() {
		return plugin;
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		configurationListener = new TcServerConfigurationResourceListener();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(configurationListener,
				IResourceChangeEvent.POST_CHANGE);

	}

	@Override
	public void stop(BundleContext context) throws Exception {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(configurationListener);
		plugin = null;
		super.stop(context);
	}

}
