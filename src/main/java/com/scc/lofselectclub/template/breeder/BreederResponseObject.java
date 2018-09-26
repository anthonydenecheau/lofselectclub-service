package com.scc.lofselectclub.template.breeder;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;

public class BreederResponseObject {

	@ApiModelProperty(notes = "The Total of breeds", position = 1, required = true)
	int size;

	@ApiModelProperty(notes = "The list of breeds", position = 2, required = true, allowEmptyValue = true)
	List<BreederBreed> breeds;

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public List<BreederBreed> getBreeds() {
		return breeds;
	}

	public void setBreeds(List<BreederBreed> breeds) {
		this.breeds = breeds;
	}

	public BreederResponseObject withSize(int size) {
		this.setSize(size);
		return this;
	}

	public BreederResponseObject withBreeds(List<BreederBreed> breeds) {
		this.setBreeds(breeds);
		return this;
	}

	public BreederResponseObject() {
		super();
	}

	public BreederResponseObject(int size, List<BreederBreed> breeds) {
		super();
		this.size = size;
		this.breeds = breeds;
	}

	@Override
	public String toString() {
		return "ResponseObject [size=" + size + ", breeds=" + breeds + "]";
	}

}
