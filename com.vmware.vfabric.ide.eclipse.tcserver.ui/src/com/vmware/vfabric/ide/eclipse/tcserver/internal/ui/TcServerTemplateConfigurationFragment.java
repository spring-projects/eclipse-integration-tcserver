/*******************************************************************************
 * Copyright (c) 2012 - 2013 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package com.vmware.vfabric.ide.eclipse.tcserver.internal.ui;

import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wst.server.ui.wizard.IWizardHandle;
import org.eclipse.wst.server.ui.wizard.WizardFragment;

import com.vmware.vfabric.ide.eclipse.tcserver.internal.core.TemplateProperty;
import com.vmware.vfabric.ide.eclipse.tcserver.internal.ui.TcServer21InstanceCreationFragment.InstanceConfiguration;

/**
 * @author Tomasz Zarna
 * 
 */
public class TcServerTemplateConfigurationFragment extends WizardFragment {

	public static final String SPECIFY_TEMPLATE_PROPERTIES_MESSAGE = "Specify template properties.";

	public static final String ENTER_VALUE_MESSAGE = "Enter a value for all required properties.";

	final private String templateName;

	private IWizardHandle wizardHandle;

	private final Set<TemplateProperty> properties;

	public TcServerTemplateConfigurationFragment(String templateName, Set<TemplateProperty> properties) {
		Assert.isNotNull(templateName);
		Assert.isNotNull(properties);
		Assert.isLegal(!properties.isEmpty());
		this.templateName = templateName;
		this.properties = properties;
	}

	@Override
	public void exit() {
		updateModelProperties();
	}

	private void updateModelProperties() {
		InstanceConfiguration model = (InstanceConfiguration) getTaskModel().getObject(
				TcServer21InstanceCreationFragment.INSTANCE_CONFIGURATION);
		if (model == null) {
			return;
		}
		for (TemplateProperty property : properties) {
			// TemplateProperty#equals() is based on 'key' and 'template',
			// so remove an old entry in case 'value' has changed
			if (model.templateProperties.contains(property)) {
				model.templateProperties.remove(property);
			}
			model.templateProperties.add(property);
		}
	}

	@Override
	public boolean hasComposite() {
		return true;
	}

	@Override
	public Composite createComposite(Composite parent, IWizardHandle handle) {
		this.wizardHandle = handle;

		handle.setTitle("Template Configuration");
		handle.setDescription(SPECIFY_TEMPLATE_PROPERTIES_MESSAGE);
		handle.setImageDescriptor(TcServerImages.WIZB_SERVER);

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));

		Label templateNameLabel = new Label(composite, SWT.NONE);
		GridDataFactory.fillDefaults().span(2, 1).applyTo(templateNameLabel);
		templateNameLabel.setText("Enter properties for template " + templateName + ":");

		for (TemplateProperty prop : properties) {
			Label message = new Label(composite, SWT.WRAP);
			GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.BEGINNING).hint(200, SWT.DEFAULT)
					.applyTo(message);
			message.setText(prop.getMessage());

			final Text value = new Text(composite, SWT.BORDER);
			GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.TOP).applyTo(value);
			value.setText(prop.getDefault());
			value.setData(prop);
			value.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					((TemplateProperty) value.getData()).setValue(value.getText());
				}
			});
		}

		Dialog.applyDialogFont(composite);
		return composite;
	}
}
