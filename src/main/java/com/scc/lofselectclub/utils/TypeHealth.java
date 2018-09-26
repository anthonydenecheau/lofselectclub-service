package com.scc.lofselectclub.utils;

public enum TypeHealth {

	SUIVI(1), SOUS_SURVEILLANCE(2), EMERGENTE(3), GENE_INTERET(4);

	private int value;

	public int getValue() {
		return value;
	}

	private TypeHealth(int value) {
		this.value = value;
	}

	public static TypeHealth fromId(int id) {
		for (TypeHealth type : values()) {
			if (type.getValue() == id) {
				return type;
			}
		}
		return null;
	}

	// public static TypeHealth fromValue(String value) throws
	// EnumValidationException {
	//
	// if(value == null) {
	// throw new EnumValidationException(value, "TypeHealth");
	// }
	//
	// for (TypeHealth category : values()) {
	// if (category.value.equalsIgnoreCase(value)) {
	// return category;
	// }
	// }
	// throw new EnumValidationException(value, "TypeHealth");
	// }

}
