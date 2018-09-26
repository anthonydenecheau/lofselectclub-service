package com.scc.lofselectclub.template.consanguinity;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;

public class ConsanguinityResponseObject {

	@ApiModelProperty(notes = "The Total of breeds", position = 1, required = true)
	int size;

	@ApiModelProperty(notes = "The list of breeds", position = 2, required = true, allowEmptyValue = true)
	List<ConsanguinityBreed> breeds;

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public List<ConsanguinityBreed> getBreeds() {
		return breeds;
	}

	public void setBreeds(List<ConsanguinityBreed> breeds) {
		this.breeds = breeds;
	}

	public ConsanguinityResponseObject withSize(int size) {
		this.setSize(size);
		return this;
	}

	public ConsanguinityResponseObject withBreeds(List<ConsanguinityBreed> breeds) {
		this.setBreeds(breeds);
		return this;
	}

	public ConsanguinityResponseObject() {
		super();
	}

	public ConsanguinityResponseObject(int size, List<ConsanguinityBreed> breeds) {
		super();
		this.size = size;
		this.breeds = breeds;
	}

	@Override
	public String toString() {
		return "ResponseObject [size=" + size + ", breeds=" + breeds + "]";
	}

}
