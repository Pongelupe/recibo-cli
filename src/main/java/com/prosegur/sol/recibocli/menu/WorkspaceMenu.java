package com.prosegur.sol.recibocli.menu;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import com.prosegur.sol.recibocli.model.BaseInfo;

public class WorkspaceMenu {

	private final BaseInfo baseInfo;
	List<MenuOption> options = Arrays.asList(MenuOption.values());

	public WorkspaceMenu(BaseInfo baseInfo) {
		this.baseInfo = baseInfo;
	}

	public void init(Scanner in) {
		MenuOption option = null;
		while (option != MenuOption.EXIT) {
			displayOptions();
			try {
				int op = in.nextInt();
				option = options.get(op);
			} catch (Exception e) {
				option = null;
			}
			Optional<MenuOption> optional = Optional.ofNullable(option);
			if (optional.isPresent()) {
				System.out.println();
				MenuOption menuOption = optional.get();
				System.out.println(menuOption.descriptionOption);
				System.out.println();
				menuOption.action.executeOption(baseInfo);
			} else {
				System.out.println("Comando não identificado");
			}
		}
	}

	private void displayOptions() {
		System.out.println();
		for (int i = 0; i < options.size(); i++) {
			System.out
					.println(i + "- " + options.get(i).shortDescriptionOption);
		}
		System.out.print("\n> ");
	}

}
