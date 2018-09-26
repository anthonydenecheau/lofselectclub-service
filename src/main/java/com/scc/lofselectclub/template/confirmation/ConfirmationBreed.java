package com.scc.lofselectclub.template.confirmation;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;

public class ConfirmationBreed {

	@ApiModelProperty(notes = "Breed id", position = 1, allowEmptyValue = true)
	int id;

	@ApiModelProperty(notes = "Breed name", position = 2, allowEmptyValue = true)
	String name;

	@ApiModelProperty(notes = "Breed statistics", position = 3, allowEmptyValue = true)
	List<ConfirmationBreedStatistics> statistics;

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

	public List<ConfirmationBreedStatistics> getStatistics() {
		return statistics;
	}

	public void setStatistics(List<ConfirmationBreedStatistics> statistics) {
		this.statistics = statistics;
	}

	public ConfirmationBreed withId(int id) {
		this.setId(id);
		return this;
	}

	public ConfirmationBreed withName(String name) {
		this.setName(name);
		return this;
	}

	public ConfirmationBreed withStatistics(List<ConfirmationBreedStatistics> statistics) {
		this.setStatistics(statistics);
		return this;
	}

	@Override
	public String toString() {
		return "Breed [id=" + id + ", name=" + name + ", statistics=" + statistics + "]";
	}

}
