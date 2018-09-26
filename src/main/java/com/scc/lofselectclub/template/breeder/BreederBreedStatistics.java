package com.scc.lofselectclub.template.breeder;

import java.util.List;
import java.util.Map;

import io.swagger.annotations.ApiModelProperty;

public class BreederBreedStatistics {

	@ApiModelProperty(notes = "year", position = 1, allowEmptyValue = true)
	int year;

	@ApiModelProperty(notes = "number of breeders", position = 2, allowEmptyValue = true)
	int qtity;

	@ApiModelProperty(dataType = "com.scc.lofselectclub.template.swaggerType.Serie", notes = "detail by series", position = 3, allowEmptyValue = true)
	List<Map<String, Object>> series;

	@ApiModelProperty(notes = "detail by variety", position = 4, allowEmptyValue = true)
	List<BreederVariety> variety;

	@ApiModelProperty(notes = "detail by months", position = 5, allowEmptyValue = true)
	List<BreederMonthStatistics> months;

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public int getQtity() {
		return qtity;
	}

	public void setQtity(int qtity) {
		this.qtity = qtity;
	}

	public List<Map<String, Object>> getSeries() {
		return series;
	}

	public void setSeries(List<Map<String, Object>> series) {
		this.series = series;
	}

	public List<BreederVariety> getVariety() {
		return variety;
	}

	public void setVariety(List<BreederVariety> variety) {
		this.variety = variety;
	}

	public List<BreederMonthStatistics> getMonths() {
		return months;
	}

	public void setMonths(List<BreederMonthStatistics> months) {
		this.months = months;
	}

	public BreederBreedStatistics withYear(int year) {
		this.setYear(year);
		return this;
	}

	public BreederBreedStatistics withQtity(int qtity) {
		this.setQtity(qtity);
		return this;
	}

	public BreederBreedStatistics withSeries(List<Map<String, Object>> series) {
		this.setSeries(series);
		return this;
	}

	public BreederBreedStatistics withVariety(List<BreederVariety> variety) {
		this.setVariety(variety);
		return this;
	}

	public BreederBreedStatistics withMonths(List<BreederMonthStatistics> months) {
		this.setMonths(months);
		return this;
	}

	@Override
	public String toString() {
		return "BreedStatistics [year=" + year + ",qtity=" + qtity + ", series=" + series + ", variety=" + variety
				+ ", months=" + months + "]";
	}

}
