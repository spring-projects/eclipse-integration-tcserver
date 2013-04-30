/*******************************************************************************
 * Copyright (c) 2012 - 2013 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package com.vmware.vfabric.ide.eclipse.tcserver.tests.ui;

import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.withText;

import org.eclipse.swtbot.eclipse.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.springsource.ide.eclipse.commons.frameworks.test.util.SWTBotUtils;

/**
 * @author Kaitlin Duck Sherwood
 * @author Tomasz Zarna
 */
public class DefineNewServerPage extends AbstractTcServerPage {

	static final String VMWARE_VFABRIC_TC_SERVER_LABEL = "VMware vFabric tc Server v2.5 - v2.9";

	DefineNewServerPage(SWTBotShell shell) {
		super(shell);
		shell.bot().waitUntil(Conditions.waitForWidget(withText("Define a New Server")));
	}

	/**
	 * Moved from TcServerNewServerWizardUiTest#selectTcServer
	 */
	void selectTcServer() {
		// the "right" way to do this sometimes fails
		try {
			// "Server").click();
			SWTBotUtils.selectChildTreeElement(shell.bot(), "New Server", "VMware",
					VMWARE_VFABRIC_TC_SERVER_LABEL);

		}
		catch (Exception e) {
			// fallback to doing it the "wrong" way.
			shell.bot().tree().collapseNode("VMware");
			shell.bot().tree().expandNode("VMware").select(VMWARE_VFABRIC_TC_SERVER_LABEL).click();
		}
	}

	String getServerName() {
		return shell.bot().textWithLabel("Server name:").getText();
	}

	TcServerConfigurationPage nextToTcServerConfigurationPage() {
		shell.bot().button("Next >").click();
		return new TcServerConfigurationPage(shell);
	}

	VMwareVFabricTcServerPage nextToVMwareVFabricTcServerPage() {
		shell.bot().button("Next >").click();
		return new VMwareVFabricTcServerPage(shell);
	}

}
