package com.scc.lofselectclub.template.health;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;

public class HealthResponseObject {

	@ApiModelProperty(notes = "The Total of breeds", position = 1, required = true)
	int size;

	@ApiModelProperty(notes = "The list of breeds", position = 2, required = true, allowEmptyValue=true)
	List<HealthBreed> breeds;
	
	public int getSize() { return size; }
	public void setSize(int size) { this.size = size; }

	public List<HealthBreed> getBreeds() { return breeds; }
	public void setBreeds(List<HealthBreed> breeds) { this.breeds = breeds; }

	public HealthResponseObject withSize(int size){ this.setSize(size); return this; }
	public HealthResponseObject withBreeds(List<HealthBreed> breeds){ this.setBreeds(breeds); return this; }

	public HealthResponseObject() {
		super();
	}

	public HealthResponseObject(int size, List<HealthBreed> breeds) {
		super();
		this.size = size;
		this.breeds = breeds;
	}
	
	@Override
	public String toString() {
		return "ResponseObject [size=" + size + ", breeds=" + breeds + "]";
	}

}
