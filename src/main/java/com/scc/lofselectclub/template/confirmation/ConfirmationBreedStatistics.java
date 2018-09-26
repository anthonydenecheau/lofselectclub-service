package com.scc.lofselectclub.template.confirmation;

import java.util.List;
import java.util.Map;

import com.scc.lofselectclub.template.confirmation.ConfirmationBreedStatistics;

import io.swagger.annotations.ApiModelProperty;

public class ConfirmationBreedStatistics {

	@ApiModelProperty(notes = "year", position = 1, allowEmptyValue = true)
	int year;

	@ApiModelProperty(notes = "number of confirmation", position = 2, allowEmptyValue = true)
	int qtity;

	@ApiModelProperty(notes = "average Height (if mandatory for the breed)", position = 3, allowEmptyValue = true)
	int avgHeight;

	@ApiModelProperty(notes = "detail by Height (if mandatory for the breed)", position = 4, allowEmptyValue = true)
	List<Map<String, Object>> series;

	@ApiModelProperty(notes = "detail by variety", position = 6, allowEmptyValue = true)
	List<ConfirmationVariety> variety;

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

	public int getAvgHeight() {
		return avgHeight;
	}

	public void setAvgHeight(int avgHeight) {
		this.avgHeight = avgHeight;
	}

	public List<Map<String, Object>> getSeries() {
		return series;
	}

	public void setSeries(List<Map<String, Object>> series) {
		this.series = series;
	}

	public List<ConfirmationVariety> getVariety() {
		return variety;
	}

	public void setVariety(List<ConfirmationVariety> variety) {
		this.variety = variety;
	}

	public ConfirmationBreedStatistics withYear(int year) {
		this.setYear(year);
		return this;
	}

	public ConfirmationBreedStatistics withQtity(int qtity) {
		this.setQtity(qtity);
		return this;
	}

	public ConfirmationBreedStatistics withAvgHeight(int avgHeight) {
		this.setAvgHeight(avgHeight);
		return this;
	}

	public ConfirmationBreedStatistics withSeries(List<Map<String, Object>> series) {
		this.setSeries(series);
		return this;
	}

	public ConfirmationBreedStatistics withVariety(List<ConfirmationVariety> variety) {
		this.setVariety(variety);
		return this;
	}

	@Override
	public String toString() {
		return "BreedStatistics [year=" + year + ", qtity=" + qtity + ", avgHeight=" + avgHeight + ", series=" + series
				+ ", variety=" + variety + "]";
	}

}
