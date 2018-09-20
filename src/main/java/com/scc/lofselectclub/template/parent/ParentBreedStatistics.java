package com.scc.lofselectclub.template.parent;

import java.util.List;
import java.util.Map;

import com.scc.lofselectclub.template.parent.ParentVariety;

import io.swagger.annotations.ApiModelProperty;

public class ParentBreedStatistics {

	@ApiModelProperty(notes = "year", position = 1, allowEmptyValue=true)
	int year;

	@ApiModelProperty(dataType = "com.scc.lofselectclub.template.swaggerType.Origin", notes = "origin by gender", position = 2, allowEmptyValue=true)
	List<Map<String, List<ParentGender>>> origins;
	
	@ApiModelProperty(notes = "detail by variety", position = 3, allowEmptyValue=true)	
	List<ParentVariety> variety;	
	
	public int getYear() { return year; }
	public void setYear(int year) { this.year = year; }
	
	public List<Map<String, List<ParentGender>>> getOrigins() { return origins; }
	public void setOrigins(List<Map<String, List<ParentGender>>> origins) { this.origins = origins; }

	public List<ParentVariety> getVariety() { return variety; }
	public void setVariety(List<ParentVariety> variety) { this.variety = variety; }

	public ParentBreedStatistics withYear(int year){ this.setYear(year); return this; }
	public ParentBreedStatistics withOrigins(List<Map<String, List<ParentGender>>> origins){ this.setOrigins(origins); return this; }
	public ParentBreedStatistics withVariety(List<ParentVariety> variety){ this.setVariety(variety); return this; }

	@Override
	public String toString() {
		return "BreedStatistics [year=" + year + ", origins=" + origins + ", variety=" + variety + "]";
	}

}