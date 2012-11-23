/*******************************************************************************
 * Copyright (c) 2012 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package com.vmware.vfabric.ide.eclipse.tcserver.internal.core;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.wst.server.core.IServer;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import com.vmware.vfabric.ide.eclipse.tcserver.tests.support.TcServerFixture;

/**
 * @author Tomasz Zarna
 */
public class TcServerTemplatePropertiesReaderTest {

	private static IServer server;

	private static TemplatePropertiesReader reader;

	@BeforeClass
	public static void beforeClass() throws Exception {
		server = TcServerFixture.V_2_8.createServer(null);
		reader = new TemplatePropertiesReader(server);
	}

	@Test
	public void haveTestsForAllTemplates() {
		List<String> actualTemplates = new ArrayList<String>();
		IPath runtimePath = server.getRuntime().getLocation();
		IPath templatePath = runtimePath.append("templates");
		if (templatePath.toFile().exists()) {
			File[] children = templatePath.toFile().listFiles();
			if (children != null) {
				for (File child : children) {
					if (isTemplate(child)) {
						actualTemplates.add(child.getName());
					}
				}
			}
		}
		assertArrayEquals(new String[] { "ajp", "apr", "apr-ssl", "async-logger", "base", "bio", "bio-ssl",
				"cluster-node", "diagnostics", "insight", "jmx-ssl", "nio", "nio-ssl" },
				actualTemplates.toArray(new String[actualTemplates.size()]));
	}

	private boolean isTemplate(File child) {
		return child.isDirectory() && !child.getName().startsWith("base-tomcat-")
				&& !child.getName().equals("apr-ssl-tomcat-6");
	}

	@Test
	public void ajpTemplate() throws Exception {
		List<TemplateProperty> props = reader.read("ajp", new NullProgressMonitor());
		Assume.assumeNotNull(props);

		assertEquals(2, props.size());
		assertPropsEquals(props.get(0), "ajp", "http.port",
				"Please enter the port that the AJP connector should listen for requests on:", "8009");
		assertPropsEquals(props.get(1), "ajp", "https.port",
				"Please enter the port that the AJP connector should redirect SSL requests to:", "8443");
	}

	@Test
	public void aprTemplate() throws Exception {
		List<TemplateProperty> props = reader.read("apr", new NullProgressMonitor());
		Assume.assumeNotNull(props);

		assertEquals(2, props.size());
		assertPropsEquals(props.get(0), "apr", "http.port",
				"Please enter the port that the APR connector should listen for HTTP requests on:", "8080");
		assertPropsEquals(props.get(1), "apr", "https.port",
				"Please enter the port that the APR connector should redirect HTTPS requests to:", "8443");
	}

	@Test
	public void aprSslTemplate() throws Exception {
		List<TemplateProperty> props = reader.read("apr-ssl", new NullProgressMonitor());
		Assume.assumeNotNull(props);

		assertEquals(13, props.size());
		assertPropsEquals(props.get(0), "apr-ssl", "https.port",
				"Please enter the port that the APR connector should listen for HTTPS requests on:", "8443");
		assertPropsEquals(
				props.get(1),
				"apr-ssl",
				"ssl.certificate.dname",
				"Please enter the distinguised name the SSL certificate should use. To create a new name, leave blank:",
				"GENERATE");
		assertPropsEquals(props.get(2), "apr-ssl", "ssl.certificate.dname.CN",
				"Please enter the first and last name the SSL certificate should use:", "Unknown");
		assertPropsEquals(props.get(3), "apr-ssl", "ssl.certificate.dname.OU",
				"Please enter the organizational unit the SSL certificate should use:", "Unknown");
		assertPropsEquals(props.get(4), "apr-ssl", "ssl.certificate.dname.O",
				"Please enter the organization the SSL certificate should use:", "Unknown");
		assertPropsEquals(props.get(5), "apr-ssl", "ssl.certificate.dname.L",
				"Please enter the city or locality the SSL certificate should use:", "Unknown");
		assertPropsEquals(props.get(6), "apr-ssl", "ssl.certificate.dname.ST",
				"Please enter the state or province the SSL certificate should use:", "Unknown");
		assertPropsEquals(props.get(7), "apr-ssl", "ssl.certificate.dname.C",
				"Please enter the two-letter country code the SSL certificate should use:", "Unknown");
		assertPropsEquals(props.get(8), "apr-ssl", "ssl.key.size",
				"Please enter the size in bits that the SSL private key should be:", "2048");
		assertPropsEquals(props.get(9), "apr-ssl", "ssl.certificate.location",
				"Please enter the path that the SSL certificate should be stored to:", "conf/tc-server.cer");
		assertPropsEquals(
				props.get(10),
				"apr-ssl",
				"ssl.certificate.location.input",
				"Please enter the path that the SSL certificate should be read from. To create a new certificate, leave blank:",
				"GENERATE");
		assertPropsEquals(props.get(11), "apr-ssl", "ssl.private.key.location",
				"Please enter the path that the SSL private key should be stored to:", "conf/tc-server.key");
		assertPropsEquals(props.get(12), "apr-ssl", "ssl.private.key.location.input",
				"Please enter the path that the SSL private key should be read from:");
	}

	@Test
	public void asyncLoggerTemplate() throws Exception {
		List<TemplateProperty> props = reader.read("async-logger", new NullProgressMonitor());
		Assume.assumeNotNull(props);

		assertEquals(0, props.size());
	}

	@Test
	public void baseTemplate() throws Exception {
		List<TemplateProperty> props = reader.read("base", new NullProgressMonitor());
		Assume.assumeNotNull(props);

		assertEquals(3, props.size());
		assertPropsEquals(props.get(0), "base", "jmx.port",
				"Please enter the port that the JMX socket listener should listen on:", "6969");
		assertPropsEquals(props.get(1), "base", "shutdown.port",
				"Please enter the port that Tomcat Shutdown should listen on:", "-1");
		assertPropsEquals(props.get(2), "base", "runtime.user",
				"Please enter the user account that should start the instance when using the 'bin/init.d.sh' script:",
				"tcserver");
	}

	@Test
	public void bioTemplate() throws Exception {
		List<TemplateProperty> props = reader.read("bio", new NullProgressMonitor());
		Assume.assumeNotNull(props);

		assertEquals(2, props.size());
		assertPropsEquals(props.get(0), "bio", "http.port",
				"Please enter the port that the BIO connector should listen for HTTP requests on:", "8080");
		assertPropsEquals(props.get(1), "bio", "https.port",
				"Please enter the port that the BIO connector should redirect HTTPS requests to:", "8443");
	}

	@Test
	public void bioSslTemplate() throws Exception {
		List<TemplateProperty> props = reader.read("bio-ssl", new NullProgressMonitor());
		Assume.assumeNotNull(props);

		assertEquals(17, props.size());
		assertPropsEquals(props.get(0), "bio-ssl", "https.port",
				"Please enter the port that the BIO connector should listen for HTTPS requests on:", "8443");
		assertPropsEquals(
				props.get(1),
				"bio-ssl",
				"ssl.certificate.dname",
				"Please enter the distinguised name the SSL certificate should use (i.e. cn=MyCompany,dc=mycompany,dc=com). To be prompted for name components leave blank:",
				"GENERATE");
		assertPropsEquals(props.get(2), "bio-ssl", "ssl.certificate.dname.CN",
				"Please enter the first and last name the SSL certificate should use:", "Unknown");
		assertPropsEquals(props.get(3), "bio-ssl", "ssl.certificate.dname.OU",
				"Please enter the organizational unit the SSL certificate should use:", "Unknown");
		assertPropsEquals(props.get(4), "bio-ssl", "ssl.certificate.dname.O",
				"Please enter the organization the SSL certificate should use:", "Unknown");
		assertPropsEquals(props.get(5), "bio-ssl", "ssl.certificate.dname.L",
				"Please enter the city or locality the SSL certificate should use:", "Unknown");
		assertPropsEquals(props.get(6), "bio-ssl", "ssl.certificate.dname.ST",
				"Please enter the state or province the SSL certificate should use:", "Unknown");
		assertPropsEquals(props.get(7), "bio-ssl", "ssl.certificate.dname.C",
				"Please enter the two-letter country code the SSL certificate should use:", "Unknown");
		assertPropsEquals(props.get(8), "bio-ssl", "ssl.key.size",
				"Please enter the size in bits that the SSL private key should be:", "2048");
		assertPropsEquals(props.get(9), "bio-ssl", "ssl.key.alias",
				"Please enter the alias that the keystore should refer to the SSL private key as:", "tc-server-bio-ssl");
		assertPropsEquals(props.get(10), "bio-ssl", "ssl.key.alias.input",
				"Please enter the alias that the keystore refers to the SSL private key as:");
		assertPropsEquals(props.get(11), "bio-ssl", "ssl.key.password",
				"Please enter the password that keystore should protect the SSL private key with:", "RANDOM");
		assertPropsEquals(props.get(12), "bio-ssl", "ssl.key.password.input",
				"Please enter the password that keystore protects the SSL private key with:");
		assertPropsEquals(props.get(13), "bio-ssl", "ssl.keystore.location",
				"Please enter the path that the SSL keystore should be stored to:", "conf/tc-server-bio-ssl.keystore");
		assertPropsEquals(
				props.get(14),
				"bio-ssl",
				"ssl.keystore.location.input",
				"Please enter the path that the SSL keystore should be read from. To create a new keystore, leave blank:",
				"GENERATE");
		assertPropsEquals(props.get(15), "bio-ssl", "ssl.keystore.password",
				"Please enter the password that the SSL keystore should protect itself with:", "RANDOM");
		assertPropsEquals(props.get(16), "bio-ssl", "ssl.keystore.password.input",
				"Please enter the password that the SSL keystore protects itself with:");
	}

	@Test
	public void clusterNodeTemplate() throws Exception {
		List<TemplateProperty> props = reader.read("cluster-node", new NullProgressMonitor());
		Assume.assumeNotNull(props);

		assertEquals(1, props.size());
		assertPropsEquals(props.get(0), "cluster-node", "node.name",
				"Please enter the cluster node name used to identify this instance:", "tc-runtime-1");
	}

	@Test
	public void diagnosticsTemplate() throws Exception {
		List<TemplateProperty> props = reader.read("diagnostics", new NullProgressMonitor());
		Assume.assumeNotNull(props);

		assertEquals(5, props.size());
		assertPropsEquals(props.get(0), "diagnostics", "jdbc.resource.name",
				"Please enter the JNDI name that the diagnostic DataSource should be available at:");
		assertPropsEquals(props.get(1), "diagnostics", "jdbc.username",
				"Please enter the username that the diagnostic DataSource should connect to the database with:");
		assertPropsEquals(props.get(2), "diagnostics", "jdbc.password",
				"Please enter the password that the diagnostic DataSource should connect to the database with:");
		assertPropsEquals(props.get(3), "diagnostics", "jdbc.driverClassName",
				"Please enter the JDBC driver class name that the diagnostic DataSource should connect to the database with:");
		assertPropsEquals(props.get(4), "diagnostics", "jdbc.url",
				"Please enter the JDBC URL that the diagnostic DataSource should connect to the database with:");
	}

	@Test
	public void insightTemplate() throws Exception {
		List<TemplateProperty> props = reader.read("insight", new NullProgressMonitor());
		Assume.assumeNotNull(props);

		assertEquals(0, props.size());
	}

	@Test
	public void jmxSslTemplate() throws Exception {
		List<TemplateProperty> props = reader.read("jmx-ssl", new NullProgressMonitor());
		Assume.assumeNotNull(props);

		assertEquals(16, props.size());
		assertPropsEquals(
				props.get(0),
				"jmx-ssl",
				"ssl.certificate.dname",
				"Please enter the distinguised name the SSL certificate should use. To be prompted for name components leave blank:",
				"GENERATE");
		assertPropsEquals(props.get(1), "jmx-ssl", "ssl.certificate.dname.CN",
				"Please enter the first and last name the SSL certificate should use:", "Unknown");
		assertPropsEquals(props.get(2), "jmx-ssl", "ssl.certificate.dname.OU",
				"Please enter the organizational unit the SSL certificate should use:", "Unknown");
		assertPropsEquals(props.get(3), "jmx-ssl", "ssl.certificate.dname.O",
				"Please enter the organization the SSL certificate should use:", "Unknown");
		assertPropsEquals(props.get(4), "jmx-ssl", "ssl.certificate.dname.L",
				"Please enter the city or locality the SSL certificate should use:", "Unknown");
		assertPropsEquals(props.get(5), "jmx-ssl", "ssl.certificate.dname.ST",
				"Please enter the state or province the SSL certificate should use:", "Unknown");
		assertPropsEquals(props.get(6), "jmx-ssl", "ssl.certificate.dname.C",
				"Please enter the two-letter country code the SSL certificate should use:", "Unknown");
		assertPropsEquals(props.get(7), "jmx-ssl", "ssl.key.size",
				"Please enter the size in bits that the SSL private key should be:", "2048");
		assertPropsEquals(props.get(8), "jmx-ssl", "ssl.key.alias",
				"Please enter the alias that the keystore should refer to the SSL private key as:", "tc-server-jmx-ssl");
		assertPropsEquals(props.get(9), "jmx-ssl", "ssl.key.alias.input",
				"Please enter the alias that the keystore refers to the SSL private key as:");
		assertPropsEquals(props.get(10), "jmx-ssl", "ssl.key.password",
				"Please enter the password that keystore should protect the SSL private key with:", "RANDOM");
		assertPropsEquals(props.get(11), "jmx-ssl", "ssl.key.password.input",
				"Please enter the password that keystore protects the SSL private key with:");
		assertPropsEquals(props.get(12), "jmx-ssl", "ssl.keystore.location",
				"Please enter the path that the SSL keystore should be stored to:", "conf/tc-server-jmx-ssl.keystore");
		assertPropsEquals(
				props.get(13),
				"jmx-ssl",
				"ssl.keystore.location.input",
				"Please enter the path that the SSL keystore should be read from. To create a new keystore, leave blank:",
				"GENERATE");
		assertPropsEquals(props.get(14), "jmx-ssl", "ssl.keystore.password",
				"Please enter the password that the SSL keystore should protect itself with:", "RANDOM");
		assertPropsEquals(props.get(15), "jmx-ssl", "ssl.keystore.password.input",
				"Please enter the password that the SSL keystore protects itself with:");
	}

	@Test
	public void nioTemplate() throws Exception {
		List<TemplateProperty> props = reader.read("nio", new NullProgressMonitor());
		Assume.assumeNotNull(props);

		assertEquals(2, props.size());
		assertPropsEquals(props.get(0), "nio", "http.port",
				"Please enter the port that the NIO connector should listen for HTTP requests on:", "8080");
		assertPropsEquals(props.get(1), "nio", "https.port",
				"Please enter the port that the NIO connector should redirect HTTPS requests to:", "8443");
	}

	@Test
	public void nioSslTemplate() throws Exception {
		List<TemplateProperty> props = reader.read("nio-ssl", new NullProgressMonitor());
		Assume.assumeNotNull(props);

		assertEquals(17, props.size());
		assertPropsEquals(props.get(0), "nio-ssl", "https.port",
				"Please enter the port that the NIO connector should listen for HTTPS requests on:", "8443");
		assertPropsEquals(
				props.get(1),
				"nio-ssl",
				"ssl.certificate.dname",
				"Please enter the distinguised name the SSL certificate should use. To be prompted for name components leave blank:",
				"GENERATE");
		assertPropsEquals(props.get(2), "nio-ssl", "ssl.certificate.dname.CN",
				"Please enter the first and last name the SSL certificate should use:", "Unknown");
		assertPropsEquals(props.get(3), "nio-ssl", "ssl.certificate.dname.OU",
				"Please enter the organizational unit the SSL certificate should use:", "Unknown");
		assertPropsEquals(props.get(4), "nio-ssl", "ssl.certificate.dname.O",
				"Please enter the organization the SSL certificate should use:", "Unknown");
		assertPropsEquals(props.get(5), "nio-ssl", "ssl.certificate.dname.L",
				"Please enter the city or locality the SSL certificate should use:", "Unknown");
		assertPropsEquals(props.get(6), "nio-ssl", "ssl.certificate.dname.ST",
				"Please enter the state or province the SSL certificate should use:", "Unknown");
		assertPropsEquals(props.get(7), "nio-ssl", "ssl.certificate.dname.C",
				"Please enter the two-letter country code the SSL certificate should use:", "Unknown");
		assertPropsEquals(props.get(8), "nio-ssl", "ssl.key.size",
				"Please enter the size in bits that the SSL private key should be:", "2048");
		assertPropsEquals(props.get(9), "nio-ssl", "ssl.key.alias",
				"Please enter the alias that the keystore should refer to the SSL private key as:", "tc-server-nio-ssl");
		assertPropsEquals(props.get(10), "nio-ssl", "ssl.key.alias.input",
				"Please enter the alias that the keystore refers to the SSL private key as:");
		assertPropsEquals(props.get(11), "nio-ssl", "ssl.key.password",
				"Please enter the password that keystore should protect the SSL private key with:", "RANDOM");
		assertPropsEquals(props.get(12), "nio-ssl", "ssl.key.password.input",
				"Please enter the password that keystore protects the SSL private key with:");
		assertPropsEquals(props.get(13), "nio-ssl", "ssl.keystore.location",
				"Please enter the path that the SSL keystore should be stored to:", "conf/tc-server-nio-ssl.keystore");
		assertPropsEquals(
				props.get(14),
				"nio-ssl",
				"ssl.keystore.location.input",
				"Please enter the path that the SSL keystore should be read from. To create a new keystore, leave blank:",
				"GENERATE");
		assertPropsEquals(props.get(15), "nio-ssl", "ssl.keystore.password",
				"Please enter the password that the SSL keystore should protect itself with:", "RANDOM");
		assertPropsEquals(props.get(16), "nio-ssl", "ssl.keystore.password.input",
				"Please enter the password that the SSL keystore protects itself with:");
	}

	private void assertPropsEquals(TemplateProperty actual, String... expected) {
		assertEquals(expected[0], actual.getTemplate());
		assertEquals(expected[1], actual.getKey());
		assertEquals(expected[2], actual.getMessage());
		if (expected.length == 4) {
			assertEquals("Default values for " + expected[1] + " didn't match:", expected[3], actual.getDefault());
		}
		else {
			assertNull("Expecting null but found: " + actual.getDefault(), actual.getDefault());
		}
	}

	@AfterClass
	public static void afterClass() throws Exception {
		if (server != null) {
			TcServerFixture.deleteServerAndRuntime(server);
		}
	}
}
