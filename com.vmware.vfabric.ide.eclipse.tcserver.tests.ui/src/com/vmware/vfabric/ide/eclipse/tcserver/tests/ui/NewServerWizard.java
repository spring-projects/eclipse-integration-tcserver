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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.TimeoutException;
import org.springsource.ide.eclipse.commons.frameworks.test.util.SWTBotUtils;

/**
 * @author Kaitlin Duck Sherwood
 * @author Tomasz Zarna
 */
public class NewServerWizard {

	private static final SWTWorkbenchBot bot = new SWTWorkbenchBot();

	private final SWTBotShell shell;

	private NewServerWizard(SWTBotShell shell) {
		this.shell = shell;
	}

	static NewServerWizard openWizard() {

		SWTBotUtils.menu(bot, "File").menu("New").menu("Other...").click();

		// The "right" way to do this sometimes fails.
		try {
			SWTBotUtils.selectChildTreeElement(bot, "New", "Server", "Server").click();
		}
		catch (Exception e) {
			// fallback to doing it the "wrong" way.
			bot.tree().collapseNode("Server");
			bot.tree().expandNode("Server").select("Server").click();
		}

		bot.button("Next >").click();

		SWTBotShell newServerDialog = bot.shell("New Server");
		assertNotNull(newServerDialog);
		return new NewServerWizard(newServerDialog);
	}

	DefineNewServerPage getDefineNewServerPage() {
		return new DefineNewServerPage(shell);
	}

	public boolean isFinishEnabled() {
		return bot.button("Finish").isEnabled();
	}

	void pressFinish(boolean corruptedFiles) {
		SWTBotButton aButton;
		try {
			aButton = bot.button("Finish");
			assertTrue(aButton.isEnabled());
			aButton.click();
		}
		catch (WidgetNotFoundException e) {
			// purely to speed debugging a little bit
			assertTrue(false); // toss an assertion error
		}

		try {
			bot.waitUntil(SWTBotUtils.widgetIsDisposed(shell));
		}
		catch (TimeoutException e) {
			// We aren't sure why, but something in SWTBot eats the
			// exception that failing to copy the files throws. This
			// means that the finish fails, which means that the
			// New Server dialog doesn't go away.
			if (corruptedFiles && shell.isVisible()) {
				bot.button("Cancel").click();
			}
		}

	}
}
