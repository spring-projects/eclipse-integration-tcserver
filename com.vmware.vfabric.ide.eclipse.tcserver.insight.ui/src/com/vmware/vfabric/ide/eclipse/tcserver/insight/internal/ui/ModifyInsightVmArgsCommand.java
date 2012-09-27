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
package com.vmware.vfabric.ide.eclipse.tcserver.insight.internal.ui;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.vmware.vfabric.ide.eclipse.tcserver.internal.core.ModifyExtraVmArgsCommand;
import com.vmware.vfabric.ide.eclipse.tcserver.internal.core.TcServer;

/**
 * Command to change the Insight property in the server configuration.
 * @author Steffen Pingel
 * @author Leo Dos Santos
 */
public class ModifyInsightVmArgsCommand extends ModifyExtraVmArgsCommand {

	private static final List<String> EMPTY_ARGS = Collections.emptyList();

	private static final List<String> DISABLE_ARGS = Arrays
			.asList(new String[] { TcServerInsightUtil.DISABLED_INSIGHT });

	public ModifyInsightVmArgsCommand(TcServer tcServer, boolean enableInsight) {
		super(tcServer, (enableInsight ? EMPTY_ARGS : DISABLE_ARGS), (enableInsight ? DISABLE_ARGS : EMPTY_ARGS));
	}

}
