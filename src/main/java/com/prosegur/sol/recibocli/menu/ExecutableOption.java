package com.prosegur.sol.recibocli.menu;

import com.prosegur.sol.recibocli.model.BaseInfo;

@FunctionalInterface
public interface ExecutableOption {
	
	void executeOption(BaseInfo baseInfo);

}
