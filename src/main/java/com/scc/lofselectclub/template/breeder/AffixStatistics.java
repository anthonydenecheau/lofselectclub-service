package com.scc.lofselectclub.template.breeder;

import java.util.List;
import java.util.Map;

import io.swagger.annotations.ApiModelProperty;

public class AffixStatistics {

	@ApiModelProperty(notes = "year", position = 1, allowEmptyValue=true)
	int year;

	@ApiModelProperty(notes = "top N affixe", position = 2, allowEmptyValue=true)
	List<Map<String, Object>> affixes;

	@ApiModelProperty(notes = "detail by variety", position = 3, allowEmptyValue=true)	
	List<AffixVariety> variety;

	public int getYear() { return year; }
	public void setYear(int year) { this.year = year; }

	public List<Map<String, Object>> getAffixes() { return affixes; }
	public void setAffixes(List<Map<String, Object>> affixes) { this.affixes = affixes; }

	public List<AffixVariety> getVariety() { return variety; }
	public void setVariety(List<AffixVariety> variety) { this.variety = variety; }

	public AffixStatistics withYear(int year){ this.setYear(year); return this; }
	public AffixStatistics withAffixes(List<Map<String, Object>> affixes){ this.setAffixes(affixes); return this; }
	public AffixStatistics withVariety(List<AffixVariety> variety){ this.setVariety(variety); return this; }

	@Override
	public String toString() {
		return "AffixStatistics [year=" + year + ", affixes=" + affixes + ", variety=" + variety  + "]";
	}
}
