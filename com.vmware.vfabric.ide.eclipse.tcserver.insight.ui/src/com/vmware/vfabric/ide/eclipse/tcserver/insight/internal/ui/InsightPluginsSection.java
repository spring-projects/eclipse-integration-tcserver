/*******************************************************************************
 * Copyright (c) 2012 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package com.vmware.vfabric.ide.eclipse.tcserver.insight.internal.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.mylyn.commons.workbench.SubstringPatternFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.wst.server.ui.editor.ServerEditorPart;
import org.eclipse.wst.server.ui.editor.ServerEditorSection;

import com.vmware.vfabric.ide.eclipse.tcserver.internal.core.TcServer;

/**
 * A section for enabling Insight plug-ins.
 * @author Steffen Pingel
 * @author Christian Dupuis
 */
public class InsightPluginsSection extends ServerEditorSection implements IInsightPageParticipant {

	public class EnablePluginsOperation extends AbstractOperation {

		private final List<InsightPlugin> plugins;

		private final boolean state;

		public EnablePluginsOperation(List<InsightPlugin> plugins, boolean state) {
			super("Select Insight plugins");
			this.plugins = plugins;
			this.state = state;
		}

		@Override
		public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			enable(state);
			return Status.OK_STATUS;
		}

		@Override
		public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			return execute(monitor, info);
		}

		@Override
		public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			enable(!state);
			return Status.OK_STATUS;
		}

		private void enable(boolean state) {
			for (InsightPlugin plugin : plugins) {
				plugin.setEnabled(state);
			}
			if (pluginViewer.getControl() != null && !pluginViewer.getControl().isDisposed()) {
				pluginViewer.refresh();
			}
		}

	}

	private class PluginLabelProvider extends LabelProvider implements IStyledLabelProvider {

		final Styler NO_STYLE = new Styler() {
			@Override
			public void applyStyles(TextStyle textStyle) {
			}
		};

		public StyledString getStyledText(Object element) {
			String text = getText(element);
			if (text != null) {
				StyledString styledString = new StyledString(text);
				if (element instanceof InsightPlugin) {
					InsightPlugin plugin = (InsightPlugin) element;
					styledString.append("  " + plugin.getDetails(), StyledString.DECORATIONS_STYLER);
				}
				return styledString;
			}
			return new StyledString();
		}

		@Override
		public String getText(Object element) {
			return ((InsightPlugin) element).getName();
		}

	}

	private TcServer serverInstance;

	private PropertyChangeListener listener;

	private Button enableButton;

	private CheckboxTreeViewer pluginViewer;

	private InsightPluginModel model;

	private InsightEditorPage editor;

	private class CheckboxFilteredTree extends FilteredTree {

		public CheckboxFilteredTree(Composite parent, int treeStyle, PatternFilter filter) {
			super(parent, treeStyle, filter, true);
		}

		@Override
		protected TreeViewer doCreateTreeViewer(Composite parent, int style) {
			return new CheckboxTreeViewer(parent, style);
		}

		public CheckboxTreeViewer getCheckboxTreeViewer() {
			return getViewer();
		}

		@Override
		public CheckboxTreeViewer getViewer() {
			return (CheckboxTreeViewer) super.getViewer();
		}

	}

	public InsightPluginsSection() {
	}

	@Override
	public void createSection(Composite parent) {
		super.createSection(parent);
		FormToolkit toolkit = getFormToolkit(parent.getDisplay());

		Section section = toolkit.createSection(parent, ExpandableComposite.TWISTIE | ExpandableComposite.TITLE_BAR
				| Section.DESCRIPTION | ExpandableComposite.FOCUS_TITLE | ExpandableComposite.EXPANDED);
		section.setText("Plugins");
		section.setDescription("Enable plug-ins to gather specific metrics.");
		section.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_VERTICAL));

		Composite composite = toolkit.createComposite(section, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = 8;
		layout.marginWidth = 8;
		composite.setLayout(layout);
		toolkit.paintBordersFor(composite);
		section.setClient(composite);

		CheckboxFilteredTree filteredTree = new CheckboxFilteredTree(composite, SWT.FULL_SELECTION | SWT.BORDER,
				new SubstringPatternFilter());
		GridDataFactory.fillDefaults().grab(true, true).hint(SWT.DEFAULT, 200).applyTo(filteredTree);
		pluginViewer = filteredTree.getCheckboxTreeViewer();
		// required to make filtering work
		pluginViewer.setLabelProvider(new PluginLabelProvider());
		pluginViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				InsightPlugin plugin = (InsightPlugin) event.getElement();
				execute(new EnablePluginsOperation(Collections.singletonList(plugin), event.getChecked()));
			}
		});
		pluginViewer.setCheckStateProvider(new ICheckStateProvider() {
			public boolean isChecked(Object element) {
				return ((InsightPlugin) element).isEnabled();
			}

			public boolean isGrayed(Object element) {
				return false;
			}
		});
		TreeViewerColumn nameColumn = new TreeViewerColumn(pluginViewer, SWT.NONE);
		nameColumn.getColumn().setText("Name");
		nameColumn.getColumn().setWidth(400);
		nameColumn.setLabelProvider(new DelegatingStyledCellLabelProvider(new PluginLabelProvider()));
		pluginViewer.setContentProvider(new ITreeContentProvider() {

			private InsightPluginModel input;

			private final Object[] EMPTY_ARRAY = new Object[0];

			public void dispose() {
				// ignore
			}

			public Object[] getChildren(Object parentElement) {
				return EMPTY_ARRAY;
			}

			public Object[] getElements(Object parent) {
				if (input != null) {
					return input.getPlugins().toArray();
				}
				return EMPTY_ARRAY;
			}

			public Object getParent(Object element) {
				return null;
			}

			public boolean hasChildren(Object element) {
				return false;
			}

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				input = (InsightPluginModel) newInput;
			}

		});
		pluginViewer.setSorter(new ViewerSorter() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				InsightPlugin p1 = (InsightPlugin) e1;
				InsightPlugin p2 = (InsightPlugin) e2;
				return p1.getName().compareTo(p2.getName());
			}
		});

		Composite buttonComposite = new Composite(composite, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.TOP).applyTo(buttonComposite);
		GridLayoutFactory.fillDefaults().numColumns(1).margins(0, 0).extendedMargins(0, 0, 0, 0)
				.applyTo(buttonComposite);
		createButtons(buttonComposite);

		initialize();
	}

	@Override
	public void setServerEditorPart(ServerEditorPart editor) {
		super.setServerEditorPart(editor);
		this.editor = (InsightEditorPage) editor;
		this.editor.addPageParticipant(this);
	}

	public void createButtons(Composite composite) {
		FormToolkit toolkit = getFormToolkit(getShell().getDisplay());

		Button selectAllButton = toolkit.createButton(composite, "", SWT.PUSH);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(selectAllButton);
		selectAllButton.setText("&Select All");
		selectAllButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				List<InsightPlugin> plugins = new ArrayList<InsightPlugin>();
				TreeItem[] items = pluginViewer.getTree().getItems();
				for (TreeItem item : items) {
					if (item.getData() instanceof InsightPlugin) {
						if (!((InsightPlugin) item.getData()).isEnabled()) {
							plugins.add((InsightPlugin) item.getData());
						}
					}
				}
				if (plugins.size() > 0) {
					execute(new EnablePluginsOperation(plugins, true));
				}
			}
		});

		Button deselectAllButton = toolkit.createButton(composite, "", SWT.PUSH);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(deselectAllButton);
		deselectAllButton.setText("&Deselect All");
		deselectAllButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				List<InsightPlugin> plugins = new ArrayList<InsightPlugin>();
				TreeItem[] items = pluginViewer.getTree().getItems();
				for (TreeItem item : items) {
					if (item.getData() instanceof InsightPlugin) {
						if (((InsightPlugin) item.getData()).isEnabled()) {
							plugins.add((InsightPlugin) item.getData());
						}
					}
				}
				if (plugins.size() > 0) {
					execute(new EnablePluginsOperation(plugins, false));
				}
			}
		});

	}

	@Override
	public void dispose() {
		if (server != null) {
			server.removePropertyChangeListener(listener);
		}
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) {
		super.init(site, input);
		serverInstance = (TcServer) server.loadAdapter(TcServer.class, null);
		addChangeListener();
		initialize();
	}

	private void update() {
		if (enableButton != null && !enableButton.isDisposed()) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					enableButton.setSelection(!serverInstance.getAddExtraVmArgs().containsAll(
							Arrays.asList(TcServerInsightUtil.DISABLED_INSIGHT)));
				}
			});
		}
		if (pluginViewer != null) {
			model = new InsightPluginModel();
			IPath insightPath = TcServerInsightUtil.getInsightPath(serverInstance.getServer());
			if (insightPath != null) {
				model.load(insightPath.append("collection-plugins"));
			}
			pluginViewer.setInput(model);
		}
	}

	protected void addChangeListener() {
		listener = new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (TcServer.PROPERTY_ADD_EXTRA_VMARGS.equals(event.getPropertyName())) {
					update();
				}
			}
		};
		server.addPropertyChangeListener(listener);
	}

	/**
	 * Initialize the fields in this editor.
	 */
	protected void initialize() {
		update();
	}

	public void doSave(IProgressMonitor monitor) {
		model.commit();
	}

}
