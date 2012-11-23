/*******************************************************************************
 * Copyright (c) 2012 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package com.vmware.vfabric.ide.eclipse.tcserver.tests.ui;

import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;

/**
 * @author Tomasz Zarna
 *
 */
public class AbstractTcServerPage {

	protected final SWTBotShell shell;

	AbstractTcServerPage(SWTBotShell shell) {
		this.shell = shell;
	}

	boolean isNextButtonEnabled() {
		return shell.bot().button("Next >").isEnabled();
	}

	void assertMessage(String message) {
		shell.bot().text(message);
	}

	void assertErrorMessage(String errorMessage) {
		assertMessage(" " + errorMessage);
	}
}