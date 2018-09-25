package com.scc.lofselectclub.template.consanguinity;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;

public class ConsanguinityBreed {

	@ApiModelProperty(notes = "Breed id", position = 1, allowEmptyValue=true)
	int id;

	@ApiModelProperty(notes = "Breed name", position = 2, allowEmptyValue=true)
	String name;
	
	@ApiModelProperty(notes = "Breed statistics", position = 3, allowEmptyValue=true)
	List<ConsanguinityBreedStatistics> statistics;

	public int getId() { return id; }
	public void setId(int id) { this.id = id; }

	public String getName() { return name; }
	public void setName(String name) { this.name = name; }

	public List<ConsanguinityBreedStatistics> getStatistics() { return statistics; }
	public void setStatistics(List<ConsanguinityBreedStatistics> statistics) { this.statistics = statistics; }

	public ConsanguinityBreed withId(int id){ this.setId(id); return this; }
	public ConsanguinityBreed withName(String name){ this.setName(name); return this; }
	public ConsanguinityBreed withStatistics(List<ConsanguinityBreedStatistics> statistics){ this.setStatistics(statistics); return this; }

	@Override
	public String toString() {
		return "Breed [id=" + id + ", name=" + name + ", statistics=" + statistics + "]";
	}

}
