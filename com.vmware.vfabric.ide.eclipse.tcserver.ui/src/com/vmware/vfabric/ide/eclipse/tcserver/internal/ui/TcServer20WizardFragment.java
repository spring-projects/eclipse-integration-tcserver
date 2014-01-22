/*******************************************************************************
 * Copyright (c) 2012 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package com.vmware.vfabric.ide.eclipse.tcserver.internal.ui;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.layout.GridDataFactory;
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
 */
public class TcServer20WizardFragment extends WizardFragment {

	public static final String SERVER_DOES_NOT_EXIST_MESSAGE = "The specified server does not exist.";

	private static final String SERVER_PATH = ".";

	private IServerWorkingCopy wc;

	private IWizardHandle wizard;

	private Combo serverNameCombo;

	private Button asfLayoutButton;

	private Button s2LayoutButton;

	private Label serverNameLabel;

	public TcServer20WizardFragment() {
	}

	@Override
	public Composite createComposite(Composite parent, IWizardHandle wizard) {
		this.wizard = wizard;

		wizard.setTitle("tc Server Configuration");
		wizard.setDescription("Specify the server instance.");
		wizard.setImageDescriptor(TcServerImages.WIZB_SERVER);

		Composite composite = new Composite(parent, SWT.NONE);

		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		asfLayoutButton = new Button(composite, SWT.RADIO);
		asfLayoutButton.setText("ASF Layout");
		asfLayoutButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (asfLayoutButton.getSelection()) {
					((ServerWorkingCopy) wc).setAttribute(TcServer.KEY_ASF_LAYOUT, true);
					s2LayoutButton.setSelection(false);
					updateButtons();
					validate();
				}
			}
		});
		GridDataFactory.fillDefaults().span(2, 1).applyTo(asfLayoutButton);

		s2LayoutButton = new Button(composite, SWT.RADIO);
		s2LayoutButton.setText("SpringSource Layout");
		s2LayoutButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (s2LayoutButton.getSelection()) {
					((ServerWorkingCopy) wc).setAttribute(TcServer.KEY_ASF_LAYOUT, false);
					asfLayoutButton.setSelection(false);
					updateButtons();
					validate();
				}
			}
		});
		GridDataFactory.fillDefaults().span(2, 1).applyTo(s2LayoutButton);

		serverNameLabel = new Label(composite, SWT.NONE);
		serverNameLabel.setText("Server Name:");
		GridData data = new GridData();
		serverNameLabel.setLayoutData(data);

		serverNameCombo = new Combo(composite, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		serverNameCombo.setLayoutData(data);
		serverNameCombo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				((ServerWorkingCopy) wc).setAttribute(TcServer.KEY_SERVER_NAME, serverNameCombo.getText());
				validate();
			}
		});

		initialize();

		return composite;
	}

	private IStatus doValidate() {
		if (wc != null) {
			boolean asfLayout = ((ServerWorkingCopy) wc).getAttribute(TcServer.KEY_ASF_LAYOUT, true);
			if (asfLayout) {
				return Status.OK_STATUS;
			}
			else {
				String serverName = ((ServerWorkingCopy) wc).getAttribute(TcServer.KEY_SERVER_NAME, (String) null);
				if (serverName != null) {
					IPath path = wc.getRuntime().getLocation();
					File directory = new File(path.toFile(), SERVER_PATH);
					if (directory.exists()) {
						File file = new File(directory, serverName);
						if (file.exists()) {
							return TcServerUtil.validateInstance(file, false);
						}
					}
				}
			}
		}
		return new Status(IStatus.ERROR, ITcServerConstants.PLUGIN_ID, SERVER_DOES_NOT_EXIST_MESSAGE);
	}

	@Override
	public void enter() {
		this.wc = (IServerWorkingCopy) getTaskModel().getObject(TaskModel.TASK_SERVER);
		initialize();
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

	private void initialize() {
		if (wc != null && serverNameCombo != null) {
			boolean asfLayout = ((ServerWorkingCopy) wc).getAttribute(TcServer.KEY_ASF_LAYOUT, true);
			asfLayoutButton.setSelection(asfLayout);
			s2LayoutButton.setSelection(!asfLayout);

			// add all directories that have a server configuration
			serverNameCombo.removeAll();
			IPath path = wc.getRuntime().getLocation();
			File file = new File(path.toFile(), SERVER_PATH);
			if (file.exists()) {
				File[] serverDirectories = file.listFiles();
				for (File directory : serverDirectories) {
					if (directory.isDirectory() && new File(directory, "conf").exists()
							&& !new File(directory, "TOMCAT_LICENSE.txt").exists()) {
						serverNameCombo.add(directory.getName());
					}
				}
			}
		}
		updateButtons();
	}

	@Override
	public boolean isComplete() {
		if (wc == null) {
			return false;
		}
		return doValidate().isOK();
	}

	protected void updateButtons() {
		serverNameCombo.setEnabled(s2LayoutButton.getSelection());
		serverNameLabel.setEnabled(s2LayoutButton.getSelection());
	}

	protected void validate() {
		if (wc == null) {
			wizard.setMessage("", IMessageProvider.ERROR);
			return;
		}

		IStatus status = doValidate();
		if (status == null || status.isOK()) {
			wizard.setMessage(null, IMessageProvider.NONE);
		}
		else if (status.getSeverity() == IStatus.WARNING) {
			wizard.setMessage(status.getMessage(), IMessageProvider.WARNING);
		}
		else {
			wizard.setMessage(status.getMessage(), IMessageProvider.ERROR);
		}
		wizard.update();
	}

	@Override
	public void performFinish(IProgressMonitor monitor) throws CoreException {
		// reset completion status in case the wizard is re-used
		setComplete(false);

		// re-trigger import of configuration to propagate exception
		TcServerUtil.importRuntimeConfiguration(wc, monitor);
	}

}
