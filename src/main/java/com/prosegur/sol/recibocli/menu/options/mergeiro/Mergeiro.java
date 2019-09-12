package com.prosegur.sol.recibocli.menu.options.mergeiro;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prosegur.sol.recibocli.menu.ExecutableOption;
import com.prosegur.sol.recibocli.menu.options.mergeiro.model.Configuracion;
import com.prosegur.sol.recibocli.menu.options.mergeiro.utils.FileSeparator;
import com.prosegur.sol.recibocli.model.BaseInfo;

public class Mergeiro implements ExecutableOption {

	private static final String GENERATED_CONF_REC_PROSEGUR = "/$generated.confRecProsegur";
	private static final String BASECONFIG_JSON = "/baseconfig.json";
	private final ObjectMapper mapper;

	public Mergeiro() {
		mapper = new ObjectMapper();
	}

	@Override
	public void executeOption(BaseInfo baseInfo) {
		FileSeparator fileSeparator = new FileSeparator();

		File devFolder = baseInfo.getFolderSelectedReciboDev();
		List<File> jasperFiles = Arrays.asList(devFolder
				.listFiles((d, s) -> s.toLowerCase().endsWith(".jasper")));
		if (!jasperFiles.isEmpty()) {
			jasperFiles.stream().map(fileSeparator::fileToByteArray)
					.reduce(fileSeparator::merge)
					.ifPresent(mergedBin -> generateConf(baseInfo, devFolder,
							mergedBin));
		} else {
			System.out.println(
					"Os .jrxml não foram compilados em .jasper ainda\n"
							+ "Compile no Ireport os relatórios");
		}
	}

	private void generateConf(BaseInfo baseInfo, File folder,
			byte[] mergedBin) {
		try {
			File baseConfig = new File(
					folder.getAbsolutePath() + BASECONFIG_JSON);
			Configuracion configuration = mapper.readValue(baseConfig,
					Configuracion.class);
			configuration.setReportBase64(
					Base64.getEncoder().encodeToString(mergedBin));
			mapper.writeValue(baseConfig, configuration);

			File generatedConfiguration = new File(folder.getAbsolutePath()
					+ GENERATED_CONF_REC_PROSEGUR.replace("$generated",
							baseInfo.getFolderSelectedRecibo().getName() + "-"
									+ configuration.getCountryCode()));
			FileUtils.copyFile(baseConfig, generatedConfiguration);
			resetBaseConfig(baseInfo);
			System.out.println("Arquivo para ser importado no sol em: "
					+ generatedConfiguration.getAbsolutePath());
		} catch (IOException e) {
			System.out.println("baseconfig.json não encontrado");
		}
	}

	private void resetBaseConfig(BaseInfo baseInfo) throws IOException {
		File devBaseConfig = new File(
				baseInfo.getFolderSelectedReciboDev().getAbsolutePath()
						+ BASECONFIG_JSON);
		File baseConfig = new File(
				baseInfo.getFolderSelectedRecibo().getAbsolutePath()
						+ BASECONFIG_JSON);

		FileUtils.copyFile(baseConfig, devBaseConfig);
	}

}
