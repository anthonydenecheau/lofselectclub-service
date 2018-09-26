package com.scc.lofselectclub.template.birth;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;

public class BirthVariety {

	@ApiModelProperty(notes = "variety id", position = 1, allowEmptyValue = true)
	int id;

	@ApiModelProperty(notes = "variety name", position = 2, allowEmptyValue = true)
	String name;

	@ApiModelProperty(notes = "number of male", position = 3, allowEmptyValue = true)
	int numberOfMale;

	@ApiModelProperty(notes = "number of female", position = 4, allowEmptyValue = true)
	int numberOfFemale;

	@ApiModelProperty(notes = "number of puppies", position = 5, allowEmptyValue = true)
	int numberOfPuppies;

	@ApiModelProperty(notes = "total of litters", position = 6, allowEmptyValue = true)
	int totalOfLitter;

	@ApiModelProperty(notes = "prolificity", position = 7, allowEmptyValue = true)
	double prolificity;

	@ApiModelProperty(notes = "cotations", position = 8, allowEmptyValue = true)
	List<BirthCotation> cotations;

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

	public int getNumberOfMale() {
		return numberOfMale;
	}

	public void setNumberOfMale(int numberOfMale) {
		this.numberOfMale = numberOfMale;
	}

	public int getNumberOfFemale() {
		return numberOfFemale;
	}

	public void setNumberOfFemale(int numberOfFemale) {
		this.numberOfFemale = numberOfFemale;
	}

	public int getNumberOfPuppies() {
		return numberOfPuppies;
	}

	public void setNumberOfPuppies(int numberOfPuppies) {
		this.numberOfPuppies = numberOfPuppies;
	}

	public int getTotalOfLitter() {
		return totalOfLitter;
	}

	public void setTotalOfLitter(int totalOfLitter) {
		this.totalOfLitter = totalOfLitter;
	}

	public double getProlificity() {
		return prolificity;
	}

	public void setProlificity(double prolificity) {
		this.prolificity = prolificity;
	}

	public List<BirthCotation> getCotations() {
		return cotations;
	}

	public void setCotations(List<BirthCotation> cotations) {
		this.cotations = cotations;
	}

	public BirthVariety withId(int id) {
		this.setId(id);
		return this;
	}

	public BirthVariety withName(String name) {
		this.setName(name);
		return this;
	}

	public BirthVariety withNumberOfMale(int numberOfMale) {
		this.setNumberOfMale(numberOfMale);
		return this;
	}

	public BirthVariety withNumberOfFemale(int numberOfFemale) {
		this.setNumberOfFemale(numberOfFemale);
		return this;
	}

	public BirthVariety withNumberOfPuppies(int numberOfPuppies) {
		this.setNumberOfPuppies(numberOfPuppies);
		return this;
	}

	public BirthVariety withTotalOfLitter(int totalOfLitter) {
		this.setTotalOfLitter(totalOfLitter);
		return this;
	}

	public BirthVariety withProlificity(double prolificity) {
		this.setProlificity(prolificity);
		return this;
	}

	public BirthVariety withCotations(List<BirthCotation> cotations) {
		this.setCotations(cotations);
		return this;
	}

}
