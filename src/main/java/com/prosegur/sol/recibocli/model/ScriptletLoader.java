package com.prosegur.sol.recibocli.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ScriptletLoader {

	private static final String JASPER_REPORT_SCRIPTLET_XPATH = "jasperReport/scriptlet";
	private static final String MASTER_JRXML_SUFFIX = "Master.jrxml";
	private static final Pattern METHOD_SIGNATURE_REGEX = Pattern.compile(
			"public\\s\\w+\\s\\w+\\s?\\(.+\\)\\s?(throws)?.*\\{",
			Pattern.MULTILINE);
	private static final Pattern CONST_VARIABLE_REGEX = Pattern.compile("public\\s(static)\\s((final)\\s)?\\w+\\s\\w+\\s?(=)(.*)(;)");

	private File folderSelectedReciboDev;
	private Properties properties;

	public ScriptletLoader(File folderSelectedReciboDev,
			Properties properties) {
		this.folderSelectedReciboDev = folderSelectedReciboDev;
		this.properties = properties;
	}

	public void load() {
		Arrays.asList(folderSelectedReciboDev
				.list((dir, name) -> name.endsWith(MASTER_JRXML_SUFFIX)))
				.stream().findFirst()
				.map(masterReport -> new File(
						folderSelectedReciboDev.getAbsolutePath(),
						masterReport))
				.ifPresent(this::loadFromReport);
	}

	private void loadFromReport(File masterReport) {
		List<String> scriptlets = getScriptlets(masterReport);
		if (scriptlets.isEmpty()) {
			System.out.println("No Scriptlets to load!");
		} else {
			scriptlets.forEach(this::loadScriptlet);
		}
	}

	private List<String> getScriptlets(File masterReport) {
		try (FileInputStream inputStream = new FileInputStream(masterReport)) {
			DocumentBuilder documentBuilder = DocumentBuilderFactory
					.newInstance().newDocumentBuilder();
			Document xmlDocument = documentBuilder.parse(inputStream);
			XPath xPath = XPathFactory.newInstance().newXPath();
			NodeList scriptletNode = (NodeList) xPath
					.compile(JASPER_REPORT_SCRIPTLET_XPATH)
					.evaluate(xmlDocument, XPathConstants.NODESET);
			return getScriptlets(scriptletNode);
		} catch (Exception e) {
			return new ArrayList<>();
		}
	}

	private List<String> getScriptlets(NodeList scriptletNode) {
		ArrayList<String> scriptlets = new ArrayList<>();

		for (int i = 0; i < scriptletNode.getLength(); i++) {
			Node node = scriptletNode.item(i);
			String scriptlet = node.getAttributes().getNamedItem("class")
					.getTextContent();
			scriptlets.add(scriptlet);
		}

		return scriptlets;
	}

	private void loadScriptlet(String scriptletFullName) {
		String pathToScriptlet = scriptletFullName.replaceAll("\\.", "/");
		String topFolder = createFoldersScriptlet(pathToScriptlet);
		String pathScriptlet = properties.getProperty("projectScriptlet")
				+ pathToScriptlet + ".java";
		File scriptlet = new File(pathScriptlet);
		System.out.println("Scriptlet found @ " + pathScriptlet);

		try {
			File stubScriptlet = createStubScriptlet(topFolder, scriptlet,
					scriptletFullName);
			compileStub(stubScriptlet.getAbsolutePath());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void compileStub(String pathStub) throws Exception {
		String jdk7Home = properties.getProperty("jdk7Home");
		ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c",
				"\"" + jdk7Home + "bin\\javac.exe\" " + pathStub);
		builder.redirectErrorStream(true);
		// builder.start();
		Process p = builder.start();
		BufferedReader r = new BufferedReader(
				new InputStreamReader(p.getInputStream()));
		String line;
		while (true) {
			line = r.readLine();
			if (line == null) {
				break;
			}
			System.out.println(line);
		}
	}

	private String createFoldersScriptlet(String scriptletFullName) {
		List<String> folders = new ArrayList<>(
				Arrays.asList(scriptletFullName.split("/")));
		folders.remove(folders.size() - 1);

		String topDir = folders.stream().collect(Collectors.joining("/"));
		new File(folderSelectedReciboDev, topDir).mkdirs();

		return folderSelectedReciboDev.getPath() + '/' + topDir;
	}

	private File createStubScriptlet(String path, File scriptlet,
			String scriptletFullName) throws IOException {
		File stub = new File(path, scriptlet.getName());

		String clazzName = scriptlet.getName().replace(".java", "");

		BufferedReader br = new BufferedReader(new FileReader(scriptlet));
		BufferedWriter bw = new BufferedWriter(new FileWriter(stub));
		bw.write("package " + scriptletFullName.replace("." + clazzName, "")
				+ ";\n\n");
		loadImports(bw);
		bw.write("public class " + clazzName + " {\n\n");
		bw.write("public " + clazzName + "() {}\n\n");
		String javaClass = IOUtils.toString(br);
		writeConstantes(bw, javaClass);
		writeMethods(bw, javaClass);
		bw.write("}");

		br.close();
		bw.close();
		return stub;
	}

	private void writeConstantes(BufferedWriter bw, String javaClass) throws IOException {
		Matcher matcher = CONST_VARIABLE_REGEX.matcher(javaClass);
		while (matcher.find()) {
			String constant = matcher.group();
			bw.write(String.format("\t %s%n", constant));
		}
		bw.write(String.format("%n%n"));
	}

	private void loadImports(BufferedWriter bw) throws IOException {
		bw.write("import java.util.*;\n");
		bw.write("import java.io.*;\n");
		bw.write("import java.awt.*;\n");
		bw.write("import java.math.*;\n");
		bw.write("import java.text.*;\n");
		bw.write("import java.lang.*;\n\n");
	}

	private void writeMethods(BufferedWriter bw, String javaClass)
			throws IOException {

		Matcher matcher = METHOD_SIGNATURE_REGEX.matcher(javaClass);
		while (matcher.find()) {
			String methodSignature = matcher.group();
			bw.write(methodSignature.replaceAll("throws \\w+", ""));
			bw.write("  return");
			bw.write(getMethodReturn(methodSignature));
			bw.write("  }\n\n");
		}
	}

	private String getMethodReturn(String methodSignature) {
		String returnValue = ";\n\n";
		String returnType = methodSignature.replace("public ", "").split(" ")[0]
				.trim();

		if (Character.isUpperCase(returnType.charAt(0))
				|| returnType.contains("[")) {
			returnValue = " null;";
		} else if (returnType.equals("boolean")) {
			returnValue = " false;";
		} else if (!returnType.equals("void")) {
			returnValue = " 0;";
		}

		return returnValue;
	}

}
