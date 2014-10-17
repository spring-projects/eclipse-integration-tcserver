/*******************************************************************************
 * Copyright (c) 2012, 2014 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package com.vmware.vfabric.ide.eclipse.tcserver.tests.ui;

import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.withText;

import org.eclipse.core.runtime.IPath;
import org.eclipse.swtbot.eclipse.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;

/**
 * @author Kaitlin Duck Sherwood
 * @author Tomasz Zarna
 */
public class VMwareVFabricTcServerPage extends AbstractTcServerPage {

	// Note: these two messages appear to be buried in TC or Tomcat code.
	private static final String INOFFENSIVE_SERVER_DIR_MESSAGE = "Specify the installation directory";

	private static final String INVALID_TOMCAT_DIR_MESSAGE = "The Tomcat installation directory is not valid. It is missing expected file or folder tcruntime-ctl.sh.";

	VMwareVFabricTcServerPage(SWTBotShell shell) {
		super(shell);
		shell.bot().waitUntil(Conditions.waitForWidget(withText("Pivotal tc Server")));
	}

	TcServerConfigurationPage nextToTcServerConfigurationPage() {
		shell.bot().button("Next >").click();
		return new TcServerConfigurationPage(shell);
	}

	void selectInstallDirectory(IPath installDirectoryPath) {
		shell.bot().waitUntil(Conditions.waitForWidget(withText("Cancel")));

		shell.bot().textWithLabel("Installation directory:").setText("nonsense nonsense");
		assertErrorMessage(INVALID_TOMCAT_DIR_MESSAGE);

		shell.bot().textWithLabel("Installation directory:").setText(installDirectoryPath.toString());
		assertMessage(INOFFENSIVE_SERVER_DIR_MESSAGE);
	}
}
