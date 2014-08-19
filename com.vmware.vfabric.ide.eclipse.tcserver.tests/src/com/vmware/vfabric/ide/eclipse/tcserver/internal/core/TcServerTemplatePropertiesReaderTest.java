/*******************************************************************************
 * Copyright (c) 2012 - 2013 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package com.vmware.vfabric.ide.eclipse.tcserver.internal.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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
		server = TcServerFixture.current().createServer(null);
		reader = new TemplatePropertiesReader(server);
	}

	@Test
	public void haveTestsForAllTemplates() {
		Set<String> actualTemplates = new HashSet<String>();
		IPath runtimePath = server.getRuntime().getLocation();
		IPath templatePath = runtimePath.append("templates");
		if (templatePath.toFile().exists()) {
			File[] children = templatePath.toFile().listFiles();
			if (children != null) {
				for (File child : children) {
					String template = TcServerUtil.getTemplateName(child);
					if (template != null) {
						actualTemplates.add(template);
					}
				}
			}
		}
		String[] expecteds = new String[] { "ajp", "apr", "apr-ssl", "async-logger", "base", "bio", "bio-ssl",
				"cluster-node", "diagnostics", "insight", "jmx-ssl", "nio", "nio-ssl" };
		assertTrue("Not all basic templates are present in current tc server runtime installation",
				actualTemplates.containsAll(Arrays.asList(expecteds)));
	}

	@Test
	public void ajpTemplate() throws Exception {
		Set<TemplateProperty> props = reader.read("ajp", new NullProgressMonitor());
		Assume.assumeNotNull(props);

		assertEquals(2, props.size());
		assertPropsEquals(props, "ajp", "http.port",
				"Please enter the port that the AJP connector should listen for requests on:", "8009");
		assertPropsEquals(props, "ajp", "https.port",
				"Please enter the port that the AJP connector should redirect SSL requests to:", "8443");
	}

	@Test
	public void aprTemplate() throws Exception {
		Set<TemplateProperty> props = reader.read("apr", new NullProgressMonitor());
		Assume.assumeNotNull(props);

		assertEquals(2, props.size());
		assertPropsEquals(props, "apr", "http.port",
				"Please enter the port that the APR connector should listen for HTTP requests on:", "8080");
		assertPropsEquals(props, "apr", "https.port",
				"Please enter the port that the APR connector should redirect HTTPS requests to:", "8443");
	}

	@Test
	public void aprSslTemplate() throws Exception {
		Set<TemplateProperty> props = reader.read("apr-ssl", new NullProgressMonitor());
		Assume.assumeNotNull(props);

		assertEquals(13, props.size());
		assertPropsEquals(props, "apr-ssl", "https.port",
				"Please enter the port that the APR connector should listen for HTTPS requests on:", "8443");
		assertPropsEquals(
				props,
				"apr-ssl",
				"ssl.certificate.dname",
				"Please enter the distinguised name the SSL certificate should use. To create a new name, leave blank:",
				"GENERATE");
		assertPropsEquals(props, "apr-ssl", "ssl.certificate.dname.CN",
				"Please enter the first and last name the SSL certificate should use:", "Unknown");
		assertPropsEquals(props, "apr-ssl", "ssl.certificate.dname.OU",
				"Please enter the organizational unit the SSL certificate should use:", "Unknown");
		assertPropsEquals(props, "apr-ssl", "ssl.certificate.dname.O",
				"Please enter the organization the SSL certificate should use:", "Unknown");
		assertPropsEquals(props, "apr-ssl", "ssl.certificate.dname.L",
				"Please enter the city or locality the SSL certificate should use:", "Unknown");
		assertPropsEquals(props, "apr-ssl", "ssl.certificate.dname.ST",
				"Please enter the state or province the SSL certificate should use:", "Unknown");
		assertPropsEquals(props, "apr-ssl", "ssl.certificate.dname.C",
				"Please enter the two-letter country code the SSL certificate should use:", "Unknown");
		assertPropsEquals(props, "apr-ssl", "ssl.key.size",
				"Please enter the size in bits that the SSL private key should be:", "2048");
		assertPropsEquals(props, "apr-ssl", "ssl.certificate.location",
				"Please enter the path that the SSL certificate should be stored to:", "conf/tc-server.cer");
		assertPropsEquals(
				props,
				"apr-ssl",
				"ssl.certificate.location.input",
				"Please enter the path that the SSL certificate should be read from. To create a new certificate, leave blank:",
				"GENERATE");
		assertPropsEquals(props, "apr-ssl", "ssl.private.key.location",
				"Please enter the path that the SSL private key should be stored to:", "conf/tc-server.key");
		assertPropsEquals(props, "apr-ssl", "ssl.private.key.location.input",
				"Please enter the path that the SSL private key should be read from:");
	}

	@Test
	public void asyncLoggerTemplate() throws Exception {
		Set<TemplateProperty> props = reader.read("async-logger", new NullProgressMonitor());
		Assume.assumeNotNull(props);

		assertEquals(0, props.size());
	}

	@Test
	public void baseTemplate() throws Exception {
		Set<TemplateProperty> props = reader.read("base", new NullProgressMonitor());
		Assume.assumeNotNull(props);

		assertEquals(3, props.size());
		assertPropsEquals(props, "base", "jmx.port",
				"Please enter the port that the JMX socket listener should listen on:", "6969");
		assertPropsEquals(props, "base", "shutdown.port",
				"Please enter the port that Tomcat Shutdown should listen on:", "-1");
		assertPropsEquals(props, "base", "runtime.user",
				"Please enter the user account that should start the instance when using the 'bin/init.d.sh' script:",
				"tcserver");
	}

	@Test
	public void bioTemplate() throws Exception {
		Set<TemplateProperty> props = reader.read("bio", new NullProgressMonitor());
		Assume.assumeNotNull(props);

		assertEquals(2, props.size());
		assertPropsEquals(props, "bio", "http.port",
				"Please enter the port that the BIO connector should listen for HTTP requests on:", "8080");
		assertPropsEquals(props, "bio", "https.port",
				"Please enter the port that the BIO connector should redirect HTTPS requests to:", "8443");
	}

	@Test
	public void bioSslTemplate() throws Exception {
		Set<TemplateProperty> props = reader.read("bio-ssl", new NullProgressMonitor());
		Assume.assumeNotNull(props);

		assertEquals(17, props.size());
		assertPropsEquals(props, "bio-ssl", "https.port",
				"Please enter the port that the BIO connector should listen for HTTPS requests on:", "8443");
		assertPropsEquals(
				props,
				"bio-ssl",
				"ssl.certificate.dname",
				"Please enter the distinguised name the SSL certificate should use (i.e. cn=MyCompany,dc=mycompany,dc=com). To be prompted for name components leave blank:",
				"GENERATE");
		assertPropsEquals(props, "bio-ssl", "ssl.certificate.dname.CN",
				"Please enter the first and last name the SSL certificate should use:", "Unknown");
		assertPropsEquals(props, "bio-ssl", "ssl.certificate.dname.OU",
				"Please enter the organizational unit the SSL certificate should use:", "Unknown");
		assertPropsEquals(props, "bio-ssl", "ssl.certificate.dname.O",
				"Please enter the organization the SSL certificate should use:", "Unknown");
		assertPropsEquals(props, "bio-ssl", "ssl.certificate.dname.L",
				"Please enter the city or locality the SSL certificate should use:", "Unknown");
		assertPropsEquals(props, "bio-ssl", "ssl.certificate.dname.ST",
				"Please enter the state or province the SSL certificate should use:", "Unknown");
		assertPropsEquals(props, "bio-ssl", "ssl.certificate.dname.C",
				"Please enter the two-letter country code the SSL certificate should use:", "Unknown");
		assertPropsEquals(props, "bio-ssl", "ssl.key.size",
				"Please enter the size in bits that the SSL private key should be:", "2048");
		assertPropsEquals(props, "bio-ssl", "ssl.key.alias",
				"Please enter the alias that the keystore should refer to the SSL private key as:", "tc-server-bio-ssl");
		assertPropsEquals(props, "bio-ssl", "ssl.key.alias.input",
				"Please enter the alias that the keystore refers to the SSL private key as:");
		assertPropsEquals(props, "bio-ssl", "ssl.key.password",
				"Please enter the password that keystore should protect the SSL private key with:", "RANDOM");
		assertPropsEquals(props, "bio-ssl", "ssl.key.password.input",
				"Please enter the password that keystore protects the SSL private key with:");
		assertPropsEquals(props, "bio-ssl", "ssl.keystore.location",
				"Please enter the path that the SSL keystore should be stored to:", "conf/tc-server-bio-ssl.keystore");
		assertPropsEquals(
				props,
				"bio-ssl",
				"ssl.keystore.location.input",
				"Please enter the path that the SSL keystore should be read from. To create a new keystore, leave blank:",
				"GENERATE");
		assertPropsEquals(props, "bio-ssl", "ssl.keystore.password",
				"Please enter the password that the SSL keystore should protect itself with:", "RANDOM");
		assertPropsEquals(props, "bio-ssl", "ssl.keystore.password.input",
				"Please enter the password that the SSL keystore protects itself with:");
	}

	@Test
	public void clusterNodeTemplate() throws Exception {
		Set<TemplateProperty> props = reader.read("cluster-node", new NullProgressMonitor());
		Assume.assumeNotNull(props);

		assertEquals(1, props.size());
		assertPropsEquals(props, "cluster-node", "node.name",
				"Please enter the cluster node name used to identify this instance:", "tc-runtime-1");
	}

	@Test
	public void diagnosticsTemplate() throws Exception {
		Set<TemplateProperty> props = reader.read("diagnostics", new NullProgressMonitor());
		Assume.assumeNotNull(props);

		assertEquals(5, props.size());
		assertPropsEquals(props, "diagnostics", "jdbc.resource.name",
				"Please enter the JNDI name that the diagnostic DataSource should be available at:");
		assertPropsEquals(props, "diagnostics", "jdbc.username",
				"Please enter the username that the diagnostic DataSource should connect to the database with:");
		assertPropsEquals(props, "diagnostics", "jdbc.password",
				"Please enter the password that the diagnostic DataSource should connect to the database with:");
		assertPropsEquals(props, "diagnostics", "jdbc.driverClassName",
				"Please enter the JDBC driver class name that the diagnostic DataSource should connect to the database with:");
		assertPropsEquals(props, "diagnostics", "jdbc.url",
				"Please enter the JDBC URL that the diagnostic DataSource should connect to the database with:");
	}

	@Test
	public void insightTemplate() throws Exception {
		Set<TemplateProperty> props = reader.read("insight", new NullProgressMonitor());
		Assume.assumeNotNull(props);

		assertEquals(4, props.size());
	}

	@Test
	public void jmxSslTemplate() throws Exception {
		Set<TemplateProperty> props = reader.read("jmx-ssl", new NullProgressMonitor());
		Assume.assumeNotNull(props);

		assertEquals(16, props.size());
		assertPropsEquals(
				props,
				"jmx-ssl",
				"ssl.certificate.dname",
				"Please enter the distinguised name the SSL certificate should use. To be prompted for name components leave blank:",
				"GENERATE");
		assertPropsEquals(props, "jmx-ssl", "ssl.certificate.dname.CN",
				"Please enter the first and last name the SSL certificate should use:", "Unknown");
		assertPropsEquals(props, "jmx-ssl", "ssl.certificate.dname.OU",
				"Please enter the organizational unit the SSL certificate should use:", "Unknown");
		assertPropsEquals(props, "jmx-ssl", "ssl.certificate.dname.O",
				"Please enter the organization the SSL certificate should use:", "Unknown");
		assertPropsEquals(props, "jmx-ssl", "ssl.certificate.dname.L",
				"Please enter the city or locality the SSL certificate should use:", "Unknown");
		assertPropsEquals(props, "jmx-ssl", "ssl.certificate.dname.ST",
				"Please enter the state or province the SSL certificate should use:", "Unknown");
		assertPropsEquals(props, "jmx-ssl", "ssl.certificate.dname.C",
				"Please enter the two-letter country code the SSL certificate should use:", "Unknown");
		assertPropsEquals(props, "jmx-ssl", "ssl.key.size",
				"Please enter the size in bits that the SSL private key should be:", "2048");
		assertPropsEquals(props, "jmx-ssl", "ssl.key.alias",
				"Please enter the alias that the keystore should refer to the SSL private key as:", "tc-server-jmx-ssl");
		assertPropsEquals(props, "jmx-ssl", "ssl.key.alias.input",
				"Please enter the alias that the keystore refers to the SSL private key as:");
		assertPropsEquals(props, "jmx-ssl", "ssl.key.password",
				"Please enter the password that keystore should protect the SSL private key with:", "RANDOM");
		assertPropsEquals(props, "jmx-ssl", "ssl.key.password.input",
				"Please enter the password that keystore protects the SSL private key with:");
		assertPropsEquals(props, "jmx-ssl", "ssl.keystore.location",
				"Please enter the path that the SSL keystore should be stored to:", "conf/tc-server-jmx-ssl.keystore");
		assertPropsEquals(
				props,
				"jmx-ssl",
				"ssl.keystore.location.input",
				"Please enter the path that the SSL keystore should be read from. To create a new keystore, leave blank:",
				"GENERATE");
		assertPropsEquals(props, "jmx-ssl", "ssl.keystore.password",
				"Please enter the password that the SSL keystore should protect itself with:", "RANDOM");
		assertPropsEquals(props, "jmx-ssl", "ssl.keystore.password.input",
				"Please enter the password that the SSL keystore protects itself with:");
	}

	@Test
	public void nioTemplate() throws Exception {
		Set<TemplateProperty> props = reader.read("nio", new NullProgressMonitor());
		Assume.assumeNotNull(props);

		assertEquals(2, props.size());
		assertPropsEquals(props, "nio", "http.port",
				"Please enter the port that the NIO connector should listen for HTTP requests on:", "8080");
		assertPropsEquals(props, "nio", "https.port",
				"Please enter the port that the NIO connector should redirect HTTPS requests to:", "8443");
	}

	@Test
	public void nioSslTemplate() throws Exception {
		Set<TemplateProperty> props = reader.read("nio-ssl", new NullProgressMonitor());
		Assume.assumeNotNull(props);

		assertEquals(17, props.size());
		assertPropsEquals(props, "nio-ssl", "https.port",
				"Please enter the port that the NIO connector should listen for HTTPS requests on:", "8443");
		assertPropsEquals(
				props,
				"nio-ssl",
				"ssl.certificate.dname",
				"Please enter the distinguised name the SSL certificate should use. To be prompted for name components leave blank:",
				"GENERATE");
		assertPropsEquals(props, "nio-ssl", "ssl.certificate.dname.CN",
				"Please enter the first and last name the SSL certificate should use:", "Unknown");
		assertPropsEquals(props, "nio-ssl", "ssl.certificate.dname.OU",
				"Please enter the organizational unit the SSL certificate should use:", "Unknown");
		assertPropsEquals(props, "nio-ssl", "ssl.certificate.dname.O",
				"Please enter the organization the SSL certificate should use:", "Unknown");
		assertPropsEquals(props, "nio-ssl", "ssl.certificate.dname.L",
				"Please enter the city or locality the SSL certificate should use:", "Unknown");
		assertPropsEquals(props, "nio-ssl", "ssl.certificate.dname.ST",
				"Please enter the state or province the SSL certificate should use:", "Unknown");
		assertPropsEquals(props, "nio-ssl", "ssl.certificate.dname.C",
				"Please enter the two-letter country code the SSL certificate should use:", "Unknown");
		assertPropsEquals(props, "nio-ssl", "ssl.key.size",
				"Please enter the size in bits that the SSL private key should be:", "2048");
		assertPropsEquals(props, "nio-ssl", "ssl.key.alias",
				"Please enter the alias that the keystore should refer to the SSL private key as:", "tc-server-nio-ssl");
		assertPropsEquals(props, "nio-ssl", "ssl.key.alias.input",
				"Please enter the alias that the keystore refers to the SSL private key as:");
		assertPropsEquals(props, "nio-ssl", "ssl.key.password",
				"Please enter the password that keystore should protect the SSL private key with:", "RANDOM");
		assertPropsEquals(props, "nio-ssl", "ssl.key.password.input",
				"Please enter the password that keystore protects the SSL private key with:");
		assertPropsEquals(props, "nio-ssl", "ssl.keystore.location",
				"Please enter the path that the SSL keystore should be stored to:", "conf/tc-server-nio-ssl.keystore");
		assertPropsEquals(
				props,
				"nio-ssl",
				"ssl.keystore.location.input",
				"Please enter the path that the SSL keystore should be read from. To create a new keystore, leave blank:",
				"GENERATE");
		assertPropsEquals(props, "nio-ssl", "ssl.keystore.password",
				"Please enter the password that the SSL keystore should protect itself with:", "RANDOM");
		assertPropsEquals(props, "nio-ssl", "ssl.keystore.password.input",
				"Please enter the password that the SSL keystore protects itself with:");
	}

	private void assertPropsEquals(Set<TemplateProperty> actual, String... expected) {
		for (TemplateProperty prop : actual) {
			if (prop.getKey().equals(expected[1])) {
				assertPropsEquals(prop, expected);
				return;
			}
		}
		fail("Could not find property with the given key: " + expected[1]);
	}

	private void assertPropsEquals(TemplateProperty actual, String... expected) {
		assertEquals(expected[0], actual.getTemplate());
		assertEquals(expected[1], actual.getKey());
		assertEquals(expected[2], actual.getMessage());
		if (expected.length == 4) {
			assertEquals("Default values for " + expected[1] + " didn't match:", expected[3], actual.getRawDefault());
		}
		else {
			assertNull("Expecting null but found: " + actual.getRawDefault(), actual.getRawDefault());
		}
	}

	@AfterClass
	public static void afterClass() throws Exception {
		if (server != null) {
			TcServerFixture.deleteServerAndRuntime(server);
		}
	}
}
