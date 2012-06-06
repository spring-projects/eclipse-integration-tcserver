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
package com.vmware.vfabric.ide.eclipse.tcserver.internal.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jst.server.tomcat.ui.internal.TomcatRuntimeComposite;
import org.eclipse.jst.server.tomcat.ui.internal.TomcatRuntimeWizardFragment;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.wst.server.core.TaskModel;
import org.eclipse.wst.server.core.internal.RuntimeWorkingCopy;
import org.eclipse.wst.server.ui.wizard.IWizardHandle;

import com.vmware.vfabric.ide.eclipse.tcserver.internal.core.TcServerRuntime;
import com.vmware.vfabric.ide.eclipse.tcserver.internal.core.TcServerUtil;

/**
 * @author Steffen Pingel
 * @author Christian Dupuis
 */
public class TcServerRuntimeWizardFragment extends TomcatRuntimeWizardFragment {

	private Combo serverVersionCombo;

	private RuntimeWorkingCopy wc;

	protected static final String PROP_LOCATION = "location";

	private String lastServerVersion;

	private final PropertyChangeListener locationListener = new PropertyChangeListener() {

		public void propertyChange(PropertyChangeEvent event) {
			if (PROP_LOCATION.equals(event.getPropertyName())) {
				String location = (String) event.getNewValue();
				updateServerVersion(location);
			}
		}
	};

	private IWizardHandle wizard;

	public TcServerRuntimeWizardFragment() {
		this.setComplete(false);
	}

	@Override
	public Composite createComposite(Composite parent, IWizardHandle wizard) {
		this.wizard = wizard;

		Composite composite = super.createComposite(parent, wizard);
		if (TcServerUtil.isSpringSource(wc)) {
			wizard.setTitle("SpringSource tc Server");
		}
		else {
			wizard.setTitle("VMware vFabric tc Server");
		}
		wizard.setImageDescriptor(TcServerImages.WIZB_SERVER);
		if (composite instanceof TomcatRuntimeComposite) {
			Control[] children = composite.getChildren();
			if (children.length >= 3) {
				Control control = children[2];
				if (control instanceof Label) {
					((Label) control).setText("Installation &directory:");
				}
			}
		}

		Label label = new Label(composite, SWT.NONE);
		label.setText("Version:");
		GridData data = new GridData();
		data.horizontalSpan = 2;
		label.setLayoutData(data);

		serverVersionCombo = new Combo(composite, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		serverVersionCombo.setLayoutData(data);
		serverVersionCombo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				(wc).setAttribute(TcServerRuntime.KEY_SERVER_VERSION, serverVersionCombo.getText());
			}
		});

		return composite;
	}

	@Override
	public void enter() {
		super.enter();
		wc = (RuntimeWorkingCopy) getTaskModel().getObject(TaskModel.TASK_RUNTIME);
		wc.addPropertyChangeListener(locationListener);
		if (wc.getLocation() != null) {
			lastServerVersion = wc.getAttribute(TcServerRuntime.KEY_SERVER_VERSION, (String) null);
			updateServerVersion(wc.getLocation().toOSString());
		}
		if (wizard != null) {
			// needs to be set when the page is the current page
			wizard.setImageDescriptor(TcServerImages.WIZB_SERVER);
		}
	}

	@Override
	public void exit() {
		super.exit();
		if (wc != null) {
			wc.removePropertyChangeListener(locationListener);
		}
	}

	private void updateServerVersion(String location) {
		if (serverVersionCombo.getSelectionIndex() != -1) {
			lastServerVersion = serverVersionCombo.getText();
		}
		serverVersionCombo.removeAll();
		wc.setAttribute(TcServerRuntime.KEY_SERVER_VERSION, (String) null);

		File file = new File(location);
		if (file.exists()) {
			boolean initialized = false;
			List<File> serverDirectories = TcServerRuntime.getTomcatVersions(file);
			for (File directory : serverDirectories) {
				String name = directory.getName();
				serverVersionCombo.add(name);
				if (name.equals(lastServerVersion)) {
					serverVersionCombo.select(serverVersionCombo.getItemCount() - 1);
					wc.setAttribute(TcServerRuntime.KEY_SERVER_VERSION, name);
					initialized = true;
				}
			}
			if (!initialized && serverDirectories.size() > 0) {
				serverVersionCombo.select(0);
				wc.setAttribute(TcServerRuntime.KEY_SERVER_VERSION, serverVersionCombo.getItem(0));
			}
		}
	}

	@Override
	public void performFinish(IProgressMonitor monitor) throws CoreException {
		super.performFinish(monitor);
		this.setComplete(false);
	}

}
