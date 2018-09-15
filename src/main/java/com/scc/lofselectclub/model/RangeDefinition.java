package com.scc.lofselectclub.model;

import javax.persistence.*;

@Entity
@Table(name = "ls_range_definition")
public class RangeDefinition {

	@Id 
    @Column(name = "id_range")
    Integer idRange;
	@Column(name = "min_value")
    Integer minValue;	
    @Column(name = "max_value")
    Integer maxValue;
    @Column(name = "lib_range")
    String libelle;
    @Column(name = "sequence")
    int sequence;
    @Column(name = "id_range_group")
    Integer idRangeGroup;
   
	public Integer getIdRange() { return idRange; }
	public void setIdRange(Integer idRange) { this.idRange = idRange; }

	public Integer getMinValue() { return minValue; }
	public void setMinValue(Integer minValue) { this.minValue = minValue; }

	public Integer getMaxValue() { return maxValue; }
	public void setMaxValue(Integer maxValue) { this.maxValue = maxValue; }

	public String getLibelle() { return libelle; }
	public void setLibelle(String libelle) { this.libelle = libelle; }

	public Integer getSequence() { return sequence; }
	public void setSequence(Integer sequence) { this.sequence = sequence; }

	public Integer getIdRangeGroup() { return idRangeGroup; }
	public void setIdRangeGroup(Integer idRangeGroup) { this.idRangeGroup = idRangeGroup; }
	
}
