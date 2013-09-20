/*******************************************************************************
 * Copyright (c) 2012 - 2013 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package com.vmware.vfabric.ide.eclipse.tcserver.livegraph;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerListener;
import org.eclipse.wst.server.core.ServerEvent;
import org.eclipse.wst.server.ui.editor.ServerEditorSection;
import org.springframework.ide.eclipse.beans.ui.livegraph.model.LiveBeansModel;
import org.springframework.ide.eclipse.beans.ui.livegraph.model.LiveBeansModelGenerator;
import org.springframework.ide.eclipse.beans.ui.livegraph.views.LiveBeansGraphView;
import org.springsource.ide.eclipse.commons.core.StatusHandler;

import com.vmware.vfabric.ide.eclipse.tcserver.internal.core.JmxCredentials;
import com.vmware.vfabric.ide.eclipse.tcserver.internal.core.JmxUtils;
import com.vmware.vfabric.ide.eclipse.tcserver.internal.core.TcServer;
import com.vmware.vfabric.ide.eclipse.tcserver.internal.core.TcServerBehaviour;

/**
 * @author Leo Dos Santos
 */
public class LiveBeansGraphEditorSection extends ServerEditorSection {

	private TcServer serverWorkingCopy;

	private TcServerBehaviour behaviour;

	private PropertyChangeListener propertyListener;

	private IServerListener stateListener;

	private Button enableMbeanButton;

	private Table appsTable;

	private TableViewer appsViewer;

	private ListApplicationsCommand listCommand;

	private void addPropertyChangeListener() {
		propertyListener = new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				if (TcServer.PROPERTY_ADD_EXTRA_VMARGS.equals(evt.getPropertyName())) {
					updateMbeanButton();
				}
			}
		};
		server.addPropertyChangeListener(propertyListener);
	}

	private void addServerStateListener() {
		stateListener = new IServerListener() {
			public void serverChanged(ServerEvent event) {
				if ((event.getKind() & ServerEvent.STATE_CHANGE) != 0) {
					if (event.getState() == IServer.STATE_STARTED) {
						setTableState(true);
						updateTableInput();
					}
					else if (event.getState() == IServer.STATE_STOPPED) {
						setTableState(false);
						updateTableInput();
					}
				}
				else if ((event.getKind() & ServerEvent.SERVER_CHANGE) != 0) {
					updateTableInput();
				}
			}
		};
		server.getOriginal().addServerListener(stateListener);
	}

	private void connectToApplication(String appName) {
		try {
			String username = null;
			String password = null;
			String serviceUrl = JmxUtils.getJmxUrl(behaviour);
			JmxCredentials credentials = JmxUtils.getJmxCredentials(behaviour);
			if (credentials != null) {
				username = credentials.getUsername();
				password = credentials.getPassword();
			}

			LiveBeansModel model = LiveBeansModelGenerator.connectToModel(serviceUrl, username, password, appName);
			IViewPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
					.showView(LiveBeansGraphView.VIEW_ID);
			if (part instanceof LiveBeansGraphView) {
				((LiveBeansGraphView) part).setInput(model);
			}
		}
		catch (PartInitException e) {
			Status status = new Status(IStatus.INFO, TcServerLiveGraphPlugin.PLUGIN_ID,
					"An error occurred while opening the Live Beans Graph View.", e);
			openErrorDialogWithStatus(status);
		}
		catch (IOException e) {
			Status status = new Status(IStatus.INFO, TcServerLiveGraphPlugin.PLUGIN_ID, e.getMessage(), e);
			openErrorDialogWithStatus(status);
		}
		catch (CoreException e) {
			Status status = new Status(IStatus.INFO, TcServerLiveGraphPlugin.PLUGIN_ID, e.getMessage(), e);
			openErrorDialogWithStatus(status);
		}
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
		GridDataFactory.fillDefaults().grab(true, false).applyTo(enableMbeanButton);

		toolkit.createLabel(composite, "Double-click to view application (requires Spring 3.2):");

		appsTable = toolkit.createTable(composite, SWT.SINGLE | SWT.V_SCROLL | SWT.FULL_SELECTION);
		GridData data = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
		data.heightHint = 90;
		appsTable.setLayoutData(data);
		appsViewer = new TableViewer(appsTable);
		appsViewer.setContentProvider(new LiveBeansTableContentProvider());
		appsViewer.setLabelProvider(new LiveBeansTableLabelProvider(server.getOriginal()));
		appsViewer.setSorter(new ViewerSorter());
		appsViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				if (event.getSelection() instanceof IStructuredSelection) {
					IStructuredSelection selection = (IStructuredSelection) event.getSelection();
					if (selection.getFirstElement() instanceof DeployedApplication) {
						DeployedApplication application = (DeployedApplication) selection.getFirstElement();
						if (application.isLiveBeansEnabled()) {
							connectToApplication(application.getName());
						}
					}
				}
			}
		});

		initializeUiState();
	}

	@Override
	public void dispose() {
		if (server != null) {
			server.removePropertyChangeListener(propertyListener);
			server.getOriginal().removeServerListener(stateListener);
		}
		super.dispose();
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) {
		super.init(site, input);
		serverWorkingCopy = (TcServer) server.loadAdapter(TcServer.class, null);
		behaviour = (TcServerBehaviour) serverWorkingCopy.getServer().loadAdapter(TcServerBehaviour.class, null);
		listCommand = new ListApplicationsCommand(behaviour, false);
		addPropertyChangeListener();
		addServerStateListener();
		initializeUiState();
	}

	private void initializeUiState() {
		setTableState(server.getOriginal().getServerState() == IServer.STATE_STARTED);
		updateTableInput();
		updateMbeanButton();
	}

	private void openErrorDialogWithStatus(IStatus status) {
		ErrorDialog.openError(getShell(), "Connection Failed", "Could not connect to the server or application.\n\n"
				+ "Please ensure that the server is configured for JMX access. "
				+ "This feature is only supported for applications on Spring Framework 3.2 or greater.\n\n"
				+ "See the Error Log for more details.", status);
		StatusHandler.log(status);
	}

	private void setTableState(final boolean enabled) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				if (appsTable != null && !appsTable.isDisposed()) {
					appsTable.setEnabled(enabled);
				}
			}
		});
	}

	private void updateMbeanButton() {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				if (enableMbeanButton != null && !enableMbeanButton.isDisposed()) {
					enableMbeanButton.setSelection(serverWorkingCopy.getAddExtraVmArgs().containsAll(
							Arrays.asList(TcServerLiveGraphPlugin.FLAG_LIVE_BEANS)));
				}
			}
		});
	}

	private void updateTableInput() {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				Set input = Collections.EMPTY_SET;
				try {
					input = listCommand.execute();
				}
				catch (TimeoutException e1) {
					// ignore, server may not be running
				}
				catch (CoreException e1) {
					// ignore, server may not be running
				}
				if (appsViewer != null && appsViewer.getControl() != null && !appsViewer.getControl().isDisposed()) {
					appsViewer.setInput(input);
					appsViewer.refresh();
				}
			}
		});
	}

}
