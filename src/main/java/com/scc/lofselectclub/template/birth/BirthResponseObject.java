package com.scc.lofselectclub.template.birth;

import java.util.List;

import com.scc.lofselectclub.template.birth.BirthBreed;

import io.swagger.annotations.ApiModelProperty;

public class BirthResponseObject {

	@ApiModelProperty(notes = "The Total of births", position = 1, required = true)
	int size;

	@ApiModelProperty(notes = "The list of births", position = 2, required = true, allowEmptyValue = true)
	List<BirthBreed> breeds;

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public List<BirthBreed> getBreeds() {
		return breeds;
	}

	public void setBreeds(List<BirthBreed> breeds) {
		this.breeds = breeds;
	}

	public BirthResponseObject withSize(int size) {
		this.setSize(size);
		return this;
	}

	public BirthResponseObject withBreeds(List<BirthBreed> breeds) {
		this.setBreeds(breeds);
		return this;
	}

	public BirthResponseObject() {
		super();
	}

	public BirthResponseObject(int size, List<BirthBreed> breeds) {
		super();
		this.size = size;
		this.breeds = breeds;
	}

	@Override
	public String toString() {
		return "ResponseObject [size=" + size + ", breeds=" + breeds + "]";
	}

}
