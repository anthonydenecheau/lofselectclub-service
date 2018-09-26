package com.scc.lofselectclub.template.birth;

import java.util.List;

import com.scc.lofselectclub.template.birth.BirthCotation;

import io.swagger.annotations.ApiModelProperty;

public class BirthBreedStatistics {

	@ApiModelProperty(notes = "year", position = 1, allowEmptyValue = true)
	int year;

	@ApiModelProperty(notes = "number of male", position = 2, allowEmptyValue = true)
	int numberOfMale;

	@ApiModelProperty(notes = "number of female", position = 3, allowEmptyValue = true)
	int numberOfFemale;

	@ApiModelProperty(notes = "number of puppies", position = 4, allowEmptyValue = true)
	int numberOfPuppies;

	@ApiModelProperty(notes = "total of litters", position = 5, allowEmptyValue = true)
	int totalOfLitter;

	@ApiModelProperty(notes = "prolificity", position = 6, allowEmptyValue = true)
	double prolificity;

	@ApiModelProperty(notes = "detail by variety", position = 7, allowEmptyValue = true)
	List<BirthVariety> variety;

	@ApiModelProperty(notes = "cotations", position = 8, allowEmptyValue = true)
	List<BirthCotation> cotations;

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
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

	public List<BirthVariety> getVariety() {
		return variety;
	}

	public void setVariety(List<BirthVariety> variety) {
		this.variety = variety;
	}

	public List<BirthCotation> getCotations() {
		return cotations;
	}

	public void setCotations(List<BirthCotation> cotations) {
		this.cotations = cotations;
	}

	public BirthBreedStatistics withYear(int year) {
		this.setYear(year);
		return this;
	}

	public BirthBreedStatistics withNumberOfMale(int numberOfMale) {
		this.setNumberOfMale(numberOfMale);
		return this;
	}

	public BirthBreedStatistics withNumberOfFemale(int numberOfFemale) {
		this.setNumberOfFemale(numberOfFemale);
		return this;
	}

	public BirthBreedStatistics withNumberOfPuppies(int numberOfPuppies) {
		this.setNumberOfPuppies(numberOfPuppies);
		return this;
	}

	public BirthBreedStatistics withTotalOfLitter(int totalOfLitter) {
		this.setTotalOfLitter(totalOfLitter);
		return this;
	}

	public BirthBreedStatistics withProlificity(double prolificity) {
		this.setProlificity(prolificity);
		return this;
	}

	public BirthBreedStatistics withVariety(List<BirthVariety> variety) {
		this.setVariety(variety);
		return this;
	}

	public BirthBreedStatistics withCotations(List<BirthCotation> cotations) {
		this.setCotations(cotations);
		return this;
	}

}
