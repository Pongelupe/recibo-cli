package com.prosegur.sol.recibocli.menu;

import com.prosegur.sol.recibocli.menu.options.mergeiro.Mergeiro;

public enum MenuOption {

	MERGE("Merge",
			"Realiza merge dos Reports e seus subReports, assim gerando o arquivo .confRecProsegur para ser importado no SOL WEB",
			new Mergeiro()), FINISH_DEVELOPMENT("Finalizar desenvolvimento",
					"Finalizar desenvolvimento. A pasta dev será excluida",
					baseInfo -> baseInfo.finishDevelopment()), ROLLBACK(
							"Rollback",
							"Rollback das alterações de Recibo, o workspace será recriado",
							baseInfo -> baseInfo.rollback()), EXIT("Sair",
									"Saindo...", baseInfo -> System.exit(0));

	private MenuOption(String shortDescriptionOption, String descriptionOption,
			ExecutableOption action) {
		this.shortDescriptionOption = shortDescriptionOption;
		this.descriptionOption = descriptionOption;
		this.action = action;
	}

	final String shortDescriptionOption;
	final String descriptionOption;
	final ExecutableOption action;

}
