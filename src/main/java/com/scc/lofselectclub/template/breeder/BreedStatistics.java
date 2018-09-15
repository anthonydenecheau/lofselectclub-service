package com.scc.lofselectclub.template.breeder;

import java.util.List;
import java.util.Map;

import io.swagger.annotations.ApiModelProperty;

public class BreedStatistics {

	@ApiModelProperty(notes = "year", position = 1, allowEmptyValue=true)
	int year;

	@ApiModelProperty(notes = "number of breeders", position = 2, allowEmptyValue=true)
	int qtity;
	
	@ApiModelProperty(notes = "detail by series", position = 3, allowEmptyValue=true)
	List<Map<String, Object>> series;
	
	@ApiModelProperty(notes = "detail by variety", position = 4, allowEmptyValue=true)	
	List<Variety> variety;
	
	@ApiModelProperty(notes = "detail by months", position = 5, allowEmptyValue=true)
	List<MonthStatistics> months;
	
	public int getYear() { return year; }
	public void setYear(int year) { this.year = year; }
	
	public int getQtity() { return qtity; }
	public void setQtity(int qtity) { this.qtity = qtity; }

	public List<Map<String, Object>> getSeries() { return series; }
	public void setSeries(List<Map<String, Object>> series) { this.series = series; }
	
	public List<Variety> getVariety() { return variety; }
	public void setVariety(List<Variety> variety) { this.variety = variety; }

	public List<MonthStatistics> getMonths() { return months; }
	public void setMonths(List<MonthStatistics> months) { this.months = months; }

	public BreedStatistics withYear(int year){ this.setYear(year); return this; }
	public BreedStatistics withQtity(int qtity){ this.setQtity(qtity); return this; }
	public BreedStatistics withSeries(List<Map<String, Object>> series){ this.setSeries(series); return this; }
	public BreedStatistics withVariety(List<Variety> variety){ this.setVariety(variety); return this; }
	public BreedStatistics withMonths(List<MonthStatistics> months){ this.setMonths(months); return this; }

	@Override
	public String toString() {
		return "BreedStatistics [year=" + year + ",qtity=" + qtity + ", series=" + series + ", variety="
				+ variety + ", months=" + months + "]";
	}

}
