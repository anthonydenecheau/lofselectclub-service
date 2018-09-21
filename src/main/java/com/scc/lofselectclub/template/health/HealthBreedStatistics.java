package com.scc.lofselectclub.template.health;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;

public class HealthBreedStatistics {

	@ApiModelProperty(notes = "year", position = 1, allowEmptyValue=true)
	int year;
	
	@ApiModelProperty(notes = "health tests by type", position = 2, allowEmptyValue=true)
	List<HealthType> healthType;
	
	public int getYear() { return year; }
	public void setYear(int year) { this.year = year; }
	
	public List<HealthType> getHealthType() { return healthType; }
	public void setHealthType(List<HealthType> healthType) { this.healthType = healthType; }
	
	public HealthBreedStatistics withYear(int year){ this.setYear(year); return this; }
	public HealthBreedStatistics withHealthType(List<HealthType> healthType){ this.setHealthType(healthType); return this; }
}
