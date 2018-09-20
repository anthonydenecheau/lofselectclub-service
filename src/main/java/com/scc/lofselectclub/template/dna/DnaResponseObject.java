package com.scc.lofselectclub.template.dna;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;

public class DnaResponseObject {

	@ApiModelProperty(notes = "The Total of breeds", position = 1, required = true)
	int size;

	@ApiModelProperty(notes = "The list of breeds", position = 2, required = true, allowEmptyValue=true)
	List<DnaBreed> breeds;
	
	public int getSize() { return size; }
	public void setSize(int size) { this.size = size; }

	public List<DnaBreed> getBreeds() { return breeds; }
	public void setBreeds(List<DnaBreed> breeds) { this.breeds = breeds; }

	public DnaResponseObject withSize(int size){ this.setSize(size); return this; }
	public DnaResponseObject withBreeds(List<DnaBreed> breeds){ this.setBreeds(breeds); return this; }

	public DnaResponseObject() {
		super();
	}

	public DnaResponseObject(int size, List<DnaBreed> breeds) {
		super();
		this.size = size;
		this.breeds = breeds;
	}
	
	@Override
	public String toString() {
		return "ResponseObject [size=" + size + ", breeds=" + breeds + "]";
	}
	
	
}
