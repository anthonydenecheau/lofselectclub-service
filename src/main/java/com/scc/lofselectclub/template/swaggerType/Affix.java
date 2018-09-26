package com.scc.lofselectclub.template.swaggerType;

import io.swagger.annotations.ApiModelProperty;

public class Affix {

	@ApiModelProperty(notes = "Name property", position = 1, allowEmptyValue = true)
	private String name;

	@ApiModelProperty(notes = "Quantity property", position = 2, allowEmptyValue = true)
	private String qtity;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getQtity() {
		return qtity;
	}

	public void setQtity(String qtity) {
		this.qtity = qtity;
	}

}
