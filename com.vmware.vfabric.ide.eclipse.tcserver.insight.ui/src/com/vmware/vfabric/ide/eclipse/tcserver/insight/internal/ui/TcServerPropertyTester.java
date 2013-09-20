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

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.wst.server.core.IServer;

/**
 * @author Steffen Pingel
 */
public class TcServerPropertyTester extends PropertyTester {

	private static final String PROPERTY_HAS_INSIGHT = "hasInsight"; //$NON-NLS-1$

	private static final String PROPERTY_IS_STARTED = "isStarted"; //$NON-NLS-1$

	private boolean equals(boolean value, Object expectedValue) {
		return new Boolean(value).equals(expectedValue);
	}

	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (receiver instanceof IServer) {
			IServer server = (IServer) receiver;
			if (PROPERTY_HAS_INSIGHT.equals(property)) {
				return equals(TcServerInsightUtil.hasInsight(server), expectedValue);
			}
			else if (PROPERTY_IS_STARTED.equals(property)) {
				return equals(server.getServerState() == IServer.STATE_STARTED, expectedValue);
			}
		}
		return false;
	}

}
