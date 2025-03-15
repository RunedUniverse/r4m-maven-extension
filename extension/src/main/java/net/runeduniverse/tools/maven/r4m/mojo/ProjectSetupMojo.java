/*
 * Copyright © 2025 VenaNocta (venanocta@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.runeduniverse.tools.maven.r4m.mojo;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * generates the full pem.xml from all active maven defaults
 *
 * @goal prj-setup
 * @requiresProject false
 * @requiresReports false
 * @threadSafe false
 * @since 1.1.0
 * @author VenaNocta
 */
public class ProjectSetupMojo extends AbstractMojo {

	/**
	 * @parameter default-value="${session}"
	 * @readonly
	 */
	private MavenSession mvnSession;
	/**
	 * @parameter default-value="${project}"
	 * @readonly
	 */
	private MavenProject mvnProject;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		final Path mvnExtConfig = Paths.get(mvnSession.getExecutionRootDirectory(), ".mvn", "extensions.xml");
		// ensure .mvn folder exists
		final Path mvnCnfFolder = mvnExtConfig.getParent();
		if (Files.notExists(mvnCnfFolder)) {
			try {
				getLog().debug("Creating folder » " + mvnCnfFolder);
				Files.createDirectories(mvnCnfFolder);
			} catch (IOException e) {
				new MojoFailureException("Failed to create maven config folder!", e);
			}
		} else if (!Files.isDirectory(mvnCnfFolder)) {
			throw new MojoFailureException("Maven config folder is a file!");
		}
		// ensure extensions.xml file is modifyable
		if (Files.exists(mvnExtConfig)) {
			if (!Files.isRegularFile(mvnExtConfig))
				throw new MojoFailureException("Maven extension config file is not a file!");
			if (!Files.isReadable(mvnExtConfig))
				throw new MojoFailureException("Maven extension config file is not readable!");
			if (!Files.isWritable(mvnExtConfig))
				throw new MojoFailureException("Maven extension config file is not writeable!");
		}
		// modify extensions.xml
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try (InputStream inSream = new FileInputStream(mvnExtConfig.toFile())) {
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(inSream);
			Element element = document.getDocumentElement();
			Element ext = document.createElement("extension");
			element.appendChild(ext);
			Element groupId = document.createElement("groupId");
			groupId.setTextContent("net.runeduniverse.tools.maven.r4m");
			ext.appendChild(groupId);
			Element artifactId = document.createElement("artifactId");
			artifactId.setTextContent("r4m-maven-extension");
			ext.appendChild(artifactId);
			Element version = document.createElement("version");
			version.setTextContent("1.1.0");
			ext.appendChild(version);

			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();

			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.STANDALONE, "no");

			DOMSource source = new DOMSource(document);

			try (FileOutputStream outStream = new FileOutputStream(mvnExtConfig.toFile())) {
				StreamResult result = new StreamResult(outStream);
				transformer.transform(source, result);
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		getLog().info(mvnExtConfig.toString());
	}
}
