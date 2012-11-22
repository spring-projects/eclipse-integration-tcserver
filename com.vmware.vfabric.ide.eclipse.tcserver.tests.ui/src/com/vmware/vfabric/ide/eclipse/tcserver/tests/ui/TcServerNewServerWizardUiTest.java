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
import org.eclipse.swtbot.eclipse.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.keyboard.Keystrokes;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTableItem;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.wst.server.core.IServer;
import org.springsource.ide.eclipse.commons.core.util.OsUtils;
import org.springsource.ide.eclipse.commons.frameworks.test.util.SWTBotUtils;
import org.springsource.ide.eclipse.commons.tests.util.swtbot.StsUiTestCase;

import com.vmware.vfabric.ide.eclipse.tcserver.insight.internal.ui.InsightTcServerCallback;
import com.vmware.vfabric.ide.eclipse.tcserver.internal.ui.TcServerUiPlugin;
import com.vmware.vfabric.ide.eclipse.tcserver.tests.support.TcServerFixture;
import com.vmware.vfabric.ide.eclipse.tcserver.tests.support.TcServerHarness;

/**
 * @author Kaitlin Duck Sherwood
 * @author Tomasz Zarna
 */
public class TcServerNewServerWizardUiTest extends StsUiTestCase {

	private static final String BASE_INSTANCE = "base-instance";

	// TODO: for now, until STS-2986 gets fixed
	private static String ARBITRARY_SERVER_NAME = DefineNewServerPage.VMWARE_VFABRIC_TC_SERVER_V25_V26_V27_V28
			+ " at localhost"; // "TESTasdfSERVERfooDELETE";

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

			fixture = TcServerFixture.V_2_8;
			harness = fixture.createHarness();

			server = harness.createServer(BASE_INSTANCE);

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
	public void testWithExistingServerWithExistingInstance() throws CoreException {
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
			assertEquals("With a clean start, expects 0 servers", 0, oldCount);
		}

		NewServerWizard newServerWizard = NewServerWizard.openWizard();

		DefineNewServerPage defineNewServerPage = newServerWizard.getDefineNewServerPage();
		defineNewServerPage.selectTcServer();
		ARBITRARY_SERVER_NAME = defineNewServerPage.getServerName();
		assertFalse(newServerWizard.isFinishEnabled());

		TcServerConfigurationPage tcServerConfigurationPage = null;
		if (!existingServer) {
			VMwareVFabricTcServerPage vMwareVFabricTcServerPage = defineNewServerPage.nextToVMwareVFabricTcServerPage();
			assertFalse(newServerWizard.isFinishEnabled());
			vMwareVFabricTcServerPage.selectInstallDirectory(baseInstallDirectoryPath);
			assertFalse(newServerWizard.isFinishEnabled());
			tcServerConfigurationPage = vMwareVFabricTcServerPage.nextToTcServerConfigurationPage();
		}
		else {
			tcServerConfigurationPage = defineNewServerPage.nextToTcServerConfigurationPage();
		}

		assertFalse(newServerWizard.isFinishEnabled());
		if (existingInstance) {
			tcServerConfigurationPage.selectExistingInstance(BASE_INSTANCE);
		}
		else {
			tcServerConfigurationPage.selectTcServerNewInstance();
			assertFalse(newServerWizard.isFinishEnabled());
			CreateTcServerInstancePage createTcServerInstancePage = tcServerConfigurationPage
					.nextToCreateTcServerInstancePage();
			createTcServerInstancePage.configureNewInstance(ARBITRARY_INSTANCE_NAME);
		}
		assertTrue(newServerWizard.isFinishEnabled());

		if (corruptedFiles) {
			corruptInstallFiles();
		}

		newServerWizard.pressFinish(corruptedFiles, "Error creating server instance. Check access permission .*");

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

	private void sanityCheckServer(String serverName) {
		// Do a sanity check here to see if the server directory has files in it
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IPath workspacePath = workspace.getRoot().getLocation();
		File deploymentDir = workspacePath.append("Servers").append(serverName + "-config").toFile();
		assertTrue("Directory '" + deploymentDir + "' does not exist", deploymentDir.exists());
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
	// tree around.
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
		// an adequate job does just fine
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
			bot.waitUntil(Conditions.shellIsActive("Java Type Hierarchy - Spring Tool Suite"));
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

				bot.waitUntil(Conditions.shellIsActive("Java Type Hierarchy - Spring Tool Suite"));
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
}
