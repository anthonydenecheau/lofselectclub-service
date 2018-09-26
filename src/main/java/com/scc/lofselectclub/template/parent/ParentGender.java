package com.scc.lofselectclub.template.parent;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;

public class ParentGender {

	@ApiModelProperty(notes = "number of parent", position = 1, allowEmptyValue = true)
	int qtity;

	@ApiModelProperty(notes = "detail by register", position = 2, allowEmptyValue = true)
	List<ParentRegisterType> registerType;

	public int getQtity() {
		return qtity;
	}

	public void setQtity(int qtity) {
		this.qtity = qtity;
	}

	public List<ParentRegisterType> getRegisterType() {
		return registerType;
	}

	public void setRegisterType(List<ParentRegisterType> registerType) {
		this.registerType = registerType;
	}

	public ParentGender withQtity(int qtity) {
		this.setQtity(qtity);
		return this;
	}

	public ParentGender withRegisterType(List<ParentRegisterType> registerType) {
		this.setRegisterType(registerType);
		return this;
	}

	@Override
	public String toString() {
		return "Gender [qtity=" + qtity + ", registerType=" + registerType + "]";
	}

}
