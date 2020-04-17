/*******************************************************************************
 * Copyright (c) 2012, 2020 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package com.vmware.vfabric.ide.eclipse.tcserver.insight.internal.ui.link;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerUtil;
import org.eclipse.wst.server.core.model.IURLProvider;

import com.vmware.vfabric.ide.eclipse.tcserver.core.util.JdtUtils;
import com.vmware.vfabric.ide.eclipse.tcserver.insight.internal.ui.Activator;
import com.vmware.vfabric.ide.eclipse.tcserver.insight.internal.ui.ILocationHandler;

/**
 * {@link ILocationHandler} that opens {@link IJavaElement}s based on the given
 * string in the link parameter.
 * @author Christian Dupuis
 * @author Steffen Pingel
 * @since 2.2.0
 */
public class JavaElementLocationHandler implements ILocationHandler {

	private static final Pattern PATTERN = Pattern.compile("(.*),(.*)/(.*)/(.*)");

	/**
	 * {@inheritDoc}
	 */
	public void handleLocation(String serverUrl, String parameter) {

		Matcher matcher = PATTERN.matcher(parameter);
		if (matcher.matches()) {
			final String contextRoot = serverUrl + matcher.group(1) + "/";
			final String className = matcher.group(2);
			// final String methodName = matcher.group(3);
			final String lineNumber = matcher.group(4);

			UIJob job = new UIJob("Go to Source") { //$NON-NLS-1$
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					try {

						IProject[] projects = findProjects(contextRoot);

						String cleanClassName = cleanClassName(className);

						IType type = null;
						for (IProject project : projects) {
							type = JdtUtils.getJavaType(project, cleanClassName);
							if (type != null) {
								break;
							}
						}
						if (type == null) {
							type = findTypeInWorkspace(cleanClassName);
						}

						if (type == null) {
							MessageDialog.openInformation(Display.getDefault().getActiveShell(), "Type not found",
									"Could not find type '" + className + "' in workspace");
						}
						else {
							IEditorPart editorPart = JdtUtils.openInEditor(type);
							int ln = Integer.valueOf(lineNumber);
							if (editorPart instanceof ITextEditor && ln >= 0) {
								ITextEditor textEditor = (ITextEditor) editorPart;
								IDocumentProvider provider = textEditor.getDocumentProvider();
								provider.connect(editorPart.getEditorInput());
								IDocument document = provider.getDocument(editorPart.getEditorInput());
								try {
									IRegion line = document.getLineInformation(ln - 1);
									textEditor.selectAndReveal(line.getOffset(), line.getLength());
								}
								catch (BadLocationException e) {
								}
								provider.disconnect(editorPart.getEditorInput());
							}
						}
					}
					catch (CoreException e) {
						return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Error finding class", e);
					}
					return Status.OK_STATUS;
				}

			};
			job.setSystem(true);
			job.schedule();
		}
	}

	private static String cleanClassName(final String className) {
		String cleanClassName = className;
		int ix = className.indexOf('$');
		if (ix > 0) {
			cleanClassName = className.substring(0, ix);
		}
		return cleanClassName;
	}

	private static IProject[] findProjects(final String contextRoot) {
		Set<IProject> projects = new HashSet<IProject>();
		IModule[] modules = ServerUtil.getModules("jst.web");
		for (IModule module : modules) {
			IServer[] servers = ServerUtil.getServersByModule(module, new NullProgressMonitor());
			for (IServer server : servers) {
				if (server.getServerState() == IServer.STATE_STARTED) {
					IURLProvider urlProvider = (IURLProvider) server.loadAdapter(IURLProvider.class,
							new NullProgressMonitor());
					if (urlProvider != null) {
						URL url = urlProvider.getModuleRootURL(module);
						if (url.toString().equals(contextRoot)) {
							projects.add(module.getProject());
						}
					}
				}
			}
		}
		return projects.toArray(new IProject[projects.size()]);
	}

	public static IType findTypeInWorkspace(String typeName) throws CoreException {
		IType[] types = findTypes(typeName, null);
		if (types.length > 0) {
			return types[0];
		}
		return null;
	}

	private static IType[] findTypes(String typeName, IProgressMonitor monitor) throws CoreException {

		final List<IType> results = new ArrayList<IType>();

		SearchRequestor collector = new SearchRequestor() {
			@Override
			public void acceptSearchMatch(SearchMatch match) throws CoreException {
				Object element = match.getElement();
				if (element instanceof IType) {
					results.add((IType) element);
				}
			}
		};

		SearchEngine engine = new SearchEngine();
		SearchPattern pattern = SearchPattern.createPattern(typeName, IJavaSearchConstants.TYPE,
				IJavaSearchConstants.DECLARATIONS, SearchPattern.R_EXACT_MATCH);
		engine.search(pattern, new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() },
				SearchEngine.createWorkspaceScope(), collector, monitor);

		return results.toArray(new IType[results.size()]);
	}
}
