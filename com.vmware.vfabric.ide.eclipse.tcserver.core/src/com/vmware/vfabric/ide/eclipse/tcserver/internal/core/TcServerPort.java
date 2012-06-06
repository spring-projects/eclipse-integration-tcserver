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
package com.vmware.vfabric.ide.eclipse.tcserver.internal.core;

import org.eclipse.wst.server.core.ServerPort;

/**
 * A {@link ServerPort} that supports a non-numeric value for the port to
 * support storing of place holders.
 * @author Steffen Pingel
 */
public class TcServerPort extends ServerPort {

	private String portString;

	public TcServerPort(String id, String name, int port, String protocol) {
		super(id, name, port, protocol);
	}

	public TcServerPort(String id, String name, int port, String protocol, String[] contentTypes, boolean advanced) {
		super(id, name, port, protocol, contentTypes, advanced);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		TcServerPort other = (TcServerPort) obj;
		if (portString == null) {
			if (other.portString != null) {
				return false;
			}
		}
		else if (!portString.equals(other.portString)) {
			return false;
		}
		return super.equals(obj);
	}

	public String getPortString() {
		return portString;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((portString == null) ? 0 : portString.hashCode());
		return result;
	}

	public void setPortString(String portString) {
		this.portString = portString;
	}

}
