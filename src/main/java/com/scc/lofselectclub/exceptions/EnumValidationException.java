package com.scc.lofselectclub.exceptions;

public class EnumValidationException extends Exception {

	private static final long serialVersionUID = 1L;
	private String enumValue = null;
	private String enumName = null;

	public String getEnumValue() {
		return enumValue;
	}

	public void setEnumValue(String enumValue) {
		this.enumValue = enumValue;
	}

	public String getEnumName() {
		return enumName;
	}

	public void setEnumName(String enumName) {
		this.enumName = enumName;
	}

	public EnumValidationException(String enumValue, String enumName) {
		super(enumValue);

		this.enumValue = enumValue;
		this.enumName = enumName;
	}

	public EnumValidationException(String enumValue, String enumName, Throwable cause) {
		super(enumValue, cause);

		this.enumValue = enumValue;
		this.enumName = enumName;
	}

}
