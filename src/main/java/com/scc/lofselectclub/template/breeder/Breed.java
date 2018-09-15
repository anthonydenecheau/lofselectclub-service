package com.scc.lofselectclub.template.breeder;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;

public class Breed {

	@ApiModelProperty(notes = "Breed id", position = 1, allowEmptyValue=true)
	int id;

	@ApiModelProperty(notes = "Breed name", position = 2, allowEmptyValue=true)
	String name;
	
	@ApiModelProperty(notes = "Breed statistics", position = 3, allowEmptyValue=true)
	List<BreedStatistics> statistics;

	@ApiModelProperty(notes = "Affixes topN", position = 4, allowEmptyValue=true)
	List<AffixStatistics> topN;

	public int getId() { return id; }
	public void setId(int id) { this.id = id; }

	public String getName() { return name; }
	public void setName(String name) { this.name = name; }

	public List<BreedStatistics> getStatistics() { return statistics; }
	public void setStatistics(List<BreedStatistics> statistics) { this.statistics = statistics; }

	public List<AffixStatistics> getTopN() { return topN; }
	public void setTopN(List<AffixStatistics> topN) { this.topN = topN; }

	public Breed withId(int id){ this.setId(id); return this; }
	public Breed withName(String name){ this.setName(name); return this; }
	public Breed withStatistics(List<BreedStatistics> statistics){ this.setStatistics(statistics); return this; }
	public Breed withTopN(List<AffixStatistics> topN){ this.setTopN(topN); return this; }

	@Override
	public String toString() {
		return "RaceObject [id=" + id + ", name=" + name + ", statistics=" + statistics + ", topN=" + topN + "]";
	}

}
