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
package com.vmware.vfabric.ide.eclipse.tcserver.livegraph;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.wst.server.ui.editor.ServerEditorSection;

import com.vmware.vfabric.ide.eclipse.tcserver.internal.core.TcServer;

/**
 * @author Leo Dos Santos
 */
public class LiveBeansGraphEditorSection extends ServerEditorSection {

	private TcServer serverWorkingCopy;

	private PropertyChangeListener listener;

	private Button enableMbeanButton;

	private void addChangeListener() {
		listener = new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				if (TcServer.PROPERTY_ADD_EXTRA_VMARGS.equals(evt.getPropertyName())) {
					update();
				}
			}
		};
		server.addPropertyChangeListener(listener);
	}

	@Override
	public void createSection(Composite parent) {
		super.createSection(parent);
		FormToolkit toolkit = getFormToolkit(parent.getDisplay());

		Section section = toolkit.createSection(parent, ExpandableComposite.TWISTIE | ExpandableComposite.EXPANDED
				| ExpandableComposite.TITLE_BAR | Section.DESCRIPTION | ExpandableComposite.FOCUS_TITLE);
		section.setText("Live Beans Graph");
		section.setDescription("Enable and launch the Live Beans Graph for deployed applications.");
		section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL));

		Composite composite = toolkit.createComposite(section);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 5;
		layout.marginWidth = 1;
		layout.verticalSpacing = 5;
		layout.horizontalSpacing = 10;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL));
		toolkit.paintBordersFor(composite);
		section.setClient(composite);

		enableMbeanButton = toolkit.createButton(composite,
				"Enable Live Beans indexing (takes effect upon server restart)", SWT.CHECK);
		enableMbeanButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				execute(new ModifyLiveGraphVmArgsCommand(serverWorkingCopy, enableMbeanButton.getSelection()));
			}
		});
		GridDataFactory.fillDefaults().applyTo(enableMbeanButton);

		initialize();
	}

	@Override
	public void dispose() {
		if (server != null) {
			server.removePropertyChangeListener(listener);
		}
		super.dispose();
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) {
		super.init(site, input);
		serverWorkingCopy = (TcServer) server.loadAdapter(TcServer.class, null);
		addChangeListener();
		initialize();
	}

	private void initialize() {
		update();
	}

	private void update() {
		if (enableMbeanButton != null && !enableMbeanButton.isDisposed()) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					enableMbeanButton.setSelection(serverWorkingCopy.getAddExtraVmArgs().containsAll(
							Arrays.asList(TcServerLiveGraphPlugin.FLAG_LIVE_BEANS)));
				}
			});
		}
	}

}
