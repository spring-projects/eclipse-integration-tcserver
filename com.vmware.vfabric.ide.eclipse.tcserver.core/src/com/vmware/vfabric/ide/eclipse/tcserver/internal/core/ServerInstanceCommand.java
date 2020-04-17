/*******************************************************************************
 * Copyright (c) 2012, 2020 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package com.vmware.vfabric.ide.eclipse.tcserver.internal.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.vmware.vfabric.ide.eclipse.tcserver.core.util.OutputWriter;
import com.vmware.vfabric.ide.eclipse.tcserver.core.util.ProcessRunner;
import com.vmware.vfabric.ide.eclipse.tcserver.core.util.StandardProcessRunner;
import com.vmware.vfabric.ide.eclipse.tcserver.core.util.SystemErrOutputWriter;
import com.vmware.vfabric.ide.eclipse.tcserver.core.util.SystemOutOutputWriter;

/**
 * @author Christian Dupuis
 * @author Steffen Pingel
 * @author Tomasz Zarna
 * @since 2.5.2
 */
public final class ServerInstanceCommand {

	private final ProcessRunner processRunner;

	private final File script;

	private final OutputWriter writer = new OutputWriter() {

		StringBuffer buffer = new StringBuffer();

		public void write(String line) {
			buffer.append(line + "\n");
		}

		public String toString() {
			return buffer.toString();
		}

	};

	public ServerInstanceCommand(ITcRuntime runtime) {
		this.script = runtime.instanceCreationScript().toFile();
		this.processRunner = new StandardProcessRunner(//
				new OutputWriter[] { new SystemOutOutputWriter(), writer }, //
				new OutputWriter[] { new SystemErrOutputWriter(), writer });
	}
	
	public String getOutput() {
		return writer.toString();
	}

	public int execute(String... arguments) {
		List<String> allArguments = new ArrayList<String>(arguments.length + 1);
		if (TcServerUtil.isWindows()) {
			allArguments.add("\"" + this.script.getAbsolutePath() + "\"");  
		} else {
			allArguments.add(this.script.getAbsolutePath());  
		}
		allArguments.addAll(Arrays.asList(arguments));

		try {
			return this.processRunner.run(this.script.getParentFile(),
					allArguments.toArray(new String[allArguments.size()]));
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	public String toString() {
		StringBuilder str = new StringBuilder();
		if (this.script != null) {
			str.append(this.script.getAbsolutePath());
		}
		return str.toString();
	}

}
