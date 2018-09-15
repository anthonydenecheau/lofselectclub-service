package com.scc.lofselectclub.template.breeder;

import java.util.List;
import java.util.Map;

import io.swagger.annotations.ApiModelProperty;

public class AffixVariety {

	@ApiModelProperty(notes = "variety id", position = 1, allowEmptyValue=true)
	int id;

	@ApiModelProperty(notes = "variety name", position = 2, allowEmptyValue=true)
	String name;

	@ApiModelProperty(notes = "top N affixe", position = 3, allowEmptyValue=true)
	List<Map<String, Object>> affixes;

	public int getId() { return id; }
	public void setId(int id) { this.id = id; }
	
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	
	public List<Map<String, Object>> getAffixes() { return affixes; }
	public void setAffixes(List<Map<String, Object>> affixes) { this.affixes = affixes; }

	public AffixVariety withId(int id){ this.setId(id); return this; }
	public AffixVariety withName(String name){ this.setName(name); return this; }
	public AffixVariety withAffixes(List<Map<String, Object>> affixes){ this.setAffixes(affixes); return this; }
	
	@Override
	public String toString() {
		return "AffixVariety [id=" + id + ", name=" + name + ", affixes=" + affixes + "]";
	}

}
