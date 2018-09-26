package com.scc.lofselectclub.model;

import javax.persistence.*;

@Entity
@Table(name = "ls_serie_definition")
public class SerieDefinition {

	@Id
	@Column(name = "id_serie")
	Integer idSerie;
	@Column(name = "min_value")
	Integer minValue;
	@Column(name = "max_value")
	Integer maxValue;
	@Column(name = "lib_serie")
	String libelle;
	@Column(name = "sequence")
	int sequence;
	@Column(name = "id_serie_group")
	Integer idSerieGroup;

	public Integer getIdSerie() {
		return idSerie;
	}

	public void setIdSerie(Integer idSerie) {
		this.idSerie = idSerie;
	}

	public Integer getMinValue() {
		return minValue;
	}

	public void setMinValue(Integer minValue) {
		this.minValue = minValue;
	}

	public Integer getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(Integer maxValue) {
		this.maxValue = maxValue;
	}

	public String getLibelle() {
		return libelle;
	}

	public void setLibelle(String libelle) {
		this.libelle = libelle;
	}

	public Integer getSequence() {
		return sequence;
	}

	public void setSequence(Integer sequence) {
		this.sequence = sequence;
	}

	public Integer getIdSerieGroup() {
		return idSerieGroup;
	}

	public void setIdSerieGroup(Integer idSerieGroup) {
		this.idSerieGroup = idSerieGroup;
	}

}
