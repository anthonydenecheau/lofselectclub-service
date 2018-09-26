package com.scc.lofselectclub.template.consanguinity;

import io.swagger.annotations.ApiModelProperty;

public class ConsanguintyCommonAncestor {

	@ApiModelProperty(notes = "number of common ancestor", position = 1, allowEmptyValue = true)
	int numberOfCommonAncestor;

	@ApiModelProperty(notes = "number of Litter", position = 2, allowEmptyValue = true)
	int numberOfLitter;

	public int getNumberOfCommonAncestor() {
		return numberOfCommonAncestor;
	}

	public void setNumberOfCommonAncestor(int numberOfCommonAncestor) {
		this.numberOfCommonAncestor = numberOfCommonAncestor;
	}

	public int getNumberOfLitter() {
		return numberOfLitter;
	}

	public void setNumberOfLitter(int numberOfLitter) {
		this.numberOfLitter = numberOfLitter;
	}

	public ConsanguintyCommonAncestor withNumberOfCommonAncestor(int numberOfCommonAncestor) {
		this.setNumberOfCommonAncestor(numberOfCommonAncestor);
		return this;
	}

	public ConsanguintyCommonAncestor withNumberOfLitter(int numberOfLitter) {
		this.setNumberOfLitter(numberOfLitter);
		return this;
	}

}
