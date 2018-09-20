package com.scc.lofselectclub.template.health;

import java.util.List;
import java.util.Map;

import io.swagger.annotations.ApiModelProperty;

public class HealthBreedStatistics {

	@ApiModelProperty(notes = "year", position = 1, allowEmptyValue=true)
	int year;
	
	@ApiModelProperty(notes = "health test by type", position = 2, allowEmptyValue=true)
	List<Map<Integer,List<HealthTest>>> healthTest;
	
	public int getYear() { return year; }
	public void setYear(int year) { this.year = year; }
	
	public List<Map<Integer, List<HealthTest>>> getHealthTest() { return healthTest; }
	public void setHealthTest(List<Map<Integer, List<HealthTest>>> healthTest) { this.healthTest = healthTest; }
	
	public HealthBreedStatistics withYear(int year){ this.setYear(year); return this; }
	public HealthBreedStatistics withHealthTest(List<Map<Integer, List<HealthTest>>> healthTest){ this.setHealthTest(healthTest); return this; }
}
