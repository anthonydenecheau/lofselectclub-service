package com.scc.lofselectclub.template.breeder;

import java.util.List;
import java.util.Map;

import io.swagger.annotations.ApiModelProperty;

public class VarietyStatistics {

	@ApiModelProperty(notes = "number of breeders", position = 1, allowEmptyValue=true)
	int qtity;

	@ApiModelProperty(notes = "detail by series", position = 2, allowEmptyValue=true)
	List<Map<String, Object>> series;
	
	public int getQtity() { return qtity; }
	public void setQtity(int qtity) { this.qtity = qtity; }

	public List<Map<String, Object>> getSeries() { return series; }
	public void setSeries(List<Map<String, Object>> series) { this.series = series; }
	
	public VarietyStatistics withQtity(int qtity){ this.setQtity(qtity); return this; }
	public VarietyStatistics withSeries(List<Map<String, Object>> series){ this.setSeries(series); return this; }

	@Override
	public String toString() {
		return "VarietyStatistics [qtity=" + qtity + ",series=" + series + "]";
	}

}
