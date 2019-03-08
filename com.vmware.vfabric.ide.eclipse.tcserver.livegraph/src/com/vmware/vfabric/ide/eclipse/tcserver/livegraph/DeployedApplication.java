/*******************************************************************************
 * Copyright (c) 2012 - 2013 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package com.vmware.vfabric.ide.eclipse.tcserver.livegraph;

/**
 * @author Leo Dos Santos
 */
public class DeployedApplication {

	private final boolean liveBeansEnabled;

	private final String name;

	private final String path;

	public DeployedApplication(String name, String path, boolean liveBeansEnabled) {
		this.liveBeansEnabled = liveBeansEnabled;
		this.name = name;
		this.path = path;
	}

	public String getName() {
		return name;
	}

	public String getPath() {
		return path;
	}

	public boolean isLiveBeansEnabled() {
		return liveBeansEnabled;
	}

}
