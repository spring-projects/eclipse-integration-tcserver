/*******************************************************************************
 * Copyright (c) 2012 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package com.vmware.vfabric.ide.eclipse.tcserver.insight.internal.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;

/**
 * @author Steffen Pingel
 */
public class InsightPluginModel {

	private final List<InsightPlugin> plugins = new ArrayList<InsightPlugin>();

	public List<InsightPlugin> getPlugins() {
		return plugins;
	}

	public void load(IPath location) {
		Assert.isNotNull(location);

		plugins.clear();

		if (location.toFile().exists()) {
			File[] files = location.toFile().listFiles();
			if (files != null) {
				for (File file : files) {
					String name = file.getName();
					if (name.startsWith(InsightPlugin.PREFIX)
							&& (name.endsWith(InsightPlugin.EXT_ENABLED) || name.endsWith(InsightPlugin.EXT_DISABLED))) {
						InsightPlugin plugin = new InsightPlugin(file);
						plugins.add(plugin);
					}
				}
			}
		}
	}

	public void commit() {
		for (InsightPlugin plugin : plugins) {
			plugin.commit();
		}
	}

}
