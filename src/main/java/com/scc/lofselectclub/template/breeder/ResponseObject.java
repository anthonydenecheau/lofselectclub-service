package com.scc.lofselectclub.template.breeder;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;

public class ResponseObject {

	@ApiModelProperty(notes = "The Total of breeds", position = 1, required = true)
	int size;

	@ApiModelProperty(notes = "The list of breeds", position = 2, required = true, allowEmptyValue=true)
	List<Breed> breeds;
	
	public int getSize() { return size; }
	public void setSize(int size) { this.size = size; }

	public List<Breed> getBreeds() { return breeds; }
	public void setBreeds(List<Breed> breeds) { this.breeds = breeds; }

	public ResponseObject withSize(int size){ this.setSize(size); return this; }
	public ResponseObject withBreeds(List<Breed> breeds){ this.setBreeds(breeds); return this; }

	public ResponseObject() {
		super();
	}

	public ResponseObject(int size, List<Breed> breeds) {
		super();
		this.size = size;
		this.breeds = breeds;
	}
	
	@Override
	public String toString() {
		return "ResponseObject [size=" + size + ", breeds=" + breeds + "]";
	}
	
	
}
