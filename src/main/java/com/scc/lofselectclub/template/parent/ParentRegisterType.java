package com.scc.lofselectclub.template.parent;

import io.swagger.annotations.ApiModelProperty;

public class ParentRegisterType {

	@ApiModelProperty(notes = "registration type", position = 1, allowEmptyValue=true)
	String registration;

	@ApiModelProperty(notes = "number of male/female", position = 2, allowEmptyValue=true)
	int qtity;

	@ApiModelProperty(notes = "percentage", position = 3, allowEmptyValue=true)
	String percentage;

	public String getRegistration() { return registration; }
	public void setRegistration(String registration) { this.registration = registration; }

	public int getQtity() { return qtity; }
	public void setQtity(int qtity) { this.qtity = qtity; }

	public String getPercentage() { return percentage; }
	public void setPercentage(String percentage) { this.percentage = percentage; }

	public ParentRegisterType withRegistration(String registration){ this.setRegistration(registration); return this; }
	public ParentRegisterType withQtity(int qtity){ this.setQtity(qtity); return this; }
	public ParentRegisterType withPercentage(String percentage){ this.setPercentage(percentage); return this; }

	@Override
	public String toString() {
		return "RegisterType [registration=" + registration + ", qtity=" + qtity + ", percentage=" + percentage + "]";
	}

}
