package com.prosegur.sol.recibocli;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.Scanner;

import com.prosegur.sol.recibocli.menu.WorkspaceMenu;
import com.prosegur.sol.recibocli.model.BaseInfo;

public class App {

	private static Scanner scanner;
	
	public static void main(String[] args) throws IOException {
		System.out.print("Recibo Electronico CLI - ");
		BaseInfo baseInfo = loadBaseInfo();
		System.out.println("Qual recibo se deseja trabalhar?");
		baseInfo.setFolderSelectedRecibo(
				selectReciboToWork(baseInfo.getPathTfs()));
		if (baseInfo.hasDevelopmentNotStarted()) {
			baseInfo.startDevelopment();
		}
	
		new WorkspaceMenu(baseInfo).init(scanner);
	}

	private static BaseInfo loadBaseInfo() throws IOException {
		final Properties properties = new Properties();
		properties.load(App.class.getClassLoader()
				.getResourceAsStream("application.properties"));
		System.out.println("version " + properties.getProperty("version"));
		return new BaseInfo(preparePathTFS(properties));
	}

	private static String preparePathTFS(Properties properties) {
		String basePath = properties.getProperty("basePath");
		String tfsFolder = properties.getProperty("tfsFolder");
		String pathBranch = properties.getProperty("pathBranch");
		String pathTFS = properties.getProperty("pathTFS");
		String pathReports = basePath.replace("$tfsFolder", tfsFolder)
				.replace("$pathBranch", pathBranch).concat(pathTFS);
		System.out.println("Buscando os recibos de " + pathReports);
		return pathReports;
	}

	private static File selectReciboToWork(String basePathJasper) {
		File[] folder = new File(basePathJasper).listFiles();
		System.out.println("****************");
		for (int i = 0; i < folder.length; i++) {
			File file = folder[i];
			System.out.println(i + " - " + file.getName());
		}
		System.out.println("****************");

		scanner = new Scanner(System.in);
		int indexFile = scanner.nextInt();
		File folderSelected = folder[indexFile];
		System.out.println(folderSelected.getName() + " selecionado");
		return folderSelected;
	}

}
