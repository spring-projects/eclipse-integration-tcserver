/*******************************************************************************
 * Copyright (c) 2012 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package com.vmware.vfabric.ide.eclipse.tcserver.internal.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jst.server.tomcat.core.internal.ITomcatServer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.TaskModel;
import org.eclipse.wst.server.core.internal.ServerWorkingCopy;
import org.eclipse.wst.server.ui.wizard.IWizardHandle;
import org.eclipse.wst.server.ui.wizard.WizardFragment;
import org.springsource.ide.eclipse.commons.core.FileUtil;
import org.springsource.ide.eclipse.commons.core.StatusHandler;

import com.vmware.vfabric.ide.eclipse.tcserver.internal.core.TcServer;
import com.vmware.vfabric.ide.eclipse.tcserver.internal.core.TcServerUtil;
import com.vmware.vfabric.ide.eclipse.tcserver.internal.core.TemplatePropertiesReader;
import com.vmware.vfabric.ide.eclipse.tcserver.internal.core.TemplateProperty;
import com.vmware.vfabric.ide.eclipse.tcserver.internal.ui.TcServer21InstanceCreationFragment.InstanceConfiguration.Layout;

/**
 * @author Steffen Pingel
 * @author Christian Dupuis
 * @author Tomasz Zarna
 */
public class TcServer21InstanceCreationFragment extends WizardFragment {

	public static final String INSTANCE_CONFIGURATION = "instanceConfiguration";

	public static final String SPECIFY_INSTANCE_PARAMETERS_MESSAGE = "Specify instance parameters.";

	public static final String SELECT_TEMPLATE_MESSAGE = "Select one or more templates.";

	public static final String LOCATION_DOES_NOT_EXIST_MESSAGE = "The specified location does not exist.";

	private static Pattern VALID_CHARS = Pattern.compile("[\\p{Alnum}-][\\p{Alnum}-_]*");

	public static class InstanceConfiguration {

		public enum Layout {
			SEPARATE, COMBINED
		};

		private List<File> templates;

		private Layout layout;

		private String name;

		Set<TemplateProperty> templateProperties = new HashSet<TemplateProperty>();

		private String instanceDir;

		public List<File> getTemplates() {
			return Collections.unmodifiableList(templates);
		}

		public Layout getLayout() {
			return layout;
		}

		public String getName() {
			return name;
		}

		public Set<TemplateProperty> getTemplateProperties() {
			return Collections.unmodifiableSet(templateProperties);
		}

		public String getInstanceDir() {
			return instanceDir;
		}
	}

	private class ChildLabelProvider extends StyledCellLabelProvider {

		@Override
		public void update(ViewerCell cell) {

			Object element = cell.getElement();
			if (element instanceof File) {
				String text = ((File) element).getName();
				cell.setText(text);
			}
			super.update(cell);
		}

	}

	private CheckboxTableViewer templateViewer;

	protected List<File> templates = Collections.emptyList();

	private Button separateLayoutButton;

	private Button combinedLayoutButton;

	private IRuntime runtime;

	private Text nameText;

	private IWizardHandle wizardHandle;

	private IServerWorkingCopy wc;

	private Text readmeText;

	private Label readmeLabel;

	private WizardFragment instancePropertiesPage;

	private Button defaultLocationCheckbox;

	private Label locationLabel;

	private Combo locationPathField;

	private Button locationBrowseButton;

	protected TcServer21InstanceCreationFragment() {
		setComplete(false);
	}

	@Override
	public boolean hasComposite() {
		return true;
	}

	@Override
	public Composite createComposite(Composite parent, IWizardHandle handle) {
		this.wizardHandle = handle;

		handle.setTitle("Create tc Server Instance");
		handle.setDescription(SPECIFY_INSTANCE_PARAMETERS_MESSAGE);
		handle.setImageDescriptor(TcServerImages.WIZB_SERVER);

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(3, false));

		Label label = new Label(composite, SWT.NONE);
		label.setText("Name:");

		nameText = new Text(composite, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(nameText);
		nameText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				validate();
				((ServerWorkingCopy) wc).setAttribute(TcServer.KEY_SERVER_NAME, nameText.getText());
			}
		});

		label = new Label(composite, SWT.NONE);
		GridDataFactory.fillDefaults().span(3, 1).applyTo(label);
		label.setText("Templates:");

		templateViewer = CheckboxTableViewer.newCheckList(composite, SWT.BORDER);
		GC gc = new GC(composite);
		FontMetrics fm = gc.getFontMetrics();
		int textLineHeight = fm.getHeight();
		int hintHeight = textLineHeight * 13; // We don't yet know how big
												// templates array is, so use
												// something reasonable
		gc.dispose();

		GridDataFactory.fillDefaults().span(3, 1).grab(true, true).hint(SWT.DEFAULT, hintHeight)
				.applyTo(templateViewer.getControl());

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

		templateViewer.setLabelProvider(new ChildLabelProvider());

		templateViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				validate();

				ISelection selection = event.getSelection();
				if (selection instanceof StructuredSelection) {
					Object file = ((StructuredSelection) selection).getFirstElement();

					if (file instanceof File) {
						File templateDir = (File) file;
						readmeLabel.setText("Information for template " + templateDir.getName() + ": ");
						File readmeFile = new File(templateDir.getPath().concat("/README.txt"));
						try {
							String readmeFileContents = FileUtil.readFile(readmeFile, new NullProgressMonitor());
							readmeText.setText(readmeFileContents);
						}
						catch (CoreException e) {
							readmeText.setText(NLS.bind(
									"Could not read information file for {0}.\n\nCheck permissions on {1}",
									templateDir.getName(), readmeFile));
						}
						updateChildFragments();
					}
					else {
						readmeText.setText("");
					}

				}
			}
		});

		templateViewer.setSorter(new ViewerSorter());

		readmeLabel = new Label(composite, SWT.NONE);
		GridDataFactory.fillDefaults().span(3, 1).grab(true, false).applyTo(readmeLabel);
		readmeLabel.setText("Template information:");

		readmeText = new Text(composite, SWT.V_SCROLL | SWT.BORDER | SWT.READ_ONLY | SWT.WRAP);
		gc = new GC(composite);
		fm = gc.getFontMetrics();
		textLineHeight = fm.getHeight();
		gc.dispose();

		GridDataFactory.fillDefaults().span(3, 1).grab(true, false).hint(SWT.DEFAULT, textLineHeight * 7)
				.applyTo(readmeText);
		readmeText.setText("Click on a template to see information about that template.");

		Group layoutGroup = new Group(composite, SWT.BORDER);
		GridDataFactory.fillDefaults().span(3, 1).grab(true, false).applyTo(layoutGroup);
		layoutGroup.setLayout(new GridLayout(1, false));
		layoutGroup.setText("Layout");

		separateLayoutButton = new Button(layoutGroup, SWT.RADIO);
		separateLayoutButton.setText("Separate (recommended default)");

		combinedLayoutButton = new Button(layoutGroup, SWT.RADIO);
		combinedLayoutButton.setText("Combined");

		defaultLocationCheckbox = new Button(composite, SWT.CHECK | SWT.LEFT);
		defaultLocationCheckbox.setSelection(true);
		defaultLocationCheckbox.setText("Use default instance location");
		GridDataFactory.fillDefaults().span(3, 1).applyTo(defaultLocationCheckbox);
		defaultLocationCheckbox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean checked = defaultLocationCheckbox.getSelection();
				if (checked) {
					locationPathField.setText(runtime.getLocation().toOSString());
					((ServerWorkingCopy) wc).setAttribute(ITomcatServer.PROPERTY_INSTANCE_DIR, (String) null);
				}
				validate();
				locationLabel.setEnabled(!checked);
				locationPathField.setEnabled(!checked);
				locationBrowseButton.setEnabled(!checked);
			}
		});

		locationLabel = new Label(composite, SWT.NONE);
		locationLabel.setText("Location:");
		GridData data = new GridData();
		locationLabel.setLayoutData(data);
		locationLabel.setEnabled(false);

		locationPathField = new Combo(composite, SWT.DROP_DOWN);
		locationPathField.setEnabled(false);
		data = new GridData(GridData.FILL_HORIZONTAL);
		locationPathField.setLayoutData(data);
		locationPathField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				validate();
				((ServerWorkingCopy) wc).setAttribute(ITomcatServer.PROPERTY_INSTANCE_DIR, locationPathField.getText());
			}
		});

		locationBrowseButton = new Button(composite, SWT.PUSH);
		locationBrowseButton.setText("Browse...");
		data = new GridData();
		locationBrowseButton.setLayoutData(data);
		locationBrowseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				handleLocationBrowseButtonPressed();
				validate();
			}
		});
		locationBrowseButton.setEnabled(false);

		Dialog.applyDialogFont(composite);
		return composite;
	}

	private void handleLocationBrowseButtonPressed() {
		DirectoryDialog dialog = new DirectoryDialog(locationBrowseButton.getShell());
		dialog.setMessage("Select location");
		String dirName = locationPathField.getText();
		if (!dirName.equals("")) {//$NON-NLS-1$
			File path = new File(dirName);
			if (path.exists()) {
				dialog.setFilterPath(dirName);
			}
		}
		String selectedDirectory = dialog.open();
		if (selectedDirectory != null) {
			locationPathField.setText(selectedDirectory);
			((ServerWorkingCopy) wc).setAttribute(ITomcatServer.PROPERTY_INSTANCE_DIR, selectedDirectory);
		}
	}

	protected void validate() {
		if (nameText.getText().trim().length() == 0) {
			wizardHandle.setMessage(TcServerInstanceConfiguratorPage.ENTER_NAME, IMessageProvider.NONE);
		}
		else if (!isDefaultLocationSelected() && !isValidLocation(locationPathField.getText())) {
			wizardHandle.setMessage(LOCATION_DOES_NOT_EXIST_MESSAGE, IMessageProvider.ERROR);
		}
		else if (instanceExists()) {
			wizardHandle.setMessage(TcServerInstanceConfiguratorPage.INSTANCE_EXISTS, IMessageProvider.ERROR);
		}
		else if (hasInvalidCharacters(nameText.getText())) {
			wizardHandle.setMessage(TcServerInstanceConfiguratorPage.ILLEGAL_SERVER_NAME, IMessageProvider.ERROR);
		}
		else if (templateViewer.getCheckedElements().length == 0) {
			wizardHandle.setMessage(SELECT_TEMPLATE_MESSAGE, IMessageProvider.NONE);
		}
		else {
			wizardHandle.setMessage(null, IMessageProvider.NONE);
		}
		setComplete(wizardHandle.getMessage() == null);
		wizardHandle.update();
	}

	private boolean hasInvalidCharacters(String text) {
		return !VALID_CHARS.matcher(text).matches();
	}

	private boolean instanceExists() {
		IPath path = null;
		if (isDefaultLocationSelected()) {
			if (runtime == null) {
				return false;
			}
			path = runtime.getLocation();
		}
		else {
			path = new Path(locationPathField.getText());
		}
		path = path.append(nameText.getText());
		return path.toFile().exists();
	}

	private boolean isDefaultLocationSelected() {
		return defaultLocationCheckbox.getSelection();
	}

	private boolean isValidLocation(String location) {
		return new File(location).exists();
	}

	private InstanceConfiguration initModel() {
		InstanceConfiguration model = new InstanceConfiguration();
		model.name = nameText.getText();
		model.templates = new ArrayList<File>();
		for (Object element : templateViewer.getCheckedElements()) {
			model.templates.add((File) element);
		}
		model.layout = (separateLayoutButton.getSelection()) ? Layout.SEPARATE : Layout.COMBINED;
		if (!defaultLocationCheckbox.getSelection()) {
			model.instanceDir = locationPathField.getText();
		}
		return model;
	}

	@Override
	public void enter() {
		this.wc = (IServerWorkingCopy) getTaskModel().getObject(TaskModel.TASK_SERVER);
		this.runtime = wc.getRuntime();
		initialize();
		validate();
	}

	@Override
	public void exit() {
		InstanceConfiguration model = initModel();
		getTaskModel().putObject(INSTANCE_CONFIGURATION, model);
	}

	private void initialize() {
		if (runtime == null) {
			setComplete(false);
			return;
		}

		if (!separateLayoutButton.isDisposed()) {
			separateLayoutButton.setSelection(true);
		}

		IPath runtimePath = runtime.getLocation();
		IPath templatePath = runtimePath.append("templates");
		if (templatePath.toFile().exists()) {
			File[] children = templatePath.toFile().listFiles();
			if (children != null) {
				templates = new ArrayList<File>(children.length);
				for (File child : children) {
					if (isTemplate(child)) {
						templates.add(child);
					}
				}
			}
		}
		templateViewer.setInput(templates);
		locationPathField.setText(runtimePath.toOSString());
	}

	private boolean isTemplate(File child) {
		return child.isDirectory() && !child.getName().startsWith("base-tomcat-")
				&& !child.getName().equals("apr-ssl-tomcat-6");
	}

	@Override
	public void performFinish(IProgressMonitor monitor) throws CoreException {
		// reset completion status in case the wizard is re-used
		setComplete(false);

		InstanceConfiguration model = (InstanceConfiguration) getTaskModel().getObject(INSTANCE_CONFIGURATION);
		if (model == null) {
			return;
		}

		IPath runtimeLocation = runtime.getLocation();

		List<String> arguments = new ArrayList<String>();
		arguments.add("create");
		arguments.add(model.getName());
		String tomcatVersion = TcServerUtil.getServerVersion(runtime);
		if (tomcatVersion != null) {
			arguments.add("-v");
			arguments.add(tomcatVersion);
		}
		for (File template : model.getTemplates()) {
			arguments.add("-t");
			arguments.add(template.getName());
		}
		for (TemplateProperty prop : model.getTemplateProperties()) {
			if (!prop.isDefault()) {
				arguments.add("--property");
				arguments.add(prop.getTemplate() + "." + prop.getKey() + "=" + prop.getValue());
			}
		}

		String instanceDir = model.getInstanceDir();
		if (instanceDir != null) {
			arguments.add("-i");
			arguments.add(instanceDir);
		}

		TcServerUtil.executeInstanceCreation(runtimeLocation, model.getName(),
				arguments.toArray(new String[arguments.size()]));

		// start server inline for new instances
		((ServerWorkingCopy) wc).setAttribute(ITomcatServer.PROPERTY_TEST_ENVIRONMENT, false);

		// load the configuration from the directory based on the selections
		// made on the wizard page
		TcServerUtil.importRuntimeConfiguration(wc, monitor);
	}

	private List<String> getCheckedTemplateNames() {
		List<String> checkedTemplateNames = new ArrayList<String>();
		if (templateViewer != null) {
			for (Object templateFile : templateViewer.getCheckedElements()) {
				checkedTemplateNames.add(((File) templateFile).getName());
			}
		}
		return checkedTemplateNames;
	}

	@Override
	protected void createChildFragments(List<WizardFragment> list) {
		List<String> templateNames = getCheckedTemplateNames();
		if (templateNames.isEmpty()) {
			return;
		}
		TemplatePropertiesReader reader = new TemplatePropertiesReader(wc);
		for (String templateName : templateNames) {
			try {
				Set<TemplateProperty> props = reader.read(templateName, new NullProgressMonitor());
				if (!props.isEmpty()) {
					TcServerTemplateConfigurationFragment page = new TcServerTemplateConfigurationFragment(
							templateName, props);
					list.add(page);
				}
			}
			catch (CoreException e) {
				StatusHandler.log(new Status(IStatus.ERROR, TcServerUiPlugin.PLUGIN_ID,
						"Failed to load a template property page for '" + templateName + "'.", e));
			}
		}
	}
}
