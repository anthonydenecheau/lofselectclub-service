package com.scc.lofselectclub.template.consanguinity;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;

public class ConsanguinityVariety {

	@ApiModelProperty(notes = "variety id", position = 1, allowEmptyValue = true)
	int id;

	@ApiModelProperty(notes = "variety name", position = 2, allowEmptyValue = true)
	String name;

	@ApiModelProperty(notes = "coefficient of consanguinity", position = 3, allowEmptyValue = true)
	String cng;

	@ApiModelProperty(notes = "number of mate by common ancestors", position = 4, allowEmptyValue = true)
	List<ConsanguintyCommonAncestor> litterByCommonAncestor;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCng() {
		return cng;
	}

	public void setCng(String cng) {
		this.cng = cng;
	}

	public List<ConsanguintyCommonAncestor> getLitterByCommonAncestor() {
		return litterByCommonAncestor;
	}

	public void setLitterByCommonAncestor(List<ConsanguintyCommonAncestor> litterByCommonAncestor) {
		this.litterByCommonAncestor = litterByCommonAncestor;
	}

	public ConsanguinityVariety withId(int id) {
		this.setId(id);
		return this;
	}

	public ConsanguinityVariety withName(String name) {
		this.setName(name);
		return this;
	}

	public ConsanguinityVariety withCng(String cng) {
		this.setCng(cng);
		return this;
	}

	public ConsanguinityVariety withLitterByCommonAncestor(List<ConsanguintyCommonAncestor> Litter) {
		this.setLitterByCommonAncestor(Litter);
		return this;
	}

}
