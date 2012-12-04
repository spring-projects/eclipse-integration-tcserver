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
import static org.junit.Assert.fail;

import org.eclipse.swtbot.eclipse.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;

/**
 * * Page Object for @link TcServerTemplateConfigurationFragment.
 *
 * @author Tomasz Zarna
 * 
 */
public class TcServerTemplatePropertiesPage extends AbstractTcServerPage {

	TcServerTemplatePropertiesPage(SWTBotShell shell) {
		super(shell);
		shell.bot().waitUntil(Conditions.waitForWidget(withText("Template Configuration")));
	}

	private void assertTemplateName(String templateName) {
		shell.bot().label("Enter properties for template " + templateName + ":");
	}

	private void assertProperty(String propertyPrompt, String propertyValue) {
		assertEquals(propertyValue, getPropertyByPrompt(propertyPrompt).getText());
	}

	void setProperty(String propertyPrompt, String propertyValue) {
		getPropertyByPrompt(propertyPrompt).setText(propertyValue);
	}

	private SWTBotText getPropertyByPrompt(String propertyPrompt) {
		return shell.bot().textWithLabel(propertyPrompt);
	}

	void assertProperties(String templateName) {
		if ("ajp".equals(templateName)) {
			assertTemplateName("ajp");
			assertProperty("Please enter the port that the AJP connector should listen for requests on:", "8009");
			assertProperty("Please enter the port that the AJP connector should redirect SSL requests to:", "8443");
		}
		else if ("diagnostics".equals(templateName)) {
			assertTemplateName("diagnostics");
			assertProperty("Please enter the JNDI name that the diagnostic DataSource should be available at:", "");
			assertProperty(
					"Please enter the username that the diagnostic DataSource should connect to the database with:", "");
			assertProperty(
					"Please enter the password that the diagnostic DataSource should connect to the database with:", "");
			assertProperty(
					"Please enter the JDBC driver class name that the diagnostic DataSource should connect to the database with:",
					"");
			assertProperty(
					"Please enter the JDBC URL that the diagnostic DataSource should connect to the database with:", "");
		}
		else if ("jmx-ssl".equals(templateName)) {
			assertTemplateName("jmx-ssl");
			assertProperty(
					"Please enter the distinguised name the SSL certificate should use. To be prompted for name components leave blank:",
					"");
			assertProperty("Please enter the first and last name the SSL certificate should use:", "Unknown");
			assertProperty("Please enter the organizational unit the SSL certificate should use:", "Unknown");
			assertProperty("Please enter the organization the SSL certificate should use:", "Unknown");
			assertProperty("Please enter the city or locality the SSL certificate should use:", "Unknown");
			assertProperty("Please enter the state or province the SSL certificate should use:", "Unknown");
			assertProperty("Please enter the two-letter country code the SSL certificate should use:", "Unknown");
			assertProperty("Please enter the size in bits that the SSL private key should be:", "2048");
			assertProperty("Please enter the alias that the keystore should refer to the SSL private key as:",
					"tc-server-jmx-ssl");
			assertProperty("Please enter the alias that the keystore refers to the SSL private key as:", "");
			assertProperty("Please enter the password that keystore should protect the SSL private key with:", "");
			assertProperty("Please enter the password that keystore protects the SSL private key with:", "");
			assertProperty("Please enter the path that the SSL keystore should be stored to:",
					"conf/tc-server-jmx-ssl.keystore");
			assertProperty(
					"Please enter the path that the SSL keystore should be read from. To create a new keystore, leave blank:",
					"");
			assertProperty("Please enter the password that the SSL keystore should protect itself with:", "");
			assertProperty("Please enter the password that the SSL keystore protects itself with:", "");
		}
		else {
			fail("Cannot assert properties. Unknown template name: " + templateName);
		}
	}

	void setProperties(String templateName) {
		if ("diagnostics".equals(templateName)) {
			setProperty("Please enter the JNDI name that the diagnostic DataSource should be available at:",
					"jdbc/TestDB");
			setProperty(
					"Please enter the username that the diagnostic DataSource should connect to the database with:",
					"root");
			setProperty(
					"Please enter the password that the diagnostic DataSource should connect to the database with:",
					"password");
			setProperty(
					"Please enter the JDBC driver class name that the diagnostic DataSource should connect to the database with:",
					"com.mysql.jdbc.Driver");
			setProperty(
					"Please enter the JDBC URL that the diagnostic DataSource should connect to the database with:",
					"jdbc:mysql://localhost:3306/mysql?autoReconnect=true");
		}
		else if ("jmx-ssl".equals(templateName)) {
			setProperty("Please enter the alias that the keystore refers to the SSL private key as:", "whatever");
			setProperty("Please enter the password that keystore protects the SSL private key with:", "whatever");
			setProperty("Please enter the password that the SSL keystore protects itself with:", "whatever");
		}
		else {
			fail("Cannot set properties. Unknown template name: " + templateName);
		}
	}

	TcServerTemplatePropertiesPage nextToTcServerTemplatePropertiesPage() {
		shell.bot().button("Next >").click();
		return new TcServerTemplatePropertiesPage(shell);
	}

	TcServerAddRemoveResourcesPage nextToTcServerAddRemoveResourcesPage() {
		shell.bot().button("Next >").click();
		return new TcServerAddRemoveResourcesPage(shell);
	}

	TcServerTemplatePropertiesPage backToTcServerTemplatePropertiesPage() {
		shell.bot().button("< Back").click();
		return new TcServerTemplatePropertiesPage(shell);
	}
}
