package com.scc.lofselectclub.template.breeder;

import java.util.List;
import java.util.Map;

import io.swagger.annotations.ApiModelProperty;

public class BreederMonthStatistics {

	@ApiModelProperty(notes = "month", position = 1, allowEmptyValue = true)
	int month;

	@ApiModelProperty(notes = "number of breeders", position = 2, allowEmptyValue = true)
	int qtity;

	@ApiModelProperty(dataType = "com.scc.lofselectclub.template.swaggerType.Serie", notes = "detail by series", position = 3, allowEmptyValue = true)
	List<Map<String, Object>> series;

	@ApiModelProperty(notes = "detail by variety", position = 5, allowEmptyValue = true)
	List<BreederVariety> variety;

	public int getMonth() {
		return month;
	}

	public void setMonth(int month) {
		this.month = month;
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

	public BreederMonthStatistics withMonth(int month) {
		this.setMonth(month);
		return this;
	}

	public BreederMonthStatistics withQtity(int qtity) {
		this.setQtity(qtity);
		return this;
	}

	public BreederMonthStatistics withSeries(List<Map<String, Object>> series) {
		this.setSeries(series);
		return this;
	}

	public BreederMonthStatistics withVariety(List<BreederVariety> variety) {
		this.setVariety(variety);
		return this;
	}

	@Override
	public String toString() {
		return "MonthObject [month=" + month + ",qtity=" + qtity + ", series=" + series + ", variety=" + variety + "]";
	}

}
