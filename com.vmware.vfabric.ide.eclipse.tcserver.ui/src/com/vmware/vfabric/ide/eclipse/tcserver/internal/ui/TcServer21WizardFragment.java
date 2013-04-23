/*******************************************************************************
 *  Copyright (c) 2012 - 2013 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package com.vmware.vfabric.ide.eclipse.tcserver.internal.ui;

import java.io.File;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jst.server.tomcat.core.internal.ITomcatServer;
import org.eclipse.jst.server.tomcat.core.internal.TomcatServer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.TaskModel;
import org.eclipse.wst.server.core.internal.ServerWorkingCopy;
import org.eclipse.wst.server.ui.wizard.IWizardHandle;
import org.eclipse.wst.server.ui.wizard.WizardFragment;
import org.springsource.ide.eclipse.commons.core.StatusHandler;

import com.vmware.vfabric.ide.eclipse.tcserver.internal.core.ITcServerConstants;
import com.vmware.vfabric.ide.eclipse.tcserver.internal.core.TcServer;
import com.vmware.vfabric.ide.eclipse.tcserver.internal.core.TcServerUtil;

/**
 * @author Steffen Pingel
 * @author Christian Dupuis
 * @author Tomasz Zarna
 */
public class TcServer21WizardFragment extends WizardFragment {

	private static final String SELECT_INSTANCE_MESSAGE = "Select an instance.";

	public static final String INVALID_SERVER_DIR_MESSAGE = "The specified server is not valid. The .tc-runtime-instance file is missing.";

	public static final String SPECIFY_TC_SERVER_INSTANCE_MESSAGE = "Specify the tc Server instance.";

	public static final String SERVER_DOES_NOT_EXIST_MESSAGE = "The specified server does not exist.";

	private static final String SERVER_PATH = ".";

	private Label descriptionLabel;

	private Combo serverNameCombo;

	private Label serverNameLabel;

	private Button serverBrowseButton;

	private IServerWorkingCopy wc;

	private IWizardHandle wizard;

	private Button existingInstanceButton;

	private Button newInstanceButton;

	private TcServer21InstanceCreationFragment instanceCreationPage;

	private boolean creatingNewInstance;

	public TcServer21WizardFragment() {
		this.setComplete(false);
	}

	@Override
	public Composite createComposite(Composite parent, IWizardHandle wizard) {
		this.wizard = wizard;

		wizard.setTitle("tc Server Configuration");
		wizard.setDescription(SPECIFY_TC_SERVER_INSTANCE_MESSAGE);
		wizard.setImageDescriptor(TcServerImages.WIZB_SERVER);

		Composite composite = new Composite(parent, SWT.NONE);

		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		newInstanceButton = new Button(composite, SWT.RADIO);
		newInstanceButton.setSelection(true);
		newInstanceButton.setText("Create new instance");
		GridDataFactory.fillDefaults().span(3, 1).applyTo(newInstanceButton);
		newInstanceButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				creatingNewInstance = newInstanceButton.getSelection();
				updateChildFragments();
				validate();
				serverNameCombo.setEnabled(false);
				serverNameLabel.setEnabled(false);
				serverBrowseButton.setEnabled(false);
			}
		});
		creatingNewInstance = true;

		existingInstanceButton = new Button(composite, SWT.RADIO);
		existingInstanceButton.setText("Existing instance");
		existingInstanceButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateChildFragments();
				validate();
				serverNameCombo.setEnabled(true);
				serverNameLabel.setEnabled(true);
				serverBrowseButton.setEnabled(true);
			}
		});
		GridDataFactory.fillDefaults().span(3, 1).applyTo(existingInstanceButton);

		serverNameLabel = new Label(composite, SWT.NONE);
		serverNameLabel.setText("Instance:");
		GridData data = new GridData();
		serverNameLabel.setLayoutData(data);

		serverNameCombo = new Combo(composite, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		serverNameCombo.setLayoutData(data);
		serverNameCombo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				IPath serverPath = new Path(serverNameCombo.getText());
				if (!serverPath.isEmpty() && serverPath.isAbsolute()) {
					((ServerWorkingCopy) wc).setAttribute(ITomcatServer.PROPERTY_INSTANCE_DIR,
							serverNameCombo.getText());
					((ServerWorkingCopy) wc).setAttribute(TcServer.KEY_SERVER_NAME, "");
				}
				else {
					((ServerWorkingCopy) wc).setAttribute(ITomcatServer.PROPERTY_INSTANCE_DIR, (String) null);
					((ServerWorkingCopy) wc).setAttribute(TcServer.KEY_SERVER_NAME, serverNameCombo.getText());
				}
				validate();
			}
		});

		serverBrowseButton = new Button(composite, SWT.PUSH);
		serverBrowseButton.setText("Browse...");
		serverBrowseButton.setLayoutData(new GridData());
		serverBrowseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				handleLocationBrowseButtonPressed();
				validate();
			}
		});

		serverNameCombo.setEnabled(false);
		serverNameLabel.setEnabled(false);
		serverBrowseButton.setEnabled(false);

		Label separator = new Label(composite, SWT.NONE);
		GridDataFactory.fillDefaults().span(3, 1).applyTo(separator);

		descriptionLabel = new Label(composite, SWT.WRAP);
		descriptionLabel.setBackground(composite.getBackground());
		GridDataFactory.fillDefaults().span(3, 1).grab(true, false).applyTo(descriptionLabel);

		initialize();

		return composite;
	}

	private void handleLocationBrowseButtonPressed() {
		File path = wc.getRuntime().getLocation().toFile();

		DirectoryDialog dialog = new DirectoryDialog(serverBrowseButton.getShell());
		dialog.setMessage("Select location");
		if (path.exists()) {
			dialog.setFilterPath(path.toString());
		}
		String selectedDirectory = dialog.open();
		if (selectedDirectory != null) {
			serverNameCombo.add(selectedDirectory, 0);
			serverNameCombo.select(0);
		}
	}

	@Override
	protected void createChildFragments(List<WizardFragment> list) {
		if (newInstanceButton != null && newInstanceButton.getSelection()) {
			if (instanceCreationPage == null) {
				instanceCreationPage = new TcServer21InstanceCreationFragment();
			}
			list.add(instanceCreationPage);
		}
	}

	@Override
	public void enter() {
		this.wc = (IServerWorkingCopy) getTaskModel().getObject(TaskModel.TASK_SERVER);
		initialize();
		updateChildFragments();
		validate();
	}

	@Override
	public void exit() {
		try {
			// load the configuration from the directory based on the selections
			// made on the wizard page
			((ServerWorkingCopy) wc).importRuntimeConfiguration(wc.getRuntime(), null);
		}
		catch (CoreException e) {
			StatusHandler.log(new Status(IStatus.ERROR, TcServerUiPlugin.PLUGIN_ID,
					"Failed to load runtime configuration", e));
			// Trace.trace(Trace.SEVERE, "Failed to load runtime configuration",
			// e);
		}
	}

	@Override
	public boolean hasComposite() {
		return true;
	}

	private File getInstanceDirectory() {
		if (wc != null) {
			String instanceDir = ((ServerWorkingCopy) wc).getAttribute(TomcatServer.PROPERTY_INSTANCE_DIR,
					(String) null);
			if (instanceDir != null) {
				File file = new File(instanceDir);
				if (file.exists()) {
					return file;
				}
			}
			String serverName = ((ServerWorkingCopy) wc).getAttribute(TcServer.KEY_SERVER_NAME, (String) null);
			if (serverName != null) {
				IPath path = wc.getRuntime().getLocation();
				File directory = new File(path.toFile(), SERVER_PATH);
				if (directory.exists()) {
					File file = new File(directory, serverName);
					if (file.exists()) {
						return file;
					}
				}
			}
		}
		return null;
	}

	private IStatus doValidate() {
		if (newInstanceButton != null && newInstanceButton.getSelection()) {
			return Status.OK_STATUS;
		}

		if (((ServerWorkingCopy) wc).getAttribute(TcServer.KEY_SERVER_NAME, (String) null) == null
				&& ((ServerWorkingCopy) wc).getAttribute(ITomcatServer.PROPERTY_INSTANCE_DIR, (String) null) == null) {
			return new Status(IStatus.INFO, ITcServerConstants.PLUGIN_ID, SELECT_INSTANCE_MESSAGE);
		}

		File file = getInstanceDirectory();
		if (file != null && file.exists()) {
			return TcServerUtil.validateInstance(file, true);
		}
		return new Status(IStatus.ERROR, ITcServerConstants.PLUGIN_ID, SERVER_DOES_NOT_EXIST_MESSAGE);
	}

	private void initialize() {
		existingInstanceButton.setEnabled(false);
		if (wc != null && serverNameCombo != null) {
			// add all directories that have a server configuration
			serverNameCombo.removeAll();
			IPath path = wc.getRuntime().getLocation();
			File file = new File(path.toFile(), SERVER_PATH);
			if (file.exists()) {
				File[] serverDirectories = file.listFiles();
				if (serverDirectories != null) {
					for (File directory : serverDirectories) {
						if (directory.isDirectory() && new File(directory, ".tc-runtime-instance").exists()) {
							serverNameCombo.add(directory.getName());
						}
					}
				}
			}
			existingInstanceButton.setEnabled(true);
		}
	}

	private void updateDescription(IStatus status) {
		if (status.isOK() && existingInstanceButton != null && existingInstanceButton.getSelection()) {
			File directory = getInstanceDirectory();
			if (directory != null) {
				File libDirectory = new File(directory, "lib");
				if (new File(libDirectory, "catalina.jar").exists()) {
					descriptionLabel.setText("This instance is configured for combined layout.");
				}
				else {
					descriptionLabel.setText("This instance is configured for separate layout.");
				}
				return;
			}
		}
		descriptionLabel.setText("");
	}

	protected void validate() {
		if (!newInstanceButton.getSelection() && serverNameCombo.getText().length() == 0) {
			wizard.setMessage(SPECIFY_TC_SERVER_INSTANCE_MESSAGE, IMessageProvider.NONE);
			setComplete(false);
			return;
		}

		if (wc == null) {
			wizard.setMessage("", IMessageProvider.ERROR);
			setComplete(false);
			return;
		}

		IStatus status = doValidate();
		if (status == null || status.isOK()) {
			wizard.setMessage(null, IMessageProvider.NONE);
			setComplete(true);
		}
		else if (status.getSeverity() == IStatus.INFO) {
			wizard.setMessage(status.getMessage(), IMessageProvider.NONE);
			setComplete(false);
		}
		else if (status.getSeverity() == IStatus.WARNING) {
			wizard.setMessage(status.getMessage(), IMessageProvider.WARNING);
			setComplete(false);
		}
		else {
			wizard.setMessage(status.getMessage(), IMessageProvider.ERROR);
			setComplete(false);
		}
		updateDescription(status);
		wizard.update();
	}

	@Override
	public void performFinish(IProgressMonitor monitor) throws CoreException {
		// reset completion status in case the wizard is re-used
		setComplete(false);

		if (creatingNewInstance) {
			// instance creation is handled by
			// TcServer21InstanceCreationFragment
			return;
		}
		// re-trigger import of configuration to propagate exception
		TcServerUtil.importRuntimeConfiguration(wc, monitor);
	}

}
