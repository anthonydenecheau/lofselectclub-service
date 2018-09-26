package com.scc.lofselectclub.template.health;

import java.util.List;

import com.scc.lofselectclub.utils.TypeHealth;

import io.swagger.annotations.ApiModelProperty;

public class HealthType {

	@ApiModelProperty(notes = "type", dataType = "com.scc.lofselectclub.utils.TypeHealth", position = 1, allowEmptyValue = true)
	TypeHealth type;

	@ApiModelProperty(notes = "health tests", position = 2, allowEmptyValue = true)
	List<HealthTest> healthTest;

	public TypeHealth getType() {
		return type;
	}

	public void setType(TypeHealth type) {
		this.type = type;
	}

	public List<HealthTest> getHealthTest() {
		return healthTest;
	}

	public void setHealthTest(List<HealthTest> healthTest) {
		this.healthTest = healthTest;
	}

	public HealthType withType(TypeHealth type) {
		this.setType(type);
		return this;
	}

	public HealthType withHealthTest(List<HealthTest> healthTest) {
		this.setHealthTest(healthTest);
		return this;
	}

}
