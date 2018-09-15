package com.scc.lofselectclub.template.breeder;

import java.util.List;
import java.util.Map;

import io.swagger.annotations.ApiModelProperty;

public class MonthStatistics {

	@ApiModelProperty(notes = "month", position = 1, allowEmptyValue=true)
	int month;

	@ApiModelProperty(notes = "number of breeders", position = 2, allowEmptyValue=true)
	int qtity;
	
	@ApiModelProperty(notes = "detail by series", position = 3, allowEmptyValue=true)
	List<Map<String, Object>> series;
	
	@ApiModelProperty(notes = "detail by variety", position = 5, allowEmptyValue=true)
	List<Variety> variety;
	
	public int getMonth() { return month; }
	public void setMonth(int month) { this.month = month; }
	
	public int getQtity() { return qtity; }
	public void setQtity(int qtity) { this.qtity = qtity; }

	public List<Map<String, Object>> getSeries() { return series; }
	public void setSeries(List<Map<String, Object>> series) { this.series = series; }
	
	public List<Variety> getVariety() { return variety; }
	public void setVariety(List<Variety> variety) { this.variety = variety; }

	public MonthStatistics withMonth(int month){ this.setMonth(month); return this; }
	public MonthStatistics withQtity(int qtity){ this.setQtity(qtity); return this; }
	public MonthStatistics withSeries(List<Map<String, Object>> series){ this.setSeries(series); return this; }
	public MonthStatistics withVariety(List<Variety> variety){ this.setVariety(variety); return this; }
	
	@Override
	public String toString() {
		return "MonthObject [month=" + month + ",qtity=" + qtity + ", series=" + series + ", variety=" + variety
				+ "]";
	}

}
