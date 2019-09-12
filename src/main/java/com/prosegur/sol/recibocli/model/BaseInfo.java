package com.prosegur.sol.recibocli.model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;

import lombok.Data;

@Data
public class BaseInfo {

	private static final String DEV_FOLDER = "dev";
	private String pathTfs;
	private File folderSelectedRecibo;
	private File folderSelectedReciboDev;

	public BaseInfo(String pathTfs) {
		this.pathTfs = pathTfs;
	}

	public boolean hasDevelopmentStarted() {
		boolean hasDevelopmentStarted = folderSelectedRecibo
				.list((d, s) -> s.equals(DEV_FOLDER)).length == 1;
		if (hasDevelopmentStarted) {
			folderSelectedReciboDev = new File(folderSelectedRecibo,
					DEV_FOLDER);
		}
		return hasDevelopmentStarted;
	}

	public boolean hasDevelopmentNotStarted() {
		return !hasDevelopmentStarted();
	}

	public void startDevelopment() {
		try {
			System.out.println("Iniciando workspace recibo "
					+ folderSelectedRecibo.getName());
			folderSelectedReciboDev = new File(folderSelectedRecibo,
					DEV_FOLDER);
			folderSelectedReciboDev.mkdir();
			createWorkspace();
			System.out.println("workspace inicializado com sucesso!");
			warnTFS(true);
		} catch (IOException e) {
			System.out.println("workspace não foi inicializado com sucesso");
		}
	}

	public void finishDevelopment() {
		try {
			System.out.println(
					"Levando as alteração de desenvolvimento ao folder do TFS");
			FileUtils.copyDirectory(folderSelectedReciboDev,
					folderSelectedRecibo);
			System.out.println("Alterações levadas\nDeletando o workspace");
			FileUtils.deleteDirectory(folderSelectedReciboDev);
			System.out.println("Processamento concluído!");
			warnTFS(false);
			System.exit(0);
		} catch (IOException e) {
			System.out.println("Os arquivos .jrxml estão com checkout?\nA pasta dev está fechada?\nO iReport está fechado?");
		}
	}

	private void createWorkspace() throws IOException {
		FileUtils.copyDirectory(folderSelectedRecibo, folderSelectedReciboDev);
		Files.delete(new File(folderSelectedReciboDev, DEV_FOLDER).toPath());
	}

	private void warnTFS(boolean checkout) {
		String tfsOperation = checkout ? "checkout" : "checkin";
		System.out.println("\n***************\nATENÇÃO: Lembre-se de dar "
				+ tfsOperation + " no TFS nos seguintes arquivos:");
		Arrays.asList(folderSelectedRecibo
				.listFiles((d, s) -> s.toLowerCase().endsWith(".jrxml")))
				.forEach(System.out::println);
		System.out.println("***************\n");
	}

	public void rollback() {
		try {
			FileUtils.deleteDirectory(folderSelectedReciboDev);
			FileUtils.copyDirectory(folderSelectedRecibo,
					folderSelectedReciboDev);
			System.out.println("Workspace recriado");
		} catch (IOException e) {
			System.out.println(
					"Não foi possível deletar o folder dev\nVerifique se o Ireport está fechado!");
		}
	}

}
