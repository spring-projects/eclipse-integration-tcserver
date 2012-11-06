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

import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.withText;

import java.util.Calendar;

import org.eclipse.swtbot.eclipse.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;

import com.vmware.vfabric.ide.eclipse.tcserver.internal.ui.TcServerInstanceConfiguratorPage;

/**
 * @author Kaitlin Duck Sherwood
 * @author Tomasz Zarna
 */
public class CreateTcServerInstancePage {
	private final SWTBotShell shell;

	CreateTcServerInstancePage(SWTBotShell shell) {
		this.shell = shell;
		shell.bot().waitUntil(Conditions.waitForWidget(withText("Create tc Server Instance")));
	}

	void configureNewInstance(final String arbitraryInstanceName) {
		Calendar now = Calendar.getInstance();
		String instanceName = arbitraryInstanceName + now.get(Calendar.SECOND);

		shell.bot().text(TcServerInstanceConfiguratorPage.ENTER_NAME);

		// De novo, there might not be any existing interfaces
		// bot.textWithLabel("Name:").setText(EXISTING_INTERFACE);
		// bot.text(" " + TcServerInstanceConfiguratorPage.INSTANCE_EXISTS);

		shell.bot().textWithLabel("Name:").setText("blah blah blah");
		shell.bot().text(" " + TcServerInstanceConfiguratorPage.ILLEGAL_SERVER_NAME);

		shell.bot().textWithLabel("Name:").setText(instanceName);
		shell.bot().text(TcServerInstanceConfiguratorPage.SELECT_TEMPLATES);

		shell.bot().radio("Combined"); // check for existence
		shell.bot().radio(0).click();

		// #4 is the "bio-ssl" feature, which works with Insight
		shell.bot().table().getTableItem(5).toggleCheck();
		shell.bot().table().getTableItem(5).click();
	}

}
