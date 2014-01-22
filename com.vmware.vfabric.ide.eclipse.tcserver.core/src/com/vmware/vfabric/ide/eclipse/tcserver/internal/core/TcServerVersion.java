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
package com.vmware.vfabric.ide.eclipse.tcserver.internal.core;

/**
 * @author Steffen Pingel
 * @author Christian Dupuis
 */
public class TcServerVersion {

	private final int major;

	private final int minor;

	private final int micro;

	private final String qualifier;

	public TcServerVersion(String version) {
		String[] segments = version == null ? new String[0] : version.split("\\.", 4); //$NON-NLS-1$
		major = segments.length > 0 ? parse(segments[0]) : 0;
		minor = segments.length > 1 ? parse(segments[1]) : 0;
		micro = segments.length > 2 ? parse(segments[2]) : 0;
		qualifier = segments.length > 3 ? segments[3] : "";
	}

	public int compareTo(TcServerVersion v) {
		if (major < v.major) {
			return -1;
		}
		else if (major > v.major) {
			return 1;
		}

		if (minor < v.minor) {
			return -1;
		}
		else if (minor > v.minor) {
			return 1;
		}

		if (micro < v.micro) {
			return -1;
		}
		else if (micro > v.micro) {
			return 1;
		}

		return qualifier.compareTo(v.qualifier);
	}

	private int parse(String segment) {
		try {
			return segment.length() == 0 ? 0 : Integer.parseInt(segment);
		}
		catch (NumberFormatException e) {
			return 0;
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(Integer.toString(major));
		sb.append(".").append(Integer.toString(minor)); //$NON-NLS-1$
		if (micro > 0) {
			sb.append(".").append(Integer.toString(micro)); //$NON-NLS-1$
		}
		if (qualifier.length() > 0) {
			sb.append(".").append(qualifier); //$NON-NLS-1$
		}
		return sb.toString();
	}

}
