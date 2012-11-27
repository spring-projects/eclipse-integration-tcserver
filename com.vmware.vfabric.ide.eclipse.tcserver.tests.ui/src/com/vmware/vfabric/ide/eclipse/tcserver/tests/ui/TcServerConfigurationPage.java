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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.swtbot.eclipse.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.matchers.AllOf;
import org.eclipse.swtbot.swt.finder.matchers.WidgetOfType;
import org.eclipse.swtbot.swt.finder.matchers.WithStyle;
import org.eclipse.swtbot.swt.finder.results.VoidResult;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCombo;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotRadio;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.hamcrest.Matcher;

import com.vmware.vfabric.ide.eclipse.tcserver.internal.ui.TcServer20WizardFragment;
import com.vmware.vfabric.ide.eclipse.tcserver.internal.ui.TcServer21WizardFragment;

/**
 * Page Object for @link TcServerInstanceConfiguratorPage
 *
 * @author Kaitlin Duck Sherwood
 * @author Tomasz Zarna
 */
public class TcServerConfigurationPage extends AbstractTcServerPage {

	TcServerConfigurationPage(SWTBotShell shell) {
		super(shell);
		shell.bot().waitUntil(Conditions.waitForWidget(withText("tc Server Configuration")));
	}

	void selectExistingInstance() {
		SWTBotCombo instanceCombo = shell.bot().comboBox(0);
		assertFalse(instanceCombo.isEnabled());

		SWTBotRadio newInstanceButton = shell.bot().radio("Create new instance");
		SWTBotRadio existingInstanceButton = shell.bot().radio("Existing instance");

		assertTrue(newInstanceButton.isEnabled());
		assertTrue(existingInstanceButton.isEnabled());

		deselectDefaultSelection();

		existingInstanceButton.click();
		assertTrue(instanceCombo.isEnabled());
	}

	private SWTBotCombo getExistingInstanceCombo() {
		return shell.bot().comboBox(0);
	}

	/**
	 * Moved from TcServerNewServerWizardUiTest#selectTcServerExistingInstance()
	 */
	void selectExistingInstance(String existingInstanceName) {
		selectExistingInstance();
		SWTBotCombo instanceCombo = getExistingInstanceCombo();

		shell.bot().text(TcServer21WizardFragment.SPECIFY_TC_SERVER_INSTANCE_MESSAGE);

		// BIZARRE; This code:
		// assertTrue(warningLabel.getText().isEqualTo(TcServer20WizardFragment.THE_SPECIFIED_SERVER_DOES_NOT_EXIST));
		// gives the error:
		// "The method isEqualTo(String) is undefined for the type String"

		// Workaround for the above problem; just try looking for the strings;
		// it will throw an exception if those labels are not found.
		instanceCombo.setText("gibberish gibberish");
		// Per this site, you need a space before the text if there
		// is a warning/error icon.
		// http://www.prait.ch/wordpress/?p=251
		shell.bot().text(" " + TcServer20WizardFragment.SERVER_DOES_NOT_EXIST_MESSAGE);
		// Any valid server will have a "lib" directory which is not
		// appropriate to use as an instance directory
		instanceCombo.setText("lib");
		shell.bot().text(" " + TcServer21WizardFragment.INVALID_SERVER_DIR_MESSAGE);
		instanceCombo.setText("");
		shell.bot().text(TcServer21WizardFragment.SPECIFY_TC_SERVER_INSTANCE_MESSAGE);

		instanceCombo.setSelection(existingInstanceName);
	}

	/**
	 * Moved from TcServerNewServerWizardUiTest#selectTcServerNewInstance().
	 */
	void selectTcServerNewInstance() {
		SWTBotCombo instanceCombo = shell.bot().comboBox(0);
		assertFalse(instanceCombo.isEnabled());

		SWTBotRadio newInstanceButton = shell.bot().radio("Create new instance");

		assertTrue(newInstanceButton.isEnabled());

		deselectDefaultSelection();
		shell.bot().radio(0).click(); // newInstanceButton;
		assertFalse(instanceCombo.isEnabled());
	}

	/**
	 * Moved from TcServerNewServerWizardUiTest#deselectDefaultSelection().
	 */
	private void deselectDefaultSelection() {
		// Workaround for bug in SWTBot code, see
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=344484
		UIThreadRunnable.syncExec(new VoidResult() {
			public void run() {
				Matcher<Widget> matcher = AllOf.allOf(WidgetOfType.widgetOfType(Button.class),
						WithStyle.withStyle(SWT.RADIO, "SWT.RADIO"));
				// the default selection
				Button b = (Button) shell.bot().widget(matcher, 0);
				b.setSelection(false);
			}
		});
	}

	void assertServerBrowseButtonEnabled(boolean enabled) {
		assertEquals(enabled, shell.bot().button("Browse...").isEnabled());
	}

	public void setInstanceLocation(String location) {
		SWTBotCombo instanceCombo = getExistingInstanceCombo();
		instanceCombo.setText(location);
	}

	public CreateTcServerInstancePage nextToCreateTcServerInstancePage() {
		shell.bot().button("Next >").click();
		return new CreateTcServerInstancePage(shell);
	}
}
