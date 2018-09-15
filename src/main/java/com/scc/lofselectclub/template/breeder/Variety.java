package com.scc.lofselectclub.template.breeder;

import io.swagger.annotations.ApiModelProperty;

public class Variety {

	@ApiModelProperty(notes = "variety id", position = 1, allowEmptyValue=true)
	int id;

	@ApiModelProperty(notes = "variety name", position = 2, allowEmptyValue=true)
	String name;
	
	@ApiModelProperty(notes = "variety statistics", position = 3, allowEmptyValue=true)
	VarietyStatistics statistics;

	public int getId() { return id; }
	public void setId(int id) { this.id = id; }
	
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	
	public VarietyStatistics getStatistics() { return statistics; }
	public void setStatistics(VarietyStatistics statistics) { this.statistics = statistics; }

	public Variety withId(int id){ this.setId(id); return this; }
	public Variety withName(String name){ this.setName(name); return this; }
	public Variety withStatistics(VarietyStatistics statistics){ this.setStatistics(statistics); return this; }

	@Override
	public String toString() {
		return "Variety [id=" + id + ", name=" + name + ", statistics=" + statistics
				+ "]";
	}

}
