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
package com.vmware.vfabric.ide.eclipse.tcserver.insight.internal.ui;

import org.eclipse.ui.IStartup;

/**
 * {@link IStartup} installation to ensure that the plugin activates without the requirement of UI interactions.
 * @author Christian Dupuis
 * @since 2.2.0
 */
public class Startup implements IStartup {

	/**
	 * {@inheritDoc}
	 */
	public void earlyStartup() {
		// bogus; do nothing other then making sure that bundle activator starts
	}

}
