package com.scc.lofselectclub.template.parent;

import java.util.List;
import java.util.Map;

import io.swagger.annotations.ApiModelProperty;

public class ParentVariety {

	@ApiModelProperty(notes = "variety id", position = 1, allowEmptyValue=true)
	int id;

	@ApiModelProperty(notes = "variety name", position = 2, allowEmptyValue=true)
	String name;

	@ApiModelProperty(dataType = "com.scc.lofselectclub.template.swaggerType.Origin", notes = "origin by gender", position = 3, allowEmptyValue=true)
	List<Map<String, List<ParentGender>>> origins;

	@ApiModelProperty(notes = "cotations", position = 4, allowEmptyValue=true)
	List<ParentCotation> cotations;
	
	@ApiModelProperty(notes = "frequency of use", position = 5, allowEmptyValue=true)
	List<ParentFrequency> frequencies;
	
	public int getId() { return id; }
	public void setId(int id) { this.id = id; }
	
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }

	public List<Map<String, List<ParentGender>>> getOrigins() { return origins; }
	public void setOrigins(List<Map<String, List<ParentGender>>> origins) { this.origins = origins; }

	public List<ParentCotation> getCotations() { return cotations; }
	public void setCotations(List<ParentCotation> cotations) { this.cotations = cotations; }

	public List<ParentFrequency> getFrequencies() { return frequencies; }
	public void setFrequencies(List<ParentFrequency> frequencies) { this.frequencies = frequencies; }

	public ParentVariety withId(int id){ this.setId(id); return this; }
	public ParentVariety withName(String name){ this.setName(name); return this; }
	public ParentVariety withOrigins(List<Map<String, List<ParentGender>>> origins){ this.setOrigins(origins); return this; }
	public ParentVariety withCotations(List<ParentCotation> cotations){ this.setCotations(cotations); return this; }
	public ParentVariety withFrequencies(List<ParentFrequency> frequencies){ this.setFrequencies(frequencies); return this; }

}
