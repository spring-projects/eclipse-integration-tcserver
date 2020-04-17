// COPIED from spring-ide org.springframework.ide.eclipse.core.java.JdtUtils
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
package com.vmware.vfabric.ide.eclipse.tcserver.core.util;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.vmware.vfabric.ide.eclipse.tcserver.internal.core.TcServerCorePlugin;


/**
 * Utility class that provides several helper methods for working with Eclipse's
 * JDT.
 * @author Christian Dupuis
 * @author Martin Lippert
 * @author Leo Dos Santos
 * @since 2.0
 */
public class JdtUtils {

	/**
	 * Returns the corresponding Java project or <code>null</code> a for given
	 * project.
	 * @param project the project the Java project is requested for
	 * @return the requested Java project or <code>null</code> if the Java
	 * project is not defined or the project is not accessible
	 */
	public static IJavaProject getJavaProject(IProject project) {
		if (project.isAccessible()) {
			try {
				if (project.hasNature(JavaCore.NATURE_ID)) {
					return (IJavaProject) project.getNature(JavaCore.NATURE_ID);
				}
			}
			catch (CoreException e) {
				TcServerCorePlugin.log(new Status(IStatus.ERROR, TcServerCorePlugin.PLUGIN_ID,
						"Error getting Java project for project '" + project.getName() + "'", e));
			}
		}
		return null;
	}

//	public static IJavaProject getJavaProject(IResource config) {
//		IJavaProject project = JavaCore.create(config.getProject());
//		return project;
//	}

	/**
	 * Returns the corresponding Java type for given full-qualified class name.
	 * @param project the JDT project the class belongs to
	 * @param className the full qualified class name of the requested Java type
	 * @return the requested Java type or null if the class is not defined or
	 * the project is not accessible
	 */
	public static IType getJavaType(IProject project, String className) {
		IJavaProject javaProject = JdtUtils.getJavaProject(project);
		if (className != null) {
			// For inner classes replace '$' by '.'
			int pos = className.lastIndexOf('$');
			if (pos > 0) {
				className = className.replace('$', '.');
			}
			try {
				IType type = null;
				// First look for the type in the Java project
				if (javaProject != null) {
					// TODO CD not sure why we need
					type = javaProject.findType(className, new NullProgressMonitor());
					// type = javaProject.findType(className);
					if (type != null) {
						return type;
					}
				}

				// Then look for the type in the referenced Java projects
				for (IProject refProject : project.getReferencedProjects()) {
					IJavaProject refJavaProject = JdtUtils.getJavaProject(refProject);
					if (refJavaProject != null) {
						type = refJavaProject.findType(className);
						if (type != null) {
							return type;
						}
					}
				}

				// fall back and try to locate the class using AJDT
				// TODO: uncomment this call
				// return getAjdtType(project, className);
			}
			catch (CoreException e) {
				TcServerCorePlugin.log(new Status(IStatus.ERROR, TcServerCorePlugin.PLUGIN_ID, "Error getting Java type '"
						+ className + "'", e));
			}
		}

		return null;
	}

//	public static int getLineNumber(IJavaElement element) {
//		if (element != null && element instanceof IMethod) {
//			try {
//				IMethod method = (IMethod) element;
//				int lines = 0;
//				if (method.getDeclaringType() != null && method.getDeclaringType().getCompilationUnit() != null) {
//					String targetsource = method.getDeclaringType().getCompilationUnit().getSource();
//					if (targetsource != null) {
//						String sourceuptomethod = targetsource.substring(0, method.getNameRange().getOffset());
//
//						char[] chars = new char[sourceuptomethod.length()];
//						sourceuptomethod.getChars(0, sourceuptomethod.length(), chars, 0);
//						for (char element0 : chars) {
//							if (element0 == '\n') {
//								lines++;
//							}
//						}
//						return new Integer(lines + 1);
//					}
//				}
//			}
//			catch (JavaModelException e) {
//			}
//		}
//		else if (element != null && element instanceof IType && ((IType) element).getCompilationUnit() != null) {
//			try {
//				IType type = (IType) element;
//				int lines = 0;
//				String targetsource = type.getCompilationUnit().getSource();
//				if (targetsource != null) {
//					String sourceuptomethod = targetsource.substring(0, type.getNameRange().getOffset());
//
//					char[] chars = new char[sourceuptomethod.length()];
//					sourceuptomethod.getChars(0, sourceuptomethod.length(), chars, 0);
//					for (char element0 : chars) {
//						if (element0 == '\n') {
//							lines++;
//						}
//					}
//					return new Integer(lines + 1);
//				}
//			}
//			catch (JavaModelException e) {
//			}
//		}
//		else if (element != null && element instanceof IField) {
//			try {
//				IField type = (IField) element;
//				int lines = 0;
//				ICompilationUnit cu = type.getCompilationUnit();
//				if (cu != null) {
//					String targetsource = cu.getSource();
//					if (targetsource != null) {
//						String sourceuptomethod = targetsource.substring(0, type.getNameRange().getOffset());
//
//						char[] chars = new char[sourceuptomethod.length()];
//						sourceuptomethod.getChars(0, sourceuptomethod.length(), chars, 0);
//						for (char element0 : chars) {
//							if (element0 == '\n') {
//								lines++;
//							}
//						}
//						return new Integer(lines + 1);
//					}
//				}
//			}
//			catch (JavaModelException e) {
//			}
//		}
//		return new Integer(-1);
//	}

	public static IEditorPart openInEditor(IJavaElement element) {
		try {
			IEditorPart editor = JavaUI.openInEditor(element);
			if (editor != null) {
				JavaUI.revealInEditor(editor, element);
			}
			return editor;
		}
		catch (PartInitException e) {
			openError("Open Editor Problems", e.getMessage(), e);
		}
		catch (JavaModelException e) {
			openError("Open Editor Problems", e.getMessage(), e);
		}
		return null;
	}

	/**
	 * Open an error style dialog for a given <code>CoreException</code> by
	 * including any extra information from a nested <code>CoreException</code>.
	 */
	private static void openError(String title, String message, CoreException exception) {
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

		// Check for a nested CoreException
		CoreException nestedException = null;
		IStatus status = exception.getStatus();
		if (status != null && status.getException() instanceof CoreException) {
			nestedException = (CoreException) status.getException();
		}
		if (nestedException != null) {
			// Open an error dialog and include the extra
			// status information from the nested CoreException
			ErrorDialog.openError(shell, title, message, nestedException.getStatus());
		}
		else {
			// Open a regular error dialog since there is no
			// extra information to display
			MessageDialog.openError(shell, title, message);
		}
	}

}
