package com.scc.lofselectclub.template.breeder;

import io.swagger.annotations.ApiModelProperty;

public class BreederVariety {

	@ApiModelProperty(notes = "variety id", position = 1, allowEmptyValue=true)
	int id;

	@ApiModelProperty(notes = "variety name", position = 2, allowEmptyValue=true)
	String name;
	
	@ApiModelProperty(notes = "variety statistics", position = 3, allowEmptyValue=true)
	BreederVarietyStatistics statistics;

	public int getId() { return id; }
	public void setId(int id) { this.id = id; }
	
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	
	public BreederVarietyStatistics getStatistics() { return statistics; }
	public void setStatistics(BreederVarietyStatistics statistics) { this.statistics = statistics; }

	public BreederVariety withId(int id){ this.setId(id); return this; }
	public BreederVariety withName(String name){ this.setName(name); return this; }
	public BreederVariety withStatistics(BreederVarietyStatistics statistics){ this.setStatistics(statistics); return this; }

	@Override
	public String toString() {
		return "Variety [id=" + id + ", name=" + name + ", statistics=" + statistics
				+ "]";
	}

}
