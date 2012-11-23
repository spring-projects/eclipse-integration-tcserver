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

import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.allOf;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.widgetOfType;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.withRegex;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotLabel;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.ui.PlatformUI;
import org.hamcrest.Matcher;
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
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
				try {
					Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
					org.eclipse.wst.server.ui.internal.wizard.NewServerWizard newServerWizard = new org.eclipse.wst.server.ui.internal.wizard.NewServerWizard();
					WizardDialog dlg = new WizardDialog(shell, newServerWizard);
					dlg.setBlockOnOpen(false);
					dlg.open();
				}
				catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		});
		bot.waitUntil(Conditions.shellIsActive("New Server"));

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

	void pressFinish() {
		pressFinish(false, null);
	}

	void pressFinish(boolean errorDialogExpected, String errorMessageRegex) {
		boolean errorDialogAutomatedMode = ErrorDialog.AUTOMATED_MODE;
		ErrorDialog.AUTOMATED_MODE = false;
		try {
			assertTrue(isFinishEnabled()); // to fail quick
			bot.button("Finish").click();
			if (errorDialogExpected) {
				bot.waitUntil(Conditions.shellIsActive("Server Error"), 10000);
				SWTBotShell serverErrorDialog = bot.shell("Server Error");
				assertNotNull(serverErrorDialog);
				@SuppressWarnings({ "unchecked", "rawtypes" })
				Matcher matcher = allOf(widgetOfType(Label.class), withRegex(errorMessageRegex));
				@SuppressWarnings("unchecked")
				SWTBotLabel label = new SWTBotLabel((Label) serverErrorDialog.bot().widget(matcher));
				assertNotNull(label);
				serverErrorDialog.close();
				// an error occurred, cancel the wizard
				bot.button("Cancel").click();
			}
			bot.waitUntil(SWTBotUtils.widgetIsDisposed(shell), 10000);
		}
		finally {
			ErrorDialog.AUTOMATED_MODE = errorDialogAutomatedMode;
		}
	}
}
