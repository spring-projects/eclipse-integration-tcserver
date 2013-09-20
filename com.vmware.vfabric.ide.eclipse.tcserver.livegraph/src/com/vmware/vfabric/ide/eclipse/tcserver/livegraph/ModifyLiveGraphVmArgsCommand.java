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
package com.vmware.vfabric.ide.eclipse.tcserver.livegraph;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.vmware.vfabric.ide.eclipse.tcserver.internal.core.ModifyExtraVmArgsCommand;
import com.vmware.vfabric.ide.eclipse.tcserver.internal.core.TcServer;

/**
 * @author Leo Dos Santos
 */
public class ModifyLiveGraphVmArgsCommand extends ModifyExtraVmArgsCommand {

	private static final List<String> EMPTY_ARGS = Collections.emptyList();

	private static final List<String> ENABLE_ARGS = Arrays
			.asList(new String[] { TcServerLiveGraphPlugin.FLAG_LIVE_BEANS });

	public ModifyLiveGraphVmArgsCommand(TcServer tcServer, boolean enableLiveGraph) {
		super(tcServer, (enableLiveGraph ? ENABLE_ARGS : EMPTY_ARGS), (enableLiveGraph ? EMPTY_ARGS : ENABLE_ARGS));
	}

}
