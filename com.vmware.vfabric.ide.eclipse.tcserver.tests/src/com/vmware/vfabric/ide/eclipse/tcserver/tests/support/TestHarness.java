/*******************************************************************************
 * Copyright (c) 2012, 2018 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package com.vmware.vfabric.ide.eclipse.tcserver.tests.support;

import java.io.File;
import java.net.URI;

import org.junit.Assert;
import org.springsource.ide.eclipse.commons.core.ZipFileUtil;
import org.springsource.ide.eclipse.commons.core.ZipFileUtil.PermissionSetter;
import org.springsource.ide.eclipse.commons.tests.util.DownloadManager;

/**
 * @author Steffen Pingel
 */
public class TestHarness {

	private static final DownloadManager downloadMan = new DownloadManager();

	private final TestConfiguration configuration;

	public TestHarness(TestConfiguration configuration) {
		this.configuration = configuration;
	}

	public TestConfiguration getConfiguration() {
		return configuration;
	}

	public void provisionRuntime(File targetPath) throws Exception {
		File runtimeArchive = downloadRuntime();
		ZipFileUtil.unzip(runtimeArchive.toURI().toURL(), targetPath, configuration.getDescription(),
				PermissionSetter.executableExtensions(".sh", ""), null);
	}

	public File downloadRuntime() throws Exception {
		Assert.assertNotNull("Configuration " + configuration + " does not specify a downloadUrl",
				configuration.getDownloadUrl());
		return downloadFile(new URI(configuration.getDownloadUrl()));
	}

	public File downloadFile(URI uri) throws Exception {
		return downloadMan.downloadFile(uri);
	}

}
