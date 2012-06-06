/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package com.vmware.vfabric.ide.eclipse.tcserver.tests.ui;

import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.withText;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.swtbot.eclipse.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.keyboard.Keystrokes;
import org.eclipse.swtbot.swt.finder.matchers.AllOf;
import org.eclipse.swtbot.swt.finder.matchers.WidgetOfType;
import org.eclipse.swtbot.swt.finder.matchers.WithStyle;
import org.eclipse.swtbot.swt.finder.results.VoidResult;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCombo;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotRadio;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTableItem;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.swtbot.swt.finder.widgets.TimeoutException;
import org.eclipse.wst.server.core.IServer;
import org.hamcrest.Matcher;
import org.springsource.ide.eclipse.commons.core.util.OsUtils;
import org.springsource.ide.eclipse.commons.frameworks.test.util.SWTBotUtils;
import org.springsource.ide.eclipse.commons.tests.util.swtbot.StsUiTestCase;

import com.vmware.vfabric.ide.eclipse.tcserver.insight.internal.ui.InsightTcServerCallback;
import com.vmware.vfabric.ide.eclipse.tcserver.internal.ui.TcServer20WizardFragment;
import com.vmware.vfabric.ide.eclipse.tcserver.internal.ui.TcServer21WizardFragment;
import com.vmware.vfabric.ide.eclipse.tcserver.internal.ui.TcServerInstanceConfiguratorPage;
import com.vmware.vfabric.ide.eclipse.tcserver.internal.ui.TcServerUiPlugin;
import com.vmware.vfabric.ide.eclipse.tcserver.tests.support.TcServerFixture;
import com.vmware.vfabric.ide.eclipse.tcserver.tests.support.TcServerHarness;

/**
 * @author Kaitlin Duck Sherwood
 */
public class TcServerNewServerWizardUiTest extends StsUiTestCase {

	// Note: these two messages appear to be buried in TC or Tomcat code.
	private static final String INOFFENSIVE_SERVER_DIR_MESSAGE = "Specify the installation directory";

	private static final String INVALID_TOMCAT_DIR_MESSAGE = "The Tomcat installation directory is not valid. It is missing expected file or folder tcruntime-ctl.sh.";

	private static final String EXISTING_INTERFACE = "spring-insight-instance";

	private static final String ARBITRARY_SERVER_NAME = "TESTasdfSERVERfooDELETE";

	private static final String ARBITRARY_INSTANCE_NAME = "TESTdiamondsINSTANCEemeraldsIGNORE";

	private TcServerFixture fixture;

	private TcServerHarness harness;

	private IServer server;

	private IPath baseInstallDirectoryPath;

	@Override
	public void setUp() throws Exception {

		super.setUp();

		// // Java Type Hierarchy is a very plain view, with no tables.
		SWTBotUtils.openPerspective(bot, "Java Type Hierarchy");
		openServersView();

		if (baseInstallDirectoryPath == null) {

			fixture = TcServerFixture.V_2_7;
			harness = fixture.createHarness();

			server = harness.createServer(TcServerFixture.INST_INSIGHT);

			baseInstallDirectoryPath = ((org.eclipse.wst.server.core.internal.Server) server).getRuntime()
					.getLocation();
		}

	}

	@Override
	protected void tearDown() throws Exception {
		harness.dispose();
		deleteServer(ARBITRARY_SERVER_NAME);
		deleteInstallDirectory(baseInstallDirectoryPath);
		baseInstallDirectoryPath = null;
	}

	// Testing the bulk of the UI in detail
	// ------------------------------------------------------------
	public void testWithExistingServerExistingInstance() throws CoreException {
		doServerCreationTest(true, true, false);
	}

	public void testWithExistingServerWithoutExistingInstance() throws CoreException {
		doServerCreationTest(true, false, false);
	}

	public void testWithoutExistingServerWithoutExistingInstance() throws CoreException {
		doServerCreationTest(false, false, false);
	}

	public void testWithoutExistingServerWithExistingInstance() throws CoreException {
		doServerCreationTest(false, true, false);
	}

	public void testFailedDeployment() throws CoreException {
		doServerCreationTest(false, false, true);
	}

	// The actual meat of the test -----------------------------------
	private void doServerCreationTest(boolean existingServer, boolean existingInstance, boolean corruptedFiles)
			throws CoreException {

		if (existingServer) {
			deleteServer(ARBITRARY_SERVER_NAME);
		}
		else {
			deleteAllServers();
			deleteAllRuntimeConfigurations();
		}

		deleteInstances(baseInstallDirectoryPath, ARBITRARY_INSTANCE_NAME);
		int oldCount = getServerCount();

		if (!existingServer) {
			assertTrue("With a clean start, expects 0 servers", 0 == oldCount);
		}

		openNewServerDialog();

		selectTcServer(ARBITRARY_SERVER_NAME);
		checkFinishButton(false);
		pressButton("Next >");

		if (!existingServer) {
			checkFinishButton(false);
			selectInstallDirectory(baseInstallDirectoryPath.toString());
			checkFinishButton(false);
			pressButton("Next >");
		}

		SWTBotShell newServerWizardShell = bot.activeShell();
		checkFinishButton(false);
		if (existingInstance) {
			selectTcServerExistingInstance();

		}
		else {
			selectTcServerNewInstance();
			checkFinishButton(false);
			pressButton("Next >");
			Calendar now = Calendar.getInstance();
			configureNewInstance(ARBITRARY_INSTANCE_NAME + now.get(Calendar.SECOND));
		}
		checkFinishButton(true);

		if (corruptedFiles) {
			corruptInstallFiles();
		}

		pressButton("Finish");

		try {
			bot.waitUntil(SWTBotUtils.widgetIsDisposed(newServerWizardShell));
		}
		catch (TimeoutException e) {
			// We aren't sure why, but something in SWTBot eats the
			// exception that failing to copy the files throws. This
			// means that the finish fails, which means that the
			// New Server dialog doesn't go away.
			if (corruptedFiles && newServerWizardShell.isVisible()) {
				bot.button("Cancel").click();
			}
		}

		int expectedCount;
		if (corruptedFiles) {
			// Under normal use, a dialog comes up.
			// Something in SWTBot prevents that from happening,
			// so the only way we can tell if things worked right
			// is to look for a server NOT getting created.
			expectedCount = oldCount;
		}
		else {
			expectedCount = oldCount + 1;
		}

		assertEquals("Expects 1 server added", expectedCount, getServerCount());

		if (!corruptedFiles) {
			sanityCheckServer(ARBITRARY_SERVER_NAME);
		}
	}

	// helpers specific to TC server testing --------------------------------

	public void selectTcServer(String newServerName) {

		// the "right" way to do this sometimes fails
		try {
			// "Server").click();
			SWTBotUtils
					.selectChildTreeElement(bot, "New Server", "VMware", "VMware vFabric tc Server v2.5, v2.6, v2.7");

		}
		catch (Exception e) {
			// fallback to doing it the "wrong" way.
			bot.tree().collapseNode("VMware");
			bot.tree().expandNode("VMware").select("VMware vFabric tc Server v2.5, v2.6, v2.7").click();
		}

		bot.textWithLabel("Server name:").setText(newServerName);

	}

	public void selectTcServerExistingInstance() {

		checkFinishButton(false);

		SWTBotCombo instanceCombo = bot.comboBox();
		// bot.Screenshot("/tmp/select.png");
		assertFalse(instanceCombo.isEnabled());

		SWTBotRadio newInstanceButton = bot.radio("Create new instance");
		SWTBotRadio existingInstanceButton = bot.radio("Existing instance");

		assertTrue(newInstanceButton.isEnabled());
		assertTrue(existingInstanceButton.isEnabled());

		deselectDefaultSelection();

		bot.radio(1).click(); // existingInstanceButton;
		assertTrue(instanceCombo.isEnabled());

		bot.text(TcServer21WizardFragment.SPECIFY_TC_SERVER_INSTANCE_MESSAGE);

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
		bot.text(" " + TcServer20WizardFragment.SERVER_DOES_NOT_EXIST_MESSAGE);
		// Any valid server will have a "lib" directory which is not
		// appropriate to use as an instance directory
		instanceCombo.setText("lib");
		bot.text(" " + TcServer21WizardFragment.INVALID_SERVER_DIR_MESSAGE);
		instanceCombo.setText("");
		bot.text(TcServer21WizardFragment.SPECIFY_TC_SERVER_INSTANCE_MESSAGE);

		instanceCombo.setSelection(EXISTING_INTERFACE);

	}

	private void selectInstallDirectory(String installDirectoryName) {
		bot.waitUntil(Conditions.waitForWidget(withText("Cancel")));

		bot.textWithLabel("Installation directory:").setText("nonsense nonsense");
		bot.text(" " + INVALID_TOMCAT_DIR_MESSAGE);

		bot.textWithLabel("Installation directory:").setText(baseInstallDirectoryPath.toString());
		bot.text(INOFFENSIVE_SERVER_DIR_MESSAGE);

	}

	private void selectTcServerNewInstance() {

		bot.waitUntil(Conditions.waitForWidget(withText("tc Server Configuration")));

		checkFinishButton(false);

		SWTBotCombo instanceCombo = bot.comboBox();
		assertFalse(instanceCombo.isEnabled());

		SWTBotRadio newInstanceButton = bot.radio("Create new instance");

		assertTrue(newInstanceButton.isEnabled());

		deselectDefaultSelection();
		bot.radio(0).click(); // newInstanceButton;
		assertFalse(instanceCombo.isEnabled());

	}

	private void configureNewInstance(String instanceName) {
		bot.text(TcServerInstanceConfiguratorPage.ENTER_NAME);

		// De novo, there might not be any existing interfaces
		// bot.textWithLabel("Name:").setText(EXISTING_INTERFACE);
		// bot.text(" " + TcServerInstanceConfiguratorPage.INSTANCE_EXISTS);

		bot.textWithLabel("Name:").setText("blah blah blah");
		bot.text(" " + TcServerInstanceConfiguratorPage.ILLEGAL_SERVER_NAME);

		bot.textWithLabel("Name:").setText(instanceName);
		bot.text(TcServerInstanceConfiguratorPage.SELECT_TEMPLATES);

		bot.radio("Combined"); // check for existence
		bot.radio(0).click();

		// #4 is the "bio-ssl" feature, which works with Insight
		bot.table().getTableItem(5).toggleCheck();
		bot.table().getTableItem(5).click();

	}

	private void sanityCheckServer(String serverName) {
		// Do a sanity check here to see if the server directory has files in it
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IPath workspacePath = workspace.getRoot().getLocation();
		File deploymentDir = workspacePath.append("Servers").append(serverName + "-config").toFile();
		assertTrue(deploymentDir.exists());
		File[] files = deploymentDir.listFiles();
		assertNotNull(files);
		// Files that should be there include catalina.policy,
		// catalina.properties, context.xml,
		// jmxremote.access, jmxremote.password, server.xml, tomcat-users.xml,
		// and web.xml
		assertTrue(files.length > 7);

	}

	private void checkForInsightDialog(String serverName) {
		SWTBotTreeItem treeItem = selectServer(serverName);
		// SWTBotShell parentShell = bot.activeShell();

		treeItem.contextMenu("Start").click();
		bot.sleep(1000);
		bot.waitUntil(Conditions.shellIsActive(InsightTcServerCallback.WANT_INSIGHT_DIALOG_TITLE));
		bot.button("Yes").click();

	}

	// helpers generic to server testing (but not TC testing
	// ------------------------

	private void openNewServerDialog() {

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
	}

	private SWTBotTreeItem selectServer(String serverName) {
		SWTBotUtils.getView(bot, "Servers");

		SWTBotTree aTree = getServerTree();
		if (aTree == null) {
			return null;
		}

		for (SWTBotTreeItem treeItem : aTree.getAllItems()) {
			String serverString = treeItem.getText();
			if (serverString == null) {
				return null;
			}
			String treeServerName = serverString.split("\\[")[0].trim();

			if (serverName.compareTo(treeServerName) == 0) {
				// This seems somewhat convoluted, but a more straightforward
				// approach led to the first server being deleted, NOT the
				// one named serverName.
				return treeItem.select();
			}

		}
		return null;
	}

	private void stopServer(String serverName) {
		SWTBotTreeItem treeItem = selectServer(serverName);
		assertNotNull(treeItem);

		treeItem.contextMenu("Stop").click();

	}

	private void deleteServer(String serverName) {
		SWTBotTreeItem treeItem;
		try {
			treeItem = selectServer(serverName);
		}
		catch (WidgetNotFoundException e) {
			// if there is no server by that name, mission accomplished!
			return;
		}
		// if there is no server by that name, mission accomplished!
		if (treeItem != null) {
			treeItem.contextMenu("Delete").click();
			bot.button("OK").click();

		}

	}

	private void deleteAllServers() {
		SWTBotUtils.getView(bot, "Servers");

		SWTBotTree aTree = getServerTree();
		if (aTree == null) {
			return;
		}

		for (SWTBotTreeItem treeItem : getServerTree().getAllItems()) {
			treeItem.contextMenu("Delete").click();
			bot.button("OK").click();
		}

	}

	// if we create a new instance in the file system, we need to remove it
	private void deleteInstances(IPath installDirectory, String instanceName) {
		File instanceDirectory = installDirectory.append(instanceName).toFile();
		try {
			FileUtils.deleteDirectory(instanceDirectory);
		}
		catch (IOException e) {
			// If the directory is already gone, that's perhaps
			// a little odd, but not dangerous.
		}
	}

	// if we create a new instance in the file system, we need to remove it
	private void deleteInstallDirectory(IPath installDirectory) {

		try {
			FileUtils.deleteDirectory(installDirectory.toFile());
		}
		catch (IOException e) {
			// If the directory is already gone, that's perhaps
			// a little odd, but not dangerous.
		}
	}

	private void deleteAllRuntimeConfigurations() throws CoreException {

		openPreferences();
		SWTBotTreeItem runtimeEntry = SWTBotUtils.selectChildTreeElement(bot, "Preferences", "Server",
				"Runtime Environments");
		runtimeEntry.select().click();

		// The condition expression is evaluated on every iteration of the loop,
		// while the table changes with every iteration of the loop.
		int rowCount = bot.table().rowCount();
		for (int i = 0; i < rowCount; i++) {
			// index 0 because the table changes every iteration of the loop
			SWTBotTableItem item = bot.table().getTableItem(0).select();

			bot.waitUntil(new DefaultCondition() {
				public boolean test() throws Exception {
					SWTBotButton removeButton = bot.button("Remove");
					try {
						bot.shell("Preferences");
					}
					catch (WidgetNotFoundException e) {
						// if there is no dialog, carry on
					}
					return removeButton.isEnabled();
				} // end test()

				public String getFailureMessage() {
					return "Was expecting the Remove button to become active";
				}
			});
			bot.button("Remove").click();
			// Sigh, sometimes you get a dialog asking if it is okay to remove;
			// sometimes you don't.
			if (bot.activeShell().getText().compareTo("Preferences") != 0) {
				pressButton("OK");
			}

		}

		bot.button("OK").click(); // close the Preferences dialog

	}

	// NOTE: which tree (i.e. bot.tree(1), bot.tree(2) )
	// varies based on which perspective you are in.
	// Worse, the order of the trees that are returned seems to change for
	// different unit tests. I am getting around it now by opening a perspective
	// that has no other trees in it, so the Servers view should be the only
	// tree
	// around.
	private SWTBotTree getServerTree() {
		return bot.tree();
	}

	private int getServerCount() {
		bot.waitUntil(Conditions.waitForWidget(withText("Servers")));

		// bot.sleep(1000); // We shouldn't need a delay here, but we do.
		SWTBotUtils.getView(bot, "Servers").setFocus();

		try {
			return getServerTree().getAllItems().length;
		}
		catch (Exception e) {
			return 0;

		}

	}

	private void openServersView() {
		SWTBotUtils.menu(bot, "Window").menu("Show View").menu("Other...").click();
		bot.waitUntil(Conditions.shellIsActive("Show View"));
		// The "right" way frequently doesn't work, and this
		// looks like a bug: the Server node opens, but there is a blank
		// node under "Servers".
		try {
			SWTBotUtils.selectChildTreeElement(bot, "Show View", "Server", "Servers").click();
		}
		catch (Exception e) {
			// Fall back to trying it the "wrong" way.
			// (Unfortunately, the "wrong" way sometimes doesn't work,
			// so we can't use it instead of the "right" way.)
			bot.tree().collapseNode("Server");
			bot.tree().expandNode("Server").select("Servers").click();
		}
		bot.button("OK").click();
	}

	private void corruptInstallFiles() {

		try {
			FileUtils.deleteDirectory(baseInstallDirectoryPath.append("templates").toFile());
			FileUtils.deleteDirectory(baseInstallDirectoryPath.append("spring-insight-instance").append("conf")
					.toFile());
		}
		catch (IOException e) {
			// The test should catch problems later if the source is corrupt.
			// If the install dir starts out corrupt, we have bigger problems.
		}

		IPath insightDirPath = baseInstallDirectoryPath.append("spring-insight-instance");
		// not strictly needed to make the test work; deleting directories does
		// an adequate job
		// does just fine
		// makeSubDirUnreadable(insightDirPath, "lib");
		// makeSubDirUnreadable(insightDirPath, "bin");
		// makeSubDirUnreadable(baseInstallDirectoryPath, "bash_completion");

	}

	// helpers generic to non-server SWTBot code
	// -------------------------------------------------------

	private void pressButton(String buttonTitle) {
		SWTBotButton aButton;
		try {
			aButton = bot.button(buttonTitle);
			assertTrue(aButton.isEnabled());
			aButton.click();
		}
		catch (WidgetNotFoundException e) {
			// purely to speed debugging a little bit
			assertTrue(false); // toss an assertion error
		}

	}

	private void checkFinishButton(Boolean isEnabled) {
		SWTBotButton finishButton = bot.button("Finish");
		assertNotNull(finishButton);
		assertTrue(finishButton.isEnabled() == isEnabled);
	}

	// Workaround for bug in SWTBot code, see
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=344484
	void deselectDefaultSelection() {
		UIThreadRunnable.syncExec(new VoidResult() {

			public void run() {
				Matcher<Widget> matcher = AllOf.allOf(WidgetOfType.widgetOfType(Button.class),
						WithStyle.withStyle(SWT.RADIO, "SWT.RADIO"));

				Button b = (Button) bot.widget(matcher, 0); // the default
															// selection
				b.setSelection(false);

			}

		});
	}

	/**
	 * This method will open the preferences dialog.
	 * 
	 */
	private void openPreferences() throws CoreException {

		/*
		 * // NOTE: bot.menu("Eclipse").menu("Preferences...").click(); DID NOT
		 * WORK on Mac.
		 */
		// "carbon".equals(SWT.getPlatform()) ||

		if (OsUtils.isWindows()) {
			bot.waitUntil(Conditions.shellIsActive("Java Type Hierarchy - SpringSource Tool Suite"));
			bot.menu("Window").menu("Preferences").click();
		}
		else {
			if ("carbon".equals(SWT.getPlatform()) || "cocoa".equals(SWT.getPlatform())) {
				try {
					bot.activeShell().pressShortcut(Keystrokes.MOD1, KeyStroke.getInstance(","));
				}
				catch (WidgetNotFoundException e) {
					System.err.println("Can't find activeShell");
				}
				catch (ParseException pe) {
					System.err.println("Error parsing \",\"");
				}
			}
			else if ("gtk".equals(SWT.getPlatform())) {

				bot.waitUntil(Conditions.shellIsActive("Java Type Hierarchy - SpringSource Tool Suite"));
				bot.menu("Window").menu("Preferences").click();
			}
			else {
				IStatus status = new Status(Status.ERROR, TcServerUiPlugin.PLUGIN_ID,
						"Unknown operating system: must be Linux (gtk), Windows, or Mac (cocoa or carbon), yet found: \""
								+ SWT.getPlatform());
				throw new CoreException(status);
			}
		}
		bot.waitUntil(Conditions.shellIsActive("Preferences"));
	}

	// helpers which could go anywhere
	// ------------------------------------------

	// private void makeSubDirUnreadable(IPath baseInstallDirectory, String
	// subDirName) {
	// File subDir =
	// baseInstallDirectory.append(subDirName).addTrailingSeparator().toFile();
	// assertTrue(subDir.exists() && subDir.isDirectory());
	// File[] configFiles = subDir.listFiles();
	// assertTrue(configFiles.length > 0);
	// for (File aFile : configFiles) {
	// aFile.setReadable(false);
	// }
	// }

}
