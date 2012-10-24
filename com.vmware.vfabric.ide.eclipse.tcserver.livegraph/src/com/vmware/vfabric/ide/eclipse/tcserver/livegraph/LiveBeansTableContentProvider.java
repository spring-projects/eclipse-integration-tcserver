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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * @author Leo Dos Santos
 */
public class LiveBeansTableContentProvider implements ITreeContentProvider {

	private Set applications;

	public void dispose() {

	}

	public Object[] getChildren(Object parentElement) {
		return null;
	}

	public Object[] getElements(Object inputElement) {
		List<String> list = new ArrayList<String>();
		Iterator iter = applications.iterator();
		while (iter.hasNext()) {
			Object obj = iter.next();
			if (obj instanceof Map) {
				Map attributes = (Map) obj;
				if (attributes.containsKey("baseName")) {
					Object name = attributes.get("baseName");
					// TODO: filter out modules that are not running Spring
					// Framework 3.2 or newer
					if (name instanceof String && !"ROOT".equals(name)) {
						list.add((String) name);
					}
				}
			}
		}
		return list.toArray();
	}

	public Object getParent(Object element) {
		return null;
	}

	public boolean hasChildren(Object element) {
		return false;
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (newInput instanceof Set) {
			applications = (Set) newInput;
		}
	}

}
