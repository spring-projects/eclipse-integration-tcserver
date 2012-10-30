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
package com.vmware.vfabric.ide.eclipse.tcserver.livegraph;

/**
 * @author Leo Dos Santos
 */
public class DeployedApplication {

	private final boolean liveBeansEnabled;

	private final String name;

	public DeployedApplication(String name, boolean liveBeansEnabled) {
		this.liveBeansEnabled = liveBeansEnabled;
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public boolean isLiveBeansEnabled() {
		return liveBeansEnabled;
	}

}
