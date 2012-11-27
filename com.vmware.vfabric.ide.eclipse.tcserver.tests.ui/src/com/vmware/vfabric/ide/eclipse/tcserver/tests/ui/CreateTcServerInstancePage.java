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

import java.util.Calendar;

import org.eclipse.swtbot.eclipse.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCheckBox;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCombo;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;

import com.vmware.vfabric.ide.eclipse.tcserver.internal.ui.TcServerInstanceConfiguratorPage;

/**
 * Page Object for @link TcServer21InstanceCreationFragment.
 *
 * @author Kaitlin Duck Sherwood
 * @author Tomasz Zarna
 */
public class CreateTcServerInstancePage extends AbstractTcServerPage {

	CreateTcServerInstancePage(SWTBotShell shell) {
		super(shell);
		shell.bot().waitUntil(Conditions.waitForWidget(withText("Create tc Server Instance")));
	}

	void configureNewInstance(final String arbitraryInstanceName) {
		Calendar now = Calendar.getInstance();
		String instanceName = arbitraryInstanceName + now.get(Calendar.SECOND);

		assertMessage(TcServerInstanceConfiguratorPage.ENTER_NAME);

		// De novo, there might not be any existing interfaces
		// bot.textWithLabel("Name:").setText(EXISTING_INTERFACE);
		// bot.text(" " + TcServerInstanceConfiguratorPage.INSTANCE_EXISTS);

		setInstanceName("blah blah blah");
		assertErrorMessage(TcServerInstanceConfiguratorPage.ILLEGAL_SERVER_NAME);

		setInstanceName(instanceName);
		assertMessage(TcServerInstanceConfiguratorPage.SELECT_TEMPLATES);

		shell.bot().radio("Combined"); // check for existence
		shell.bot().radio(0).click();

		selectTemplate("insight"); // no template properties will be added
	}

	void selectTemplate(String... templateNames) {
		for (String templateName : templateNames) {
			shell.bot().table().getTableItem(templateName).toggleCheck();
			shell.bot().table().getTableItem(templateName).click();
		}
	}

	private SWTBotCheckBox getUseDefaultLocationCheckbox() {
		return shell.bot().checkBox("Use default instance location");
	}

	void selectTemplate(String templateName) {
		shell.bot().table().getTableItem(templateName).toggleCheck();
		shell.bot().table().getTableItem(templateName).click();
	}

	void assertUseDefaultServerLocationChecked(boolean isChecked) {
		assertEquals(isChecked, getUseDefaultLocationCheckbox().isChecked());
	}

	private SWTBotCombo getServerLocationCombobox() {
		return shell.bot().comboBox(0);
	}

	void assertServerLocationEnabled(boolean isEnabled) {
		assertEquals(isEnabled, shell.bot().label("Location:").isEnabled());
		assertEquals(isEnabled, getServerLocationCombobox().isEnabled());
	}

	void selectUseDefaultServerLocation(boolean select) {
		SWTBotCheckBox checkbox = getUseDefaultLocationCheckbox();
		if (select) {
			checkbox.select();
		}
		else {
			checkbox.deselect();
		}
	}

	void setInstanceName(String instanceName) {
		shell.bot().textWithLabel("Name:").setText(instanceName);
	}

	void setServerLocation(String location) {
		selectUseDefaultServerLocation(false);
		getServerLocationCombobox().setText(location);
	}

	public TcServerTemplatePropertiesPage nextToTcServerTemplatePropertiesPage() {
		shell.bot().button("Next >").click();
		return new TcServerTemplatePropertiesPage(shell);
	}
}
