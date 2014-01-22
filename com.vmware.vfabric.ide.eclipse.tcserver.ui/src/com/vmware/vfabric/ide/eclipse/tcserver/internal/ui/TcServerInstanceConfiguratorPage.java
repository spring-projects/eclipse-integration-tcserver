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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.vmware.vfabric.ide.eclipse.tcserver.internal.core.TcServer;
import com.vmware.vfabric.ide.eclipse.tcserver.internal.ui.TcServerInstanceConfiguratorPage.InstanceConfiguration.Layout;

/**
 * @author Steffen Pingel
 * @author Christian Dupuis
 */
public class TcServerInstanceConfiguratorPage extends WizardPage {

	public static final String SELECT_TEMPLATES = "Select one or more templates.";

	public static final String INSTANCE_EXISTS = "An instance with that name already exists.";

	public static final String ENTER_NAME = "Enter a name for the instance.";

	public static final String ILLEGAL_SERVER_NAME = "The name must consist only of alphanumeric characters, dash, and underscore and cannot start with an underscore.";

	public static class InstanceConfiguration {

		public enum Layout {
			SEPARATE, COMBINED
		};

		private List<String> templates;

		private Layout layout;

		private String name;

		public List<String> getTemplates() {
			return Collections.unmodifiableList(templates);
		}

		public Layout getLayout() {
			return layout;
		}

		public String getName() {
			return name;
		}

	}

	private CheckboxTableViewer templateViewer;

	protected List<String> templates = Collections.emptyList();

	private Button separateLayoutButton;

	private Button combinedLayoutButton;

	private final TcServer tcServer;

	private Text nameText;

	protected TcServerInstanceConfiguratorPage(TcServer tcServer) {
		super("CreateInstance");
		this.tcServer = tcServer;
		setTitle("Create tc Server Instance");
		setDescription("Specify instance parameters.");
		setImageDescriptor(TcServerImages.WIZB_SERVER);
	}

	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));

		Label label = new Label(composite, SWT.NONE);
		label.setText("Name:");

		nameText = new Text(composite, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(nameText);
		nameText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				validate();
			}
		});

		label = new Label(composite, SWT.NONE);
		GridDataFactory.fillDefaults().span(2, 1).applyTo(label);
		label.setText("Templates:");

		templateViewer = CheckboxTableViewer.newCheckList(composite, SWT.BORDER);
		GridDataFactory.fillDefaults().span(2, 1).grab(true, true).applyTo(templateViewer.getControl());
		templateViewer.setContentProvider(new IStructuredContentProvider() {
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				// ignore
			}

			public void dispose() {
				// ignore
			}

			public Object[] getElements(Object inputElement) {
				return templates.toArray();
			}
		});
		templateViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				validate();
			}
		});
		Group layoutGroup = new Group(composite, SWT.BORDER);
		GridDataFactory.fillDefaults().span(2, 1).grab(true, false).applyTo(layoutGroup);
		layoutGroup.setLayout(new GridLayout(1, false));
		layoutGroup.setText("Layout");

		separateLayoutButton = new Button(layoutGroup, SWT.RADIO);
		separateLayoutButton.setText("Separate");

		combinedLayoutButton = new Button(layoutGroup, SWT.RADIO);
		combinedLayoutButton.setText("Combined");

		initialize();
		validate();

		Dialog.applyDialogFont(composite);
		setControl(composite);
	}

	protected void validate() {
		if (nameText.getText().trim().length() == 0) {
			setMessage(ENTER_NAME);
		}
		else if (instanceExists()) {
			setMessage(INSTANCE_EXISTS, IMessageProvider.ERROR);
		}
		else if (templateViewer.getCheckedElements().length == 0) {
			setMessage(SELECT_TEMPLATES);
		}
		else {
			setMessage(null);
		}
		setPageComplete(getMessage() == null);
	}

	private boolean instanceExists() {
		IPath path = tcServer.getServer().getRuntime().getLocation().append(nameText.getText());
		return path.toFile().exists();
	}

	public InstanceConfiguration getModel() {
		InstanceConfiguration model = new InstanceConfiguration();
		model.name = nameText.getText();
		model.templates = new ArrayList<String>();
		for (Object element : templateViewer.getCheckedElements()) {
			model.templates.add((String) element);
		}
		model.layout = (separateLayoutButton.getSelection()) ? Layout.SEPARATE : Layout.COMBINED;
		return model;
	}

	private void initialize() {
		separateLayoutButton.setSelection(true);

		IPath runtimePath = tcServer.getServer().getRuntime().getLocation();
		IPath templatePath = runtimePath.append("templates");
		if (templatePath.toFile().exists()) {
			File[] children = templatePath.toFile().listFiles();
			if (children != null) {
				templates = new ArrayList<String>(children.length);
				for (File child : children) {
					if (child.isDirectory()) {
						templates.add(child.getName());
					}
				}
			}
		}
		templateViewer.setInput(templates);
	}
}
