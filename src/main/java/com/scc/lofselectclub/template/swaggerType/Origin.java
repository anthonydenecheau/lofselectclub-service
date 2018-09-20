package com.scc.lofselectclub.template.swaggerType;

import java.util.List;

import com.scc.lofselectclub.template.parent.ParentGender;

import io.swagger.annotations.ApiModelProperty;

public class Origin {

	@ApiModelProperty(notes = "Gender property", position = 1, allowEmptyValue=true)
	private String gender;

	@ApiModelProperty(notes = "Detail by gender", position = 2, allowEmptyValue=true)
	private List<ParentGender> origins;

	public String getGender() { return gender; }
	public void setGender(String gender) { this.gender = gender; }

	public List<ParentGender> getOrigins() { return origins; }
	public void setOrigins(List<ParentGender> origins) { this.origins = origins; }

}
